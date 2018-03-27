package org.fbi.ltf.domain.tps;

/**
 * 订单信息查询回传报文
 *No.	参数名称	参数解释	是否必填	数据长度（<）
 1	orderNo	订单编号	是	35
 2	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 3	orderDetail	订单主键	是	33
 4	bankMare	领票地点标识（各银行提供）	是	20
 5	payment	罚款金额	是	最大：99999.99
 6	overdueFine	滞纳金	否	最大：99999.99
 7	ticketNo	处罚决定书编号	是	50
 8	payerName	付款人姓名	是	64
 9	dept	机关代码	是	20
 10	node	备注	否	128
 */
public class TIAT60007 {
    private String orderNo;
    private String transTime;
    private String orderDetail;
    private String bankMare;
    private String payment;
    private String overdueFine;
    private String ticketNo;
    private String payerName;
    private String dept;
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

    public String getBankMare() {
        return bankMare;
    }

    public void setBankMare(String bankMare) {
        this.bankMare = bankMare;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "   orderNo='" + orderNo + '\'' +
                ",  transTime='" + transTime + '\'' +
                ",  orderDetail='" + orderDetail + '\'' +
                ",  bankMare='" + bankMare + '\'' +
                ",  payment='" + payment + '\'' +
                ",  ticketNo='" + ticketNo + '\'' +
                ",  overdueFine='" + overdueFine + '\'' +
                ",  node='" + node + '\'' +
                ",  payerName='" + payerName + '\'' +
                ",  dept='" + dept + '\'' +
                '}';
    }

}
