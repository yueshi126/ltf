package org.fbi.ltf.domain.cbs.T6030Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6030 {
    @DataField(seq = 1)
    private String billNo;
    @DataField(seq = 2)
    private String ticketNo;

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

    @Override
    public String toString() {
        return "CbsTia6020{" +
                "ticketNo='" + ticketNo + '\'' +
                ", billNo='" + billNo + '\'' +
                '}';
    }
}