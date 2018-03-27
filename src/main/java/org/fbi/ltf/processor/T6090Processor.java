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
// * ����������佻��
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
//            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
//            throw new RuntimeException(e);
//        }
//        //ҵ���߼�����
//        CbsRtnInfo cbsRtnInfo = null;
//        try {
//            cbsRtnInfo = processTxn(tia);
////            //��ɫƽ̨��Ӧ
//            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
//            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
////            response.setHeader("rtnCode", "0000");
////              cbsRespMsg ="6|10137198661031331000800027,120,600001|10137198661031331000800027,2,600002|10137198661031331000800027,3,600003|10137198661031331000800027,4,600004|10137198661031331000800027,5,600005|10137198661031331000800027,6,600006|";
//
//            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
//            throw new RuntimeException("���״����쳣", e);
//        }
//    }
//
//    private CbsRtnInfo processTxn(CbsTia6090 tia) throws Exception {
//        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
//        try {
//            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
//            session = sqlSessionFactory.openSession();
//            session.getConnection().setAutoCommit(false);
//            //���ش���
//            //1���鿴���Ӧ����Ŀ�����Ƿ���ڼ�¼
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
//                cbsRtnInfo.setRtnMsg("û��ת����Ϣ");
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
//            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");
//            return cbsRtnInfo;
//        } finally {
//            if (session != null) {
//                session.close();
//            }
//        }
//    }
//
//
//    //����CBS��Ӧ����
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
//            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
//        }
//        return cbsRespMsg;
//    }
//
//    //�жϸ������Ƿ��Ѿ�����
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
