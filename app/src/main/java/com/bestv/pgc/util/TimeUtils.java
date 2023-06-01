package com.bestv.pgc.util;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class TimeUtils {


    public static boolean updateTime(long starttime, long endtime) {
        Date startdate = new Date(starttime);
        Date endtate = new Date(endtime);
        long time = (endtate.getTime() - startdate.getTime()) / 1000;
        if (time > 18 * 3600) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * 登录活跃是否超过24小时
     *
     * @param starttime
     * @param endtime
     * @return
     */
    public static boolean LoginDialogTime(long starttime, long endtime) {
        Date startdate = new Date(starttime);
        Date endtate = new Date(endtime);
        long time = (endtate.getTime() - startdate.getTime()) / 1000;
        if (time > 24 * 3600) {
            return true;
        } else {
            return false;
        }

    }

    public static String getCurTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date());
    }

    public static boolean isSameData(long nowLong, long dataLong) {
        try {
//            nowLong = nowLong * 1000;
//            dataLong = dataLong * 1000;
            Calendar nowCal = Calendar.getInstance();
            Calendar dataCal = Calendar.getInstance();
            SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
            String data1 = df1.format(nowLong);
            String data2 = df2.format(dataLong);
            Date now = df1.parse(data1);
            Date date = df2.parse(data2);
            nowCal.setTime(now);
            dataCal.setTime(date);
            return isSameDay(nowCal, dataCal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 != null && cal2 != null) {
            return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                    && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                    && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }
    }

    public static String getStandardTime(long timeInMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        return simpleDateFormat.format(new Date(timeInMillis));
    }


    //当前时间小于结束时间
    public static boolean compareDate(String nowDate, String compareDate) {
        Log.e("time", nowDate + "---" + compareDate);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date now = df.parse(nowDate);
            Date compare = df.parse(compareDate);
            if (now.before(compare)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Date parseTimeString2Date(String timeString) {
        if ((timeString == null) || (timeString.equals(""))) {
            return null;
        }
        Date date = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = new Date(dateFormat.parse(timeString).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String convertDate2String(String time) {
        if(!TextUtils.isEmpty(time)){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = sdf.parse(time);
                String str = sdf2.format(date);
                return str;
            } catch (ParseException e) {
                e.printStackTrace();
                return "";
            }
        }else {
            return "";
        }

    }
}
