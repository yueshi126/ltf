package org.fbi.ltf.processor;


import com.thoughtworks.xstream.XStream;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6080Request.CbsTia6080;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FtpClient;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.model.FsLtfTicketInfo;
import org.fbi.ltf.repository.model.FsLtfTicketInfoExample;
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

public class T6080Processorbak extends AbstractTxnProcessor {
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
            //���ش���
            //1����ѯ��Ҫ�ϴ�������
            List<FsLtfTicketInfo> infos = this.selectTicketInfoList(tia.getChkDate(), tia.getChkDate());
            if (infos.size() == 0) {
                session.commit();
                logger.info("��ѯ������û�ж������ݣ���ʼ���ڣ�" + tia.getChkDate() + "���������ڣ�" + tia.getChkDate());
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
                return cbsRtnInfo;
            }
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
            dataMap.put("txnDate", txnDate);
            dataMap.put("fileName", fileName);
            isUpload = uploadFileData(dataMap);
            if (isUpload) {
                for (FsLtfTicketInfo ticketInfo : infos) {
                    ticketInfo.setQdfChkFlag("1");
                    ticketInfo.setChkActDt(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                    updateTicketInfo(ticketInfo);
                }
                session.commit();
                logger.info("�ϴ��ļ��ɹ����������ڣ�" + txnDate);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_SECCESS.getTitle());
            } else {
                session.rollback();
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
            String uploadDate = (String) dataMap.get("txnDate");
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
}
