package org.fbi.ltf.domain.tps;

/**
 * 2.3.15	��ͨΥ��������Ϣ��ѯ�ӿڻش�����
 1	ticketNo	������������	��	15
 2	driveNo	��ʻ֤���	��	18
 3	dabh	�������	��	12
 4	party	����������	��	50
 5	plateType	��������	��	2
 6	plateTypeName	������������	��	20
 7	plateNo	���ƺ���	��	15
 8	ticketTime	Υ��ʱ��	��	yyyy-MM-dd HH:mm:ss��ע���������ϱ��ӿڵ�ticketTime�ֶΣ�
 9	illegalTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 10	ticketAddr	Υ����ַ	��	128
 11	handleDept	������أ����жӣ�	��	30
 12	ticketAmount	�������λ��Ԫ��	��	6��Integer���ͣ�
 13	orderCharges	Υ������	��	10,Υ������
 14	orderChargesName	Υ����������	��	200
 15	node	��ע	��	128

 */
public class TIAT60012 {
    private String ticketNo;
    private String driveNo;
    private String dabh;
    private String party;
    private String plateType;
    private String plateTypeName;
    private String plateNo;
    private String ticketTime;
    private String illegalTime;
    private String ticketAddr;
    private String handleDept;
    private String ticketAmount;
    private String orderCharges;
    private String orderChargesName;
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
        return "TIAT60012{" +
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
