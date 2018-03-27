package org.fbi.ltf.domain.cbs.T6012Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *��·Ʊ�ݴ�ӡ������
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6012 {
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
        return "CbsTia6012{" +
                "ticketNo='" + ticketNo + '\'' +
                '}';
    }
}