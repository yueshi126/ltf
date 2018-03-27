package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6011Request.CbsTia6011;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfPoliceOrgCompMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketItemMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * �ɿ����
 * Created by Thinkpad on 2015/11/3.
 */
public class T6011Processor extends AbstractTxnProcessor{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6011 tia = new CbsTia6011();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6011) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6011");
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

    private CbsRtnInfo processTxn(CbsTia6011 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        FsLtfTicketInfo ticketInfo = new FsLtfTicketInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            //1���ж�Ʊ���Ƿ��Ѿ��ɿ�
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia.getTxnDate(),tia.getSeqNo());
            if(infoList.size()>0){
                ticketInfo =(FsLtfTicketInfo) infoList.get(0);
                if("1".equals(ticketInfo.getQdfBookFlag())){
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("�÷���Ʊ�����ϴ������ܳ���");
                    return cbsRtnInfo;
                }else{
                    ticketInfo.setHostBookFlag("2");
                    updateTicketInfo(ticketInfo);
                    session.commit();
                    logger.info("�����ɹ�������Ϊ��"+tia.getTxnDate()+",��ˮ��Ϊ��"+tia.getSeqNo());
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                    return cbsRtnInfo;
                }
            }else{
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�÷���û�нɿ���ܳ���");
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
    //�жϸ������Ƿ��Ѿ�����
    private List<FsLtfTicketInfo> selectTicketInfo(String operDate,String seqNo){
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andOperDateEqualTo(operDate).andCbsActSerialEqualTo(seqNo).andHostBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper =session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }
    //������
    private void updateTicketInfo(FsLtfTicketInfo ticketInfo){
        FsLtfTicketInfoMapper mapper =session.getMapper(FsLtfTicketInfoMapper.class);
        mapper.updateByPrimaryKey(ticketInfo);
    }
}
