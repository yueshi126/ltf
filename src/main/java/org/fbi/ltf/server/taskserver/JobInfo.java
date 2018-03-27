package org.fbi.ltf.server.taskserver;


import org.fbi.ltf.repository.model.FsLtfScheduler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;


import java.text.ParseException;
import java.util.*;


public class JobInfo {

  String JOBID; // 作业编号
  String JOBNAME; // 作业名字
  String CRONEXPRESSION; // 作业时间表达式
  String STATUS; // 作业状态
  String MAILONFAIL; // 作业失败发送邮件地址通知
  String JOBACTION; // 作业动作类
  String JOBMETHOD; // 作业动作函数
  String JOBPARAM; // 作业动作参数

  JobDetail job;
  Trigger trigger;

  /**
   * 初始化作业信息
   * @param scheduler
   */
  public JobInfo(FsLtfScheduler scheduler) {
    this.JOBID = scheduler.getJobid() + "";
    this.JOBNAME = scheduler.getJobname();
    this.CRONEXPRESSION = scheduler.getCronexpression();
    this.STATUS = scheduler.getStatus();
    this.MAILONFAIL = scheduler.getMailonfail();
    this.JOBACTION = scheduler.getJobaction();
    this.JOBMETHOD = scheduler.getJobmethod();
    this.JOBPARAM = scheduler.getJobparam();

    if ("1".equals(this.STATUS)) {
      try {
        // 设置启动时间
        Date startTime = new Date( (new Date()).getTime() + 15000);
        // 创建作业
        this.job = new JobDetail(this.JOBID, this.JOBID, SchedulerJob.class);
        // 创建触发器
        this.trigger = new CronTrigger(this.JOBID, this.JOBID, this.JOBID,
                                       this.JOBID, startTime, null,
                                       this.CRONEXPRESSION);
        // 进行调度
        startTime = SchedulerManager.scheduler.scheduleJob(this.job,
            this.trigger);
        // 保存作业信息
        SchedulerManager.jobInfoMap.put(this.JOBID, this);

        System.out.println(this.toString() + "的作业初始化成功，该作业将在【" + startTime +
                           "】第一次启动");

      }
      catch (ParseException e) {
        System.out.println("编号为【" + JOBID + "】的作业，定时表达式【" + CRONEXPRESSION +
                           "】格式有问题！");
        e.printStackTrace();
      }
      catch (SchedulerException e) {
        System.out.println("编号为【" + JOBID + "】的作业，进行调度出错！");
        e.printStackTrace();
      }
    }
  }

  /**
   * 日志
   */
  public void log(Throwable ex) {
    System.out.println("log: "+ex.getMessage().toString());
//    DatabaseConnection dc = null;
//    try {
//      String info;
//      String update;
//      String title = "";
//
//      // 运行成功
//      if (ex == null) {
//        title = new Date() + "sucess！";
//        info = new Date() + "sucess！";
//
//        update =
//            "update SYS_SCHEDULER set SUCCESSCOUNT=SUCCESSCOUNT+1, LASTEXECUTETIME="
//            + toOracleDateTimeFormat(new GregorianCalendar()) +
//            ", INFORMATION='" + info + "'"
//            + " where JOBID='" + this.JOBID + "'";
//
//      }
//      else {
//        title = new Date() + "fail！";
//        info = new Date() + "fail！" + ex.getMessage();
//
//        update =
//            "update SYS_SCHEDULER set FAILCOUNT=FAILCOUNT+1, LASTEXECUTETIME="
//            + toOracleDateTimeFormat(new GregorianCalendar()) +
//            ", INFORMATION='" + info + "'"
//            + " where JOBID='" + this.JOBID + "'";
//
//      }
//
//      title += " " + this.toString();
//      if (this.MAILONFAIL != null && this.MAILONFAIL.indexOf("@") > 0) {
//        sendMail(title, info + "\n" + this.toString());
//      }
//
//      info += " " + this.toString();
//
//       System.out.println(info);
//
//      String insert =
//          "insert into SYS_SCHEDULER_LOG values('"
//          + this.getJOBID()
//          + "','"
//          + this.getJOBNAME()
//          + "',sysdate,'"
//          + info
//          + "')";
//
//      dc = ConnectionManager.getInstance().getConnection();
//      dc.executeUpdate(insert);
//      dc.executeUpdate(update);
//
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//
//    }
//    finally {
//      if (dc != null) {
//        ConnectionManager.getInstance().releaseConnection(dc);
//      }
//    }
  }

  /**
   * 发送邮件
   * @param info 邮件内容
   */
  private void sendMail(String title, String info) {
//    List receiverList = new ArrayList();
//    receiverList.add(this.MAILONFAIL);
//
////		MailSendHelper helper = new MailSendHelper(
////				"调度作业运行失败! " + new Date(), info, receiverList, null);
//    MailSendHelper helper = new MailSendHelper(title, info, receiverList, null);
//
//    try {
//      helper.send();
//    }
//    catch (Exception e) {
//      e.printStackTrace();
//    }
  }

  /**
   * 转换成'0000-00-00 00:00:00'的日期格式，使用于Oracle数据库
   * @param calendar
   * @return
   */
  public static String toOracleDateTimeFormat(Calendar calendar) {
    StringBuffer sb = new StringBuffer();
    sb.append(" to_date('");
    sb.append(calendar.get(Calendar.YEAR) + "-");
    sb.append( (calendar.get(Calendar.MONTH) + 1) + "-");
    sb.append(calendar.get(Calendar.DAY_OF_MONTH) + " ");
    sb.append(calendar.get(Calendar.HOUR_OF_DAY) + ":");
    sb.append(calendar.get(Calendar.MINUTE) + ":");
    sb.append(calendar.get(Calendar.SECOND) + "', 'YYYY-MM-DD hh24:mi:ss') ");
    return sb.toString();
  }

  // 调度任务信息
  public String toString() {
    return "编号【" + JOBID + "】，作业名【" + JOBNAME + "】，作业动作类【" + JOBACTION + "】，方法【"+JOBMETHOD+"】";
  }

  public String getCRONEXPRESSION() {
    return CRONEXPRESSION;
  }

  public String getJOBACTION() {
    return JOBACTION;
  }

  public String getJOBID() {
    return JOBID;
  }

  public String getJOBMETHOD() {
    return JOBMETHOD;
  }

  public String getJOBNAME() {
    return JOBNAME;
  }

  public String getJOBPARAM() {
    return JOBPARAM;
  }

  public String getMAILONFAIL() {
    return MAILONFAIL;
  }

  public String getSTATUS() {
    return STATUS;
  }

  public static void main(String[] args) {

  }

}
