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
 * ����Ʊ�ݴ�ӡ
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

            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }
    }

    private CbsRtnInfo processTxn(CbsTia6020 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            //���ش���
            //1�������жϴ�ӡ��ʽ�������״���������ڲ�ѯ��������Ʊ�ݺźͷ����Ų�ѯ��
            //ͬʱ���û����Ŀ�������С��һ��������������Ԥ��
            String branchId = request.getHeader("branchId");
            FsLtfOrgComp orgComp = selectOrg(branchId);
            if (orgComp == null) {
                logger.info("�����Ϊ��" + branchId + "֧�У�û�ж�Ӧ��Ϣ��");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("���㲻������Ʊ�ݴ�ӡ����");
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
            if ("1".equals(tia.getOrg())) { //������
                if ("1".equals(tia.getPrintType())) { //
                    // �����ڴ�ӡ
                    String startDate = tia.getStartDate();
                    String endDate = tia.getEndDate();
                    vchOutList = selectVchOutList(startDate, endDate, orgCode);
                } else {
                    if (StringUtils.isEmpty(tia.getBillNo()) && StringUtils.isEmpty(tia.getTicketNo())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("Ʊ�ݺͷ���Ų���ͬʱΪ��");
                        return cbsRtnInfo;
                    }
                    vchOutList = selectVchOut(tia.getBillNo(), tia.getTicketNo(), orgCode);
                }

            } else if ("2".equals(tia.getOrg())) {
                // ����Ʊ��
                if ("1".equals(tia.getPrintType())) { // �����ڴ�ӡ
                    String startDate = tia.getStartDate();
                    String endDate = tia.getEndDate();
                    ticketInfoList = selectTickNoList(startDate, endDate, orgCode);
                } else {
                    if (StringUtils.isEmpty(tia.getBillNo()) && StringUtils.isEmpty(tia.getTicketNo())) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("Ʊ�ݺͷ���Ų���ͬʱΪ��");
                        return cbsRtnInfo;
                    }
                }
                ticketInfoList = selectTicketInfo(tia.getBillNo(), tia.getTicketNo(), orgCode);
            } else {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("Ʊ����Դ����");
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
                cbsRtnInfo.setRtnMsg("��ǰû����Ҫ��ӡ��Ʊ��");
            }
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info(e.getMessage());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");
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

    // ��������Ʊ�ݺŻ�ȡ������Ϣ
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

    // ��ȡ���淣����Ϣ+Ʊ�ݺ�
    private List<FsLtfTicketInfo> selectTickNoList(String startDate, String endDate, String orgCode) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andOperDateGreaterThan(startDate).andOperDateLessThanOrEqualTo(endDate).andBranchIdEqualTo(orgCode).andPrintTimeIsNull().andBillNoIsNotNull();
        example.setOrderByClause(" bill_no");
        List<FsLtfTicketInfo> infos = mapper.selectByExample(example);
        return infos;
    }

    //����CBS��Ӧ����
    private String generateCbsRespMsg(List<FsLtfVchOut> vchOutList, boolean flag, int count) {
        CbsToa6020 cbsToa = new CbsToa6020();
        if (flag) {
            cbsToa.setRemark("����Ʊ��ʣ�ࣺ" + String.valueOf(count) + "�ţ������㼰ʱ��ȡƱ�ݣ�����Ӱ��ʹ�ã�");
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
//                    stringBuffer.append("#").append("��ͨΥ����û����");
//                }else{
//                    stringBuffer.append("#").append(outItem.getItemName());
//                }
//                stringBuffer.append("#").append(outItem.getAmount());
//                stringBuffer.append("#");
//            }
            cbsToaItem.setItems(stringBuffer.toString());
            FbiBeanUtils.copyProperties(vchOut, cbsToaItem);
            currBillNo = Long.parseLong(cbsToaItem.getBillNo());
            //����Ʊ��������Ӧ�����
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
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��.", e);
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
