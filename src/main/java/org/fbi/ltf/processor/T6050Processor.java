package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6020Request.CbsTia6020;
import org.fbi.ltf.domain.cbs.T6050Request.CbsTia6050;
import org.fbi.ltf.domain.cbs.T6050Response.CbsToa6050;
import org.fbi.ltf.domain.tps.TOAT60003;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfChargeNameMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketItemMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询缴费项目信息
 * Created by Thinkpad on 2015/11/3.
 */
public class T6050Processor extends AbstractTxnProcessor{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6050 tia = new CbsTia6050();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6050) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6050");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常"+e.getMessage());
        }
    }

    private CbsRtnInfo processTxn(CbsTia6050 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、查看相对应的项目代码是否存在记录
            if(StringUtils.isEmpty(tia.getTicketCode())){
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("项目代码不能为空");
            }
            FsLtfChargeName chargeName = selectchargeName(tia.getTicketCode());
            if(chargeName!=null){
                session.commit();
                String cbsRespMsg = generateCbsRespMsg(chargeName);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }else{
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有对应的项目名称");
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
    private FsLtfChargeName selectchargeName(String ticketCode){
        FsLtfChargeNameExample example = new FsLtfChargeNameExample();
        example.createCriteria().andTicketCodeEqualTo(ticketCode).andIsCancelIsNull();
        example.or().andIsCancelNotEqualTo("1");
        FsLtfChargeNameMapper mapper =session.getMapper(FsLtfChargeNameMapper.class);
        List<FsLtfChargeName> infoList = mapper.selectByExample(example);
        return infoList.size()>0?(FsLtfChargeName)infoList.get(0):null;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(FsLtfChargeName chargeName) {
        CbsToa6050 cbsToa = new CbsToa6050();
        FbiBeanUtils.copyProperties(chargeName, cbsToa, true);
        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败."+e.getMessage());
        }
        return cbsRespMsg;
    }
}
