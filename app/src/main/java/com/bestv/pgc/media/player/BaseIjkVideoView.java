package com.bestv.pgc.media.player;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bestv.ijkplayer.player.IMediaPlayer;
import com.bestv.ijkplayer.player.IjkMediaPlayer;
import com.bestv.pgc.media.listener.MediaEngineInterface;
import com.bestv.pgc.media.listener.MediaPlayerControl;
import com.bestv.pgc.media.listener.VideoListener;
import com.bestv.pgc.media.utils.SystemUtils;

import java.util.Timer;
import java.util.TimerTask;


/**
 * 播放器
 */

public abstract class BaseIjkVideoView extends FrameLayout implements MediaPlayerControl, MediaEngineInterface {

    protected BaseMediaEngine mMediaPlayer;//播放引擎
    protected VideoListener listener;
    protected int bufferPercentage;//缓冲百分比
    protected boolean isMute;//是否静音

    protected String mCurrentUrl;//当前播放视频的地址
    protected int mCurrentPosition;//当前正在播放视频的位置
    protected String mCurrentTitle = "";//当前正在播放视频的标题

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
    protected int mCurrentPlayState = STATE_IDLE;//当前播放器的状态

    public static final int PLAYER_NORMAL = 10;        // 普通播放器
    public static final int PLAYER_FULL_SCREEN = 11;   // 全屏播放器
    protected int mCurrentPlayerState = PLAYER_NORMAL;

    protected AudioManager mAudioManager;//系统音频管理器
    @NonNull
    protected AudioFocusHelper mAudioFocusHelper = new AudioFocusHelper();

    protected int currentOrientation = 0;
    protected static final int PORTRAIT = 1;
    protected static final int LANDSCAPE = 2;
    protected static final int REVERSE_LANDSCAPE = 3;

    protected boolean isLockFullScreen;//是否锁定屏幕
    protected PlayerConfig mPlayerConfig;//播放器配置
    private Timer mTimer;



    public BaseIjkVideoView(@NonNull Context context) {
        this(context, null);
    }


    public BaseIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseIjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAudioManager = (AudioManager) getContext().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mPlayerConfig = new PlayerConfig.Builder().build();
    }

    protected void initPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new IjkMediaEngine();
            mMediaPlayer.setMediaEngineInterface(this);
            mMediaPlayer.initPlayer();

            mMediaPlayer.setLooping(mPlayerConfig.isLooping);

        }
    }

    protected abstract void setPlayState(int playState);

    protected abstract void setPlayerState(int playerState);

    /**
     * 开始准备播放（直接播放）
     */
    protected void startPrepare(boolean needReset) {
        if (mCurrentUrl == null || mCurrentUrl.trim().equals("")) return;
        if (needReset) mMediaPlayer.reset();
        try {
//            if (mPlayerConfig.isCache) {
//                HttpProxyCacheServer cacheServer = getCacheServer();
//                String proxyPath = cacheServer.getProxyUrl(mCurrentUrl);
//                cacheServer.registerCacheListener(cacheListener, mCurrentUrl);
//                if (cacheServer.isCached(mCurrentUrl)) {
//                    bufferPercentage = 100;
//                }
//                mMediaPlayer.setDataSource(proxyPath);
//            } else {
            mMediaPlayer.setDataSource(mCurrentUrl);
            //}
            mMediaPlayer.prepareAsync();
            mCurrentPlayState = STATE_PREPARING;
            setPlayState(mCurrentPlayState);
            mCurrentPlayerState = isFullScreen() ? PLAYER_FULL_SCREEN : PLAYER_NORMAL;
            setPlayerState(mCurrentPlayerState);
            SetupTimer();
            if (listener != null) listener.startPrepare();
        } catch (Exception e) {
            onError();
            e.printStackTrace();
        }
    }

