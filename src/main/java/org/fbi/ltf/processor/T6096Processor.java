package org.fbi.ltf.processor;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.linking.codec.dataformat.SeperatedTextDataFormat;
import org.fbi.linking.processor.ProcessorException;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorResponse;
import org.fbi.ltf.domain.cbs.T6096Request.CbsTia6096;
import org.fbi.ltf.domain.cbs.T6096Response.CbsToa6096;
import org.fbi.ltf.domain.cbs.T6096Response.CbsToa6096Item;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.FbiBeanUtils;
import org.fbi.ltf.helper.LTFTools;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfTicketInfoMapper;
import org.fbi.ltf.repository.dao.FsLtfTicketItemMapper;
import org.fbi.ltf.repository.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * 2.3.14	柜台缴费系统勘误接口 查询
 * Created by Thinkpad on 2018/02/28
 */
public class T6096Processor extends AbstractTxnProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSession session = null;

    @Override
    protected void doRequest(Stdp10ProcessorRequest request, Stdp10ProcessorResponse response) throws ProcessorException, IOException {

        String hostTxnsn = request.getHeader("serialNo");

        CbsTia6096 tia = new CbsTia6096();
        try {
            SeperatedTextDataFormat dataFormat = new SeperatedTextDataFormat(tia.getClass().getPackage().getName());
            tia = (CbsTia6096) dataFormat.fromMessage(new String(request.getRequestBody(), "GBK"), "CbsTia6096");
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "特色业务平台请求报文解析错误.", e);
            throw new RuntimeException(e);
        }
        //业务逻辑处理
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia);
//            //特色平台响应
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "交易处理异常.", e);
            throw new RuntimeException("交易处理异常", e);
        }
    }

    public CbsRtnInfo processTxn(CbsTia6096 tia) throws Exception {
        CbsRtnInfo cbsRtnInfo = new CbsRtnInfo();
        List<FsLtfTicketInfo> infoList;
        List<FsLtfTicketItem> itemList;
        String starringRespMsg = "";
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            infoList = selectTicketInfo(tia);

            //本地处理
            //1、查看相对应的项目代码是否存在记录
            if (infoList.size() != 1) {
                session.rollback();
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("罚单号不存在");
                return cbsRtnInfo;
            } else {
                itemList = selectTicketItem(tia, infoList.get(0).getPkid());
                starringRespMsg = generateCbsRespMsg(infoList.get(0), itemList);
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                cbsRtnInfo.setRtnMsg(starringRespMsg);
            }
            return cbsRtnInfo;

        } catch (Exception e) {
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg("数据库处理异常");
            logger.info("6096 交易处理 异常" + e.getMessage().toString());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // 获取为对账成功的罚单信息
    private List<FsLtfTicketInfo> selectTicketInfo(CbsTia6096 tia) {
        FsLtfTicketInfoExample example = new FsLtfTicketInfoExample();
        List<String> qdfChkFlagList = new ArrayList<String>();
        qdfChkFlagList.add("1");
        qdfChkFlagList.add("8");
        FsLtfTicketInfoExample.Criteria criteria1 = example.createCriteria();
        criteria1.andTicketNoEqualTo(tia.getTicketNo()).andHostBookFlagEqualTo("1").andQdfChkFlagNotIn(qdfChkFlagList);
        FsLtfTicketInfoExample.Criteria criteria2 = example.createCriteria();
        criteria2.andTicketNoEqualTo(tia.getTicketNo()).andHostBookFlagEqualTo("1").andQdfBookFlagIsNull();
        example.or(criteria2);
        FsLtfTicketInfoMapper mapper = session.getMapper(FsLtfTicketInfoMapper.class);
        List<FsLtfTicketInfo> infoList = mapper.selectByExample(example);
        return infoList;
    }

    // 获取为对账成功的罚单信息
    private List<FsLtfTicketItem> selectTicketItem(CbsTia6096 tia, String infoId) {
        FsLtfTicketItemExample example = new FsLtfTicketItemExample();
        FsLtfTicketItemExample.Criteria criteria1 = example.createCriteria();
        criteria1.andTicketNoEqualTo(tia.getTicketNo()).andInfoIdEqualTo(infoId);
        FsLtfTicketItemMapper mapper = session.getMapper(FsLtfTicketItemMapper.class);
        List<FsLtfTicketItem> infoList = mapper.selectByExample(example);
        return infoList;
    }

    //生成CBS响应报文
    private String generateCbsRespMsg(FsLtfTicketInfo fsLtfTicketInfo, List<FsLtfTicketItem> items) {
        CbsToa6096 cbsToa = new CbsToa6096();
        fsLtfTicketInfo.setTicketTime(LTFTools.replceDate(fsLtfTicketInfo.getTicketTime()));
        fsLtfTicketInfo.setTransTime(LTFTools.replceDate(fsLtfTicketInfo.getTransTime()));
        FbiBeanUtils.copyProperties(fsLtfTicketInfo, cbsToa);
        List<CbsToa6096Item> itemList = new ArrayList<CbsToa6096Item>();
        //去掉3702
        for (int i = 0; i < items.size(); i++) {
            CbsToa6096Item cbsToaIem = new CbsToa6096Item();
            items.get(i).setItemCode(items.get(i).getItemCode().substring(4));
            cbsToaIem.setItemCode(items.get(i).getItemCode());
            cbsToaIem.setItemName(items.get(i).getItemName());
            cbsToaIem.setAmount(String.valueOf(items.get(i).getAmount()));
            itemList.add(cbsToaIem);
        }
        // 明细
        cbsToa.setItemNum(String.valueOf(items.size()));
        cbsToa.setItems(itemList);
        String cbsRespMsg = "";
        try {
            Map<String, Object> modelObjectsMap = new HashMap<String, Object>();
            modelObjectsMap.put(cbsToa.getClass().getName(), cbsToa);
            SeperatedTextDataFormat cbsDataFormat = new SeperatedTextDataFormat(cbsToa.getClass().getPackage().getName());
            cbsRespMsg = (String) cbsDataFormat.toMessage(modelObjectsMap);
        } catch (Exception e) {
            throw new RuntimeException("6096组织返回报文出错.");
        }
        return cbsRespMsg;
    }

}
