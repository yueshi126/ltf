package org.fbi.ltf.domain.tps;

/**
 * �쳣Ʊ�ݱ���ӿڻش�����
 *No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	appName	������Դ	��	HLWFWPT �������ɷ� ICBC�������б�ʶ����̨�ɷ�
 2	orderNo	�������	��	35
 3	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 6	orderDetail	��������	��	33
 7	billNo	Ʊ�ݱ��	��	20
 8	causeType	�쳣����	��	2 ��Ʊ��ͬһ��Ʊ�Ŷ�Ӧ��������� 3 ��Ʊ�����ǻ�����Ʊ�ݣ�
 9	cause	�쳣ԭ��	��	200
 10	node	��ע	��	128
 */
public class TIAT60008 {
    private String appName;
    private String orderNo;
    private String transTime;
    private String orderDetail;
    private String billNo;
    private String causeType;
    private String cause;
    private String node;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getCauseType() {
        return causeType;
    }

    public void setCauseType(String causeType) {
        this.causeType = causeType;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "   orderNo='" + orderNo + '\'' +
                ",  transTime='" + transTime + '\'' +
                ",  orderDetail='" + orderDetail + '\'' +
                ",  appName='" + appName + '\'' +
                ",  billNo='" + billNo + '\'' +
                ",  causeType='" + causeType + '\'' +
                ",  cause='" + cause + '\'' +
                ",  node='" + node + '\'' +
                '}';
    }

}
