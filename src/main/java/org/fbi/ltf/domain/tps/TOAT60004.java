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

public class TOAT60004 {
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
