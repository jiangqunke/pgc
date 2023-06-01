package com.bestv.pgc.preloader.ui;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;


import com.bestv.pgc.preloader.ui.adapter.BestTVNetworkAdapter;
import com.bestv.pgc.preloader.ui.adapter.INetworkAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BestTVPreloadFuture implements LifecycleObserver  {
    private static final String TAG = BestTVPreloadFuture.class.getSimpleName();

    private volatile List<String> mUrls;
    private final String mBusId;
    private String mCurrentUrl;
    private volatile int mCurrentIndex = -1;
    private volatile boolean toPreLoad = false;
    private final ReentrantLock mLock = new ReentrantLock();
    private final Condition empty = mLock.newCondition();
    private final Condition network = mLock.newCondition();
    private final LinkedBlockingDeque<BestTVPreloadTask> mLoadingTaskDeque = new LinkedBlockingDeque<>();
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(8);//Executors.newCachedThreadPool();
    private ConsumerThread mConsumerThread;
    private CurrentLoadingHandler mHandler;
    private final Context mContext;
    private INetworkAdapter mNetworkAdapter;
    private BroadcastReceiver mNetworkReceiver;
    private volatile boolean mIsWifi = false;
    private boolean mHasPause = false;
    /**
     * @param context Activity的context或者app的context
     * @param preloadBusId 每个页面对应一个busId
     * */
    public BestTVPreloadFuture(Context context, String preloadBusId) {
        mContext = context;
        mHandler = new CurrentLoadingHandler(this);

        if (context instanceof Application) {
            throw new RuntimeException("context should not be an Application");
        }

        if (context instanceof LifecycleOwner) {
            ((LifecycleOwner) context).getLifecycle().addObserver(this);
        }

        if (TextUtils.isEmpty(preloadBusId)) {
            throw new RuntimeException("busId should not be empty");
        }

        this.mBusId = preloadBusId;

        BestTVPreloader.getInstance().putFuture(mBusId, this);

        boolean canPreloadIfNotWifi = BestTVPreloader.getInstance().getCurrentConfig().canPreloadIfNotWifi;
        setNetworkAdapter(new BestTVNetworkAdapter(canPreloadIfNotWifi));

        if (mNetworkReceiver == null) {
            mNetworkReceiver = new NetworkBroadcastReceiver();
        }

        if (mContext != null) {
            try {
                mContext.registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            } catch (Exception e) {
                Log.e(TAG, this + "\tregisterReceiver exp:" + e);
            }
        }

        mConsumerThread = new ConsumerThread();
        mConsumerThread.start();
    }

    public void setNetworkAdapter(INetworkAdapter networkAdapter) {
        mNetworkAdapter = networkAdapter;
    }

    /**
     * 向当前预加载任务中添加需要预加载的url
     * @param url 待预加载的url
     */
    public void addUrl(String url) {
        mLock.lock();
        try {
            if (this.mUrls == null) {
                this.mUrls = new ArrayList<>();
            }

            this.mUrls.add(url);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 向当前预加载任务中添加预加载url列表
     * @param urls 预加载url列表
     */
    public void addUrls(List<String> urls) {
        mLock.lock();
        try {
            if (this.mUrls == null) {
                this.mUrls = new ArrayList<>();
            }

            this.mUrls.addAll(urls);
        } finally {
            mLock.unlock();
        }
    }

    /**
     * 向当前预加载任务中更新预加载url列表
     * @param urls 预加载url列表
     */
    public void updateUrls(List<String> urls) {
        mLock.lock();
        try {
            if (this.mUrls != null) {
                this.mUrls.clear();
                this.mUrls.addAll(urls);
            } else {
                this.mUrls = urls;
            }
        } finally {
            mLock.unlock();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        /**
         * 线程进入阻塞
         * */
        Log.d(TAG, "onPause: ");

        if (mNetworkReceiver != null) {
            try {
                if (mContext != null) {
                    mContext.unregisterReceiver(mNetworkReceiver);
                }
            } catch (Exception e) {
                Log.e(TAG, this + "\tunregisterReceiver exp:" + e);
            }
        }

        mLock.lock();
        mHasPause = true;
        try {
            BestTVPreloadTask task;
            while ((task = mLoadingTaskDeque.poll()) != null) {
                task.setStatus(BestTVPreloadTask.STATUS_CANCEL);
            }
        } catch (Exception e) {
            Log.e(TAG, "onPause: " + e.getMessage());
        } finally {
            mLock.unlock();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        /**
         * 唤醒进入阻塞的线程
         * */
        Log.d(TAG, "onResume: ");

        if (mNetworkReceiver == null) {
            mNetworkReceiver = new NetworkBroadcastReceiver();
        }

        if (mContext != null) {
            try {
                mContext.registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            } catch (Exception e) {
                Log.e(TAG, this + "\tregisterReceiver exp:" + e);
            }
        }

        mLock.lock();
        try {
            if (mHasPause && !TextUtils.isEmpty(mCurrentUrl)) {
                toPreLoad = true;
                mHasPause = false;
                Log.d(TAG, "ConsumerThread is notified");
                empty.signal();
            }
        } catch (Exception e) {
            Log.e(TAG, "onResume: " + e.getMessage());
        } finally {
            mLock.unlock();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        BestTVPreloader.getInstance().removeFuture(mBusId);
        /**
         * 关闭线程
         * */
        if (mConsumerThread != null && !mConsumerThread.isInterrupted()) {
            mLock.lock();
            try {
                mConsumerThread.interrupt();
                mCurrentIndex = -1;
                empty.signal();
                BestTVPreloadTask task;
                while ((task = mLoadingTaskDeque.poll()) != null) {
                    task.setStatus(BestTVPreloadTask.STATUS_CANCEL);
                }
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: " + e.getMessage());
            } finally {
                mLock.unlock();
            }
        }
    }

    /**
     * @brief: 获取当前预加载的代理url
     * @param url 当前网络请求的真实url
     * @return 预加载代理播放url
     */
    public String getCurrentProxyUrl(@NonNull String url) {
        return BestTVPreloader.getInstance().getLocalProxyUrl(url);
    }

    /**
     * @brief: 预加载当前url之后的短视频
     * @param url: 当前播放的url地址
     */
    public void preloadForwardUrl(@NonNull String url) {
        mHandler.removeMessages(CurrentLoadingHandler.MSG_PRELOAD_FORWARD);
        Message message = Message.obtain();
        message.what = CurrentLoadingHandler.MSG_PRELOAD_FORWARD;
        message.obj = url;
        mHandler.sendMessage(message);
    }

    private void preloadForwardUrlInternel(String url) {
        mLock.lock();
        try {
            if (mUrls == null || mUrls.size() <= 0) {
                throw new RuntimeException("url list should not be empty");
            }

            if (!mUrls.contains(url)) {
                return;
            }

            mCurrentUrl = url;
            int currentIndex = mUrls.indexOf(url);
            if (currentIndex != - 1 && currentIndex != mCurrentIndex) {
                Log.d(TAG, "currentPlayUrl: [url: " + url + ", index: " + currentIndex + "]");
                mCurrentIndex = currentIndex;
                toPreLoad = true;
                // notify
                Log.d(TAG, "ConsumerThread is notified");
                empty.signal();
            }
        } catch (Exception e) {
            Log.e(TAG, "currentPlayUrl: " + e.getMessage());
        } finally {
            mLock.unlock();
        }
    }

    private boolean isNetWorkConnect() {
        if (mContext == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        boolean isConnect = (netInfo != null && netInfo.isConnected());
        return isConnect;
    }

    private final class ConsumerThread extends Thread {
        @Override
        public void run() {
            //android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            BestTVPreloaderConfig preloaderConfig = BestTVPreloader.getInstance().getCurrentConfig();
            int preloadFilesCount = Math.max(1, preloaderConfig.preloadFilesCount);
            preloadFilesCount = Math.min(10, preloadFilesCount);

            mLock.lock();
            try {
                while (!isInterrupted()) {
                    if (!isNetWorkConnect() || (!mIsWifi && !mNetworkAdapter.canPreLoadIfNotWifi())) {
                        Log.d(BestTVPreloader.TAG, "ConsumerThread is await for" + (isNetWorkConnect() ? " is not wifi " : " network not connect"));
                        network.await();
                    }

                    if (!toPreLoad) {
                        Log.d(BestTVPreloader.TAG, "ConsumerThread is await");
                        empty.await();
                    }

                    if (mCurrentIndex == -1) {
                        continue;
                    }

                    /**
                     * 默认加入队列为
                     * 【max(mCurrentIndex - 3, 0)， min(mCurrentIndex + 4, mUrls.size()-1 )]
                     * */
                    Log.d(BestTVPreloader.TAG, "Consumer thread current index is: " + mCurrentIndex);
                    int firstIndex = mCurrentIndex + 1;//Math.max(0, mCurrentIndex - 3);
                    int lastIndex = Math.min(mCurrentIndex + 1 + preloadFilesCount, mUrls.size() - 1);
                    BestTVPreloadTask preloadTask = null;
                    String url;
                    String cacheKey;
                    for (int i = firstIndex; i < lastIndex; i++) {

                        if (i == mCurrentIndex) {
                            continue;
                        }

                        url = mUrls.get(i);
                        if (TextUtils.isEmpty(url)) {
                            continue;
                        }

                        //++
                        /*
                        boolean preloadTaskExists = false;
                        cacheKey = BestTVPreloader.getInstance().getFileNameGenerator().generate(url);
                        for (BestTVPreloadTask task : mLoadingTaskDeque) {
                            if (task.getIndex() <= mCurrentIndex || task.getIndex() > mCurrentIndex + 1 + preloadFilesCount) {
                                task.setStatus(BestTVPreloadTask.STATUS_CANCEL);
                            }
                            if (task.getCacheKey().equals(cacheKey)) {
                                preloadTaskExists = true;
                                break;
                            }
                        }
                        if (preloadTaskExists)
                            continue;
                        */
                        //--

                        preloadTask = BestTVPreloader.getInstance().createTask(mBusId, url, i);
                        if (!mLoadingTaskDeque.contains(preloadTask)) {
                            if (mLoadingTaskDeque.size() >= 16) {
                                BestTVPreloadTask ingPreloadTask = mLoadingTaskDeque.pollLast();
                                ingPreloadTask.setStatus(BestTVPreloadTask.STATUS_CANCEL);
                                Log.d(BestTVPreloader.TAG, "mLoadingTaskDeque size more than 16, remove index: " + ingPreloadTask.getIndex());
                            }

                            Log.d(BestTVPreloader.TAG, "Put into mLoadingTaskDeque: " + preloadTask.getUrl());
                            mLoadingTaskDeque.addFirst(preloadTask);
                            mExecutorService.submit(preloadTask);
                        } else {
                            mLoadingTaskDeque.remove(preloadTask);
                            mLoadingTaskDeque.addFirst(preloadTask);
                        }
                    }

                    toPreLoad = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                Log.d(BestTVPreloader.TAG, "ConsumerThread is finish");
                mLock.unlock();
            }
        }
    }

    public void removeTask(BestTVPreloadTask task) {
        mLock.lock();
        try {
            boolean flag = mLoadingTaskDeque.remove(task);
            Log.d(BestTVPreloader.TAG, "removeTask " + (flag ? "success" : "fail"));
        } finally {
            mLock.unlock();
        }
    }

    public class NetworkBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            boolean isConnect = (netInfo != null && netInfo.isConnected());
            if (isConnect) {
                int networkType = netInfo.getType();
                if (networkType == ConnectivityManager.TYPE_WIFI /*|| networkType == ConnectivityManager.TYPE_MOBILE*/) {
                    mLock.lock();
                    try {
                        mIsWifi = true;
                        network.signal();
                    } finally {
                        mLock.unlock();
                    }
                } else {
                    mIsWifi = false;
                }
            } else {
                mIsWifi = false;
            }
        }
    }

    private static class CurrentLoadingHandler extends Handler {

        private static final int MSG_PRELOAD_FORWARD = 100;

        private final WeakReference<BestTVPreloadFuture> videoPreLoadFutureWeakReference;

        public CurrentLoadingHandler(BestTVPreloadFuture preloadFuture) {
            videoPreLoadFutureWeakReference = new WeakReference<>(preloadFuture);
        }

        @Override
        public void handleMessage(Message msg) {

            BestTVPreloadFuture videoPreLoadFuture = videoPreLoadFutureWeakReference.get();

            if (videoPreLoadFuture == null) {
                return;
            }

            switch (msg.what) {
                case MSG_PRELOAD_FORWARD:
                    if (msg.obj instanceof String) {
                        videoPreLoadFuture.preloadForwardUrlInternel((String) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
