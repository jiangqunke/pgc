package com.bestv.pgc.util;

import android.content.Context;
import android.content.Intent;

import com.bestv.pgc.preloader.ui.BestTVPreloader;
import com.bestv.pgc.preloader.ui.BestTVPreloaderConfig;
import com.bestv.pgc.ui.PlaylistActivity;
import com.google.android.exoplayer2.C;

public class BestvAgent {
    private Context mBaseContext;
    private volatile static BestvAgent bestvAgent;

    public static BestvAgent getInstance() {
        if (null == bestvAgent) {
            synchronized (BestvAgent.class) {
                if (bestvAgent == null) {
                    bestvAgent = new BestvAgent();
                }
            }
        }
        return bestvAgent;
    }

    public void init(Context context) {
        this.mBaseContext = context;
        initPreloaderConfig(context);
    }

    public Context getBaseContext() {
        return mBaseContext;
    }

    public void setBaseContext(Context context) {
        mBaseContext = context;
    }


    //看点预加载
    private void initPreloaderConfig(Context context) {
        BestTVPreloaderConfig config = new BestTVPreloaderConfig.Builder(context)
                .maxCacheFilesCount(200) //200个缓存文件
                .maxCacheFilesSize(2048000000) //2GB最大缓存
                .preloadFilesCount(5) //预加载5个短视频
                .preloadLengthInBytes(1024000) //预加载1024KB
                .downloadThreadPoolSize(20)
                .downloadThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY)
                .useOKHttp(false)
                .canPreloadIfNotWifi(true)
                .build();
        // 初始化全局预加载代理服务（必须选项）
        BestTVPreloader.getInstance().initWithConfig(config);
    }

    public void playVideo(Context context, String openId, String poi, String scene, String videoJsonInfo) {
        Intent intent = new Intent();
        intent.putExtra("openId", openId);
        intent.putExtra("poi", poi);
        intent.putExtra("scene", scene);
        intent.putExtra("videoInfo", videoJsonInfo);
        intent.setClass(context, PlaylistActivity.class);
        context.startActivity(intent);
    }
}
