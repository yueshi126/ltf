package org.fbi.ltf.domain.cbs.T6081Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6081 {
    @DataField(seq = 1)
    private String txnDate;
    @DataField(seq = 2)
    private String operNo;
    @DataField(seq = 3)
    private String flag;

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getOperNo() {
        return operNo;
    }

    public void setOperNo(String operNo) {
        this.operNo = operNo;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return "CbsTia6081{" +
                "txnDate='" + txnDate + '\'' +
                ",operNo='" + operNo + '\'' +
                ",flag='" + flag + '\'' +
                '}';
    }
}
