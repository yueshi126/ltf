package org.fbi.ltf.domain.cbs.T6050Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6050 {
    @DataField(seq = 1)
    private String chargeName;
    @DataField(seq = 2)
    private String amount;

    public String getChargeName() {
        return chargeName;
    }

    public void setChargeName(String chargeName) {
        this.chargeName = chargeName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CbsToa6050{" +
                "chargeName='" + chargeName + '\'' +
                ",amount='" + amount + '\'' +
                '}';
    }
}