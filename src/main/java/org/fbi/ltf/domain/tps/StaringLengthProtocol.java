package org.fbi.ltf.domain.tps;

import org.apache.commons.lang.StringUtils;
import org.fbi.ltf.helper.MD5Helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ��ɫ��������
 * Created by ZZP_YY on 2018-03-22.
 */
public class StaringLengthProtocol {
    public String branchID = "";                        // ������    3
    public String tellerID = "";                          // ��Ա��    18
    public String serialNo = "";                       // ������ˮ��    4
    public String ip = "";                           // IP��ַ    7
    public String terNum = "";                          // �ն˺�  9
    public String track = "";                          // ���ŵ�����   12
    public String ueserID = "";                           // �û�ID    6
    public String fileNum = "0";                             // �����ļ���   6
    public String reFile = "";                           // �����ļ�   14
    // ������+8λ��������+�û�ID����ASC�ַ���ʾ��16����MD5ֵ
    public String mac;                                   //  16λ
    public String txncode;                                   //  16λ
    public String msgBody;                               // ������
    public void assembleFields(byte[] bytes) {
        if (bytes == null || bytes.length < 111) {
            throw new RuntimeException("����ͷ�ֽڳ��ȴ���");
        }
//        version = new String(bytes, 0, 3).trim();
//        serialNo = new String(bytes, 3, 18).trim();
//        rtnCode = new String(bytes, 21, 4).trim();
//        txnCode = new String(bytes, 25, 7).trim();
//        branchID = new String(bytes, 32, 9).trim();
//        tellerID = new String(bytes, 41, 12).trim();
//        ueserID = new String(bytes, 53, 6).trim();
//        appID = new String(bytes, 59, 6).trim();
//        txnTime = new String(bytes, 65, 14).trim();
//        mac = new String(bytes, 79, 32);
        // ������
//        msgBody = new byte[bytes.length - 13];
//        System.arraycopy(bytes, 111, msgBody, 0, bytes.length - 111);
    }

    public String getBranchID() {
        return branchID;
    }

    public void setBranchID(String branchID) {
        this.branchID = branchID;
    }

    public String getTellerID() {
        return tellerID;
    }

    public void setTellerID(String tellerID) {
        this.tellerID = tellerID;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTerNum() {
        return terNum;
    }

    public void setTerNum(String terNum) {
        this.terNum = terNum;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getUeserID() {
        return ueserID;
    }

    public void setUeserID(String ueserID) {
        this.ueserID = ueserID;
    }

    public String getFileNum() {
        return fileNum;
    }

    public void setFileNum(String fileNum) {
        this.fileNum = fileNum;
    }

    public String getReFile() {
        return reFile;
    }

    public void setReFile(String reFile) {
        this.reFile = reFile;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getTxncode() {
        return txncode;
    }

    public void setTxncode(String txncode) {
        this.txncode = txncode;
    }

    public String getMsgBody() {
        return msgBody;
    }

    public void setMsgBody(String msgBody) {
        this.msgBody = msgBody;
    }

    public byte[] toByteArray() {
        StringBuilder headBuilder = new StringBuilder();
        StringBuilder pubBuilder = new StringBuilder();
        StringBuilder allBuilder = new StringBuilder();
        int length = 0;
        String todayStr = new SimpleDateFormat("yyMMddHH").format(new Date());
        // ����mac 16λ
        if (msgBody == null) {
            String md5 = MD5Helper.getMD5String(todayStr + ueserID.trim());
            mac = md5.substring(8, 24);
        } else {
            String md5 = MD5Helper.getMD5String(new String(msgBody) + todayStr.substring(0, 8) + ueserID.trim());
            mac = md5.substring(8, 24);
        }
        pubBuilder.append(branchID + "|");
        pubBuilder.append(tellerID + "|");
        pubBuilder.append(serialNo + "|");
        pubBuilder.append(ip + "|");
        pubBuilder.append(terNum + "|");
        pubBuilder.append(track + "|");
        pubBuilder.append(ueserID + "|");
        pubBuilder.append(fileNum + "|");
        if (fileNum != null && !fileNum.equals("0")) {
            pubBuilder.append(reFile + "|");
        }
        pubBuilder.append(msgBody + "|");
        // ĩβ�̶���16 λmac
        pubBuilder.append(StringUtils.leftPad(String.valueOf(mac), 16, " "));
        length = pubBuilder.toString().getBytes().length - 16;  // ���ĳ���  =�����峤�� ��ȥmac ����
        headBuilder.append(StringUtils.rightPad(String.valueOf(length), 8, " "));   //���ݰ��� 8
        headBuilder.append(StringUtils.rightPad(String.valueOf(txncode), 15, " "));  // ������  15
        allBuilder = headBuilder.append(pubBuilder);
        return allBuilder.toString().getBytes();
    }
}
