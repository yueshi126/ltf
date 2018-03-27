package org.fbi.ltf.repository.model.common;

import java.math.BigDecimal;

public class FsLtfOutAcct {

    private String orderno;
    private BigDecimal totalamt;
    private String orderNum;
    private String orderno2;
    private BigDecimal totalamt2;
    private String orderNum2;

    public String getOrderno() {
        return orderno;
    }

    public void setOrderno(String orderno) {
        this.orderno = orderno;
    }



    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getOrderno2() {
        return orderno2;
    }

    public void setOrderno2(String orderno2) {
        this.orderno2 = orderno2;
    }

    public BigDecimal getTotalamt() {
        return totalamt;
    }

    public void setTotalamt(BigDecimal totalamt) {
        this.totalamt = totalamt;
    }

    public BigDecimal getTotalamt2() {
        return totalamt2;
    }

    public void setTotalamt2(BigDecimal totalamt2) {
        this.totalamt2 = totalamt2;
    }

    public String getOrderNum2() {
        return orderNum2;
    }

    public void setOrderNum2(String orderNum2) {
        this.orderNum2 = orderNum2;
    }
}