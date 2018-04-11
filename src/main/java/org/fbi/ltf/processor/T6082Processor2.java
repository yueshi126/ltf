package org.fbi.ltf.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6082Request.CbsTia6082;
import org.fbi.ltf.domain.cbs.T6082Response.CbsToa6082;
import org.fbi.ltf.domain.cbs.T6082Response.CbsToa6082Item;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfVchJrnlMapper;
import org.fbi.ltf.repository.dao.FsLtfVchStoreMapper;
import org.fbi.ltf.repository.model.FsLtfVchJrnl;
import org.fbi.ltf.repository.model.FsLtfVchJrnlExample;
import org.fbi.ltf.repository.model.FsLtfVchStore;
import org.fbi.ltf.repository.model.FsLtfVchStoreExample;
import org.fbi.ltf.repository.model.common.FsLtfInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 票据使用情况查询
 * Created by Thinkpad on 2015/11/3.
 */
public class T6082Processor2 extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6082 tia = new CbsTia6082();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6082) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6082");
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
//            response.setHeader("rtnCode", "0000");
//            String cbsRespMsg = "3|3|使用1,10001,10002,20151220,网络1|使用2,10003,10003,20151220,网络2|使用3,10004,10004,20151220,网络3|";

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6082 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //本地处理
            //1、查看相对应的项目代码是否存在记录
            String tip = "";
            String branchId = request.getHeader("branchId");
            String cbsRespMsg = "";
            // 库存
            List<FsLtfVchStore> vchStroeList = this.selectVchStore(tia.getTxnDate(), branchId);
            // 使用
            List<FsLtfVchJrnl> vchJrnlUseList = this.selectVchJrnl(tia.getTxnDate(), branchId, "2");
            // 作废
            List<FsLtfVchJrnl> vchJrnlCanclList = this.selectVchJrnl(tia.getTxnDate(), branchId, "3");
            int storeNum = vchStroeList.size();
            int usedNum = vchJrnlUseList.size();
            int canclNum = vchJrnlCanclList.size();

            if (vchStroeList.size() == 0) {
                session.rollback();
                tip = tip + "该机构票据库存为0；";
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("该机构票据库存为0");

            } else {
                session.commit();
                tip = tip + "该机构票据库存为:" + vchStroeList + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }
            if (vchJrnlUseList.size() == 0) {
                session.rollback();
                tip = tip + "该机构已使用票据为0；";
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有交通罚款单信息");

            } else {
                session.commit();
                tip = tip + "该机构票据库存为:" + vchJrnlUseList.size() + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }

            if (vchJrnlCanclList.size() == 0) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("没有信息");
            } else {
                session.commit();
                tip = tip + "该机构票据库存为:" + vchJrnlCanclList.size() + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }
            // 明细list
            List<FsLtfInfo> cbsToaItems = new ArrayList<>();
            for (int i = 0; i < vchStroeList.size(); i++) {
                FsLtfInfo item = new FsLtfInfo();
                item.setBillCode(vchStroeList.get(i).getBillCode());
                item.setBranchId(vchStroeList.get(i).getBranchId());
                item.setVchStartNo(vchStroeList.get(i).getVchStartNo());
                item.setVchEndNo(vchStroeList.get(i).getVchEndNo());
                cbsToaItems.add(item);

            }
            String preBillNo = "0";
            FsLtfInfo item = new FsLtfInfo();
            int num = 0;
            for (int i = 0; i < vchJrnlUseList.size(); i++) {
                if (preBillNo.equals("0")) {
                    item.setBillCode(vchJrnlUseList.get(i).getBillCode());
                    item.setBranchId(vchJrnlUseList.get(i).getBranchId());
                    item.setVchStartNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchEndNo());
                    item.setVchCount(vchJrnlUseList.get(i).getVchCount());
                    item.setOprDate(vchJrnlUseList.get(i).getOprDate());
                    num = 1;
                    preBillNo = item.getVchStartNo();
                } else if (Long.parseLong(preBillNo + 1) == Long.parseLong(item.getVchStartNo())) {
                    // 起始号
                    num++;
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchCount(num);
                    preBillNo = item.getVchEndNo();

                } else if (Long.parseLong(preBillNo + 1) != Long.parseLong(item.getVchStartNo())) {
                    cbsToaItems.add(item);
                    item.setBillCode(vchJrnlUseList.get(i).getBillCode());
                    item.setBranchId(vchJrnlUseList.get(i).getBranchId());
                    item.setVchStartNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchEndNo());
                    item.setVchCount(vchJrnlUseList.get(i).getVchCount());
                    item.setOprDate(vchJrnlUseList.get(i).getOprDate());
                    num = 0;
                    preBillNo = item.getVchStartNo();
                    num++;
                }
            }
            cbsToaItems.add(item);
            preBillNo = "0";
            num = 0;
            for (int i = 0; i < vchJrnlCanclList.size(); i++) {
                if (preBillNo.equals("0")) {
                    item.setBillCode(vchJrnlUseList.get(i).getBillCode());
                    item.setBranchId(vchJrnlUseList.get(i).getBranchId());
                    item.setVchStartNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchEndNo());
                    item.setVchCount(vchJrnlUseList.get(i).getVchCount());
                    item.setOprDate(vchJrnlUseList.get(i).getOprDate());
                    num = 1;
                    preBillNo = item.getVchStartNo();
                } else if (Long.parseLong(preBillNo + 1) == Long.parseLong(item.getVchStartNo())) {
                    // 起始号
                    num++;
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchCount(num);
                    preBillNo = item.getVchEndNo();

                } else if (Long.parseLong(preBillNo + 1) != Long.parseLong(item.getVchStartNo())) {
                    cbsToaItems.add(item);
                    item.setBillCode(vchJrnlUseList.get(i).getBillCode());
                    item.setBranchId(vchJrnlUseList.get(i).getBranchId());
                    item.setVchStartNo(vchJrnlUseList.get(i).getVchStartNo());
                    item.setVchEndNo(vchJrnlUseList.get(i).getVchEndNo());
                    item.setVchCount(vchJrnlUseList.get(i).getVchCount());
                    item.setOprDate(vchJrnlUseList.get(i).getOprDate());
                    num = 0;
                    preBillNo = item.getVchStartNo();
                    num++;
                }
            }
            cbsToaItems.add(item);

            if (storeNum > 0 || usedNum > 0 || canclNum > 0) {
                cbsRespMsg = generateCbsRespMsg(cbsToaItems, tip);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            } else {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("无库存数据、废票票据、使用票据可查询");
            }
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


    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfInfo> infoList, String tip) {
        CbsToa6082 cbsToa = new CbsToa6082();
        cbsToa.setItemNum(String.valueOf(infoList.size()));
        List<CbsToa6082Item> cbsToaItems = new ArrayList<>();
        for (FsLtfInfo info : infoList) {
            CbsToa6082Item item = new CbsToa6082Item();
            item.setBillEndNo(info.getVchEndNo());
            item.setBillStartNo(info.getVchStartNo());
            item.setVchState(info.getVchState());
            item.setOperDate(info.getOprDate());
            item.setBus_code(info.getBusCode());
            cbsToaItems.add(item);
        }
        cbsToa.setItems(cbsToaItems);
        cbsToa.setTip(tip);
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

    //库存
    private List<FsLtfVchJrnl> selectVchJrnl(String operDate, String brandId, String vchState) {
        FsLtfVchJrnlExample example = new FsLtfVchJrnlExample();
        example.createCriteria().andBranchIdEqualTo(brandId).andOprDateEqualTo(operDate).andVchStateEqualTo(vchState);
        example.setOrderByClause("vch_start_no");
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        List<FsLtfVchJrnl> infoList = mapper.selectByExample(example);
        return infoList;
    }

    // 使用情况
    private List<FsLtfVchStore> selectVchStore(String operDate, String brandId) {
        FsLtfVchStoreExample example = new FsLtfVchStoreExample();
        example.createCriteria().andBranchIdEqualTo(brandId);
        example.setOrderByClause("vch_start_no");
        FsLtfVchStoreMapper mapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> infoList = mapper.selectByExample(example);
        return infoList;
    }
}
