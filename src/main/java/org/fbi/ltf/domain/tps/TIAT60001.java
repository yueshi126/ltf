package org.fbi.ltf.domain.tps;

import org.fbi.ltf.repository.model.FsLtfVchOut;

/**  Ʊ�ݷ���ش�����
 * 1	orderNo	�������	��	35
 2	transTime	����ʱ��	��	yyyy-MM-dd HH:mm:ss
 3	orderDetail	��������	��	33
 4	orderCharges	�շ���Ŀ������ö��Ÿ�����	��	35
 5	bankMare	��Ʊ�ص��ʶ���������ṩ��	��	20
 6	payment	�շѽ��	��	���99999.99
 7	ticketNo	������������	��	50
 8	ticketCode	Υ�´��루����ö��Ÿ�����	��	35
 9	ticketTime	Υ��ʱ��	��	yyyy-MM-dd HH:mm:ss
 10	driveNo	֤������	��	20
 11	payerName	����������	��	64
 12	dept	���ش���	��	20
 */
public class TIAT60001  extends FsLtfVchOut{

    @Override
    public String toString() {
        return "MsgBody{" +
                "   orderNo='" + super.getOrderNo() + '\'' +
                ",  transTime='" + super.getTransTime() + '\'' +
                ",  orderDetail='" + super.getOrderDetail() + '\'' +
                ",  orderCharges='" + super.getOrderCharges() + '\'' +
                ",  bankMare='" + super.getBankMare() + '\'' +
                ",  payment='" + super.getPayment() + '\'' +
                ",  ticketNo='" + super.getTicketNo() + '\'' +
                ",  ticketCode='" + super.getTicketCode() + '\'' +
                ",  ticketTime='" + super.getTicketTime() + '\'' +
                ",  driveNo='" + super.getDriveNo() + '\'' +
                ",  payerName='" + super.getPayerName() + '\'' +
                ",  dept='" + super.getDept() + '\'' +
                '}';
    }

}
