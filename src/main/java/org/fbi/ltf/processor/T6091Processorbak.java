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
// * ODSB装载数据交易
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
//            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
//            throw new RuntimeException(e);
//        }
//        //业务逻辑处理
//        CbsRtnInfo cbsRtnInfo = null;
//        try {
//            cbsRtnInfo = processTxn(tia);
////            特色平台响应
//            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
//            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
////            String cbsRespMsg ="6|10137198661031331000800027,1,600001|10137198661031331000800027,2,600002|10137198661031331000800027,3,600003|10137198661031331000800027,4,600004|10137198661031331000800027,5,600005|10137198661031331000800027,6,600006|";
//            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
//        } catch (Exception e) {
//            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
//            throw new RuntimeException("交易处理异常", e);
//        }
//    }
//
//    public CbsRtnInfo processTxn(CbsTia6091 tia) throws Exception {
//        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
//        try {
//            /*
//            * 0 -平
//            * 1 -金额不平
//            * 2 - 订单不存在
//            * 3- 生成转账记录了
//            * */
//            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
//            session = sqlSessionFactory.openSession();
//            session.getConnection().setAutoCommit(false);
//            /*
//            * 每次对账对账日期用来从odsb 取数据用，取数据结束之后这个就没用了，具体对账不按日期，按没有对账+对账失败的订单对账
//            * */
//
//            int res = -1;
//            // 交易流水号 ,这个一定要唯一
//            String serNum = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + (RandomStringUtils.randomNumeric(4));
//            String operDate8 = tia.getTxnDate();
//            // out 表的时间格式yyyymmdd
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
//             1 数据准备，保证out中的数据同一个 order_no 不能有两种状态，网银订单也是
//             以网银为主，用网银的订单找out订单对是否平账，这样只要网银的订单不出现问题，out不会有问题，所以保证网银的一个订单只能有一个状态
//             2-本地数据不为空，不需要读取日期operDate10的数据，前提： 保证每天只要网银有就认为这个数据是正确的，网银不会后期补入缺失的某几条数据
//            */
//            res = this.selectPosNumByOperdate(operDate10);
//            if (res == 0) {
//                //todo  没有建立odsb 的dblink 暂时注释
//                res = this.insertFromPos(operDate10);   // odsb 取数据
//                res = this.insertFromPosTemp(operDate10,operDate10Back7); // 剔除已经对账成功的订单写入act_info，
//                if (res < 0) {
//                    session.rollback();
//                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
//                    cbsRtnInfo.setRtnMsg("获取网银数据出错");
//                    logger.info("获取网银数据出错");
//                    return cbsRtnInfo;
//                } else if (res == 0) {
//                    session.rollback();
//                    logger.info("当日网银数据为空");
//                }
//            } else {
//                logger.info("日期："+operDate10+"本地网银数据已存在,无需获取odsb网银数据");
//            }
//            // 提交网银数据  只要获取到网银数据commit ，下次取数据时已经存在就不用读取
//            session.commit();
//            // 保证流水表的单号 在SEND_SYS_ID =99 /00 各自是唯一的不能有重复的单号
//
//            // 获取 网银对账不平的数据 暂时取全部数据，参数保留，以后可以按天进行对账
//            // todo  operDate10 可以减少循环次数  ，去掉这个参数修改sql 可以对所有网银的数据
//            List<FsLtfOutAcct> fsLtfAcctInfoList = this.seletAcctInfo(operDate10);
//            for (FsLtfOutAcct ltfVchOutAcct : fsLtfAcctInfoList) {
//                List<FsLtfOutAcct> fsLtfOutAcctList = this.selectVchOutByorderNo(ltfVchOutAcct.getOrderno());
//                if (fsLtfOutAcctList.size() == 1.) { // group by orderno 所以只要存在就是一条数据否则就是不存在
//                    if (fsLtfOutAcctList.get(0).getTotalamt().compareTo(ltfVchOutAcct.getTotalamt()) == 0) {
//                        // 金额一致写入流水表
//                        FsLtfChkAct fsLtfChkAct = new FsLtfChkAct();
//                        fsLtfChkAct.setSendSysId("99");
//                        fsLtfChkAct.setActAmt(fsLtfOutAcctList.get(0).getTotalamt());
//                        fsLtfChkAct.setOrderNo(fsLtfOutAcctList.get(0).getOrderno());
//                        fsLtfChkAct.setOrderNum(fsLtfOutAcctList.get(0).getOrderNum());
//                        fsLtfChkAct.setTxnDate(operDate8);  // 对账日期
//                        fsLtfChkAct.setChksts("0");  // 对账成功
//                        fsLtfChkAct.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date())); // 操作日期
//                        fsLtfChkAct.setSerNum(serNum); // 流水号
//                        this.insertChkAct(fsLtfChkAct);
//                        fsLtfChkAct = new FsLtfChkAct();
//                        fsLtfChkAct.setSendSysId("00");
//                        fsLtfChkAct.setActAmt(ltfVchOutAcct.getTotalamt());
//                        fsLtfChkAct.setOrderNo(ltfVchOutAcct.getOrderno());
//                        fsLtfChkAct.setOrderNum(ltfVchOutAcct.getOrderNum());
//                        fsLtfChkAct.setTxnDate(operDate8); // 对战成功
//                        fsLtfChkAct.setChksts("0");  // 对账成功
//                        fsLtfChkAct.setOprDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
//                        fsLtfChkAct.setSerNum(serNum); // 流水号
//                        // 写入对账数据
//                        this.insertChkAct(fsLtfChkAct);
//                        this.updateOutBydateOrderNo(fsLtfOutAcctList.get(0).getOrderno(), serNum, "0");
//                        this.updatePOSBydateOrderno(fsLtfOutAcctList.get(0).getOrderno(), serNum, "0");
//                    } else {
//                        // 金额不等
//                        this.updatePOSBydateOrderno(fsLtfOutAcctList.get(0).getOrderno(), serNum, "1");
//                        this.updateOutBydateOrderNo(fsLtfOutAcctList.get(0).getOrderno(), serNum, "1");
//                    }
//                } else {
//                    // pos 的数据交警不存在
//                    this.updatePOSBydateOrderno(ltfVchOutAcct.getOrderno(), serNum, "2");
//                }
//            }
////            session.commit();
//            //此时 out 订单状态属于对账平标识，需要产生待转账的数据
//            List<FsLtfAcctDeal> acctDealListtList = this.selectAcctResult(serNum);
//            String ltf_bank_act = ProjectConfigManager.getInstance().getProperty("ltf_bank_act");
//            for (FsLtfAcctDeal acctDeal : acctDealListtList) {
//                FsLtfAcctDeal fsLtfAcctDeal = new FsLtfAcctDeal();
//                fsLtfAcctDeal.setHostBookFlag("0");  // 记账标识
//                fsLtfAcctDeal.setSernum(serNum); // 交易流水
//                fsLtfAcctDeal.setRemark(acctDeal.getInOrgCode()+ ":" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));  // 对账结束结果平
//                // 企业号
//                fsLtfAcctDeal.setInOrgCode(acctDeal.getInOrgCode());
//                // 转入账号
//                fsLtfAcctDeal.setInAcctNo(acctDeal.getInAcctNo());
//                 // 转出账号
//                fsLtfAcctDeal.setOutAcctNo(ltf_bank_act);
//                // 金额
//                fsLtfAcctDeal.setAcctMoney(acctDeal.getAcctMoney());
//                // 对账日期
//                fsLtfAcctDeal.setOperDate(new SimpleDateFormat("yyyyMMdd").format(new Date()));
//                // 交易量
//                fsLtfAcctDeal.setBillnum(acctDeal.getBillnum() );
//                fsLtfAcctDeal.setTransDate(operDate8);
//                res = this.insertAcctDeal(fsLtfAcctDeal);
//            }
//            // 更新生成转账数据
//            this.updateVchOut_ChkFlag(serNum, "3");
//            this.updateActInfo_ChkFlag(serNum, "3");
//            session.commit();
//            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
//            cbsRtnInfo.setRtnMsg("对账成功");
//            logger.info("对账成功：" + new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date()));
//            return cbsRtnInfo;
//
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
//    // 获取本地网银 在operdtae10交易的数量
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
