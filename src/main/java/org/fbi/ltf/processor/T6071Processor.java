package org.fbi.ltf.processor;

import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6071Request.CbsTia6071;
import org.fbi.ltf.domain.tps.TIAT60071;
import org.fbi.ltf.domain.tps.TOAT60007;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.FsLtfVchQryMapper;
import org.fbi.ltf.repository.dao.common.CommonMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ������Ϣ��ѯ
 * 1002���ۺ�Ӧ��ƽ̨���˽����
 * Created by Thinkpad on 2018/2/27.
 */
public class T6071Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;
    private final String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");


    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6071 tia = new CbsTia6071();
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia);
            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6071 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        TOAT60007 toat60007 = null;
        Boolean updateFlag = false;
        String ticketNo100 = "";
        String ticketNoFail100 = "";
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            List<FsLtfTicketInfo> infoList = qryTicket100();
            List<FsLtfTicketInfo> infoFailList = qryTicketFail100();
            String[] ticketInfall = new String[2];
            for (FsLtfTicketInfo fsLtfTicketInfo : infoList) {
                ticketNo100 += fsLtfTicketInfo.getTicketNo() + ",";
            }
            for (FsLtfTicketInfo fsLtfTicketInfo : infoFailList) {
                ticketNoFail100 += fsLtfTicketInfo.getTicketNo() + ",";
            }
            if (StringUtils.isEmpty(ticketNo100) && StringUtils.isEmpty(ticketNoFail100)) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("����Ҫ��ѯ��1002���ķ���");
                return cbsRtnInfo;
            }
            ticketInfall[0] = ticketNo100;
            ticketInfall[1] = ticketNoFail100;
            for (int num = 0;num < 2; num++) {
                if (ticketInfall[num].trim().length() <=0) {
                    continue;
                }
                //�����˴���
                //1����������������JSON������
                toat60007 = new TOAT60007();
                toat60007.setBankCode(bankCode);
                toat60007.setType("1002"); //1002���ۺ�Ӧ��ƽ̨���˽����
                toat60007.setNode1( ticketInfall[num].substring(0, ticketInfall[num].length() - 1));
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
                        for (int i = 0; i < resArray.size(); i++) {
                            TIAT60071 tiat60071 = FbiBeanUtils.jsonToBean(resArray.getString(i), TIAT60071.class);
                            //1,�����ж��Ƿ��Ѿ����ڣ�ֻ�в����ڲŲ����µ�����
                            FsLtfTicketInfo tickInfo = this.selectTickInfo(tiat60071);
                            if (tickInfo == null) {
                                logger.info("������ѯ����1002�ۺ�ƽ̨���˽�������ţ�" + tiat60071.getTicketNo() + "���ز�����");
                            } else {
                                tickInfo.setChkActDt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                                tickInfo.setQdfChkFlag(tiat60071.getLedgerState().trim());
                                updateTickInfoChk(tickInfo);
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
            }
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info("������ѯ1002ʧ�ܣ�" + e.getMessage());
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
        client.doPost(msgReq,30000);
        return msgReq.getResdata();
    }

    private void deleteBillQry(FsLtfVchQry billQry) {
        FsLtfVchQryMapper mapper = session.getMapper(FsLtfVchQryMapper.class);
        FsLtfVchQryExample example = new FsLtfVchQryExample();
        example.createCriteria().andNode1EqualTo(billQry.getNode1()).andNode2EqualTo(billQry.getNode2());
        mapper.deleteByExample(example);
    }

    //�жϸ������Ƿ��Ѿ�����
    private List<FsLtfTicketInfo> qryTicket100() {
        CommonMapper mapper = session.getMapper(CommonMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.qryTicket100();
        return infoList;
    }

    private List<FsLtfTicketInfo> qryTicketFail100() {
        CommonMapper mapper = session.getMapper(CommonMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.qryTicketFail100();
        return infoList;
    }

    //��ѯ��ɫ���˳ɹ�������
    private FsLtfTicketInfo selectTickInfo(TIAT60071 tiat60071) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andTicketNoEqualTo(tiat60071.getTicketNo()).andHostChkFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        if (infoList.size() > 0) {
            return infoList.get(0);
        } else {
            return null;
        }
    }

    // �����ۺ�ƽ̨������±��ض��˽��
    private void updateTickInfoChk(FsLtfTicketInfo fsLtfTicketInfo) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andPkidEqualTo(fsLtfTicketInfo.getPkid()).
                andTicketNoEqualTo(fsLtfTicketInfo.getTicketNo()).
                andHostChkFlagEqualTo("1");
        mapper.updateByExampleSelective(fsLtfTicketInfo, example);
    }
}
