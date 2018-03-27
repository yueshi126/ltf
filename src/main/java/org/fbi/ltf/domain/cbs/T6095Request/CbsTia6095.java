package org.fbi.ltf.domain.cbs.T6095Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by ZZP on 18-02-24.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6095 {
    /*
2	orderNo	订单编号	是	35
3	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
     orderDetail 订单主键 35
6	node1	补录字段1	是	35
     */

    @DataField(seq = 1)
    private String orderNo;
    @DataField(seq = 2)
    private String transTime;
    @DataField(seq = 3)
    private String orderDetail;
    @DataField(seq = 4)
    private String node1;


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

    public String getNode1() {
        return node1;
    }

    public void setNode1(String node1) {
        this.node1 = node1;
    }

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
    }

    @Override
    public String toString() {
        return "CbsTia6095{" +
                "orderNo='" + orderNo + '\'' +
                "transTime='" + transTime + '\'' +
                "node1='" + node1 + '\'' +
                "orderDetail='" + orderDetail + '\'' +
                '}';
    }
}
