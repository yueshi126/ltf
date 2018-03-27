package org.fbi.ltf.domain.cbs.T6080Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6080 {
    @DataField(seq = 1)
    private String areaCode;
    @DataField(seq = 2)
    private String chkDate;
    @DataField(seq = 3)
    private String accountNo;
    @DataField(seq = 4)
    private String totalAmt;
    @DataField(seq = 5)
    private String itemNum;
    @DataField(seq = 6)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6080Request.CbsTia6080Item", totalNumberField = "itemNum")
    private java.util.List<CbsTia6080Item> items;

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getChkDate() {
        return chkDate;
    }

    public void setChkDate(String chkDate) {
        this.chkDate = chkDate;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public List<CbsTia6080Item> getItems() {
        return items;
    }

    public void setItems(List<CbsTia6080Item> items) {
        this.items = items;
    }

    public String getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(String totalAmt) {
        this.totalAmt = totalAmt;
    }

    @Override
    public String toString() {
        return "CbsTia6080{" +
                "areaCode='" + areaCode + '\'' +
                ",chkDate='" + chkDate + '\'' +
                ", accountNo='" + accountNo + '\'' +
                ", totalAmt='" + totalAmt + '\'' +
                ", itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}
