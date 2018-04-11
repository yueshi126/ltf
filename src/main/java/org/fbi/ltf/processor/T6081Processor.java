package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6081Request.CbsTia6081;
import org.fbi.ltf.domain.cbs.T6081Response.CbsToa6081;
import org.fbi.ltf.domain.cbs.T6081Response.CbsToa6081Item;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 票据未上传数据查询
 * Created by Thinkpad on 2015/11/3.
 */
public class T6081Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6081 tia = new CbsTia6081();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6081) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6081");
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
            throw new RuntimeException("交易处理异常", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6081 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、查看相对应的项目代码是否存在记录
            List<FsLtfTicketInfo> infoList = this.selectTicketInfo(tia.getTxnDate(), tia.getOperNo(), tia.getFlag());
            if (infoList.size() == 0) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有交通罚款单信息");
            } else {
                String cbsRespMsg = generateCbsRespMsg(infoList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
                session.commit();
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


    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfTicketInfo> infoList) {
        CbsToa6081 cbsToa = new CbsToa6081();
        cbsToa.setItemNum(String.valueOf(infoList.size()));
        List<CbsToa6081Item> cbsToaItems = new ArrayList<>();
        for (FsLtfTicketInfo info : infoList) {
            CbsToa6081Item item = new CbsToa6081Item();
            item.setTicketNo(info.getTicketNo());
            item.setBillNo(info.getBillNo());
            item.setTxnAmt(info.getAmount().toString());
            item.setOperNo(info.getOperid());
            cbsToaItems.add(item);
        }
        cbsToa.setItems(cbsToaItems);
        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败.", e);
        }
        return cbsRespMsg;
    }

    //判断该数据是否已经存在
    private List<FsLtfTicketInfo> selectTicketInfo(String operDate, String operNo, String flag) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        FsLtfTicketInfoExample.Criteria criteria = example.createCriteria();
        if (!StringUtils.isEmpty(flag)) {
            if (flag.equals("N"))
                criteria.andQdfBookFlagIsNull().andHostBookFlagNotEqualTo("2");
            if (flag.equals("Y"))
                criteria.andQdfBookFlagEqualTo("1");
        }
        if (!StringUtils.isEmpty(operNo)) {
            criteria.andOperidEqualTo(operNo);
        }
        if (!StringUtils.isEmpty(operDate)) {
            criteria.andOperDateEqualTo(operDate);
        }

        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }
}
