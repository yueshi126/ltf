package org.fbi.ltf.domain.tps;



public class TOAT60012 {
    /* 罚单信息查询接口
1	bankCode	银行标识	是	20
2	salesName	业务查询人姓名	否	50
4	ticketNo	处罚决定书编号	是	15
5	node	备注	否	128


  */
    private String bankCode;
    private String salesName;
    private String ticketNo;
    private String node;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "TOAT60012{" +
                " bankCode ='" + bankCode  + '\'' +
                " salesName ='" + salesName  + '\'' +
                " ticketNo ='" + ticketNo  + '\'' +
                " node ='" + node  + '\'' +
                '}';
    }

}
