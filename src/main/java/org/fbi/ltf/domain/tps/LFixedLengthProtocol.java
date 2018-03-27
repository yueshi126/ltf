package org.fbi.ltf.domain.tps;

import org.apache.commons.lang.StringUtils;
import org.fbi.ltf.helper.MD5Helper;
import org.fbi.ltf.helper.StringPad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Linking定长协议
 */
public class LFixedLengthProtocol {
    private static Logger logger = LoggerFactory.getLogger(LFixedLengthProtocol.class);

    public String version = "1.0";                        // 版本号    3
    public String serialNo = "";                          // 流水号    18
    public String rtnCode = "0000";                       // 返回码    4
    public String txnCode = "";                           // 交易码    7
    public String branchID = "";                          // 网点编号  9
    public String tellerID = "";                          // 柜员编号   12
    public String ueserID = "";                           // 用户ID    6
    public String appID = "";                             // 应用标识   6
    public String txnTime = "";                           // 交易时间   14
    // 报文体+8位交易日期+用户ID，用ASC字符表示的16进制MD5值
    public String mac;                                   // message anthentication code 32
    public byte[] msgBody;                               // 报文体

    public void assembleFields(byte[] bytes) {
        if (bytes == null || bytes.length < 111) {
            throw new RuntimeException("报文头字节长度错误！");
        }
        version = new String(bytes, 0, 3).trim();
        serialNo = new String(bytes, 3, 18).trim();
        rtnCode = new String(bytes, 21, 4).trim();
        txnCode = new String(bytes, 25, 7).trim();
        branchID = new String(bytes, 32, 9).trim();
        tellerID = new String(bytes, 41, 12).trim();
        ueserID = new String(bytes, 53, 6).trim();
        appID = new String(bytes, 59, 6).trim();
        txnTime = new String(bytes, 65, 14).trim();
        mac = new String(bytes, 79, 32);
        // 报文体
        msgBody = new byte[bytes.length - 111];
        System.arraycopy(bytes, 111, msgBody, 0, bytes.length - 111);
    }

    public byte[] toByteArray() {
        StringBuilder headBuilder = new StringBuilder();

        // 报文长度
//        int length = msgBody == null ? (6 + 111) : (6 + 111 + msgBody.length);
        int length = 0;
        if (!"0000".equals(rtnCode) && !"0001".equals(rtnCode)) {
            // 错误信息
           /* msgBody = msgBody.length > 16 ?
                    new String(msgBody).substring(0, 16).getBytes() :
                    StringUtils.rightPad(new String(msgBody), 16, " ").getBytes();*/
            msgBody = StringPad.rightPad4ChineseToByteLength(new String(msgBody), 34, " ").getBytes();
        }
        // 生成mac
        if (msgBody == null) {
            String md5 = MD5Helper.getMD5String(txnTime.substring(0, 8) + ueserID.trim());
            mac = md5;
        } else {
            String md5 = MD5Helper.getMD5String(new String(msgBody) + txnTime.substring(0, 8) + ueserID.trim());
            mac = md5;
        }
        length = msgBody == null ? (6 + 111) : (6 + 111 + msgBody.length);
        headBuilder.append(StringUtils.rightPad(String.valueOf(length), 6, " "));
        // 报文头内字段
        headBuilder.append(StringUtils.rightPad(version, 3, " "));
        headBuilder.append(StringUtils.rightPad(serialNo, 18, " "));
        headBuilder.append(StringUtils.rightPad(rtnCode, 4, " "));
        headBuilder.append(StringUtils.rightPad(txnCode, 7, " "));
        headBuilder.append(StringUtils.rightPad(branchID, 9, " "));
        headBuilder.append(StringUtils.rightPad(tellerID, 12, " "));
        headBuilder.append(StringUtils.rightPad(ueserID, 6, " "));
        headBuilder.append(StringUtils.rightPad(appID, 6, " "));
        headBuilder.append(StringUtils.rightPad(txnTime, 14, " "));
        headBuilder.append(StringUtils.rightPad(mac, 32, " "));
        // 报文体
        if (msgBody == null) return headBuilder.toString().getBytes();
        else {
            byte[] msgBytes = new byte[length];
            logger.info("[发送到linking]报文头:" + headBuilder.toString() + " 字节数：" + headBuilder.toString().getBytes().length);
            logger.info("[发送到linking]报文体:" + new String(msgBody) + " 字节数：" + msgBody.length);
            System.arraycopy(headBuilder.toString().getBytes(), 0, msgBytes, 0, (6 + 111));
            System.arraycopy(msgBody, 0, msgBytes, (6 + 111), msgBody.length);
            return msgBytes;
        }
    }
}
