package org.fbi.ltf.domain.cbs.T6012Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by zzp on 18-03-05.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsToa6012 {
    /*
    1 pkid ����
2	orderNo	�������	��	35
3	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
4	ticketNo	������������	��	50
5	ticketTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
6	itemUnicode	�շ���Ŀ������ö��Ÿ�����	��	100
7	amount	���׽��	��	���999999.99
8	overdueFine	���ɽ�	��	���99999.99
9	ticketAmount	�������	��	���99999.99
10	billNo	Ʊ�ݱ��	��	12
11	node	��ע	��	128

     */

    @DataField(seq = 1)
    private String ticketNo;
    @DataField(seq = 2)
    private String driveNo;
    @DataField(seq = 3)
    private String dabh;
    @DataField(seq = 4)
    private String party;
    @DataField(seq = 5)
    private String plateType;
    @DataField(seq = 6)
    private String plateTypeName;
    @DataField(seq = 7)
    private String plateNo;
    @DataField(seq = 8)
    private String ticketTime;
    @DataField(seq = 9)
    private String illegalTime;
    @DataField(seq = 10)
    private String ticketAddr;
    @DataField(seq = 11)
    private String handleDept;
    @DataField(seq = 12)
    private String ticketAmount;
    @DataField(seq = 13)
    private String orderCharges;
    @DataField(seq = 14)
    private String orderChargesName;
    @DataField(seq = 15)
    private String node;


    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getDriveNo() {
        return driveNo;
    }

    public void setDriveNo(String driveNo) {
        this.driveNo = driveNo;
    }

    public String getDabh() {
        return dabh;
    }

    public void setDabh(String dabh) {
        this.dabh = dabh;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getPlateType() {
        return plateType;
    }

    public void setPlateType(String plateType) {
        this.plateType = plateType;
    }

    public String getPlateTypeName() {
        return plateTypeName;
    }

    public void setPlateTypeName(String plateTypeName) {
        this.plateTypeName = plateTypeName;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getIllegalTime() {
        return illegalTime;
    }

    public void setIllegalTime(String illegalTime) {
        this.illegalTime = illegalTime;
    }

    public String getTicketAddr() {
        return ticketAddr;
    }

    public void setTicketAddr(String ticketAddr) {
        this.ticketAddr = ticketAddr;
    }

    public String getHandleDept() {
        return handleDept;
    }

    public void setHandleDept(String handleDept) {
        this.handleDept = handleDept;
    }

    public String getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(String ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public String getOrderCharges() {
        return orderCharges;
    }

    public void setOrderCharges(String orderCharges) {
        this.orderCharges = orderCharges;
    }

    public String getOrderChargesName() {
        return orderChargesName;
    }

    public void setOrderChargesName(String orderChargesName) {
        this.orderChargesName = orderChargesName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "CbsToa6012{" +
                "   ticketNo='" + ticketNo + '\'' +
                ",  driveNo='" + driveNo + '\'' +
                ",  dabh='" + dabh + '\'' +
                ",  party='" + party + '\'' +
                ",  plateType='" + plateType + '\'' +
                ",  plateTypeName='" + plateTypeName + '\'' +
                ",  plateNo='" + plateNo + '\'' +
                ",  ticketTime='" + ticketTime + '\'' +
                ",  illegalTime='" + illegalTime + '\'' +
                ",  ticketAddr='" + ticketAddr + '\'' +
                ",  handleDept='" + handleDept + '\'' +
                ",  ticketAmount='" + ticketAmount + '\'' +
                ",  orderCharges='" + orderCharges + '\'' +
                ",  orderChargesName='" + orderChargesName + '\'' +
                ",  node='" + node + '\'' +
                '}';
    }
}
