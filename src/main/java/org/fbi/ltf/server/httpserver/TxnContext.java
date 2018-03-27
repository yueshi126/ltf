package org.fbi.ltf.server.httpserver;

import org.fbi.ltf.domain.tps.TpsMsgResForLtf;
import org.fbi.ltf.domain.tps.TpsMsgResForLtf60002;
import org.fbi.ltf.helper.FbiBeanUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by zhanrui on 2014/11/6.
 */
public class TxnContext {
    private String msgtia;
    private String msgtoa;
    private String msgtoa60002;
    Map<String, List<String>> mapTia;

    public String getMsgtia() {
        return msgtia;
    }

    public void setMsgtia(String msgtia) {
        this.msgtia = msgtia;
    }

    public String getMsgtoa() {
        return msgtoa;
    }

    public void setMsgtoa(TpsMsgResForLtf msgtoa) {
        this.msgtoa = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(msgtoa));
    }

    public String getMsgtoa60002() {
        return msgtoa60002;
    }

    public void setMsgtoa60002(TpsMsgResForLtf60002 msgtoa60002) {
        this.msgtoa60002 = FbiBeanUtils.encode64(FbiBeanUtils.beanToJson(msgtoa60002));;
    }

    public Map<String, List<String>> getMapTia() {
        return mapTia;
    }

    public void setMapTia(Map<String, List<String>> mapTia) {
        this.mapTia = mapTia;
    }
}
