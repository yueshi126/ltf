package org.fbi.ltf.server.processor;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.ltf.domain.tps.TIAT60008;
import org.fbi.ltf.domain.tps.TpsMsgResForLtf60002;
import org.fbi.ltf.enums.VouchStatus;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.*;
import org.fbi.ltf.repository.dao.common.FsVoucherMapper;
import org.fbi.ltf.repository.model.*;
import org.fbi.ltf.server.httpserver.Processor;
import org.fbi.ltf.server.httpserver.TxnContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 异常票据变更接口
 * Created by Thinkpad on 2015/11/1.
 */
public class T60002Processor implements Processor{
    private static final Logger logger = LoggerFactory.getLogger(T60002Processor.class);
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;


    @Override
    public void service(TxnContext ctx) {

        Map<String, List<String>> params = ctx.getMapTia();
        String reqData = params.get("reqdata").get(0).toString();
        TpsMsgResForLtf60002 toa = new TpsMsgResForLtf60002();
        try {
            String req = FbiBeanUtils.decode64(reqData);
            logger.info("收到对方请求数据，解析后为："+req);
            TIAT60008 tia = new TIAT60008();
            tia = FbiBeanUtils.jsonToBean(req,TIAT60008.class);
            processTxn(tia, toa);
            ctx.setMsgtoa60002(toa);
        } catch (Exception e) {
            logger.error("交易处理失败", e);
            toa.setCode("9999");
            toa.setComment("系统错误， 交易处理失败。");
            ctx.setMsgtoa60002(toa);
        }
    }
    private void processTxn(TIAT60008 tia, TpsMsgResForLtf60002 toa) {
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            FsLtfVchAbnormal billAbnormal = new FsLtfVchAbnormal();
            FbiBeanUtils.copyProperties(tia,billAbnormal);

            //1，判断该条数据异常种类，若是2（重票），查看票据分配表中是否存在一个票据号码对应多个罚单，若不是进行处理
            //若是3（多票），查看票据领取中，查看该票据是否属于网络票据
            if ("2".equals(tia.getCauseType())){
                List<FsLtfVchOut> vchOutList = selectVchOut(tia.getBillNo());
                if( vchOutList==null){
                    toa.setCode("2002");
                    toa.setComment("该票据号码没有关联的罚款单号!");
                    billAbnormal.setResCode("2002");
                    billAbnormal.setResContent("该票据号码没有关联的罚款单号");
                    insertBillAbnormal(billAbnormal);
                    session.commit();
                    return;
                }else{
                    if (vchOutList.size()==1){
                        FsLtfVchOut vchOut = vchOutList.get(0);
                        toa.setCode("9999");
                        toa.setComment("该票据号只对应一个罚款单号，罚款单号为：" + vchOut.getTicketNo());
                        billAbnormal.setResCode("9999");
                        billAbnormal.setResContent("该票据号只对应一个罚款单号，罚款单号为：" + vchOut.getTicketNo());
                        insertBillAbnormal(billAbnormal);
                        session.commit();
                        return;
                    }else{
                        //1,根据原有分配票据查询对应的部门
                        FsLtfVchOut vchOut = selectVchOutInfo(tia);
                        if(vchOut==null){
                            toa.setCode("2002");
                            toa.setComment("重新分票失败！");
                            return  ;
                        }

                        FsLtfOrgComp orgComp = selectOrg(vchOut.getBankMare());
                        if (orgComp == null){
                            logger.info("网点号为："+vchOut.getBankMare()+"支行，没有对应的信息。");
                            toa.setCode("2002");
                            toa.setComment("网点号为："+vchOut.getBankMare()+"支行，没有对应的信息。");
                            session.commit();
                            return;
                        }
                        String dept = orgComp.getDeptCode();

                        //2，根据领取网点号，查询该网点是否存在多余票据。
                        FsLtfVchStore vchStore = selectVchStore(dept);
                        if (vchStore == null){
                            //(1),首先查询总行是否还有库存，若存在则自动分配一定数量的票据，一般是50的倍数
                            //若不存在库存，则直接告诉对方没有多余票据
                            //todo
                            logger.info("网点号为："+vchOut.getBankMare()+"支行，没有多余的票据。请选择其他网点。");
                            toa.setCode("2002");
                            toa.setComment("网点号为："+vchOut.getBankMare()+"支行，没有多余的票据。请选择其他网点。");
                            session.commit();
                            return;
                        }
                        //3，分配票据
                        synchronized (this) {
                            String startNo = vchStore.getVchStartNo();
                            vchOut.setBillNo(startNo);
                            //4，更新库存
                            String endNo = startNo;
                            String operCode = vchOut.getOprNo();
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
                                throw new RuntimeException("库存总分不符！");
                            }
                            updateVchOut(vchOut);
                            logger.info("重新分票成功。票号为："+startNo);
                            toa.setCode("0000");
                            toa.setComment(startNo);
                            session.commit();
                        }
                    }
                }
            }else if("3".equals(tia.getCauseType())){
                List<FsLtfVchJrnl> jrnlList = selectJrnlList(tia.getBillNo());
                if(jrnlList==null){
                    List<FsLtfVchOut> vchOutList = selectVchOut(tia.getBillNo());
                    if( vchOutList==null){
                        toa.setCode("2002");
                        toa.setComment("该票据号码没有关联的罚款单号!");
                        billAbnormal.setResCode("2002");
                        billAbnormal.setResContent("该票据号码没有关联的罚款单号");
                        insertBillAbnormal(billAbnormal);
                        session.commit();
                        return;
                    }else{
                        if (vchOutList.size()==1){
                            FsLtfVchOut vchOut = vchOutList.get(0);
                            toa.setCode("9999");
                            toa.setComment("该票据号只对应一个罚款单号，罚款单号为：" + vchOut.getTicketNo());
                            billAbnormal.setResCode("9999");
                            billAbnormal.setResContent("该票据号只对应一个罚款单号，罚款单号为：" + vchOut.getTicketNo());
                            insertBillAbnormal(billAbnormal);
                            session.commit();
                            return;
                        }else{
                            //1,根据原有分配票据查询对应的部门
                            FsLtfVchOut vchOut = selectVchOutInfo(tia);
                            if(vchOut==null){
                                toa.setCode("2002");
                                toa.setComment("重新分票失败！");
                                return;
                            }

                            FsLtfOrgComp orgComp = selectOrg(vchOut.getBankMare());
                            if (orgComp == null){
                                logger.info("网点号为："+vchOut.getBankMare()+"支行，没有对应的信息。");
                                toa.setCode("2002");
                                toa.setComment("网点号为："+vchOut.getBankMare()+"支行，没有对应的信息。");
                                session.commit();
                                return;
                            }
                            String dept = orgComp.getDeptCode();

                            //2，根据领取网点号，查询该网点是否存在多余票据。
                            FsLtfVchStore vchStore = selectVchStore(dept);
                            if (vchStore == null){
                                //(1),首先查询总行是否还有库存，若存在则自动分配一定数量的票据，一般是50的倍数
                                //若不存在库存，则直接告诉对方没有多余票据
                                //todo
                                logger.info("网点号为："+vchOut.getBankMare()+"支行，没有多余的票据。请选择其他网点。");
                                toa.setCode("2002");
                                toa.setComment("网点号为："+vchOut.getBankMare()+"支行，没有多余的票据。请选择其他网点。");
                                session.commit();
                                return;
                            }
                            //3，分配票据
                            synchronized (this) {
                                String startNo = vchStore.getVchStartNo();
                                vchOut.setBillNo(startNo);
                                //4，更新库存
                                String endNo = startNo;
                                String operCode = vchOut.getOprNo();
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
                                    throw new RuntimeException("库存总分不符！");
                                }
                                updateVchOut(vchOut);
                                logger.info("重新分票成功。票号为："+startNo);
                                toa.setCode("0000");
                                toa.setComment(startNo);
                                session.commit();
                            }
                        }
                    }
                }else{
                    toa.setCode("9999");
                    toa.setComment("该票号不是网络票据。");
                }
            }
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            toa.setCode("2002");
            toa.setComment("系统错误， 交易处理失败。");
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //根据订单号查询是否在票据分配表中已经存在
    private List<FsLtfVchOut> selectVchOut(String billNo){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample outExample = new FsLtfVchOutExample();
        outExample.createCriteria().andBillNoEqualTo(billNo);
        List<FsLtfVchOut> vchOutList = outMapper.selectByExample(outExample);
        if (vchOutList.size()>0){
            return vchOutList;
        }else{
            return null;
        }
    }

    //根据订单号查询是否在票据分配表中已经存在
    private FsLtfVchOut selectVchOutInfo(TIAT60008 tia){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample outExample = new FsLtfVchOutExample();
        outExample.createCriteria().andBillNoEqualTo(tia.getBillNo()).andOrderDetailEqualTo(tia.getOrderDetail()).
            andTransTimeEqualTo(tia.getTransTime()).andOrderNoEqualTo(tia.getOrderNo());
        List<FsLtfVchOut> vchOutList = outMapper.selectByExample(outExample);
        if (vchOutList.size()>0){
            return vchOutList.get(0);
        }else{
            return null;
        }
    }
    //插入票据分配表
    private void insertVchOut(FsLtfVchOut vchOut){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        outMapper.insert(vchOut);
    }

    //插入票据分配表
    private void insertVchOutItem(FsLtfVchOut vchOut){
        FsLtfVchOutItemMapper itemMapper = session.getMapper(FsLtfVchOutItemMapper.class);
        String orderCharges = vchOut.getOrderCharges();
        String[] arrOrderCharges = orderCharges.split(",");
        for(String charge : arrOrderCharges){
            FsLtfVchOutItem item = new FsLtfVchOutItem();
            FsLtfChargeName chargeName = selectchargeName(charge);
            if (chargeName == null){
                chargeName = new FsLtfChargeName();
                item.setItemName("");
                item.setAmount(vchOut.getPayment());
            }else{
                item.setItemName(chargeName.getChargeName());
                item.setAmount(chargeName.getAmount());
            }
            item.setItemCode(charge);
            item.setTicketNo(vchOut.getTicketNo());
            item.setInfoId(vchOut.getPkid());
            itemMapper.insert(item);
        }
    }

    private FsLtfChargeName selectchargeName(String chargeCode){
        FsLtfChargeNameExample example = new FsLtfChargeNameExample();
        example.createCriteria().andChargeCodeEqualTo(chargeCode).andIsCancelNotEqualTo("1");
        FsLtfChargeNameMapper mapper =session.getMapper(FsLtfChargeNameMapper.class);
        List<FsLtfChargeName> infoList = mapper.selectByExample(example);
        return infoList.size()>0?(FsLtfChargeName)infoList.get(0):null;
    }

    private void updateVchOut(FsLtfVchOut vchOut){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        outMapper.updateByPrimaryKey(vchOut);
    }
    //查询库存表
    private FsLtfVchStore selectVchStore(String branchId){
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(branchId).andBusCodeEqualTo("1").andBillCodeEqualTo("3004");
        storeExample.setOrderByClause("vch_start_no");
        List<FsLtfVchStore> vchStoreList = storeMapper.selectByExample(storeExample);
        if (vchStoreList.size()>0){
            return vchStoreList.get(0);
        }else {
            return null;
        }
    }
    private void deleteVchStore(String pkid){
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.deleteByPrimaryKey(pkid);
    }
    private void updateVchStore(FsLtfVchStore vchStore){
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.updateByPrimaryKey(vchStore);
    }

    private void insertVoucherJournal(long vchCnt, String startNo, String endNo, String branchId,
                                      VouchStatus status, String remark,String operCode) {
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnl vchJrnl = new FsLtfVchJrnl();
        vchJrnl.setVchCount((int)vchCnt);
        vchJrnl.setVchStartNo(startNo);
        vchJrnl.setVchEndNo(endNo);

        vchJrnl.setBranchId(branchId);
        vchJrnl.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchJrnl.setOprNo(operCode);
        vchJrnl.setRecversion(0);
        vchJrnl.setRemark(remark);
        vchJrnl.setBusCode("1");
        vchJrnl.setBillCode("3004");
        vchJrnl.setVchState(status.getCode());
        mapper.insert(vchJrnl);
    }
    private void doVchUseOrCancel(String instNo, String startNo, String endNo, List<FsLtfVchStore> storeParamList) {
        FsLtfVchStore storeDb = selectVchStoreByStartNo(instNo, startNo);
        FsLtfVchStore storeParam = new FsLtfVchStore();
        storeParam.setPkid(storeDb.getPkid());
        storeParam.setVchStartNo(startNo);
        if (storeDb.getVchEndNo().compareTo(endNo) < 0) {
            storeParam.setVchEndNo(storeDb.getVchEndNo());
            storeParam.setVchCount((int)(Long.parseLong(storeDb.getVchEndNo()) - Long.parseLong(startNo) + 1));
            storeParamList.add(storeParam);
            String vchNo = getStandLengthForVoucherString(Long.parseLong(storeDb.getVchEndNo()) + 1);
            doVchUseOrCancel(instNo, vchNo, endNo, storeParamList);
        } else {
            storeParam.setVchEndNo(endNo);
            storeParam.setVchCount((int)(Long.parseLong(endNo) - Long.parseLong(startNo) + 1));
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
            return  null;
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

    private void insertVoucherStore(long vchCnt, String startNo, String endNo, String instNo, String remark,String operCode) {
        FsLtfVchStore vchStore = new FsLtfVchStore();
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        vchStore.setVchCount((int)vchCnt);
        vchStore.setVchStartNo(startNo);
        vchStore.setVchEndNo(endNo);
        vchStore.setBankCode("CCB");
        vchStore.setBusCode("1");
        vchStore.setBillCode("3004");
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

    private void insertBillAbnormal(FsLtfVchAbnormal bill){
        FsLtfVchAbnormalMapper mapper = session.getMapper(FsLtfVchAbnormalMapper.class);
        mapper.insert(bill);
    }

    private FsLtfOrgComp selectOrg(String orgCode){
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if(orgCompList.size()>0){
            return orgCompList.get(0);
        }else{
            return null;
        }
    }
    private List<FsLtfVchJrnl> selectJrnlList(String billNo){
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        FsLtfVchJrnlExample example = new FsLtfVchJrnlExample();
        example.createCriteria().andVchStartNoEqualTo(billNo).andVchEndNoEqualTo(billNo)
        .andBusCodeEqualTo("1");
        List<FsLtfVchJrnl> jrnlList = mapper.selectByExample(example);
        if(jrnlList.size()>0){
            return jrnlList;
        }else{
            return null;
        }
    }
}
