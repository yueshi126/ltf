package org.fbi.ltf.domain.cbs.T6081Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * Created by Administrator on 15-12-13.
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6081Item {
    @DataField(seq = 1)
    private String ticketNo;
    @DataField(seq = 2)
    private String billNo;
    @DataField(seq = 3)
    private String txnAmt;
    @DataField(seq = 4)
    private String operNo;


    public String getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(String txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getOperNo() {
        return operNo;
    }

    public void setOperNo(String operNo) {
        this.operNo = operNo;
    }

    @Override
    public String toString() {
        return "CbsToa6081Item{" +
                "ticketNo='" + ticketNo+ '\'' +
                ", txnAmt='" + txnAmt + '\'' +
                ", billNo=" + billNo +
                ", operNo=" + operNo +
                '}';
    }
}
