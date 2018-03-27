package org.fbi.ltf.domain.cbs.T6082Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * Created by Administrator on 15-12-13.
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6082Item {
    @DataField(seq = 1)
    private String vchState;
    @DataField(seq = 2)
    private String billStartNo;
    @DataField(seq = 3)
    private String billEndNo;
    @DataField(seq = 4)
    private String operDate;
    @DataField(seq = 5)
    private String bus_code;

    public String getVchState() {
        return vchState;
    }

    public void setVchState(String vchState) {
        this.vchState = vchState;
    }

    public String getBillStartNo() {
        return billStartNo;
    }

    public void setBillStartNo(String billStartNo) {
        this.billStartNo = billStartNo;
    }

    public String getBillEndNo() {
        return billEndNo;
    }

    public void setBillEndNo(String billEndNo) {
        this.billEndNo = billEndNo;
    }

    public String getOperDate() {
        return operDate;
    }

    public void setOperDate(String operDate) {
        this.operDate = operDate;
    }

    public String getBus_code() {
        return bus_code;
    }

    public void setBus_code(String bus_code) {
        this.bus_code = bus_code;
    }

    @Override
    public String toString() {
        return "CbsToa6082Item{" +
                "vchState='" + vchState+ '\'' +
                ", billStartNo='" + billStartNo + '\'' +
                '}';
    }
}
