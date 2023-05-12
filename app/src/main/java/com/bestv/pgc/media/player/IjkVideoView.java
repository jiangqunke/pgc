package com.bestv.pgc.media.player;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bestv.ijkplayer.player.IjkMediaPlayer;
import com.bestv.pgc.media.ResizeSurfaceView;
import com.bestv.pgc.media.ResizeTextureView;


/**
 * 播放器
 */

public class IjkVideoView extends BaseIjkVideoView {
    protected ResizeSurfaceView mSurfaceView;
    protected ResizeTextureView mTextureView;
    protected SurfaceTexture mSurfaceTexture;
    protected FrameLayout playerContainer;
    protected boolean isFullScreen;//是否处于全屏状态

    public static final int SCREEN_SCALE_DEFAULT = 0;
    public static final int SCREEN_SCALE_16_9 = 1;
    public static final int SCREEN_SCALE_4_3 = 2;
    public static final int SCREEN_SCALE_MATCH_PARENT = 3;
    public static final int SCREEN_SCALE_ORIGINAL = 4;
    public static final int SCREEN_SCALE_HEIGHT = 5;
    protected int mCurrentScreenScale = SCREEN_SCALE_DEFAULT;

    public IjkVideoView(@NonNull Context context) {
        this(context, null);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    /**
     * 初始化播放器视图
     */
    protected void initView() {
        playerContainer = new FrameLayout(getContext());
        playerContainer.setBackgroundColor(Color.BLACK);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(playerContainer, params);
    }

    /**
     * 创建播放器实例，设置播放地址及播放器参数
     */
    @Override
    protected void initPlayer() {
        super.initPlayer();
        addDisplay();
    }

    protected void addDisplay() {
        if (mPlayerConfig.usingSurfaceView) {
            addSurfaceView();
        } else {
            addTextureView();
        }
    }

    @Override
    protected void setPlayState(int playState) {
    }

    @Override
    protected void setPlayerState(int playerState) {
    }

    @Override
    protected void startPlay() {
//        if (checkNetwork()) return;
        super.startPlay();
    }

    /**
     * 添加SurfaceView
     */
    private void addSurfaceView() {
        playerContainer.removeView(mSurfaceView);
        mSurfaceView = new ResizeSurfaceView(getContext());
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setDisplay(holder);
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        playerContainer.addView(mSurfaceView, 0, params);
    }

    /**
     * 添加TextureView
     */
    private void addTextureView() {
        playerContainer.removeView(mTextureView);
        mSurfaceTexture = null;
        mTextureView = new ResizeTextureView(getContext());
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                if (mSurfaceTexture != null) {
                    mTextureView.setSurfaceTexture(mSurfaceTexture);
                } else {
                    mSurfaceTexture = surfaceTexture;
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
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);
        playerContainer.addView(mTextureView, 0, params);
    }



    @Override
    public void release() {
        super.release();
        playerContainer.removeView(mTextureView);
        playerContainer.removeView(mSurfaceView);
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCurrentScreenScale = SCREEN_SCALE_DEFAULT;
    }

    @Override
    public void startFullScreen() {
    }

    @Override
    public void stopFullScreen() {
    }


    @Override
    public void onPrepared() {
        super.onPrepared();
        if (mPlayerConfig.usingAndroidMediaPlayer) mMediaPlayer.start();
    }

    @Override
    public void onError() {
        super.onError();

        addDisplay();
        mMediaPlayer.reset();
        startPrepare(true);
//        playerContainer.removeView(statusView);
//        if (statusView == null) {
//            statusView = new StatusView(getContext());
//        }
//        statusView.setMessage(getResources().getString(R.string.error_message));
//        statusView.setButtonTextAndAction(getResources().getString(R.string.retry), new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                playerContainer.removeView(statusView);
//                addDisplay();
//                mMediaPlayer.reset();
//                startPrepare(true);
//            }
//        });
//        playerContainer.addView(statusView);
    }

    @Override
    public void onInfo(int what, int extra) {
        super.onInfo(what, extra);
        switch (what) {
            case IjkMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                if (mTextureView != null)
                    mTextureView.setRotation(extra);
                break;
        }
    }

    @Override
    public void onVideoSizeChanged(int videoWidth, int videoHeight) {
        if (mPlayerConfig.usingSurfaceView) {
            mSurfaceView.setScreenScale(mCurrentScreenScale);
            mSurfaceView.setVideoSize(videoWidth, videoHeight);
        } else {
            mTextureView.setScreenScale(mCurrentScreenScale);
            mTextureView.setVideoSize(videoWidth, videoHeight);
        }
    }


    /**
     * 设置视频地址
     */
    public void setUrl(String url) {
        this.mCurrentUrl = url;
    }

    /**
     * 一开始播放就seek到预先设置好的位置
     */
    public void skipPositionWhenPlay(String url, int position) {
        this.mCurrentUrl = url;
        this.mCurrentPosition = position;
    }

    /**
     * 设置标题
     */
    public void setTitle(String title) {
        if (title != null) {
            this.mCurrentTitle = title;
        }
    }

    /**
     * 设置视频比例
     */
    @Override
    public void setScreenScale(int screenScale) {
        this.mCurrentScreenScale = screenScale;
        if (mSurfaceView != null) mSurfaceView.setScreenScale(screenScale);
        if (mTextureView != null) mTextureView.setScreenScale(screenScale);
    }
}
