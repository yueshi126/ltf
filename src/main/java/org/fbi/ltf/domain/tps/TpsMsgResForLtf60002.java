package org.fbi.ltf.domain.tps;

import net.sf.json.JSONObject;

import java.io.Serializable;

public class TpsMsgResForLtf60002 implements Serializable {
    private String code = "";
    private String comment = "";

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


    @Override
    public String toString() {
        return "TpsMsgOutHead{" +
                "code='" + code + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
