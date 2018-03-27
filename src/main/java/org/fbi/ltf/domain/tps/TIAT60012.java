package org.fbi.ltf.domain.tps;

/**
 * 2.3.15	交通违法罚单信息查询接口回传报文
 1	ticketNo	处罚决定书编号	是	15
 2	driveNo	驾驶证编号	否	18
 3	dabh	档案编号	否	12
 4	party	当事人姓名	否	50
 5	plateType	号牌种类	否	2
 6	plateTypeName	号码种类名称	否	20
 7	plateNo	号牌号码	否	15
 8	ticketTime	违法时间	否	yyyy-MM-dd HH:mm:ss（注意区分与上报接口的ticketTime字段）
 9	illegalTime	处理时间	否	yyyy-MM-dd HH:mm:ss
 10	ticketAddr	违法地址	否	128
 11	handleDept	处理机关（到中队）	否	30
 12	ticketAmount	罚款金额（单位：元）	否	6（Integer类型）
 13	orderCharges	违法代码	否	10,违法代码
 14	orderChargesName	违法代码名称	否	200
 15	node	备注	否	128

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
