package org.fbi.ltf.repository.model.common;

import java.math.BigDecimal;

/**
 * Created by ZZP_YY on 2018-03-22.
 */
public class FsLtfTransAmt {
    private String areaCode;
    private BigDecimal totalamt;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public BigDecimal getTotalamt() {
        return totalamt;
    }

    public void setTotalamt(BigDecimal totalamt) {
        this.totalamt = totalamt;
    }
}
