//package org.fbi.ltf.server.taskserver;
//
//import org.quartz.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import skyline.stp.common.jobfactory.JobSyncFactory;
//import skyline.stp.repository.model.StpScheduleJob;
//
///**
// * Created by XIANGYANG on 2015-8-10.
// * ��ʱ��������
// */
//
//public class ScheduleUtils {
//
//    /** ��־���� */
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleUtils.class);
//
//    /**
//     * ��ȡ������key
//     *
//     * @param jobName
//     * @param jobGroup
//     * @return
//     */
//    public static TriggerKey getTriggerKey(String jobName, String jobGroup) {
//        return TriggerKey.triggerKey(jobName, jobGroup);
//    }
//
//    /**
//     * ��ȡ���ʽ������
//     *
//     * @param scheduler the scheduler
//     * @param jobName the job name
//     * @param jobGroup the job group
//     * @return cron trigger
//     */
//    public static CronTrigger getCronTrigger(Scheduler scheduler, String jobName, String jobGroup) {
//        try {
//            TriggerKey triggerKey = TriggerKey.triggerKey(scheduler.getSchedulerName(), scheduler.getj);
//            return (CronTrigger) scheduler.getTrigger(triggerKey);
//        } catch (SchedulerException e) {
//            logger.error("��ȡ��ʱ����CronTrigger�����쳣", e);
//            throw new RuntimeException("��ȡ��ʱ����CronTrigger�����쳣");
//        }
//    }
//
//    /**
//     * ��������
//     *
//     * @param scheduler the scheduler
//     * @param scheduleJobPara the schedule job
//     */
//    public static void createScheduleJob(Scheduler scheduler, StpScheduleJob scheduleJobPara) {
//        //ͬ�����첽
////        Class<? extends Job> jobClass = EnumSyns.SYNS.getCode().equals(isSync) ? JobSyncFactory.class : JobUnSyncFactory.class;
//        Class<? extends Job> jobClass =JobSyncFactory.class;
//
//        //����job��Ϣ
//        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup()).storeDurably(true).build();
//        //�������������ʱ�ķ������Ի�ȡ
//        jobDetail.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, scheduleJobPara.getPkid());
//
//        //���ʽ���ȹ�����
//        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJobPara.getCronExpression());
//
//        //���µ�cronExpression���ʽ����һ���µ�trigger
//        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup())
//                .withSchedule(scheduleBuilder).build();
//        /*//�������������ʱ�ķ������Ի�ȡ
//        trigger.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, param);*/
//
//        try {
//            scheduler.scheduleJob(jobDetail, trigger);
//        } catch (SchedulerException e) {
//            logger.error("������ʱ����ʧ��", e);
//            throw new RuntimeException("������ʱ����ʧ��");
//        }
//    }
//
//    /**
//     * ����һ������
//     *
//     * @param scheduler
//     * @param scheduleJobPara
//     */
//    public static void runOnce(Scheduler scheduler, StpScheduleJob scheduleJobPara) {
//        JobKey jobKey = JobKey.jobKey(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup());
//        try {
//            scheduler.triggerJob(jobKey);
////            JobDataMap jobDataMap=new JobDataMap();
////            jobDataMap.put(StpScheduleJob.JOB_PARAM_KEY,param);
////            scheduler.triggerJob(jobKey,jobDataMap);
//        } catch (SchedulerException e) {
//            logger.error("����һ�ζ�ʱ����ʧ��", e);
//            throw new RuntimeException("����һ�ζ�ʱ����ʧ��");
//        }
//    }
//
//    /**
//     * ��ͣ����
//     *
//     * @param scheduler
//     * @param jobName
//     * @param jobGroup
//     */
//    public static void pauseJob(Scheduler scheduler, String jobName, String jobGroup) {
//
//        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
//        try {
//            scheduler.pauseJob(jobKey);
//        } catch (SchedulerException e) {
//            logger.error("��ͣ��ʱ����ʧ��", e);
//            throw new RuntimeException("��ͣ��ʱ����ʧ��");
//        }
//    }
//
//    /**
//     * �ָ�����
//     *
//     * @param scheduler
//     * @param jobName
//     * @param jobGroup
//     */
//    public static void resumeJob(Scheduler scheduler, String jobName, String jobGroup) {
//
//        JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
//        try {
//            scheduler.resumeJob(jobKey);
//        } catch (SchedulerException e) {
//            logger.error("�ָ���ʱ����ʧ��", e);
//            throw new RuntimeException("�ָ���ʱ����ʧ��");
//        }
//    }
//
//    /**
//     * ��ȡjobKey
//     *
//     * @param jobName the job name
//     * @param jobGroup the job group
//     * @return the job key
//     */
//    public static JobKey getJobKey(String jobName, String jobGroup) {
//        return JobKey.jobKey(jobName, jobGroup);
//    }
//
//    /**
//     * ���¶�ʱ����
//     *
//     * @param scheduler the scheduler
//     * @param scheduleJobPara the schedule job
//     */
//    public static void updateScheduleJob(Scheduler scheduler, StpScheduleJob scheduleJobPara) {
//        try {
//            TriggerKey triggerKey = ScheduleUtils.getTriggerKey(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup());
//            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//            // �жϴ�DB��ȡ�õ�����ʱ��(dbCronExpression)�����ڵ�quartz�߳��е�����ʱ��(originConExpression)�Ƿ����
//            // �����ȣ����ʾ�û���û�������趨���ݿ��е�����ʱ�䣬�����������Ҫ����rescheduleJob
//            String originConExpression = trigger.getCronExpression();
//            if(!originConExpression.equalsIgnoreCase(scheduleJobPara.getCronExpression())){
//                //���ʽ���ȹ�����
//                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJobPara.getCronExpression());
//                //���µ�cronExpression���ʽ���¹���trigger
//                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
////                trigger.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, param);
//                scheduler.rescheduleJob(triggerKey, trigger);
//            }
//        } catch (SchedulerException e) {
//            logger.error("���¶�ʱ����ʧ��", e);
//            throw new RuntimeException("���¶�ʱ����ʧ��");
//        }
//    }
//
//    /**
//     * ɾ����ʱ����
//     *
//     * @param scheduler
//     * @param jobName
//     * @param jobGroup
//     */
//    public static void deleteScheduleJob(Scheduler scheduler, String jobName, String jobGroup) {
//        try {
//            scheduler.deleteJob(getJobKey(jobName, jobGroup));
//        } catch (SchedulerException e) {
//            logger.error("ɾ����ʱ����ʧ��", e);
//            throw new RuntimeException("ɾ����ʱ����ʧ��");
//        }
//    }
//}
