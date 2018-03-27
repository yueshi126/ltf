package org.fbi.ltf.domain.tps;

/**
 * 异常票据变更接口回传报文
 *No.	参数名称	参数解释	是否必填	数据长度（<）
 1	appName	订单来源	是	HLWFWPT 互联网缴费 ICBC（各银行标识）柜台缴费
 2	orderNo	订单编号	是	35
 3	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 6	orderDetail	订单主键	否	33
 7	billNo	票据编号	是	20
 8	causeType	异常种类	是	2 重票（同一个票号对应多个订单） 3 多票（不是互联网票据）
 9	cause	异常原因	否	200
 10	node	备注	否	128
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
