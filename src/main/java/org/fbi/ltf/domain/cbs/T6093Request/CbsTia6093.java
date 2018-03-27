package org.fbi.ltf.domain.cbs.T6093Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6093 {
    @DataField(seq = 1)
    private String txnDate;

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }

    @Override
    public String toString() {
        return "CbsTia6093{" +
                "txnDate='" + txnDate + '\'' +
                '}';
    }
}
