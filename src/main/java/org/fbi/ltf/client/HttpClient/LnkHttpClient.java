package org.fbi.ltf.client.HttpClient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.fbi.ltf.domain.tps.TpsMsgReq;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.ProjectConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: zhangxiaobo
 * Date: 11-12-30
 * Time: 下午2:24
 * To change this template use File | Settings | File Templates.
 */

public class LnkHttpClient {

    private static final Logger logger = LoggerFactory.getLogger(LnkHttpClient.class);
    String debug = ProjectConfigManager.getInstance().getProperty("debug");


    private String serverUrl;
    private HttpClient httpclient = null;
    private HttpPost httppost = null;

    private void init(String serverUrl) {
        this.serverUrl = serverUrl;
        try {
            httpclient = new DefaultHttpClient();
            //请求超时
            httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000 * 2);
            //读取超时
            httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 6000 * 5);
            httppost = new HttpPost(serverUrl);
            httppost.getURI();
        } catch (Exception e) {
            logger.error("初始化http网关错误!URL: " + serverUrl, e);
            httpclient.getConnectionManager().shutdown();
            throw new RuntimeException(e);
        }
    }
    private void init(String serverUrl,int timeOut) {
        this.serverUrl = serverUrl;
        try {
            httpclient = HttpClients.createDefault();
            httppost = new HttpPost(serverUrl);
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(timeOut).
                    setConnectionRequestTimeout(timeOut).
                    setSocketTimeout(timeOut).build();
            httppost.setConfig(requestConfig);
        } catch (Exception e) {
            logger.error("初始化http网关错误!URL: " + serverUrl, e);
            httpclient.getConnectionManager().shutdown();
            throw new RuntimeException(e);
        }
    }

    public String doPost( TpsMsgReq msgReq) {
        String responseBody = null;
        String errmsg = "";
        if(debug.equals("true")){
            logger.info("本地发送密文为："+msgReq.getReqdata());
            logger.info("本地发送明文为："+ FbiBeanUtils.decode64(msgReq.getReqdata()));
            responseBody="ewogICAgImNvZGUiOiIwMDAwIiwgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAiY29tbWVudCI6Iue7k+aenOeahOWkh+azqOivtOaYjiJ9Cg==";
            responseBody="ewogICAgImNvZGUiOiIwMDAwIiwgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAiY29tbWVudCI6Iue7k+aenOeahOWkh+azqOivtOaYjiJ9Cg==";
            msgReq.setResdata(responseBody);
            return responseBody;
        }
        try {
            init(msgReq.getUri());
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("siteCode", msgReq.getSiteCode()));
            formparams.add(new BasicNameValuePair("version", msgReq.getVersion()));
            formparams.add(new BasicNameValuePair("reqdata", msgReq.getReqdata()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            httppost.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
//            responseBody = httpclient.execute(httppost, responseHandler);
            logger.info("本地发送密文为："+msgReq.getReqdata());
            logger.info("本地发送明文为："+ FbiBeanUtils.decode64(msgReq.getReqdata()));
            HttpResponse httpResponse = httpclient.execute(httppost);
//            String charset = EntityUtils.getContentCharSet(httpResponse.getEntity());
            // 设置字符集，以防乱码
            responseBody = EntityUtils.toString(httpResponse.getEntity(), msgReq.getCharsetName());
            logger.info("本地接收响应密文为："+responseBody);
            logger.info("本地接收响应明文为："+FbiBeanUtils.decode64(responseBody));
            msgReq.setResdata(responseBody);

        } catch (Exception e) {
            errmsg = "Http接口的通讯错误!";
            logger.error(errmsg, e);
            throw new RuntimeException(errmsg, e);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        if (StringUtils.isEmpty(responseBody)) {
            throw new RuntimeException("通讯可能出现错误，返回报文为空！");
        } else {
            logger.info("HttpClient接收报文内容：" + responseBody);
        }
        return responseBody;
    }
    public String doPost( TpsMsgReq msgReq,int timeOut) {
        String responseBody = null;
        String errmsg = "";
        if(debug.equals("true")){
            logger.info("本地发送密文为："+msgReq.getReqdata());
            logger.info("本地发送明文为："+ FbiBeanUtils.decode64(msgReq.getReqdata()));
            responseBody="ewogICAgImNvZGUiOiIwMDAwIiwgICAgICAgICAgICAgICAgICAgICAgICAgCiAgICAiY29tbWVudCI6Iue7k+aenOeahOWkh+azqOivtOaYjiJ9Cg==";
            msgReq.setResdata(responseBody);
            return responseBody;
        }
        try {
            init(msgReq.getUri(),timeOut);
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("siteCode", msgReq.getSiteCode()));
            formparams.add(new BasicNameValuePair("version", msgReq.getVersion()));
            formparams.add(new BasicNameValuePair("reqdata", msgReq.getReqdata()));
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
            httppost.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
//            responseBody = httpclient.execute(httppost, responseHandler);
            logger.info("本地发送密文为："+msgReq.getReqdata());
            logger.info("本地发送明文为："+ FbiBeanUtils.decode64(msgReq.getReqdata()));
            HttpResponse httpResponse = httpclient.execute(httppost);
//            String charset = EntityUtils.getContentCharSet(httpResponse.getEntity());
            // 设置字符集，以防乱码
            responseBody = EntityUtils.toString(httpResponse.getEntity(), msgReq.getCharsetName());
            logger.info("本地接收响应密文为："+responseBody);
            logger.info("本地接收响应明文为："+FbiBeanUtils.decode64(responseBody));
            msgReq.setResdata(responseBody);

        } catch (Exception e) {
            errmsg = "Http接口的通讯错误!";
            logger.error(errmsg, e);
            throw new RuntimeException(errmsg, e);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        if (StringUtils.isEmpty(responseBody)) {
            throw new RuntimeException("通讯可能出现错误，返回报文为空！");
        } else {
            logger.info("HttpClient接收报文内容：" + responseBody);
        }
        return responseBody;
    }

    public static void main(String[] args){
        LnkHttpClient client = new LnkHttpClient();
        TpsMsgReq msgReq = new TpsMsgReq();
        msgReq.setReqdata("eyJiYW5rTWFyZSI6IjAzODgyMjciLCJiaWxsQ29kZSI6IjMwMDEiLCJkZXB0Ijoi5Lqk6K2m5pSv6YOoIiwiZHJpdmVObyI6IiIsIm5vZGUiOiIiLCJvcmRlckNoYXJnZXMiOiIxMDAxMSIsIm9yZGVyRGVhdGlsIjoiMDJmYjFkNjExYzJjNDk5MDg5YTJlMzkwMTcwZDM2YTIiLCJvcmRlck5vIjoiMDAwMDAwMjAxNTExMjAwNyIsIm92ZXJkdWVGaW5lIjowLCJwYXllck5hbWUiOiLkuZTmmZMiLCJwYXltZW50IjowLjAxLCJ0aWNrZXRDb2RlIjoiMTEiLCJ0aWNrZXRObyI6IjEwMDIwMDMwMDQwMDUwMDAwMSIsInRpY2tldFRpbWUiOiIyMDE1LTExLTIwIDAwOjAwOjAwIiwidHJhbnNUaW1lIjoiMjAxNS0xMS0yMCAxMDozNTo0OCJ9");
        msgReq.setUri("http://127.0.0.1:8080/processor/T60001");
        client.doPost(msgReq);
    }
}
