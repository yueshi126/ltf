package org.fbi.ltf.domain.tps;

/**
 * 交款信息上传报文
 * 参数名称	参数解释	是否必填	数据长度（<）
 bankCode	银行标识（各银行提供）	是	20
 BankTake	柜台标识(各银行提供)	是	33
 orderNo	订单编号	是	35
 transTime	交易时间	是	yyyy-MM-dd HH:mm:ss
 amount	交易金额	是	最大：999999.99
 overdueFine	滞纳金	是	最大：99999.99
 busCode	处理业务	是	35
 orderCharges	收费项目（多个用逗号隔开）	是	35
 accountNo	收款财政账户	否	33
 billNo	票据号码	是	12
 ticketAmount	处罚决定书编号	是	50
 ticketTime	违章时间	是	yyyy-MM-dd HH:mm:ss
 ticketAmount	罚款金额	是	最大：99999.99
 payerName	付款人	是	50
 idCard	付款人身份证号	否	20
 phoneNo	付款人联系方式	否	15
 callDept	采集机关代码（到大队）	否	20
 handleDept	处罚机关代码（到大队）	是	20
 driveNo	当事人驾驶证编号	否	20
 runNo	当事人行驶证编号	否	20
 plateNo	车牌号码	否	20
 plateColor	车牌颜色	否	2
 carType	车辆类型	否	2
 carColor	车身颜色	否	2
 party	当事人姓名	是	50
 partyCard	当事人身份证号	否	18
 salesNo	业务员代码	否	20
 salesName	业务员姓名	否	50
 node	备注	否	288
 */

public class TOAT60003 {
    private String bankCode;
    private String bankTake;
    private String orderNo;
    private String transTime;
    private String amount;
    private String overdueFine;
    private String busCode;
    private String orderCharges;
    private String accountNo;
    private String billNo;
    private String ticketNo;
    private String ticketTime;
    private String ticketAmount;
    private String payerName;
    private String idCard;
    private String phoneNo;
    private String callDept;
    private String handleDept;
    private String driveNo;
    private String runNo;
    private String plateNo;
    private String plateColor;
    private String carType;
    private String carColor;
    private String party;
    private String partyCard;
    private String salesNo;
    private String salesName;
    private String node;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankTake() {
        return bankTake;
    }

    public void setBankTake(String bankTake) {
        this.bankTake = bankTake;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public String getOverdueFine() {
        return overdueFine;
    }

    public void setOverdueFine(String overdueFine) {
        this.overdueFine = overdueFine;
    }

    public String getBusCode() {
        return busCode;
    }

    public void setBusCode(String busCode) {
        this.busCode = busCode;
    }

    public String getOrderCharges() {
        return orderCharges;
    }

    public void setOrderCharges(String orderCharges) {
        this.orderCharges = orderCharges;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
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

    public String getTicketTime() {
        return ticketTime;
    }

    public void setTicketTime(String ticketTime) {
        this.ticketTime = ticketTime;
    }

    public String getTicketAmount() {
        return ticketAmount;
    }

    public void setTicketAmount(String ticketAmount) {
        this.ticketAmount = ticketAmount;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getCallDept() {
        return callDept;
    }

    public void setCallDept(String callDept) {
        this.callDept = callDept;
    }

    public String getHandleDept() {
        return handleDept;
    }

    public void setHandleDept(String handleDept) {
        this.handleDept = handleDept;
    }

    public String getDriveNo() {
        return driveNo;
    }

    public void setDriveNo(String driveNo) {
        this.driveNo = driveNo;
    }

    public String getRunNo() {
        return runNo;
    }

    public void setRunNo(String runNo) {
        this.runNo = runNo;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public String getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(String plateColor) {
        this.plateColor = plateColor;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getPartyCard() {
        return partyCard;
    }

    public void setPartyCard(String partyCard) {
        this.partyCard = partyCard;
    }

    public String getSalesNo() {
        return salesNo;
    }

    public void setSalesNo(String salesNo) {
        this.salesNo = salesNo;
    }

    public String getSalesName() {
        return salesName;
    }

    public void setSalesName(String salesName) {
        this.salesName = salesName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  bankCode='" + bankCode + '\'' +
                ", bankTake='" + bankTake + '\'' +
                ",  orderNo='" + orderNo + '\'' +
                ", transTime='" + transTime + '\'' +
                ",  amount='" + amount + '\'' +
                ", overdueFine='" + overdueFine + '\'' +
                ",  busCode='" + busCode + '\'' +
                ", orderCharges='" + orderCharges + '\'' +
                ",  accountNo='" + accountNo + '\'' +
                ", billNo='" + billNo + '\'' +
                ",  ticketNo='" + ticketNo + '\'' +
                ", ticketTime='" + ticketTime + '\'' +
                ",  ticketAmount='" + ticketAmount + '\'' +
                ", payerName='" + payerName + '\'' +
                ",  idCard='" + idCard + '\'' +
                ", phoneNo='" + phoneNo + '\'' +
                ",  callDept='" + callDept + '\'' +
                ", handleDept='" + handleDept + '\'' +
                ",  driveNo='" + driveNo + '\'' +
                ", runNo='" + runNo + '\'' +
                ",  plateNo='" + plateNo + '\'' +
                ", plateColor='" + plateColor + '\'' +
                ",  carType='" + carType + '\'' +
                ", carColor='" + carColor + '\'' +
                ",  party='" + party + '\'' +
                ", partyCard='" + partyCard + '\'' +
                ",  salesNo='" + salesNo + '\'' +
                ", salesName='" + salesName + '\'' +
                ",  node='" + node + '\'' +
                '}';
    }

}
