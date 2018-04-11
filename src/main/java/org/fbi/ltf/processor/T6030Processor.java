package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6030Request.CbsTia6030;
import org.fbi.ltf.domain.tps.TOAT60003;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * �ɿ���Ϣ�ϱ�
 * Created by Thinkpad on 2015/11/3.
 */
public class T6030Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6030 tia = new CbsTia6030();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6030) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6030");
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

    private CbsRtnInfo processTxn(CbsTia6030 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        TOAT60003 toat60003 = null;
        FsLtfTicketInfo ticketInfo = new FsLtfTicketInfo();
        Boolean updateFlag = false;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            //1���ж�Ʊ���Ƿ��Ѿ��ɿ�
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia);
            if (infoList.size() == 0) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�÷���û�нɿ�������ϱ�");
                return cbsRtnInfo;
                //2,�ж��Ƿ��Ѿ��ϱ�
            } else if (infoList.size() > 0) {
                ticketInfo = (FsLtfTicketInfo) infoList.get(0);
                updateFlag = true;
                if (("1".equals(ticketInfo.getQdfBookFlag())) && (!StringUtils.isEmpty(ticketInfo.getBillNo()))) {
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("�÷������ϱ����������ظ��ϱ�");
                    return cbsRtnInfo;
                }
            }
            //3,�жϸ�Ʊ���Ƿ����ڹ���Ʊ��
            String dept = request.getHeader("branchId");
            String billNo = tia.getBillNo();
            FsLtfVchStore vchStore = selectVchStore(dept, billNo);
            if (vchStore == null) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�û���û�е�ǰƱ�ţ���������");
                return cbsRtnInfo;
            }

            //�����˴���
            //1����������������JSON������
            FsLtfOrgComp orgComp = selectOrg(dept);
            if (orgComp == null) {
                logger.info("�����Ϊ��" + dept + "֧�У�û�ж�Ӧ��Ϣ��");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("��֧���ڽ���û�ж�Ӧ����Ϣ");
                return cbsRtnInfo;
            }
            toat60003 = new TOAT60003();
            FbiBeanUtils.copyProperties(ticketInfo, toat60003);
            if (toat60003.getTicketNo().length() == 16) {
                toat60003.setTicketNo(toat60003.getTicketNo().substring(0, 15));
            }
            toat60003.setBankTake(orgComp.getOrgCode());
//            toat60003.setTransTime(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
            toat60003.setBillNo(billNo);
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60003));
            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strGainBill = ProjectConfigManager.getInstance().getProperty("tps.server.reportCounterInfo");
            msgReq.setUri(msgReq.getHost() + strGainBill);
            //2����������
            String respBaseStr = processThirdPartyServer(msgReq);
            //3���������ݣ����д���
            String respStr = FbiBeanUtils.decode64(respBaseStr);
            TpsMsgRes msgRes = new TpsMsgRes();
            if (!StringUtils.isEmpty(respStr)) {
                msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                String resultCode = msgRes.getCode();
                if ("0000".equals(msgRes.getCode())) { //���״���ɹ�
                    if (updateFlag) {
                        //����Ʊ��
                        String startNo = billNo;
                        String endNo = billNo;
                        String operCode = request.getHeader("tellerId");
                        VouchStatus status = VouchStatus.USED;
                        List<FsLtfVchStore> storeParamList = new ArrayList<FsLtfVchStore>();
                        doVchUseOrCancel(dept, startNo, endNo, storeParamList);
                        String remark = status.getTitle();
                        for (final FsLtfVchStore storeParam : storeParamList) {
                            FsLtfVchStore storeDb = selectVchStoreByStartNo(dept, storeParam.getVchStartNo());
                            long startNoDb = Long.parseLong(storeDb.getVchStartNo());
                            long endNoDb = Long.parseLong(storeDb.getVchEndNo());
                            long startNoParam = Long.parseLong(storeParam.getVchStartNo());
                            long endNoParam = Long.parseLong(storeParam.getVchEndNo());

                            deleteVchStore(storeDb.getPkid());
                            if (startNoDb != startNoParam || endNoDb != endNoParam) {//����������¼
                                if (startNoParam != startNoDb) {
                                    insertVoucherStore(startNoParam - startNoDb, storeDb.getVchStartNo(),
                                            getStandLengthForVoucherString(startNoParam - 1), dept, remark, operCode);
                                }
                                if (endNoParam != endNoDb) {
                                    insertVoucherStore(endNoDb - endNoParam, getStandLengthForVoucherString(endNoParam + 1),
                                            storeDb.getVchEndNo(), dept, remark, operCode);
                                }
                            }
                        }
                        insertVoucherJournal(Long.parseLong(endNo) - Long.parseLong(startNo) + 1, startNo, endNo, dept, status, remark, operCode);
                        //�ֺܷ˶�
                        if (!verifyVchStoreAndJrnl(dept)) {
                            logger.info("����ֲܷ���");
                            throw new RuntimeException("����ֲܷ�����");
                        }

                        //���½ɿ�����
                        ticketInfo.setBillNo(billNo);
                        ticketInfo.setQdfBookFlag("1");
                        updateTicketInfo(ticketInfo);
                    }
                    session.commit();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                    logger.info("Ʊ���ϴ��ɹ��������ţ�" + tia.getTicketNo() + ",Ʊ�ݺţ�" + tia.getBillNo());
                } else {
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg(msgRes.getComment());
                }
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("Ʊ����Ϣ�ϴ�ʧ��");
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

    //�жϸ������Ƿ��Ѿ�����
    private List<FsLtfTicketInfo> selectTicketInfo(CbsTia6030 tia) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andTicketNoEqualTo(tia.getTicketNo()).andHostBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    //��������
    private void updateTicketInfo(FsLtfTicketInfo ticketInfo) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        mapper.updateByPrimaryKey(ticketInfo);
    }

    //��ѯ����
    private FsLtfVchStore selectVchStore(String branchId, String billNo) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(branchId).andBusCodeEqualTo("2").andBillCodeEqualTo("3005").
                andVchStartNoLessThanOrEqualTo(billNo).andVchEndNoGreaterThanOrEqualTo(billNo);
        storeExample.setOrderByClause("vch_start_no");
        List<FsLtfVchStore> vchStoreList = storeMapper.selectByExample(storeExample);
        if (vchStoreList.size() > 0) {
            return vchStoreList.get(0);
        } else {
            return null;
        }
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

    private void deleteVchStore(String pkid) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.deleteByPrimaryKey(pkid);
    }

    private void insertVoucherJournal(long vchCnt, String startNo, String endNo, String branchId,
                                      VouchStatus status, String remark, String operCode) {
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
        vchJrnl.setBusCode("2");
        vchJrnl.setBillCode("3005");

        vchJrnl.setVchState(status.getCode());
        mapper.insert(vchJrnl);
    }

    private void insertVoucherStore(long vchCnt, String startNo, String endNo, String instNo, String remark, String operCode) {
        FsLtfVchStore vchStore = new FsLtfVchStore();
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        vchStore.setVchCount((int) vchCnt);
        vchStore.setVchStartNo(startNo);
        vchStore.setVchEndNo(endNo);
        vchStore.setBankCode("CCB");
        vchStore.setBusCode("2");
        vchStore.setBillCode("3005");
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
        return store == jrnl;
    }

    private FsLtfOrgComp selectOrg(String deptCode) {
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andDeptCodeEqualTo(deptCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }
}
