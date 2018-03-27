package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ�ϴ�����
 * ��������	��������	�Ƿ����	���ݳ��ȣ�<��
 bankCode	���б�ʶ���������ṩ��	��	20
 BankTake	��̨��ʶ(�������ṩ)	��	33
 orderNo	�������	��	35
 transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 amount	���׽��	��	���999999.99
 overdueFine	���ɽ�	��	���99999.99
 busCode	����ҵ��	��	35
 orderCharges	�շ���Ŀ������ö��Ÿ�����	��	35
 accountNo	�տ�����˻�	��	33
 billNo	Ʊ�ݺ���	��	12
 ticketAmount	������������	��	50
 ticketTime	Υ��ʱ��	��	yyyy-MM-dd HH:mm:ss
 ticketAmount	������	��	���99999.99
 payerName	������	��	50
 idCard	���������֤��	��	20
 phoneNo	��������ϵ��ʽ	��	15
 callDept	�ɼ����ش��루����ӣ�	��	20
 handleDept	�������ش��루����ӣ�	��	20
 driveNo	�����˼�ʻ֤���	��	20
 runNo	��������ʻ֤���	��	20
 plateNo	���ƺ���	��	20
 plateColor	������ɫ	��	2
 carType	��������	��	2
 carColor	������ɫ	��	2
 party	����������	��	50
 partyCard	���������֤��	��	18
 salesNo	ҵ��Ա����	��	20
 salesName	ҵ��Ա����	��	50
 node	��ע	��	288
 */

public class TOAT60095 {
    /*
1	bankCode	���б�ʶ	��	20
2	orderNo	�������	��	35
3	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
4	orderDetail	��������	��	35
5	type	��¼����	��	1001:��¼�ɿ��û��˺�
6	node1	��¼�ֶ�1	��	35
7	node2	��¼�ֶ�2	��
8	node3	��¼�ֶ�3	��
9	node4	��¼�ֶ�4	��
   */
    private String bankCode;
    private String orderNo;
    private String transTime;
    private String orderDetail;
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

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
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
        return "CbsTia6095{" +
                "bankCode='" + bankCode + '\'' +
                "orderNo='" + orderNo + '\'' +
                "transTime='" + transTime + '\'' +
                "orderDetail='" + orderDetail + '\'' +
                "type='" + type + '\'' +
                "node1='" + node1 + '\'' +
                "node2='" + node2 + '\'' +
                "node3='" + node3 + '\'' +
                "node4='" + node4 + '\'' +
                '}';
    }

}
