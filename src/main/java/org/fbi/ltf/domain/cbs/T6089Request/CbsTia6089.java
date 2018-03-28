package org.fbi.ltf.domain.cbs.T6089Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by ZZP on 18-03-28
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6089 {
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
        return "CbsTia6089{" +
                " ticketNo ='" + ticketNo  + '\'' +
                '}';
    }
}
