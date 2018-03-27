package org.fbi.ltf.domain.tps;

/**
 * Ʊ����ȡ�ϴ�����
 * ��������	��������	�Ƿ����	���ݳ��ȣ�<��
 bankCode	���б�ʶ���������ṩ��	��	20
 busCode	ҵ�����(1������Ʊ�� 2��̨�ɷ�Ʊ��)	��	3
 billCode	Ʊ������	��	4
 billBatch	��Ʊ�루�������ṩ��ÿ�ε�����Ψһ���ظ����ظ��᷵�ص�һ�λ�ȡʱ���أ�	��	20
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
