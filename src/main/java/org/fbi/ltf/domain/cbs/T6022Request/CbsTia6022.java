package org.fbi.ltf.domain.cbs.T6022Request;

import org.fbi.linking.codec.dataformat.annotation.DataField;
import org.fbi.linking.codec.dataformat.annotation.OneToMany;
import org.fbi.linking.codec.dataformat.annotation.SeperatedTextMessage;
import org.fbi.ltf.domain.cbs.T6010Request.CbsTia6010Item;

import java.util.List;

/**
 *网路票据打印请求报文
 *begNo 起始票号
 * endNo 终止票号
 * print    打印机类型
 * printType  打印方式  1- 套打  2- 票据号
 * org   来源
 */
@SeperatedTextMessage(separator = "\\|", mainClass = true)
public class CbsTia6022 {

    @DataField(seq = 1)
    private String begNo;
    @DataField(seq = 2)
    private String endNo;
    @DataField(seq = 3)
    private String print;
    @DataField(seq = 4)
    private String printType;
    @DataField(seq = 5)
    private String org;


    public String getBegNo() {
        return begNo;
    }

    public void setBegNo(String begNo) {
        this.begNo = begNo;
    }

    public String getEndNo() {
        return endNo;
    }

    public void setEndNo(String endNo) {
        this.endNo = endNo;
    }

    public String getPrint() {
        return print;
    }

    public void setPrint(String print) {
        this.print = print;
    }

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    @Override
    public String toString() {
        return "CbsTia6020{" +
                "begNo='" + begNo + '\'' +
                ", endNo='" + endNo + '\'' +
                ", printType='" + printType + '\'' +
                ", print='" + print + '\'' +
                ", org='" + org + '\'' +
                '}';
    }
}