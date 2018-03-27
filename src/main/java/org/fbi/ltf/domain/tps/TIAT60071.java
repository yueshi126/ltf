package org.fbi.ltf.domain.tps;

/**
 * 订单信息查询回传报文 综合应用平台对账结果
 No.	参数名称	参数解释	是否必填	数据长度（<）
 1	orderNo	订单编号	是	35
 2	orderDetail	订单主键	是	35
 3	ticketNo	处罚决定书编号	是	35
 4	ledgerType	对账类型	是	1 自动对账 2人工勘误
 5	ledgerState	对账状态	是	1 对账成功
                             2 对账失败（重复对账）
                             3 对账失败（无此处罚决定书）
                             4 对账失败（罚款金额不足）
                             5 对账失败（超过对账期限）
                             6 对账失败（不明确）
                             7 对账失败（滞纳金不足）
                             8 对账成功（民警48小时无录入）
                             9对账失败（服务平台无录入）
                             10对账失败（服务平台重复缴费）
 6	ledgerTime	对账时间	是	yyyy-MM-dd HH:mm:ss
 7	node	备注	否	128

 */
public class TIAT60071 {
    private String orderNo;
    private String orderDetail;
    private String ticketNo;
    private String ledgerType;
    private String ledgerState;
    private String ledgerTime;
    private String node;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getLedgerType() {
        return ledgerType;
    }

    public void setLedgerType(String ledgerType) {
        this.ledgerType = ledgerType;
    }

    public String getLedgerState() {
        return ledgerState;
    }

    public void setLedgerState(String ledgerState) {
        this.ledgerState = ledgerState;
    }

    public String getLedgerTime() {
        return ledgerTime;
    }

    public void setLedgerTime(String ledgerTime) {
        this.ledgerTime = ledgerTime;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "TIAT60071{" +
                "   orderNo='" + orderNo + '\'' +
                ",  orderDetail='" + orderDetail + '\'' +
                ",  ticketNo='" + ticketNo + '\'' +
                ",  ledgerType='" + ledgerType + '\'' +
                ",  ledgerState='" + ledgerState + '\'' +
                ",  ledgerTime='" + ledgerTime + '\'' +
                ",  node='" + node + '\'' +
                '}';
    }

}
