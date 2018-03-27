package org.fbi.ltf.domain.tps;

/**
 * 订单信息查询上传报文
 * No.	参数名称	参数解释	是否必填	数据长度（<）
 1	len	 	报文长度
 2	txcode	交易码
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
