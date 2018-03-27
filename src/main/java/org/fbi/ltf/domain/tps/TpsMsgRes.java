package org.fbi.ltf.domain.tps;

import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
public class TpsMsgRes implements Serializable {
    private String code = "";
    private String comment = "";
    private JSONArray reqdata = new JSONArray();

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public JSONArray getReqdata() {
        return reqdata;
    }

    public void setReqdata(JSONArray reqdata) {
        this.reqdata = reqdata;
    }

    @Override
    public String toString() {
        return "TpsMsgOutHead{" +
                "code='" + code + '\'' +
                ", comment='" + comment + '\'' +
                ", reqdata='" + reqdata + '\'' +
                '}';
    }
}
