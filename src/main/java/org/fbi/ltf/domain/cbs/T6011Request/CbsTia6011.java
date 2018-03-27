package org.fbi.ltf.domain.cbs.T6011Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6011 {
    @DataField(seq = 1)
    private String seqNo;
    @DataField(seq = 2)
    private String txnDate;

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    public String getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(String seqNo) {
        this.seqNo = seqNo;
    }

    @Override
    public String toString() {
        return "CbsTia6011{" +
                "txnDate='" + txnDate + '\'' +
                ", seqNo='" + seqNo + '\'' +
                '}';
    }
}