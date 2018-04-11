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
import org.fbi.ltf.domain.cbs.T6070Request.CbsTia6070;
import org.fbi.ltf.domain.tps.TIAT60007;
import org.fbi.ltf.domain.tps.TOAT60007;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfOrgCompMapper;
import org.fbi.ltf.repository.dao.FsLtfVchQryMapper;
import org.fbi.ltf.repository.model.FsLtfOrgComp;
import org.fbi.ltf.repository.model.FsLtfOrgCompExample;
import org.fbi.ltf.repository.model.FsLtfVchQry;
import org.fbi.ltf.repository.model.FsLtfVchQryExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * ������Ϣ��ѯ
 * 1001������Ʊ��
 * Created by Thinkpad on 2015/11/3.
 */
public class T6070Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6070 tia = new CbsTia6070();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6070) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6070");
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

    private CbsRtnInfo processTxn(CbsTia6070 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        TOAT60007 toat60007 = null;
        Boolean updateFlag = false;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���

            //�����˴���
            //1����������������JSON������
            toat60007 = new TOAT60007();
            FbiBeanUtils.copyProperties(tia, toat60007);
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60007));

            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strGainBill = ProjectConfigManager.getInstance().getProperty("tps.server.orderBillQuery");
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
                    JSONArray resArray = msgRes.getReqdata();
//                    //20180227 add ɾ����ѯ���ڵĶ���
//                    FsLtfVchQry delBillQry = new FsLtfVchQry();
//                    delBillQry.setNode1(tia.getNode1());
//                    delBillQry.setNode2(tia.getNode1());
//                    deleteBillQry(delBillQry);
                    for (int i = 0; i < resArray.size(); i++) {
                        TIAT60007 tiat60007 = FbiBeanUtils.jsonToBean(resArray.getString(i), TIAT60007.class);
                        //1,�����ж��Ƿ��Ѿ����ڣ�ֻ�в����ڲŲ����µ�����
                        FsLtfVchQry billQry = this.selectBill(tiat60007.getOrderNo(), tiat60007.getTransTime(), tiat60007.getOrderDetail());
                        if (billQry == null) {
                            billQry = new FsLtfVchQry();
                            FbiBeanUtils.copyProperties(tiat60007, billQry);
                            String orgCode = tiat60007.getBankMare();
                            FsLtfOrgComp orgComp = selectOrg(orgCode);
                            if (orgComp == null) {
                                logger.info("�����ڶ�Ӧ�Ļ���" + orgCode);
                            }
                            String branchId = orgComp.getDeptCode();
                            billQry.setBranchId(branchId);
                            billQry.setType(tia.getType());
                            billQry.setNode1(tia.getNode1());
                            billQry.setNode2(tia.getNode2());
                            billQry.setNode3(tia.getNode3());
                            billQry.setNode4(tia.getNode4());
                            billQry.setOprNo(request.getHeader("tellerId"));
                            billQry.setOprDate(request.getHeader("txnTime").substring(0, 8));
                            insertBillQry(billQry);
                        }
                    }
                    session.commit();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                } else {
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg(msgRes.getComment());
                }
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("������Ϣ��ѯ����ʧ�ܣ�");
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

    private void insertBillQry(FsLtfVchQry billQry) {
        FsLtfVchQryMapper mapper = session.getMapper(FsLtfVchQryMapper.class);
        mapper.insert(billQry);

    }

    private void deleteBillQry(FsLtfVchQry billQry) {
        FsLtfVchQryMapper mapper = session.getMapper(FsLtfVchQryMapper.class);
        FsLtfVchQryExample example = new FsLtfVchQryExample();
        example.createCriteria().andNode1EqualTo(billQry.getNode1()).andNode2EqualTo(billQry.getNode2());
        mapper.deleteByExample(example);
    }

    private FsLtfOrgComp selectOrg(String orgCode) {
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }

    private FsLtfVchQry selectBill(String orderNo, String transTime, String orderDetail) {
        FsLtfVchQryMapper mapper = session.getMapper(FsLtfVchQryMapper.class);
        FsLtfVchQryExample example = new FsLtfVchQryExample();
        example.createCriteria().andOrderNoEqualTo(orderNo).andTransTimeEqualTo(transTime).andOrderDetailEqualTo(orderDetail);
        List<FsLtfVchQry> vchQryList = mapper.selectByExample(example);
        if (vchQryList.size() > 0) {
            return vchQryList.get(0);
        } else {
            return null;
        }
    }

}
