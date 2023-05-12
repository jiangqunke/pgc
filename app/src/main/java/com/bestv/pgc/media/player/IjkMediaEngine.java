package com.bestv.pgc.media.player;

import android.media.AudioManager;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.bestv.ijkplayer.player.IMediaPlayer;
import com.bestv.ijkplayer.player.IjkMediaPlayer;

import java.io.IOException;


public class IjkMediaEngine extends BaseMediaEngine {

    protected IMediaPlayer mMediaPlayer;
    private boolean isLooping;

    @Override
    public void start() {
        mMediaPlayer.start();
    }

    @Override
    public void initPlayer() {
        mMediaPlayer = new IjkMediaPlayer();
        setOptions();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnErrorListener(onErrorListener);
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
        mMediaPlayer.setOnInfoListener(onInfoListener);
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(onPreparedListener);
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
//        mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
//        mMediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
//            @Override
//            public boolean onNativeInvoke(int i, Bundle bundle) {
//                return true;
//            }
//        });
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        mMediaPlayer.setDataSource(path);
    }

    @Override
    public void pause() {
        mMediaPlayer.pause();
    }

    @Override
    public void stop() {
        mMediaPlayer.stop();
    }

    @Override
    public void prepareAsync() {
        try {
            mMediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void reset() {
        mMediaPlayer.reset();
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mMediaPlayer.setLooping(isLooping);
        setOptions();
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(long time) {
        mMediaPlayer.seekTo((int) time);
    }

    @Override
    public void release() {
        if (mMediaPlayer != null)
            mMediaPlayer.release();
    }

    @Override
    public long getCurrentPosition() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public void setSurface(Surface surface) {
        mMediaPlayer.setSurface(surface);
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
    }


    @Override
    public void setVolume(int v1, int v2) {
        mMediaPlayer.setVolume(v1, v2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        this.isLooping = isLooping;
        mMediaPlayer.setLooping(isLooping);
    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {
        int value = isEnable ? 1 : 0;
        initPlayer((IjkMediaPlayer)mMediaPlayer);
    }

    @Override
    public void setOptions() {

    }

    @Override
    public void setSpeed(float speed) {
        ((IjkMediaPlayer)mMediaPlayer).setPlayRate(speed);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public float getPinchScale() {
        return 0;
    }


    @Override
    public void onDestroy() {

    }

    @Override
    public void decreaseBuffSize(boolean isEnableBuffSize) {

    }

    @Override
    public void handlerTouch(MotionEvent event) {

    }

    protected IjkMediaPlayer initPlayer(IjkMediaPlayer player) {
        if (player != null) {
            player.setBufferSize(20);
            //ijkMediaPlayer.setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
            //ijkMediaPlayer.setOverlayFormat(AvFourCC.SDL_FCC_RV32);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);

            //framedrop参数设置为2，解决有的视频两倍速播放音视频不同步问题，不能设置太大，不然会出现明显丢帧
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 2);
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);

            //HLS常连接设置
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "hls_long_connection", 1);
            //ijkMediaPlayer.setFrameDrop(12);

            //硬解码相关设置

            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1); //0关闭，1开启，默认0
            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-hevc", 1); //0关闭，1开启，默认0

            player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
//			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { //处理6.0以下设备播放器倍速播放声音异常（人物音色与实际不符）的问题
//				player.setOption(ItyMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
//			}

        }
        return player;
    }

    private IMediaPlayer.OnErrorListener onErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int framework_err, int impl_err) {
            if (mMediaEngineInterface != null) mMediaEngineInterface.onError();
            return true;
        }
    };

    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            if (mMediaEngineInterface != null) mMediaEngineInterface.onCompletion();
        }
    };

    private IMediaPlayer.OnInfoListener onInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            if (mMediaEngineInterface != null) mMediaEngineInterface.onInfo(what, extra);
            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            if (mMediaEngineInterface != null) mMediaEngineInterface.onBufferingUpdate(percent);
        }
    };


    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (mMediaEngineInterface != null) mMediaEngineInterface.onPrepared();
        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
            int videoWidth = iMediaPlayer.getVideoWidth();
            int videoHeight = iMediaPlayer.getVideoHeight();
            if (videoWidth != 0 && videoHeight != 0) {
                if (mMediaEngineInterface != null)
                    mMediaEngineInterface.onVideoSizeChanged(videoWidth, videoHeight);
            }
        }
    };

//    private IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
//        @Override
//        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
//            if (mMediaEngineInterface != null) mMediaEngineInterface.OnSeekCompleteListener(iMediaPlayer);
//        }
//    };
}
