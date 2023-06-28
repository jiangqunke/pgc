package com.bestv.pgc.net;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.bestv.pgc.util.BestvAgent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/8
 * 简单的 OkHttpUtils 实现
 */

public final class OkHttpUtils {

    private static final String TAG = "OkHttpUtils";
    private static final int REQUST_OK = 200;
    private static volatile OkHttpUtils mInstance;
    private static OkHttpClient mHttpClient;
    //调试模式开关
    public static boolean DEBUG = false;
    //是否正在请求
    public static boolean isRequst = false;
    //共有参数
    private static HashMap<String, String> mDefaultParams;
    //子线程
    private HandlerThread mHandlerThread;
    private Handler mThreadHandler;
    //主线程
    private static Handler mHandler;
    private static Gson mGson;
    /**
     * API错误码
     */
    //IO异常，一般发生在同步请求下
    public static final int ERROR_IO = 3000;
    //OkHttpUtils内部异常
    public static final int ERROR_INVALID = 3001;
    //返回正常但数据为空
    public static final int ERROR_EMPTY = 3002;
    //JSON解析失败
    public static final int ERROR_JSON_FORMAT = 3003;
    /**
     * 下载APK错误码
     */
    //IO异常
    public static final int UPDATE_IO_ERROR = 300;
    //网络异常
    public static final int UPDATE_NET_ERROR = 400;
    //APK异常
    public static final int UPDATE_APK_ERROR = 500;
    //操作异常
    public static final int UPDATE_HANDLE_ERROR = 600;
    //权限异常
    public static final int UPDATE_PERMISSION_ERROR = 700;

    //是否继续下载
    private static boolean mDownload = true;

    public static OkHttpUtils getInstance() {
        if (null == mInstance) {
            synchronized (OkHttpUtils.class) {
                if (null == mInstance) {
                    mInstance = new OkHttpUtils();
                }
            }
        }
        return mInstance;
    }

    private OkHttpUtils() {
        mHttpClient = createHttpUtils();
        mGson = new Gson();
        mHandler = new Handler(Looper.getMainLooper());
        DEBUG =! BestvAgent.getInstance().isOficial();
    }

