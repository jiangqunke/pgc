package com.bestv.pgc.ui.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bestv.pgc.R;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.util.ImageAnimation;


public class SpotFullScreenTopView extends LinearLayout implements View.OnClickListener {
    private ObjectAnimator objectAnimator;
    private LinearLayout ll_top;
    private ImageView iv_back;
    private TextView tv_title;
    private ImageView iv_video_zan;
    private LinearLayout ll_spot_function;

    private SpotTopViewClickCallBack spotTopViewListening;

    public SpotTopViewClickCallBack getSpotTopViewListening() {
        return spotTopViewListening;
    }

    public void setSpotTopViewListening(SpotTopViewClickCallBack spotTopViewListening) {
        this.spotTopViewListening = spotTopViewListening;
    }

    private Handler hideHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            showOrHideViewAnimator(false);
        }
    };

    public SpotFullScreenTopView(Context context) {
        super(context);
        init();
    }

    public SpotFullScreenTopView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpotFullScreenTopView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SpotFullScreenTopView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.spot_full_top_view, this);
        iv_back = view.findViewById(R.id.iv_back);
        tv_title = view.findViewById(R.id.tv_title);
        iv_video_zan = view.findViewById(R.id.iv_video_zan);
        ll_top = view.findViewById(R.id.ll_top);
        iv_back.setOnClickListener(this);
        iv_video_zan.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            if (spotTopViewListening != null) {
                spotTopViewListening.backClick();
            }
        } else if (id == R.id.iv_video_zan) {
            if (spotTopViewListening != null) {
                spotTopViewListening.pariseClick();
            }
        }
    }
    public void showOrHideTopView(boolean isShow) {
        if (isShow){
            ll_top.setVisibility(View.VISIBLE);
            showOrHideViewAnimator(true);
            hideHandler.postDelayed(runnable, 3000);
        }else {
            showOrHideViewAnimator(false);
            hideHandler.removeCallbacks(runnable);
        }
    }
    public void notifyScrollStart() {
        if (ll_top.getVisibility() == View.GONE) {
            ll_top.setVisibility(View.VISIBLE);
        }
        hideHandler.removeCallbacks(runnable);
    }

    public void notifyOnMPScrollEnd() {
        hideHandler.postDelayed(runnable, 3000);
    }


    public void updateSeekBarProgress() {
        if (ll_top.getVisibility() == View.GONE) {
            ll_top.setVisibility(View.VISIBLE);
            hideHandler.removeCallbacks(runnable);
        }
    }

    public void hideOrShowControlView() {
        if (ll_top.getVisibility() == View.VISIBLE) {
            showOrHideViewAnimator(false);
            hideHandler.removeCallbacks(runnable);
        } else {
            ll_top.setVisibility(View.VISIBLE);
            showOrHideViewAnimator(true);
            hideHandler.postDelayed(runnable, 3000);
        }

    }

    private void showOrHideViewAnimator(boolean isShow) {
        hideHandler.removeCallbacks(runnable);
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        if (isShow) {
            objectAnimator = ObjectAnimator.ofFloat(ll_top, "translationY", -ll_top.getHeight(), 0);
        } else {
            objectAnimator = ObjectAnimator.ofFloat(ll_top, "translationY", 0, -ll_top.getHeight());
        }
        objectAnimator.setDuration(250);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    ll_top.setVisibility(GONE);
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
        if (ll_top.getVisibility() == View.VISIBLE) {
            ll_top.setVisibility(View.GONE);
            setBackground(null);
        }
        hideHandler.removeCallbacks(runnable);
    }

    public void showControlView() {
        if (ll_top.getVisibility() == View.GONE) {
            ll_top.setVisibility(View.VISIBLE);
        }
        hideHandler.removeCallbacks(runnable);
        hideHandler.postDelayed(runnable,3000);
    }

    public void dismissPortraitTopView() {
        hideHandler.removeCallbacksAndMessages(null);
    }

    public void notifyDoubleClick(boolean isPlaying) {
        if (!isPlaying) {
            if (ll_top.getVisibility() == View.VISIBLE) {
                hideHandler.postDelayed(runnable, 3000);
            }
        } else {
            hideHandler.removeCallbacks(runnable);
        }
    }

    public void setVideoData(SpotBean spotBean) {
        tv_title.setText(spotBean.getTitle());
        if (spotBean.isPraise()) {
            iv_video_zan.setImageResource(R.mipmap.icon_full_praise);
        } else {
            iv_video_zan.setImageResource(R.mipmap.icon_full_unpraise);
        }
    }

    public void updatePariseState(boolean isPraise) {
//        iv_video_zan.setImageResource(R.drawable.give_show);
        if (isPraise) {
            iv_video_zan.setImageResource(R.mipmap.icon_full_praise);
        } else {
            iv_video_zan.setImageResource(R.mipmap.icon_full_unpraise);
        }
    }

    public interface SpotTopViewClickCallBack {
        void backClick();
        void pariseClick();
    }
}
