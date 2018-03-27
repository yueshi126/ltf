package org.fbi.ltf.domain.tps;



public class TOAT60097 {
    /* 柜台勘误接口
2	orderNo	订单编号	是	35
3	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
4	ticketNo	处罚决定书编号	是	50
5	ticketTime	处理时间	是	yyyy-MM-dd HH:mm:ss
6	itemUnicode	收费项目（多个用逗号隔开）	是	100
7	amount	交易金额	是	最大：999999.99
8	overdueFine	滞纳金	是	最大：99999.99
9	ticketAmount	处罚金额	是	最大：99999.99
10	billNo	票据编号	是	12
11	node	备注	否	128

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
