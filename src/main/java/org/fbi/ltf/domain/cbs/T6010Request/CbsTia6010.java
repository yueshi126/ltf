package org.fbi.ltf.domain.cbs.T6010Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

import java.math.BigDecimal;
import java.util.List;

/**
 * bankCode	银行标识（各银行提供）	是
 BankTake	柜台标识(各银行提供)	是
 orderNo	订单编号	是
 transTime	交易时间	是
 amount	交易金额	是
 overdueFine	滞纳金	是
 busCode	处理业务	是
 orderCharges	收费项目（多个用逗号隔开）	是
 accountNo	收款财政账户	否
 billNo	票据号码	是
 ticketAmount	处罚决定书编号	是
 ticketTime	处理时间	是
 ticketAmount	罚款金额	是
 payerName	付款人	是
 idCard	付款人身份证号	否
 phoneNo	付款人联系方式	否
 callDept	采集机关代码（到大队）	否
 handleDept	处罚机关代码（到大队）	是
 driveNo	当事人驾驶证编号	否
 runNo	当事人行驶证编号	否
 plateNo	车牌号码	否
 plateColor	车牌颜色	否
 carType	车辆类型	否
 carColor	车身颜色	否
 party	当事人姓名	是
 partyCard	当事人身份证号	否
 salesNo	业务员代码	否
 salesName	业务员姓名	否
 node	备注	否
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6010 {
    @DataField(seq = 1)
    private String accountNo;
    @DataField(seq = 2)
    private String ticketNo;
    @DataField(seq = 3)
    private String billNo;
    @DataField(seq = 4)
    private String orderNo;
    @DataField(seq = 5)
    private String amount;
    @DataField(seq = 6)
    private String overdueFine;
    @DataField(seq = 7)
    private String busCode;
    @DataField(seq = 8)
    private String ticketTime;
    @DataField(seq = 9)
    private String ticketAmount;
    @DataField(seq = 10)
    private String payerName;
    @DataField(seq = 11)
    private String idCard;
    @DataField(seq = 12)
    private String phoneNo;
    @DataField(seq = 13)
    private String callDept;
    @DataField(seq = 14)
    private String handleDept;
    @DataField(seq = 15)
    private String driveNo;
    @DataField(seq = 16)
    private String runNo;
    @DataField(seq = 17)
    private String plateNo;
    @DataField(seq = 18)
    private String plateColor;
    @DataField(seq = 19)
    private String carType;
    @DataField(seq = 20)
    private String carColor;
    @DataField(seq = 21)
    private String party;
    @DataField(seq = 22)
    private String partyCard;
    @DataField(seq = 23)
    private String salesNo;
    @DataField(seq = 24)
    private String salesName;
    @DataField(seq = 25)
    private String node;
    @DataField(seq = 26)
    private String areaCode;
    @DataField(seq = 27)
    private String payType;
    @DataField(seq = 28)
    private String itemNum;
    @DataField(seq = 29)
    @OneToMany(mappedTo = "org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item", totalNumberField = "itemNum")
    private java.util.List<CbsTia6010Item> items;

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public List<CbsTia6010Item> getItems() {
        return items;
    }

    public void setItems(List<CbsTia6010Item> items) {
        this.items = items;
    }

    public String getItemNum() {
        return itemNum;
    }

    public void setItemNum(String itemNum) {
        this.itemNum = itemNum;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    @Override
    public String toString() {
        return "CbsTia6010{" +
/*                "instCode='" + instCode + '\'' +
                ", inDate='" + inDate + '\'' +
                ", inAmount=" + inAmount +
                ", inName='" + inName + '\'' +
                ", inAcct='" + inAcct + '\'' +
                ", inMemo='" + inMemo + '\'' +*/
                '}';
    }
}