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
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;
import org.fbi.ltf.domain.cbs.T6012Request.CbsTia6012;
import org.fbi.ltf.domain.tps.*;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * 罚单信息查询
 * Created by Thinkpad on 2018/03/05
 */
public class T6012Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6012 tia = new CbsTia6012();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6012) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6012");
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

    private CbsRtnInfo processTxn(CbsTia6012 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        FsLtfTicketInfo ticketInfo = new FsLtfTicketInfo();
        String salesName = request.getHeader("tellerId");
        TOAT60012 toat60012 = new TOAT60012();
        String starringRespMsg = "";
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、判断票据是否已经缴款
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia.getTicketNo());
            if (infoList.size() > 0) {
                ticketInfo = infoList.get(0);
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该罚单票据已缴款,不能缴款");
                return cbsRtnInfo;

            } else {
                // 本地没有缴款信息查询海博
                String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
                toat60012.setBankCode(bankCode);
                toat60012.setSalesName(salesName);
                toat60012.setTicketNo(tia.getTicketNo());
                // 加密
                //2、发送数据
                String respBaseStr = processThirdPartyServer(toat60012);
                //3、解析数据，进行处理
                String respStr = FbiBeanUtils.decode64(respBaseStr);
                TpsMsgRes msgRes = new TpsMsgRes();
                if (!StringUtils.isEmpty(respStr)) {
                    msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                    String resultCode = msgRes.getCode();
                    if (("0000".equals(msgRes.getCode()))) { //交易处理成功
                        JSONArray resArray = msgRes.getReqdata();
                        for (int i = 0; i < resArray.size(); i++) {
                            TIAT60012 tiat60012 = FbiBeanUtils.jsonToBean(resArray.getString(i), TIAT60012.class);
                            //1,首先判断是否已经存在，只有不存在才插入新的数据
                            starringRespMsg = generateCbsRespMsg(tiat60012);
                        }
                        session.commit();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                        cbsRtnInfo.setRtnMsg(starringRespMsg);
                    } else if ("1004".equals(msgRes.getCode())) {
                        // 单据已缴款
                        session.rollback();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("处罚决定书已缴款");
                    } else { //查询没有结果，前台需要录入
                        session.commit();
                        TIAT60012 tiat60012 = new TIAT60012();
                        tiat60012.setTicketNo(tia.getTicketNo());
                        starringRespMsg = generateCbsRespMsgNull(tiat60012);
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                        cbsRtnInfo.setRtnMsg(starringRespMsg);
                    }
                }
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

    //判断该数据是否已经存在
    private List<FsLtfTicketInfo> selectTicketInfo(String ticketNo) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andTicketNoEqualTo(ticketNo).andQdfBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    private String processThirdPartyServer(TOAT60012 toat60012) throws Exception {
        TpsMsgReq msgReq = new TpsMsgReq();
        // 加密
        String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60012));
        msgReq.setReqdata(reqdata);
        String strPayerAccount = ProjectConfigManager.getInstance().getProperty("tps.server.ticketSearch");
        msgReq.setUri(msgReq.getHost() + strPayerAccount);
        LnkHttpClient client = new LnkHttpClient();
        client.doPost(msgReq);
        return msgReq.getResdata();
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(TIAT60012 tiat60012) {
        List<CbsTia6010Item> items = new ArrayList<CbsTia6010Item>();
        CbsTia6010Item cbsTia6010Item = new CbsTia6010Item();
        CbsTia6010 cbsToa = new CbsTia6010();
        FbiBeanUtils.copyProperties(tiat60012, cbsToa);
        cbsToa.setItemNum("1");
        cbsTia6010Item.setAmount(tiat60012.getTicketAmount());
        cbsTia6010Item.setItemCode(tiat60012.getOrderCharges());
        ;
        cbsTia6010Item.setItemName(tiat60012.getOrderChargesName());
        // 处理时间对应字段不一致单独赋值
        String strTicketTime = "";
        if (!tiat60012.getIllegalTime().isEmpty()) {
            strTicketTime = tiat60012.getIllegalTime().replace("-", "").replace(":", "");
        }
        cbsToa.setTicketTime(strTicketTime);
        String cbsRespMsg = "";
        try {
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("6012组织返回报文出错.", e);
        }
        return cbsRespMsg;
    }

    //生成CBS响应报文 查询罚单信息返回空,返回特色空报文
    private String generateCbsRespMsgNull(TIAT60012 tiat60012) {
        CbsTia6010 cbsToa = new CbsTia6010();
        String cbsRespMsg = "";
        FbiBeanUtils.copyProperties(tiat60012, cbsToa);
        try {
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("6012组织返回报文出错.", e);
        }
        return cbsRespMsg;
    }
}
