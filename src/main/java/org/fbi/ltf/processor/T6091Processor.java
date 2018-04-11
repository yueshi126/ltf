package org.fbi.ltf.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6091Request.CbsTia6091;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.FsLtfVchOutMapper;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;
import org.fbi.ltf.repository.model.FsLtfVchOut;
import org.fbi.ltf.repository.model.FsLtfVchOutExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ZZP_YY on 2018-03-27.
 * ת��ʧ��֮��ʹ�� ������ˮ���޸�Ʊ�ݶ�������
 */
public class T6091Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6091 tia = new CbsTia6091();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6091) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6091");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6091 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        String preActSerial = tia.getPreActSerial();
        String toDay = new SimpleDateFormat("yyMMddHH").format(new Date());
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            int ct = qryCounterTicketBypreActSerial(preActSerial);
            int nt = qryNetTicketBypreActSerial(preActSerial);
            if (ct > 0) {
            }
            updateCounterTicketBypreActSerial(toDay);
            if (nt > 0) {
                updateNetTicketBypreActSerial(toDay);
            }
            session.commit();
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg("�޸Ķ������ڳɹ���");
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info("�޸Ķ�������ʧ�ܣ�" + e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("�޸Ķ�������ʧ��");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //  ������ˮ
    private int qryCounterTicketBypreActSerial(String preActSerial) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andPreActSerialEqualTo(preActSerial);
        int cnt = mapper.countByExample(example);
        return cnt;
    }

    //  �ۺ�ƽ̨��ˮ
    private int qryNetTicketBypreActSerial(String preActSerial) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        example.createCriteria().andPreActSerialEqualTo(preActSerial);
        int cnt = mapper.countByExample(example);
        return cnt;
    }

    // �޸� ������ˮ ��������
    private int updateCounterTicketBypreActSerial(String chkDate) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfo fsLtfTicketInfo = new FsLtfTicketInfo();
        fsLtfTicketInfo.setChkActDt(chkDate);
        int cnt = mapper.updateByPrimaryKeySelective(fsLtfTicketInfo);
        return cnt;
    }

    // �޸� �ۺ�ƽ̨��ˮ ��ս����
    private int updateNetTicketBypreActSerial(String chkDate) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOut fsLtfVchOut = new FsLtfVchOut();
        fsLtfVchOut.setChkActDt(chkDate);
        int cnt = mapper.updateByPrimaryKeySelective(fsLtfVchOut);
        return cnt;
    }
}
