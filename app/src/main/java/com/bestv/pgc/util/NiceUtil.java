package com.bestv.pgc.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.TypedValue;
import android.view.WindowManager;

import java.util.Formatter;
import java.util.Locale;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

/**
 * Created by ren on 2017/5/8.
 * 工具类.
 */
public class NiceUtil {
    /**
     * Get activity from context object
     *
     * @param context something
     * @return object of Activity or null if it is not Activity
     */
    public static Activity scanForActivity(Context context) {
        if (context == null) return null;
        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * Get AppCompatActivity from context
     *
     * @param context
     * @return AppCompatActivity if it's not null
     */
    public static AppCompatActivity getAppCompActivity(Context context) {
        if (context == null) return null;
        if (context instanceof AppCompatActivity) {
            return (AppCompatActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getAppCompActivity(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    @SuppressLint("RestrictedApi")
    public static void showActionBar(Context context) {
        AppCompatActivity appCompActivity = getAppCompActivity(context);
        if (appCompActivity != null) {
            ActionBar ab = appCompActivity.getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.show();
            }
        }
            scanForActivity(context)
                    .getWindow()
                    .clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @SuppressLint("RestrictedApi")
    public static void hideActionBar(Context context) {
        AppCompatActivity appCompActivity = getAppCompActivity(context);
        if (appCompActivity != null) {
            ActionBar ab = appCompActivity.getSupportActionBar();
            if (ab != null) {
                ab.setShowHideAnimationEnabled(false);
                ab.hide();
            }
        }

        scanForActivity(context)
                    .getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return width of the screen.
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * dp转px
     *
     * @param context
     * @param dpVal   dp value
     * @return px value
     */
    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 将毫秒数格式化为"##:##"的时间
     *
     * @param milliseconds 毫秒数
     * @return ##:##
     */
    public static String formatTime(int milliseconds) {
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00";
        }
        int totalSeconds = milliseconds / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        StringBuilder stringBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(stringBuilder, Locale.getDefault());
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }


    /**
     * Convert the time in seconds to "00:00:00" style.
     *
     * @param time
     *            The time in seconds.
     * @return The formated style like "00:00:00".
     */
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = "00:" + unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":"
                        + unitFormat(second);
            }
        }
        return timeStr;
    }


    /**
     * Make sure if the parameter is less than 10 to add "0" before it.
     *
     * @param i
     *            The number to be formatted.
     * @return The formatted number like "00" or "12";
     */
    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else if (i >= 10 && i <= 60) {
            retStr = "" + i;
        } else {
            retStr = "00";
        }
        return retStr;
    }
}

