package org.fbi.ltf.domain.tps;

/**
 * ��Ʊ��Ϣ�ϴ�����
 * No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	bankCode	���б�ʶ���������ṩ��	��	20
 2	busCode	ҵ�����(1������Ʊ�� 2��̨�ɷ�Ʊ��)	��	3
 3	billNo	Ʊ�ݱ��	��	20
 4	billMoney	Ʊ�����	��	3������Ʊ�Ż�ʹ�ø��ֶΣ�
 5	applyTime	��Ʊʱ��	��	yyyy-MM-dd HH:mm:ss
 6	applyCause	��Ʊ����	��	200
 7	applyName	��Ʊ������	��	50�����ε���ʱʹ�ã���Ϊϵͳʹ�ã�����д�������ƣ�
 8	Node	��ע	��	128����Ʊ�ݱ�ţ�
 */

public class TOAT60006 {
    private String bankCode;
    private String busCode;
    private String billNo;
    private String billMoney;
    private String applyTime;
    private String applyCause;
    private String applyName;
    private String node;

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

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyCause() {
        return applyCause;
    }

    public void setApplyCause(String applyCause) {
        this.applyCause = applyCause;
    }

    public String getApplyName() {
        return applyName;
    }

    public void setApplyName(String applyName) {
        this.applyName = applyName;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getBillMoney() {
        return billMoney;
    }

    public void setBillMoney(String billMoney) {
        this.billMoney = billMoney;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  bankCode='" + bankCode + '\'' +
                ", busCode='" + busCode + '\'' +
                ", billNo='" + billNo + '\'' +
                ", billMoney='" + billMoney + '\'' +
                ", applyTime='" + applyTime + '\'' +
                ", applyCause='" + applyCause + '\'' +
                ", applyName='" + applyName + '\'' +
                ", node='" + node + '\'' +
                '}';
    }

}
