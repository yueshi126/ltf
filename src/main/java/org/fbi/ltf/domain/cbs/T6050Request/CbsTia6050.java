package org.fbi.ltf.domain.cbs.T6050Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6050 {
    @DataField(seq = 1)
    private String ticketCode;

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    @Override
    public String toString() {
        return "CbsTia6050{" +
                "ticketCode='" + ticketCode + '\'' +
                '}';
    }
}