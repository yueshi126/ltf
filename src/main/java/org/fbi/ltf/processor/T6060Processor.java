package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6060Request.CbsTia6060;
import org.fbi.ltf.domain.cbs.T6060Response.CbsToa6060;
import org.fbi.ltf.domain.tps.TOAT60006;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.enums.VouchStatus;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.dao.common.FsVoucherMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ��Ʊ��Ϣ�ϱ�  ����ֻ������ʵ�ʷ�Ʊ,һ��ʹ�ò���������
 * Created by Thinkpad on 2015/11/3.
 */
public class T6060Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;
    private final String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6060 tia = new CbsTia6060();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6060) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6060");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6060 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        TOAT60006 toat60006 = null;
        Boolean updateFlag = false;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            //1�����Ȳ�ѯ��Ʊ���Ƿ��Ѿ�
            String branchId = request.getHeader("branchId");
            FsLtfOrgComp orgComp = selectOrg(branchId);
            if (orgComp == null) {
                logger.info("����ţ�" + branchId + "��û�ж�Ӧ��Ϣ��");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("��֧���ڽ���û�ж�Ӧ����Ϣ");
                return cbsRtnInfo;
            }
            String orgCode = orgComp.getOrgCode();
            String newNo = "";//��Ʊ�ݺ�
            List<FsLtfVchJrnl> vchJrnlList = selectVchJrnl(tia.getBillNo(), branchId);
            //����Ѿ���ʹ��״̬�����ڱ�ע���������Ʊ�� TODO

            List<FsLtfVchOut> outList = null;

            //����Ʊ������
            if ("1".equals(tia.getBusCode())) {
                outList = selectVchOut(tia.getBillNo(), orgCode);
                if (outList.size() > 0) {
//                    if(vchJrnlList.size() == 0 ){
//                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                        cbsRtnInfo.setRtnMsg("����Ʊ��û����⣬��������");
//                        return cbsRtnInfo;
//                    }
//                    FsLtfVchJrnl jrnl = vchJrnlList.get(0);
////                    if (("2".equals(jrnl.getVchState())) && (StringUtils.isEmpty(tia.getNode())) ) {
////                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
////                        cbsRtnInfo.setRtnMsg("�������õ���Ʊ����������Ʊ");
////                        return cbsRtnInfo;
////                    }
//                    if ("3".equals(jrnl.getVchState())) {
//                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                        cbsRtnInfo.setRtnMsg("��Ʊ���Ѿ����ϣ������ٴ�����");
//                        return cbsRtnInfo;
//                    }
//                    FsLtfVchStore store = selectVchStore(branchId);
//                    if (store==null){
//                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                        cbsRtnInfo.setRtnMsg("�û���û����Ʊ����������");
//                        return  cbsRtnInfo;
//                    }
//                    String vchStartNo = store.getVchStartNo();
//                    String vchEndNo = store.getVchEndNo();
//                    if(Long.parseLong(vchStartNo)+300>Long.parseLong(vchEndNo)){
//                        newNo = vchEndNo;
//                    }else{
//                        newNo = String.valueOf(Long.parseLong(vchStartNo) + 300);
//                    }
//                    updateFlag = true;
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("��ʹ�õ�����Ʊ�ݣ���������");
                    return cbsRtnInfo;
                } else {
//                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                    cbsRtnInfo.setRtnMsg("δʹ�õ�����Ʊ�ݣ���������");
//                    return cbsRtnInfo;
                    newNo = tia.getBillNo();
                }
            } else if ("2".equals(tia.getBusCode())) {
                if (vchJrnlList.size() > 0) {
                    FsLtfVchJrnl jrnl = vchJrnlList.get(0);
                    if ("3".equals(jrnl.getVchState())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("��Ʊ��������,�����ٴ�����");
                        return cbsRtnInfo;
                    } else if ("2".equals(jrnl.getVchState())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("����Ʊ����ʹ��,�����ظ�����");
                        return cbsRtnInfo;
                    }
                }
                newNo = tia.getBillNo();
            }
            FsLtfVchStore storeRemain = selectVchStoreByStartNo(branchId, newNo);
            if (storeRemain == null) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�û���û�е�ǰƱ�ţ���������");
                return cbsRtnInfo;
            }
            //�����˴���
            //1����������������JSON������
            toat60006 = new TOAT60006();
            FbiBeanUtils.copyProperties(tia, toat60006);
            toat60006.setBankCode(bankCode);
            toat60006.setApplyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            toat60006.setNode(newNo);
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60006));

            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strGainBill = ProjectConfigManager.getInstance().getProperty("tps.server.reportDiscarBill");
            msgReq.setUri(msgReq.getHost() + strGainBill);

            //2����������
            String respBaseStr = processThirdPartyServer(msgReq);
            //3���������ݣ����д���
            String respStr = FbiBeanUtils.decode64(respBaseStr);
            TpsMsgRes msgRes = new TpsMsgRes();
            if (!StringUtils.isEmpty(respStr)) {
                msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                if (("0000".equals(msgRes.getCode()))) { //���״���ɹ�

                    //���¿��
                    String dept = branchId;
                    String operCode = request.getHeader("tellerId");
                    String startNo = "";
                    String endNo = "";
                    VouchStatus status = null;
//                    if(updateFlag){
//                        startNo = newNo;
//                        endNo = startNo;
//                        status = VouchStatus.USED;
//                        FsLtfVchOut vchOut = outList.get(0);
//                        vchOut.setBillNo(newNo);
//                        this.updateVchOut(vchOut);
//                    }else{
                    startNo = newNo;
                    endNo = startNo;
                    status = VouchStatus.CANCEL;
//                    }
                    List<FsLtfVchStore> storeParamList = new ArrayList<FsLtfVchStore>();
                    doVchUseOrCancel(dept, startNo, endNo, storeParamList);
                    String remark = status.getTitle();
                    String busCode = "";
                    String billCode = "";
                    for (final FsLtfVchStore storeParam : storeParamList) {
                        FsLtfVchStore storeDb = selectVchStoreByStartNo(dept, storeParam.getVchStartNo());
                        long startNoDb = Long.parseLong(storeDb.getVchStartNo());
                        long endNoDb = Long.parseLong(storeDb.getVchEndNo());
                        long startNoParam = Long.parseLong(storeParam.getVchStartNo());
                        long endNoParam = Long.parseLong(storeParam.getVchEndNo());
                        busCode = storeDb.getBusCode();
                        billCode = storeDb.getBillCode();

                        deleteVchStore(storeDb.getPkid());
                        if (startNoDb != startNoParam || endNoDb != endNoParam) {//����������¼
                            if (startNoParam != startNoDb) {
                                insertVoucherStore(startNoParam - startNoDb, storeDb.getVchStartNo(),
                                        getStandLengthForVoucherString(startNoParam - 1), dept, remark, operCode, busCode, billCode);
                            }
                            if (endNoParam != endNoDb) {
                                insertVoucherStore(endNoDb - endNoParam, getStandLengthForVoucherString(endNoParam + 1),
                                        storeDb.getVchEndNo(), dept, remark, operCode, busCode, billCode);
                            }
                        }
                    }
                    // ����Ʊ�����ϣ���עд��ԭʼƱ��
                    if ("1".equals(tia.getBusCode())) {
                        remark = "���ϣ�" + tia.getBillNo();
                    }
                    insertVoucherJournal(Long.parseLong(endNo) - Long.parseLong(startNo) + 1, startNo, endNo, dept, status, remark, operCode, busCode, billCode);
                    //�ֺܷ˶�
                    if (!verifyVchStoreAndJrnl(dept)) {
                        throw new RuntimeException("����ֲܷ�����");
                    }

//                    if(updateFlag){
//                        String starringRespMsg = generateCbsRespMsg(newNo);
//                        cbsRtnInfo.setRtnMsg(starringRespMsg);  //todo
//                        cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
//                    }else{
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
//                    }
                    session.commit();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    return cbsRtnInfo;
                } else {
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg(msgRes.getComment());
                    if (msgRes.getComment().length() > 16) {
                        cbsRtnInfo.setRtnMsg(msgRes.getComment().substring(0, 16));
                    }

                    return cbsRtnInfo;
                }
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("Ʊ�����ϴ���ʧ��");
                return cbsRtnInfo;
            }

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

    private void insertVoucherJournal(long vchCnt, String startNo, String endNo, String branchId,
                                      VouchStatus status, String remark, String operCode, String busCode, String billCode) {
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnl vchJrnl = new FsLtfVchJrnl();
        vchJrnl.setVchCount((int) vchCnt);
        vchJrnl.setVchStartNo(startNo);
        vchJrnl.setVchEndNo(endNo);

        vchJrnl.setBranchId(branchId);
        vchJrnl.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchJrnl.setOprNo(operCode);
        vchJrnl.setRecversion(0);
        vchJrnl.setRemark(remark);
        vchJrnl.setBusCode(busCode);
        vchJrnl.setBillCode(billCode);

        vchJrnl.setVchState(status.getCode());
        mapper.insert(vchJrnl);
    }

    private void doVchUseOrCancel(String instNo, String startNo, String endNo, List<FsLtfVchStore> storeParamList) {
        FsLtfVchStore storeDb = selectVchStoreByStartNo(instNo, startNo);
        FsLtfVchStore storeParam = new FsLtfVchStore();
        storeParam.setPkid(storeDb.getPkid());
        storeParam.setVchStartNo(startNo);
        if (storeDb.getVchEndNo().compareTo(endNo) < 0) {
            storeParam.setVchEndNo(storeDb.getVchEndNo());
            storeParam.setVchCount((int) (Long.parseLong(storeDb.getVchEndNo()) - Long.parseLong(startNo) + 1));
            storeParamList.add(storeParam);
            String vchNo = getStandLengthForVoucherString(Long.parseLong(storeDb.getVchEndNo()) + 1);
            doVchUseOrCancel(instNo, vchNo, endNo, storeParamList);
        } else {
            storeParam.setVchEndNo(endNo);
            storeParam.setVchCount((int) (Long.parseLong(endNo) - Long.parseLong(startNo) + 1));
            storeParamList.add(storeParam);
        }
    }

    //������Ų������ݿ��к��д˺���Ŀ���¼
    private FsLtfVchStore selectVchStoreByStartNo(String instNo, String startNo) {
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(instNo)
                .andVchStartNoLessThanOrEqualTo(startNo)
                .andVchEndNoGreaterThanOrEqualTo(startNo);
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> storesTmp = storeMapper.selectByExample(storeExample);
        if (storesTmp.size() != 1) {
//            throw new RuntimeException("δ�ҵ�����¼��");
            return null;
        }
        return storesTmp.get(0);
    }

    //��Ʊ�ݺų���
    private String getStandLengthForVoucherString(long vchno) {
        String vchNo = "" + vchno;
        String vch_length = ProjectConfigManager.getInstance().getProperty("voucher.no.length");
        int vchnoLen = Integer.parseInt(vch_length);
        if (vchNo.length() != vchnoLen) { //���Ȳ��� ����
            vchNo = StringUtils.leftPad(vchNo, vchnoLen, "0");
        }
        return vchNo;
    }

    private void insertVoucherStore(long vchCnt, String startNo, String endNo, String instNo, String remark, String operCode,
                                    String busCode, String billCode) {
        FsLtfVchStore vchStore = new FsLtfVchStore();
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        vchStore.setVchCount((int) vchCnt);
        vchStore.setVchStartNo(startNo);
        vchStore.setVchEndNo(endNo);
        vchStore.setBankCode("CCB");
        vchStore.setBusCode(busCode);
        vchStore.setBillCode(billCode);
        vchStore.setBranchId(instNo);
        vchStore.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchStore.setOprNo(operCode);
        vchStore.setRecversion(0);
        vchStore.setRemark(remark);
        storeMapper.insert(vchStore);
    }

    private boolean verifyVchStoreAndJrnl(String instNo) {
        FsVoucherMapper voucherMapper = session.getMapper(FsVoucherMapper.class);
        int store = voucherMapper.selectVchStoreTotalNum(instNo);
        int jrnl = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.RECEIVED.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.OUTSTORE.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.USED.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.CANCEL.getCode());
        long re = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.RECEIVED.getCode());
        long ou = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.OUTSTORE.getCode());
        long use = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.USED.getCode());
        long ca = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.CANCEL.getCode());
        return store == jrnl;
    }

    //��ѯƱ����ˮ�Ƿ��Ѿ�����
    private List<FsLtfVchJrnl> selectVchJrnl(String billNo, String branchId) {
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnlExample example = new FsLtfVchJrnlExample();
        example.createCriteria().andVchStartNoEqualTo(billNo).andVchEndNoEqualTo(billNo).andBranchIdEqualTo(branchId);
        List<FsLtfVchJrnl> vchJrnlList = mapper.selectByExample(example);
        return vchJrnlList;
    }

    //��ѯƱ����ˮ�Ƿ��Ѿ�����
    private List<FsLtfVchOut> selectVchOut(String billNo, String branchId) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        example.createCriteria().andBillNoEqualTo(billNo).andBankMareEqualTo(branchId);
        List<FsLtfVchOut> vchOutList = mapper.selectByExample(example);
        return vchOutList;
    }

    private void deleteVchStore(String pkid) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.deleteByPrimaryKey(pkid);
    }

    private FsLtfOrgComp selectOrg(String orgCode) {
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andDeptCodeEqualTo(orgCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }

    private void updateVchOut(FsLtfVchOut vchOut) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        mapper.updateByPrimaryKey(vchOut);
    }

    //������Ų������ݿ��к��д˺���Ŀ���¼
    private FsLtfVchStore selectVchStore(String instNo) {
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(instNo).andBusCodeEqualTo("1").andBillCodeEqualTo("3004");
        storeExample.setOrderByClause(" vch_start_no");
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> storesTmp = storeMapper.selectByExample(storeExample);
        if (storesTmp.size() == 0) {
//            throw new RuntimeException("δ�ҵ�����¼��");
            return null;
        }
        return storesTmp.get(0);
    }

    //����CBS��Ӧ����
    private String generateCbsRespMsg(String billNo) {
        CbsToa6060 cbsToa = new CbsToa6060();
        cbsToa.setBillNo(billNo);
        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
        }
        return cbsRespMsg;
    }

}
