package org.fbi.ltf.server.taskserver;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.Method;


public class SchedulerJob implements Job {

    public void execute(JobExecutionContext context) throws JobExecutionException {
    	String jobName = context.getJobDetail().getName();
    	JobInfo job = (JobInfo)SchedulerManager.jobInfoMap.get(jobName);

    	try {
			Class actionClass = Class.forName(job.getJOBACTION());
			Object action = actionClass.newInstance();

			// ���ַ��������ĺ���
			Method method = getMethod(actionClass, job.getJOBMETHOD(), new Class[]{String.class});
			if(method != null){
				method.invoke(action, new Object[]{job.getJOBPARAM()});
				job.log(null);
				return;
			}

			// �����ַ����ĺ���
			method = getMethod(actionClass, job.getJOBMETHOD(), null);
			if(method != null){
				method.invoke(action, null);
				job.log(null);
				return;
			}

			job.log(new Exception("û���ҵ�" + job + "��ҵaction��Ӧ�ĺ���"));
			System.out.println("û���ҵ�" + job + "��ҵaction��Ӧ�ĺ���");


		} catch (Exception e) {
			job.log(e.getCause());
			e.printStackTrace();
		}
    }


    private Method getMethod(Class actionClass, String methodName, Class[] params){
    	try {
			return actionClass.getMethod(methodName, params);
		} catch (Exception e) {
			return null;
		}
    }

}
