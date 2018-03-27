package org.fbi.ltf.processor;

import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6040Request.CbsTia6040;
import org.fbi.ltf.domain.tps.TIAT60002;
import org.fbi.ltf.domain.tps.TOAT60002;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.enums.VouchStatus;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfVchJrnlMapper;
import org.fbi.ltf.repository.dao.FsLtfVchStoreMapper;
import org.fbi.ltf.repository.dao.common.FsVoucherMapper;
import org.fbi.ltf.repository.model.FsLtfVchJrnl;
import org.fbi.ltf.repository.model.FsLtfVchStore;
import org.fbi.ltf.repository.model.FsLtfVchStoreExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ��ȡƱ��
 * Created by Thinkpad on 2015/11/3.
 */
public class T6040Processor extends AbstractTxnProcessor{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6040 tia = new CbsTia6040();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6040) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6040");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }


        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia,request);
            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6040 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            // �������˺š������˺ź͸������˻�����ѯ��¼�������������������������
            List<FsLtfVchStore> vchStoreList = selectVchStore(tia);
            if(vchStoreList.size()>0){
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("��Ʊ���ظ�");
                return cbsRtnInfo;
            }

            //�����˴���
            //1����������������JSON������
            TOAT60002 toat60002 = generateToa(tia);
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60002));
            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strGainBill = ProjectConfigManager.getInstance().getProperty("tps.server.gainbill");
            msgReq.setUri(msgReq.getHost()+strGainBill);
            //2����������
            String respBaseStr = processThirdPartyServer(msgReq);
            //3���������ݣ����д���
            String respStr = FbiBeanUtils.decode64(respBaseStr);
            TpsMsgRes msgRes = new TpsMsgRes();
            if(!StringUtils.isEmpty(respStr)){
                msgRes = FbiBeanUtils.jsonToBean(respStr,TpsMsgRes.class);
                String resultCode = msgRes.getCode();
                if ("0000".equals(msgRes.getCode())) { //���״���ɹ�
                    JSONArray resArray = msgRes.getReqdata();
                    for(int i=0;i<resArray.size();i++){
                        TIAT60002 tiat60002 = FbiBeanUtils.jsonToBean(resArray.getString(i),TIAT60002.class);
                        int vchCnt =Integer.parseInt(tiat60002.getBillNum());
                        String startNo = tiat60002.getBillStart();
                        String endNo = tiat60002.getBillEnd();
                        String deptNo = request.getHeader("branchId");
                        String operId = request.getHeader("tellerId");

                        FsLtfVchStore vchStore = new FsLtfVchStore();
                        vchStore.setVchCount(vchCnt);
                        vchStore.setBankCode(tia.getBankCode());
                        vchStore.setBillCode(tia.getBillCode());
                        vchStore.setBusCode(tia.getBusCode());
                        vchStore.setBillBatch(tia.getBillBatch());
                        vchStore.setBranchId(deptNo);
                        vchStore.setOprNo(operId);
                        vchStore.setVchStartNo(startNo);
                        vchStore.setVchEndNo(endNo);
                        vchStore.setSheets(tiat60002.getSheets());

                        processHoInstVchInput(vchCnt, startNo, endNo, deptNo, operId, vchStore);
                    }
                    session.commit();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                }else{
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg(msgRes.getComment());
                }
            }else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("����Ʊ�ݴ���ʧ��");
            }

            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info(e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
    //�������������ɸ��ݽ��׺����ò�ͬ�ĳ�ʱʱ��
    private String processThirdPartyServer(TpsMsgReq msgReq) throws Exception {
        LnkHttpClient client = new LnkHttpClient();
        client.doPost(msgReq);
        return msgReq.getResdata();
    }
    //�ж���Ʊ���Ƿ����
    private List<FsLtfVchStore> selectVchStore(CbsTia6040 tia){
        FsLtfVchStoreExample vchStoreExample = new FsLtfVchStoreExample();
        vchStoreExample.createCriteria().andBankCodeEqualTo(tia.getBankCode()).andBusCodeEqualTo(tia.getBusCode()).andBillCodeEqualTo(tia.getBillCode()).andBillBatchEqualTo(tia.getBillBatch());
        FsLtfVchStoreMapper vchStoreMapper =session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> vchStoreList = vchStoreMapper.selectByExample(vchStoreExample);
        return vchStoreList;
    }

    private TOAT60002 generateToa(CbsTia6040 tia){
        TOAT60002 toat60002 = new TOAT60002();
        toat60002.setBankCode(tia.getBankCode());
        toat60002.setBillCode(tia.getBillCode());
        toat60002.setBusCode(tia.getBusCode());
        toat60002.setBillBatch(tia.getBillBatch());
        return toat60002;
    }

    //����Ʊ������
    public void processHoInstVchInput(int vchCnt, String startNo, String endNo,String deptNo,String operId,FsLtfVchStore vchStore) {
        //��ȡ���п�����
        processInstVoucherInput(vchCnt, startNo, endNo, deptNo, "����������",operId,vchStore);
        insertVoucherJournal(vchCnt, startNo, endNo, deptNo, VouchStatus.RECEIVED, "����������",operId,vchStore.getBusCode(),vchStore.getBillCode());
    }

    private void processInstVoucherInput(int vchCnt, String startNo, String endNo, String instNo, String remark,String operId,FsLtfVchStore vchStore) {
        FsVoucherMapper voucherMapper = session.getMapper(FsVoucherMapper.class);
        String maxEndno = voucherMapper.selectInstVchMaxEndNo(instNo);
        if (maxEndno == null) {
            maxEndno = "0";
        }

        if (startNo.compareTo(maxEndno) > 0) {//����¼����űȿ���еĶ���
            insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,operId,vchStore);
        } else {
            //����Ƿ��������¼�������ֹ��֮��ļ�¼
            int recordNum = voucherMapper.selectStoreRecordnumBetweenStartnoAndEndno(startNo, endNo);
            if (recordNum > 0) {
                throw new RuntimeException("Ʊ�ų�ͻ��");
            }
            String minNearbyStartno = voucherMapper.selectStoreStartno_GreaterThanVchno(endNo);
            if (minNearbyStartno == null) {
                throw new RuntimeException("Ʊ�ų�ͻ��");
            } else {
                String maxNearbyEndno = voucherMapper.selectStoreEndno_LessThanVchno(startNo);
                if (maxNearbyEndno == null) {//�п�棬��ÿ����¼����Ŷ�������¼��ֹ�Ŵ�
                    insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,operId,vchStore);
                } else {
                    long dbVchCnt = Long.parseLong(minNearbyStartno) - Long.parseLong(maxNearbyEndno) - 1;
                    if (vchCnt <= dbVchCnt) {
                        insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,operId,vchStore);
                    } else {
                        throw new RuntimeException("Ʊ�ų�ͻ��");
                    }
                }
            }
        }
    }

    private void insertVoucherStore(long vchCnt, String startNo, String endNo, String instNo, String remark,String operId,FsLtfVchStore vchStore) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        vchStore.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchStore.setRecversion(0);
        vchStore.setRemark(remark);
        storeMapper.insert(vchStore);
    }

    private void insertVoucherJournal(long vchCnt, String startNo, String endNo, String branchId,
                                      VouchStatus status, String remark,String operCode,String busCode,String billCode) {
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnl vchJrnl = new FsLtfVchJrnl();
        vchJrnl.setVchCount((int)vchCnt);
        vchJrnl.setVchStartNo(startNo);
        vchJrnl.setVchEndNo(endNo);
        vchJrnl.setBranchId(branchId);
        vchJrnl.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchJrnl.setOprNo(operCode);
        vchJrnl.setRecversion(0);
        vchJrnl.setRemark(remark);
        vchJrnl.setVchState(status.getCode());
        vchJrnl.setBusCode(busCode);
        vchJrnl.setBillCode(billCode);
        mapper.insert(vchJrnl);
    }
}
