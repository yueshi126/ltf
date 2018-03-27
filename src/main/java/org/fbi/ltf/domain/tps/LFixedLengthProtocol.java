package org.fbi.ltf.domain.tps;

import org.apache.commons.lang.StringUtils;
import org.fbi.ltf.helper.MD5Helper;
import org.fbi.ltf.helper.StringPad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Linking����Э��
 */
public class LFixedLengthProtocol {
    private static Logger logger = LoggerFactory.getLogger(LFixedLengthProtocol.class);

    public String version = "1.0";                        // �汾��    3
    public String serialNo = "";                          // ��ˮ��    18
    public String rtnCode = "0000";                       // ������    4
    public String txnCode = "";                           // ������    7
    public String branchID = "";                          // ������  9
    public String tellerID = "";                          // ��Ա���   12
    public String ueserID = "";                           // �û�ID    6
    public String appID = "";                             // Ӧ�ñ�ʶ   6
    public String txnTime = "";                           // ����ʱ��   14
    // ������+8λ��������+�û�ID����ASC�ַ���ʾ��16����MD5ֵ
    public String mac;                                   // message anthentication code 32
    public byte[] msgBody;                               // ������

    public void assembleFields(byte[] bytes) {
        if (bytes == null || bytes.length < 111) {
            throw new RuntimeException("����ͷ�ֽڳ��ȴ���");
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
        // ������
        msgBody = new byte[bytes.length - 111];
        System.arraycopy(bytes, 111, msgBody, 0, bytes.length - 111);
    }

    public byte[] toByteArray() {
        StringBuilder headBuilder = new StringBuilder();

        // ���ĳ���
//        int length = msgBody == null ? (6 + 111) : (6 + 111 + msgBody.length);
        int length = 0;
        if (!"0000".equals(rtnCode) && !"0001".equals(rtnCode)) {
            // ������Ϣ
           /* msgBody = msgBody.length > 16 ?
                    new String(msgBody).substring(0, 16).getBytes() :
                    StringUtils.rightPad(new String(msgBody), 16, " ").getBytes();*/
            msgBody = StringPad.rightPad4ChineseToByteLength(new String(msgBody), 34, " ").getBytes();
        }
        // ����mac
        if (msgBody == null) {
            String md5 = MD5Helper.getMD5String(txnTime.substring(0, 8) + ueserID.trim());
            mac = md5;
        } else {
            String md5 = MD5Helper.getMD5String(new String(msgBody) + txnTime.substring(0, 8) + ueserID.trim());
            mac = md5;
        }
        length = msgBody == null ? (6 + 111) : (6 + 111 + msgBody.length);
        headBuilder.append(StringUtils.rightPad(String.valueOf(length), 6, " "));
        // ����ͷ���ֶ�
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
        // ������
        if (msgBody == null) return headBuilder.toString().getBytes();
        else {
            byte[] msgBytes = new byte[length];
            logger.info("[���͵�linking]����ͷ:" + headBuilder.toString() + " �ֽ�����" + headBuilder.toString().getBytes().length);
            logger.info("[���͵�linking]������:" + new String(msgBody) + " �ֽ�����" + msgBody.length);
            System.arraycopy(headBuilder.toString().getBytes(), 0, msgBytes, 0, (6 + 111));
            System.arraycopy(msgBody, 0, msgBytes, (6 + 111), msgBody.length);
            return msgBytes;
        }
    }
}
