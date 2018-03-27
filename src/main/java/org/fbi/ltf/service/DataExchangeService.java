package org.fbi.ltf.service;

import org.fbi.ltf.helper.ProjectConfigManager;
import org.fbi.ltf.helper.TpsSocketClientLtf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ZZP_YY on 2018-03-23.
 */
public class DataExchangeService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());


    //第三方服务处理：可根据交易号设置不同的超时时间
    public byte[] processThirdPartyServer(byte[] sendTpsBuf, String txnCode) throws Exception {
        String servIp = ProjectConfigManager.getInstance().getProperty("cbs.server.ip");
        int servPort = Integer.parseInt(ProjectConfigManager.getInstance().getProperty("cbs.server.port"));
        String timeoutCfg = ProjectConfigManager.getInstance().getProperty("cbs.server.timeout." + txnCode);
        if (timeoutCfg != null) {
        } else {
            timeoutCfg = ProjectConfigManager.getInstance().getProperty("cbs.server.timeout");
        }
        int timeout = Integer.parseInt(timeoutCfg);
        TpsSocketClientLtf client = new TpsSocketClientLtf(servIp, servPort,timeout);
        logger.info("请求特色 Request:" + new String(sendTpsBuf, "GBK"));
        byte[] rcvTpsBuf = client.call(sendTpsBuf);
        logger.info("特色返回 Response:" + new String(rcvTpsBuf, "GBK"));
        return rcvTpsBuf;
    }
}
