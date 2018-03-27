package org.fbi.ltf.helper;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ZZP_YY on 2018-03-07.
 */
public class LTFTools {
    private static Logger logger = LoggerFactory.getLogger(LTFTools.class);

    public static String dateFormat(String oldDate, String oldDatepatern, String newDatepatern) {
        String newDate = "";
        SimpleDateFormat sdf1 = new SimpleDateFormat(oldDatepatern);
        SimpleDateFormat sdf2 = new SimpleDateFormat(newDatepatern);
        Date date = null;
        if (StringUtils.isEmpty(oldDate)) {
            return "";
        }
        try {
            date = (sdf1.parse(oldDate));
        } catch (ParseException e) {
            logger.info("日志转化错误");
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        newDate = sdf2.format(cal.getTime());
        return newDate;
    }

    public static String datePlusOnedayFormat(String oldDate, String oldDatepatern, String newDatepatern) {
        String newDate = "";
        SimpleDateFormat sdf1 = new SimpleDateFormat(oldDatepatern);
        SimpleDateFormat sdf2 = new SimpleDateFormat(newDatepatern);
        Date date = null;
        try {
            date = (sdf1.parse(oldDate));
        } catch (ParseException e) {
            logger.info("日志转化错误");
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        newDate = sdf2.format(cal.getTime());
        return newDate;
    }

    // 取消日期格式中的符号，只保留数字
    public static String replceDate(String oldDate) {
        String newDate = "";
        if (StringUtils.isEmpty(oldDate)) {
            return "";
        } else {
            newDate = oldDate.replace("-", "").replace(":", "").replace(" ", "").trim();
        }
        return newDate;
    }
    /**
     * 日期：加一天
     * @param oldDate：yyyyMMdd
     * @return newDate:yyyyMMdd
     */
    public static String datePlusOneday(String oldDate){
        String newDate = "";
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        Date date = null;
        try {
            date = (sdf1.parse(oldDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, 1);
        newDate = sdf2.format(cal.getTime());
        return newDate;
    }

    public static void main(String[] args) {
        System.out.println(dateFormat("20151212", "yyyyMMdd", "yyyy-MM-dd HH:mm:ss"));

    }

}
