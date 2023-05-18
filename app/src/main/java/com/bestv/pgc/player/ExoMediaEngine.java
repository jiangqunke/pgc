package com.bestv.pgc.player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.Map;

public class ExoMediaEngine extends AbstractPlayer implements VideoListener, Player.EventListener{
    protected SimpleExoPlayer mMediaPlayer;
    private Context context;
    protected ExoMediaSourceHelper mMediaSourceHelper;
    private LoadControl mLoadControl;
    private RenderersFactory mRenderersFactory;
    private TrackSelector mTrackSelector;
    private boolean mIsPreparing;
    private boolean mIsBuffering;
    private PlaybackParameters mSpeedPlaybackParameters;
    private int mLastReportedPlaybackState = Player.STATE_IDLE;
    private boolean mLastReportedPlayWhenReady = false;
    private Surface mSurface;
    protected MediaSource mMediaSource;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    public ExoMediaEngine(Context context) {
        this.context = context;
        mMediaSourceHelper = new ExoMediaSourceHelper(context);
    }
    @Override
    public void initPlayer() {
        mLoadControl = new LoadControlWrapper(mLoadControl == null ? new DefaultLoadControl() : mLoadControl);
        mRenderersFactory = mRenderersFactory == null ? new DefaultRenderersFactory(context) : mRenderersFactory;
        mTrackSelector = mTrackSelector == null ? new DefaultTrackSelector(context) : mTrackSelector;
//        mMediaPlayer = ExoPlayerFactory.newSimpleInstance(context, mRenderersFactory, mTrackSelector, mLoadControl);
        mMediaPlayer = new SimpleExoPlayer.Builder(context).setLoadControl(mLoadControl).build();
        mMediaPlayer.addListener(this);
        mMediaPlayer.addVideoListener(this);
        setOptions();
    }

    @Override
    public void setDataSource(String path, Map<String, String> headers) {
        mMediaSource = mMediaSourceHelper.getMediaSource(path);
        mMediaSourceHelper.setHeaders(headers);
    }

    @Override
    public void setDataSource(AssetFileDescriptor fd) {

    }


    @Override
    public void start() {
        if (mMediaPlayer == null)
            return;
        mMediaPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        if (mMediaPlayer == null)
            return;
        mMediaPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        if (mMediaPlayer == null)
            return;
        mMediaPlayer.stop();
    }

    @Override
    public void prepareAsync() {
        if (mMediaPlayer == null)
            return;
        if (mMediaSource == null) return;
        if (mSpeedPlaybackParameters != null) {
            mMediaPlayer.setPlaybackParameters(mSpeedPlaybackParameters);
        }
        if (mSurface != null) {
            mMediaPlayer.setVideoSurface(mSurface);
        }
        mIsPreparing = true;
        mMediaPlayer.prepare(mMediaSource);
    }

