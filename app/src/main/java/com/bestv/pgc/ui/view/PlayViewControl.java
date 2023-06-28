package com.bestv.pgc.ui.view;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.analysys.AnalysysAgent;
import com.bestv.pgc.R;
import com.bestv.pgc.beans.FunctionSpeedModel;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.expand.ExpandableTextView;
import com.bestv.pgc.expand.StatusType;
import com.bestv.pgc.player.AbstractPlayer;
import com.bestv.pgc.player.ExoVideoView;
import com.bestv.pgc.player.SystemUtils;
import com.bestv.pgc.player.VideoListener;
import com.bestv.pgc.ui.PlaylistActivity;
import com.bestv.pgc.ui.SpeedAdapter;
import com.bestv.pgc.util.NiceUtil;
import com.bestv.pgc.util.PlayerCountTimer;
import com.bestv.pgc.util.StringUtil;
import com.bestv.pgc.util.TimeSwitchUtils;
import com.bestv.pgc.util.WindowUtil;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayViewControl extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, VideoListener {
    private Context context;
    private ExoVideoView videoView;
    private ImageView iv_full;
    private RelativeLayout rl_portrait_play;
    private RelativeLayout rl_playtime;
    private ImageView iv_bg;
    private TextView tvProgress;
    private TextView tvDuration;
    private TextView tv_describe, tv_describe_all;
    private ImageView iv_expand;
    private MySlideRelativeLayout rlSeekbar;
    private RelativeLayout rl_titkok_seekbar;
    private CustomSeekBar seekbar;
    private TiktokLoadingView animator_iv;
    private ImageView iconPause;
    private ImageView iv_praise;
    private TextView tv_praise;
    private SpotFullScreenTopView fullScreenTopView;
    SpotFullScreenBottomView fullScreenBottomView;
    PortraitCenterControlView portraitCenterControlView;
    private GestureDetector mGestureDetector;
    private boolean isFullscreen;
    private boolean isLock;
    private boolean isPlaying = true;
    private Rect mChangeImageBackgroundRect = null;
    private SpotBean data;
    //显示倍速
    private boolean isShowSpeed = false;
    private float curSpeed = 1.0f;
    private boolean isLongPress;
    private boolean isRightScroll;
    private boolean isLeftScroll;
    private boolean isHorizontalScroll;
    public long startScrollPosition;
    private double mPositionData = -1;
    private int cutVolume;
    private double duationTime;
    private long currentPosition;
    private boolean isAnim;
    private ConstraintLayout llDescribe;
    private boolean isShowMore = true;
    public PlayerCountTimer countTimer;
    private long play_length;
    private List<FunctionSpeedModel> speedDatas = new ArrayList<>();
    private boolean isPraise = false;
    private OnPlayerStateCallBack pausedCallBack;
    private int progress;
    private InterceptTouchListening interceptTouchListening;
    private OnFinishListening onFinishListening;
    private boolean isFragmentVisible = false;
    private RelativeLayout rl_speed_select;
    private RecyclerView speedRecycleView;
    private SpeedAdapter speedAdapter;
    private ExpandableTextView tv_expand;
    private LinearLayout ll_praise;



    public long getPlay_length() {
        return play_length;
    }

    public void setPlay_length(long play_length) {
        this.play_length = play_length;
    }
    public InterceptTouchListening getInterceptTouchListening() {
        return interceptTouchListening;
    }

    public void setInterceptTouchListening(InterceptTouchListening interceptTouchListening) {
        this.interceptTouchListening = interceptTouchListening;
    }

    public OnFinishListening getOnFinishListening() {
        return onFinishListening;
    }

    public void setOnFinishListening(OnFinishListening onFinishListening) {
        this.onFinishListening = onFinishListening;
    }

    public boolean isFragmentVisible() {
        return isFragmentVisible;
    }

    public void setFragmentVisible(boolean fragmentVisible) {
        isFragmentVisible = fragmentVisible;
    }

    public void hideBgImage() {
        iv_bg.setVisibility(GONE);
    }


    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public OnPlayerStateCallBack getPausedCallBack() {
        return pausedCallBack;
    }

    public void setPausedCallBack(OnPlayerStateCallBack pausedCallBack) {
        this.pausedCallBack = pausedCallBack;
    }

    public void resetSeekProgress() {
        setSeekProgress(0);
        stopPlayTimer();
    }

    public PlayViewControl(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public PlayViewControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public PlayViewControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public PlayViewControl(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init();
    }

    private void init() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.pgc_view_control_layout, this);
        videoView = root.findViewById(R.id.video_view);
//        videoView.setScreenScale(ExoVideoView.SCREEN_SCALE_MATCH_PARENT);
        videoView.setLooping(true);
        iv_expand = root.findViewById(R.id.iv_expand);
        ll_praise = root.findViewById(R.id.ll_praise);
        iv_bg = root.findViewById(R.id.iv_bg);
        iv_full = root.findViewById(R.id.iv_full);
        llDescribe = root.findViewById(R.id.ll_describe);
        tv_praise = root.findViewById(R.id.tv_praise);
        tv_expand = root.findViewById(R.id.tv_expand);
        iv_praise = root.findViewById(R.id.iv_praise);
        rl_portrait_play = root.findViewById(R.id.rl_portrait_play);
        rl_playtime = root.findViewById(R.id.rl_playtime);
        tvProgress = root.findViewById(R.id.tv_progress);
        tvDuration = root.findViewById(R.id.tv_duration);
        tv_describe = root.findViewById(R.id.tv_describe);
        tv_describe_all = root.findViewById(R.id.tv_describe_all);
        rlSeekbar = root.findViewById(R.id.rl_seekbar);
        rl_titkok_seekbar = root.findViewById(R.id.rl_titkok_seekbar);
        seekbar = root.findViewById(R.id.seekbar);
        animator_iv = root.findViewById(R.id.animator_iv);
        iconPause = root.findViewById(R.id.icon_pause);
        fullScreenTopView = root.findViewById(R.id.ll_full_top_view);
        fullScreenBottomView = root.findViewById(R.id.rl_full_bottom_view);
        portraitCenterControlView = root.findViewById(R.id.portrait_center_control_view);
        rl_speed_select = root.findViewById(R.id.rl_speed_select);
        speedRecycleView = root.findViewById(R.id.drawer_speed_recycleview);
        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    notifyOnMPScrollEnd();
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    Log.e("event", "event_canel");
//                    if (interceptTouchListening != null){
//                        interceptTouchListening.interceptTouch(false);
//                    }
                }

                if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
                    if (isLongPress) {
                        isLongPress = false;
                        notifyOnMPLongCancel();
                    } else if (isRightScroll) {
                        isRightScroll = false;
                        notifyScrollCancel();
                    } else if (isLeftScroll) {
                        isLeftScroll = false;
                        notifyScrollCancel();
                    } else if (isHorizontalScroll) {
                        isHorizontalScroll = false;
                        notifyHorizontalScrollCancel();
                    }
                }
                return mGestureDetector.onTouchEvent(event);
            }
        });
        seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot));
        seekbar.setOnSeekBarChangeListener(this);
        animator_iv.setTimePeriod(10);
        rlSeekbar.setmSetOnSlideListener(new MySlideRelativeLayout.setOnSlideListener() {

            @Override
            public void onSingleClick() {
                seekbar.setProgress(progress);
                int time = (int) (progress * duationTime / seekbar.getMax()) * 1000;
                videoView.seekTo(time);
            }
        });
        rlSeekbar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("event", "event_up" + event + "left=" + seekbar.getProgress());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    progress = seekbar.getProgress();
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (interceptTouchListening != null) {
                        interceptTouchListening.interceptTouch(true);
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                    hidePlayTimeView();
                }
                Rect seekRect = new Rect();
                seekbar.getHitRect(seekRect);
                if ((event.getY() >= (seekRect.top - 5500)) && (event.getY() <= (seekRect.bottom + 5500))) {
                    float y = seekRect.top + seekRect.height() / 2;
                    //seekBar only accept relative x
                    float x = event.getX() - seekRect.left;
                    if (x < 0) {
                        x = 0;
                    } else if (x > seekRect.width()) {
                        x = seekRect.width();
                    }
                    MotionEvent me = MotionEvent.obtain(event.getDownTime(), event.getEventTime(),
                            event.getAction(), x, y, event.getMetaState());
                    return seekbar.onTouchEvent(me);

                }
                return false;
            }
        });
        initPlayTimer();
        cutVolume = SystemUtils.getSystemCurrentVolume(getContext());
    }

    public void setVideoData(SpotBean data) {
        this.data = data;
        duationTime  =data.getDuration();
        String text = TimeSwitchUtils.secToTime((int) (data.getDuration()));
        tvDuration.setText(text);
        seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot));
        seekbar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot));
        videoView.setUrl(!TextUtils.isEmpty(data.getDownloadQualityUrl()) ? data.getDownloadQualityUrl() : data.getQualityUrl());
        if (!TextUtils.isEmpty(data.getBgCover())) {
            iv_bg.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(data.getBgCover())
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error(R.mipmap.zwt_all_adult))
                    .into(iv_bg);
        } else {
            iv_bg.setVisibility(View.GONE);
        }
        tv_expand.setExpandOrContractClickListener(new ExpandableTextView.OnExpandOrContractClickListener() {
            @Override
            public void onClick(StatusType type) {
//                iv_expand.setImageDrawable(ContextCompat.getDrawable(context, isExpand ? R.mipmap.icon_unexpand : R.mipmap.icon_expand));
//                tv_describe_all.setVisibility(isExpand ? VISIBLE : GONE);
//                tv_describe.setVisibility(isExpand ? GONE : VISIBLE);
//                if (type.equals(StatusType.STATUS_CONTRACT)) {
//                    isExpand = false;
//                    iv_expand.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_expand));
//                } else {
//                    isExpand = true;
//                    iv_expand.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.icon_unexpand ));
//                }
            }
        });
        iv_praise.setOnClickListener(this);
        iv_expand.setOnClickListener(this);
        videoView.setVideoListener(this);
        rl_playtime.setVisibility(GONE);
        if (!TextUtils.isEmpty(data.getTitle())) {
            tv_expand.setContent(data.getTitle());
            llDescribe.setVisibility(VISIBLE);
            tv_describe.setVisibility(GONE);
//            tv_expand.setOnGetLineCountListener(new ExpandableTextView.OnGetLineCountListener() {
//                @Override
//                public void onGetLineCount(int lineCount, boolean canExpand) {
//                        iv_expand.setVisibility(canExpand ? VISIBLE : GONE);
//                }
//            });
//            tv_describe.setText(data.getTitle());
//            tv_expand.post(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        int ellipsisCount = tv_expand.getExpandableLineCount();
//                        Log.e("expand", "expand=" + tv_expand.getExpandableLineCount());
//                        iv_expand.setVisibility(ellipsisCount > 2 ? VISIBLE : GONE);
////                        iv_expand.setVisibility(ellipsisCount > 0 ? VISIBLE : GONE);
//                    } catch (Exception exception) {
//                        exception.printStackTrace();
//                    }
//                }
//            });

        } else {
            llDescribe.setVisibility(GONE);
        }
        if (data.getPraiseCount() > 0) {
            tv_praise.setText(StringUtil.getNum("" + data.getPraiseCount()));
        } else {
            tv_praise.setText("点赞");
        }
        isPraise = data.isPraise();
        if (isPraise) {
            iv_praise.setImageResource(R.mipmap.icon_praise);
        } else {
            iv_praise.setImageResource(R.mipmap.icon_praise_no);
        }

        iv_full.setOnClickListener(this);
        rl_speed_select.setOnClickListener(this);
        if (isNeedFullScreenVideo()) {
            iv_full.setVisibility(VISIBLE);
        } else {
            iv_full.setVisibility(GONE);
        }
        if (portraitCenterControlView != null) {
            portraitCenterControlView.setupSpotProgressOrVolume();
            portraitCenterControlView.setLockListening(new PortraitCenterControlView.SpotCenterViewListening() {
                @Override
                public void lockClick(boolean isLock) {
                    showOrHideLockView(isLock);
                }

                @Override
                public void longClick() {
                    setCurSpeed(2.0f);
                }
            });
        }
        if (fullScreenTopView != null) {
            fullScreenTopView.setSpotTopViewListening(new SpotFullScreenTopView.SpotTopViewClickCallBack() {
                @Override
                public void backClick() {
                    showOrHideFullScreen(false);
                }


                @Override
                public void pariseClick() {
                    userPraise();
                }
            });
            fullScreenTopView.setVideoData(data);
        }
        if (fullScreenBottomView != null) {
            fullScreenBottomView.notifyDoubleClick(true);
            fullScreenBottomView.setVideoData(data);
            fullScreenBottomView.setSpotBottomViewListening(new SpotFullScreenBottomView.SpotBottomViewClickCallBack() {
                @Override
                public void playClick() {
                    notifyDoubleClick(0, 0);
                }

                @Override
                public void speedClick() {
                    showSelectSpeed();
                }

                @Override
                public void stopTrackingTouch(SeekBar fullSeekBar) {
                    seekbar.setProgress(fullSeekBar.getProgress());
                    onStopTrackingTouch(seekbar);
                    if (fullScreenTopView != null) {
                        fullScreenTopView.notifyOnMPScrollEnd();
                    }
                    if (fullScreenBottomView != null) {
                        fullScreenBottomView.notifyOnMPScrollEnd();
                    }
                }

                @Override
                public void startTrackingTouch(SeekBar fullSeekBar) {
                    onStartTrackingTouch(seekbar);
                    if (fullScreenTopView != null) {
                        fullScreenTopView.notifyScrollStart();
                    }
                    if (fullScreenBottomView != null) {
                        fullScreenBottomView.notifyScrollStart();
                    }
                }

                @Override
                public void bottomViewShowOrHided(boolean isShow) {
                    portraitCenterControlView.showOrHideLockView(isShow);
                    if (fullScreenTopView != null) {
                        fullScreenTopView.showOrHideTopView(isShow);
                    }
                }
            });
            fullScreenBottomView.setupProgressPosition();
        }
        setupVideoHeight();
    }

    public void resumeVideo() {
        if (videoView != null) {
            videoView.resume();
        }
    }

    //显示隐藏锁屏UI
    private void showOrHideLockView(boolean isLock) {
        this.isLock = isLock;
        if (isLock) {
            fullScreenTopView.hideControlView();
            fullScreenBottomView.hideControlView();
        } else {
            fullScreenTopView.showControlView();
            fullScreenBottomView.showControlView();
        }

    }

    private boolean isExpand = false;

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.iv_full) {
            showOrHideFullScreen(true);
        } else if (id == R.id.rl_speed_select) {
            rl_speed_select.setVisibility(GONE);
        } else if (id == R.id.iv_expand) {//                isExpand = !isExpand;
//                tv_expand.setCurrStatus(isExpand?StatusType.STATUS_EXPAND:StatusType.STATUS_CONTRACT);
//                iv_expand.setImageDrawable(ContextCompat.getDrawable(context, isExpand ? R.mipmap.icon_unexpand : R.mipmap.icon_expand));
//                tv_describe_all.setVisibility(isExpand ? VISIBLE : GONE);
//                tv_describe.setVisibility(isExpand ? GONE : VISIBLE);
        } else if (id == R.id.iv_praise) {
            userPraise();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String text = TimeSwitchUtils.secToTime((int) (progress / 100.0f * duationTime));
        if (tvProgress != null) {
            tvProgress.setText(text);
        }
        if (!fromUser) {
        } else {
            rl_playtime.setVisibility(View.VISIBLE);
            llDescribe.setVisibility(INVISIBLE);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_tiktok_scroll_btn));
        seekBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot_select));
        rl_playtime.setVisibility(View.VISIBLE);
        tvProgress.setTextSize(25);
        ll_praise.setVisibility(INVISIBLE);
        llDescribe.setVisibility(INVISIBLE);
        if (interceptTouchListening != null) {
            interceptTouchListening.interceptTouch(true);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        hidePlayTimeView(seekBar);
    }

    @Override
    public void startPrepare() {
        animator_iv.setVisibility(VISIBLE);
    }

    @Override
    public void onComplete() {
        if (isFullscreen) {
            videoView.seekTo(0);
        } else {
            if (pausedCallBack != null) {
                pausedCallBack.playComplete();
            }
        }
    }

    @Override
    public void onPrepared() {
        animator_iv.setVisibility(GONE);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onInfo(int what, int extra) {
        switch (what) {
            case AbstractPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                iv_bg.setVisibility(GONE);
                startPlayTimer();
                if (!isFragmentVisible) {
                    videoView.pause();
                    pausePlayTimer();
                }
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_START:
                animator_iv.setVisibility(VISIBLE);
                pausePlayTimer();
                break;
            case AbstractPlayer.MEDIA_INFO_BUFFERING_END:
                animator_iv.setVisibility(GONE);
                resumePlayTimer();
                break;
        }
    }

    @Override
    public void onProgress(int progress, long currentPosition, long duration) {
        try {
            if (duration > 0) {
                if (duationTime == 0) {
                    duationTime = duration / 1000.0f;
                    String text = TimeSwitchUtils.secToTime((int) (duationTime));
                    tvDuration.setText(text);
                }
                int playingTime = (int) (currentPosition / 1000.0f);
                setSeekProgress(progress);
                this.currentPosition = currentPosition;
                if (currentPosition / 1000.0f >= duration) {
                    data.setPlayFinish(true);
                }
                data.setPlayDuration(playingTime);
                if (fullScreenBottomView != null) {
                    fullScreenBottomView.updateProgress(progress, playingTime);
                }
            }
            cutVolume = SystemUtils.getSystemCurrentVolume(getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSeekProgress(int progress) {
        if (seekbar != null) {
            seekbar.setProgress(progress);
        }
    }

    private void hidePlayTimeView(SeekBar mSeekBar) {
        Log.e("event", "event_up");
        if (rl_playtime.getVisibility() == VISIBLE) {
            if (!isFullscreen) {
                mSeekBar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot));
                mSeekBar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot));
                rl_playtime.setVisibility(View.GONE);
                tvProgress.setTextSize(18);
                llDescribe.setVisibility(VISIBLE);
                if (isNeedFullScreenVideo()) {
                    iv_full.setVisibility(VISIBLE);
                }
                ll_praise.setVisibility(VISIBLE);
            }
            fullScreenBottomView.notifyDoubleClick(true);
            iconPause.setVisibility(View.GONE);
            float progress = seekbar.getProgress() / 100f;
            int time = (int) (seekbar.getProgress() * duationTime / seekbar.getMax()) * 1000;
            Log.e("play","progress1="+time);
//            seekbar.setProgress(seekbar_scroll.getProgress());
            if (time > videoView.getCurrentPosition()) {
//                interactWithVideo("向前拖动进度条");
            } else {
//                interactWithVideo("向后拖动进度条");
            }
            if (progress == 1.0f) {
                videoView.seekTo(time - 10);
            } else {
                videoView.seekTo(time);
            }
            isPlaying = true;
            videoView.resume();
//            resumePlayTimer();
            if (pausedCallBack != null) {
                pausedCallBack.playerPaused(false);
            }
            data.setPause(false);
            updateView();
        }

    }

    /**
     * 用户点赞
     */
    public void userPraise() {
        if (pausedCallBack != null) {
            pausedCallBack.userPraise(isPraise, TextUtils.isEmpty(data.getTitleId()) ? "" : data.getTitleId());
        }
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        private boolean firstTouch;
        private boolean mChangePosition;
        private boolean mChangeBrightness;
        private boolean mChangeVolume;
        private int streamVolume;
        private float mBrightness;
        private int streamMaxVolume;

        @Override
        public boolean onDown(MotionEvent e) {
            if (WindowUtil.isSmallEdge(context, e)) return super.onDown(e);
            Log.e("touch1", "touch=" + e);
            streamVolume = SystemUtils.getSystemCurrentVolume(context);
            streamMaxVolume = SystemUtils.getSystemMAXVolume(context);
            mBrightness = WindowUtil.scanForActivity(context).getWindow().getAttributes().screenBrightness;
            firstTouch = true;
            mChangePosition = false;
            mChangeBrightness = false;
            mChangeVolume = false;
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (isFullscreen && (isInChangeImageZone(fullScreenTopView, (int) e.getX(), (int) e.getY()) || isInChangeImageZone(fullScreenBottomView, (int) e.getX(), (int) e.getY()))) {
                return true;
            }
            notifyClick();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (context == null /*|| !ijkVrVideoView.isFullScreen()*/) {
                return false;
            }
            if (WindowUtil.isSmallEdge(context, e1) /*|| !ijkVrVideoView.isFullScreen()*/)
                return super.onScroll(e1, e2, distanceX, distanceY);
            Log.e("touch2", "touch=" + e1);
            float deltaX = e1.getX() - e2.getX();
            float deltaY = e1.getY() - e2.getY();
            if (firstTouch) {
//                mChangePosition = Math.abs(distanceX) >= Math.abs(distanceY);
                if ((Math.abs(distanceX) - Math.abs(distanceY) > 10)) {
                    mChangePosition = true;
                }
                firstTouch = false;
                if (!mChangePosition) {
                    if (e2.getX() < WindowUtil.getScreenWidth(context) / 3) {
                        mChangeBrightness = true;
                    } else if (e2.getX() > WindowUtil.getScreenWidth(context) * 2 / 3) {
                        mChangeVolume = true;
                    }
                }
            }
            if (!isLock) {
                if (mChangePosition) {
                    slideToChangePosition(distanceX);
                } else if (mChangeBrightness) {
                    slideToChangeBrightness(deltaY);
                } else if (mChangeVolume) {
                    slideToChangeVolume(deltaY);
                }
            }

            return true;
        }

        @Override

        public void onLongPress(MotionEvent e) {
            if (WindowUtil.isSmallEdge(context, e) || isLock || !isPlaying)
                return;
            if (e.getY() > getResources().getDimensionPixelOffset(R.dimen.dp_100)) {
                if (isFullscreen && e.getY() > ScreenUtils.getScreenHeight() - getResources().getDimensionPixelOffset(R.dimen.dp_100)) {
                    return;
                }
                isLongPress = true;
                notifyLongClick(e.getX() >= WindowUtil.getScreenWidth(context) / 2);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (isLock) return true;
            notifyDoubleClick((int) e.getX(), (int) e.getY());
            return true;
        }


        protected void slideToChangePosition(float deltaX) {
            if (isFullscreen) {
                isHorizontalScroll = true;
                startScrollPosition = videoView.getCurrentPosition();
                int mDuration = videoView.getDuration();
                if (mPositionData == -1) {
                    mPositionData = videoView.getCurrentPosition();
                }
                if (deltaX > -3 && deltaX < 3) {
                    return;
                }
                deltaX = -(int) deltaX / 3;
                int positionAdjustProgress = (int) (deltaX * mDuration / 60 / 60);
                if (duationTime <= 3 * 60) {//片长小于3分钟的特殊处理
                    positionAdjustProgress = (int) (deltaX * mDuration / 60 / 5);
                }
                mPositionData = mPositionData + positionAdjustProgress;
                if (mPositionData < 0) {
                    mPositionData = 0;
                } else if (mPositionData > mDuration) {
                    mPositionData = mDuration;
                }
                if (portraitCenterControlView != null) {
                    portraitCenterControlView.showTitktokSeek();
//                    portraitCenterControlView.updatePlayerInfo((int) (mPositionData / 1000), mDuration / 1000, deltaX > 0);
                    String targetPosition = TimeSwitchUtils.secToTime((int) (mPositionData / 1000));
                    tvProgress.setText(targetPosition);
                    int progress = (int) (mPositionData / 1000 * 1.0f / duationTime * 100);
                    fullScreenBottomView.updateProgress(progress, (int) (mPositionData / 1000));
//                    seekbar.setProgress(progress);
//                    tv_dlna_progress_time.setText(targetPosition);
                }
                fullScreenTopView.notifyScrollStart();
                fullScreenBottomView.notifyScrollStart();
            }
        }

        protected void slideToChangeBrightness(float deltaY) {
            isLeftScroll = true;
            int height = WindowUtil.getScreenHeight(context);
            if (mBrightness == -1.0f) mBrightness = 0.5f;
            float brightness = deltaY * 2 / height * 1.0f + mBrightness;
            if (brightness < 0) {
                brightness = 0f;
            }
            if (brightness > 1.0f) brightness = 1.0f;
            //转化成实际亮度
            SystemUtils.setWindowBrightness(context, brightness);
            notifyChangeBrightness(brightness);
        }

        protected void slideToChangeVolume(float deltaY) {
            isRightScroll = true;
            int height = WindowUtil.getScreenHeight(context);
            float deltaV = deltaY / height;

            //设置的系统音量
            float realProgress = streamVolume * 1f / streamMaxVolume + deltaV;
            if (realProgress < 0) {
                realProgress = 0;
            } else if (realProgress > 1f) {
                realProgress = 1f;
            }
            //转化成实际音量
            SystemUtils.setSystemVolume(context, (int) (realProgress * streamMaxVolume));
            notifyChangeVolume(realProgress, deltaV > 0);
        }
    }

    public void notifyDoubleClick(int enentX, int eventY) {
        if (isFullscreen) {
            if (isLock) return;

            if (isPlaying && videoView.isPlaying()) {
                isPlaying = false;
                videoView.pause();
//                pausePlayTimer();
                pausedCallBack.playerPaused(true);
                iconPause.setVisibility(View.VISIBLE);
                seekbar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot_select));
                seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot_seek));
                data.setPause(true);
                rl_playtime.setVisibility(VISIBLE);
                llDescribe.setVisibility(INVISIBLE);
            } else {
                seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot));
                seekbar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot));
                isPlaying = true;
                videoView.resume();
