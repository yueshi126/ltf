package org.fbi.ltf.domain.cbs.T6020Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;

import java.util.List;

/**
 *��·Ʊ�ݴ�ӡ������
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6020 {
    /*
    org	Ʊ����Դ	��	1-	������Ʊ�� 2-��̨�ɷ�Ʊ��
    printType ��ӡ��ʽ	��	1-	�����ڴ�ӡ 2-	��Ʊ�Ŵ�ӡ
    ticketNo	������		20
    billNo	Ʊ�ݺ�		20
//    startDate	��ʼ����
//    endDate	��������
*/
    @DataField(seq = 1)
    private String org;
    @DataField(seq = 2)
    private String printType;
    @DataField(seq = 3)
    private String ticketNo;
    @DataField(seq = 4)
    private String billNo;
//    @DataField(seq = 5)
//    private String startDate;
//    @DataField(seq = 6)
//    private String endDate;

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
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

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }


    @Override
    public String toString() {
        return "CbsTia6020{" +
                "org='" + org + '\'' +
                ", printType='" + printType + '\'' +
                ",ticketNo='" + ticketNo + '\'' +
                ", billNo='" + billNo + '\'' +
                '}';
    }
}