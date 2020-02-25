package com.dili.ss.util;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by asiam on 2017/4/28 0028.
 */
public class DateUtils {

    /**
     * 获取当前毫秒数
     * @return
     */
    public static long getServerTime() {
        return System.currentTimeMillis();
    }

    /**
     * 日期格式化为yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static String format(Date date) {
        return format(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 当前日期格式化(时区为GMT+08:00)
     * @param format
     * @return
     */
    public static String format(String format) {
        return format(LocalDateTime.now(ZoneId.of("GMT+08:00")), format);
    }

    /**
     * 日期格式化
     * @param localDateTime
     * @param format
     * @return
     */
    public static String format(LocalDateTime localDateTime, String format) {
        return DateTimeFormatter.ofPattern(format).format(localDateTime);
    }
    /**
     * 日期格式化
     * @param date
     * @param format
     * @return
     */
    public static String format(Date date, String format) {
        if (date == null) {
            return null;
        } else {
            try {
                SimpleDateFormat e = new SimpleDateFormat(format);
                return e.format(date);
            } catch (Exception var3) {
                throw new RuntimeException("日期格式化转换失败", var3);
            }
        }
    }

    /**
     * 毫秒数转日期串
     * @param time
     * @return
     */
    public static String dateFormat(long time) {
        return format(new Date(time), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 转换日期串格式
     * @param dateStr
     * @param oldFromat
     * @param newFormat
     * @return
     */
    public static String format(String dateStr, String oldFromat, String newFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(oldFromat);
                Date date = e.parse(dateStr);
                return format(date, newFormat);
            }
        } catch (Exception var5) {
            throw new RuntimeException("日期格式化转换失败", var5);
        }
    }

