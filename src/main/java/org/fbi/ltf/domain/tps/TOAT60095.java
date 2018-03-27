package org.fbi.ltf.domain.tps;

/**
 * 交款信息上传报文
 * 参数名称	参数解释	是否必填	数据长度（<）
 bankCode	银行标识（各银行提供）	是	20
 BankTake	柜台标识(各银行提供)	是	33
 orderNo	订单编号	是	35
 transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 amount	交易金额	是	最大：999999.99
 overdueFine	滞纳金	是	最大：99999.99
 busCode	处理业务	是	35
 orderCharges	收费项目（多个用逗号隔开）	是	35
 accountNo	收款财政账户	否	33
 billNo	票据号码	是	12
 ticketAmount	处罚决定书编号	是	50
 ticketTime	违章时间	是	yyyy-MM-dd HH:mm:ss
 ticketAmount	罚款金额	是	最大：99999.99
 payerName	付款人	是	50
 idCard	付款人身份证号	否	20
 phoneNo	付款人联系方式	否	15
 callDept	采集机关代码（到大队）	否	20
 handleDept	处罚机关代码（到大队）	是	20
 driveNo	当事人驾驶证编号	否	20
 runNo	当事人行驶证编号	否	20
 plateNo	车牌号码	否	20
 plateColor	车牌颜色	否	2
 carType	车辆类型	否	2
 carColor	车身颜色	否	2
 party	当事人姓名	是	50
 partyCard	当事人身份证号	否	18
 salesNo	业务员代码	否	20
 salesName	业务员姓名	否	50
 node	备注	否	288
 */

public class TOAT60095 {
    /*
1	bankCode	银行标识	是	20
2	orderNo	订单编号	是	35
3	transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
4	orderDetail	订单主键	否	35
5	type	补录类型	是	1001:补录缴款用户账号
6	node1	补录字段1	是	35
7	node2	补录字段2	否
8	node3	补录字段3	否
9	node4	补录字段4	否
   */
    private String bankCode;
    private String orderNo;
    private String transTime;
    private String orderDetail;
    private String type;
    private String node1;
    private String node2;
    private String node3;
    private String node4;

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

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNode1() {
        return node1;
    }

    public void setNode1(String node1) {
        this.node1 = node1;
    }

    public String getNode2() {
        return node2;
    }

    public void setNode2(String node2) {
        this.node2 = node2;
    }

    public String getNode3() {
        return node3;
    }

    public void setNode3(String node3) {
        this.node3 = node3;
    }

    public String getNode4() {
        return node4;
    }

    public void setNode4(String node4) {
        this.node4 = node4;
    }

    @Override
    public String toString() {
        return "CbsTia6095{" +
                "bankCode='" + bankCode + '\'' +
                "orderNo='" + orderNo + '\'' +
                "transTime='" + transTime + '\'' +
                "orderDetail='" + orderDetail + '\'' +
                "type='" + type + '\'' +
                "node1='" + node1 + '\'' +
                "node2='" + node2 + '\'' +
                "node3='" + node3 + '\'' +
                "node4='" + node4 + '\'' +
                '}';
    }

}
