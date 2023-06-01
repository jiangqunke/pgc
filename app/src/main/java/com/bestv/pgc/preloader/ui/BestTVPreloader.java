package com.bestv.pgc.preloader.ui;


import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.bestv.pgc.preloader.ui.util.AndroidUtils;
import com.bestv.pgc.preloader.ui.util.StorageUtils;
import com.bestv.pgc.preloader.videocache.HttpProxyCacheServer;
import com.bestv.pgc.preloader.videocache.ProxyCacheUtils;
import com.bestv.pgc.preloader.videocache.file.FileNameGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public final class BestTVPreloader {
    public static final String TAG = BestTVPreloader.class.getSimpleName();

    private static volatile BestTVPreloader sInstance;

    private final ArrayMap<String, BestTVPreloadFuture> mVideoPreLoadFutureArrayMap = new ArrayMap<>();
    private final List<BestTVPreloadTask> mPreloadTaskPool = new ArrayList<>();

    private FileNameGenerator mFileNameGenerator;
    private HttpProxyCacheServer mHttpProxyCacheServer;
    private BestTVPreloaderConfig mConfig;

    private BestTVPreloader() {

    }

    public static BestTVPreloader getInstance() {
        if (sInstance == null) {
            synchronized (BestTVPreloader.class) {
                if (sInstance == null) {
                    sInstance = new BestTVPreloader();
                }
            }
        }
        return sInstance;
    }



    /**
     * @brief: 初始化本地预加载上下文（全局调用一次）
     * @param preloaderConfig: 预加载缓存配置结构体
     */
    public void initWithConfig(@NonNull BestTVPreloaderConfig preloaderConfig) {
        mConfig = preloaderConfig;

        mFileNameGenerator = new CustomizedMD5FileGenerator();
        File cacheRoot = StorageUtils.getIndividualCacheDirectory(mConfig.context);
        BestTVSourceInfoStorage sourceInfoStorage = new BestTVSourceInfoStorage(cacheRoot, mFileNameGenerator);
        mHttpProxyCacheServer = PlayerEnvironment.newProxy(
                mConfig.context,
                mConfig.maxCacheFilesSize,
                mConfig.maxCacheFilesCount,
                mFileNameGenerator,
                sourceInfoStorage,
                mConfig.useOKHttp);
    }

    /**
     * @brief: 根据指定的busId(比如页面tag),执行预加载任务
     * @param busId 用户指定的busId(比如页面的tag)
     * @param url 当前对应页面busId中，当前准备播放的url
     */
    public void preloadForwardUrl(@NonNull String busId, @NonNull String url) {
        if (TextUtils.isEmpty(busId) || TextUtils.isEmpty(url)) {
            return;
        }

        BestTVPreloadFuture preloadFuture = getVideoPreLoadFuture(busId);

        if (preloadFuture != null) {
            preloadFuture.preloadForwardUrl(url);
        }
    }

    /**
     * @brief 根据网络求轻的url地址获取本地预加载播放地址
     * @param url 待请求的真实网路url
     * @return 本地代理播放地址
     */
    public final String getLocalProxyUrl(@NonNull final String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        return mHttpProxyCacheServer.getProxyUrl(url);
    }

    public BestTVPreloadFuture getVideoPreLoadFuture(@NonNull String busId) {
        return mVideoPreLoadFutureArrayMap.get(busId);
    }

    protected final boolean hasEnoughCache(@NonNull String url) {
        final Context context = mConfig.context;
        final long preloadLengthInBytes = mConfig.preloadLengthInBytes;
        return AndroidUtils.hasEnoughCache(context, mFileNameGenerator, url, preloadLengthInBytes);
    }

    protected void putFuture(@NonNull String busId, @NonNull BestTVPreloadFuture videoPreloadFuture) {
        mVideoPreLoadFutureArrayMap.put(busId, videoPreloadFuture);
    }

    protected void removeFuture(@NonNull String busId) {
        mVideoPreLoadFutureArrayMap.remove(busId);
    }

    protected synchronized BestTVPreloadTask createTask(@NonNull final String busId, @NonNull String url, int index) {
        BestTVPreloadTask preloadTask = null;
        if (mPreloadTaskPool.size() > 0) {
            preloadTask = mPreloadTaskPool.get(0);
            mPreloadTaskPool.remove(0);
            Log.d(TAG, "get PreLoadTask from pool");
        }

        if (preloadTask == null) {
            preloadTask = new BestTVPreloadTask(this, url, index, mConfig.preloadLengthInBytes);
            Log.d(TAG, "new PreLoadTask");
            final BestTVPreloadTask tmpPreloadTask = preloadTask;
            preloadTask.setTaskCallback(() -> {
                BestTVPreloadFuture videoPreloadFuture = getVideoPreLoadFuture(busId);
                if (videoPreloadFuture != null) {
                    videoPreloadFuture.removeTask(tmpPreloadTask);
                }
                recyclerPreLoadTask(tmpPreloadTask);
            });
        } else {
            preloadTask.init(url, index);
        }

        return preloadTask;
    }

    protected synchronized void recyclerPreLoadTask(BestTVPreloadTask task) {
        int threadPoolSize = Math.max(5, mConfig.downloadThreadPoolSize);
        threadPoolSize = Math.min(40, threadPoolSize);

        if (mPreloadTaskPool.size() <= threadPoolSize) {
            Log.d(TAG, "recycler PreLoadTask into pool");
            mPreloadTaskPool.add(task);
        }
    }

    protected final BestTVPreloaderConfig getCurrentConfig() {
        return mConfig;
    }

    protected final FileNameGenerator getFileNameGenerator() {
        return mFileNameGenerator;
    }

    /**
     * @brief: 根据传入的url参数，删除内部对应的缓存文件（url可以带token，也可以不带token）
     * @param url：待删除的网络url指定的缓存文件
     */
    public synchronized boolean cleanCacheFile(Context context, String url) {
        boolean cleanResult = AndroidUtils.removeCache(context, mFileNameGenerator, url);
        if (!cleanResult) {
            Log.w(TAG, "cleanCache url:" + url + "failed");
        }
        return cleanResult;
    }

    /**
     * @brief: 获取当前所有缓存的文件名列表
     * @return： 当前本地缓存列表文件名
     */
    public List<String> getCacheFiles(Context context) {
        ArrayList<String> cacheFileLists = new ArrayList<>();
        File appCache = StorageUtils.getIndividualCacheDirectory(context);
        if (appCache.exists() && appCache.isDirectory()) {
            File[] listFiles = appCache.listFiles();
            if (listFiles != null &&listFiles.length > 0) {
                for (File file : listFiles) {
                    String fileName = file.getName();
                    cacheFileLists.add(fileName);
                }
            }
        }
        return cacheFileLists;
    }

    /**
     * @brief: 删除所有预加载的本地缓存
     */
    public synchronized void cleanAllCache(Context context) {
        File appCache = StorageUtils.getIndividualCacheDirectory(context);
        cleanDir(appCache);
    }

    private static void cleanDir(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] listFile = file.listFiles();
            for (int i = 0; i < listFile.length; i++) {
                cleanDir(listFile[i]);
                listFile[i].delete();
            }
        }
    }

    /**
     * @brief 释放本地预加载上下文（全局调用一次）
     */
    public void release() {
        if (mHttpProxyCacheServer != null) {
            mHttpProxyCacheServer.shutdown();
            mHttpProxyCacheServer = null;
        }
    }

    private static final class CustomizedMD5FileGenerator implements FileNameGenerator {
        private static final int MAX_EXTENSION_LENGTH = 4;

        @Override
        public String generate(String url) {
            String extension = getExtension(url);
            int extensionIndex = url.indexOf(extension);
            String urlWithoutToken = url.substring(0, extensionIndex);
            //String name = ProxyCacheUtils.computeMD5(url);
            String name = ProxyCacheUtils.computeMD5(urlWithoutToken);
            return TextUtils.isEmpty(extension) ? name : name + extension;
            //return TextUtils.isEmpty(extension) ? urlWithoutToken : urlWithoutToken + extension;
        }

        private String getExtension(String url) {
            if (url.toLowerCase().contains(".mp4"))
                return ".mp4";
            else if (url.toLowerCase().contains(".mov"))
                return ".mov";
            else if (url.toLowerCase().contains(".flv"))
                return ".flv";
            else if (url.toLowerCase().contains(".mpg"))
                return ".mpg";
            else if (url.toLowerCase().contains(".mkv"))
                return ".mkv";
            else if (url.toLowerCase().contains(".ts"))
                return ".ts";

            int dotIndex = url.lastIndexOf('.');
            int slashIndex = url.lastIndexOf('/');
            return dotIndex != -1 && dotIndex > slashIndex && dotIndex + 2 + MAX_EXTENSION_LENGTH > url.length() ?
                    url.substring(dotIndex + 1, url.length()) : "";
        }
    }
}