    /**
     * 创建HttpClient
     *
     * @return HttpClient example
     */
    private OkHttpClient createHttpUtils() {
        if (null == mHttpClient) {
            synchronized (OkHttpClient.class) {
                mHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .cookieJar(new CookieJar() {
                            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

                            @Override
                            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                                cookieStore.put(url.host(), cookies);
                            }

                            @Override
                            public List<Cookie> loadForRequest(HttpUrl url) {
                                List<Cookie> cookies = cookieStore.get(url.host());
                                return cookies != null ? cookies : new ArrayList<Cookie>();
//                                List<Cookie> cookies = cookieStore.get(url.host());
//                                List<Cookie> newCookies = new ArrayList<>();
//                                if (cookies!= null){
//                                    for (Cookie cookie : cookies){
//                                        if (cookie.value().contains("username")){
//                                            newCookies.add(cookie);
//                                            return newCookies;
//                                        }
//                                    }
//                                }
//                                return new ArrayList<Cookie>();
//                                List<Cookie> newCookies = new ArrayList<>();
//                               if (cookies!=null){
//                                   newCookies.add(cookies.get(0));
//                                   cookies.get(0).value()
//                               }
//                               if (newCookies.size()>0){
//                                   return newCookies;
//                               }else {
//                                   return new ArrayList<Cookie>();
//                               }
//                                return cookies != null ? cookies.get(0) : new ArrayList<Cookie>();
                            }
                        })
                        .build();
            }
        }
        return mHttpClient;
    }

    /**
     * 返回一个子线程Handler
     *
     * @return 子线程Handler
     */
    private Handler getThreadHandler() {
        if (null == mHandlerThread) {
            mHandlerThread = new HandlerThread("okHttpUtils");
            mHandlerThread.start();
            mThreadHandler = new Handler(mHandlerThread.getLooper());
        }
        return mThreadHandler;
    }

    /**
     * 设置默认参数
     *
     * @param defaultParams 键值对参数
     */
    public static void setDefaultParams(HashMap<String, String> defaultParams) {
        mDefaultParams = defaultParams;
    }


    /**
     * 对外的异步方法  GET Requst
     *
     * @param url      host api
     * @param callBack 回调
     */
    public static void get(String url, OnResultCallBack callBack) {
        get(url, null, null, callBack);
    }

    /**
     * 对外的异步方法  GET Requst
     *
     * @param url      host api
     * @param params   键值对参数
     * @param callBack 回调
     */
    public static void get(String url, Map<String, String> params, OnResultCallBack callBack) {
        OkHttpUtils.getInstance().sendGetRequst(url, params, null, callBack, false);
    }


    /**
     * 对外的异步方法  GET Requst
     *
     * @param url      host api
     * @param params   键值对参数
     * @param headers  键值对Header
     * @param callBack 回调
     */
    public static void get(String url, Map<String, String> params, Map<String, String> headers,
                           OnResultCallBack callBack) {
        OkHttpUtils.getInstance().sendGetRequst(url, params, headers, callBack, false);
    }

    /**
     * 对外的异同步方法  GET Requst
     *
     * @param url      host api
     * @param params   键值对参数
     * @param headers  键值对Header
     * @param callBack 回调
     */
    public static void getSynchro(String url, Map<String, String> params, Map<String, String> headers,
                                  OnResultCallBack callBack) {
        OkHttpUtils.getInstance().sendGetRequst(url, params, headers, callBack, true);
    }

    /**
     * 对外的异步方法  POST Requst
     *
     * @param url      host api
     * @param callBack 回调
     */
    public static void post(String url, OnResultCallBack callBack) {
        post(url, null, null, callBack);
    }

    /**
     * 对外的异步方法 POST Requst
     *
     * @param url      host api
     * @param params   键值对参数
     * @param callBack 回调
     */
    public static void post(String url, Map<String, Object> params, OnResultCallBack callBack) {
        String jsonParam = new Gson().toJson(params);
        if (DEBUG){
            Log.d(TAG, "the request url-->" + url);
            Log.d(TAG, "the request params-->" + jsonParam);
        }
        OkHttpUtils.getInstance().sendPostRequstNoKey(url, jsonParam, null, callBack, false);
    }

    /**
     * 对外的异步方法 POST Requst
     *
     * @param url      host api
     * @param params   键值对参数
     * @param headers  键值对Header
     * @param callBack 回调
     */
    public static void post(String url, Map<String, String> params, Map<String, String> headers,
                            OnResultCallBack callBack) {
        OkHttpUtils.getInstance().sendPostRequst(url, params, headers, callBack, false);
    }



    /**
     * 发送GET请求
     *
     * @param url       host api
     * @param params    键值对参数
     * @param headers   键值对Header
     * @param callBack  回调
     * @param isSynchro 是否同步调用
     */
    private void sendGetRequst(String url, Map<String, String> params, Map<String, String> headers, OnResultCallBack
            callBack, boolean isSynchro) {

        Request.Builder builder = new Request.Builder();
        //初始化共有参数
        if (null == params) {
            params = new HashMap<>();
        }
        if (null != mDefaultParams) {
            params.putAll(mDefaultParams);
        }
        //构建请求参数
        if (null != params && params.size() > 0) {
            url = buildParamsToUrl(url, params);
        }
        builder.url(url);
        //构建Header
        builder = buildHeaderToRequest(builder, headers);
        final Request request = builder.build();
        setdRequst(request, callBack, isSynchro);
    }

    /**
     * 发送POST请求
     *
     * @param url       host api
     * @param params    键值对参数
     * @param headers   键值对Header
     * @param callBack  回调
     * @param isSynchro 是否同步调用
     */
    private void sendPostRequst(String url, Map<String, String> params, Map<String, String> headers,
                                OnResultCallBack callBack, boolean isSynchro) {
        //构建请求Body参数
        RequestBody requestBody = formatPostParams(params);
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(requestBody);
        //构建Header参数
        builder = buildHeaderToRequest(builder, headers);
        setdRequst(builder.build(), callBack, isSynchro);
    }

    /**
     * 发送POST请求
     *
     * @param url       host api
     * @param headers   键值对Header
     * @param callBack  回调
     * @param isSynchro 是否同步调用
     */
    private void sendPostRequstNoKey(String url, String json, Map<String, String> headers,
                                     OnResultCallBack callBack, boolean isSynchro) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        //构建请求Body参数
        RequestBody requestBody = RequestBody.create(JSON, json);
        Request.Builder builder = new Request.Builder();
        builder.url(url).post(requestBody);
        //构建Header参数
        builder = buildHeaderToRequest(builder, headers);
        setdRequst(builder.build(), callBack, isSynchro);
    }

    /**
     * 根据MAP构建URL
     *
     * @param url    原URL
     * @param params 参数
     * @return 组合后的URL
     */
    private String buildParamsToUrl(String url, Map<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder(url);
        if (null != params && params.size() > 0) {
            boolean hasParams = false;
            if (url.contains("?")) {
                //如果URL中已经存在参数，标记一下
                hasParams = true;
            }
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                if (i == 0 && !hasParams) {
                    //如果不存在参数，拼接第一个参数
                    stringBuilder.append("?" + next.getKey() + "=" + next.getValue());
                } else {
                    stringBuilder.append("&" + next.getKey() + "=" + next.getValue());
                }
                i++;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 添加Body参数
     *
     * @param params 键值对参数
     * @return RequestBody
     */
    private RequestBody formatPostParams(Map<String, String> params) {
        //初始化共有参数
        if (null == params) {
            params = new HashMap<>();
        }
        if (null != mDefaultParams) {
            params.putAll(mDefaultParams);
        }
        if (null != params && params.size() > 0) {
            FormBody.Builder builder = new FormBody.Builder();
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                builder.add(next.getKey(), next.getValue());
            }
            return builder.build();
        }
        return null;
    }

    /**
     * 添加Header参数
     *
     * @param builder requst
     * @param headers 头部参数
     * @return 设置参数后的builder
     */
    private Request.Builder buildHeaderToRequest(Request.Builder builder, Map<String, String> headers) {
        if (null != headers && headers.size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> next = iterator.next();
                builder.header(next.getKey(), next.getValue());
            }
        }
        return builder;
    }

    /**
     * 发送请求
     *
     * @param request   requst
     * @param callBack  callBack
     * @param isSynchro 是否同步调用
     */
    private void setdRequst(final Request request, final OnResultCallBack callBack,
                            boolean isSynchro) {
        if (DEBUG) {
            Log.d(TAG, "setdRequst-->URL:" + request.url() + ",isSynchro:" + isSynchro);
        }
        if (null == mHandler) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        //同步请求
        if (isSynchro) {
            getThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        isRequst = true;
                        Response response = createHttpUtils().newCall(request).execute();
                        formatResponse(response, callBack);
                    } catch (final IOException e) {
                        e.printStackTrace();
                        isRequst = false;
                        error(callBack, ERROR_IO, e.getMessage());
                    } catch (final RuntimeException e) {
                        e.printStackTrace();
                        isRequst = false;
                        error(callBack, ERROR_IO, e.getMessage());
                    }
                }
            });
            return;
        }
        //异步请求
        isRequst = true;
        createHttpUtils().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (DEBUG) Log.d(TAG, "onFailure-->e:" + e.getMessage() + call.toString());
                isRequst = false;
                error(callBack, ERROR_IO, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                formatResponse(response, callBack);
            }
        });
    }

    /**
     * 解析Response
     *
     * @param response response响应体
     * @param callBack callBack回调
     */
    private void formatResponse(final Response response, final OnResultCallBack callBack) {
        isRequst = false;
        if (null != mHandler && null != callBack) {
            if (null != response) {
                if (REQUST_OK == response.code()) {
                    try {
                        String string = response.body().string();
                        response.close();
                        if (!TextUtils.isEmpty(string)) {
                            if (DEBUG) Log.d(TAG, "the response data-->" + string);
                            final Object resultInfo = getResultInfo(string, callBack.getType());
                            if (null != mHandler && null != callBack) {
                                if (null != resultInfo) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            callBack.onResponse(resultInfo);
                                        }
                                    });
                                } else {
                                    error(callBack, ERROR_JSON_FORMAT, string);
                                }
                            }
                        } else {
                            int code = response.code();
                            response.close();
                            error(callBack, code, "body is empty");
                        }
                    } catch (final IOException e) {
                        e.printStackTrace();
                        error(callBack, ERROR_JSON_FORMAT, e.getMessage());
                    } catch (final RuntimeException e) {
                        e.printStackTrace();
                        error(callBack, ERROR_JSON_FORMAT, e.getMessage());
                    }
                } else {
                    int code = response.code();
                    String message = response.message();
                    response.close();
                    error(callBack, code, message);
                }
            } else {
                error(callBack, ERROR_EMPTY, "response is empty");
            }
        }
    }

    /**
     * 异常抛出
     *
     * @param callBack  回调
     * @param errorCode 错误码
     * @param msg       描述信息
     */
    private void error(final OnResultCallBack callBack, final int errorCode, final String msg) {
        if (null != mHandler && null != callBack) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(errorCode,
                            msg);
                }
            });
        }
    }

    /**
     * JSON解析,这里不再返回T了，直接Object
     *
     * @param json json字符串
     * @param type 指定class
     * @return 泛型实体对象
     */
    public Object getResultInfo(String json, Type type) {
        Object resultData = null;
        if (null == mGson) {
            mGson = new Gson();
        }
        try {
            if (null != type) {
                resultData = mGson.fromJson(json, type);
            } else {
                //如果用户没有指定Type,则直接使用String.class
                resultData = mGson.fromJson(json, new TypeToken<String>() {
                }.getType());
            }
            return resultData;
        } catch (RuntimeException e) {
            e.printStackTrace();
            return resultData;
        }
    }


    /**
     * 取消下载
     */
    public static void cancelDownload() {
        mDownload = false;
    }

    /**
     * 释放、销毁
     */
    public void onDestroy() {
        if (null != mHttpClient) {
            mHttpClient = null;
        }
        isRequst = false;
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.removeMessages(0);
            mHandler = null;
        }
        if (null != mThreadHandler) {
            mThreadHandler.removeCallbacksAndMessages(null);
            mThreadHandler.removeMessages(0);
            mThreadHandler = null;
        }
        if (null != mHandlerThread) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        mGson = null;
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;//trust All的写法就是在这里写的，直接无视hostName，直接返回true，表示信任所有主机
        }

    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    private static class TrustAllCerts implements X509TrustManager {

        //checkServerTrusted和checkClientTrusted 这两个方法好像是用于，server和client双向验证
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}