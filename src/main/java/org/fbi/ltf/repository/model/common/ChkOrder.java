package org.fbi.ltf.repository.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by Thinkpad on 2015/12/9.
 * No.	参数名称	参数解释	是否必填	数据长度（<）
 1	orderNo	订单编号	是	35
 2	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 3	amount	交易金额	是	最大：99999.99
 4	billNo	票据编号	否	20
 5	node	备注	否	128
 */
@XStreamAlias("order")
public class ChkOrder {
    private String orderNo;
    private String transTime;
    private String amount;
    private String billNo;
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

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
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
}
