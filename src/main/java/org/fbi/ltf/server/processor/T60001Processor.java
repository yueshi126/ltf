package org.fbi.ltf.server.processor;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.ltf.domain.tps.TIAT60001;
import org.fbi.ltf.domain.tps.TOAT60001;
import org.fbi.ltf.domain.tps.TpsMsgResForLtf;
import org.fbi.ltf.enums.TxnRtnCode;
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
 * Ʊ�ݷ���
 * Created by Thinkpad on 2015/11/1.
 */
public class T60001Processor implements Processor{
    private static final Logger logger = LoggerFactory.getLogger(T60001Processor.class);
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;
    private TOAT60001 resBody = new TOAT60001();


    @Override
    public void service(TxnContext ctx) {

        Map<String, List<String>> params = ctx.getMapTia();
        String reqData = params.get("reqdata").get(0).toString();
        TpsMsgResForLtf toa = new TpsMsgResForLtf();
        try {
            String req = FbiBeanUtils.decode64(reqData);
            logger.info("�յ��Է��������ݣ�������Ϊ��"+req);
            TIAT60001 tia = new TIAT60001();
            tia = FbiBeanUtils.jsonToBean(req,TIAT60001.class);
            processTxn(tia, toa);
            ctx.setMsgtoa(toa);
        } catch (Exception e) {
            logger.error("���״���ʧ��", e);
            toa.setCode("9999");
            toa.setComment("ϵͳ���� ���״���ʧ�ܡ�");
            ctx.setMsgtoa(toa);
        }
    }
    private void processTxn(TIAT60001 tia, TpsMsgResForLtf toa) {
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            toa.setCode("0000");
            toa.setComment("�ɹ�");

            //1���жϸ��������Ƿ��Ѿ�����Ʊ�ݷ�����У������ڣ���ֱ�ӷ��ر��е�Ʊ�ݺţ��������һ�����ݡ�
            FsLtfVchOut vchOut = selectVchOut(tia.getOrderNo(),tia.getOrderDetail());
            if( vchOut==null){
                tia.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                FsLtfOrgComp orgComp = selectOrg(tia.getBankMare());
                if (orgComp != null){
                    tia.setBranchId(orgComp.getDeptCode());
                }
                FsLtfPoliceOrgComp policeOrgComp = selectPoliceOrgComp(tia.getDept());
                if (policeOrgComp != null){
                    tia.setAreaCode(policeOrgComp.getAreaCode());
                }else{
                    toa.setCode("9999");
                    toa.setComment("���ش���:"+tia.getDept()+" ���ز�����");
                    session.rollback();
                    logger.info( "���ش���:"+tia.getDept()+" ���ز�����");
                    return ;
                }

                insertVchOut(tia);
                vchOut = selectVchOut(tia.getOrderNo(), tia.getOrderDetail());
                insertVchOutItem(vchOut);
            }else{
                if (!StringUtils.isEmpty(vchOut.getBillNo())){
                    FbiBeanUtils.copyProperties(vchOut,resBody);
                    toa.setReqdata(FbiBeanUtils.beanToJsonObejct(resBody));
                    return;
                }
            }
            //2��������ȡ����ţ���ѯ�������Ƿ���ڶ���Ʊ�ݡ�
            FsLtfOrgComp orgComp = selectOrg(tia.getBankMare());
            if (orgComp == null){
                logger.info("�����Ϊ��"+tia.getBankMare()+"֧�У�û�ж�Ӧ��֧�С�");
                toa.setCode("9999");
                toa.setComment("�����Ϊ��"+tia.getBankMare()+"֧�У�û�ж�Ӧ��֧�С�");
                session.commit();
                return;
            }
            String dept = orgComp.getDeptCode();
            FsLtfVchStore vchStore = selectVchStore(dept);
            if (vchStore == null){
                //(1),���Ȳ�ѯ�����Ƿ��п�棬���������Զ�����һ��������Ʊ�ݣ�һ����50�ı���
                //�������ڿ�棬��ֱ�Ӹ��߶Է�û�ж���Ʊ��
                //todo
                //
                logger.info("�����Ϊ��"+tia.getBankMare()+"֧�У�û�ж����Ʊ�ݡ�����371980000������� ��");
//                toa.setCode("9999");
//                toa.setComment("�����Ϊ��"+tia.getBankMare()+"֧�У�û�ж����Ʊ�ݡ���ѡ���������㡣");
//                session.commit();
//                return;

                FsLtfVchStore headVchStore = selectVchStore("371980000");
                if (headVchStore == null){
                    logger.info("���кţ�371980000��û�ж����Ʊ�ݡ�");
                    toa.setCode("9999");
                    //Ʊ��12λ�����ء�000000000000��
                    long vchno=0;
                    toa.setComment(getStandLengthForVoucherString(vchno) );
                    session.commit();
                    return;
                }else {
                    String warningCount = ProjectConfigManager.getInstance().getProperty("auto.transfer.count");
                    int warCount = Integer.parseInt(warningCount);
                    headVchStore.setVchEndNo(String.valueOf(Long.parseLong(headVchStore.getVchStartNo())+(warCount-1)));
                    headVchStore.setVchCount(warCount);
                    processVchTransfer("371980000", dept, headVchStore);
                    vchStore = selectVchStore(dept);
                    logger.info("Ʊ���Զ�����ɹ�����ʼ�ţ�"+headVchStore.getVchStartNo()+",��ֹ�ţ�"+headVchStore.getVchEndNo());
                }
            }
            //3������Ʊ��
            synchronized (this){
                String startNo = vchStore.getVchStartNo();
                vchOut.setBillNo(startNo);
                //4�����¿��
                String endNo = startNo;
                String operCode = tia.getOprNo();
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
                    if (startNoDb != startNoParam || endNoDb != endNoParam) {//����������¼
                        if (startNoParam != startNoDb) {
                            insertVoucherStore(startNoParam - startNoDb, storeDb.getVchStartNo(),
                                    getStandLengthForVoucherString(startNoParam - 1), dept, remark,operCode);
                        }
                        if (endNoParam != endNoDb) {
                            insertVoucherStore(endNoDb - endNoParam, getStandLengthForVoucherString(endNoParam + 1),
                                    storeDb.getVchEndNo(), dept, remark,operCode);
                        }
                    }
                }
                insertVoucherJournal(Long.parseLong(endNo) - Long.parseLong(startNo) + 1, startNo, endNo, dept, status, remark,operCode);
                //�ֺܷ˶�
                if (!verifyVchStoreAndJrnl(dept)) {
                    throw new RuntimeException("����ֲܷ�����");
                }
                BigDecimal payment = tia.getPayment();
                BigDecimal overdueFine = tia.getOverdueFine();
                BigDecimal totalAmount = payment.add(overdueFine);
                vchOut.setBillMoney(totalAmount);
                updateVchOut(vchOut);
                FbiBeanUtils.copyProperties(vchOut, resBody);
                toa.setReqdata(FbiBeanUtils.beanToJsonObejct(resBody));
                logger.info("Ʊ�ݷ���ɹ��������ţ�"+tia.getTicketNo()+"Ʊ�ݺţ�"+startNo);
                session.commit();
            }
        } catch (Exception e) {
            if (session != null) {
                session.rollback();
            }
            toa.setCode("9999");
            toa.setComment("ϵͳ���� ���״���ʧ�ܡ�");
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    //���ݶ����Ų�ѯ�Ƿ���Ʊ�ݷ�������Ѿ�����
    private FsLtfVchOut selectVchOut(String orderNo,String orderDetail){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample outExample = new FsLtfVchOutExample();
        outExample.createCriteria().andOrderNoEqualTo(orderNo). andOrderDetailEqualTo(orderDetail);
        List<FsLtfVchOut> vchOutList = outMapper.selectByExample(outExample);
        if (vchOutList.size()>0){
            return vchOutList.get(0);
        }else{
            return null;
        }
    }
    //����Ʊ�ݷ����
    private void insertVchOut(FsLtfVchOut vchOut){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        int aa =outMapper.insert(vchOut);
    }

    //����Ʊ�ݷ����
    private void insertVchOutItem(FsLtfVchOut vchOut){
        FsLtfVchOutItemMapper itemMapper = session.getMapper(FsLtfVchOutItemMapper.class);
        String orderCharges = vchOut.getOrderCharges();
        if(!StringUtils.isEmpty(orderCharges)){
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
    }

    private FsLtfChargeName selectchargeName(String ticketCode){
        FsLtfChargeNameExample example = new FsLtfChargeNameExample();
        example.createCriteria().andTicketCodeEqualTo(ticketCode).andIsCancelNotEqualTo("1");
        FsLtfChargeNameMapper mapper =session.getMapper(FsLtfChargeNameMapper.class);
        List<FsLtfChargeName> infoList = mapper.selectByExample(example);
        return infoList.size()>0?(FsLtfChargeName)infoList.get(0):null;
    }

    private void updateVchOut(FsLtfVchOut vchOut){
        FsLtfVchOutMapper outMapper = session.getMapper(FsLtfVchOutMapper.class);
        outMapper.updateByPrimaryKeySelective(vchOut);
    }
    //��ѯ����
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

    //������Ų������ݿ��к��д˺���Ŀ���¼
    private FsLtfVchStore selectVchStoreByStartNo(String instNo, String startNo) {
        FsLtfVchStoreExample storeExample = new FsLtfVchStoreExample();
        storeExample.createCriteria().andBranchIdEqualTo(instNo)
                .andVchStartNoLessThanOrEqualTo(startNo)
                .andVchEndNoGreaterThanOrEqualTo(startNo);
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        List<FsLtfVchStore> storesTmp = storeMapper.selectByExample(storeExample);
        if (storesTmp.size() != 1) {
//            throw new RuntimeException("δ�ҵ�����¼��");
            return  null;
        }
        return storesTmp.get(0);
    }

    //��Ʊ�ݺų���
    private String getStandLengthForVoucherString(long vchno) {
        String vchNo = "" + vchno;
        String vch_length = ProjectConfigManager.getInstance().getProperty("voucher.no.length");
        int vchnoLen = Integer.parseInt(vch_length);
        if (vchNo.length() != vchnoLen) { //���Ȳ��� ����
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
        vchStore.setBranchId(instNo);
        vchStore.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchStore.setOprNo(operCode);
        vchStore.setRecversion(0);
        vchStore.setBankCode("CCB");
        vchStore.setBusCode("1");
        vchStore.setBillCode("3004");
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

    private FsLtfPoliceOrgComp selectPoliceOrgComp(String orgCode){
        FsLtfPoliceOrgCompMapper mapper = session.getMapper(FsLtfPoliceOrgCompMapper.class);
        FsLtfPoliceOrgCompExample example = new FsLtfPoliceOrgCompExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<FsLtfPoliceOrgComp> configList = mapper.selectByExample(example);
        if(configList.size()>0){
            return configList.get(0);
        }else{
            return  null;
        }
    }

    public synchronized void processVchTransfer(String fromInst, String toInst, FsLtfVchStore vchStoreParam) {
        if (StringUtils.isEmpty(vchStoreParam.getPkid())) {
            throw new IllegalArgumentException("δѡ��������¼.");
        }

        FsLtfVchStore vchStoreDB = selectVchStoreByPkid(vchStoreParam.getPkid());
        if (!vchStoreDB.getVchStartNo().equals(vchStoreParam.getVchStartNo())) {
            throw new RuntimeException("������������治��.");
        }

        if (Long.parseLong(vchStoreParam.getVchEndNo()) > Long.parseLong(vchStoreDB.getVchEndNo())) {
            throw new RuntimeException("������ֹ�ų�����Χ.");
        }

        long nTransInStartNo = Long.parseLong(vchStoreParam.getVchEndNo()) + 1;
        String sTransInStartNo = getStandLengthForVoucherString(nTransInStartNo);

        if (vchStoreParam.getRecversion().compareTo(vchStoreDB.getRecversion()) != 0) {
            throw new RuntimeException("������ͻ����¼�ѱ��޸ġ�");
        }

        //����
        vchStoreDB.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
        vchStoreDB.setOprNo("auto");
        vchStoreDB.setRecversion(vchStoreParam.getRecversion() + 1);
        if (vchStoreParam.getVchEndNo().equals(vchStoreDB.getVchEndNo())) { //������¼���������
            //������־
            insertVoucherJournal(vchStoreParam.getVchCount(), vchStoreParam.getVchStartNo(), vchStoreParam.getVchEndNo(),
                    fromInst, VouchStatus.OUTSTORE, "ȫ������","autoOut");
            //ֱ���޸�ԭ��¼�Ļ�����
            vchStoreDB.setRemark("ȫ������");
            vchStoreDB.setBranchId(toInst);
            updateVchStoreByPkid(vchStoreDB);
            //������־
            insertVoucherJournal(vchStoreDB.getVchCount(), vchStoreDB.getVchStartNo(), vchStoreDB.getVchEndNo(),
                    vchStoreDB.getBranchId(), VouchStatus.RECEIVED, "ȫ������","autoIn");
        } else {//��¼�в���Ʊ�Ų��������
            //��־
            insertVoucherJournal(vchStoreParam.getVchCount(), vchStoreParam.getVchStartNo(), vchStoreParam.getVchEndNo(),
                    vchStoreDB.getBranchId(), VouchStatus.OUTSTORE, "���ֲ���","autoOut");

            //�޸�ԭ��¼�����
            vchStoreDB.setRemark("���ֲ���");
            vchStoreDB.setVchStartNo(sTransInStartNo);
            vchStoreDB.setVchCount((int)(Long.parseLong(vchStoreDB.getVchEndNo()) - Long.parseLong(vchStoreDB.getVchStartNo()) + 1));
            updateVchStoreByPkid(vchStoreDB);

            //�����¼�¼
            processInstVoucherInput(vchStoreParam.getVchCount(), vchStoreParam.getVchStartNo(), vchStoreParam.getVchEndNo(), toInst, "���ֲ���",vchStoreDB);
            //��־
            insertVoucherJournal(vchStoreParam.getVchCount(), vchStoreParam.getVchStartNo(), vchStoreParam.getVchEndNo(),
                    toInst, VouchStatus.RECEIVED, "���ֲ���","autoIn");
        }

//        //�ֺܷ˶�
        if (!verifyVchStoreAndJrnl(fromInst) || !verifyVchStoreAndJrnl(toInst)) {
            throw new RuntimeException("����ֲܷ�����");
        }
        //�ֺܷ˶�
/*        if ( !verifyVchStoreAndJrnl(toInst)) {
            throw new RuntimeException("����ֲܷ�����");
        }*/
    }

    private FsLtfVchStore selectVchStoreByPkid(String pkid){
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        FsLtfVchStore storesTmp = storeMapper.selectByPrimaryKey(pkid);
        return storesTmp;
    }

    private void updateVchStoreByPkid(FsLtfVchStore store){
        FsLtfVchStoreMapper storeMapper = session.getMapper(FsLtfVchStoreMapper.class);
        storeMapper.updateByPrimaryKey(store);
    }

    private void processInstVoucherInput(int vchCnt, String startNo, String endNo, String instNo, String remark,FsLtfVchStore vchStore) {
        FsVoucherMapper voucherMapper = session.getMapper(FsVoucherMapper.class);
        String maxEndno = voucherMapper.selectInstVchMaxEndNo(instNo);
        if (maxEndno == null) {
            maxEndno = "0";
        }

        if (startNo.compareTo(maxEndno) > 0) {//����¼����űȿ���еĶ���
            insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,"autoIn");
        } else {
            //����Ƿ��������¼�������ֹ��֮��ļ�¼
            int recordNum = voucherMapper.selectStoreRecordnumBetweenStartnoAndEndno(startNo, endNo);
            if (recordNum > 0) {
                throw new RuntimeException("Ʊ�ų�ͻ��");
            }
            String minNearbyStartno = voucherMapper.selectStoreStartno_GreaterThanVchno(endNo);
            if (minNearbyStartno == null) {
                throw new RuntimeException("Ʊ�ų�ͻ��");
            } else {
                String maxNearbyEndno = voucherMapper.selectStoreEndno_LessThanVchno(startNo);
                if (maxNearbyEndno == null) {//�п�棬��ÿ����¼����Ŷ�������¼��ֹ�Ŵ�
                    insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,"autoIn");
                } else {
                    long dbVchCnt = Long.parseLong(minNearbyStartno) - Long.parseLong(maxNearbyEndno) - 1;
                    if (vchCnt <= dbVchCnt) {
                        insertVoucherStore(vchCnt, startNo, endNo, instNo, remark,"autoIn");
                    } else {
                        throw new RuntimeException("Ʊ�ų�ͻ��");
                    }
                }
            }
        }
    }
}