//    private HttpProxyCacheServer getCacheServer() {
//        return VideoCacheManager.getProxy(getContext().getApplicationContext());
//    }

    @Override
    public void start() {
        if (mCurrentPlayState == STATE_IDLE) {
            startPlay();
        } else if (isInPlaybackState()) {
            startInPlaybackState();
        }
        setKeepScreenOn(true);
        mAudioFocusHelper.requestFocus();
    }

    /**
     * 第一次播放
     */
    protected void startPlay() {
        initPlayer();
//        mMediaPlayer.reset();
        mMediaPlayer.setEnableMediaCodec(mPlayerConfig.enableMediaCodec);
        startPrepare(false);

    }

    /**
     * 播放状态下开始播放
     */
    protected void startInPlaybackState() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
            mCurrentPlayState = STATE_PLAYING;
            setPlayState(mCurrentPlayState);
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null) {
            if (isInPlaybackState()) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mCurrentPlayState = STATE_PAUSED;
                    setPlayState(mCurrentPlayState);
                    setKeepScreenOn(false);
                    mAudioFocusHelper.abandonFocus();
                }
            }
        }

    }


    public void resume() {
        if (mMediaPlayer != null) {
            if (isInPlaybackState() && !mMediaPlayer.isPlaying() && mCurrentPlayState != STATE_PLAYBACK_COMPLETED) {
                mMediaPlayer.start();
                mCurrentPlayState = STATE_PLAYING;
                setPlayState(mCurrentPlayState);
                mAudioFocusHelper.requestFocus();
                setKeepScreenOn(true);
            }
        }
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mCurrentPlayState = STATE_IDLE;
            setPlayState(mCurrentPlayState);
            mAudioFocusHelper.abandonFocus();
            setKeepScreenOn(false);
        }

        //if (mPlayerConfig.isCache) getCacheServer().unregisterCacheListener(cacheListener);

        isLockFullScreen = false;
        mCurrentPosition = 0;
        CancelTimer();
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentPlayState = STATE_IDLE;
            setPlayState(mCurrentPlayState);
            mAudioFocusHelper.abandonFocus();
            setKeepScreenOn(false);
        }
        //if (mPlayerConfig.isCache) getCacheServer().unregisterCacheListener(cacheListener);

        isLockFullScreen = false;
        mCurrentPosition = 0;
        CancelTimer();
    }

    public void setVideoListener(VideoListener listener) {
        this.listener = listener;
    }

    protected boolean isInPlaybackState() {
        return (mMediaPlayer != null && mCurrentPlayState != STATE_ERROR
                && mCurrentPlayState != STATE_IDLE && mCurrentPlayState != STATE_PREPARING);
    }

    @Override
    public int getDuration() {
        if (mMediaPlayer != null) {
            if (isInPlaybackState()) {
                return (int) mMediaPlayer.getDuration();
            }
        }
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            if (isInPlaybackState()) {
                mCurrentPosition = (int) mMediaPlayer.getCurrentPosition();
                return mCurrentPosition;
            }
        }
        return 0;
    }

    @Override
    public void seekTo(int pos) {
        if (mMediaPlayer != null) {
            if (isInPlaybackState()) {
                mMediaPlayer.seekTo(pos);
            }
        }
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return mMediaPlayer != null ? bufferPercentage : 0;
    }

    /**
     * 设置静音
     */
    @Override
    public void setMute(boolean isMute) {
        if (mMediaPlayer != null) {
            this.isMute = isMute;
            if (isMute) {
                mMediaPlayer.setVolume(0, 0);
            } else {
                mMediaPlayer.setVolume(1, 1);
            }
        }
    }

    @Override
    public boolean getMute() {
        return isMute;
    }

    @Override
    public void setSpeed(float speed) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setSpeed(speed);
        }

    }

    @Override
    public void setVolume(int index) {
        SystemUtils.setSystemVolume(getContext(), index);
    }

    @Override
    public int getVolume() {
        return SystemUtils.getSystemCurrentVolume(getContext());
    }

    @Override
    public void setLock(boolean isLocked) {
        this.isLockFullScreen = isLocked;
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public String getTitle() {
        return mCurrentTitle;
    }


//    private CacheListener cacheListener = new CacheListener() {
//        @Override
//        public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
//            bufferPercentage = percentsAvailable;
//        }
//    };

    @Override
    public void onError() {
        mCurrentPlayState = STATE_ERROR;
        if (listener != null) listener.onError();
        setPlayState(mCurrentPlayState);
        mCurrentPosition = getCurrentPosition();
    }

    @Override
    public void onCompletion() {
        mCurrentPlayState = STATE_PLAYBACK_COMPLETED;
        if (listener != null) listener.onComplete();
        setPlayState(mCurrentPlayState);
        setKeepScreenOn(false);
    }

    @Override
    public void onInfo(int what, int extra) {
        if (listener != null) listener.onInfo(what, extra);
        switch (what) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mCurrentPlayState = STATE_BUFFERING;
                setPlayState(mCurrentPlayState);
                break;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mCurrentPlayState = STATE_BUFFERED;
                setPlayState(mCurrentPlayState);
                break;
            case IjkMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START: // 视频开始渲染
                mCurrentPlayState = STATE_PLAYING;
                setPlayState(mCurrentPlayState);
                if (getWindowVisibility() != VISIBLE) pause();
                break;
        }
    }

    @Override
    public void onBufferingUpdate(final int position) {
        if (!mPlayerConfig.isCache) {
            bufferPercentage = position;
//            post(new Runnable() {
//                @Override
//                public void run() {
//                    if (mCurrentPlayerState != STATE_IDLE && mCurrentPlayerState != STATE_PREPARING) {
//                        if (position != 0) {
//                            int position = getCurrentPosition();
//                            int duration = getDuration();
//                            int progress = position * 100 / (duration == 0 ? 1 : duration);
//                            if (listener != null) {
//                                listener.onProgress(progress, position, duration);
//                            }
//                        }
//                    }
//                }
//            });
//            if (position != 0) {
//
//            }
        }
    }

    @Override
    public void onPrepared() {
        mCurrentPlayState = STATE_PREPARED;
        if (listener != null) listener.onPrepared();
        setPlayState(mCurrentPlayState);
        if (mCurrentPosition > 0) {
            seekTo(mCurrentPosition);
        }
    }

