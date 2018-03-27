package org.fbi.ltf.processor;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6093Request.CbsTia6093;
import org.fbi.ltf.domain.tps.StaringLengthProtocol;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.*;
import org.fbi.ltf.repository.dao.FsLtfSysCltMapper;
import org.fbi.ltf.repository.dao.FsLtfVchDzwcMapper;
import org.fbi.ltf.repository.model.*;
import org.fbi.ltf.repository.model.common.FsLtfTransAmt;
import org.fbi.ltf.service.CommService;
import org.fbi.ltf.service.DataExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ��ȡ����������������ļ�  /
 * ÿ��ִ������,���Զ�ʱ10��,��������
 * Created by Thinkpad on 2015/11/3.
 */
public class T6093Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;
    private CommService commService = new CommService();
    private DataExchangeService dataExchangeService;

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
            cbsRtnInfo = processTxn(tia, request);
//            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6093 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
            String txnDate = "";
            FsLtfSysClt sysClt = commService.selectFsLtfSysCtl("01");
            if (!StringUtils.isEmpty(tia.getTxnDate())) {
                // 01 -dzwc
                txnDate = sysClt.getChkDate();
            } else {
                txnDate = tia.getTxnDate();
            }
            if(txnDate.compareTo(new SimpleDateFormat("yyyyMMdd").format(new Date()))>0){
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg("��ȡdzwc�ļ����ڲ��ܴ��ڽ���");
                return cbsRtnInfo;
            }
            String fileWCName = bankCode + "_yhdz_dzwc_" + txnDate + ".txt";
            String fileWCData = getFtpfileWCData(null, fileWCName);
            logger.info("����dzwc�ļ����ļ����ڣ�" + txnDate + "�ļ�ǰ׺��" + fileWCName);
            if (!StringUtils.isEmpty(fileWCData)) {
                Map dataMap = new HashMap();
                dataMap.put("txnData", fileWCData);
                dataMap.put("fileName", fileWCName);
                boolean isUpload = uploadFileData(dataMap);
                if (!isUpload) {
                    logger.info("����dzwc�ļ�ʧ�ܣ��������ڣ�" + txnDate);
                } else {
                    this.insertdate(fileWCData, txnDate);
                }
                // �޸�out ���˳ɹ���־  ǰ����ˮ
                String preActSerial = new SimpleDateFormat("yyMMddHHmmss").format(new Date());

                int i = commService.updateLVOChkActDt(txnDate, session, preActSerial);
                // ת��
                Boolean flag = transAacoutProcess(txnDate, preActSerial, request);
                // ���¿��Ʊ�״̬
                sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                commService.updateFsLtfSysCtl(sysClt);
            } else {
                if (sysClt.getFlag().equals("1")) {
                    // ����������ǰ����
                    sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                    commService.updateFsLtfSysCtl(sysClt);
                    logger.info("dzwc�ļ�������,�������ڣ�" + txnDate + ",����������ǰ����");
                } else {
                    logger.info("dzwc�ļ�������,�������ڣ�" + txnDate + ",�����ȴ�");
                }
            }
            session.commit();
            logger.info("�����ļ����������ڣ�" + txnDate);
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

    public int insertdate(String ftpdata, String txndate) throws IOException {
        String[] lines = StringUtils.splitByWholeSeparatorPreserveAllTokens(ftpdata, "#&#");
        int i = 0;
        FsLtfVchDzwcMapper fsLtfVchDzwcMapper = session.getMapper(FsLtfVchDzwcMapper.class);
        FsLtfVchDzwcExample example = new FsLtfVchDzwcExample();
        example.createCriteria().andChkActDtEqualTo(txndate);
        fsLtfVchDzwcMapper.deleteByExample(example);
        for (String line : lines) {
            i++;
            line = line + "|";
            String[] maininfo = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "|");
            FsLtfVchDzwc fsLtfVchDzwc = new FsLtfVchDzwc();
            fsLtfVchDzwc.setAppName(maininfo[0]);
            fsLtfVchDzwc.setOrderNo(maininfo[1]);
            fsLtfVchDzwc.setTransTime(maininfo[2]);
            fsLtfVchDzwc.setAmount(new BigDecimal(maininfo[3]));
            fsLtfVchDzwc.setEditState(maininfo[4]);
            fsLtfVchDzwc.setEditFlag(maininfo[5]);
            fsLtfVchDzwc.setNode(maininfo[6]);
            fsLtfVchDzwc.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
            fsLtfVchDzwc.setOperdatetime(new SimpleDateFormat("yyyyMMddHHmmsss").format(new Date()));
            fsLtfVchDzwc.setChkActDt(txndate);
            fsLtfVchDzwcMapper.insertSelective(fsLtfVchDzwc);
        }
