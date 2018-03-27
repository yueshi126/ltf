package org.fbi.ltf.repository.model.common;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by Thinkpad on 2015/12/9.
 * No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	orderNo	�������	��	35
 2	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 3	amount	���׽��	��	���99999.99
 4	billNo	Ʊ�ݱ��	��	20
 5	node	��ע	��	128
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
