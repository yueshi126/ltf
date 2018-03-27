package org.fbi.ltf.domain.cbs.T6091Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zzp on 18-03-27
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6091 {
    @DataField(seq = 1)
    private String preActSerial;

    public String getPreActSerial() {
        return preActSerial;
    }

    public void setPreActSerial(String preActSerial) {
        this.preActSerial = preActSerial;
    }

    @Override
    public String toString() {
        return "CbsTia6091{" +
                "preActSerial='" + preActSerial + '\'' +
                '}';
    }

}
