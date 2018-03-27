package org.fbi.ltf.domain.tps;

/**
 * 订单信息查询上传报文
 * No.	参数名称	参数解释	是否必填	数据长度（<）
 1	bankCode	银行标识（各银行提供）	是	20
 2	type	查询类型	是	1001 待分票
 3	node1	查询字段1	是	128
 4	node2	查询字段2	否	128
 5	node3	查询字段3	否	128
 6	node4	查询字段4	否	128
 */

public class TOAT60007 {
    private String bankCode;
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
        return "MsgBody{" +
                "  bankCode='" + bankCode + '\'' +
                ", type='" + type + '\'' +
                ", node1='" + node1 + '\'' +
                ", node2='" + node2 + '\'' +
                ", node3='" + node3 + '\'' +
                ", node4='" + node4 + '\'' +
                '}';
    }

}
