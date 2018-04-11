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
 * 转账失败之后使用 根据流水号修改票据对账日期
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
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
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
            //本地处理
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
            cbsRtnInfo.setRtnMsg("修改对账日期成功！");
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info("修改对账日期失败：" + e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("修改对账日期失败");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //  柜面流水
    private int qryCounterTicketBypreActSerial(String preActSerial) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andPreActSerialEqualTo(preActSerial);
        int cnt = mapper.countByExample(example);
        return cnt;
    }

    //  综合平台流水
    private int qryNetTicketBypreActSerial(String preActSerial) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        example.createCriteria().andPreActSerialEqualTo(preActSerial);
        int cnt = mapper.countByExample(example);
        return cnt;
    }

    // 修改 柜面流水 对账日期
    private int updateCounterTicketBypreActSerial(String chkDate) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfo fsLtfTicketInfo = new FsLtfTicketInfo();
        fsLtfTicketInfo.setChkActDt(chkDate);
        int cnt = mapper.updateByPrimaryKeySelective(fsLtfTicketInfo);
        return cnt;
    }

    // 修改 综合平台流水 对战日期
    private int updateNetTicketBypreActSerial(String chkDate) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOut fsLtfVchOut = new FsLtfVchOut();
        fsLtfVchOut.setChkActDt(chkDate);
        int cnt = mapper.updateByPrimaryKeySelective(fsLtfVchOut);
        return cnt;
    }
}
