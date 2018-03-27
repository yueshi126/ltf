package org.fbi.ltf.processor;


import com.thoughtworks.xstream.XStream;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.client.HttpClient.LnkHttpClient;
import org.fbi.ltf.domain.cbs.T6080Request.CbsTia6080;
import org.fbi.ltf.domain.cbs.T6080Request.CbsTia6080Item;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.domain.tps.TpsMsgRes;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.*;
import org.fbi.ltf.repository.dao.FsLtfChkTxnMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.common.CommonMapper;
import org.fbi.ltf.repository.model.*;
import org.fbi.ltf.repository.model.common.ChkData;
import org.fbi.ltf.repository.model.common.ChkOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * �����ļ��ϴ�
 * Created by Thinkpad on 2015/11/3.
 */

public class T6080Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6080 tia = new CbsTia6080();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6080) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6080");
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

    private CbsRtnInfo processTxn(CbsTia6080 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        Boolean updateFlag = false;
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            if (Integer.parseInt(tia.getItemNum()) == 0) {
                session.commit();
                logger.info("�������ڣ�" + tia.getChkDate() + "������");
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                return cbsRtnInfo;
            }
            int cnt = insertLtfCheckTxn(tia);
            BigDecimal totalVchAmt = this.qryTotalAmtByDate(tia.getChkDate());
            if (totalVchAmt == null) totalVchAmt = new BigDecimal("0.00");
            int fsCnt = this.qryCbsTxnCnt(tia.getChkDate());
            int cbsCnt = Integer.parseInt(tia.getItemNum());
            BigDecimal totalCbsAmt = new BigDecimal(tia.getTotalAmt().trim());

            if (cbsCnt != fsCnt || totalCbsAmt.compareTo(totalVchAmt) != 0) {
                int cancalCnt = this.cancalCbsTxnt(tia.getChkDate());
                session.commit();
                logger.info("�������ڣ�" + tia.getChkDate() + "��������ˮ������" + cancalCnt);
            }
            //���ش���
            //1����ѯ��Ҫ�ϴ�������

            List<FsLtfTicketInfo> infos = this.selectTicketInfoList(tia.getChkDate(), tia.getChkDate());
            if (infos.size() == 0) {
                XStream xStream = new XStream();
                xStream.autodetectAnnotations(true);
                String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" ;
                boolean isUpload = false;
                Map dataMap = new HashMap();
                String bankCode = ProjectConfigManager.getInstance().getProperty("ltf.bank.code");
                String txnDate = tia.getChkDate();
                String fileName = bankCode + "_jtwfgtjf_" + txnDate + ".txt";
                dataMap.put("xmlData", xml);
                dataMap.put("fileName", fileName);
                isUpload = uploadFileData(dataMap);
                logger.info("��ѯ������û�ж������ݣ���ʼ���ڣ�" + tia.getChkDate() + "���������ڣ�" + tia.getChkDate());
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                return cbsRtnInfo;
            }
            // �޸��������˳ɹ�
            for (FsLtfTicketInfo ticketInfo : infos) {
                ticketInfo.setHostChkFlag("1");
                updateTicketInfo(ticketInfo);
            }
            session.commit();
            List<ChkOrder> orders = new ArrayList<ChkOrder>();
            BigDecimal orderCount = new BigDecimal("0");
            BigDecimal orderAmount = new BigDecimal("0");
            for (FsLtfTicketInfo ticketInfo : infos) {
                ChkOrder order = new ChkOrder();
                order.setOrderNo(ticketInfo.getOrderNo());
                order.setTransTime(ticketInfo.getTransTime());
                order.setAmount(ticketInfo.getAmount().toString());
                order.setBillNo(ticketInfo.getBillNo());
                order.setNode(ticketInfo.getNode());
                orders.add(order);
                orderCount = orderCount.add(new BigDecimal("1"));
                orderAmount = orderAmount.add(ticketInfo.getAmount());
            }
            ChkData data = new ChkData();
            ChkData.BodyRecord record = new ChkData().getBody();
            record.setOrderTotal(orderCount.toString());
            record.setMoneyTotal(orderAmount.toString());
            record.setOrders(orders);
            data.setBody(record);

            XStream xStream = new XStream();
            xStream.autodetectAnnotations(true);
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" + xStream.toXML(data);
            boolean isUpload = false;
            Map dataMap = new HashMap();
            String bankCode = ProjectConfigManager.getInstance().getProperty("ltf.bank.code");
            String txnDate = tia.getChkDate();
            String fileName = bankCode + "_jtwfgtjf_" + txnDate + ".txt";
            dataMap.put("xmlData", xml);
            dataMap.put("fileName", fileName);
            isUpload = uploadFileData(dataMap);
            // ���������˽����ύ����
            session.commit();
            if (isUpload) {
//                for (FsLtfTicketInfo ticketInfo : infos) {
//                    ticketInfo.setQdfChkFlag("1");
//                    ticketInfo.setChkActDt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
//                    updateTicketInfo(ticketInfo);
//                }
                logger.info("�ϴ��ļ��ɹ����������ڣ�" + txnDate + ",�ļ�����" + fileName);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            } else {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�����ļ��ϴ�����ʧ��");
            }
            logger.info("��������������" + tia.getAreaCode());
            logger.info("�������ڣ�" + tia.getChkDate());
            logger.info("���˽�" + tia.getTotalAmt());
            logger.info("�����ܱ�����" + tia.getItemNum());
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } catch (SQLException e) {
            session.rollback();
            logger.info(e.getMessage());
            /*cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("���ݿ⴦���쳣");*/
            //���˶����������ɹ�
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean uploadFileData(Map dataMap) {
        boolean uploadFlag = false;
        try {
            String fileName = (String) dataMap.get("fileName");
            String uploadFileData = (String) dataMap.get("xmlData");
            String uploadFilePath = ProjectConfigManager.getInstance().getProperty("upload.ftp.local.file.path");
            if (uploadLocalFileData(uploadFilePath, fileName, uploadFileData)) {
                uploadFlag = true;
            } else {
                uploadFlag = false;
            }
        } catch (IOException e) {
            return false;
        }
        return uploadFlag;
    }

    public boolean uploadLocalFileData(String localPath, String fileName, String fileData) throws IOException {

        File file = new File(localPath, fileName);
        if (file.exists()) {
            file.delete();
        } else {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileData.getBytes());
        fos.flush();
        fos.close();
        return this.uploadFtpfileData(localPath, fileName);
    }

    public boolean uploadFtpfileData(String localPath, String fileName) throws IOException {
        String ftpIp = ProjectConfigManager.getInstance().getProperty("tps.ftp.ip");
        String user = ProjectConfigManager.getInstance().getProperty("tps.ftp.userid");
        String password = ProjectConfigManager.getInstance().getProperty("tps.ftp.password");
        FtpClient ftpClient = new FtpClient(ftpIp, user, password);
        boolean isUploaded = ftpClient.uploadFile(null, localPath, fileName);
        ftpClient.logout();
        return isUploaded;
    }

    private List<FsLtfTicketInfo> selectTicketInfoList(String startDate, String endDate) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        example.createCriteria().andOperDateGreaterThanOrEqualTo(startDate).
                andOperDateLessThanOrEqualTo(endDate).
                andHostBookFlagEqualTo("1").
                andQdfBookFlagEqualTo("1");
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    //��������
    private void updateTicketInfo(FsLtfTicketInfo ticketInfo) {
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        mapper.updateByPrimaryKey(ticketInfo);
    }

    public BigDecimal qryTotalAmtByDate(String chkact) {
        CommonMapper mapper = session.getMapper(CommonMapper.class);
        return mapper.qryTotalAmtByDate(chkact);
    }

    public int qryCbsTxnCnt(String chkact) {
        CommonMapper mapper = session.getMapper(CommonMapper.class);
        return mapper.qryCbsTxnCnt(chkact);
    }

    // ��ɫ��ˮΪ��������ǰ�û�����ˮ
    public int cancalCbsTxnt(String chkact) {
        CommonMapper mapper = session.getMapper(CommonMapper.class);
        return mapper.cancalCbsTxnt(chkact);
    }


    //��������
    private int insertLtfCheckTxn(CbsTia6080 tia) {
        int cnt = 0;
        FsLtfChkTxnMapper mapper = session.getMapper(FsLtfChkTxnMapper.class);
        int cbsCnt = Integer.parseInt(tia.getItemNum());
        FsLtfChkTxnExample example = new FsLtfChkTxnExample();
        example.createCriteria().andTxnDateEqualTo(tia.getChkDate());
        mapper.deleteByExample(example);
        String cbsActno = tia.getAccountNo();
        BigDecimal totalCbsAmt = new BigDecimal("0.00");
        if (cbsCnt > 0) {
            List<CbsTia6080Item> detailsStr = tia.getItems();
            for (CbsTia6080Item cbsTia6080Item : detailsStr) {
                FsLtfChkTxn cbsTxn = new FsLtfChkTxn();
                cbsTxn.setActno(cbsActno);
                cbsTxn.setActbal(new BigDecimal(tia.getTotalAmt().trim()));
                cbsTxn.setDcFlag(cbsTia6080Item.getPayFlag());
                cbsTxn.setMsgSn(cbsTia6080Item.getSeqNo());
                cbsTxn.setTxnamt(new BigDecimal((cbsTia6080Item.getTxnAmt()).trim()));
                cbsTxn.setTxnDate(tia.getChkDate());
                cbsTxn.setSendSysId("0");
                mapper.insert(cbsTxn);
                cnt++;
            }
        }
        return cnt;
    }
}
