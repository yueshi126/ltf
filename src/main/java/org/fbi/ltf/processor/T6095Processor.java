package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6095Request.CbsTia6095;
import org.fbi.ltf.domain.tps.TOAT60095;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.*;
import org.fbi.ltf.repository.dao.FsLtfOrderMakeupMapper;
import org.fbi.ltf.repository.model.FsLtfOrderMakeup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 2.3.13	������Ϣ��¼�ӿ�
 * Created by Thinkpad on 2018/02/24.
 */
public class T6095Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6095 tia = new CbsTia6095();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6095) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6095");
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

    public CbsRtnInfo processTxn(CbsTia6095 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        TOAT60095 toat60095 = new TOAT60095();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            FbiBeanUtils.copyProperties(tia, toat60095);
            toat60095.setTransTime(LTFTools.dateFormat(toat60095.getTransTime(), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
            String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
            toat60095.setBankCode(bankCode);
            toat60095.setType("1001");
            // ����
            String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60095));
            TpsMsgReq msgReq = new TpsMsgReq();
            msgReq.setReqdata(reqdata);
            String strPayerAccount = ProjectConfigManager.getInstance().getProperty("tps.server.reportPayerAccount");
            msgReq.setUri(msgReq.getHost() + strPayerAccount);
            //2����������
            String respBaseStr = processThirdPartyServer(msgReq);
            //3���������ݣ����д���
            String respStr = FbiBeanUtils.decode64(respBaseStr);
            TpsMsgRes msgRes = new TpsMsgRes();
            if (!StringUtils.isEmpty(respStr)) {
                msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                String resultCode = msgRes.getCode();
                if (("0000".equals(msgRes.getCode()))) { //���״���ɹ�
                    FsLtfOrderMakeup fsLtfOrderMakeup = new FsLtfOrderMakeup();
                    FbiBeanUtils.copyProperties(toat60095, fsLtfOrderMakeup);
                    fsLtfOrderMakeup.setOperid(request.getHeader("tellerId"));
                    fsLtfOrderMakeup.setOperdate(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                    insertOrderMakeup(fsLtfOrderMakeup);
                }
            }
            session.commit();
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            logger.info("������¼�ɹ�,������ţ�" + toat60095.getOrderNo() + ",��¼�˺ţ�" + toat60095.getNode1());
            return cbsRtnInfo;
        } catch (Exception e) {
            logger.info("������¼ʧ�ܣ�" + e.getMessage());
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

    private String processThirdPartyServer(TpsMsgReq msgReq) throws Exception {
        LnkHttpClient client = new LnkHttpClient();
        client.doPost(msgReq);
        return msgReq.getResdata();
    }

    //���벹¼����
    private void insertOrderMakeup(FsLtfOrderMakeup fsLtfOrderMakeup) {
        FsLtfOrderMakeupMapper mapper = session.getMapper(FsLtfOrderMakeupMapper.class);
        mapper.insert(fsLtfOrderMakeup);
    }

}
