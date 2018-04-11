package org.fbi.ltf.server.taskserver;

import org.fbi.linking.codec.dataformat.format.DatePatternFormat;
import org.fbi.linking.processor.standprotocol10.Stdp10ProcessorRequest;
import org.fbi.ltf.domain.cbs.T6071Request.CbsTia6071;
import org.fbi.ltf.domain.cbs.T6091Request.CbsTia6091;
import org.fbi.ltf.domain.cbs.T6092Request.CbsTia6092;
import org.fbi.ltf.domain.cbs.T6093Request.CbsTia6093;
import org.fbi.ltf.processor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by test on
 * 2016/2/25.
 * 自动任务
 */
public class AutoTask {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String date8 = new SimpleDateFormat("yyyyMMdd").format(new Date());
    private String operDate8Back1 = getDateAfter(date8, -1, "yyyyMMdd");

    /*
    点单查询 查询综合平台对账结果1002
     */
    public void autoOrderBillQuery1002() {
        try {
            logger.info("autoOrderBillQuery1002");
            Thread.sleep(1000);
            logger.info("autoOrderBillQuery1002 task started at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            txn1606071("");
            logger.info("autoOrderBillQuery1002 task finished at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        } catch (Exception E) {
            logger.error("AutoTak.java.autoRecSys:" + E.getMessage());
        }

    }

//    public void autoTaskUpdate() {
//        try {
//            Thread.sleep(5000);
//            logger.info("autoTaskUpdate task started at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//            SchedulerManager schedulerManager= new SchedulerManager();
//            schedulerManager.update();
//            logger.info("autoTaskUpdate task finished at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//        } catch (Exception E) {
//            logger.error("AutoTak.java.autoRecSys:" + E.getMessage());
//        }
//
//    }

//    // automatically reconciliation system 自动对账
//    public void autoRecSys() {
//        try {
//            Thread.sleep(3000);
//            logger.info("autoRecSys task started at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//            task60091();
//            task60092();
//            logger.info("autoRecSys task finished at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//        } catch (Exception E) {
//            logger.error("AutoTak.java.autoRecSys:" + E.getMessage());
//        }
//
//    }

    // 对账完成数据
    public void yhdzwc() {
        try {
            logger.info("task1606093  task started at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            task1606093(operDate8Back1);
            logger.info("task1606093  task finished at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        } catch (Exception E) {
            logger.error("AutoTak.java.yhdzwc:" + E.getMessage());
        }

    }

    // 对账异常数据
    public void yhdzyc() {
        try {
            logger.info("task1606094 task started at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            task1606094(operDate8Back1);
            logger.info("task1606094 task finished at:" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        } catch (Exception E) {
            logger.error("AutoTak.java.yhdzyc:" + E.getMessage());
        }

    }

    public void task60091() {
        try {
            txn1606091(operDate8Back1);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("task60091 error");
        }
    }

    public void task60092() {
        try {
            // 对昨天的帐，转日期是今天转今天的帐 ，
            txn1606092(date8);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("task60092 error");
        }
    }



    public void txn1606091(String chkDate) {
//        T6091Processor t6091Processor = new T6091Processor();
//        CbsTia6091 tia = new CbsTia6091();
//        tia.setTxnDate(chkDate);
//        try {
//            t6091Processor.processTxn(tia);
//        } catch (Exception E) {
//            logger.info("txn1606091：err" + E.getMessage());
//        }
    }

    public void txn1606092(String chkDate) {
//        T6092Processor t6092Processor = new T6092Processor();
//        CbsTia6092 tia = new CbsTia6092();
//        tia.setTxnDate(chkDate);
//        try {
//            t6092Processor.processTxn(tia);
//        } catch (Exception E) {
//            logger.info("txn1606092：err" + E.getMessage());
//        }
    }

    public void task1606093(String chkDate) {
        T6093Processor t6093Processor = new T6093Processor();
        CbsTia6093 tia = new CbsTia6093();
        try {
            t6093Processor.processTxn(tia,null );
        } catch (Exception E) {
            logger.info("task1606093：err" + E.getMessage());
        }
    }

    public void task1606094(String chkDate) {
        T6094Processor t6094Processor = new T6094Processor();
        CbsTia6093 tia = new CbsTia6093();
        try {
            t6094Processor.processTxn(tia);
        } catch (Exception E) {
            logger.info("task1606094：err" + E.getMessage());
        }
    }
    public void txn1606071(String str) {
        T6071Processor t6071Processor = new T6071Processor();
        CbsTia6071 tia = new CbsTia6071();
        try {
            t6071Processor.processTxn(tia);
        } catch (Exception E) {
            logger.info("txn1606071：err" + E.getMessage());
        }
    }


    public String getDateAfter(String strDate, int days, String pattern) {
        try {
            DatePatternFormat datePatternFormat = new DatePatternFormat("yyyyMMdd");
            Date date = datePatternFormat.parse(strDate);
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + days);
            return sdf.format(calendar.getTime());
        } catch (Exception e) {
            logger.error("autotask:" + e.getMessage());
            throw new RuntimeException("时间转化异常");
        }
    }
}
