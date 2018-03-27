package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6097Request.CbsTia6097;
import org.fbi.ltf.domain.cbs.T6097Request.CbsTia6097Item;
import org.fbi.ltf.domain.tps.TOAT60097;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.enums.VouchStatus;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.LTFTools;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.dao.common.FsVoucherMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 2.3.16	2.3.14	柜台缴费系统勘误接口 写入
 * Created by Thinkpad on 2018/02/28
 */
public class T6097Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");
        CbsTia6097 tia = new CbsTia6097();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6097) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6097");
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

    public CbsRtnInfo processTxn(CbsTia6097 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        List<FsLtfTicketInfo> infoList;
        FsLtfTicketInfo fsLtfTicketInfo = new FsLtfTicketInfo();
        List<FsLtfTicketItem> fsLtfTicketItemList = new ArrayList<FsLtfTicketItem>();
        String starringRespMsg = "";
        // 收费代码多个用 ，隔开
        String orderCharge = "";
        TOAT60097 toat60097 = new TOAT60097();
        String dept = request.getHeader("branchId");
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            infoList = selectTicketInfo(tia);
            //本地处理
            //1、查看相对应的项目代码是否存在记录
            if (infoList.size() != 1) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("罚单号不存在或者信息已变更");
                return cbsRtnInfo;
            } else {
                FbiBeanUtils.copyProperties(tia, fsLtfTicketInfo);
//                toat60095.setTransTime(LTFTools.dateFormat(toat60095.getTransTime(), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));

                fsLtfTicketInfo.setTicketTime(LTFTools.dateFormat(fsLtfTicketInfo.getTicketTime(), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
                fsLtfTicketInfo.setTransTime(LTFTools.dateFormat(fsLtfTicketInfo.getTransTime(), "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss"));
                //收费项目代码
                for (CbsTia6097Item tmpItem : tia.getItems()) {
                    orderCharge += "3702" + tmpItem.getItemCode() + ",";
                }
                fsLtfTicketInfo.setOrderCharges(orderCharge.substring(0, orderCharge.length() - 1));
                String billNo = tia.getBillNo();
                Boolean isNew = false;
                //判断是否录入新票号
                if ((StringUtils.isEmpty(tia.getBillNo()))) {
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("勘误录入票据号不能为空");
                    return cbsRtnInfo;
                }
                if ((StringUtils.isNotEmpty(tia.getBillNo()))) {
                    if (StringUtils.isEmpty(infoList.get(0).getBillNo())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("罚单没有对应的票据号");
                        return cbsRtnInfo;
                    } else {
                        if (!(tia.getBillNo().equals(infoList.get(0).getBillNo()))) {
                            isNew = true;
                        } else {
                            isNew = false;
                        }
                    }
                }
                if (isNew) {
                    FsLtfVchStore vchStore = selectVchStore(dept, tia.getBillNo());
                    if (vchStore == null) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("该机构没有当前票号，不允许处理");
                        return cbsRtnInfo;
                    }
                    //核销票据
                    String startNo = billNo;
                    String endNo = billNo;
                    String operCode = request.getHeader("tellerId");
                    VouchStatus status = VouchStatus.USED;
                    List<FsLtfVchStore> storeParamList = new ArrayList<FsLtfVchStore>();
                    doVchUseOrCancel(dept, startNo, endNo, storeParamList);
                    String remark = status.getTitle();
                    for (final FsLtfVchStore storeParam : storeParamList) {
                        FsLtfVchStore storeDb = selectVchStoreByStartNo(dept, storeParam.getVchStartNo());
                        long startNoDb = Long.parseLong(storeDb.getVchStartNo());
                        long endNoDb = Long.parseLong(storeDb.getVchEndNo());
                        long startNoParam = Long.parseLong(storeParam.getVchStartNo());
                        long endNoParam = Long.parseLong(storeParam.getVchEndNo());

                        deleteVchStore(storeDb.getPkid());
                        if (startNoDb != startNoParam || endNoDb != endNoParam) {//处理整个记录
                            if (startNoParam != startNoDb) {
                                insertVoucherStore(startNoParam - startNoDb, storeDb.getVchStartNo(),
                                        getStandLengthForVoucherString(startNoParam - 1), dept, remark, operCode);
                            }
                            if (endNoParam != endNoDb) {
                                insertVoucherStore(endNoDb - endNoParam, getStandLengthForVoucherString(endNoParam + 1),
                                        storeDb.getVchEndNo(), dept, remark, operCode);
                            }
                        }
                    }
                    insertVoucherJournal(Long.parseLong(endNo) - Long.parseLong(startNo) + 1, startNo, endNo, dept, status, remark, operCode);
                    //总分核对
                    if (!verifyVchStoreAndJrnl(dept)) {
                        logger.info("库存总分不符");
                        throw new RuntimeException("库存总分不符！");
                    }
                }
                //更新缴款主表
                fsLtfTicketInfo.setBillNo(billNo);
                updateTicketInfo(fsLtfTicketInfo);
                deleteTicketItem(fsLtfTicketInfo);
                List<CbsTia6097Item> itemList = tia.getItems();
                List<CbsTia6097Item> tmpItemList = new ArrayList<CbsTia6097Item>();
                for (CbsTia6097Item item : itemList) {
                    FsLtfTicketItem ticketItem = new FsLtfTicketItem();
                    FbiBeanUtils.copyProperties(item, ticketItem);
                    ticketItem.setItemCode("3702" + ticketItem.getItemCode());
                    ticketItem.setInfoId(fsLtfTicketInfo.getPkid());
                    ticketItem.setTicketNo(tia.getTicketNo());
                    isertTicketItem(ticketItem);
                }

                FbiBeanUtils.copyProperties(tia, toat60097);
                toat60097.setItemUnicode(orderCharge.substring(0, orderCharge.length() - 1));
                toat60097.setItemUnicode(fsLtfTicketInfo.getOrderCharges());
                String bankCode = ProjectConfigManager.getInstance().getProperty("tps.server.bankCode");
                toat60097.setBankCode(bankCode);
                //2、发送数据
                String respBaseStr = processThirdPartyServer(toat60097);
                //3、解析数据，进行处理
                String respStr = FbiBeanUtils.decode64(respBaseStr);
                TpsMsgRes msgRes = new TpsMsgRes();
                if (!StringUtils.isEmpty(respStr)) {
                    msgRes = FbiBeanUtils.jsonToBean(respStr, TpsMsgRes.class);
                    String resultCode = msgRes.getCode();
                    if (("0000".equals(msgRes.getCode()))) { //交易处理成功
                        session.commit();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                        cbsRtnInfo.setRtnMsg(starringRespMsg);
                    } else {
                        session.rollback();
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                        cbsRtnInfo.setRtnMsg(msgRes.getComment());
                    }
                }
            }
            session.commit();
            return cbsRtnInfo;

        } catch (Exception e) {
            session.rollback();
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("交易处理异常");
            logger.info("6096交易处理异常" + e.getMessage().toString());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // 根据pkid 查罚单信息
    private List<FsLtfTicketInfo> selectTicketInfo(CbsTia6097 tia) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        List<String> qdfChkFlagList = new ArrayList<String>();
        qdfChkFlagList.add("1");
        qdfChkFlagList.add("8");
        FsLtfTicketInfoExample.Criteria criteria = example.createCriteria();
        criteria.andPkidEqualTo(tia.getPkid()).andHostBookFlagEqualTo("1").andQdfChkFlagNotIn(qdfChkFlagList);
        FsLtfTicketInfoExample.Criteria criteria2 = example.createCriteria();
        criteria2.andPkidEqualTo(tia.getPkid()).andHostBookFlagEqualTo("1").andQdfBookFlagIsNull();
        example.or(criteria2);
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    private String processThirdPartyServer(TOAT60097 toat60097) throws Exception {
        TpsMsgReq msgReq = new TpsMsgReq();
        // 加密
        String reqdata = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(toat60097));
        msgReq.setReqdata(reqdata);
        String strPayerAccount = ProjectConfigManager.getInstance().getProperty("tps.server.reportCounterErrata");
        msgReq.setUri(msgReq.getHost() + strPayerAccount);
        LnkHttpClient client = new LnkHttpClient();
        client.doPost(msgReq, 30000);
        return msgReq.getResdata();
    }

    //查询库存表
    private FsLtfVchStore selectVchStore(String branchId, String billNo) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(branchId).andBusCodeEqualTo("2").andBillCodeEqualTo("3005").
                andVchStartNoLessThanOrEqualTo(billNo).andVchEndNoGreaterThanOrEqualTo(billNo);
        storeExample.setOrderByClause("vch_start_no");
        List<FsLtfVchStore> vchStoreList = storeMapper.selectByExample(storeExample);
        if (vchStoreList.size() > 0) {
            return vchStoreList.get(0);
        } else {
            return null;
        }
    }

    private void doVchUseOrCancel(String instNo, String startNo, String endNo, List<FsLtfVchStore> storeParamList) {
        FsLtfVchStore storeDb = selectVchStoreByStartNo(instNo, startNo);
        FsLtfVchStore storeParam = new FsLtfVchStore();
        storeParam.setPkid(storeDb.getPkid());
        storeParam.setVchStartNo(startNo);
        if (storeDb.getVchEndNo().compareTo(endNo) < 0) {
            storeParam.setVchEndNo(storeDb.getVchEndNo());
            storeParam.setVchCount((int) (Long.parseLong(storeDb.getVchEndNo()) - Long.parseLong(startNo) + 1));
            storeParamList.add(storeParam);
            String vchNo = getStandLengthForVoucherString(Long.parseLong(storeDb.getVchEndNo()) + 1);
            doVchUseOrCancel(instNo, vchNo, endNo, storeParamList);
        } else {
            storeParam.setVchEndNo(endNo);
            storeParam.setVchCount((int) (Long.parseLong(endNo) - Long.parseLong(startNo) + 1));
            storeParamList.add(storeParam);
        }
    }

    //根据起号查找数据库中含有此号码的库存记录
    private FsLtfVchStore selectVchStoreByStartNo(String instNo, String startNo) {
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(instNo)
                .andVchStartNoLessThanOrEqualTo(startNo)
                .andVchEndNoGreaterThanOrEqualTo(startNo);
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> storesTmp = storeMapper.selectByExample(storeExample);
        if (storesTmp.size() != 1) {
//            throw new RuntimeException("未找到库存记录。");
            return null;
        }
        return storesTmp.get(0);
    }

    //补票据号长度
    private String getStandLengthForVoucherString(long vchno) {
        String vchNo = "" + vchno;
        String vch_length = ProjectConfigManager.getInstance().getProperty("voucher.no.length");
        int vchnoLen = Integer.parseInt(vch_length);
        if (vchNo.length() != vchnoLen) { //长度不足 左补零
            vchNo = StringUtils.leftPad(vchNo, vchnoLen, "0");
        }
        return vchNo;
    }

    private void insertVoucherJournal(long vchCnt, String startNo, String endNo, String branchId,
                                      VouchStatus status, String remark, String operCode) {
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnl vchJrnl = new FsLtfVchJrnl();
        vchJrnl.setVchCount((int) vchCnt);
        vchJrnl.setVchStartNo(startNo);
        vchJrnl.setVchEndNo(endNo);

        vchJrnl.setBranchId(branchId);
        vchJrnl.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchJrnl.setOprNo(operCode);
        vchJrnl.setRecversion(0);
        vchJrnl.setRemark(remark);
        vchJrnl.setBusCode("2");
        vchJrnl.setBillCode("3005");

        vchJrnl.setVchState(status.getCode());
        mapper.insert(vchJrnl);
    }

    private void deleteVchStore(String pkid) {
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.deleteByPrimaryKey(pkid);
    }

    //更新数据
    private void updateTicketInfo(FsLtfTicketInfo ticketInfo) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andPkidEqualTo(ticketInfo.getPkid());
        ticketInfo.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        ticketInfo.setOperTime(new SimpleDateFormat("HHmmss").format(new Date()));
        mapper.updateByExampleSelective(ticketInfo, example);
    }

    private void deleteTicketItem(FsLtfTicketInfo ticketInfo) {
        FsLtfTicketItemMapper mapper = session.getMapper(FsLtfTicketItemMapper.class);
        FsLtfTicketItemExample example = new FsLtfTicketItemExample();
        example.createCriteria().andInfoIdEqualTo(ticketInfo.getPkid()).andTicketNoEqualTo(ticketInfo.getTicketNo());
        mapper.deleteByExample(example);
    }

    private void insertVoucherStore(long vchCnt, String startNo, String endNo, String instNo, String remark, String operCode) {
        FsLtfVchStore vchStore = new FsLtfVchStore();
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        vchStore.setVchCount((int) vchCnt);
        vchStore.setVchStartNo(startNo);
        vchStore.setVchEndNo(endNo);
        vchStore.setBankCode("CCB");
        vchStore.setBusCode("2");
        vchStore.setBillCode("3005");
        vchStore.setBranchId(instNo);
        vchStore.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchStore.setOprNo(operCode);
        vchStore.setRecversion(0);
        vchStore.setRemark(remark);
        storeMapper.insert(vchStore);
    }

    private boolean verifyVchStoreAndJrnl(String instNo) {
        FsVoucherMapper voucherMapper = session.getMapper(FsVoucherMapper.class);
        int store = voucherMapper.selectVchStoreTotalNum(instNo);
        int jrnl = voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.RECEIVED.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.OUTSTORE.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.USED.getCode())
                - voucherMapper.selectVchJrnlTotalNum(instNo, VouchStatus.CANCEL.getCode());
        return store == jrnl;
    }

    // 明细重新写入
    private void isertTicketItem(FsLtfTicketItem ticketItem) {
        FsLtfTicketItemMapper mapper = session.getMapper(FsLtfTicketItemMapper.class);
        FsLtfTicketItemExample example = new FsLtfTicketItemExample();
        example.createCriteria().andInfoIdEqualTo(ticketItem.getInfoId()).andTicketNoEqualTo(ticketItem.getTicketNo());
        ticketItem.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        ticketItem.setOperTime(new SimpleDateFormat("HHmmss").format(new Date()));
        mapper.insert(ticketItem);
    }


}
