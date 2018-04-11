package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6092Request.CbsTia6092;
import org.fbi.ltf.domain.tps.StaringLengthProtocol;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.model.FsLtfAcctDeal;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;
import org.fbi.ltf.repository.model.common.FsLtfTransAmt;
import org.fbi.ltf.service.CommService;
import org.fbi.ltf.service.DataExchangeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ���潻��ת��
 * Created by ZZP_YY on 2018-03-26.
 */
public class T6092Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSession session = null;
    private SqlSessionFactory sqlSessionFactory = null;
    private DataExchangeService dataExchangeService = new DataExchangeService();
    private CommService commService = new CommService();

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6092 tia = new CbsTia6092();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6092) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6092");
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

    public CbsRtnInfo processTxn(CbsTia6092 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        // Ĭ�ϻ�ȡ���������ת��
        String toDay = new SimpleDateFormat("yyyyMMdd").format(new Date());

        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            String txnDate = tia.getTxnDate() != null ? tia.getTxnDate() : toDay;
            String preActSerial = "92" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + txnDate;
            int i = commService.updateTickActDt(txnDate, session, preActSerial);
            Boolean flag = transAacoutProcess(txnDate, preActSerial, request);
            if (flag) {
                i = commService.updateTickUpload(txnDate, session, preActSerial);
            }
            //���ش���
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } catch (Exception e) {
            session.rollback();
            logger.info("��������ʧ��1��", e);
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public Boolean transAacoutProcess(String txnDate, String preActSerial, Stdp10ProcessorRequest request) {
        List<FsLtfTransAmt> ltfTransAmtList = commService.selectCounterAmt(txnDate);
        StaringLengthProtocol toa = new StaringLengthProtocol();
        toa.txncode = ProjectConfigManager.getInstance().getProperty("ltf.transfer.txCode");
        toa.serialNo = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
        String cbseActSerial;
        String operCode = request != null ? request.getHeader("tellerId") : "9999";
        String dept = request != null ? request.getHeader("branchId") : "9999";
        Boolean flag = true;
        int cnt = -1;
        if (ltfTransAmtList.size() == 0) {
            logger.info("���ڣ�" + txnDate + " û���ۺ�ƽ̨�ɷ�����,����Ҫת��");
            return flag;
        } else {
            for (FsLtfTransAmt fsLtfTransAmt : ltfTransAmtList) {
                if (fsLtfTransAmt.getTotalamt().compareTo(new BigDecimal("0")) > 0) {
                    String msgBody = fsLtfTransAmt.getAreaCode() + "|" + preActSerial + "|" + fsLtfTransAmt.getTotalamt() + "|";
                    toa.msgBody = msgBody;
                    try {

                        Thread.sleep(1000);// ����ÿ���ӳ�һ��
                        FsLtfAcctDeal fsLtfAcctDeal = new FsLtfAcctDeal();
                        fsLtfAcctDeal.setTxDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                        fsLtfAcctDeal.setTxAmt(fsLtfTransAmt.getTotalamt());
                        fsLtfAcctDeal.setTxFlag("0"); // ����״̬
                        fsLtfAcctDeal.setEntpNo(fsLtfTransAmt.getAreaCode());  //  ��ҵ��
                        fsLtfAcctDeal.setPreActSerial(preActSerial);
                        fsLtfAcctDeal.setBranchId(dept);
                        fsLtfAcctDeal.setOperNo(operCode);
                        fsLtfAcctDeal.setOrg("������������");
                        fsLtfAcctDeal.setChkActDt(txnDate);
                        fsLtfAcctDeal.setTxCode(toa.txncode);
                        fsLtfAcctDeal.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                        fsLtfAcctDeal.setOperTime(new SimpleDateFormat("HHmmss").format(new Date()));
                        cnt = commService.insertAcctDeal(fsLtfAcctDeal);
                        if (cnt < 1) {
                            logger.info("���ڣ�" + txnDate + " Ԥ����ˮʧ��");
                        }
                        String resStr = new String(dataExchangeService.processThirdPartyServer((toa.toByteArray()), toa.txncode), "GBK");
                        // �����붨��15
                        String[] respondBody = StringUtils.splitByWholeSeparatorPreserveAllTokens(resStr.substring(15), "|");
                        if (respondBody.length < 8) {
                            logger.info("��ɫ���ױ����쳣,������Ϣ" + resStr);
                            continue;
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
                            fsLtfAcctDeal.setCbsActSerial(respondBody[4]);
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

    private List<FsLtfTicketInfo> selectTickInfo(String txnDate) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andChkActDtEqualTo(txnDate);
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }
}
