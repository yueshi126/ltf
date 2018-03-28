package org.fbi.ltf.domain.cbs.T6089Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;
import org.fbi.ltf.domain.cbs.T6098Response.CbsToa6098Item;

import java.util.List;

/**
 * Created by zzp on 18-03-21.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6089 {
    /*
transTime	交易时间		yyyy-MM-dd HH:mm:ss
payment	罚款金额		最大：99999.99
overdueFine	滞纳金		最大：99999.99
ticketNo	处罚决定书编号		50
billNo	票据号		20
ticketTime	违法时间		yyyy-MM-dd HH:mm:ss
driveNo	证件号码		20
payerName	付款人姓名		64
   */
    @DataField(seq = 1)
    private String transTime;
    @DataField(seq = 2)
    private String payment;
    @DataField(seq = 3)
    private String overdueFine;
    @DataField(seq = 4)
    private String ticketNo;
    @DataField(seq = 5)
    private String billNo;
    @DataField(seq = 6)
    private String ticketTime;
    @DataField(seq = 7)
    private String driveNo;
    @DataField(seq = 8)
    private String payerName;

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getPayment() {
        return payment;
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
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

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getDriveNo() {
        return driveNo;
    }

    public void setDriveNo(String driveNo) {
        this.driveNo = driveNo;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    @Override
    public String toString() {
        return "CbsToa6099{" +
                " transTime ='" + transTime  + '\'' +
                " payment ='" + payment  + '\'' +
                " overdueFine ='" + overdueFine  + '\'' +
                " ticketNo ='" + ticketNo  + '\'' +
                " billNo ='" + billNo  + '\'' +
                " ticketTime ='" + ticketTime  + '\'' +
                " driveNo ='" + driveNo  + '\'' +
                " payerName ='" + payerName  + '\'' +
                '}';
    }
}
