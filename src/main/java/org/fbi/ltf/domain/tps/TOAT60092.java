package org.fbi.ltf.domain.tps;

/**
 * ������Ϣ��ѯ�ϴ�����
 * No.	��������	��������	�Ƿ����	���ݳ��ȣ�<��
 1	len	 	���ĳ���
 2	txcode	������
 3	body

 */

public class TOAT60092 {
    private String len;
    private String txcode;
    private String body;


    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len;
    }

    public String getTxcode() {
        return txcode;
    }

    public void setTxcode(String txcode) {
        this.txcode = txcode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "MsgBody{" +
                "  len='" + len + '\'' +
                ", txcode='" + txcode + '\'' +
                ", body='" + body + '\'' +
                '}';
    }

}
