package com.bestv.pgc.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ExoVideoView extends FrameLayout implements MediaPlayerControl, MediaEngineInterface {
    //播放器的各种状态
    public static final int STATE_ERROR = -1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    public static final int STATE_BUFFERING = 6;
    public static final int STATE_BUFFERED = 7;
    private Timer mTimer;
    public int getCurrentPlayState() {
        return mCurrentPlayState;
    }

    public void setCurrentPlayState(int mCurrentPlayState) {
        this.mCurrentPlayState = mCurrentPlayState;
    }

    protected int mCurrentPlayState = STATE_IDLE;//当前播放器的状态

    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    public static final int PLAYER_TINY_SCREEN = 12;   // 小屏播放器
    protected int mCurrentPlayerState = PLAYER_NORMAL;

    private int isResetCount = 0;
    protected ExoMediaEngine mMediaPlayer;//播放器
    //--------- data sources ---------//
    protected String mUrl;//当前播放视频的地址
    protected Map<String, String> mHeaders;//当前视频地址的请求头
    protected int mCurrentPosition;//当前正在播放视频的位置
    protected VideoListener listener;
    protected AudioFocusHelper mAudioFocusHelper;
    protected boolean mIsMute;//是否静音
    protected PlayerConfig mPlayerConfig;//播放器配置
    /**
     * 真正承载播放器视图的容器
     */
    protected FrameLayout mPlayerContainer;

    protected ResizeTextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    /**
     * 循环播放
     */
    protected boolean mIsLooping;

    private boolean isDlanModel= false;
    private boolean isStopTouch = false;
    private boolean isEnableScale = true;

    public static final int SCREEN_SCALE_DEFAULT = 0;
    public static final int SCREEN_SCALE_16_9 = 1;
    public static final int SCREEN_SCALE_4_3 = 2;
    public static final int SCREEN_SCALE_MATCH_PARENT = 3;
    public static final int SCREEN_SCALE_ORIGINAL = 4;
    public static final int SCREEN_SCALE_HEIGHT = 5;

    public boolean isDlanModel() {
        return isDlanModel;
    }

    public void setDlanModel(boolean dlanModel) {
        isDlanModel = dlanModel;
    }

    public boolean isStopTouch() {
        return isStopTouch;
    }

    public boolean isEnableScale() {
        return isEnableScale;
    }

    public ExoVideoView(@NonNull Context context) {
        this(context, null);
    }

    public ExoVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExoVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPlayerConfig = new PlayerConfig.Builder().build();
        mAudioFocusHelper = new AudioFocusHelper(this);
        initView();
    }

    private void initView() {
        mPlayerConfig.addToPlayerManager = true;
        mPlayerContainer = new FrameLayout(getContext());
        mPlayerContainer.setBackgroundColor(Color.BLACK);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mPlayerContainer, params);
    }


    /**
     * 初始化播放器
     */
    protected void initPlayer() {
        mMediaPlayer = new ExoMediaEngine(getContext());
        mMediaPlayer.setPlayerEventListener(this);
        setInitOptions();
        mMediaPlayer.initPlayer();
        setOptions();
    }

    /**
     * 设置视频地址
     */
    public void setUrl(String url) {
        setUrl(url, null);
    }

    /**
     * 设置包含请求头信息的视频地址
     *
     * @param url     视频地址
     * @param headers 请求头
     */
    public void setUrl(String url, Map<String, String> headers) {
        mUrl = url;
        mHeaders = headers;
    }
    /**
     * 初始化之前的配置项
     */
    protected void setInitOptions() {

    }

    /**
     * 初始化之后的配置项
     */
    protected void setOptions() {
        mMediaPlayer.setLooping(mIsLooping);
    }
    public int getCurrentPlayerState() {
        return mCurrentPlayerState;
    }

    /**
     * 循环播放， 默认不循环播放
     */
    public void setLooping(boolean looping) {
        mIsLooping = looping;
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(looping);
        }
    }


    public void setCurrentPlayerState(int mCurrentPlayerState) {
        this.mCurrentPlayerState = mCurrentPlayerState;
    }

    public VideoListener getVideoListener() {
        return listener;
    }

    public void setVideoListener(VideoListener listener) {
        this.listener = listener;
    }
    /**
     * 向Controller设置播放状态，用于控制Controller的ui展示
     */
    protected void setPlayState(int playState) {
        mCurrentPlayState = playState;
    }
    @Override
    public void onError() {
        try {
            if (isResetCount<=2 && mMediaPlayer != null){
                mMediaPlayer.reset();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addTextureView();
                        startPrepare(true);
                        isResetCount++;
                    }
                }, 800);
            }else {
                isResetCount=0;
                setPlayState(STATE_ERROR);
                if (listener != null) listener.onError();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void addTextureView() {

        mPlayerContainer.removeView(mTextureView);
        mSurfaceTexture = null;
        mTextureView = new ResizeTextureView(getContext());
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (mSurfaceTexture != null) {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                } else {
                    mSurfaceTexture = surfaceTexture;
                    if (mMediaPlayer == null){
                        initPlayer();
                    }
                    mMediaPlayer.setSurface(new Surface(surfaceTexture));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                return mSurfaceTexture == null;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }
        });
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        mPlayerContainer.addView(mTextureView, 0, params);
    }

    /**
     * 开始准备播放（直接播放）
     */
    protected void startPrepare(boolean reset) {
        try {
            if (mMediaPlayer == null){
                initPlayer();
            }
            if (reset) mMediaPlayer.reset();
            if (prepareDataSource()) {
                mMediaPlayer.prepareAsync();
            }
            SetupTimer();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    /**
     * 设置播放数据
     * @return 播放数据是否设置成功
     */
    protected boolean prepareDataSource() {
     if (!TextUtils.isEmpty(mUrl)) {
            mMediaPlayer.setDataSource(mUrl, mHeaders);
            return true;
        }
        return false;
    }

    @Override
    public void onCompletion() {
        if (listener != null) listener.onComplete();
        mCurrentPosition = 0;
        setPlayState(STATE_PLAYBACK_COMPLETED);
        setKeepScreenOn(false);
    }

    @Override
    public void onInfo(int what, int extra) {
        if (listener != null) listener.onInfo(what, extra);
        switch (what) {
            case AbstractPlayer.MEDIA_INFO_BUFFERING_START:
                setPlayState(STATE_BUFFERING);
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_END:
                setPlayState(STATE_BUFFERED);
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
                setPlayState(STATE_PLAYING);
                if (getWindowVisibility() != VISIBLE) pause();
                break;
            case AbstractPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                break;
        }
    }

    @Override
    public void onBufferingUpdate(int percent) {

    }

    @Override
    public void onPrepared() {
        setPlayState(STATE_PREPARED);
        addTextureView();
        if (listener != null) listener.onPrepared();
        if (mCurrentPosition > 0) {
            seekTo(mCurrentPosition);
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        if (mTextureView != null) {
            mTextureView.setScreenScale(0);
            mTextureView.setVideoSize(width, height);
        }
    }
    /**
     * 是否处于未播放转态，此时{@link #mMediaPlayer}为null
     */
    protected boolean isInIdleState() {
        return mMediaPlayer == null
                || mCurrentPlayState == STATE_IDLE;
    }
    @Override
    public void start() {
        boolean isStarted = false;
        if (isInIdleState()) {
            startPlay();
            isStarted = true;
        } else if (isInPlaybackState()) {
            startInPlaybackState();
            isStarted = true;
        }
        if (isStarted) {
            setKeepScreenOn(true);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.requestFocus();
        }
    }
    /**
     * 播放状态下开始播放
     */
    protected void startInPlaybackState() {
        mMediaPlayer.start();
        setPlayState(STATE_PLAYING);
    }

    /**
     * 是否处于播放状态
     */
    protected boolean isInPlaybackState() {
        return mMediaPlayer != null
                && mCurrentPlayState != STATE_ERROR
                && mCurrentPlayState != STATE_IDLE
                && mCurrentPlayState != STATE_PREPARING
                && mCurrentPlayState != STATE_PLAYBACK_COMPLETED;
    }

    public void resume() {
        if (isInPlaybackState()
                && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            setPlayState(STATE_PLAYING);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.requestFocus();
            setKeepScreenOn(true);
        }
    }

    /**
     * 第一次播放
     */
    protected void startPlay() {

//        mMediaPlayer.reset();
        initPlayer();
        mMediaPlayer.setEnableMediaCodec(mPlayerConfig.enableMediaCodec);
        startPrepare(false);
    }

    public void stopPlayback() {
        release();
    }
    /**
     * 释放播放器
     */
    public void release() {
        if (!isInIdleState()) {
            mCurrentPlayState = STATE_IDLE;
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            setKeepScreenOn(false);
            if (mAudioFocusHelper != null) {
                mAudioFocusHelper.abandonFocus();
            }
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
                mSurfaceTexture = null;
            }
            mCurrentPosition = 0;
            setPlayState(STATE_IDLE);
        }
    }
    @Override
    public void pause() {
        if (isPlaying()) {
            mMediaPlayer.pause();
            setPlayState(STATE_PAUSED);
            setKeepScreenOn(false);
            if (mAudioFocusHelper != null)
                mAudioFocusHelper.abandonFocus();
        }
    }

    @Override
    public int getDuration() {
        if (isInPlaybackState()) {
            return (int)mMediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            mCurrentPosition = (int) mMediaPlayer.getCurrentPosition();
            return mCurrentPosition;
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(pos);
        }
    }

    @Override
    public boolean isPlaying() {
        return isInPlaybackState()  && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public void startFullScreen() {

    }

    @Override
    public void stopFullScreen() {

    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setMute(boolean isMute) {
        if (mMediaPlayer != null) {
            this.mIsMute = isMute;
            float volume = isMute ? 0.0f : 1.0f;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    @Override
    public boolean getMute() {
        return mIsMute;
    }

    @Override
    public void setLock(boolean isLocked) {

    }

    @Override
    public void setScreenScale(int screenScale) {
        if (mTextureView != null) {
            mTextureView.setScreenScale(screenScale);
        }
    }

    @Override
    public void setSpeed(float speed) {
        if (isInPlaybackState()) {
            mMediaPlayer.setSpeed(speed);
        }
    }

    @Override
    public void setVolume(int volume) {
        SystemUtils.setSystemVolume(getContext(), volume);
    }

    @Override
    public int getVolume() {
      return   SystemUtils.getSystemCurrentVolume(getContext());
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onDestroy() {
    }

    public void setStopTouch(boolean b) {
    }

    public void setEnableScale(boolean b) {
    }

    public void setPlayerBuffSizeEnable(boolean b) {
    }

    public Bitmap getNetVideoBitmap() {
        return null;
    }

    private void SetupTimer() {
        if (mTimer != null) {
            CancelTimer();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mMediaPlayer == null) return;
//                if ( bufferPercentage != 0) {

                post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPosition();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                        if (listener != null) {
                            listener.onProgress(progress, position, duration);
                        }
                    }
                });
//                }
            }
        }, 1000, 1000);
    }

    /**
     * 关闭一个Timer
     */
    private void CancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }
}
