//package org.fbi.ltf.processor;
//
//import org.apache.commons.lang.StringUtils;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
//import org.fbi.linking.processor.ProcessorException;
//import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
//import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
//import org.fbi.ltf.domain.cbs.T6090Request.CbsTia6090;
//import org.fbi.ltf.domain.cbs.T6090Response.CbsToa6090;
//import org.fbi.ltf.domain.cbs.T6090Response.CbsToa6090Item;
//import org.fbi.ltf.enums.TxnRtnCode;
//import org.fbi.ltf.helper.MybatisFactory;
//import org.fbi.ltf.repository.dao.FsLtfAcctDealMapper;
//import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
//import org.fbi.ltf.repository.model.FsLtfAcctDeal;
//import org.fbi.ltf.repository.model.FsLtfAcctDealExample;
//import org.fbi.ltf.repository.model.FsLtfTicketInfo;
//import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 网银账务分配交易
// * Created by Thinkpad on 2015/11/3.
// */
//public class T6090Processor extends AbstractTxnProcessor{
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
//    private SqlSessionFactory sqlSessionFactory = null;
//    private SqlSession session = null;
//
//    @Override
//    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
//        String hostTxnsn = request.getHeader("serialNo");
//
//        CbsTia6090 tia = new CbsTia6090();
//        try {
//            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
//            tia = (CbsTia6090) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6090");
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
//            throw new RuntimeException(e);
//        }
//        //业务逻辑处理
//        CbsRtnInfo cbsRtnInfo = null;
//        try {
//            cbsRtnInfo = processTxn(tia);
////            //特色平台响应
//            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
//            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
////            response.setHeader("rtnCode", "0000");
////              cbsRespMsg ="6|10137198661031331000800027,120,600001|10137198661031331000800027,2,600002|10137198661031331000800027,3,600003|10137198661031331000800027,4,600004|10137198661031331000800027,5,600005|10137198661031331000800027,6,600006|";
//
//            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
//            throw new RuntimeException("交易处理异常", e);
//        }
//    }
//
//    private CbsRtnInfo processTxn(CbsTia6090 tia) throws Exception {
//        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
//        try {
//            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
//            session = sqlSessionFactory.openSession();
//            session.getConnection().setAutoCommit(false);
//            //本地处理
//            //1、查看相对应的项目代码是否存在记录
//            String operdate = tia.getTxnDate();
//            if (!StringUtils.isEmpty(operdate)) {
//                if (operdate.length() == 8) {
//                    operdate = operdate.substring(0, 4) + "-" + operdate.substring(4, 6) + "-" + operdate.substring(6);
//                }
//            }
//            List<FsLtfAcctDeal> infoList = this.selectAcctInfo(operdate);
//            if (infoList.size()==0){
//                session.rollback();
//                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                cbsRtnInfo.setRtnMsg("没有转账信息");
//            }else {
//
//                String cbsRespMsg = generateCbsRespMsg(infoList);
//                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
//                cbsRtnInfo.setRtnMsg(cbsRespMsg);
//            }
//            return cbsRtnInfo;
//        } catch (SQLException e) {
//            session.rollback();
//            logger.info(e.getMessage());
//            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//            cbsRtnInfo.setRtnMsg("数据库处理异常");
//            return cbsRtnInfo;
//        } finally {
//            if (session != null) {
//                session.close();
//            }
//        }
//    }
//
//
//    //生成CBS响应报文
//    private String generateCbsRespMsg(List<FsLtfAcctDeal> infoList) {
//        CbsToa6090 cbsToa = new CbsToa6090();
//        cbsToa.setItemNum(String.valueOf(infoList.size()));
//        List<CbsToa6090Item> cbsToaItems = new ArrayList<>();
//        for(FsLtfAcctDeal info : infoList){
//            CbsToa6090Item item = new CbsToa6090Item();
//            item.setAcctNo(info.getInAcctNo());
//            item.setTxnAmt(info.getAcctMoney().toString());
//            item.setEntpNo(info.getInOrgCode().toString());
//            cbsToaItems.add(item);
//            info.setHostBookFlag("1");
//            this.updateAcctDeal(info);
//        }
//        session.commit();
//        cbsToa.setItems(cbsToaItems);
//        String cbsRespMsg = "";
//        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
//        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
//        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
//        try {
//            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
//        } catch (Exception e) {
//            throw new RuntimeException("特色平台报文转换失败.", e);
//        }
//        return cbsRespMsg;
//    }
//
//    //判断该数据是否已经存在
//    private List<FsLtfAcctDeal> selectAcctInfo(String operDate){
//        FsLtfAcctDealExample example = new FsLtfAcctDealExample();
//        FsLtfAcctDealMapper mapper =session.getMapper(FsLtfAcctDealMapper.class);
//        example.createCriteria().andOperDateEqualTo(operDate).andHostChkFlagEqualTo("0");
//        List<FsLtfAcctDeal> infoList = mapper.selectByExample(example);
//        return infoList;
//    }
//
//    private void updateAcctDeal(FsLtfAcctDeal acctDeal){
//        FsLtfAcctDealMapper mapper =session.getMapper(FsLtfAcctDealMapper.class);
//        mapper.updateByPrimaryKey(acctDeal);
//    }
//
//}
