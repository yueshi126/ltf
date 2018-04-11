package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6098Request.CbsTia6098;
import org.fbi.ltf.domain.cbs.T6098Response.CbsToa6098;
import org.fbi.ltf.domain.cbs.T6098Response.CbsToa6098Item;
import org.fbi.ltf.enums.ChkFlagCode;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.LTFTools;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 查询挂账、罚单信息查询
 * Created by Thinkpad on 2018/02/28
 */
public class T6098Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");
        CbsTia6098 tia = new CbsTia6098();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6098) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6098");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
//            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }


    }

    public CbsRtnInfo processTxn(CbsTia6098 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        List<FsLtfTicketInfo> infoList;
        FsLtfTicketInfo fsLtfTicketInfo = new FsLtfTicketInfo();
        String dept = request.getHeader("branchId");
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            infoList = selectTicketInfo(tia);
            //本地处理
            //1、查看相对应的项目代码是否存在记录
            if (infoList.size() < 1) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有罚单信息");
                return cbsRtnInfo;
            } else {
                String cbsRespMsg = generateCbsRespMsg(infoList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
                return cbsRtnInfo;
            }
        } catch (Exception e) {
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle());
            logger.info("6096 交易处理 异常" + e.getMessage().toString());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // 根据pkid 查罚单信息
    private List<FsLtfTicketInfo> selectTicketInfo(CbsTia6098 tia) {
        List<FsLtfTicketInfo> infoList = new ArrayList<>();
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        List<String> qdfChkFlagList = new ArrayList<String>();
        qdfChkFlagList.add("1");
        qdfChkFlagList.add("8");
        if (StringUtils.isNotEmpty(tia.getTicketNo())) {
            example.createCriteria().andTicketNoEqualTo(tia.getTicketNo()).andHostBookFlagEqualTo("1");
            infoList = mapper.selectByExample(example);
            return infoList;
        } else {
            if (tia.getFlag() != null) {
                if (tia.getFlag().equals("1")) { // 挂账
                    FsLtfTicketInfoExample.Criteria criteria1 = example.createCriteria();
                    criteria1.andTransTimeBetween(
                            LTFTools.dateFormat(tia.getBegTransTime(), "yyyyMMdd", "yyyy-MM-dd"),
                            LTFTools.datePlusOnedayFormat(tia.getEndTransTime(), "yyyyMMdd", "yyyy-MM-dd"))
                            .andQdfChkFlagNotIn(qdfChkFlagList);
                    example.setOrderByClause("trans_time");
                    int num = mapper.countByExample(example);
                    if (num > 10000) {
                        logger.info("查询数据过大");
                        throw new RuntimeException("查询数据过大！");
                    }
                    infoList = mapper.selectByExample(example);
                } else if (tia.getFlag().equals("2")) { //正常 =所有有效缴款  hostbookflag =‘1’
                    FsLtfTicketInfoExample.Criteria criteria1 = example.createCriteria();
                    criteria1.andTransTimeBetween(
                            LTFTools.dateFormat(tia.getBegTransTime(), "yyyyMMdd", "yyyy-MM-dd"),
                            LTFTools.datePlusOnedayFormat(tia.getEndTransTime(), "yyyyMMdd", "yyyy-MM-dd"))
                            .andHostBookFlagEqualTo("1");
//                FsLtfTicketInfoExample.Criteria criteria2 = example.createCriteria();
//                criteria2.andTransTimeBetween(
//                        LTFTools.dateFormat(tia.getBegTransTime(), "yyyyMMdd", "yyyy-MM-dd"),
//                        LTFTools.datePlusOnedayFormat(tia.getEndTransTime(), "yyyyMMdd", "yyyy-MM-dd"))
//                        .andQdfChkFlagIsNull();
//                example.or(criteria2);
                    example.setOrderByClause("trans_time");
                    int num = mapper.countByExample(example);
                    if (num > 10000) {
                        logger.info("查询数据过大");
                        throw new RuntimeException("查询数据过大！");
                    }
                    infoList = mapper.selectByExample(example);
                }
            } else {
                return infoList;
            }
        }
        return infoList;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfTicketInfo> infoList) {
        CbsToa6098 cbsToa = new CbsToa6098();
        cbsToa.setItemNum(String.valueOf(infoList.size()));
        List<CbsToa6098Item> cbsToaItems = new ArrayList<>();

        for (FsLtfTicketInfo info : infoList) {
            CbsToa6098Item item = new CbsToa6098Item();
            item.setTicketNo(info.getTicketNo());
            item.setBillNo(info.getBillNo());
            item.setTransTime(info.getTransTime());
            item.setAmount(info.getAmount() == null ? "" : info.getAmount().toString());
            item.setTicketAmount(info.getTicketAmount() == null ? "" : info.getTicketAmount().toString());
            item.setOverdueFine(info.getOverdueFine() == null ? "" : info.getOverdueFine().toString());
            item.setPhoneNo(info.getPhoneNo());
            item.setPartyCard(info.getPartyCard());
            item.setOrderCharges(info.getOrderCharges());
            try {
                item.setQdfChkFlag(ChkFlagCode.valueOfAlias(info.getQdfChkFlag().toString()).getTitle());
            } catch (Exception e) {
                item.setQdfChkFlag(info.getQdfChkFlag());
            }
            cbsToaItems.add(item);
        }
        cbsToa.setItems(cbsToaItems);
        try {
            String cbsRespMsg = "";
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
            return cbsRespMsg;
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败." + e.getMessage());
        }

    }

}
