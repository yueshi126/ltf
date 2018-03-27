package org.fbi.ltf.domain.cbs.T6080Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * Created by Administrator on 15-12-13.
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsTia6080Item {
    @DataField(seq = 1)
    private String seqNo;
    @DataField(seq = 2)
    private String txnAmt;
    @DataField(seq = 3)
    private String payFlag;

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    public String getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(String txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getPayFlag() {
        return payFlag;
    }

    public void setPayFlag(String payFlag) {
        this.payFlag = payFlag;
    }

    @Override
    public String toString() {
        return "CbsTia6080Item{" +
                "seqNo='" + seqNo + '\'' +
                ", txnAmt='" + txnAmt + '\'' +
                ", payFlag=" + payFlag +
                '}';
    }
}
