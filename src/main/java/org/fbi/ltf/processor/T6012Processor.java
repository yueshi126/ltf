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
 * ������Ϣ��ѯ
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
            //���ش���
            //1���ж�Ʊ���Ƿ��Ѿ��ɿ�
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia.getTicketNo());
            if (infoList.size() > 0) {
                ticketInfo = infoList.get(0);
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�÷���Ʊ���ѽɿ�,���ܽɿ�");
                return cbsRtnInfo;

            } else {
                // ����û�нɿ���Ϣ��ѯ����
                String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
                toat60012.setBankCode(bankCode);
                toat60012.setSalesName(salesName);
                toat60012.setTicketNo(tia.getTicketNo());
                // ����
                //2����������
                String respBaseStr = processThirdPartyServer(toat60012);
                //3���������ݣ����д���
                String respStr = FbiBeanUtils.decode64(respBaseStr);
                TpsMsgRes msgRes = new TpsMsgRes();
                if (!StringUtils.isEmpty(respStr)) {
                    msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                    String resultCode = msgRes.getCode();
                    if (("0000".equals(msgRes.getCode()))) { //���״���ɹ�
                        JSONArray resArray = msgRes.getReqdata();
                        for (int i = 0; i < resArray.size(); i++) {
                            TIAT60012 tiat60012 = FbiBeanUtils.jsonToBean(resArray.getString(i), TIAT60012.class);
                            //1,�����ж��Ƿ��Ѿ����ڣ�ֻ�в����ڲŲ����µ�����
                            starringRespMsg = generateCbsRespMsg(tiat60012);
                        }
                        session.commit();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                        cbsRtnInfo.setRtnMsg(starringRespMsg);
                    } else if ("1004".equals(msgRes.getCode())) {
                        // �����ѽɿ�
                        session.rollback();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("�����������ѽɿ�");
                    } else { //��ѯû�н����ǰ̨��Ҫ¼��
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
            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //�жϸ������Ƿ��Ѿ�����
    private List<FsLtfTicketInfo> selectTicketInfo(String ticketNo) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andTicketNoEqualTo(ticketNo).andQdfBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    private String processThirdPartyServer(TOAT60012 toat60012) throws Exception {
        TpsMsgReq msgReq = new TpsMsgReq();
        // ����
        String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60012));
        msgReq.setReqdata(reqdata);
        String strPayerAccount = ProjectConfigManager.getInstance().getProperty("tps.server.ticketSearch");
        msgReq.setUri(msgReq.getHost() + strPayerAccount);
        LnkHttpClient client = new LnkHttpClient();
        client.doPost(msgReq);
        return msgReq.getResdata();
    }

    //����CBS��Ӧ����
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
        // ����ʱ���Ӧ�ֶβ�һ�µ�����ֵ
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
            throw new RuntimeException("6012��֯���ر��ĳ���.", e);
        }
        return cbsRespMsg;
    }

    //����CBS��Ӧ���� ��ѯ������Ϣ���ؿ�,������ɫ�ձ���
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
            throw new RuntimeException("6012��֯���ر��ĳ���.", e);
        }
        return cbsRespMsg;
    }
}
