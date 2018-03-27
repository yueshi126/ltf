package org.fbi.ltf.domain.cbs.T6060Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6060 {
    @DataField(seq = 1)
    private String busCode;
    @DataField(seq = 2)
    private String billNo;
    @DataField(seq = 3)
    private String billMoney;
    @DataField(seq = 4)
    private String applyTime;
    @DataField(seq = 5)
    private String applyCause;
    @DataField(seq = 6)
    private String applyName;
    @DataField(seq = 7)
    private String node;

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
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

    @Override
    public String toString() {
        return "CbsTia6060{" +
                "busCode='" + busCode + '\'' +
                ",billNo='" + billNo + '\'' +
                ", billMoney='" + billMoney + '\'' +
                ", applyTime='" + applyTime + '\'' +
                ", applyCause='" + applyCause + '\'' +
                ", applyName='" + applyName + '\'' +
                ", node='" + node + '\'' +
                '}';
    }
}
