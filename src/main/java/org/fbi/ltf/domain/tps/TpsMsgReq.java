package org.fbi.ltf.domain.tps;

import org.fbi.ltf.helper.ProjectConfigManager;

import java.io.Serializable;

public class TpsMsgReq implements Serializable {
    private final String siteCode = ProjectConfigManager.getInstance().getProperty("tps.server.siteCode");
    private final String version = ProjectConfigManager.getInstance().getProperty("tps.server.version");
    private final String host = ProjectConfigManager.getInstance().getProperty("tps.server.host");
    private final String method = ProjectConfigManager.getInstance().getProperty("tps.server.method");
    private String reqdata = "";
    private String resdata = "";
    private String charsetName = "";
    private String uri="";

    public String getHost() {
        return host;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getCharsetName() {
        if("".equals(charsetName))
            charsetName="UTF-8";
        return charsetName;
    }

    public void setCharsetName(String charsetName) {
        this.charsetName = charsetName;
    }

    public String getSiteCode() {
        return siteCode;
    }


    public String getVersion() {
        return version;
    }



    public String getReqdata() {
        return reqdata;
    }

    public void setReqdata(String reqdata) {
        this.reqdata = reqdata;
    }

    public String getResdata() {
        return resdata;
    }

    public void setResdata(String resdata) {
        this.resdata = resdata;
    }

    public String getUri() {
        return uri;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "TpsMsgOutHead{" +
                "siteCode='" + siteCode + '\'' +
                ", version='" + version + '\'' +
                ", reqdata='" + reqdata + '\'' +
                '}';
    }
}
