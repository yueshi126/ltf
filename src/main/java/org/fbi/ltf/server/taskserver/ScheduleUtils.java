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
// * 定时任务辅助类
// */
//
//public class ScheduleUtils {
//
//    /** 日志对象 */
//    private static final Logger logger = LoggerFactory.getLogger(ScheduleUtils.class);
//
//    /**
//     * 获取触发器key
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
//     * 获取表达式触发器
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
//            logger.error("获取定时任务CronTrigger出现异常", e);
//            throw new RuntimeException("获取定时任务CronTrigger出现异常");
//        }
//    }
//
//    /**
//     * 创建任务
//     *
//     * @param scheduler the scheduler
//     * @param scheduleJobPara the schedule job
//     */
//    public static void createScheduleJob(Scheduler scheduler, StpScheduleJob scheduleJobPara) {
//        //同步或异步
////        Class<? extends Job> jobClass = EnumSyns.SYNS.getCode().equals(isSync) ? JobSyncFactory.class : JobUnSyncFactory.class;
//        Class<? extends Job> jobClass =JobSyncFactory.class;
//
//        //构建job信息
//        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup()).storeDurably(true).build();
//        //放入参数，运行时的方法可以获取
//        jobDetail.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, scheduleJobPara.getPkid());
//
//        //表达式调度构建器
//        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJobPara.getCronExpression());
//
//        //按新的cronExpression表达式构建一个新的trigger
//        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup())
//                .withSchedule(scheduleBuilder).build();
//        /*//放入参数，运行时的方法可以获取
//        trigger.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, param);*/
//
//        try {
//            scheduler.scheduleJob(jobDetail, trigger);
//        } catch (SchedulerException e) {
//            logger.error("创建定时任务失败", e);
//            throw new RuntimeException("创建定时任务失败");
//        }
//    }
//
//    /**
//     * 运行一次任务
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
//            logger.error("运行一次定时任务失败", e);
//            throw new RuntimeException("运行一次定时任务失败");
//        }
//    }
//
//    /**
//     * 暂停任务
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
//            logger.error("暂停定时任务失败", e);
//            throw new RuntimeException("暂停定时任务失败");
//        }
//    }
//
//    /**
//     * 恢复任务
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
//            logger.error("恢复定时任务失败", e);
//            throw new RuntimeException("恢复定时任务失败");
//        }
//    }
//
//    /**
//     * 获取jobKey
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
//     * 更新定时任务
//     *
//     * @param scheduler the scheduler
//     * @param scheduleJobPara the schedule job
//     */
//    public static void updateScheduleJob(Scheduler scheduler, StpScheduleJob scheduleJobPara) {
//        try {
//            TriggerKey triggerKey = ScheduleUtils.getTriggerKey(scheduleJobPara.getJobName(), scheduleJobPara.getJobGroup());
//            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
//            // 判断从DB中取得的任务时间(dbCronExpression)和现在的quartz线程中的任务时间(originConExpression)是否相等
//            // 如果相等，则表示用户并没有重新设定数据库中的任务时间，这种情况不需要重新rescheduleJob
//            String originConExpression = trigger.getCronExpression();
//            if(!originConExpression.equalsIgnoreCase(scheduleJobPara.getCronExpression())){
//                //表达式调度构建器
//                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJobPara.getCronExpression());
//                //按新的cronExpression表达式重新构建trigger
//                trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
////                trigger.getJobDataMap().put(StpScheduleJob.JOB_PARAM_KEY, param);
//                scheduler.rescheduleJob(triggerKey, trigger);
//            }
//        } catch (SchedulerException e) {
//            logger.error("更新定时任务失败", e);
//            throw new RuntimeException("更新定时任务失败");
//        }
//    }
//
//    /**
//     * 删除定时任务
//     *
//     * @param scheduler
//     * @param jobName
//     * @param jobGroup
//     */
//    public static void deleteScheduleJob(Scheduler scheduler, String jobName, String jobGroup) {
//        try {
//            scheduler.deleteJob(getJobKey(jobName, jobGroup));
//        } catch (SchedulerException e) {
//            logger.error("删除定时任务失败", e);
//            throw new RuntimeException("删除定时任务失败");
//        }
//    }
//}
