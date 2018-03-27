package org.fbi.ltf.domain.cbs.T6070Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;

/**
 * Created by zhanrui on 14-10-20.
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6070 {
    @DataField(seq = 1)
    private String bankCode;
    @DataField(seq = 2)
    private String type;
    @DataField(seq = 3)
    private String node1;
    @DataField(seq = 4)
    private String node2;
    @DataField(seq = 5)
    private String node3;
    @DataField(seq = 6)
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
        return "CbsTia6070{" +
                "bankCode='" + bankCode + '\'' +
                ",type='" + type + '\'' +
                ", node1='" + node1 + '\'' +
                ", node2='" + node2 + '\'' +
                ", node3='" + node3 + '\'' +
                ", node4='" + node4 + '\'' +
                '}';
    }
}
