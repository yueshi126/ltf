package org.fbi.ltf.domain.cbs.T6092Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6092 {
    @DataField(seq = 1)
    private String outAcctNo;
    @DataField(seq = 2)
    private String inAcctNo;
    @DataField(seq = 3)
    private BigDecimal acctMoney;
    @DataField(seq = 4)
    private String inOrgCode;


    public String getOutAcctNo() {
        return outAcctNo;
    }

    public void setOutAcctNo(String outAcctNo) {
        this.outAcctNo = outAcctNo;
    }

    public String getInAcctNo() {
        return inAcctNo;
    }

    public void setInAcctNo(String inAcctNo) {
        this.inAcctNo = inAcctNo;
    }

    public BigDecimal getAcctMoney() {
        return acctMoney;
    }

    public void setAcctMoney(BigDecimal acctMoney) {
        this.acctMoney = acctMoney;
    }

    public String getInOrgCode() {
        return inOrgCode;
    }

    public void setInOrgCode(String inOrgCode) {
        this.inOrgCode = inOrgCode;
    }

    @Override
    public String toString() {
        return "CbsToa6092{" +
                " outAcctNo='" + outAcctNo + '\'' +
                " inAcctNo='" + inAcctNo + '\'' +
                " acctMoney='" + acctMoney + '\'' +
                " inOrgCode='" + inOrgCode + '\'' +
                '}';
    }
}
