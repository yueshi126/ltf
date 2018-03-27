package org.fbi.ltf.server.taskserver;


import org.fbi.ltf.repository.model.FsLtfScheduler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;


import java.text.ParseException;
import java.util.*;


public class JobInfo {

  String JOBID; // ��ҵ���
  String JOBNAME; // ��ҵ����
  String CRONEXPRESSION; // ��ҵʱ����ʽ
  String STATUS; // ��ҵ״̬
  String MAILONFAIL; // ��ҵʧ�ܷ����ʼ���ַ֪ͨ
  String JOBACTION; // ��ҵ������
  String JOBMETHOD; // ��ҵ��������
  String JOBPARAM; // ��ҵ��������

  JobDetail job;
  Trigger trigger;

  /**
   * ��ʼ����ҵ��Ϣ
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
        // ��������ʱ��
        Date startTime = new Date( (new Date()).getTime() + 15000);
        // ������ҵ
        this.job = new JobDetail(this.JOBID, this.JOBID, SchedulerJob.class);
        // ����������
        this.trigger = new CronTrigger(this.JOBID, this.JOBID, this.JOBID,
                                       this.JOBID, startTime, null,
                                       this.CRONEXPRESSION);
        // ���е���
        startTime = SchedulerManager.scheduler.scheduleJob(this.job,
            this.trigger);
        // ������ҵ��Ϣ
        SchedulerManager.jobInfoMap.put(this.JOBID, this);

        System.out.println(this.toString() + "����ҵ��ʼ���ɹ�������ҵ���ڡ�" + startTime +
                           "����һ������");

      }
      catch (ParseException e) {
        System.out.println("���Ϊ��" + JOBID + "������ҵ����ʱ���ʽ��" + CRONEXPRESSION +
                           "����ʽ�����⣡");
        e.printStackTrace();
      }
      catch (SchedulerException e) {
        System.out.println("���Ϊ��" + JOBID + "������ҵ�����е��ȳ���");
        e.printStackTrace();
      }
    }
  }

  /**
   * ��־
   */
  public void log(Throwable ex) {
    System.out.println("log: "+ex.getMessage().toString());
//    DatabaseConnection dc = null;
//    try {
//      String info;
//      String update;
//      String title = "";
//
//      // ���гɹ�
//      if (ex == null) {
//        title = new Date() + "sucess��";
//        info = new Date() + "sucess��";
//
//        update =
//            "update SYS_SCHEDULER set SUCCESSCOUNT=SUCCESSCOUNT+1, LASTEXECUTETIME="
//            + toOracleDateTimeFormat(new GregorianCalendar()) +
//            ", INFORMATION='" + info + "'"
//            + " where JOBID='" + this.JOBID + "'";
//
//      }
//      else {
//        title = new Date() + "fail��";
//        info = new Date() + "fail��" + ex.getMessage();
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
   * �����ʼ�
   * @param info �ʼ�����
   */
  private void sendMail(String title, String info) {
//    List receiverList = new ArrayList();
//    receiverList.add(this.MAILONFAIL);
//
////		MailSendHelper helper = new MailSendHelper(
////				"������ҵ����ʧ��! " + new Date(), info, receiverList, null);
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
   * ת����'0000-00-00 00:00:00'�����ڸ�ʽ��ʹ����Oracle���ݿ�
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

  // ����������Ϣ
  public String toString() {
    return "��š�" + JOBID + "������ҵ����" + JOBNAME + "������ҵ�����ࡾ" + JOBACTION + "����������"+JOBMETHOD+"��";
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
