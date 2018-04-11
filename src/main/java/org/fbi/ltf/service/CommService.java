package org.fbi.ltf.service;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.fbi.ltf.enums.TxnRtnCode;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.helper.MybatisManager;
import org.fbi.ltf.repository.dao.FsLtfAcctDealMapper;
import org.fbi.ltf.repository.dao.FsLtfSysCltMapper;
import org.fbi.ltf.repository.dao.common.CommonMapper;
import org.fbi.ltf.repository.model.FsLtfAcctDeal;
import org.fbi.ltf.repository.model.FsLtfSysClt;
import org.fbi.ltf.repository.model.FsLtfSysCltExample;
import org.fbi.ltf.repository.model.common.FsLtfTransAmt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ZZP_YY on 2018-03-16.
 */
public class CommService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    MybatisManager manager = new MybatisManager();

    public FsLtfSysClt selectFsLtfSysCtl(String sysid) {
        SqlSession session = null;
        try {
            session = manager.getSessionFactory().openSession();
            FsLtfSysCltMapper fsLtfVchDzwcMapper = session.getMapper(FsLtfSysCltMapper.class);
            FsLtfSysCltExample example = new FsLtfSysCltExample();
            example.createCriteria().andSysIdEqualTo(sysid);
            List<FsLtfSysClt> fsLtfSysCltList = fsLtfVchDzwcMapper.selectByExample(example);
            if (fsLtfSysCltList.size() == 1) {
                return fsLtfSysCltList.get(0);
            } else {
                logger.info("");
                return null;
            }
        } finally {
            if (session != null) session.close();
        }
    }

    public int updateFsLtfSysCtl(FsLtfSysClt fsLtfSysClt) {
        SqlSession session = null;
        int cnt = -1;
        try {
            session = manager.getSessionFactory().openSession();
            FsLtfSysCltMapper fsLtfVchDzwcMapper = session.getMapper(FsLtfSysCltMapper.class);
            FsLtfSysCltExample example = new FsLtfSysCltExample();
            example.createCriteria().andPkidEqualTo(fsLtfSysClt.getPkid());
            cnt = fsLtfVchDzwcMapper.updateByExampleSelective(fsLtfSysClt, example);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
        return cnt;
    }

    public int updateLVOChkActDt(String chkActDt, SqlSession session, String preActSerial) {
        int cnt = -1;
        CommonMapper commonMapper = session.getMapper(CommonMapper.class);
        cnt = commonMapper.updateLVOChkActDt(chkActDt, preActSerial);
        return cnt;
    }

    // 修改综合平台上传财政标志 0- 等待上传
    public int updateLVOUpload(String chkActDt, SqlSession session, String preActSerial) {
        int cnt = -1;
        CommonMapper commonMapper = session.getMapper(CommonMapper.class);
        cnt = commonMapper.updateLVOUpload(chkActDt, preActSerial);
        return cnt;
    }

    public int updateTickActDt(String chkActDt, SqlSession session, String preActSerial) {
        int cnt = -1;
        CommonMapper commonMapper = session.getMapper(CommonMapper.class);
        cnt = commonMapper.updateTickActDt(chkActDt, preActSerial);
        return cnt;
    }

    public int updateTickUpload(String chkActDt, SqlSession session, String preActSerial) {
        int cnt = -1;
        CommonMapper commonMapper = session.getMapper(CommonMapper.class);
        cnt = commonMapper.updateTickUpload(chkActDt, preActSerial);
        return cnt;
    }

    //  综合平台交款数据
    public List<FsLtfTransAmt> selectNetAmt(String chkActDt,SqlSession session) {
        List<FsLtfTransAmt> ltfTransAmtList = new ArrayList<>();
        try {
            session = manager.getSessionFactory().openSession();
            CommonMapper commonMapper = session.getMapper(CommonMapper.class);
            ltfTransAmtList = commonMapper.selectNetAmt(chkActDt);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
        return ltfTransAmtList;
    }

    //柜面交款数据
    public List<FsLtfTransAmt> selectCounterAmt(String chkActDt) {
        SqlSession session = null;
        List<FsLtfTransAmt> ltfTransAmtList = new ArrayList<>();
        try {
            session = manager.getSessionFactory().openSession();
            CommonMapper commonMapper = session.getMapper(CommonMapper.class);
            ltfTransAmtList = commonMapper.selectCounterAmt(chkActDt);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
        return ltfTransAmtList;
    }

    public int insertAcctDeal(FsLtfAcctDeal record) {
        int cnt = -1;
        SqlSession session = null;
        List<FsLtfTransAmt> ltfTransAmtList = new ArrayList<>();
        try {
            session = manager.getSessionFactory().openSession();
            FsLtfAcctDealMapper fsLtfAcctDealMapper = session.getMapper(FsLtfAcctDealMapper.class);
            cnt = fsLtfAcctDealMapper.insertSelective(record);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
        return cnt;
    }

    public int updateAcctDeal(FsLtfAcctDeal record) {
        int cnt = -1;
        SqlSession session = null;
        List<FsLtfTransAmt> ltfTransAmtList = new ArrayList<>();
        try {
            session = manager.getSessionFactory().openSession();
            FsLtfAcctDealMapper fsLtfAcctDealMapper = session.getMapper(FsLtfAcctDealMapper.class);
            cnt = fsLtfAcctDealMapper.updateByPrimaryKeySelective(record);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null) session.close();
        }
        return cnt;
    }

}
