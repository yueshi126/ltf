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
 * 缴款冲正
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
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia,request);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6011 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        FsLtfTicketInfo ticketInfo = new FsLtfTicketInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、判断票据是否已经缴款
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia.getTxnDate(),tia.getSeqNo());
            if(infoList.size()>0){
                ticketInfo =(FsLtfTicketInfo) infoList.get(0);
                if("1".equals(ticketInfo.getQdfBookFlag())){
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("该罚单票据已上传，不能冲正");
                    return cbsRtnInfo;
                }else{
                    ticketInfo.setHostBookFlag("2");
                    updateTicketInfo(ticketInfo);
                    session.commit();
                    logger.info("冲正成功，日期为："+tia.getTxnDate()+",流水号为："+tia.getSeqNo());
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                    return cbsRtnInfo;
                }
            }else{
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该罚单没有缴款，不能冲正");
                return cbsRtnInfo;
            }
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
    private List<FsLtfTicketInfo> selectTicketInfo(String operDate,String seqNo){
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andOperDateEqualTo(operDate).andCbsActSerialEqualTo(seqNo).andHostBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper =session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }
    //插入库存
    private void updateTicketInfo(FsLtfTicketInfo ticketInfo){
        FsLtfTicketInfoMapper mapper =session.getMapper(FsLtfTicketInfoMapper.class);
        mapper.updateByPrimaryKey(ticketInfo);
    }
}
