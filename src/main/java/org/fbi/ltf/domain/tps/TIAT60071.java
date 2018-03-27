package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ��ѯ�ش����� �ۺ�Ӧ��ƽ̨���˽��
 No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	orderNo	�������	��	35
 2	orderDetail	��������	��	35
 3	ticketNo	������������	��	35
 4	ledgerType	��������	��	1 �Զ����� 2�˹�����
 5	ledgerState	����״̬	��	1 ���˳ɹ�
                             2 ����ʧ�ܣ��ظ����ˣ�
                             3 ����ʧ�ܣ��޴˴��������飩
                             4 ����ʧ�ܣ�������㣩
                             5 ����ʧ�ܣ������������ޣ�
                             6 ����ʧ�ܣ�����ȷ��
                             7 ����ʧ�ܣ����ɽ��㣩
                             8 ���˳ɹ�����48Сʱ��¼�룩
                             9����ʧ�ܣ�����ƽ̨��¼�룩
                             10����ʧ�ܣ�����ƽ̨�ظ��ɷѣ�
 6	ledgerTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 7	node	��ע	��	128

 */
public class TIAT60071 {
    private String orderNo;
    private String orderDetail;
    private String ticketNo;
    private String ledgerType;
    private String ledgerState;
    private String ledgerTime;
    private String node;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDetail() {
        return orderDetail;
    }

    public void setOrderDetail(String orderDetail) {
        this.orderDetail = orderDetail;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getLedgerType() {
        return ledgerType;
    }

    public void setLedgerType(String ledgerType) {
        this.ledgerType = ledgerType;
    }

    public String getLedgerState() {
        return ledgerState;
    }

    public void setLedgerState(String ledgerState) {
        this.ledgerState = ledgerState;
    }

    public String getLedgerTime() {
        return ledgerTime;
    }

    public void setLedgerTime(String ledgerTime) {
        this.ledgerTime = ledgerTime;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "TIAT60071{" +
                "   orderNo='" + orderNo + '\'' +
                ",  orderDetail='" + orderDetail + '\'' +
                ",  ticketNo='" + ticketNo + '\'' +
                ",  ledgerType='" + ledgerType + '\'' +
                ",  ledgerState='" + ledgerState + '\'' +
                ",  ledgerTime='" + ledgerTime + '\'' +
                ",  node='" + node + '\'' +
                '}';
    }

}
