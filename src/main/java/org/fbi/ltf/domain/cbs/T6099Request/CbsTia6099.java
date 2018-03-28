package org.fbi.ltf.domain.cbs.T6099Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by ZZP on 18-03-06
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6099 {
    @DataField(seq = 1)
    private String begTransTime;
    @DataField(seq = 2)
    private String endTransTime;

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

    @Override
    public String toString() {
        return "CbsTia6099{" +
                " begTransTime ='" + begTransTime  + '\'' +
                " endTransTime ='" + endTransTime  + '\'' +
                '}';
    }
}
