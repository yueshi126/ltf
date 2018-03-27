package org.fbi.ltf.domain.cbs.T6099Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by ZZP on 18-03-06
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6099 {
    /*
    ticketNo ·£µ¥ºÅ
*/
    @DataField(seq = 1)
    private String ticketNo;

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    @Override
    public String toString() {
        return "CbsTia6099{" +
                " ticketNo ='" + ticketNo  + '\'' +
                '}';
    }
}
