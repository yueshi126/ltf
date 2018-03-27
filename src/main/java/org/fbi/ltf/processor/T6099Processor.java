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
 * ��ѯ���Ͻ�����Ϣ
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
            logger.error("[sn=" + hostTxnsn + "] " + "��ɫҵ��ƽ̨�����Ľ�������.", e);
            throw new RuntimeException(e);
        }
        //ҵ���߼�����
        CbsRtnInfo cbsRtnInfo = null;
        try {
            cbsRtnInfo = processTxn(tia, request);
//            //��ɫƽ̨��Ӧ
            response.setHeader("rtnCode", cbsRtnInfo.getRtnCode().getCode());
            String cbsRespMsg = cbsRtnInfo.getRtnMsg();
            response.setResponseBody(cbsRespMsg.getBytes(response.getCharacterEncoding()));
        } catch (Exception e) {
            logger.error("[sn=" + hostTxnsn + "] " + "���״����쳣.", e);
            throw new RuntimeException("���״����쳣", e);
        }


    }

    public CbsRtnInfo processTxn(CbsTia6099 tia, Stdp10ProcessorRequest request) throws Exception {
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
                //���ش���
                //1���鿴���Ӧ����Ŀ�����Ƿ���ڼ�¼
                if (infoList.size() != 1) {
                    session.rollback();
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                    cbsRtnInfo.setRtnMsg("û�з�����Ϣ");
                    return cbsRtnInfo;
                } else {
                    FsLtfOrgComp orgComp = selectOrg(infoList.get(0).getBankMare());
                    if(!orgComp.getDeptCode().equals(dept)){
                        cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                        cbsRtnInfo.setRtnMsg("��������"+orgComp.getOrgName());
                        return cbsRtnInfo;
                    }
                    String cbsRespMsg = generateCbsRespMsg(infoList.get(0));
                    cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_SECCESS);
                    cbsRtnInfo.setRtnMsg(cbsRespMsg);
                    return cbsRtnInfo;
                }
            } else {
                cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
                cbsRtnInfo.setRtnMsg("�����Ų���Ϊ��");
                return cbsRtnInfo;
            }
        } catch (Exception e) {
            cbsRtnInfo.setRtnCode(TxnRtnCode.TXN_EXECUTE_FAILED);
            cbsRtnInfo.setRtnMsg(TxnRtnCode.TXN_EXECUTE_FAILED.getTitle());
            logger.info("6096���״����쳣" + e.getMessage().toString());
            return cbsRtnInfo;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // ����pkid �鷣����Ϣ
    private List<FsLtfVchOut> selectVchOut(CbsTia6099 tia) {
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

    //����CBS��Ӧ����
    private String generateCbsRespMsg(FsLtfVchOut fsLtfVchOut) {
        CbsToa6099 cbsToa = new CbsToa6099();
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
            throw new RuntimeException("��ɫƽ̨����ת��ʧ��." + e.getMessage());
        }

    }

    private FsLtfOrgComp selectOrg(String orgCode){
        FsLtfOrgCompMapper mapper = session.getMapper(FsLtfOrgCompMapper.class);
        FsLtfOrgCompExample example = new FsLtfOrgCompExample();
        example.createCriteria().andOrgCodeEqualTo(orgCode);
        List<FsLtfOrgComp> orgCompList = mapper.selectByExample(example);
        if(orgCompList.size()>0){
            return orgCompList.get(0);
        }else{
            return null;
        }
    }

}