    @Override
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop(true);
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer == null)
            return false;
        int state = mMediaPlayer.getPlaybackState();
        switch (state) {
            case Player.STATE_BUFFERING:
            case Player.STATE_READY:
                return mMediaPlayer.getPlayWhenReady();
            case Player.STATE_IDLE:
            case Player.STATE_ENDED:
            default:
                return false;
        }
    }

    @Override
    public void seekTo(long time) {
        if (mMediaPlayer == null)
            return;
        mMediaPlayer.seekTo(time);
    }

    @Override
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.removeListener(this);
            mMediaPlayer.removeVideoListener(this);
            final SimpleExoPlayer player = mMediaPlayer;
            new Thread() {
                @Override
                public void run() {
                    //异步释放，防止卡顿
                    player.release();
                }
            }.start();
            mMediaPlayer = null;
        }

        mHandler.removeCallbacksAndMessages(null);

        mSurface = null;
        mIsPreparing = false;
        mIsBuffering = false;
        mLastReportedPlaybackState = Player.STATE_IDLE;
        mLastReportedPlayWhenReady = false;
        mSpeedPlaybackParameters = null;
    }

    @Override
    public long getCurrentPosition() {
        if (mMediaPlayer == null)
            return 0;
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        if (mMediaPlayer == null)
            return 0;
        return mMediaPlayer.getDuration();
    }

    @Override
    public int getBufferedPercentage() {
        return mMediaPlayer == null ? 0 : mMediaPlayer.getBufferedPercentage();

    }

    @Override
    public void setSurface(Surface surface) {
        mSurface = surface;
        if (mMediaPlayer != null) {
            mMediaPlayer.setVideoSurface(surface);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder holder) {
        if (holder == null)
            setSurface(null);
        else
            setSurface(holder.getSurface());
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null)
            mMediaPlayer.setVolume((leftVolume + rightVolume) / 2);
    }

    @Override
    public void setLooping(boolean isLooping) {
        if (mMediaPlayer != null)
            mMediaPlayer.setRepeatMode(isLooping ? Player.REPEAT_MODE_ALL : Player.REPEAT_MODE_OFF);

    }

    @Override
    public void setEnableMediaCodec(boolean isEnable) {

    }

    @Override
    public void setOptions() {
        mMediaPlayer.setPlayWhenReady(true);
    }

    @Override
    public void setSpeed(float speed) {
        PlaybackParameters playbackParameters = new PlaybackParameters(speed);
        mSpeedPlaybackParameters = playbackParameters;
        if (mMediaPlayer != null) {
            mMediaPlayer.setPlaybackParameters(playbackParameters);
        }
    }

    @Override
    public long getTcpSpeed() {
        return 0;
    }
    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mPlayerEventListener == null) return;
        if (mIsPreparing) return;
        if (mLastReportedPlayWhenReady != playWhenReady || mLastReportedPlaybackState != playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_START, getBufferedPercentage());
                    mIsBuffering = true;
                    break;
                case Player.STATE_READY:
                    if (mIsBuffering) {
                        mPlayerEventListener.onInfo(MEDIA_INFO_BUFFERING_END, getBufferedPercentage());
                        mIsBuffering = false;
                    }
                    break;
                case Player.STATE_ENDED:
                    mPlayerEventListener.onCompletion();
                    break;
            }
            mLastReportedPlaybackState = playbackState;
            mLastReportedPlayWhenReady = playWhenReady;
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onError();
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        if (mPlayerEventListener != null) {
            mPlayerEventListener.onVideoSizeChanged(width, height);
            if (unappliedRotationDegrees > 0) {
                mPlayerEventListener.onInfo(MEDIA_INFO_VIDEO_ROTATION_CHANGED, unappliedRotationDegrees);
            }
        }
    }

    @Override
    public void onRenderedFirstFrame() {
        if (mPlayerEventListener != null && mIsPreparing) {
            mPlayerEventListener.onInfo(MEDIA_INFO_VIDEO_RENDERING_START, 0);
            mIsPreparing = false;
        }
    }
    public void handlerTouch(MotionEvent event) {
    }

    /**
     * LoadControl包装类。用来监听onPrepared状态
     */
    private class LoadControlWrapper implements LoadControl {

        private LoadControl mLoadControl;

        LoadControlWrapper(LoadControl loadControl) {
            mLoadControl = loadControl;
        }

        @Override
        public void onPrepared() {
            mLoadControl.onPrepared();
            //切换到主线程
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mPlayerEventListener != null) {
                        mPlayerEventListener.onPrepared();
                    }
                }
            });
        }

        @Override
        public void onTracksSelected(Renderer[] renderers, TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            mLoadControl.onTracksSelected(renderers, trackGroups, trackSelections);
        }

        @Override
        public void onStopped() {
            mLoadControl.onStopped();
        }

        @Override
        public void onReleased() {
            mLoadControl.onReleased();
        }

        @Override
        public Allocator getAllocator() {
            return mLoadControl.getAllocator();
        }

        @Override
        public long getBackBufferDurationUs() {
            return mLoadControl.getBackBufferDurationUs();
        }

        @Override
        public boolean retainBackBufferFromKeyframe() {
            return mLoadControl.retainBackBufferFromKeyframe();
        }

        @Override
        public boolean shouldContinueLoading(long bufferedDurationUs, float playbackSpeed) {
            return mLoadControl.shouldContinueLoading(bufferedDurationUs, playbackSpeed);
        }

        @Override
        public boolean shouldStartPlayback(long bufferedDurationUs, float playbackSpeed, boolean rebuffering) {
            return mLoadControl.shouldStartPlayback(bufferedDurationUs, playbackSpeed, rebuffering);
        }
    }
}
