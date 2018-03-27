package org.fbi.ltf.domain.tps;

/**
 * 票据分配上传报文
 * orderNo	订单编号	是		35
 transTime	交易时间	是		yyyy-MM-dd HH:mm:ss
 orderDetail	订单主键	是		33
 orderCharges	收费项目	否		35
 billNo	票据编号	是		12
 billMoney	票据面额	是		最大：99999.99
 */

public class TOAT60001 {
    private String orderNo;
    private String transTime;
    private String orderDetail;
    private String orderCharges;
    private String billNo;
    private String billMoney;

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

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillMoney() {
        return billMoney;
    }

    public void setBillMoney(String billMoney) {
        this.billMoney = billMoney;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  orderNo='" + orderNo + '\'' +
                ", transTime='" + transTime + '\'' +
                "  orderDetail='" + orderDetail + '\'' +
                ", orderCharges='" + orderCharges + '\'' +
                "  billNo='" + billNo + '\'' +
                ", billMoney='" + billMoney + '\'' +
                '}';
    }

}
