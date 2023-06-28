package com.bestv.pgc.util;

import android.content.Context;
import android.content.Intent;

import com.bestv.pgc.net.ApiUrl;
import com.bestv.pgc.preloader.ui.BestTVPreloader;
import com.bestv.pgc.preloader.ui.BestTVPreloaderConfig;
import com.bestv.pgc.ui.PlaylistActivity;

public class BestvAgent {
    private Context mBaseContext;
    private volatile static BestvAgent bestvAgent;
    private boolean isOficial;
    private String url;
    private OnPariseListening pariseListening;
    public OnPariseListening getPariseListening() {
        return pariseListening;
    }

    public void setPariseListening(OnPariseListening pariseListening) {
        this.pariseListening = pariseListening;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isOficial() {
        return isOficial;
    }

    public void setOficial(boolean oficial) {
        isOficial = oficial;
    }

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

    public void init(Context context,boolean isOficial) {
        this.isOficial = isOficial;
         this.url = isOficial?"https://cms-ff.ibbtv.cn":"http://121.41.196.103:32016";
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

    public void playVideo(Context context, String openId, String poi, String scene, String videoJsonInfo,String analysysInfo,OnPariseListening listening) {
        this.pariseListening = listening;
        Intent intent = new Intent();
        intent.putExtra("openId", openId);
        intent.putExtra("poi", poi);
        intent.putExtra("scene", scene);
        intent.putExtra("analysysInfo", analysysInfo);
        intent.putExtra("videoInfo", videoJsonInfo);
        intent.setClass(context, PlaylistActivity.class);
        context.startActivity(intent);
    }
}
