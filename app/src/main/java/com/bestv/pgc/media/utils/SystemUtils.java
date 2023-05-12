package com.bestv.pgc.media.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

public class SystemUtils {

    //关闭系统自动亮度调节
    public static void closeAutoBrightness(Context context) {
        Activity activity = (Activity) context;
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

    //打开系统自动亮度调节
    public static void openAutoBrightness(Context context) {
        Activity activity = (Activity) context;
        setScreenBritness(context, -1);
        Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    //获取系统亮度
    public static int getSystemScreenBrightness(Context context) {
        Activity activity = (Activity) context;
        try {
            return Settings.System.getInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //设置当前窗口的亮度
    public static void setWindowBrightness(Context context, float brightness) {
        Activity activity = (Activity) context;
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);
    }

    //获取当前窗口的亮度
    public static float getWindowBrightness(Context context) {
        Activity activity = (Activity) context;
        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        return lp.screenBrightness;
    }


    //判断是否设置自动亮度调节
    public static boolean isAutoBrightness(Context context) {
        try {
            int autoBrightness = Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (autoBrightness == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                return true;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //设置当前屏幕亮度并保存
    public static void setScreenBritness(Context context, int brightness) {
        Activity activity = (Activity) context;
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            if (brightness < 10) {
                brightness = 10;
            }
            lp.screenBrightness = Float.valueOf(brightness / 255f);
            Settings.System.putInt(activity.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
        activity.getWindow().setAttributes(lp);
    }

    //获取系统最大音量
    public static int getSystemMAXVolume(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);// 3
        return max;
    }

    //获取系统当前音量
    public static int getSystemCurrentVolume(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);// 3
        return current;
    }

    //设置系统当前音量
    public static void setSystemVolume(Context context, int index) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC,index, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
}