    /**
     * 格式化日期串为Calendar
     * @param dateStr
     * @param dateStrFormat
     * @return
     */
    public static Calendar format(String dateStr, String dateStrFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(dateStrFormat);
                Date date = e.parse(dateStr);
                Calendar ca = GregorianCalendar.getInstance();
                ca.setTime(date);
                return ca;
            }
        } catch (Exception var5) {
            throw new RuntimeException("日期格式化转换失败", var5);
        }
    }

    /**
     * 格式化日期串为Date
     * @param dateStr
     * @param dateStrFormat
     * @return
     */
    public static Date dateStr2Date(String dateStr, String dateStrFormat) {
        try {
            if (StringUtils.isBlank(dateStr)) {
                return null;
            } else {
                SimpleDateFormat e = new SimpleDateFormat(dateStrFormat);
                return e.parse(dateStr);
            }
        } catch (Exception var3) {
            throw new RuntimeException("日期格式化转换失败", var3);
        }
    }

    /**
     * 指定时间添加日
     * @param date
     * @param amount
     * @return
     */
    public static Date addDays(Date date, int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 指定时间添加小时
     * @param date
     * @param amount
     * @return
     */
    public static Date addHours(Date date, int amount) {
        return add(date, 11, amount);
    }

    /**
     * 指定时间减小时
     * @param date
     * @param amount
     * @return
     */
    public static Date reduceHours(Date date, int amount) {
        if (null == date) {
            return null;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            if (amount < 0) {
                cal.add(11, amount);
            } else {
                cal.add(11, -amount);
            }

            return cal.getTime();
        }
    }

    /**
     * 指定时间添加秒
     * @param date
     * @param amount
     * @return
     */
    public static Date addSeconds(Date date, int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    public static Date addMilliSeconds(Date date, int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 当前时间添加秒
     * @param amount
     * @return
     */
    public static Date addSeconds(int amount) {
        return add(new Date(), Calendar.SECOND, amount);
    }

    /**
     * 添加指定时间， field(单位)为Calendar.***
     * @param date
     * @param field
     * @param amount
     * @return
     */
    private static Date add(Date date, int field, int amount) {
        if (null == date) {
            return null;
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(field, amount);
            return cal.getTime();
        }
    }

    /**
     * 获取月的第几天
     * @param date
     * @return
     */
    public static int getDayOfMonth(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 获取dataStr(格式为yyyy-MM-dd)的最早时间0:0:0
     * @param dateStr
     * @return
     */
    public static String formatDate2DateTimeStart(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return format(calendar.getTime());
    }

    /**
     * 获取dataStr(格式为yyyy-MM-dd)的最晚时间23:59:59.999
     * @param dateStr
     * @return
     */
    public static String formatDate2DateTimeEnd(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return format(calendar.getTime());
    }

    /**
     * 获取dataStr(格式为yyyy-MM-dd)的最晚时间23:59:59，精确到秒
     * @param dateStr
     * @return
     */
    public static String formatDate2DateTimeEndSecond(String dateStr) {
        Calendar calendar = format(dateStr, "yyyy-MM-dd");
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return format(calendar.getTime());
    }

    /**
     * 获取date的最早时间0:0:0
     * @param date
     * @return
     */
    public static Date formatDate2DateTimeStart(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            return calendar.getTime();
        }
    }

    /**
     * 获取date的最晚时间23:59:59
     * @param date
     * @return
     */
    public static Date formatDate2DateTimeEnd(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            return calendar.getTime();
        }
    }

    /**
     * 获取date的最晚时间23:59:59，精确到秒
     * @param date
     * @return
     */
    public static Date formatDate2DateTimeEndSecond(Date date) {
        if (date == null) {
            return null;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            return calendar.getTime();
        }
    }

    /**
     * 字符串转日期，格式为yyyy-MM-dd HH:mm:ss
     * @param date
     * @return
     */
    public static Date formatDateStr2Date(String date) {
        return dateStr2Date(date, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 获取当前时间
     * @return
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * 添加分钟数
     * @param nextDate
     * @param amount
     * @return
     */
    public static Date addMinutes(Date nextDate, int amount) {
        return add(new Date(), 12, amount);
    }

    /**
     * 判断两个日期是否相等
     * @param time1
     * @param time2
     * @return
     */
    public static boolean equals(Date time1, Date time2) {
        return (time1 != null || time2 == null) && (time1 == null || time2 != null) ? (time1 == null && time2 == null ? true : StringUtils.equals(format(time1), format(time2))) : false;
    }

    /**
     * date2比date1多的天数
     * 只是通过日期来进行比较两个日期的相差天数的比较，没有精确到相差到一天的时间。如果是只是纯粹通过日期（年月日）来比较比较的话就是方式一。
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);

        //如果date1比date2靠后，交换年和日
        if(date1.after(date2)){
            int tmp = year1;
            year1 = year2;
            year2 = tmp;
            tmp = day1;
            day1 = day2;
            day2 = tmp;
        }
        //不同年
        if (year1 != year2){
            int timeDistance = 0;

            for (int i = year1; i < year2; i++) {
                if (i % 4 == 0 && i % 100 != 0 || i % 400 == 0)    //闰年
                {
                    timeDistance += 366;
                } else    //不是闰年
                {
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2 - day1);
        }//同一年
        else {
            return day2 - day1;
        }
    }

    /**
     * date2比date1多的天数
     * 通过时间秒毫秒数判断两个时间的间隔
     * 是通过计算两个日期相差的毫秒数来计算两个日期的天数差的。一样会有一个小问题，就是当他们相差是23个小时的时候，它就不算一天了。如下面的两个日期
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDaysByMillisecond(Date date1,Date date2){
        return (int) ((date2.getTime() - date1.getTime()) / (1000*3600*24));
    }

    /**
     * date转换为LocalDateTime
     * @param date
     * @return
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }


    /**
     * java.util.Date --> java.time.LocalDate
     * @param date
     * @return
     */
    public static LocalDate dateToLocalDate(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalDate();
    }

    /**
     * java.util.Date --> java.time.LocalTime
     * @param date
     * @return
     */
    public LocalTime dateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }


    /**
     * java.time.LocalDateTime --> java.util.Date
     * @param localDateTime
     * @return
     */
    public static Date localDateTimeToUdate(LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }


    /**
     * java.time.LocalDate --> java.util.Date
     * @param localDate
     * @return
     */
    public static Date localDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }

    /**
     * java.time.LocalTime --> java.util.Date
     */
    public static Date localTimeToUdate(LocalTime localTime) {
        LocalDate localDate = LocalDate.now();
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
