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
 * 订单信息查询
 * 1001（待分票）
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
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
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
            //本地处理

            //交警端处理
            //1、将请求数据生成JSON并加密
            toat60007 = new TOAT60007();
            FbiBeanUtils.copyProperties(tia, toat60007);
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60007));

            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strGainBill = ProjectConfigManager.getInstance().getProperty("tps.server.orderBillQuery");
            msgReq.setUri(msgReq.getHost() + strGainBill);

            //2、发送数据
            String respBaseStr = processThirdPartyServer(msgReq);
            //3、解析数据，进行处理
            String respStr = FbiBeanUtils.decode64(respBaseStr);
            TpsMsgRes msgRes = new TpsMsgRes();
            if (!StringUtils.isEmpty(respStr)) {
                msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                String resultCode = msgRes.getCode();
                if ("0000".equals(msgRes.getCode())) { //交易处理成功
                    JSONArray resArray = msgRes.getReqdata();
//                    //20180227 add 删除查询日期的订单
//                    FsLtfVchQry delBillQry = new FsLtfVchQry();
//                    delBillQry.setNode1(tia.getNode1());
//                    delBillQry.setNode2(tia.getNode1());
//                    deleteBillQry(delBillQry);
                    for (int i = 0; i < resArray.size(); i++) {
                        TIAT60007 tiat60007 = FbiBeanUtils.jsonToBean(resArray.getString(i), TIAT60007.class);
                        //1,首先判断是否已经存在，只有不存在才插入新的数据
                        FsLtfVchQry billQry = this.selectBill(tiat60007.getOrderNo(), tiat60007.getTransTime(), tiat60007.getOrderDetail());
                        if (billQry == null) {
                            billQry = new FsLtfVchQry();
                            FbiBeanUtils.copyProperties(tiat60007, billQry);
                            String orgCode = tiat60007.getBankMare();
                            FsLtfOrgComp orgComp = selectOrg(orgCode);
                            if (orgComp == null) {
                                logger.info("不存在对应的机构" + orgCode);
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
                cbsRtnInfo.setRtnMsg("订单信息查询处理失败！");
            }

            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info(e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("数据库处理异常");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //第三方服务处理：可根据交易号设置不同的超时时间
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
