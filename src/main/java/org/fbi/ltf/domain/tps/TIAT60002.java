package org.fbi.ltf.domain.tps;

/**
 * Ʊ����ȡ�ش�����
 * billStart	Ʊ�ݱ�ſ�ʼ	��
 billEnd	Ʊ�ݱ�Ž���	��
 sheets	һ��Ʊ���ж�����Ʊ��	��
 billNum	Ʊ������	��
 */
public class TIAT60002 {
    private String billStart;
    private String billEnd;
    private String sheets;
    private String billNum;

    public String getBillStart() {
        return billStart;
    }

    public void setBillStart(String billStart) {
        this.billStart = billStart;
    }

    public String getBillEnd() {
        return billEnd;
    }

    public void setBillEnd(String billEnd) {
        this.billEnd = billEnd;
    }

    public String getSheets() {
        return sheets;
    }

    public void setSheets(String sheets) {
        this.sheets = sheets;
    }

    public String getBillNum() {
        return billNum;
    }

    public void setBillNum(String billNum) {
        this.billNum = billNum;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "   billStart'" + billStart + '\'' +
                ",  billEnd='" + billEnd + '\'' +
                ",  sheets='" + sheets + '\'' +
                ",  billNum='" + billNum + '\'' +
                '}';
    }

}