//    @Override
//    public void OnSeekCompleteListener(IMediaPlayer iMediaPlayer) {
//        if (listener != null) listener.onSeekComplete(iMediaPlayer);
//    }

    public void setPlayerConfig(PlayerConfig config) {
        this.mPlayerConfig = config;
    }

    /**
     * 获取当前播放器的状态
     */
    public int getCurrentPlayerState() {
        return mCurrentPlayerState;
    }

    /**
     * 获取当前的播放状态
     */
    public int getCurrentPlayState() {
        return mCurrentPlayState;
    }

    public void setLooping(boolean flag) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(flag);
        }
    }


    /**
     * 音频焦点改变监听
     */
    private class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {
        boolean startRequested = false;
        boolean pausedForLoss = false;
        int currentFocus = 0;

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (currentFocus == focusChange) {
                return;
            }

            currentFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    if (startRequested || pausedForLoss) {
                        start();
                        startRequested = false;
                        pausedForLoss = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (isPlaying()) {
                        pausedForLoss = true;
                        pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (isPlaying()) {
                        pausedForLoss = true;
                        pause();
                    }
                    break;
            }
        }

        /**
         * Requests to obtain the audio focus
         *
         * @return True if the focus was granted
         */
        boolean requestFocus() {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return true;
            }

            if (mAudioManager == null) {
                return false;
            }

            int status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN;
                return true;
            }

            startRequested = true;
            return false;
        }

        /**
         * Requests the system to drop the audio focus
         *
         * @return True if the focus was lost
         */
        boolean abandonFocus() {

            if (mAudioManager == null) {
                return false;
            }

            startRequested = false;
            int status = mAudioManager.abandonAudioFocus(this);
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == status;
        }
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
        }, 0, 1000);
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
