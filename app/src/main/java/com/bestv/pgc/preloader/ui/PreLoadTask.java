package com.bestv.pgc.preloader.ui;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import com.bestv.pgc.preloader.ui.util.AndroidUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhipeng.zhuo
 * @date 2020-05-14
 */
public class PreLoadTask implements Runnable {

    public static final int STATUS_INIT = 0;
    public static final int STATUS_PRELOADING = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CANCEL = 4;

    private volatile int mStatus = STATUS_INIT;
    private volatile String mUrl;
    private volatile int mIndex;
    private volatile String cacheKey;

    private Context context;

    private ITaskCallback iTaskCallback;
    private ReentrantLock lock = new ReentrantLock();
    private Condition waitCondition = lock.newCondition();

    public PreLoadTask(Context context, final String url, final int mIndex) {
        this.context = context;
        this.mUrl = url;
        this.mIndex = mIndex;
        if (!TextUtils.isEmpty(url)) {
            this.cacheKey = AndroidUtils.textToMD5(url);
        }
    }

    public void init(String url, int index) {
        lock.lock();
        try {
            this.mUrl = url;
            this.mIndex = index;
            this.cacheKey = AndroidUtils.textToMD5(url);
            this.mStatus = STATUS_INIT;
        } finally {
            lock.unlock();
        }
    }

    public void setiTaskCallback(ITaskCallback callback) {
        this.iTaskCallback = callback;
    }

    public void setStatus(int mStatus) {
        lock.lock();
        try {
            this.mStatus = mStatus;
            Log.d("TTTT", "status change1 " + this.mStatus + " index: " + mIndex);
        } finally {
            lock.unlock();
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public int getIndex() {
        return mIndex;
    }

    @Override
    public void run() {
        Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "----task run begin----");
        if (mStatus == STATUS_CANCEL) {
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + " has cancel");
            finish();
            return;
        }

        if (TextUtils.isEmpty(this.mUrl)) {
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + " url is empty");
            finish();
            return;
        }

        mStatus = STATUS_PRELOADING;
        preloadInternal();

        Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "----task run end----");
    }

    private void preloadInternal() {
        if (mStatus != STATUS_PRELOADING) {
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "preload() " + "status is: " + mStatus);
            return;
        }

        if (PreLoadManager.getInstance(context).hasEnoughCache(this.mUrl)) {
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "videoId " + mUrl + " has enough cache");
            finish();
            return;
        }

        InputStream inputStream  = null;
        long start = System.currentTimeMillis();
        boolean flag = false;
        try {
            URL url = new URL(PreLoadManager.getInstance(context).getLocalUrlAppendWithUrl(this.mUrl));
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Range","bytes=0-2048000");
            urlConnection.setConnectTimeout(5000);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            mStatus = STATUS_LOADING;
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "PreLoadTask run: loading" );
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int length = 0;
            int tmp = 0;
            while (mStatus == STATUS_LOADING && (tmp = inputStream.read(buffer)) != -1) {
                //Since we just need to kick start the prefetching, dont need to do anything here
                //  or we can use ByteArrayOutputStream to write down the data to disk
                length += tmp;
                //Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + " downloaded length: " + length + "");
                if (!flag) {
                    Log.d("TTTT", "status change2: " + mStatus + " index: " + mIndex);
                    flag = true;
                }

                if (length >= 2048000) {
                    Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + " downloaded length: " + length + "");
                    mStatus = STATUS_COMPLETED;
                }
            }

            if (mStatus == STATUS_CANCEL) {
                Log.d("TTTT", Thread.currentThread().getName() + "task cancel!");
            }

            inputStream.close();
        } catch (IOException e) {
            Log.d(PreLoadManager.TAG, e.getMessage() + "");
        }  catch (Exception e) {
            Log.d(PreLoadManager.TAG, e.getMessage() + "");
        } finally {
            Log.d(PreLoadManager.TAG, Thread.currentThread().getName() + "preload video url [url: " + PreLoadTask.this.mUrl + ", time: "
                    + (System.currentTimeMillis() - start) + "ms, index: " + PreLoadTask.this.mIndex + "， status: " + this.mStatus + "]");

            finish();
        }

    }

    private void finish() {
        if (iTaskCallback != null) {
            iTaskCallback.finish();
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PreLoadTask) {
//            Log.d(PreloadManager.TAG, "equals [" + this.url + ", " + ((PreLoadTask)obj).url + "]");
            return !TextUtils.isEmpty(this.mUrl) && this.mUrl.equals(((PreLoadTask)obj).mUrl);
        }

//        Log.d(PreloadManager.TAG, "two PreLoadTask not equal");
        return false;
    }

    /**
     * 此处没涉及map/set操作,涉及需要重写该方法
     * */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    interface ITaskCallback {
        void finish();
    }
}

