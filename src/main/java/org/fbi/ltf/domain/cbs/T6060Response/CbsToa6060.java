package org.fbi.ltf.domain.cbs.T6060Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6060 {
    @DataField(seq = 1)
    private String billNo;

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    @Override
    public String toString() {
        return "CbsToa6060{" +
                "billNo='" + billNo + '\'' +
                '}';
    }
}