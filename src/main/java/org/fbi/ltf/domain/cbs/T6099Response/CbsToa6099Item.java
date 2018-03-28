package org.fbi.ltf.domain.cbs.T6099Response;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToManySeperatedTextMessage;

/**
 * Created by ZZP_YY on 2018-03-28.
 * /**
 * Created by ZZP_YY on 2018-03-28.
 *  *
 *  txDate	��������	��	yyyyMMdd
 org	������Դ	��	(1-�������� 2-��������)
 entpNo	��ҵ��	��	10
 txAmt	���	��
 txFlag	�ɹ���־	��	(0000-�ɹ�,9999-ʧ��)
 rmrk	��ע	��	50
 cbsActSerial	������ˮ�ţ���ɫת��ʱ������	��	10
 */
@OneToManySeperatedTextMessage(separator = ",")
public class CbsToa6099Item {

    @DataField(seq = 1)
    private String txDate;
    @DataField(seq = 2)
    private String org;
    @DataField(seq = 3)
    private String entpNo;
    @DataField(seq = 4)
    private String txAmt;
    @DataField(seq = 5)
    private String txFlag;
    @DataField(seq = 6)
    private String rmrk;
    @DataField(seq = 7)
    private String cbsActSerial;
    @DataField(seq = 8)
    private String preActSerial;

    public String getTxDate() {
        return txDate;
    }

    public void setTxDate(String txDate) {
        this.txDate = txDate;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getEntpNo() {
        return entpNo;
    }

    public void setEntpNo(String entpNo) {
        this.entpNo = entpNo;
    }

    public String getTxAmt() {
        return txAmt;
    }

    public void setTxAmt(String txAmt) {
        this.txAmt = txAmt;
    }

    public String getTxFlag() {
        return txFlag;
    }

    public void setTxFlag(String txFlag) {
        this.txFlag = txFlag;
    }

    public String getRmrk() {
        return rmrk;
    }

    public void setRmrk(String rmrk) {
        this.rmrk = rmrk;
    }

    public String getCbsActSerial() {
        return cbsActSerial;
    }

    public void setCbsActSerial(String cbsActSerial) {
        this.cbsActSerial = cbsActSerial;
    }

    public String getPreActSerial() {
        return preActSerial;
    }

    public void setPreActSerial(String preActSerial) {
        this.preActSerial = preActSerial;
    }

    @Override
    public String toString() {
        return "CbsToa6099Item{" +
                "txDate='" + txDate + '\'' +
                ", org='" + org + '\'' +
                ", entpNo=" + entpNo +
                ", txAmt=" + txAmt +
                ", txFlag=" + txFlag +
                ", rmrk=" + rmrk +
                ", cbsActSerial=" + cbsActSerial +
                ", preActSerial=" + preActSerial +
                '}';
    }
}
