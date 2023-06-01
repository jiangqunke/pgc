package com.bestv.pgc.preloader.ui;

import android.content.Context;

public class BestTVPreloaderConfig {
    public final Context context;

    /**
     * 每一个预加载页面预加载文件数（相对当前播放地址）
     */
    public final int preloadFilesCount;

    /**
     * 每一个文件预加载大小(字节)
     */
    public final long preloadLengthInBytes;

    /**
     * 最大缓存文件大小
     */
    public final long maxCacheFilesSize;

    /**
     * 最大缓存文件数
     */
    public final int maxCacheFilesCount;

    /**
     * 下载线程池线程数
     */
    public final int downloadThreadPoolSize;

    /**
     * 下载线程池线程等级
     */
    public final int downloadThreadPriority;

    /**
     * 是否使用OKHttp下载视频
     */
    public final boolean useOKHttp;

    /**
     * 4G/5G非wifi网络下开启预加载
     */
    public final boolean canPreloadIfNotWifi;

    BestTVPreloaderConfig(Builder builder) {
        context = builder.context;
        preloadFilesCount = builder.preloadFilesCount;
        preloadLengthInBytes = builder.preloadLengthInBytes;
        maxCacheFilesSize = builder.maxCacheFilesSize;
        maxCacheFilesCount = builder.maxCacheFilesCount;
        downloadThreadPoolSize = builder.downloadThreadPoolSize;
        downloadThreadPriority = builder.downloadThreadPriority;
        useOKHttp = builder.useOKHttp;
        canPreloadIfNotWifi = builder.canPreloadIfNotWifi;
    }

    /**
     * {@link BestTVPreloaderConfig}构造器
     */
    public static final class Builder {
        private final Context context;

        private int preloadFilesCount = 5; //默认预加载5个文件

        private long preloadLengthInBytes = 1024 * 1024L; // 默认预加载1024KB

        private long maxCacheFilesSize = 2048 * 1024 * 1024L; // 默认最大缓存2GB

        private int maxCacheFilesCount = 200; // 默认最多缓存200哥视频

        private int downloadThreadPoolSize = 20; // 默认下载线程池最大20个线程

        private int downloadThreadPriority = android.os.Process.THREAD_PRIORITY_DEFAULT; // 下载线程默认优先级

        private boolean useOKHttp; // 默认不使用

        private boolean canPreloadIfNotWifi; // 默认开启4G预加载

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder preloadFilesCount(int preloadFilesCount) {
            this.preloadFilesCount = preloadFilesCount;
            return this;
        }

        public Builder preloadLengthInBytes(long preloadLengthInBytes) {
            this.preloadLengthInBytes = preloadLengthInBytes;
            return this;
        }

        public Builder maxCacheFilesSize(long maxCacheFilesSize) {
            this.maxCacheFilesSize = maxCacheFilesSize;
            return this;
        }

        public Builder maxCacheFilesCount(int maxCacheFilesCount) {
            this.maxCacheFilesCount = maxCacheFilesCount;
            return this;
        }

        public Builder downloadThreadPoolSize(int downloadThreadPoolSize) {
            this.downloadThreadPoolSize = downloadThreadPoolSize;
            return this;
        }

        public Builder downloadThreadPriority(int downloadThreadPriority) {
            this.downloadThreadPriority = downloadThreadPriority;
            return this;
        }

        public Builder useOKHttp(boolean useOKHttp) {
            this.useOKHttp = useOKHttp;
            return this;
        }

        public Builder canPreloadIfNotWifi(boolean canPreloadIfNotWifi) {
            this.canPreloadIfNotWifi = canPreloadIfNotWifi;
            return this;
        }

        public BestTVPreloaderConfig build() {
            return new BestTVPreloaderConfig(this);
        }
    }

}
