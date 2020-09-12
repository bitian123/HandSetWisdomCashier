package com.centerm.epos.ebi.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by liubit on 2017/12/25.
 */

public class DateUtil {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getTerminalDate(){
        String date = SDF.format(new Date());
        return date.substring(0,8);
    }

    /**
     *获取几个月前的日期
     * @return
     */
    public static String getMonthAgo() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }

    public static String getMonthAgo(int month) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -month);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }

    /**
     *获取几天前的日期
     * @return
     */
    public static String getDayAgo() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -1);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }
    public static String getDayAgo(int day) {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, -day);
        String monthAgo = simpleDateFormat.format(calendar.getTime());
        return monthAgo;
    }

    //获取两个日期之间的天数
    public static int getDayBetween(String dayBefore,String dayAfter) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date bdate = simpleDateFormat.parse(dayAfter);
            Date smdate = simpleDateFormat.parse(dayBefore);

            SimpleDateFormat sdf = SDF;
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
            Calendar cal = Calendar.getInstance();
            cal.setTime(smdate);
            long time1 = cal.getTimeInMillis();
            cal.setTime(bdate);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;

    }
    //获取1个日期到今天的天数
    public static int getDayBetween(String dayBefore) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            Date bdate = new Date();
            Date smdate = simpleDateFormat.parse(dayBefore);

            SimpleDateFormat sdf = SDF;
            smdate = sdf.parse(sdf.format(smdate));
            bdate = sdf.parse(sdf.format(bdate));
            Calendar cal = Calendar.getInstance();
            cal.setTime(smdate);
            long time1 = cal.getTimeInMillis();
            cal.setTime(bdate);
            long time2 = cal.getTimeInMillis();
            long between_days = (time2 - time1) / (1000 * 3600 * 24);
            return Integer.parseInt(String.valueOf(between_days));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;

    }

    //判断8位日期格式是否正确
    public static boolean dateCheck(String dateStr) {
        boolean convertSuccess = true;
        // 指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        try {
            // 设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            format.setLenient(false);
            format.parse(dateStr);
        } catch (ParseException e) {
            // e.printStackTrace();
            // 如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            convertSuccess = false;
        }
        return convertSuccess;
    }

    public static String getToday(String format){
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(new Date());
    }

    public static long getNowTime(){
        return new Date().getTime();
    }

    public static String formatTime(String time, String preFormat, String toFormat){
        SimpleDateFormat preSdf = new SimpleDateFormat(preFormat);
        SimpleDateFormat toSdf = new SimpleDateFormat(toFormat);
        try {
            Date date = preSdf.parse(time);
            return toSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