//                resumePlayTimer();
                pausedCallBack.playerPaused(false);
                iconPause.setVisibility(View.GONE);
                data.setPause(false);
                rl_playtime.setVisibility(GONE);
                if (isShowMore) {
                    llDescribe.setVisibility(VISIBLE);
                }
                updateView();
            }
            if (fullScreenBottomView != null) {
                fullScreenBottomView.notifyDoubleClick(isPlaying);
            }
            if (fullScreenTopView != null) {
                fullScreenTopView.notifyDoubleClick(isPlaying);
            }
            return;
        }
        isAnim = true;
        if (!isPraise) {
            userPraise();
        }

//        final ImageView imageView = new ImageView(getContext());
//        LayoutParams params = new LayoutParams(getContext().getResources().getDimensionPixelSize(R.dimen.dp_160), getContext().getResources().getDimensionPixelSize(R.dimen.dp_214));
//        params.leftMargin = enentX - getContext().getResources().getDimensionPixelSize(R.dimen.dp_80);
//        params.topMargin = eventY - getContext().getResources().getDimensionPixelSize(R.dimen.dp_214);
////            imageView.setImageResource(R.drawable.tiktok_double_praise);
//        imageView.setLayoutParams(params);
//        addView(imageView);
//        ImageAnimation.giveStart(imageView);
//        AnimationDrawable anim = (AnimationDrawable) imageView.getDrawable();
//        int durationTime = 0;
//        for (int i = 0; i < anim.getNumberOfFrames(); i++) {
//            durationTime += anim.getDuration(i);
//        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                removeView(imageView);
//                isAnim = false;
//            }
//        }, durationTime);
    }

    public void notifyChangeVolume(float realProgress, boolean isAddVolume) {
        portraitCenterControlView.notifyChangeVolume(realProgress);
        cutVolume = SystemUtils.getSystemCurrentVolume(getContext());
    }

    public void notifyChangeBrightness(float realProgress) {
        portraitCenterControlView.notifyChangeBrightness(realProgress);
    }

    public void notifyClick() {
        if (isFullscreen) {
            if (isLock) return;
//            if (rl_speed_select.getVisibility() == VISIBLE) {
//                isShowMoreDrawer = false;
//                isShowSpeed = false;
//                showOrHideSpeedViewAnimator(false);
//                return;
//            }
//            fullScreenTopView.hideOrShowControlView();
            fullScreenBottomView.hideOrShowControlView();
            return;
        }
        setupFounctionView(true);
        if (isAnim) return;
        if (isPlaying && videoView.isPlaying()) {
//            seekbar_scroll.setVisibility(VISIBLE);
//            seekbar.setVisibility(GONE);
            isPlaying = false;
            videoView.pause();
            if (isNeedFullScreenVideo()) {
                iv_full.setVisibility(GONE);
            }
            pausePlayTimer();
            pausedCallBack.playerPaused(true);
            iconPause.setVisibility(View.VISIBLE);
            seekbar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot_select));
            seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot_seek));
            data.setPause(true);
            ll_praise.setVisibility(INVISIBLE);
            rl_playtime.setVisibility(VISIBLE);
            llDescribe.setVisibility(INVISIBLE);
            if (fullScreenBottomView != null) {
                fullScreenBottomView.notifyDoubleClick(false);
            }
        } else {
            if (isNeedFullScreenVideo()) {
                iv_full.setVisibility(VISIBLE);
            }
//            seekbar_scroll.setVisibility(GONE);
//            seekbar.setVisibility(VISIBLE);
            seekbar.setThumb(ContextCompat.getDrawable(context, R.drawable.shape_seekbar_btn_spot));
            seekbar.setProgressDrawable(ContextCompat.getDrawable(context, R.drawable.seekbar_shape_spot));
            isPlaying = true;
            videoView.resume();
            resumePlayTimer();
            ll_praise.setVisibility(VISIBLE);
            pausedCallBack.playerPaused(false);
            iconPause.setVisibility(View.GONE);
            data.setPause(false);
            rl_playtime.setVisibility(GONE);
            if (isShowMore) {
                llDescribe.setVisibility(VISIBLE);
            }
            updateView();
            if (fullScreenBottomView != null) {
                fullScreenBottomView.notifyDoubleClick(true);
            }

        }
    }

    public void updatePraiseState(boolean isPraise) {
        if (fullScreenTopView != null) {
            fullScreenTopView.updatePariseState(isPraise);
        }
        data.setPraise(isPraise);
        this.isPraise = isPraise;
        if (data.isPraise()) {
            iv_praise.setImageResource(R.mipmap.icon_praise);
            data.setPraiseCount(data.getPraiseCount() + 1);
        } else {
            iv_praise.setImageResource(R.mipmap.icon_praise_no);
            if (data.getPraiseCount() > 0) {
                data.setPraiseCount(data.getPraiseCount() - 1);
            }
        }
        if (data.getPraiseCount() > 0) {
            tv_praise.setText(StringUtil.getNum("" + data.getPraiseCount()));
        } else {
            tv_praise.setText("点赞");
        }
        userLike(isPraise);
    }

    public void updateView() {
        isPraise = data.isPraise();
//        if (isPraise) {
//            iv_praise.setImageResource(R.drawable.tiktok_give_no_show);
//        } else {
//            iv_praise.setImageResource(R.drawable.tiktok_give_show);
//        }
//        if (data.getPraiseCount() > 0) {
//            tv_praise.setText(Utils.getNum("" + data.getPraiseCount()));
//        } else {
//            tv_praise.setText("点赞");
//        }
//        tv_praise.setText(Utils.getNum("" + data.getPraiseCount()));
    }

    public void setupFounctionView(boolean isVisible) {
        isShowMore = isVisible;
        llDescribe.setVisibility(isVisible ? VISIBLE : INVISIBLE);
        rlSeekbar.setVisibility(isVisible ? VISIBLE : INVISIBLE);
        if (isVisible) {
            if (!isPlaying) {
                iconPause.setVisibility(VISIBLE);
                rl_playtime.setVisibility(VISIBLE);
                llDescribe.setVisibility(INVISIBLE);

            } else {
                iconPause.setVisibility(INVISIBLE);
                rl_playtime.setVisibility(INVISIBLE);
            }
        } else {
            if (!isPlaying && rl_playtime.getVisibility() == VISIBLE) {
                rl_playtime.setVisibility(INVISIBLE);
                iconPause.setVisibility(GONE);
            }
        }

    }

    private boolean isNeedFullScreenVideo() {
        if (data != null) {
            float videoRate = data.getResolutionHeight() / (data.getResolutionWidth() * 1.0f);
            float rate = 9 / (16 * 1.0f);
            return videoRate == rate;
        } else {
            return false;
        }
    }

    private void notifyLongClick(boolean isRight) {
        setCurSpeed(2.0f);
        if (portraitCenterControlView != null) {
            portraitCenterControlView.notifyLongClick();
        }
    }

    public float getCurSpeed() {
        return curSpeed;
    }

    public void setCurSpeed(float curSpeed) {
        this.curSpeed = curSpeed;
        if (videoView != null) {
            videoView.setSpeed(curSpeed);
        }
        if (fullScreenBottomView != null) {
            if (speedDatas.size() == 0) {
                speedDatas = getSpeedData();
            }
            if (curSpeed == 1.0f) {
                fullScreenBottomView.updateSpeedValue("倍速");
            } else {
                for (FunctionSpeedModel functionSpeedModel : speedDatas) {
                    if (functionSpeedModel.getValue() == curSpeed) {
                        fullScreenBottomView.updateSpeedValue(functionSpeedModel.getName() + "");
                        break;
                    }
                }

            }
        }

    }

    private List<FunctionSpeedModel> getSpeedData() {
        List<FunctionSpeedModel> speedDatas = new ArrayList<>();
        speedDatas.add(new FunctionSpeedModel("0.5X", 0.5f, false));
        speedDatas.add(new FunctionSpeedModel("0.75X", 0.75f, false));
        speedDatas.add(new FunctionSpeedModel("1.0X", 1f, true));
        speedDatas.add(new FunctionSpeedModel("1.25X", 1.25f, false));
        speedDatas.add(new FunctionSpeedModel("1.5X", 1.5f, false));
        speedDatas.add(new FunctionSpeedModel("1.75X", 1.75f, false));
        speedDatas.add(new FunctionSpeedModel("2.0X", 2f, false));
        return speedDatas;
    }

    private boolean isInChangeImageZone(View view, int x, int y) {
        try {
            if (null == mChangeImageBackgroundRect) {
                mChangeImageBackgroundRect = new Rect();
            }
            view.getDrawingRect(mChangeImageBackgroundRect);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            mChangeImageBackgroundRect.left = location[0];
            mChangeImageBackgroundRect.top = location[1];
            mChangeImageBackgroundRect.right = mChangeImageBackgroundRect.right + location[0];
            mChangeImageBackgroundRect.bottom = mChangeImageBackgroundRect.bottom + location[1];
            return mChangeImageBackgroundRect.contains(x, y);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return false;
    }

    //是否显示全屏
    private void showOrHideFullScreen(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            NiceUtil.hideActionBar(context);
            NiceUtil.scanForActivity(context)
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            fullScreenTopView.showControlView();
            fullScreenBottomView.showControlView();
//            iv_add.setVisibility(data.isFocus() ? GONE : VISIBLE);
        } else {
            NiceUtil.showActionBar(context);
            NiceUtil.scanForActivity(context)
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (!isPlaying) {
                notifyClick();
            }
        }
        setupVideoHeight();
        ll_praise.setVisibility(isFullscreen ? GONE : (isPlaying ? VISIBLE : GONE));
        portraitCenterControlView.setupView(isFullscreen);
        rl_portrait_play.setVisibility(isFullscreen ? GONE : VISIBLE);
        fullScreenTopView.setVisibility(isFullscreen ? VISIBLE : GONE);
        fullScreenBottomView.setVisibility(isFullscreen ? VISIBLE : GONE);
        if (onFinishListening != null) {
            onFinishListening.hideOrShowTopBottomView(isFullscreen, this, data);
        }
    }

    private void setupVideoHeight() {
        if (data.getResolutionHeight() < data.getResolutionWidth() && !isFullscreen) {
            setUpViewMargin(videoView, getResources().getDimensionPixelOffset(R.dimen.dp_100));
            setUpViewMargin(iv_bg, getResources().getDimensionPixelOffset(R.dimen.dp_100));
            int height = (int) (ScreenUtils.getScreenHeight() / ScreenUtils.getScreenWidth() * getResources().getDimensionPixelOffset(R.dimen.dp_200) / 16.0f * 9);
            if (BarUtils.isNavBarVisible((Activity) getContext())) {
                height = (int) ((ScreenUtils.getScreenHeight() - BarUtils.getNavBarHeight()) / ScreenUtils.getAppScreenWidth() * getResources().getDimensionPixelOffset(R.dimen.dp_200) / 16.0f * 9) - getResources().getDimensionPixelOffset(R.dimen.dp_150) - BarUtils.getNavBarHeight();
            } else {
                height = height - getResources().getDimensionPixelOffset(R.dimen.dp_150);
            }
            setUpViewHeight(iv_full, height);
        } else {
            setUpViewMargin(videoView, getResources().getDimensionPixelOffset(R.dimen.dp_0));
            setUpViewMargin(iv_bg, getResources().getDimensionPixelOffset(R.dimen.dp_0));
            iv_full.setVisibility(GONE);
        }
    }

    private void setUpViewHeight(View view, int height) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        lp.height = height;
        view.setLayoutParams(lp);
    }

    private void setUpViewMargin(View view, int height) {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        lp.bottomMargin = height;
        view.setLayoutParams(lp);
    }


    private void notifyOnMPScrollEnd() {
        if (portraitCenterControlView.isShowSeek()) {
            portraitCenterControlView.hideTitktokSeek();
            videoView.seekTo((int) (mPositionData));
            mPositionData = -1;
            fullScreenBottomView.notifyOnMPScrollEnd();
            fullScreenTopView.notifyOnMPScrollEnd();
        }
    }

    private void notifyOnMPLongCancel() {
        setCurSpeed(1.0f);
        if (portraitCenterControlView != null) {
            portraitCenterControlView.notifyLongClickCancel();
        }
    }

    //显示倍速
    private void showSelectSpeed() {
        if (isShowSpeed) {
            rl_speed_select.setVisibility(GONE);
            isShowSpeed = false;
            return;
        }
        rl_speed_select.setVisibility(VISIBLE);
        fullScreenTopView.hideControlView();
        fullScreenBottomView.hideControlView();
        portraitCenterControlView.showOrHideLockView(false);
        List<FunctionSpeedModel> newSpeedDatas = new ArrayList<>();
        newSpeedDatas.clear();
        isShowSpeed = true;
        if (speedDatas.size() == 0) {
            speedDatas = getSpeedData();
        }
        for (int i = 0; i < speedDatas.size(); i++) {
            if (getCurSpeed() == speedDatas.get(i).getValue()) {
                speedDatas.get(i).setSelect(true);
            } else {
                speedDatas.get(i).setSelect(false);
            }
        }
        newSpeedDatas.addAll(speedDatas);
        speedRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        speedAdapter = new SpeedAdapter(context, newSpeedDatas, new SpeedAdapter.SpeedClickListenting() {
            @Override
            public void speedSelect(int position) {
                isShowSpeed = false;
                rl_speed_select.setVisibility(GONE);
                if (newSpeedDatas.get(position).isSelect()) {
                    return;
                }
                for (FunctionSpeedModel speedData : newSpeedDatas) {
                    speedData.setSelect(false);
                }
                FunctionSpeedModel speedModel = newSpeedDatas.get(position);
                if ("1.0X".equalsIgnoreCase(speedModel.getName())) {
                    fullScreenBottomView.updateSpeedValue("倍速");
                } else {
                    fullScreenBottomView.updateSpeedValue(speedModel.getName());
                }
                if (videoView != null) {
                    videoView.setSpeed(speedModel.getValue());
                    curSpeed = speedModel.getValue();
                    ToastUtils.showShort("已为您切换为" + speedModel.getValue() + "X倍速度播放");
                }
                speedModel.setSelect(true);
                speedAdapter.notifyDataSetChanged();
            }
        });
        speedRecycleView.setAdapter(speedAdapter);
    }

    private void notifyScrollCancel() {
        portraitCenterControlView.hideSeekView();
    }

    private void notifyHorizontalScrollCancel() {

    }

    public interface OnFinishListening {
        public void finishSteepPage();
        public void hideOrShowTopBottomView(boolean isHide, PlayViewControl videoView, SpotBean data);
    }

    public interface InterceptTouchListening {
        public void interceptTouch(boolean isIntercept);
    }

    public interface OnPlayerStateCallBack {
        public void playerPaused(boolean isPaused);

        public void playerStateListening();

        public void playComplete();


        public void userPraise(boolean isPraise, String titleId);


    }

    //埋点-用户点赞
    private void userLike(boolean isParise){
        Map<String, Object> map = new HashMap<>();
        map.put("$url", PlaylistActivity.class.getName());
        map.put("$url_domain",  PlaylistActivity.class.getName());
        map.put("$title",  "Metro大都会播放页");
        map.put("tab",  "0");
        map.put("gerneral_type",  "单片视频");
        map.put("gerneral_id",  TextUtils.isEmpty(data.getTitleId())?"0":data.getTitleId());
        map.put("gerneral_name",  TextUtils.isEmpty(data.getTitle())?"0":data.getTitle());
        map.put("action", isParise? "点赞":"取消点赞");
        Log.e("isParise_video", "isParise_video" + new Gson().toJson(map));
        AnalysysAgent.track(context, "user_like", map);
    }

    public void startPlayTimer() {
        if (countTimer != null) {
            countTimer.start();
        }
    }

    public void pausePlayTimer() {
        if (countTimer != null) {
            countTimer.pause();
        }
    }

    public void resumePlayTimer() {
        if (countTimer != null) {
            countTimer.resume();
        }
    }

    public void stopPlayTimer() {
        if (countTimer != null) {
            countTimer.cancel();
        }
    }

    //启动定时器
    public void initPlayTimer() {
        if (countTimer != null) {
            countTimer.cancel();
        }
        countTimer = new PlayerCountTimer(1000) {
            @Override
            protected void onCancel(long millisFly) {
                super.onCancel(millisFly);
                play_length = millisFly;
            }

            @Override
            protected void onTick(long millisFly) {
                super.onTick(millisFly);
                play_length = millisFly;
                Log.e("playtime", "playtime=" + millisFly);
            }
        };
    }


}
