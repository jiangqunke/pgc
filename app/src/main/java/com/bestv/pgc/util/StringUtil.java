package com.bestv.pgc.util;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtil {
    /**
     * 将一个InputStream流转换成字符串，UTF-8
     */
    public static String inputStream2String(InputStream is, String code) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(is, code));
        int i;
        char[] b = new char[1000];
        StringBuffer sb = new StringBuffer();

        while ((i = in.read(b)) != -1) {
            sb.append(new String(b, 0, i));
        }
        String content = sb.toString();
        return content;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static String getNoNull(String input) {
        if (TextUtils.isEmpty(input)) {
            return "";
        } else {
            return input;
        }
    }


    public static String getNum(String num) {
        if (TextUtils.isEmpty(num)) {
            return "0";
        } else {
            float count = Float.valueOf(num);
            if (count >= 10000) {
                return ((float) (Math.round((count / 10000) * 10)) / 10) + "w";
            } else {
                return num;
            }
        }
    }
}
