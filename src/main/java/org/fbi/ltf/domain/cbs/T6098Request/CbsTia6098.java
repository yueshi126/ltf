package org.fbi.ltf.domain.cbs.T6098Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by ZZP on 18-03-06
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6098 {
    /*
    flag	挂账标志	是	1-挂账 2-正常
    begTransTime	交易日期	是	yyyyMMdd
    endTransTime	交易日期	是	yyyyMMdd
*/
    @DataField(seq = 1)
    private String flag;
    @DataField(seq = 2)
    private String begTransTime;
    @DataField(seq = 3)
    private String endTransTime;
    @DataField(seq = 4)
    private String ticketNo;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getBegTransTime() {
        return begTransTime;
    }

    public void setBegTransTime(String begTransTime) {
        this.begTransTime = begTransTime;
    }

    public String getEndTransTime() {
        return endTransTime;
    }

    public void setEndTransTime(String endTransTime) {
        this.endTransTime = endTransTime;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    @Override
    public String toString() {
        return "CbsTia6098{" +
                " flag ='" + flag  + '\'' +
                " begTransTime ='" + begTransTime  + '\'' +
                " endTransTime ='" + endTransTime  + '\'' +
                " ticketNo ='" + ticketNo  + '\'' +
                '}';
    }
}
