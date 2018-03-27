package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10Processor;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: zhanrui
 * Date: 2014-10-19
 */

public abstract class AbstractTxnProcessor extends Stdp10Processor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void service(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String txnCode = request.getHeader("txnCode");
        String tellerId = request.getHeader("tellerId");
        String hostTxnsn = request.getHeader("serialNo");

        if (StringUtils.isEmpty(tellerId)) {
            tellerId = "TELLERID";
        }

        try {
            MDC.put("txnCode", txnCode);
            MDC.put("tellerId", tellerId);
            logger.info("CBS Request:" + "[sn=" + hostTxnsn + "]\n" + request.toString());
            doRequest(request, response);
            logger.info("CBS Response:" + "[sn=" + hostTxnsn + "]\n" + response.toString());
//        }catch (Exception e){
//            response.setHeader("rtnCode", TxnRtnCode.UNKNOWN_EXCEPTION.getCode());
//            throw new RuntimeException(e);
        } finally {
            MDC.remove("txnCode");
            MDC.remove("tellerId");
        }
    }

    abstract protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException;


    //生成特色平台响应报文
    protected <T> String marshalCbsResponseMsg(T cbsToa) {
        String cbsRespMsg = "";
        Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
        modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
        SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
        try {
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败.", e);
        }
        return cbsRespMsg;
    }

}
