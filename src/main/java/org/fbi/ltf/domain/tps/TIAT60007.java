package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ��ѯ�ش�����
 *No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	orderNo	�������	��	35
 2	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 3	orderDetail	��������	��	33
 4	bankMare	��Ʊ�ص��ʶ���������ṩ��	��	20
 5	payment	������	��	���99999.99
 6	overdueFine	���ɽ�	��	���99999.99
 7	ticketNo	������������	��	50
 8	payerName	����������	��	64
 9	dept	���ش���	��	20
 10	node	��ע	��	128
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