//        session.commit();
        return i;
    }

    public Boolean transAacoutProcess(String txnDate, String preActSerial, Stdp10ProcessorRequest request) {
        List<FsLtfTransAmt> ltfTransAmtList = commService.selectNetAmt(txnDate);
        StaringLengthProtocol toa = new StaringLengthProtocol();
        toa.txncode = "69093";
        String cbseActSerial;
        String operCode =request!=null? request.getHeader("tellerId"):"9999";
        String dept = request!=null?request.getHeader("branchId"):"9999";
        Boolean flag = true;
        int cnt = -1;
        if (ltfTransAmtList.size() == 0) {
            logger.info("���ڣ�" + txnDate + " û���ۺ�ƽ̨�ɷ�����,����Ҫת��");
            return flag;
        } else {
            for (FsLtfTransAmt fsLtfTransAmt : ltfTransAmtList) {
                if (fsLtfTransAmt.getTotalamt().compareTo(new BigDecimal("0")) > 0) {
                    String msgBody = fsLtfTransAmt.getAreaCode() + "|" + fsLtfTransAmt.getTotalamt() + "|";
                    toa.msgBody = msgBody;
                    try {
                        Thread.sleep(1000);// ����ÿ���ӳ�һ��
                        FsLtfAcctDeal fsLtfAcctDeal = new FsLtfAcctDeal();
                        fsLtfAcctDeal.setTxDate(new SimpleDateFormat("yyMMddHH").format(new Date()));
                        fsLtfAcctDeal.setTxAmt(fsLtfTransAmt.getTotalamt());
                        fsLtfAcctDeal.setTxFlag("0"); // ����״̬
                        fsLtfAcctDeal.setEntpNo(fsLtfTransAmt.getAreaCode());  //  ��ҵ��
                        fsLtfAcctDeal.setPreActSerial(preActSerial);
                        fsLtfAcctDeal.setBranchId(dept);
                        fsLtfAcctDeal.setOperNo(operCode);
                        fsLtfAcctDeal.setOrg("������������");
                        fsLtfAcctDeal.setChkActDt(txnDate);
                        cnt = commService.insertAcctDeal(fsLtfAcctDeal);
                        if (cnt < 1) {
                            logger.info("���ڣ�" + txnDate + " Ԥ����ˮʧ��");
                        }
                        String[] respondBody = null;
                        String resStr = "";
                        try {
                            resStr = new String(dataExchangeService.processThirdPartyServer((toa.toByteArray()), "GBK"), toa.txncode);
//                           String resStr = "0000000000000001|9999|0|0|369|||||";
                            respondBody = StringUtils.splitByWholeSeparatorPreserveAllTokens(resStr.substring(15), "|");
                        } catch (Exception e) {
                            logger.info("����ɫͨѶ�쳣��err" + e.getMessage());
                        }
                        if (respondBody.length < 8) {
                            logger.info("��ɫ�����쳣,������Ϣ" + resStr);
                        }
                        String status = respondBody[0];// ����״̬
                        String repCdoe = respondBody[1];   //������
                        if (repCdoe != null && repCdoe.equals("000000")) {
                            cbseActSerial = respondBody[4];   //������ˮ��
                            fsLtfAcctDeal.setTxFlag("2");    // 2-�ɹ�
                            fsLtfAcctDeal.setCbsActSerial(cbseActSerial);
                            cnt = commService.updateAcctDeal(fsLtfAcctDeal);
                        } else {
                            flag = false;
                            fsLtfAcctDeal.setTxFlag("1"); // ʧ��
                            fsLtfAcctDeal.setRemark(respondBody[2]);
//                          fsLtfAcctDeal.setCbsActSerial(cbseActSerial);
                            commService.updateAcctDeal(fsLtfAcctDeal);
                        }

                    } catch (Exception e) {
                        flag = false;
                        logger.info("�����쳣��" + e.getMessage());
                        continue;
                    }
                } else {
                    logger.info("��ҵ�ţ�" + fsLtfTransAmt.getAreaCode() + "ת�˽��Ϊ" + fsLtfTransAmt.getTotalamt() + "����Ҫת��");
                }
            }
        }
        return flag;
    }

}
