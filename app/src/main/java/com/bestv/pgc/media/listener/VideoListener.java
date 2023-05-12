package com.bestv.pgc.media.listener;


public interface VideoListener {
    void startPrepare();
    //播放完成
    void onComplete();

    //准备完成
    void onPrepared();

    void onError();

    void onInfo(int what, int extra);

    void onProgress(int progress, long currentPosition, long duration);

//    void onSeekComplete(IMediaPlayer iMediaPlayer);

}
