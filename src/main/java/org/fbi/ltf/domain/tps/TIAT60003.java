package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ�ش�����
 * 1	orderNo	�������	��	35
 2	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 3	orderDetail	��������	��	33
 4	orderCharges	�շ���Ŀ������ö��Ÿ�����	��	35
 5	bankMare	��Ʊ�ص��ʶ���������ṩ��	��	20
 6	payment	�շѽ��	��	���99999.99
 7	ticketNo	������������	��	50
 8	ticketCode	Υ�´��루����ö��Ÿ�����	��	35
 9	ticketTime	Υ��ʱ��	��	yyyy-MM-dd HH:mm:ss
 10	driveNo	֤������	��	20
 11	payerName	����������	��	64
 12	dept	���ش���	��	20
 */
public class TIAT60003 {
    private String orderNo;
    private String transTime;
    private String orderDetail;
    private String orderCharges;
    private String bankMare;
    private String payment;
    private String ticketNo;
    private String ticketCode;
    private String ticketTime;
    private String driveNo;
    private String payerName;
    private String dept;

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

    public String getOrderCharges() {
        return orderCharges;
    }

    public void setOrderCharges(String orderCharges) {
        this.orderCharges = orderCharges;
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

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getDriveNo() {
        return driveNo;
    }

    public void setDriveNo(String driveNo) {
        this.driveNo = driveNo;
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

    @Override
    public String toString() {
        return "MsgBody{" +
                "   orderNo='" + orderNo + '\'' +
                ",  transTime='" + transTime + '\'' +
                ",  orderDetail='" + orderDetail + '\'' +
                ",  orderCharges='" + orderCharges + '\'' +
                ",  bankMare='" + bankMare + '\'' +
                ",  payment='" + payment + '\'' +
                ",  ticketNo='" + ticketNo + '\'' +
                ",  ticketCode='" + ticketCode + '\'' +
                ",  ticketTime='" + ticketTime + '\'' +
                ",  driveNo='" + driveNo + '\'' +
                ",  payerName='" + payerName + '\'' +
                ",  dept='" + dept + '\'' +
                '}';
    }

}
