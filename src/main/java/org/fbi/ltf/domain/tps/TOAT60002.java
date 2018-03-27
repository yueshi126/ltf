package org.fbi.ltf.domain.tps;

/**
 * 票据领取上传报文
 * 参数名称	参数解释	是否必填	数据长度（<）
 bankCode	银行标识（各银行提供）	是	20
 busCode	业务代码(1互联网票据 2柜台缴费票据)	是	3
 billCode	票据类型	是	4
 billBatch	领票码（各银行提供，每次调用需唯一不重复，重复会返回第一次获取时返回）	是	20
 */

public class TOAT60002 {
    private String bankCode;
    private String busCode;
    private String billCode;
    private String billBatch;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getBillCode() {
        return billCode;
    }

    public void setBillCode(String billCode) {
        this.billCode = billCode;
    }

    public String getBillBatch() {
        return billBatch;
    }

    public void setBillBatch(String billBatch) {
        this.billBatch = billBatch;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  bankCode='" + bankCode + '\'' +
                ", busCode='" + busCode + '\'' +
                "  billCode='" + billCode + '\'' +
                ", billBatch='" + billBatch + '\'' +
                '}';
    }

}
