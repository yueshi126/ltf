package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ��ѯ�ϴ�����
 * No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	bankCode	���б�ʶ���������ṩ��	��	20
 2	type	��ѯ����	��	1001 ����Ʊ
 3	node1	��ѯ�ֶ�1	��	128
 4	node2	��ѯ�ֶ�2	��	128
 5	node3	��ѯ�ֶ�3	��	128
 6	node4	��ѯ�ֶ�4	��	128
 */

public class TOAT60007 {
    private String bankCode;
    private String type;
    private String node1;
    private String node2;
    private String node3;
    private String node4;

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNode1() {
        return node1;
    }

    public void setNode1(String node1) {
        this.node1 = node1;
    }

    public String getNode2() {
        return node2;
    }

    public void setNode2(String node2) {
        this.node2 = node2;
    }

    public String getNode3() {
        return node3;
    }

    public void setNode3(String node3) {
        this.node3 = node3;
    }

    public String getNode4() {
        return node4;
    }

    public void setNode4(String node4) {
        this.node4 = node4;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  bankCode='" + bankCode + '\'' +
                ", type='" + type + '\'' +
                ", node1='" + node1 + '\'' +
                ", node2='" + node2 + '\'' +
                ", node3='" + node3 + '\'' +
                ", node4='" + node4 + '\'' +
                '}';
    }

}
