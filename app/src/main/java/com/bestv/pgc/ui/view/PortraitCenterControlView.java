package com.bestv.pgc.ui.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bestv.pgc.R;
import com.bestv.pgc.util.ImageAnimation;
import com.bestv.pgc.util.TimeSwitchUtils;
import com.blankj.utilcode.util.SizeUtils;


public class PortraitCenterControlView extends LinearLayout implements View.OnClickListener {
    private Context mContext;
    private ImageView iv_progress_light_1;
    private ImageView iv_progress_voice_1;
    private TextView tv_progress;
    private TextView tv_duration;
    private ImageView iv_seek;
    private boolean isShowAnimation = false;
    private LinearLayout ll_seek;
    private TextView tv_dev;
    private RelativeLayout portrait_rl_light;
    private LinearLayout portrait_ll_seek;
    private RelativeLayout portrait_rl_voice;
    private ImageView iv_voice;
    private ImageView iv_light;
    private ImageView iv_lock;
    private SpotCenterViewListening lockListening;
    private ImageView ivAnimRight;
    private TextView tvAnimText;
    private LinearLayout ll_speed_rate;
    private boolean isDlna;

    public boolean isDlna() {
        return isDlna;
    }

    public void setDlna(boolean isDlna) {
        this.isDlna = isDlna;
        iv_lock.setVisibility(isDlna ? GONE : VISIBLE);
    }

    public SpotCenterViewListening getLockListening() {
        return lockListening;
    }

    public void setLockListening(SpotCenterViewListening lockListening) {
        this.lockListening = lockListening;
    }

