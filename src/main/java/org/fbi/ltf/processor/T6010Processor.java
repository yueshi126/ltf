package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 缴款
 * Created by Thinkpad on 2015/11/3.
 */
public class T6010Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6010 tia = new CbsTia6010();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6010) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6010");
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

    private CbsRtnInfo processTxn(CbsTia6010 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        FsLtfTicketInfo ticketInfo = new FsLtfTicketInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、判断票据是否已经缴款
            List<FsLtfTicketInfo> infoList = selectTicketInfo(tia);
            if (infoList.size() > 0) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该罚单已缴款，不能重复缴款");
                return cbsRtnInfo;
            }
            String handleDept = tia.getHandleDept();
            FsLtfPoliceOrgComp orgComp = selectOrg(handleDept);
            if (orgComp == null) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该处罚机构不存在");
                return cbsRtnInfo;
            }
            tia.setHandleDept(orgComp.getOrgCode());
            String callDept = tia.getCallDept();
            if (!StringUtils.isEmpty(callDept)) {
                orgComp = selectOrg(callDept);
                if (orgComp != null) {
                    tia.setHandleDept(orgComp.getOrgCode());
                }
            }
            //3,判断该票据是否属于柜面票据
            String dept = request.getHeader("branchId");
            String billNo = tia.getBillNo();
            FsLtfVchStore vchStore = selectVchStore(dept, billNo);
            if (vchStore == null) {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该机构没有当前票号，不允许处理");
                return cbsRtnInfo;
            }
            FbiBeanUtils.copyProperties(tia, ticketInfo);
            // 罚单号16位,最后一位是校验位直接取消用15录入,与海博接口要求全部用15位,这样统一用15位录入数据库
            if (ticketInfo.getTicketNo().length() == 16) {
                ticketInfo.setByzd1(ticketInfo.getTicketNo());
                ticketInfo.setTicketNo(ticketInfo.getTicketNo().substring(0, 15));
            }
            ticketInfo.setBankCode(ProjectConfigManager.getInstance().getProperty("ltf.bank.code"));
//            交易日期
            ticketInfo.setTransTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            if (ticketInfo.getTicketTime().length() == 14) {
                Date tickTIme = new SimpleDateFormat("yyyyMMddHHmmss").parse(ticketInfo.getTicketTime());
                ticketInfo.setTicketTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tickTIme));
            }

            ticketInfo.setHostBookFlag("1");
            ticketInfo.setOperDate(request.getHeader("txnTime").substring(0, 8));
            ticketInfo.setOperTime(request.getHeader("txnTime").substring(8, 14));
            ticketInfo.setBranchId(request.getHeader("branchId"));
            ticketInfo.setBankTake(request.getHeader("branchId"));
            ticketInfo.setOperid(request.getHeader("tellerId"));
            ticketInfo.setCbsActSerial(request.getHeader("serialNo"));
            ticketInfo.setOrderNo(request.getHeader("txnTime").substring(0, 8) + request.getHeader("serialNo"));
            if (tia.getItems().size() > 0) {
                String ordercharger_tmp = "";
                for (CbsTia6010Item item : tia.getItems()) {
                    if (item != null) {
                        // 20180313 修改收费
//                        FsLtfChargeName chargeName = selectChargeName(item.getItemCode());
                        FsLtfChargeName chargeName = null;
                        if (chargeName != null) {
                            if (!"".equals(ordercharger_tmp)) {//todo
                                ordercharger_tmp = ordercharger_tmp + "," + chargeName.getChargeCode();
                            } else {
                                ordercharger_tmp = chargeName.getChargeCode();
                            }
                        } else {
                            if (!"".equals(ordercharger_tmp)) {//todo
                                ordercharger_tmp = ordercharger_tmp + ",3702" + item.getItemCode();
                            } else {
                                ordercharger_tmp = "3702" + item.getItemCode();
                            }
                        }

                    }
                }
                ticketInfo.setOrderCharges(ordercharger_tmp);
            }
            insertTicketInfo(ticketInfo);

            List<CbsTia6010Item> itemList = tia.getItems();
            for (CbsTia6010Item item : itemList) {
                FsLtfTicketItem ticketItem = new FsLtfTicketItem();
                FbiBeanUtils.copyProperties(item, ticketItem);
                ticketItem.setItemCode("3702" + ticketItem.getItemCode());
                ticketItem.setInfoId(ticketInfo.getPkid());
                ticketItem.setTicketNo(tia.getTicketNo());
                insertTicketItem(ticketItem);
            }
            session.commit();
            logger.info("缴款成功！罚单号为：" + tia.getTicketNo());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
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
    private List<FsLtfTicketInfo> selectTicketInfo(CbsTia6010 tia) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andTicketNoEqualTo(tia.getTicketNo()).andHostBookFlagEqualTo("1");
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    //插入库存
    private void insertTicketInfo(FsLtfTicketInfo ticketInfo) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        mapper.insert(ticketInfo);
    }

    private void insertTicketItem(FsLtfTicketItem ticketItem) {
        FsLtfTicketItemMapper mapper = session.getMapper(FsLtfTicketItemMapper.class);
        mapper.insert(ticketItem);
    }

    private FsLtfPoliceOrgComp selectOrg(String orgCode) {
        FsLtfPoliceOrgCompMapper mapper = session.getMapper(FsLtfPoliceOrgCompMapper.class);
        FsLtfPoliceOrgCompExample example = new FsLtfPoliceOrgCompExample();
        example.createCriteria().andOrgCodeShortEqualTo(orgCode);
        List<FsLtfPoliceOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }

    //查询库存表
    private FsLtfVchStore selectVchStore(String branchId, String billNo) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(branchId).andBusCodeEqualTo("2")
                .andBillCodeEqualTo("3005").
                andChannelEqualTo("1")
                .andVchStartNoLessThanOrEqualTo(billNo).andVchEndNoGreaterThanOrEqualTo(billNo);
        storeExample.setOrderByClause("vch_start_no");
        List<FsLtfVchStore> vchStoreList = storeMapper.selectByExample(storeExample);
        if (vchStoreList.size() > 0) {
            return vchStoreList.get(0);
        } else {
            return null;
        }
    }

//    private FsLtfChargeName selectChargeName(String ticketCode) {
//        FsLtfChargeNameExample example = new FsLtfChargeNameExample();
//        example.createCriteria().andTicketCodeEqualTo(ticketCode).andIsCancelIsNull();
//        example.or().andIsCancelNotEqualTo("1");
//        FsLtfChargeNameMapper mapper = session.getMapper(FsLtfChargeNameMapper.class);
//        List<FsLtfChargeName> infoList = mapper.selectByExample(example);
//        return infoList.size() > 0 ? (FsLtfChargeName) infoList.get(0) : null;
//    }
}
