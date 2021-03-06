package org.fbi.ltf.processor;

import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6089Request.CbsTia6089;
import org.fbi.ltf.domain.cbs.T6089Response.CbsToa6089;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfOrgCompMapper;
import org.fbi.ltf.repository.dao.FsLtfVchOutMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询网上缴费信息查询
 * Created by Thinkpad on 2018/03/28
 */
public class T6089Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {
        String hostTxnsn = request.getHeader("serialNo");
        CbsTia6089 tia = new CbsTia6089();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6089) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6089");
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

    public CbsRtnInfo processTxn(CbsTia6089 tia, Stdp10ProcessorRequest request) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        List<FsLtfVchOut> infoList;
        FsLtfTicketInfo fsLtfTicketInfo = new FsLtfTicketInfo();
        String dept = request.getHeader("branchId");
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            if (!StringUtils.isEmpty(tia.getTicketNo())) {
                infoList = selectVchOut(tia);
                //本地处理
                //1、查看相对应的项目代码是否存在记录
                if (infoList.size() != 1) {
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("没有罚单信息");
                    return cbsRtnInfo;
                } else {
                    FsLtfOrgComp orgComp = selectOrg(infoList.get(0).getBankMare());
                    if (!orgComp.getDeptCode().equals(dept)) {
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("罚单属于" + orgComp.getOrgName());
                        return cbsRtnInfo;
                    }
                    String cbsRespMsg = generateCbsRespMsg(infoList.get(0));
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(cbsRespMsg);
                    return cbsRtnInfo;
                }
            } else {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("罚单号不能为空");
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
    private List<FsLtfVchOut> selectVchOut(CbsTia6089 tia) {
        List<FsLtfVchOut> infoList = new ArrayList<>();
        FsLtfVchOutMapper mapper = session.getMapper(FsLtfVchOutMapper.class);
        FsLtfVchOutExample example = new FsLtfVchOutExample();
        String tickNo = "";
        if (tia.getTicketNo().length() == 16) {
            tickNo = tia.getTicketNo().substring(0, 15);
        } else {
            tickNo = tia.getTicketNo();
        }
        example.createCriteria().andTicketNoEqualTo(tickNo);
        infoList = mapper.selectByExample(example);
        return infoList;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(FsLtfVchOut fsLtfVchOut) {
        CbsToa6089 cbsToa = new CbsToa6089();
        FbiBeanUtils.copyProperties(fsLtfVchOut, cbsToa);
        try {
            String cbsRespMsg = "";
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
            return cbsRespMsg;
        } catch (
                Exception e)

        {
            throw new RuntimeException("特色平台报文转换失败." + e.getMessage());
        }

    }

    private FsLtfOrgComp selectOrg(String orgCode) {
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if (orgCompList.size() > 0) {
            return orgCompList.get(0);
        } else {
            return null;
        }
    }

}
