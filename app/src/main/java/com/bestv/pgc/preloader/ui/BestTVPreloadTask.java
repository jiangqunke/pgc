package com.bestv.pgc.preloader.ui;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.locks.ReentrantLock;

import static android.os.Process.setThreadPriority;

import com.bestv.pgc.preloader.ui.util.AndroidUtils;
import com.bestv.pgc.preloader.videocache.file.FileNameGenerator;

public class BestTVPreloadTask implements Runnable {
    private static final String TAG = BestTVPreloadTask.class.getSimpleName();

    public static final int STATUS_INIT = 0;
    public static final int STATUS_PRELOADING = 1;
    public static final int STATUS_LOADING = 2;
    public static final int STATUS_COMPLETED = 3;
    public static final int STATUS_CANCEL = 4;

    private final BestTVPreloader mPreloadManager;
    private final FileNameGenerator mFileNameGenerator;
    private volatile int mStatus = STATUS_INIT;
    private volatile String mUrl;
    private volatile String mCacheKey;
    private volatile int mIndex;
    private volatile long mPreloadLength = 1024 * 1024L;

    private ITaskCallback mTaskCallback;
    private final ReentrantLock mLock = new ReentrantLock();

    public BestTVPreloadTask(BestTVPreloader preloadManager, final String url, final int mIndex) {
        this.mPreloadManager = preloadManager;
        this.mUrl = url;
        this.mIndex = mIndex;
        this.mFileNameGenerator = preloadManager.getFileNameGenerator();
        if (mFileNameGenerator != null) {
            this.mCacheKey = mFileNameGenerator.generate(url);
        } else {
            this.mCacheKey = AndroidUtils.textToMD5(url);
        }
    }

    public BestTVPreloadTask(BestTVPreloader preloadManager, final String url, final int mIndex, final long preloadLength) {
        this(preloadManager, url, mIndex);
        this.mPreloadLength = preloadLength;
    }

    public void init(String url, int index) {
        mLock.lock();
        try {
            this.mUrl = url;
            this.mIndex = index;
            if (this.mFileNameGenerator != null) {
                this.mCacheKey = mFileNameGenerator.generate(url);
            } else {
                this.mCacheKey = AndroidUtils.textToMD5(url);
            }
            this.mStatus = STATUS_INIT;
        } finally {
            mLock.unlock();
        }
    }

    public void setStatus(int mStatus) {
        mLock.lock();
        try {
            this.mStatus = mStatus;
            Log.d("TTTT", "status change1 " + this.mStatus + " index: " + mIndex);
        } finally {
            mLock.unlock();
        }
    }

    public void setTaskCallback(ITaskCallback callback) {
        this.mTaskCallback = callback;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getCacheKey() {
        return mCacheKey;
    }

    public int getIndex() {
        return mIndex;
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof BestTVPreloadTask) {
            //Log.d(PreLoadManager.TAG, "equals [" + this.mUrl + ", " + ((BestTVPreloadTask)obj).mUrl + "]");
            return !TextUtils.isEmpty(this.mUrl) && this.mUrl.equals(((BestTVPreloadTask)obj).mUrl);
        }
        //Log.d(PreLoadManager.TAG, "two PreLoadTask not equal");
        return false;
    }

    /**
     * 此处没涉及map/set操作,涉及需要重写该方法
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void run() {
        setThreadPriority(mPreloadManager.getCurrentConfig().downloadThreadPriority);

        final String currentThreadName = Thread.currentThread().getName();
        Log.d(TAG, currentThreadName + "----task run begin----");
        if (mStatus == STATUS_CANCEL) {
            Log.d(TAG, currentThreadName + " has cancel");
            finish();
            return;
        }

        if (TextUtils.isEmpty(this.mUrl)) {
            Log.d(TAG, currentThreadName + " url is empty");
            finish();
            return;
        }

        mStatus = STATUS_PRELOADING;
        preloadInternal(currentThreadName);

        Log.d(TAG, currentThreadName + "----task run end----");
    }

    private void preloadInternal(final String currentThreadName) {
        if (mStatus != STATUS_PRELOADING) {
            Log.d(TAG, currentThreadName + "preload() " + "status is: " + mStatus);
            return;
        }

        if (mPreloadManager.hasEnoughCache(this.mUrl)) {
            Log.d(TAG, currentThreadName + "videoId " + mUrl + " has enough cache");
            finish();
            return;
        }

        InputStream inputStream  = null;
        long start = System.currentTimeMillis();
        boolean flag = false;
        try {
            URL url = new URL(mPreloadManager.getLocalProxyUrl(this.mUrl));
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Range","bytes=0-2048000");
            urlConnection.setConnectTimeout(5000);
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            mStatus = STATUS_LOADING;
            Log.d(TAG, currentThreadName + "PreLoadTask run: loading" );
            int bufferSize = 8192;
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

                if (length >= mPreloadLength) {
                    Log.d(TAG, currentThreadName + " downloaded length: " + length + "");
                    mStatus = STATUS_COMPLETED;
                }
            }

            if (mStatus == STATUS_CANCEL) {
                Log.d("TTTT", currentThreadName + "task cancel!");
            }

            inputStream.close();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage() + "");
        }  catch (Exception e) {
            Log.d(TAG, e.getMessage() + "");
        } finally {
            Log.d(TAG, currentThreadName + "preload video url [url: " + this.mUrl + ", time: "
                    + (System.currentTimeMillis() - start) + "ms, index: " + this.mIndex + "， status: " + this.mStatus + "]");

            finish();
        }
    }

    private void finish() {
        if (mTaskCallback != null) {
            mTaskCallback.finish();
        }
    }

    interface ITaskCallback {
        void finish();
    }
}
