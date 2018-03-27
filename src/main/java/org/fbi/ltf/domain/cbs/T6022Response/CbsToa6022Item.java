package org.fbi.ltf.domain.cbs.T6022Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.util.List;

/**
 * 特色平台网络票据打印响应报文
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6022Item {
    @DataField(seq = 1)
    private String serialNo;
    @DataField(seq = 2)
    private String billNo;
    @DataField(seq = 3)
    private String ticketNo;
    @DataField(seq = 4)
    private String oprDate;
    @DataField(seq = 5)
    private String payment;
    @DataField(seq = 6)
    private String payerName;
    @DataField(seq = 7)
    private String areaCode;
    @DataField(seq = 8)
    private String overdueFine;
/*    @DataField(seq = 7)
    private String itemNum;*/
    @DataField(seq = 9)
    private String items;
    /*@OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6020Response.CbsToa6020SubItem", totalNumberField = "itemNum")
    private List<CbsToa6020SubItem> items;*/

    public String getOprDate() {
        return oprDate;
    }

    public void setOprDate(String oprDate) {
        this.oprDate = oprDate;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
    }

/*    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }*/

/*    public List<CbsToa6020SubItem> getItems() {
        return items;
    }

    public void setItems(List<CbsToa6020SubItem> items) {
        this.items = items;
    }*/

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    @Override
    public String toString() {
        return "CbsToa6020Item{" +
                "serialNo='" + serialNo + '\'' +
                ", billNo='" + billNo + '\'' +
                ", ticketNo='" + ticketNo + '\'' +
                ", oprDate=" + oprDate +
                ", payment='" + payment + '\'' +
                ", payerName='" + payerName + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", overdueFine='" + overdueFine + '\'' +
//                ", itemNum='" + itemNum + '\'' +
                ", items='" + items + '\'' +
                '}';
    }
}