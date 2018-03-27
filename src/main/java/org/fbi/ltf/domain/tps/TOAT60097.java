package org.fbi.ltf.domain.tps;



public class TOAT60097 {
    /* ��̨����ӿ�
2	orderNo	�������	��	35
3	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
4	ticketNo	������������	��	50
5	ticketTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
6	itemUnicode	�շ���Ŀ������ö��Ÿ�����	��	100
7	amount	���׽��	��	���999999.99
8	overdueFine	���ɽ�	��	���99999.99
9	ticketAmount	�������	��	���99999.99
10	billNo	Ʊ�ݱ��	��	12
11	node	��ע	��	128

  */
    private String bankCode;
    private String orderNo;
    private String transTime;
    private String ticketNo;
    private String ticketTime;
    private String itemUnicode;
    private String amount;
    private String overdueFine;
    private String ticketAmount;
    private String billNo;
    private String node;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

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

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getItemUnicode() {
        return itemUnicode;
    }

    public void setItemUnicode(String itemUnicode) {
        this.itemUnicode = itemUnicode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
    }

    public String getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(String ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }


    @Override
    public String toString() {
        return "TOAT60097{" +
                "  bankCode='" + bankCode  + '\'' +
                "  orderNo='" + orderNo  + '\'' +
                " transTime ='" + transTime  + '\'' +
                " ticketNo ='" +  ticketNo + '\'' +
                " ticketTime ='" + ticketTime  + '\'' +
                " itemUnicode ='" + itemUnicode  + '\'' +
                " amount ='" +  amount + '\'' +
                " overdueFine ='" + overdueFine  + '\'' +
                " ticketAmount ='" + ticketAmount  + '\'' +
                " billNo ='" + billNo  + '\'' +
                " node ='" + node  + '\'' +
                '}';
    }

}
