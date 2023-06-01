package com.bestv.pgc.preloader.tool.listener;


import com.bestv.pgc.preloader.tool.common.VideoCacheException;
import com.bestv.pgc.preloader.tool.m3u8.M3U8;
import com.bestv.pgc.preloader.tool.model.VideoCacheInfo;

public abstract class VideoInfoParsedListener implements IVideoInfoParsedListener {


    @Override
    public void onM3U8ParsedFinished(M3U8 m3u8, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8ParsedFailed(VideoCacheException e, VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onM3U8LiveCallback(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFinished(VideoCacheInfo cacheInfo) {

    }

    @Override
    public void onNonM3U8ParsedFailed(VideoCacheException e, VideoCacheInfo cacheInfo) {

    }
}
