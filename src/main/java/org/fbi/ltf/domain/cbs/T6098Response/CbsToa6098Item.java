package org.fbi.ltf.domain.cbs.T6098Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zzp on 18-03-07.
 */
@OneToManySeperatedTextMessage(separator = "|")
public class CbsToa6098Item {

    /*
    ticketNo	������������	��	50
	billNo	Ʊ�ݺ���	��	12
	transTime	��������	��	yyyy-MM-dd HH:mm:ss
	amount	���׽��	��	���99999.99
	ticketAmount	������	��	���99999.99
	overdueFine	���ɽ�	��	���99999.99
	phoneNo	��ϵ�绰	��	20
	partyCard	���������֤��	��	18
	orderCharges	�շѴ���	��	100 ������Ÿ���
	qdfChkFlag	���˽��	��	30
     */
    @DataField(seq = 1)
    private String ticketNo;
    @DataField(seq = 2)
    private String billNo;
    @DataField(seq = 3)
    private String transTime;
    @DataField(seq = 4)
    private String amount;
    @DataField(seq = 5)
    private String ticketAmount;
    @DataField(seq = 6)
    private String overdueFine;
    @DataField(seq = 7)
    private String phoneNo;
    @DataField(seq = 8)
    private String partyCard;
    @DataField(seq = 9)
    private String orderCharges;
    @DataField(seq = 10)
    private String qdfChkFlag;

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

    public String getTransTime() {
        return transTime;
    }

    public void setTransTime(String transTime) {
        this.transTime = transTime;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(String ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getPartyCard() {
        return partyCard;
    }

    public void setPartyCard(String partyCard) {
        this.partyCard = partyCard;
    }

    public String getOrderCharges() {
        return orderCharges;
    }

    public void setOrderCharges(String orderCharges) {
        this.orderCharges = orderCharges;
    }

    public String getQdfChkFlag() {
        return qdfChkFlag;
    }

    public void setQdfChkFlag(String qdfChkFlag) {
        this.qdfChkFlag = qdfChkFlag;
    }

    @Override
    public String toString() {
        return "CbsToa6098Item{" +
                " ticketNo='" + ticketNo + '\'' +
                " billNo='" + billNo + '\'' +
                " transTime='" + transTime + '\'' +
                " amount='" + amount + '\'' +
                " ticketAmount='" + ticketAmount + '\'' +
                " overdueFine='" + overdueFine + '\'' +
                " phoneNo='" + phoneNo + '\'' +
                " partyCard='" + partyCard + '\'' +
                " orderCharges='" + orderCharges + '\'' +
                " qdfChkFlag='" + qdfChkFlag + '\'' +
                '}';
    }
}
