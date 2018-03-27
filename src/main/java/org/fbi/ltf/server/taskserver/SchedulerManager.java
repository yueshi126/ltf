package org.fbi.ltf.server.taskserver;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.fbi.ltf.helper.MybatisFactory;
import org.fbi.ltf.repository.dao.FsLtfSchedulerMapper;
import org.fbi.ltf.repository.model.FsLtfScheduler;
import org.fbi.ltf.repository.model.FsLtfSchedulerExample;
import org.osgi.framework.BundleContext;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerManager {
    private static final Logger logger = LoggerFactory.getLogger(SchedulerManager.class);

    private static SqlSessionFactory sqlSessionFactory = null;
    private static SqlSession session = null;

    public static Scheduler scheduler;
    public static Map jobInfoMap = new HashMap();

    public SchedulerManager() {
    }

    // 初始化
    public void start() {
        logger.info("正在加载调度作业任务信息......");
        try {
            SchedulerManager.loadSchedulerInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("加载调度作业任务信息结束！");

    }

    // 销毁
    public void destroy() {
        logger.info("正在关闭所有调度作业任务......");
        try {
            // 关闭调度作业
            SchedulerManager.shutdownScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("关闭调度作业任务信息结束！");

    }

    // 重新加载作业信息
    public static void reload() {
        logger.info("开始重新加载作业信息......");
        try {
            SchedulerManager.loadSchedulerInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("重新加载作业信息结束！");
    }

    public static void shutdown() {
        logger.info("开始停止所有作业......");
        try {
            SchedulerManager.shutdownScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("停止所有作业结束！");
    }

    // 加载作业信息
    private static synchronized void loadSchedulerInfo() throws Exception {

        SchedulerManager.shutdownScheduler();

        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        SchedulerManager.scheduler = schedulerFactory.getScheduler();
        sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
        session = sqlSessionFactory.openSession();
        session.getConnection().setAutoCommit(false);

        FsLtfSchedulerMapper fsLtfSchedulerMapper = session.getMapper(FsLtfSchedulerMapper.class);
        FsLtfSchedulerExample example = new FsLtfSchedulerExample();
        example.setOrderByClause(" jobid ");
        List<FsLtfScheduler> FsLtfSchedulerList = fsLtfSchedulerMapper.selectByExample(example);
        for (int i = 0; i < FsLtfSchedulerList.size(); i++) {
            FsLtfScheduler scheuler1 = (FsLtfScheduler) FsLtfSchedulerList.get(i);
            JobInfo job = new JobInfo(scheuler1);
        }
        SchedulerManager.scheduler.start();
    }

    // 关闭调度作业
    private static synchronized void shutdownScheduler() throws Exception {
        if (SchedulerManager.scheduler != null) {
            SchedulerManager.scheduler.shutdown(false);
            SchedulerManager.jobInfoMap.clear();
        }
    }

    public static void update() {
        try {
            sqlSessionFactory = MybatisFactory.ORACLE.getInstance();
            session = sqlSessionFactory.openSession();
            session.getConnection().setAutoCommit(false);
            FsLtfSchedulerMapper fsLtfSchedulerMapper = session.getMapper(FsLtfSchedulerMapper.class);
            FsLtfSchedulerExample example = new FsLtfSchedulerExample();
            example.createCriteria().andStatusNotEqualTo("0");
            example.setOrderByClause(" jobid ");
            List<FsLtfScheduler> FsLtfSchedulerStopList = fsLtfSchedulerMapper.selectByExample(example);
            for (int i = 0; i < FsLtfSchedulerStopList.size(); i++) {
                FsLtfScheduler fsLtfScheduler = (FsLtfScheduler) FsLtfSchedulerStopList.get(i);
                updateScheduleJob(scheduler, fsLtfScheduler);
            }
        } catch (Exception e) {
            logger.info(e.getMessage().toString());
        }
    }

    public static void updateScheduleJob(Scheduler scheduler, FsLtfScheduler scheduleJobPara) {
        try {
            List aa= scheduler.getCurrentlyExecutingJobs();
            List bb= scheduler.getGlobalTriggerListeners();
            List cc= scheduler.getSchedulerListeners();
            String[] c3= scheduler.getJobGroupNames();
            String[] c1= scheduler.getTriggerGroupNames();
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(scheduleJobPara.getJobid()+"", scheduleJobPara.getJobid()+"");
            String originConExpression = trigger.getCronExpression();
            if (!originConExpression.equalsIgnoreCase(scheduleJobPara.getCronexpression())) {
                trigger.setCronExpression(scheduleJobPara.getCronexpression());
                logger.info("修改作业名："+scheduleJobPara.getJobname()+" ID:" + scheduleJobPara.getJobid().toString()+" 时间为："+scheduleJobPara.getCronexpression());
                scheduler.rescheduleJob(scheduleJobPara.getJobid().toString(), scheduleJobPara.getJobid().toString(), trigger);
            }
        } catch (Exception e) {
            logger.info("修改定时任务id"+scheduleJobPara.getJobid()+"出错：" + e.getMessage().toString());


        }
    }


}





