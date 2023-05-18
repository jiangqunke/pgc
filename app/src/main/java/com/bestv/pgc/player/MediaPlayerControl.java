package com.bestv.pgc.player;

public interface MediaPlayerControl {
    void start();

    void pause();

    int getDuration();

    int getCurrentPosition();

    void seekTo(int pos);

    boolean isPlaying();

    int getBufferPercentage();

    void startFullScreen();

    void stopFullScreen();

    boolean isFullScreen();

    String getTitle();

    void setMute(boolean isMute);

    boolean getMute();

    void setLock(boolean isLocked);

    void setScreenScale(int screenScale);

    void setSpeed(float speed);

    void setVolume(int volume);

    int getVolume();
}