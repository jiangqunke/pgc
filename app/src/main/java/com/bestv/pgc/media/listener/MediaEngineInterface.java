package com.bestv.pgc.media.listener;


public interface MediaEngineInterface {

    void onError();

    void onCompletion();

    void onInfo(int what, int extra);

    void onBufferingUpdate(int percent);

    void onPrepared();

    void onVideoSizeChanged(int width, int height);

//    void OnSeekCompleteListener(IMediaPlayer iMediaPlayer);

}
