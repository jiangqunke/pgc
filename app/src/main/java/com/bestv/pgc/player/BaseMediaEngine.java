package com.bestv.pgc.player;

import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;


import java.io.IOException;


public abstract class BaseMediaEngine {
    protected MediaEngineInterface mMediaEngineInterface;

    public abstract void initPlayer();

    public abstract void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    public abstract void start();

    public abstract void pause();

    public abstract void stop();

    public abstract void prepareAsync();

    public abstract void reset();

    public abstract boolean isPlaying();

    public abstract void seekTo(long time);

    public abstract void release();

    public abstract long getCurrentPosition();

    public abstract long getDuration();

    public abstract void setSurface(Surface surface);

    public abstract void setDisplay(SurfaceHolder holder);


    public abstract void setVolume(int v1, int v2);

    public abstract void setLooping(boolean isLooping);

    public abstract void setEnableMediaCodec(boolean isEnable);

    public abstract void setOptions();

    public abstract void setSpeed(float speed);

    public abstract void onResume();

    public abstract void onPause();

    public abstract float getPinchScale();

    public abstract void onDestroy();

    public abstract void  decreaseBuffSize(boolean isEnableBuffSize);

    public abstract void handlerTouch(MotionEvent event);
    public void setMediaEngineInterface(MediaEngineInterface mediaEngineInterface) {
        this.mMediaEngineInterface = mediaEngineInterface;
    }

}
