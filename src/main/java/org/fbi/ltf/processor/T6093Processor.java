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
 * 获取交警对账数据完成文件  /
 * 每天执行两次,可以定时10点,下午三点
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
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
//            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
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
                cbsRtnInfo.setRtnMsg("获取dzwc文件日期不能大于今天");
                return cbsRtnInfo;
            }
            String fileWCName = bankCode + "_yhdz_dzwc_" + txnDate + ".txt";
            String fileWCData = getFtpfileWCData(null, fileWCName);
            logger.info("接收dzwc文件，文件日期：" + txnDate + "文件前缀：" + fileWCName);
            if (!StringUtils.isEmpty(fileWCData)) {
                Map dataMap = new HashMap();
                dataMap.put("txnData", fileWCData);
                dataMap.put("fileName", fileWCName);
                boolean isUpload = uploadFileData(dataMap);
                if (!isUpload) {
                    logger.info("接收dzwc文件失败，对账日期：" + txnDate);
                } else {
                    this.insertdate(fileWCData, txnDate);
                }
                // 修改out 对账成功标志  前置流水
                String preActSerial = new SimpleDateFormat("yyMMddHHmmss").format(new Date());

                int i = commService.updateLVOChkActDt(txnDate, session, preActSerial);
                // 转账
                Boolean flag = transAacoutProcess(txnDate, preActSerial, request);
                // 更新控制表状态
                sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                commService.updateFsLtfSysCtl(sysClt);
            } else {
                if (sysClt.getFlag().equals("1")) {
                    // 允许跳过当前日期
                    sysClt.setChkDate(LTFTools.datePlusOneday(txnDate));
                    commService.updateFsLtfSysCtl(sysClt);
                    logger.info("dzwc文件不存在,对账日期：" + txnDate + ",允许跳过当前日期");
                } else {
                    logger.info("dzwc文件不存在,对账日期：" + txnDate + ",继续等待");
                }
            }
            session.commit();
            logger.info("接收文件结束。日期：" + txnDate);
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
            logger.info("日期：" + txnDate + " 没有综合平台缴费数据,不需要转账");
            return flag;
        } else {
            for (FsLtfTransAmt fsLtfTransAmt : ltfTransAmtList) {
                if (fsLtfTransAmt.getTotalamt().compareTo(new BigDecimal("0")) > 0) {
                    String msgBody = fsLtfTransAmt.getAreaCode() + "|" + fsLtfTransAmt.getTotalamt() + "|";
                    toa.msgBody = msgBody;
                    try {
                        Thread.sleep(1000);// 交易每次延迟一秒
                        FsLtfAcctDeal fsLtfAcctDeal = new FsLtfAcctDeal();
                        fsLtfAcctDeal.setTxDate(new SimpleDateFormat("yyMMddHH").format(new Date()));
                        fsLtfAcctDeal.setTxAmt(fsLtfTransAmt.getTotalamt());
                        fsLtfAcctDeal.setTxFlag("0"); // 不明状态
                        fsLtfAcctDeal.setEntpNo(fsLtfTransAmt.getAreaCode());  //  企业号
                        fsLtfAcctDeal.setPreActSerial(preActSerial);
                        fsLtfAcctDeal.setBranchId(dept);
                        fsLtfAcctDeal.setOperNo(operCode);
                        fsLtfAcctDeal.setOrg("网银数据清算");
                        fsLtfAcctDeal.setChkActDt(txnDate);
                        cnt = commService.insertAcctDeal(fsLtfAcctDeal);
                        if (cnt < 1) {
                            logger.info("日期：" + txnDate + " 预计流水失败");
                        }
                        String[] respondBody = null;
                        String resStr = "";
                        try {
                            resStr = new String(dataExchangeService.processThirdPartyServer((toa.toByteArray()), "GBK"), toa.txncode);
//                           String resStr = "0000000000000001|9999|0|0|369|||||";
                            respondBody = StringUtils.splitByWholeSeparatorPreserveAllTokens(resStr.substring(15), "|");
                        } catch (Exception e) {
                            logger.info("与特色通讯异常：err" + e.getMessage());
                        }
                        if (respondBody.length < 8) {
                            logger.info("特色交易异常,返回信息" + resStr);
                        }
                        String status = respondBody[0];// 交易状态
                        String repCdoe = respondBody[1];   //返回码
                        if (repCdoe != null && repCdoe.equals("000000")) {
                            cbseActSerial = respondBody[4];   //主机流水号
                            fsLtfAcctDeal.setTxFlag("2");    // 2-成功
                            fsLtfAcctDeal.setCbsActSerial(cbseActSerial);
                            cnt = commService.updateAcctDeal(fsLtfAcctDeal);
                        } else {
                            flag = false;
                            fsLtfAcctDeal.setTxFlag("1"); // 失败
                            fsLtfAcctDeal.setRemark(respondBody[2]);
//                          fsLtfAcctDeal.setCbsActSerial(cbseActSerial);
                            commService.updateAcctDeal(fsLtfAcctDeal);
                        }

                    } catch (Exception e) {
                        flag = false;
                        logger.info("清算异常：" + e.getMessage());
                        continue;
                    }
                } else {
                    logger.info("企业号：" + fsLtfTransAmt.getAreaCode() + "转账金额为" + fsLtfTransAmt.getTotalamt() + "不需要转账");
                }
            }
        }
        return flag;
    }

}
