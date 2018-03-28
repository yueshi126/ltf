package org.fbi.ltf.domain.cbs.T6020Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;

import java.util.List;

/**
 *网路票据打印请求报文
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6020 {
    /*
    org	票据来源	是	1-	互联网票据 2-柜台缴费票据
    printType 打印方式	是	1-	按日期打印 2-	按票号打印
    ticketNo	罚单号		20
    billNo	票据号		20
//    startDate	开始日期
//    endDate	结束日期
*/
    @DataField(seq = 1)
    private String org;
    @DataField(seq = 2)
    private String printType;
    @DataField(seq = 3)
    private String ticketNo;
    @DataField(seq = 4)
    private String billNo;
//    @DataField(seq = 5)
//    private String startDate;
//    @DataField(seq = 6)
//    private String endDate;

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }


    @Override
    public String toString() {
        return "CbsTia6020{" +
                "org='" + org + '\'' +
                ", printType='" + printType + '\'' +
                ",ticketNo='" + ticketNo + '\'' +
                ", billNo='" + billNo + '\'' +
                '}';
    }
}