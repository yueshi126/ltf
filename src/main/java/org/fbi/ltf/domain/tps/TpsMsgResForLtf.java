package org.fbi.ltf.domain.tps;

import net.sf.json.JSONObject;

import java.io.Serializable;

public class TpsMsgResForLtf implements Serializable {
    private String code = "";
    private String comment = "";
    private JSONObject reqdata = new JSONObject();

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

    public JSONObject getReqdata() {
        return reqdata;
    }

    public void setReqdata(JSONObject reqdata) {
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
