package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6022Response.CbsToa6022Item;
import org.fbi.ltf.domain.cbs.T6022Response.CbsToa6022SubItem;
import org.fbi.ltf.domain.cbs.T6022Request.CbsTia6022;
import org.fbi.ltf.domain.cbs.T6022Response.CbsToa6022;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfOrgCompMapper;
import org.fbi.ltf.repository.dao.FsLtfVchOutItemMapper;
import org.fbi.ltf.repository.dao.FsLtfVchOutMapper;
import org.fbi.ltf.repository.dao.FsLtfVchStoreMapper;
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
public class T6022Processor extends AbstractTxnProcessor{
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6022 tia = new CbsTia6022();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6022) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6022");
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
//            response.setHeader("rtnCode","0000");
//           String cbsRespMsg = "票据剩余50张，请领取|3|piaojuhao1,chfa1,shijian1,111,xm1,jgbm1,2#sf#mc#200#sf#mc#200#|piaojuhao2,chfa2,shijian2,222,xm2,jgbm2,1#sf#mc#200#|piaojuhao3,chfa3,shijian3,333,xm3,jgbm3,3#3sf#3mc#3200#|";


            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6022 tia, Stdp10ProcessorRequest request) throws Exception {
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
            if (orgComp == null){
                logger.info("网点号为：" + branchId + "支行，没有对应信息。");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有需要打印的票据");
                return cbsRtnInfo;
            }
            String orgCode = orgComp.getOrgCode();
            List<FsLtfVchStore> vchStoreList = selectVchStoreList(branchId);
            String warningCount = ProjectConfigManager.getInstance().getProperty("warning.voucher.count");
            int warCount = Integer.parseInt(warningCount);
            int leaveCount = 0;
            boolean warFlag = false;

            List<FsLtfVchOut> vchOutList = new ArrayList<>();
            String begNO = tia.getBegNo();
            String endNo = tia.getEndNo();
            //  日期打印是1  ，票号打印 2-
            if("1".equals(tia.getPrintType())){
                vchOutList = selectVchOutList(begNO,endNo,orgCode,tia.getPrintType());
            }else{
                if(StringUtils.isEmpty(begNO) && StringUtils.isEmpty(endNo)){
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("获取打印票据号票据出错");
                    return cbsRtnInfo;
                }
                vchOutList = selectVchOutList(begNO, endNo, orgCode, tia.getPrintType());
            }

            if (vchOutList.size()>0) {
                String starringRespMsg = generateCbsRespMsg(vchOutList,warFlag,leaveCount);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(starringRespMsg);
                String dateTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                for(FsLtfVchOut vchOut:vchOutList){
                    vchOut.setPrintTime(dateTime);
                    updateSelectedVchOut(vchOut);
                }
                session.commit();
            }else{
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
    private void updateSelectedVchOut(FsLtfVchOut vchOut){
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        mapper.updateByPrimaryKeySelective(vchOut);
    }

    private List<FsLtfVchStore> selectVchStoreList(String branchId){
        FsLtfVchStoreMapper mapper =session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStoreExample example = new FsLtfVchStoreExample();
        example.createCriteria().andBranchIdEqualTo(branchId);
        List<FsLtfVchStore> vchStoreList = mapper.selectByExample(example);
        return vchStoreList;
    }

    private List<FsLtfVchOut> selectVchOut(String billNo,String ticketNo,String orgCode) {
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        if((!StringUtils.isEmpty(billNo))&&(!StringUtils.isEmpty(ticketNo))){
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andTicketNoEqualTo(ticketNo)
                    .andBankMareEqualTo(orgCode);
        }else if(!StringUtils.isEmpty(billNo)){
            example.createCriteria()
                    .andBillNoEqualTo(billNo)
                    .andBankMareEqualTo(orgCode);
        } else if(!StringUtils.isEmpty(ticketNo)){
            example.createCriteria()
                    .andTicketNoEqualTo(ticketNo)
                    .andBankMareEqualTo(orgCode).andBillNoIsNotNull();
        }
        example.setOrderByClause(" bill_no");
        List<FsLtfVchOut> infos = mapper.selectByExample(example);
        return infos;
    }

    private List<FsLtfVchOut> selectVchOutList(String begNO,String endNo,String orgCode,String printFlag){
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        FsLtfVchOutExample.Criteria criteria = example.createCriteria();
        if(printFlag.equals("1")){  //
            criteria.andBillNoBetween(begNO,endNo).andBankMareEqualTo(orgCode).andPrintTimeIsNull().andBillNoIsNotNull();
        }else {
            criteria.andBillNoBetween(begNO,endNo).andBankMareEqualTo(orgCode);
        }
        example.setOrderByClause(" bill_no");
        List<FsLtfVchOut> infos = mapper.selectByExample(example);
        return infos;
    }
    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfVchOut> vchOutList,boolean flag,int count) {
        CbsToa6022 cbsToa = new CbsToa6022();
        cbsToa.setRemark("备注");

        cbsToa.setItemNum(String.valueOf(vchOutList.size()));
        List<CbsToa6022Item> cbsToaItems = new ArrayList<>();
        long preBillNo = 0;
        long currBillNo = 0;
        long serialNo = 0;
        for(FsLtfVchOut vchOut : vchOutList){
            CbsToa6022Item cbsToaItem = new CbsToa6022Item();
            List<CbsToa6022SubItem> cbsToa6022SubItems = new ArrayList<>();
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
            FbiBeanUtils.copyProperties(vchOut,cbsToaItem);
            currBillNo = Long.parseLong(cbsToaItem.getBillNo());
            if(preBillNo == 0){
                serialNo = 1;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            }else if(preBillNo+1 == currBillNo){
                serialNo ++;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            }else{
                long tempNo = currBillNo - preBillNo;
                serialNo += tempNo;
                cbsToaItem.setSerialNo(String.valueOf(serialNo));
                preBillNo = currBillNo;
            }

            cbsToaItems.add(cbsToaItem);
        }
        cbsToa.setItemNum(""+cbsToaItems.size());
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

    private List<FsLtfVchOutItem> selectVchOutItemList(String infoId){
        FsLtfVchOutItemMapper itemMapper = session.getMapper(FsLtfVchOutItemMapper.class);
        FsLtfVchOutItemExample example = new FsLtfVchOutItemExample();
        example.createCriteria().andInfoIdEqualTo(infoId);
        List<FsLtfVchOutItem> itemList = itemMapper.selectByExample(example);
        return itemList;
    }
    private FsLtfOrgComp selectOrg(String deptCode){
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andDeptCodeEqualTo(deptCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if(orgCompList.size()>0){
            return orgCompList.get(0);
        }else{
            return null;
        }
    }
}
