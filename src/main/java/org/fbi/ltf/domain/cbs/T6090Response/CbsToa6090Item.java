package org.fbi.ltf.domain.cbs.T6090Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * Created by Administrator on 15-12-13.
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6090Item {
    @DataField(seq = 1)
    private String acctNo;
    @DataField(seq = 2)
    private String txnAmt;
    @DataField(seq = 3)
    private String entpNo;

    public String getAcctNo() {
        return acctNo;
    }

    public void setAcctNo(String acctNo) {
        this.acctNo = acctNo;
    }

    public String getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(String txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getEntpNo() {
        return entpNo;
    }

    public void setEntpNo(String entpNo) {
        this.entpNo = entpNo;
    }

    @Override
    public String toString() {
        return "CbsToa6090Item{" +
                "acctNo='" + acctNo+ '\'' +
                ", txnAmt='" + txnAmt + '\'' +
                ", remark='" + entpNo + '\'' +
                '}';
    }
}
