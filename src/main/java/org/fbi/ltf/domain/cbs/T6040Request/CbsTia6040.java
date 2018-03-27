package org.fbi.ltf.domain.cbs.T6040Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6040 {
    @DataField(seq = 1)
    private String bankCode;
    @DataField(seq = 2)
    private String busCode;
    @DataField(seq = 3)
    private String billCode;
    @DataField(seq = 4)
    private String billBatch;

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    public String getBillBatch() {
        return billBatch;
    }

    public void setBillBatch(String billBatch) {
        this.billBatch = billBatch;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    @Override
    public String toString() {
        return "CbsToa1100{" +
                "bankCode='" + bankCode + '\'' +
                ",busCode='" + busCode + '\'' +
                ", billCode='" + billCode + '\'' +
                ", billBatch='" + billBatch + '\'' +
                '}';
    }
}
