package org.fbi.ltf.domain.tps;

/**
 * 票据领取回传报文
 * billStart	票据编号开始	是
 billEnd	票据编号结束	是
 sheets	一本票据有多少张票号	是
 billNum	票据数量	是
 */
public class TIAT60002 {
    private String billStart;
    private String billEnd;
    private String sheets;
    private String billNum;

    public String getBillStart() {
        return billStart;
    }

    public void setBillStart(String billStart) {
        this.billStart = billStart;
    }

    public String getBillEnd() {
        return billEnd;
    }

    public void setBillEnd(String billEnd) {
        this.billEnd = billEnd;
    }

    public String getSheets() {
        return sheets;
    }

    public void setSheets(String sheets) {
        this.sheets = sheets;
    }

    public String getBillNum() {
        return billNum;
    }

    public void setBillNum(String billNum) {
        this.billNum = billNum;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "   billStart'" + billStart + '\'' +
                ",  billEnd='" + billEnd + '\'' +
                ",  sheets='" + sheets + '\'' +
                ",  billNum='" + billNum + '\'' +
                '}';
    }

}
