package org.fbi.ltf.domain.cbs.T6096Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6096 {
    /*
 	pkid	主键（前端不显示）	是	50
	orderNo	订单编号	是	35
	ticketNo	处罚决定书编号	是	50
	billNo	票据编号	是	12
	ticketAmount	处罚金额	是	最大：99999.99
	transTime	交易时间	是	yyyyMMdd HH:mm:ss
	overdueFine	滞纳金	是	最大：99999.99
	ticketTime	处理时间	是	yyyyMM-dd HH:mm:ss
	amount	交易金额	是	最大：999999.99
	node	备注	否	128
	itemNum	收费项目数量	是


     */
    @DataField(seq = 1)
    private String pkid;
    @DataField(seq = 2)
    private String orderNo;
    @DataField(seq = 3)
    private String ticketNo;
    @DataField(seq = 4)
    private String billNo;
    @DataField(seq = 5)
    private String ticketAmount;
    @DataField(seq = 6)
    private String transTime;
    @DataField(seq = 7)
    private String overdueFine;
    @DataField(seq = 8)
    private String ticketTime;
    @DataField(seq = 9)
    private String amount;
    @DataField(seq = 10)
    private String node;
    @DataField(seq = 11)
    private String itemNum;
    @DataField(seq = 12)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6096Response.CbsToa6096Item", totalNumberField = "itemNum")
    private java.util.List<CbsToa6096Item> items;

    public String getPkid() {
        return pkid;
    }

    public void setPkid(String pkid) {
        this.pkid = pkid;
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

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsToa6096Item> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6096Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "CbsToa6096{" +
                "  pkid='" + pkid  + '\'' +
                "  orderNo='" + orderNo  + '\'' +
                " transTime ='" + transTime  + '\'' +
                " ticketNo ='" +  ticketNo + '\'' +
                " ticketTime ='" + ticketTime  + '\'' +
                " amount ='" +  amount + '\'' +
                " overdueFine ='" + overdueFine  + '\'' +
                " ticketAmount ='" + ticketAmount  + '\'' +
                " billNo ='" + billNo  + '\'' +
                " node ='" + node  + '\'' +
                " itemNum ='" + itemNum  + '\'' +
                '}';
    }
}
