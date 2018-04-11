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
 * 获取交警对账数据异常文件
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
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia);
//            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6093 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //  异常数据3
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
                cbsRtnInfo.setRtnMsg("获取dzyc文件日期不能大于今天");
                return cbsRtnInfo;
            }
            logger.info("接收dzyc文件，文件日期：" + txnDate + "文件前缀：" + fileYCName);
            if (!StringUtils.isEmpty(fileYCData)) {
                Map dataMap = new HashMap();
                dataMap.put("txnData", fileYCData);
                dataMap.put("fileName", fileYCName);
                boolean isUpload = uploadFileData(dataMap);
                if (!isUpload) {
                    logger.info("接收dzyc文件失败，对账日期：" + txnDate);
                } else {
                    this.insertdate(fileYCData);
                }
                sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                commService.updateFsLtfSysCtl(sysClt);
            } else {
                logger.info("ftp不存在dzyc文件，对账日期：" + txnDate);
                if (sysClt.getFlag().equals("1")) {
                    // 允许跳过当前日期
                    sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                    commService.updateFsLtfSysCtl(sysClt);
                    logger.info("dzwc文件不存在,对账日期：" + txnDate + ",允许跳过当前日期");
                } else {
                    logger.info("dzwc文件不存在,对账日期：" + txnDate + ",继续等待");
                }
            }
            logger.info("接收文件成功,日期：" + txnDate);
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } catch (Exception e) {
            logger.info("接收失：" + e.getMessage());
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
