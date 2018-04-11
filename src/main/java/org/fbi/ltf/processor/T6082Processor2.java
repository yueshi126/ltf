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
 * Ʊ��ʹ�������ѯ
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
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
            //��ɫƽ̨��Ӧ

            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
//            response.setHeader("rtnCode", "0000");
//            String cbsRespMsg = "3|3|ʹ��1,10001,10002,20151220,����1|ʹ��2,10003,10003,20151220,����2|ʹ��3,10004,10004,20151220,����3|";

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6082 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            //1���鿴���Ӧ����Ŀ�����Ƿ���ڼ�¼
            String tip = "";
            String branchId = request.getHeader("branchId");
            String cbsRespMsg = "";
            // ���
            List<FsLtfVchStore> vchStroeList = this.selectVchStore(tia.getTxnDate(), branchId);
            // ʹ��
            List<FsLtfVchJrnl> vchJrnlUseList = this.selectVchJrnl(tia.getTxnDate(), branchId, "2");
            // ����
            List<FsLtfVchJrnl> vchJrnlCanclList = this.selectVchJrnl(tia.getTxnDate(), branchId, "3");
            int storeNum = vchStroeList.size();
            int usedNum = vchJrnlUseList.size();
            int canclNum = vchJrnlCanclList.size();

            if (vchStroeList.size() == 0) {
                session.rollback();
                tip = tip + "�û���Ʊ�ݿ��Ϊ0��";
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�û���Ʊ�ݿ��Ϊ0");

            } else {
                session.commit();
                tip = tip + "�û���Ʊ�ݿ��Ϊ:" + vchStroeList + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }
            if (vchJrnlUseList.size() == 0) {
                session.rollback();
                tip = tip + "�û�����ʹ��Ʊ��Ϊ0��";
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("û�н�ͨ�����Ϣ");

            } else {
                session.commit();
                tip = tip + "�û���Ʊ�ݿ��Ϊ:" + vchJrnlUseList.size() + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }

            if (vchJrnlCanclList.size() == 0) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("û����Ϣ");
            } else {
                session.commit();
                tip = tip + "�û���Ʊ�ݿ��Ϊ:" + vchJrnlCanclList.size() + ";";
//                cbsRespMsg = cbsRespMsg + generateCbsRespMsg(vchStroeList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(cbsRespMsg);
            }
            // ��ϸlist
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
                    // ��ʼ��
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
                    // ��ʼ��
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
                cbsRtnInfo.setRtnMsg("�޿�����ݡ���ƱƱ�ݡ�ʹ��Ʊ�ݿɲ�ѯ");
            }
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }


    //����CBS��Ӧ����
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
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
        }
        return cbsRespMsg;
    }

    //���
    private List<FsLtfVchJrnl> selectVchJrnl(String operDate, String brandId, String vchState) {
        FsLtfVchJrnlExample example = new FsLtfVchJrnlExample();
        example.createCriteria().andBranchIdEqualTo(brandId).andOprDateEqualTo(operDate).andVchStateEqualTo(vchState);
        example.setOrderByClause("vch_start_no");
        FsLtfVchJrnlMapper mapper = session.getMapper(FsLtfVchJrnlMapper.class);
        List<FsLtfVchJrnl> infoList = mapper.selectByExample(example);
        return infoList;
    }

    // ʹ�����
    private List<FsLtfVchStore> selectVchStore(String operDate, String brandId) {
        FsLtfVchStoreExample example = new FsLtfVchStoreExample();
        example.createCriteria().andBranchIdEqualTo(brandId);
        example.setOrderByClause("vch_start_no");
        FsLtfVchStoreMapper mapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> infoList = mapper.selectByExample(example);
        return infoList;
    }
}
