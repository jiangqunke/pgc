package com.bestv.pgc.ui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bestv.pgc.R;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.util.TimeSwitchUtils;

public class SpotFullScreenBottomView extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private ObjectAnimator objectAnimator;
    private RelativeLayout ll_bottom;
    private LinearLayout ll_progress;
    private TextView tv_progress_time;
    private TextView tv_total_time;
    private ImageView iv_play;
    private TextView tv_speed;
    private CustomSeekBar seekbar;
    private RelativeLayout rl_seekbar;
    private SpotBottomViewClickCallBack spotBottomViewListening;
    private int duationTime;
    private LinearLayout ll_progress_flag;
    private TextView flag_progress, flag_duration;
    private boolean isDlna;

    public boolean isDlna() {
        return isDlna;
    }

    public void setDlna(boolean isDlna) {
        this.isDlna = isDlna;
        tv_speed.setVisibility(isDlna?GONE:VISIBLE);
    }

    public SpotBottomViewClickCallBack getSpotBottomViewListening() {
        return spotBottomViewListening;
    }

    public void setSpotBottomViewListening(SpotBottomViewClickCallBack spotBottomViewListening) {
        this.spotBottomViewListening = spotBottomViewListening;
    }

    private Handler hideHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showOrHideViewAnimator(false);
            if (spotBottomViewListening != null){
                spotBottomViewListening.bottomViewShowOrHided(false);
            }
        }
    };

    public SpotFullScreenBottomView(Context context) {
        super(context);
        init();
    }

    public SpotFullScreenBottomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpotFullScreenBottomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SpotFullScreenBottomView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.spot_full_bottom_view, this);
        ll_progress = view.findViewById(R.id.ll_progress);
        tv_progress_time = view.findViewById(R.id.tv_progress_time);
        tv_total_time = view.findViewById(R.id.tv_total_time);
        iv_play = view.findViewById(R.id.iv_play);
        tv_speed = view.findViewById(R.id.tv_speed);
        seekbar = view.findViewById(R.id.seekbar);
        ll_bottom = view.findViewById(R.id.rl_bottom);
        iv_play.setOnClickListener(this);
        tv_speed.setOnClickListener(this);
        seekbar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.shape_seekbar_btn));
        seekbar.setProgressDrawable(ContextCompat.getDrawable(getContext(), R.drawable.seekbar_shape));
        seekbar.setOnSeekBarChangeListener(this);
        rl_seekbar = view.findViewById(R.id.rl_seekbar);
        ll_progress_flag = view.findViewById(R.id.ll_progress_flag);
        flag_progress = view.findViewById(R.id.flag_progress);
        flag_duration = view.findViewById(R.id.flag_duration);
        rl_seekbar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Rect seekRect = new Rect();
                seekbar.getHitRect(seekRect);
                if ((event.getY() >= (seekRect.top - 500)) && (event.getY() <= (seekRect.bottom + 500))) {
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
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_play) {
            if (spotBottomViewListening != null) {
                spotBottomViewListening.playClick();
            }
        } else if (id == R.id.tv_speed) {
            if (spotBottomViewListening != null) {
                spotBottomViewListening.speedClick();
            }
        }
    }
    public void showOrHideTopView(boolean isShow) {
        showOrHideViewAnimator(isShow);
    }
    public void notifyScrollStart() {
        ll_progress_flag.setVisibility(VISIBLE);
        if (ll_bottom.getVisibility() == View.GONE) {
            ll_bottom.setVisibility(View.VISIBLE);
        }
        hideHandler.removeCallbacks(runnable);
        if (spotBottomViewListening != null){
            spotBottomViewListening.bottomViewShowOrHided(true);
        }
    }

    public void notifyOnMPScrollEnd() {
        ll_progress_flag.setVisibility(GONE);
        hideHandler.postDelayed(runnable, 3000);
    }


    public void updateSeekBarProgress() {
        if (ll_bottom.getVisibility() == View.GONE) {
            ll_bottom.setVisibility(View.VISIBLE);
            hideHandler.removeCallbacks(runnable);
        }
    }

    public void hideOrShowControlView() {
        if (ll_bottom.getVisibility() == View.VISIBLE) {
            showOrHideViewAnimator(false);
            hideHandler.removeCallbacks(runnable);
            if (spotBottomViewListening != null){
                spotBottomViewListening.bottomViewShowOrHided(false);
            }
        } else {
            ll_bottom.setVisibility(View.VISIBLE);
            showOrHideViewAnimator(true);
            if (spotBottomViewListening != null){
                spotBottomViewListening.bottomViewShowOrHided(true);
            }
            hideHandler.postDelayed(runnable, 3000);
        }

    }

    private void showOrHideViewAnimator(boolean isShow) {
        hideHandler.removeCallbacks(runnable);
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        if (isShow) {
            objectAnimator = ObjectAnimator.ofFloat(ll_bottom, "translationY", ll_bottom.getHeight(), 0);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(ll_bottom, "translationY", 0, ll_bottom.getHeight());
        }
        objectAnimator.setDuration(250);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    ll_bottom.setVisibility(GONE);
                    showOrHideViewAnimator(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.setInterpolator(new DecelerateInterpolator());
        objectAnimator.start();
    }

    public void delayHideControlView() {
        hideHandler.postDelayed(runnable, 3000);
    }

    public void hideControlView() {
        if (ll_bottom.getVisibility() == View.VISIBLE) {
            ll_bottom.setVisibility(View.GONE);
            setBackground(null);
        }
        hideHandler.removeCallbacks(runnable);
    }

    public void showControlView() {
        if (ll_bottom.getVisibility() == View.GONE) {
            ll_bottom.setVisibility(View.VISIBLE);
        }
        hideHandler.removeCallbacks(runnable);
        hideHandler.postDelayed(runnable, 3000);
    }

    public void dismissPortraitBottomView() {
        hideHandler.removeCallbacksAndMessages(null);
    }

    public void notifyDoubleClick(boolean isPlaying) {
        if (!isPlaying) {
            if (ll_bottom.getVisibility() == View.VISIBLE) {
                hideHandler.postDelayed(runnable, 3000);
            }
        } else {
            hideHandler.removeCallbacks(runnable);
        }
        iv_play.setImageDrawable(ContextCompat.getDrawable(getContext(), isPlaying ? R.mipmap.ic_video_portrait_pause : R.mipmap.ic_video_portrait_play));
    }
    public void setPlayState(boolean isPlaying){
        iv_play.setImageDrawable(ContextCompat.getDrawable(getContext(), isPlaying ? R.mipmap.ic_video_portrait_pause : R.mipmap.ic_video_portrait_play));
    }

    public void setVideoData(SpotBean spotBean) {
        this.duationTime = spotBean.getDuration();
        String text = TimeSwitchUtils.secToTime((int) (spotBean.getDuration()));
        tv_total_time.setText(text);
        flag_duration.setText(text);
    }

    public void updateProgress(int progress, int playTime) {
        seekbar.setProgress(progress);
        String text = TimeSwitchUtils.secToTime((int) playTime);
        if (tv_progress_time != null) {
            tv_progress_time.setText(text);
        }
        flag_progress.setText(text);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        String text = TimeSwitchUtils.secToTime((int) (progress / 100.0f * duationTime));
        if (tv_progress_time != null) {
            tv_progress_time.setText(text);
        }
        flag_progress.setText(text);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        ll_progress_flag.setVisibility(VISIBLE);
        hideHandler.removeCallbacks(runnable);
        if (spotBottomViewListening != null) {
            spotBottomViewListening.startTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ll_progress_flag.setVisibility(GONE);
        hideHandler.postDelayed(runnable, 3000);
        if (spotBottomViewListening != null) {
            spotBottomViewListening.stopTrackingTouch(seekBar);
        }
        iv_play.setImageDrawable(ContextCompat.getDrawable(getContext(),  R.mipmap.ic_video_portrait_pause ));
    }

    public void setupProgressPosition() {
        tv_progress_time.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                float playDuration = ijkVrVideoView.getCurrentPosition() / 1000.0f;
//                seekbar.startTrackVideoHeaderAndTrailer(headTime, playDuration, duation, new CustomSeekBar.PositionListening() {
//                    @Override
//                    public void getPosition(int position) {
//
//                    }
//                });
                seekbar.setSeekListening(new CustomSeekBar.SeekListening() {
                    @Override
                    public void getSeekPosition(int position) {
                        LayoutParams layoutParam = (LayoutParams) ll_progress_flag.getLayoutParams();
                        layoutParam.leftMargin = (int) (right + position);
                        ll_progress_flag.setLayoutParams(layoutParam);
                    }
                });

            }
        });


    }
    public void updateSpeedValue(String speed) {
        tv_speed.setText(speed);
    }

    public interface SpotBottomViewClickCallBack {

        void playClick();

        void speedClick();

        void stopTrackingTouch(SeekBar fullSeekBar);

        void startTrackingTouch(SeekBar fullSeekBar);

        void bottomViewShowOrHided(boolean isShow);
    }
}
