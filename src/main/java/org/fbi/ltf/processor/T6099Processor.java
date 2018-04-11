package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6099Request.CbsTia6099;
import org.fbi.ltf.domain.cbs.T6099Response.CbsToa6099;
import org.fbi.ltf.domain.cbs.T6099Response.CbsToa6099Item;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfAcctDealMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询网上交款信息
 * Created by Thinkpad on 2018/03/21
 */
public class T6099Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");
        CbsTia6099 tia = new CbsTia6099();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6099) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6099");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
//            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }


    }

    public CbsRtnInfo processTxn(CbsTia6099 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        List<FsLtfAcctDeal> infoList;
        String dept = request.getHeader("branchId");
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            if (!StringUtils.isEmpty(tia.getBegTransTime()) && !StringUtils.isEmpty(tia.getEndTransTime())) {
                infoList = selectAcctDeal(tia);
                //本地处理
                //1、查看相对应的项目代码是否存在记录
                if (infoList.size() > 0) {
                    String cbsRespMsg = generateCbsRespMsg(infoList);
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(cbsRespMsg);
                    session.commit();
                    return cbsRtnInfo;
                } else {

                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("没有清算记录");
                    return cbsRtnInfo;
                }
            } else {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("查询日期不能为空");
                return cbsRtnInfo;
            }
        } catch (Exception e) {
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle());
            logger.info("6096交易处理异常" + e.getMessage().toString());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // 根据pkid 查罚单信息
    private List<FsLtfAcctDeal> selectAcctDeal(CbsTia6099 tia) {
        List<FsLtfAcctDeal> infoList = new ArrayList<>();
        FsLtfAcctDealMapper mapper = session.getMapper(FsLtfAcctDealMapper.class);
        FsLtfAcctDealExample example = new FsLtfAcctDealExample();

        example.createCriteria().andTxDateBetween(tia.getBegTransTime(), tia.getEndTransTime());
        infoList = mapper.selectByExample(example);
        return infoList;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(List<FsLtfAcctDeal> fsLtfAcctDeals) {
        CbsToa6099 cbsToa = new CbsToa6099();
        List<CbsToa6099Item> cbsToaItems = new ArrayList<>();
        for (FsLtfAcctDeal fsLtfAcctDeal : fsLtfAcctDeals) {
            CbsToa6099Item cbsToaItem = new CbsToa6099Item();
            FbiBeanUtils.copyProperties(fsLtfAcctDeal, cbsToaItem);
            cbsToaItems.add(cbsToaItem);
        }
        cbsToa.setItemNum("" + cbsToaItems.size());
        cbsToa.setItems(cbsToaItems);
        try {
            String cbsRespMsg = "";
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
            return cbsRespMsg;
        } catch (Exception e) {
            throw new RuntimeException("特色平台报文转换失败." + e.getMessage());
        }

    }

}
