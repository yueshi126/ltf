package org.fbi.ltf.domain.tps;

/**
 * 废票信息上传报文
 * No.	参数名称	参数解释	是否必填	数据长度（<）
 1	bankCode	银行标识（各银行提供）	是	20
 2	busCode	业务代码(1互联网票据 2柜台缴费票据)	是	3
 3	billNo	票据编号	是	20
 4	billMoney	票据面额	否	3（定额票才会使用该字段）
 5	applyTime	废票时间	是	yyyy-MM-dd HH:mm:ss
 6	applyCause	废票理由	是	200
 7	applyName	废票责任人	是	50（责任倒查时使用，如为系统使用，可填写银行名称）
 8	Node	备注	否	128（新票据编号）
 */

public class TOAT60006 {
    private String bankCode;
    private String busCode;
    private String billNo;
    private String billMoney;
    private String applyTime;
    private String applyCause;
    private String applyName;
    private String node;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyCause() {
        return applyCause;
    }

    public void setApplyCause(String applyCause) {
        this.applyCause = applyCause;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
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
                "  bankCode='" + bankCode + '\'' +
                ", busCode='" + busCode + '\'' +
                ", billNo='" + billNo + '\'' +
                ", billMoney='" + billMoney + '\'' +
                ", applyTime='" + applyTime + '\'' +
                ", applyCause='" + applyCause + '\'' +
                ", applyName='" + applyName + '\'' +
                ", node='" + node + '\'' +
                '}';
    }

}
