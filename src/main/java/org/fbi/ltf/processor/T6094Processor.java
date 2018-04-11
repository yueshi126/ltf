package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6093Request.CbsTia6093;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FtpClientLTF;
import org.fbi.ltf.helper.LTFTools;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfVchDzycMapper;
import org.fbi.ltf.repository.model.*;
import org.fbi.ltf.service.CommService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ��ȡ�������������쳣�ļ�
 * Created by Thinkpad on 2015/11/3.
 */
public class T6094Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;
    private CommService commService = new CommService();

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6093 tia = new CbsTia6093();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6093) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6093");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia);
//            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6093 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //  �쳣����3
            String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
            String txnDate = tia.getTxnDate();
            String dzycid = ProjectConfigManager.getInstance().getProperty("ltf.sysid.dzyc");
            FsLtfSysClt sysClt = commService.selectFsLtfSysCtl(dzycid);
            if (StringUtils.isEmpty(tia.getTxnDate())) {
                // 02-dzyc
                txnDate = sysClt.getChkDate();
            } else {
                txnDate = tia.getTxnDate();
            }
            String fileYCName = bankCode + "_yhdz_dzyc_" + txnDate + ".txt";
            String fileYCData = getFtpfileWCData(null, fileYCName);
            if (txnDate.compareTo(new SimpleDateFormat("yyyyMMdd").format(new Date())) > 0) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("��ȡdzyc�ļ����ڲ��ܴ��ڽ���");
                return cbsRtnInfo;
            }
            logger.info("����dzyc�ļ����ļ����ڣ�" + txnDate + "�ļ�ǰ׺��" + fileYCName);
            if (!StringUtils.isEmpty(fileYCData)) {
                Map dataMap = new HashMap();
                dataMap.put("txnData", fileYCData);
                dataMap.put("fileName", fileYCName);
                boolean isUpload = uploadFileData(dataMap);
                if (!isUpload) {
                    logger.info("����dzyc�ļ�ʧ�ܣ��������ڣ�" + txnDate);
                } else {
                    this.insertdate(fileYCData);
                }
                sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                commService.updateFsLtfSysCtl(sysClt);
            } else {
                logger.info("ftp������dzyc�ļ����������ڣ�" + txnDate);
                if (sysClt.getFlag().equals("1")) {
                    // ����������ǰ����
                    sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                    commService.updateFsLtfSysCtl(sysClt);
                    logger.info("dzwc�ļ�������,�������ڣ�" + txnDate + ",����������ǰ����");
                } else {
                    logger.info("dzwc�ļ�������,�������ڣ�" + txnDate + ",�����ȴ�");
                }
            }
            logger.info("�����ļ��ɹ�,���ڣ�" + txnDate);
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } catch (Exception e) {
            logger.info("����ʧ��" + e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle());
            session.rollback();
            return cbsRtnInfo;

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean uploadFileData(Map dataMap) {
        boolean uploadFlag = false;
        try {
            String fileName = (String) dataMap.get("fileName");
            String uploadFileData = (String) dataMap.get("txnData");
            String uploadFilePath = ProjectConfigManager.getInstance().getProperty("ccbyhdz.ftp.local.file.path");
            if (uploadLocalFileData(uploadFilePath, fileName, uploadFileData)) {
                uploadFlag = true;
            } else {
                uploadFlag = false;
            }
        } catch (IOException e) {
            return false;
        }
        return uploadFlag;
    }

    public boolean uploadLocalFileData(String localPath, String fileName, String fileData) throws IOException {

        File file = new File(localPath, fileName);
        if (file.exists()) {
            file.delete();
        } else {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData.getBytes());
        fos.flush();
        fos.close();
        return true;
    }

    public String getFtpfileWCData(String dir, String fileName) throws IOException {
        String ftpIp = ProjectConfigManager.getInstance().getProperty("tps.ftp.ccbyhdz.ip");
        String user = ProjectConfigManager.getInstance().getProperty("tps.ftp.ccbyhdz.userid");
        String password = ProjectConfigManager.getInstance().getProperty("tps.ftp.ccbyhdz.password");
        FtpClientLTF FtpClientLTF = new FtpClientLTF(ftpIp, user, password);
        String data = FtpClientLTF.readFile(null, fileName);
        FtpClientLTF.logout();
        return data;
    }


    public int insertdate(String ftpdata) throws IOException {
        String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(ftpdata, "#&#");
        int i = 0;
        FsLtfVchDzycMapper fsLtfVchDzwcMapper = session.getMapper(FsLtfVchDzycMapper.class);
        for (String line : lines) {
            i++;
            line = line + "|";
            String[] maininfo = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "|");
            FsLtfVchDzyc fsLtfVchDzyc = new FsLtfVchDzyc();
            fsLtfVchDzyc.setAppname(maininfo[0]);
            fsLtfVchDzyc.setOrderno(maininfo[1]);
            fsLtfVchDzyc.setTranstime(maininfo[2]);
            fsLtfVchDzyc.setAmount(new BigDecimal(maininfo[3]));
            fsLtfVchDzyc.setLedgerstate(maininfo[4]);
            fsLtfVchDzyc.setNode(maininfo[5]);
            fsLtfVchDzyc.setOperdate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            fsLtfVchDzyc.setOperdatetime(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//            infos.add(fsLtfVchDzwc);
            fsLtfVchDzwcMapper.insertSelective(fsLtfVchDzyc);
        }
        session.commit();
        return i;
    }

}
