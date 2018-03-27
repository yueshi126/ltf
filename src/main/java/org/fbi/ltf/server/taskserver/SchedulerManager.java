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

    // ��ʼ��
    public void start() {
        logger.info("���ڼ��ص�����ҵ������Ϣ......");
        try {
            SchedulerManager.loadSchedulerInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("���ص�����ҵ������Ϣ������");

    }

    // ����
    public void destroy() {
        logger.info("���ڹر����е�����ҵ����......");
        try {
            // �رյ�����ҵ
            SchedulerManager.shutdownScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("�رյ�����ҵ������Ϣ������");

    }

    // ���¼�����ҵ��Ϣ
    public static void reload() {
        logger.info("��ʼ���¼�����ҵ��Ϣ......");
        try {
            SchedulerManager.loadSchedulerInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("���¼�����ҵ��Ϣ������");
    }

    public static void shutdown() {
        logger.info("��ʼֹͣ������ҵ......");
        try {
            SchedulerManager.shutdownScheduler();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("ֹͣ������ҵ������");
    }

    // ������ҵ��Ϣ
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

    // �رյ�����ҵ
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
                logger.info("�޸���ҵ����"+scheduleJobPara.getJobname()+" ID:" + scheduleJobPara.getJobid().toString()+" ʱ��Ϊ��"+scheduleJobPara.getCronexpression());
                scheduler.rescheduleJob(scheduleJobPara.getJobid().toString(), scheduleJobPara.getJobid().toString(), trigger);
            }
        } catch (Exception e) {
            logger.info("�޸Ķ�ʱ����id"+scheduleJobPara.getJobid()+"����" + e.getMessage().toString());


        }
    }


}





