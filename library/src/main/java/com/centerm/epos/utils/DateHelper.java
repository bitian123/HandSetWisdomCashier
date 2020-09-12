package com.centerm.epos.utils;


import org.joda.time.DateTime;

public class DateHelper {
    public static final long ONE_DAY = 1000 * 60 * 60 * 24;

    public static long  calcDelay(int hour, int minute, int second) {
        if (!(0 <= hour && hour <= 23 && 0 <= minute && minute <= 59 && 0 <= second && second <= 59)) {
            throw new IllegalArgumentException();
        }
        return calcDelay(fixed(hour, minute, second));
    }

    private static long calcDelay(DateTime targetDatetimeOfToday) {
        long delay = 0;
        DateTime now = new DateTime();

        //时间点已过，只好延时到明天的这个时间点再执行
        if (now.isAfter(targetDatetimeOfToday)) {
            delay = now.plusDays(1).getMillis() - now.getMillis();

            //时间点未到
        } else {
            delay = targetDatetimeOfToday.getMillis() - now.getMillis();
        }

        return delay;
    }

    /**
     * 返回这样一个DateTime对象：
     * 1.日期为今天
     * 2.时分秒为参数指定的值
     *
     * @param hour   0-23
     * @param minute 0-59
     * @param second 0-59
     * @return
     */
    private static DateTime fixed(int hour, int minute, int second) {
        return new DateTime()
                .withHourOfDay(hour).withMinuteOfHour(minute).withSecondOfMinute(second);
    }
}