    private Handler hideHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            iv_lock.setVisibility(GONE);
        }
    };

    public boolean isShowSeek() {
        return isShowSeek;
    }

    public void setShowSeek(boolean showSeek) {
        isShowSeek = showSeek;
    }

    private boolean isShowSeek = false;

    public PortraitCenterControlView(Context context) {
        super(context);
        init();
    }

    public PortraitCenterControlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PortraitCenterControlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PortraitCenterControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.portrait_center_control_view, this);
        iv_progress_light_1 = contentView.findViewById(R.id.portrait_iv_progress_light_1);
        iv_progress_voice_1 = contentView.findViewById(R.id.portrait_iv_progress_voice_1);
        tv_progress = contentView.findViewById(R.id.portrait_tv_progress);
        tv_duration = contentView.findViewById(R.id.portrait_tv_duration);
        iv_seek = contentView.findViewById(R.id.portrait_iv_seek);
        ll_seek = contentView.findViewById(R.id.portrait_ll_seek);
        tv_dev = contentView.findViewById(R.id.portrait_tv_dev);
        portrait_rl_light = contentView.findViewById(R.id.portrait_rl_light);
        portrait_ll_seek = contentView.findViewById(R.id.portrait_ll_seek);
        portrait_rl_voice = contentView.findViewById(R.id.portrait_rl_voice);
        iv_voice = contentView.findViewById(R.id.iv_voice);
        iv_light = contentView.findViewById(R.id.iv_light);
        iv_lock = contentView.findViewById(R.id.iv_lock);
        iv_lock.setOnClickListener(this);
        ivAnimRight = contentView.findViewById(R.id.iv_anim_right);
        tvAnimText = contentView.findViewById(R.id.tv_anim_text);
        ll_speed_rate = contentView.findViewById(R.id.ll_speed_rate);
    }

    //brightness
    public void notifyChangeBrightness(float realProgress) {
        portrait_rl_light.setVisibility(VISIBLE);
        portrait_ll_seek.setVisibility(GONE);
        portrait_rl_voice.setVisibility(GONE);
        iv_progress_light_1.getDrawable().setLevel((int) (realProgress * 10000));
    }

    public void setupSpotProgressOrVolume() {
        setUpViewSize(iv_progress_light_1, 3, 150);
        setUpViewSize(iv_progress_voice_1, 3, 150);
        setUpViewSize(iv_light, 30, 30);
        setUpViewSize(iv_voice, 30, 30);

    }

    //volume
    public void notifyChangeVolume(float realProgress) {
        portrait_rl_light.setVisibility(GONE);
        portrait_ll_seek.setVisibility(GONE);
        portrait_rl_voice.setVisibility(VISIBLE);
        iv_progress_voice_1.getDrawable().setLevel((int) (realProgress * 10000));
    }

    //seekview
    public void showFullSeek() {
        portrait_rl_light.setVisibility(GONE);
        portrait_ll_seek.setVisibility(VISIBLE);
        portrait_rl_voice.setVisibility(GONE);
        setUpViewSize(ll_seek, 190, 150);
        setUpViewSize(iv_seek, 70, 30);
        tv_progress.setTextSize(18);
        tv_duration.setTextSize(18);
        tv_dev.setTextSize(12);
        ImageAnimation.giveStart(iv_seek);
        isShowSeek = true;
    }

    private void setUpViewSize(View view, float width, float height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = SizeUtils.dp2px(height);
        params.width = SizeUtils.dp2px(width);
        view.setLayoutParams(params);
    }


    public void showPortraitSeek() {
        portrait_rl_light.setVisibility(GONE);
        portrait_ll_seek.setVisibility(VISIBLE);
        portrait_rl_voice.setVisibility(GONE);
        setUpViewSize(ll_seek, 120, 80);
        setUpViewSize(iv_seek, 35, 15);
        tv_progress.setTextSize(10);
        tv_duration.setTextSize(10);
        tv_dev.setTextSize(7);
        ImageAnimation.giveStart(iv_seek);
        isShowSeek = true;
    }

    public void showTitktokSeek() {
        portrait_rl_light.setVisibility(GONE);
        portrait_rl_voice.setVisibility(GONE);
        isShowSeek = true;
    }



    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            isShowAnimation = false;
            isShowSeek = false;
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_lock) {
            if (lockListening != null) {
                if (iv_lock.getTag().equals("unlock")) {
                    iv_lock.setTag("lock");
                    lockListening.lockClick(true);
                    iv_lock.setImageResource(R.mipmap.ic_video_lock);
                } else {
                    iv_lock.setTag("unlock");
                    lockListening.lockClick(false);
                    iv_lock.setImageResource(R.mipmap.ic_video_unlock);
                }
            }
        }
    }

    public void notifyLongClick() {
        ll_speed_rate.setVisibility(VISIBLE);
        ivAnimRight.setImageResource(R.drawable.speed_red);
        ImageAnimation.giveStart(ivAnimRight);
    }

    public void notifyLongClickCancel() {
        ll_speed_rate.setVisibility(GONE);
    }

    //设置倍速view与锁屏
    public void setupView(boolean isFullscreen) {
        iv_lock.setVisibility(isFullscreen ? VISIBLE : GONE);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) ll_speed_rate.getLayoutParams();
        lp.topMargin = isFullscreen ? getResources().getDimensionPixelOffset(R.dimen.dp_80) : getResources().getDimensionPixelOffset(R.dimen.dp_150);
        ll_speed_rate.setLayoutParams(lp);
    }

    public void showOrHideLockView(boolean isShow) {
        if (iv_lock.getTag().equals("unlock")) {
            iv_lock.setVisibility(isShow ? (isDlna ? GONE : VISIBLE) : GONE);
        }
    }

    public void hideSeekView() {
        portrait_ll_seek.setVisibility(GONE);
        portrait_rl_light.setVisibility(GONE);
        portrait_rl_voice.setVisibility(GONE);
        isShowAnimation = false;
        isShowSeek = false;
    }

    public void hideTitktokSeek() {
        portrait_rl_light.setVisibility(GONE);
        portrait_rl_voice.setVisibility(GONE);
        isShowSeek = false;
    }


    public interface SpotCenterViewListening {
        public void lockClick(boolean isLock);

        public void longClick();
    }
}


