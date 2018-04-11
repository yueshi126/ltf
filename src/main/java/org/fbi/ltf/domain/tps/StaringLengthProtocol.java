package org.fbi.ltf.domain.tps;

import org.apache.commons.lang.StringUtils;
import org.fbi.ltf.helper.MD5Helper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 特色定长报文
 * Created by ZZP_YY on 2018-03-22.
 */
public class StaringLengthProtocol {
    public String branchID = "";                        // 机构号    3
    public String tellerID = "";                          // 柜员号    18
    public String serialNo = "";                       // 请求流水号    4
    public String ip = "";                           // IP地址    7
    public String terNum = "";                          // 终端号  9
    public String track = "";                          // 二磁道数据   12
    public String ueserID = "";                           // 用户ID    6
    public String fileNum = "0";                             // 接收文件数   6
    public String reFile = "";                           // 接收文件   14
    // 报文体+8位交易日期+用户ID，用ASC字符表示的16进制MD5值
    public String mac;                                   //  16位
    public String txncode;                                   //  16位
    public String msgBody;                               // 报文体
    public void assembleFields(byte[] bytes) {
        if (bytes == null || bytes.length < 111) {
            throw new RuntimeException("报文头字节长度错误！");
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
        // 报文体
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
        // 生成mac 16位
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
        // 末尾固定加16 位mac
        pubBuilder.append(StringUtils.leftPad(String.valueOf(mac), 16, " "));
        length = pubBuilder.toString().getBytes().length - 16;  // 报文长度  =报文体长度 减去mac 长度
        headBuilder.append(StringUtils.rightPad(String.valueOf(length), 8, " "));   //数据包长 8
        headBuilder.append(StringUtils.rightPad(String.valueOf(txncode), 15, " "));  // 交易码  15
        allBuilder = headBuilder.append(pubBuilder);
        return allBuilder.toString().getBytes();
    }
}
