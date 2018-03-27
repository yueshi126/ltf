package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6020Request.CbsTia6020;
import org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020;
import org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020Item;
import org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020SubItem;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 网络票据打印
 * Created by Thinkpad on 2015/11/3.
 */
public class T6020Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6020 tia = new CbsTia6020();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6020) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6020");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6020 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、首先判断打印方式，若是套打，则根据日期查询，否则按照票据号和罚单号查询。
            //同时检查该机构的库存情况，小于一定的数量，进行预警
            String branchId = request.getHeader("branchId");
            FsLtfOrgComp orgComp = selectOrg(branchId);
            if (orgComp == null) {
                logger.info("网点号为：" + branchId + "支行，没有对应信息。");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("网点不是网络票据打印机构");
                return cbsRtnInfo;
            }
            String orgCode = orgComp.getOrgCode();
            List<FsLtfVchStore> vchStoreList = selectVchStoreList(branchId);
            String warningCount = ProjectConfigManager.getInstance().getProperty("warning.voucher.count");
            int warCount = Integer.parseInt(warningCount);
            int leaveCount = 0;
            boolean warFlag = false;
            for (FsLtfVchStore store : vchStoreList) {
                leaveCount = +store.getVchCount();
            }
            if (new BigDecimal(warCount).compareTo(new BigDecimal(leaveCount)) > 0) {
                warFlag = true;
            }
            List<FsLtfVchOut> vchOutList = new ArrayList<>();
            List<FsLtfTicketInfo> ticketInfoList = new ArrayList<>();
            if ("1".equals(tia.getOrg())) { //互联网
                if ("1".equals(tia.getPrintType())) { //
                    // 按日期打印
                    String startDate = tia.getStartDate();
                    String endDate = tia.getEndDate();
                    vchOutList = selectVchOutList(startDate, endDate, orgCode);
                } else {
                    if (StringUtils.isEmpty(tia.getBillNo()) && StringUtils.isEmpty(tia.getTicketNo())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("票据和罚款单号不能同时为空");
                        return cbsRtnInfo;
                    }
                    vchOutList = selectVchOut(tia.getBillNo(), tia.getTicketNo(), orgCode);
                }

            } else if ("2".equals(tia.getOrg())) {
                // 柜面票据
                if ("1".equals(tia.getPrintType())) { // 按日期打印
                    String startDate = tia.getStartDate();
                    String endDate = tia.getEndDate();
                    ticketInfoList = selectTickNoList(startDate, endDate, orgCode);
                } else {
                    if (StringUtils.isEmpty(tia.getBillNo()) && StringUtils.isEmpty(tia.getTicketNo())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("票据和罚款单号不能同时为空");
                        return cbsRtnInfo;
                    }
                }
                ticketInfoList = selectTicketInfo(tia.getBillNo(), tia.getTicketNo(), orgCode);
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("票据来源有误");
            }
            if (vchOutList.size() > 0 || ticketInfoList.size() > 0) {
                List<FsLtfVchOut> vchOutAllList = new ArrayList<>();
                if (vchOutList.size() > 0) {
                    vchOutAllList.addAll(vchOutList);
                }
                if (ticketInfoList.size() > 0) {
                    for (FsLtfTicketInfo fsLtfTicketInfo : ticketInfoList) {
                        FsLtfVchOut fsLtfVchOutTmp = new FsLtfVchOut();
                        FbiBeanUtils.copyProperties(fsLtfTicketInfo, fsLtfVchOutTmp);
                        fsLtfVchOutTmp.setOprDate(fsLtfTicketInfo.getTransTime());
                        fsLtfVchOutTmp.setPayment(fsLtfTicketInfo.getAmount());
                        vchOutAllList.add(fsLtfVchOutTmp);
                    }
                }
                String starringRespMsg = generateCbsRespMsg(vchOutAllList, warFlag, leaveCount);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(starringRespMsg);
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("当前没有需要打印的票据");
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

    private void updateSelectedVchOut(FsLtfVchOut vchOut) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        mapper.updateByPrimaryKeySelective(vchOut);
    }

    private List<FsLtfVchStore> selectVchStoreList(String branchId) {
        FsLtfVchStoreMapper mapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample example = new FsLtfVchStoreExample();
        example.createCriteria().andBranchIdEqualTo(branchId);
        List<FsLtfVchStore> vchStoreList = mapper.selectByExample(example);
        return vchStoreList;
    }

    private List<FsLtfVchOut> selectVchOut(String billNo, String ticketNo, String orgCode) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        if ((!StringUtils.isEmpty(billNo)) && (!StringUtils.isEmpty(ticketNo))) {
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andTicketNoEqualTo(ticketNo)
                    .andBankMareEqualTo(orgCode);
        } else if (!StringUtils.isEmpty(billNo)) {
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andBankMareEqualTo(orgCode);
        } else if (!StringUtils.isEmpty(ticketNo)) {
            example.createCriteria()
                    .andTicketNoEqualTo(ticketNo)
                    .andBankMareEqualTo(orgCode).andBillNoIsNotNull();
        }
        example.setOrderByClause(" bill_no");
        List<FsLtfVchOut> infos = mapper.selectByExample(example);
        return infos;
    }

    // 罚单或者票据号获取罚单信息
    private List<FsLtfTicketInfo> selectTicketInfo(String billNo, String ticketNo, String orgCode) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        if ((!StringUtils.isEmpty(billNo)) && (!StringUtils.isEmpty(ticketNo))) {
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andTicketNoEqualTo(ticketNo)
                    .andBranchIdEqualTo(orgCode);
        } else if (!StringUtils.isEmpty(billNo)) {
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andBranchIdEqualTo(orgCode);
        } else if (!StringUtils.isEmpty(ticketNo)) {
            example.createCriteria()
                    .andTicketNoEqualTo(ticketNo)
                    .andBranchIdEqualTo(orgCode).andBillNoIsNotNull();
        }
        example.setOrderByClause(" bill_no");
        List<FsLtfTicketInfo> infos = mapper.selectByExample(example);
        return infos;
    }

    private List<FsLtfVchOut> selectVchOutList(String startDate, String endDate, String orgCode) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        example.createCriteria().andOprDateGreaterThanOrEqualTo(startDate).andOprDateLessThanOrEqualTo(endDate).andBankMareEqualTo(orgCode).andPrintTimeIsNull().andBillNoIsNotNull();
        example.setOrderByClause(" bill_no");
        List<FsLtfVchOut> infos = mapper.selectByExample(example);
        return infos;
    }

    // 获取柜面罚单信息+票据号
    private List<FsLtfTicketInfo> selectTickNoList(String startDate, String endDate, String orgCode) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andOperDateGreaterThan(startDate).andOperDateLessThanOrEqualTo(endDate).andBranchIdEqualTo(orgCode).andPrintTimeIsNull().andBillNoIsNotNull();
        example.setOrderByClause(" bill_no");
        List<FsLtfTicketInfo> infos = mapper.selectByExample(example);
        return infos;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfVchOut> vchOutList, boolean flag, int count) {
        CbsToa6020 cbsToa = new CbsToa6020();
        if (flag) {
            cbsToa.setRemark("网络票据剩余：" + String.valueOf(count) + "张，建议你及时领取票据，以免影响使用！");
        }
        cbsToa.setItemNum(String.valueOf(vchOutList.size()));
        List<CbsToa6020Item> cbsToaItems = new ArrayList<>();
        long preBillNo = 0;
        long currBillNo = 0;
        long serialNo = 0;
        for (FsLtfVchOut vchOut : vchOutList) {
            CbsToa6020Item cbsToaItem = new CbsToa6020Item();
//            List<FsLtfVchOutItem> vchOutItems = selectVchOutItemList(vchOut.getPkid());
            StringBuffer stringBuffer = new StringBuffer();
//            stringBuffer.append(""+vchOutItems.size());
            stringBuffer.append("");
//            for(FsLtfVchOutItem outItem:vchOutItems){
//                stringBuffer.append("#").append(outItem.getItemCode());
//                if(StringUtils.isEmpty(outItem.getItemName())){
//                    stringBuffer.append("#").append("交通违法罚没收入");
//                }else{
//                    stringBuffer.append("#").append(outItem.getItemName());
//                }
//                stringBuffer.append("#").append(outItem.getAmount());
//                stringBuffer.append("#");
//            }
            cbsToaItem.setItems(stringBuffer.toString());
            FbiBeanUtils.copyProperties(vchOut, cbsToaItem);
            currBillNo = Long.parseLong(cbsToaItem.getBillNo());
            //根据票号生成相应的序号
            if (preBillNo == 0) {
                serialNo = 1;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            } else if (preBillNo + 1 == currBillNo) {
                serialNo++;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            } else {
                long tempNo = currBillNo - preBillNo;
                serialNo += tempNo;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            }

            cbsToaItems.add(cbsToaItem);
        }
        cbsToa.setItemNum("" + cbsToaItems.size());
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

    private List<FsLtfVchOutItem> selectVchOutItemList(String infoId) {
        FsLtfVchOutItemMapper itemMapper = session.getMapper(FsLtfVchOutItemMapper.class);
        FsLtfVchOutItemExample example = new FsLtfVchOutItemExample();
        example.createCriteria().andInfoIdEqualTo(infoId);
        List<FsLtfVchOutItem> itemList = itemMapper.selectByExample(example);
        return itemList;
    }

    private FsLtfOrgComp selectOrg(String deptCode) {
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andDeptCodeEqualTo(deptCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }
}
