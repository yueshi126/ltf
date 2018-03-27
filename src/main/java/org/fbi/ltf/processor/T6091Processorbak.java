//package org.fbi.ltf.processor;
//
//import org.apache.commons.lang.RandomStringUtils;
//import org.apache.commons.lang.StringUtils;
//import org.apache.ibatis.session.SqlSession;
//import org.apache.ibatis.session.SqlSessionFactory;
//import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
//import org.fbi.linking.codec.dataformat.format.DatePatternFormat;
//import org.fbi.linking.processor.ProcessorException;
//import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
//import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
//import org.fbi.ltf.domain.cbs.T6091Request.CbsTia6091;
//import org.fbi.ltf.enums.TxnRtnCode;
//import org.fbi.ltf.helper.MybatisFactory;
//import org.fbi.ltf.helper.ProjectConfigManager;
//import org.fbi.ltf.repository.dao.*;;
//import org.fbi.ltf.repository.dao.common.FsLtfOutAcctInfoMapper;
//import org.fbi.ltf.repository.model.*;
//import org.fbi.ltf.repository.model.common.FsLtfOutAcct;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
///**
// * ODSBװ�����ݽ���
// * Created by Thinkpad on 2015/11/3.
// */
//public class T6091Processor extends AbstractTxnProcessor {
//    private Logger logger = LoggerFactory.getLogger(this.getClass());
//    private SqlSessionFactory sqlSessionFactory = null;
//    private SqlSession session = null;
//
//    @Override
//    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
//        String hostTxnsn = request.getHeader("serialNo");
//
//        CbsTia6091 tia = new CbsTia6091();
//        try {
//            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
//            tia = (CbsTia6091) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6091");
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
//            throw new RuntimeException(e);
//        }
//        //ҵ���߼�����
//        CbsRtnInfo cbsRtnInfo = null;
//        try {
//            cbsRtnInfo = processTxn(tia);
////            ��ɫƽ̨��Ӧ
//            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
//            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
////            String cbsRespMsg ="6|10137198661031331000800027,1,600001|10137198661031331000800027,2,600002|10137198661031331000800027,3,600003|10137198661031331000800027,4,600004|10137198661031331000800027,5,600005|10137198661031331000800027,6,600006|";
//            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
//            throw new RuntimeException("���״����쳣", e);
//        }
//    }
//
//    public CbsRtnInfo processTxn(CbsTia6091 tia) throws Exception {
//        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
//        try {
//            /*
//            * 0 -ƽ
//            * 1 -��ƽ
//            * 2 - ����������
//            * 3- ����ת�˼�¼��
//            * */
//            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
//            session = sqlSessionFactory.openSession();
//            session.getConnection().setAutoCommit(false);
//            /*
//            * ÿ�ζ��˶�������������odsb ȡ�����ã�ȡ���ݽ���֮�������û���ˣ�������˲������ڣ���û�ж���+����ʧ�ܵĶ�������
//            * */
//
//            int res = -1;
//            // ������ˮ�� ,���һ��ҪΨһ
//            String serNum = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + (RandomStringUtils.randomNumeric(4));
//            String operDate8 = tia.getTxnDate();
//            // out ���ʱ���ʽyyyymmdd
//            String operDate10 = "";
//            if (!StringUtils.isEmpty(operDate8)) {
//                if (operDate8.length() == 8) {
//                    operDate10 = operDate8.substring(0, 4) + "-" + operDate8.substring(4, 6) + "-" + operDate8.substring(6);
//                }
//            }
//            DatePatternFormat sdf = new DatePatternFormat("yyyy-MM-dd");
//            Date objDate = sdf.parse(operDate10);
//            String operDate10Back7 = getDateAfter(objDate, -7, "yyyyMMdd");
//            /*
//             1 ����׼������֤out�е�����ͬһ�� order_no ����������״̬����������Ҳ��
//             ������Ϊ�����������Ķ�����out�������Ƿ�ƽ�ˣ�����ֻҪ�����Ķ������������⣬out���������⣬���Ա�֤������һ������ֻ����һ��״̬
//             2-�������ݲ�Ϊ�գ�����Ҫ��ȡ����operDate10�����ݣ�ǰ�᣺ ��֤ÿ��ֻҪ�����о���Ϊ�����������ȷ�ģ�����������ڲ���ȱʧ��ĳ��������
//            */
//            res = this.selectPosNumByOperdate(operDate10);
//            if (res == 0) {
//                //todo  û�н���odsb ��dblink ��ʱע��
//                res = this.insertFromPos(operDate10);   // odsb ȡ����
//                res = this.insertFromPosTemp(operDate10,operDate10Back7); // �޳��Ѿ����˳ɹ��Ķ���д��act_info��
//                if (res < 0) {
//                    session.rollback();
//                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                    cbsRtnInfo.setRtnMsg("��ȡ�������ݳ���");
//                    logger.info("��ȡ�������ݳ���");
//                    return cbsRtnInfo;
//                } else if (res == 0) {
//                    session.rollback();
//                    logger.info("������������Ϊ��");
//                }
//            } else {
//                logger.info("���ڣ�"+operDate10+"�������������Ѵ���,�����ȡodsb��������");
//            }
//            // �ύ��������  ֻҪ��ȡ����������commit ���´�ȡ����ʱ�Ѿ����ھͲ��ö�ȡ
//            session.commit();
//            // ��֤��ˮ��ĵ��� ��SEND_SYS_ID =99 /00 ������Ψһ�Ĳ������ظ��ĵ���
//
//            // ��ȡ �������˲�ƽ������ ��ʱȡȫ�����ݣ������������Ժ���԰�����ж���
//            // todo  operDate10 ���Լ���ѭ������  ��ȥ����������޸�sql ���Զ���������������
//            List<FsLtfOutAcct> fsLtfAcctInfoList = this.seletAcctInfo(operDate10);
//            for (FsLtfOutAcct ltfVchOutAcct : fsLtfAcctInfoList) {
//                List<FsLtfOutAcct> fsLtfOutAcctList = this.selectVchOutByorderNo(ltfVchOutAcct.getOrderno());
//                if (fsLtfOutAcctList.size() == 1.) { // group by orderno ����ֻҪ���ھ���һ�����ݷ�����ǲ�����
//                    if (fsLtfOutAcctList.get(0).getTotalamt().compareTo(ltfVchOutAcct.getTotalamt()) == 0) {
//                        // ���һ��д����ˮ��
//                        FsLtfChkAct fsLtfChkAct = new FsLtfChkAct();
//                        fsLtfChkAct.setSendSysId("99");
//                        fsLtfChkAct.setActAmt(fsLtfOutAcctList.get(0).getTotalamt());
//                        fsLtfChkAct.setOrderNo(fsLtfOutAcctList.get(0).getOrderno());
//                        fsLtfChkAct.setOrderNum(fsLtfOutAcctList.get(0).getOrderNum());
//                        fsLtfChkAct.setTxnDate(operDate8);  // ��������
//                        fsLtfChkAct.setChksts("0");  // ���˳ɹ�
//                        fsLtfChkAct.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // ��������
//                        fsLtfChkAct.setSerNum(serNum); // ��ˮ��
//                        this.insertChkAct(fsLtfChkAct);
//                        fsLtfChkAct = new FsLtfChkAct();
//                        fsLtfChkAct.setSendSysId("00");
//                        fsLtfChkAct.setActAmt(ltfVchOutAcct.getTotalamt());
//                        fsLtfChkAct.setOrderNo(ltfVchOutAcct.getOrderno());
//                        fsLtfChkAct.setOrderNum(ltfVchOutAcct.getOrderNum());
//                        fsLtfChkAct.setTxnDate(operDate8); // ��ս�ɹ�
//                        fsLtfChkAct.setChksts("0");  // ���˳ɹ�
//                        fsLtfChkAct.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
//                        fsLtfChkAct.setSerNum(serNum); // ��ˮ��
//                        // д���������
//                        this.insertChkAct(fsLtfChkAct);
//                        this.updateOutBydateOrderNo(fsLtfOutAcctList.get(0).getOrderno(), serNum, "0");
//                        this.updatePOSBydateOrderno(fsLtfOutAcctList.get(0).getOrderno(), serNum, "0");
//                    } else {
//                        // ����
//                        this.updatePOSBydateOrderno(fsLtfOutAcctList.get(0).getOrderno(), serNum, "1");
//                        this.updateOutBydateOrderNo(fsLtfOutAcctList.get(0).getOrderno(), serNum, "1");
//                    }
//                } else {
//                    // pos �����ݽ���������
//                    this.updatePOSBydateOrderno(ltfVchOutAcct.getOrderno(), serNum, "2");
//                }
//            }
////            session.commit();
//            //��ʱ out ����״̬���ڶ���ƽ��ʶ����Ҫ������ת�˵�����
//            List<FsLtfAcctDeal> acctDealListtList = this.selectAcctResult(serNum);
//            String ltf_bank_act = ProjectConfigManager.getInstance().getProperty("ltf_bank_act");
//            for (FsLtfAcctDeal acctDeal : acctDealListtList) {
//                FsLtfAcctDeal fsLtfAcctDeal = new FsLtfAcctDeal();
//                fsLtfAcctDeal.setHostBookFlag("0");  // ���˱�ʶ
//                fsLtfAcctDeal.setSernum(serNum); // ������ˮ
//                fsLtfAcctDeal.setRemark(acctDeal.getInOrgCode()+ ":" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));  // ���˽������ƽ
//                // ��ҵ��
//                fsLtfAcctDeal.setInOrgCode(acctDeal.getInOrgCode());
//                // ת���˺�
//                fsLtfAcctDeal.setInAcctNo(acctDeal.getInAcctNo());
//                 // ת���˺�
//                fsLtfAcctDeal.setOutAcctNo(ltf_bank_act);
//                // ���
//                fsLtfAcctDeal.setAcctMoney(acctDeal.getAcctMoney());
//                // ��������
//                fsLtfAcctDeal.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
//                // ������
//                fsLtfAcctDeal.setBillnum(acctDeal.getBillnum() );
//                fsLtfAcctDeal.setTransDate(operDate8);
//                res = this.insertAcctDeal(fsLtfAcctDeal);
//            }
//            // ��������ת������
//            this.updateVchOut_ChkFlag(serNum, "3");
//            this.updateActInfo_ChkFlag(serNum, "3");
//            session.commit();
//            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
//            cbsRtnInfo.setRtnMsg("���˳ɹ�");
//            logger.info("���˳ɹ���" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));
//            return cbsRtnInfo;
//
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
//
//    }
//
//    private List<FsLtfAcctDeal> selectAcctResult(String serNum) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        List<FsLtfAcctDeal> infoList = mapper.selectAcctResult(serNum);
//        return infoList;
//    }
//
//    private int updateOutBydateOrderNo(String orderNo, String serNum, String chkFlag) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.updateOutBydateOrderNo(orderNo, serNum, chkFlag);
//    }
//
//    private int updatePOSBydateOrderno(String orderNo, String serNum, String chkFlag) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.updatePOSBydateOrderno(orderNo, serNum, chkFlag);
//    }
//
//    private int insertAcctDeal(FsLtfAcctDeal fsLtfAcctDeal) {
//        FsLtfAcctDealMapper fsLtfAcctDealMapper = session.getMapper(FsLtfAcctDealMapper.class);
//        return fsLtfAcctDealMapper.insert(fsLtfAcctDeal);
//    }
//
//    private int insertChkAct(FsLtfChkAct fsLtfChkAct) {
//        FsLtfChkActMapper fsLtfChkActMapper = session.getMapper(FsLtfChkActMapper.class);
//        return fsLtfChkActMapper.insert(fsLtfChkAct);
//    }
//
//    public String getDateAfter(Date date, int days, String pattern) {
//        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + days);
//        return sdf.format(calendar.getTime());
//    }
//
//    // ��ȡ�������� ��operdtae10���׵�����
//    private int selectPosNumByOperdate(String operDate10) {
//        FsLtfAcctInfoMapper fsLtfAcctInfoMapper = session.getMapper(FsLtfAcctInfoMapper.class);
//        FsLtfAcctInfoExample example = new FsLtfAcctInfoExample();
//        example.createCriteria().andCrTxDtEqualTo(operDate10);
//        int res = fsLtfAcctInfoMapper.selectByExample(example).size();
//        return res;
//    }
//
//    private int insertFromPos(String operDate10) {
//        FsLtfOutAcctInfoMapper fsLtfOutAcctInfoMapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return fsLtfOutAcctInfoMapper.insertFromPos(operDate10);
//    }
//
//    private int insertFromPosTemp(String operDate10,String operDate10Back7) {
//        FsLtfOutAcctInfoMapper fsLtfOutAcctInfoMapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return fsLtfOutAcctInfoMapper.insertFromPosTemp(operDate10,operDate10Back7);
//    }
//
//    private List<FsLtfOutAcct> seletAcctInfo(String operDate10) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.seletAcctInfo(operDate10);
//    }
//
//    private List<FsLtfOutAcct> selectVchOutByorderNo(String orderno) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.selectVchOutByorderNo(orderno);
//    }
//
//    private int updateVchOut_ChkFlag(String serNum, String chkFlag) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.updateVchOut_ChkFlag(serNum, chkFlag);
//    }
//
//    private int updateActInfo_ChkFlag(String serNum, String chkFlag) {
//        FsLtfOutAcctInfoMapper mapper = session.getMapper(FsLtfOutAcctInfoMapper.class);
//        return mapper.updateActInfo_ChkFlag(serNum, chkFlag);
//    }
//}
