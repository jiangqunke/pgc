package com.bestv.pgc.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.OrientationHelper;

import com.bestv.pgc.R;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.databinding.ActivityPlayListBinding;
import com.bestv.pgc.player.ExoVideoView;
import com.bestv.pgc.preloader.ui.BestTVPreloadFuture;
import com.bestv.pgc.preloader.ui.BestTVPreloader;
import com.bestv.pgc.preloader.ui.BestTVPreloaderConfig;
import com.bestv.pgc.refreshview.XRefreshView;
import com.bestv.pgc.ui.view.PlayViewControl;
import com.bestv.pgc.ui.view.TiktokLoadingView;
import com.bestv.pgc.util.OnViewPagerListener;
import com.bestv.pgc.util.PagerLayoutManager;
import com.bestv.pgc.util.TimeUtils;
import com.bestv.pgc.util.UltimateBar;
import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends SlideBackActivity implements View.OnClickListener {
    ActivityPlayListBinding binding;
    private PlayViewModel viewModel;
    private VideoAdapter tiktokPageAdapter;
    private List<SpotBean> mDatas = new ArrayList<>();
    private PagerLayoutManager mLayoutManager;
    private PlayViewControl playViewControl;
    private boolean isVisible;
    private ExoVideoView mVideoView;
    private boolean isPlayerPaused = false;
    private int currentPosition = 0;
    private ImageView iconPause;
    private boolean isFirst4GTip = false;
    public BestTVPreloadFuture mFuture;
    private String titleId;
    public SpotBean spotBean;
    private TiktokLoadingView animatorIv;
    private int page = 0;
    private boolean isNoMore = false;
    private String openId;
    private String poi;
    private String scene;
    private String videoInfo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UltimateBar.Companion.with(this)
                .statusDark(false)                  // 状态栏灰色模式(Android 6.0+)，默认 flase
                .statusDrawable2(null)         // Android 6.0 以下状态栏灰色模式时状态栏颜色
                .applyNavigation(false)              // 应用到导航栏，默认 flase
                .navigationDark(false)              // 导航栏灰色模式(Android 8.0+)，默认 false
                .navigationDrawable2(null)     // Android 8.0 以下导航栏灰色模式时导航栏颜色
                .create()
                .immersionBar();
        binding = ActivityPlayListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(PlayViewModel.class);
        getLifecycle().addObserver(viewModel);
        mFuture = new BestTVPreloadFuture(this, this.getClass().getSimpleName());
        initView();
        setUpData();

    }


    private void initView() {
        openId = getIntent().getStringExtra("openId");
        poi = getIntent().getStringExtra("poi");
        scene = getIntent().getStringExtra("scene");
        videoInfo = getIntent().getStringExtra("videoInfo");
        viewModel.init(openId,poi,scene);
        if (!TextUtils.isEmpty(videoInfo)){
            Gson gson = new Gson();
            SpotBean bean = gson.fromJson(videoInfo, SpotBean.class);
            String qualityUrl = bean.getQualityUrl();
            if (!TextUtils.isEmpty(qualityUrl) && qualityUrl.toLowerCase().contains(".mp4")) {
                mFuture.addUrl(qualityUrl);
            }
            mDatas.add(bean);
        }
        tiktokPageAdapter = new VideoAdapter(this, mDatas);
        mLayoutManager = new PagerLayoutManager(this, OrientationHelper.VERTICAL);
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
            @Override
            public void onInitComplete(View view) {
                if (currentPosition == 0) {
                    playVideo(0, view);
                } else {
                    playVideo(currentPosition, view);
                }
            }

            @Override
            public void onPageSelected(int position, boolean isBottom, View view) {
                if (currentPosition == position) return;
                if ((position > currentPosition && position + 2 == mDatas.size())) {
                    page++;
                    viewModel.loadSpotDatas();
                }
                playVideo(position, view);
            }

            @Override
            public void onPageScroll() {

            }

            @Override
            public void onPageRelease(boolean isNext, int position, View view) {
                if (currentPosition == position) {
                    releaseVideo(view);
                }
            }
        });
        binding.llBack.setOnClickListener(this);
        binding.recyclerView.setLayoutManager(mLayoutManager);
        binding.recyclerView.setAdapter(tiktokPageAdapter);
        playViewControl = new PlayViewControl(this);
        playViewControl.setOnFinishListening(new PlayViewControl.OnFinishListening() {
            @Override
            public void finishSteepPage() {
                onBackPressed();
            }

            @Override
            public void hideOrShowTopBottomView(boolean isHide, PlayViewControl videoView, SpotBean data) {
                binding.rlTiktokTop.setVisibility(isHide ? View.GONE : View.VISIBLE);
                if (isHide) {
                    ((ViewGroup) videoView.getParent()).removeAllViews();
                    binding.rlTiktokContent.addView(videoView);
                    binding.recyclerView.setVisibility(View.GONE);
                } else {
                    int index = -1;
                    for (int i = 0; i < binding.rlTiktokContent.getChildCount(); i++) {
                        if (binding.rlTiktokContent.getChildAt(i) instanceof PlayViewControl) {
                            index = i;
                        }
                    }
                    if (index > -1) {
                        binding.rlTiktokContent.removeViewAt(index);
                    }
                    hideFullVideoView(data);
                }
            }
        });
        playViewControl.setInterceptTouchListening(new PlayViewControl.InterceptTouchListening() {
            @Override
            public void interceptTouch(boolean isIntercept) {
                if (isIntercept) {
                    setVelocity(false);
                } else {
                    setVelocity(true);
                }
            }
        });
        playViewControl.setPausedCallBack(new PlayViewControl.OnPlayerStateCallBack() {
            @Override
            public void playerPaused(boolean isPaused) {
                isPlayerPaused = isPaused;
            }

            @Override
            public void playerStateListening() {
                if (!isVisible && mVideoView != null && playViewControl != null) {
                    mVideoView.pause();
                }
            }

            @Override
            public void playComplete() {
                if (!NetworkUtils.isConnected()) {
                    ToastUtils.showShort("无法连接到网络");
                    return;
                }
                int position = currentPosition + 1;
                if (position < mDatas.size()) {
                    mLayoutManager.scrollToNextVideo();
                    binding.recyclerView.smoothScrollToPosition(position);
                }
            }

            @Override
            public void userPraise(boolean isPraise, String titleId) {
                viewModel.praise(isPraise,titleId);
            }

        });
    }

    private void setUpData() {
        binding.xrefreshview.setPinnedTime(1000);
        binding.xrefreshview.setMoveForHorizontal(true);
        binding.xrefreshview.setPullRefreshEnable(false);
        binding.xrefreshview.setPullLoadEnable(true);
        binding.xrefreshview.setAutoLoadMore(true);
        binding.xrefreshview.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {
            @Override
            public void onRefresh(boolean isPullDown) {
                if (binding.xrefreshview == null || !isVisible) {
                    return;
                }

                if (!NetworkUtils.isConnected()) {
                    binding.xrefreshview.stopRefresh();
                    ToastUtils.showShort("无法连接到网络");
                    return;
                }
                isNoMore = false;
                binding.xrefreshview.stopRefresh();
                ToastUtils.showShort("当前已经是第一条视频");
            }

            @Override
            public void onLoadMore(boolean isSilence) {
                if (!isVisible) return;
                if (!NetworkUtils.isConnected()) {
                    binding.xrefreshview.stopLoadMore();
                    ToastUtils.showShort("无法连接到网络");
                    return;
                }

//                if (AppVarManager.getInstance().getSpotPosition() == mDatas.size() - 1) {
                if (isNoMore) {
                    binding.xrefreshview.stopLoadMore();
                    ToastUtils.showShort("已经是最后一条视频了");
                } else {
                    page++;
                    viewModel.loadSpotDatas();
                }
            }
        });
        viewModel.loadSpotDatas();
        viewModel.pgcData.observe(this, new Observer<SpotBean>() {
            @Override
            public void onChanged(SpotBean s) {
                dealWithData(s);
            }
        });
        viewModel.praiseData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isPraise) {
                if (playViewControl != null){
                    playViewControl.updatePraiseState(isPraise);
                }
            }
        });
        viewModel.failData.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                try {
                    if ( binding.xrefreshview != null){
                        binding.xrefreshview.stopRefresh();
                        binding.xrefreshview.stopLoadMore();
                    }
                    if (binding.llNo != null && CollectionUtils.isEmpty(mDatas)) {
                        binding.tvNo.setTextColor(Color.parseColor("#FFFFFF"));
                        binding.ivNo.setImageResource(R.mipmap.bczy);
                        binding.tvNo.setText("这里空空如也，去其他地方转转吧～");
                        binding.llNo.setVisibility(View.VISIBLE);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    private void dealWithData(SpotBean bean) {
        if ( binding.xrefreshview != null){
            binding.xrefreshview.stopRefresh();
            binding.xrefreshview.stopLoadMore();
        }
        if (CollectionUtils.isEmpty(bean.dt)) {
            if (page == 0) {
                if (binding.llNo != null) {
                    binding.tvNo.setTextColor(Color.parseColor("#FFFFFF"));
                    binding.ivNo.setImageResource(R.mipmap.bczy);
                    binding.tvNo.setText("这里空空如也，去其他地方转转吧～");
                    binding.llNo.setVisibility(View.VISIBLE);
                }
            } else {
                isNoMore = true;
            }
        } else {
            mDatas.addAll(bean.dt);
            tiktokPageAdapter.notifyDataSetChanged();
            for (SpotBean spotBean : mDatas) {
                String qualityUrl = spotBean.getQualityUrl();
                if (!TextUtils.isEmpty(qualityUrl) && qualityUrl.toLowerCase().contains(".mp4")) {
                    mFuture.addUrl(qualityUrl);
                }
            }
        }


    }

    /**
     * 播放视频
     */
    private void playVideo(int position, View view) {
        try {
            if (view != null) {
                stopPlay();
                if (NetworkUtils.isMobileData()) {
                    if (!isFirst4GTip) {
                        ToastUtils.showShort("当前为非Wi-Fi环境，请注意流量消耗");
                        isFirst4GTip = true;
                    }
                }
                View itemView = binding.recyclerView.getChildAt(0);
                FrameLayout frameLayout = itemView.findViewById(R.id.container);
                ViewParent parent = playViewControl.getParent();
                if (parent != null && parent instanceof FrameLayout) {
                    ((FrameLayout) parent).removeView(playViewControl);
                }
                frameLayout.addView(playViewControl);
                String qualityUrl = mDatas.get(position).getQualityUrl();
                if (!TextUtils.isEmpty(qualityUrl) && qualityUrl.toLowerCase().contains(".mp4")) {
                    mFuture.preloadForwardUrl(qualityUrl);
                    mDatas.get(position).setDownloadQualityUrl(mFuture.getCurrentProxyUrl(qualityUrl));
                }
                titleId = mDatas.get(position).getTitleId();
                spotBean = mDatas.get(position);
                playViewControl.setCurSpeed(1.0f);
                playViewControl.setVideoData(mDatas.get(position));
                currentPosition = position;
                SpotBean bean = mDatas.get(position);
                bean.setPlayFinish(false);
                bean.setPlayDuration(0);
                bean.setTime(TimeUtils.getCurTime());
                mVideoView = view.findViewById(R.id.video_view);
                iconPause = view.findViewById(R.id.icon_pause);
                iconPause.setVisibility(View.GONE);
                animatorIv = view.findViewById(R.id.animator_iv);
                mVideoView.start();
                if (playViewControl != null) {
                    playViewControl.setFragmentVisible(isVisible);
                }
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    public void stopPlay() {
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView.release();
        }
    }

    /**
     * 停止播放
     */
    private void releaseVideo(View view) {
        if (view != null && isVisible) {
            if (mVideoView != null) {
                mVideoView.stopPlayback();
            }
            if (playViewControl != null) {
                playViewControl.resetSeekProgress();
            }
            if (iconPause != null) {
                iconPause.setVisibility(View.GONE);
            }
        }
    }

    public void hideFullVideoView(SpotBean data) {
        View itemView = binding.recyclerView.getChildAt(0);
        if (itemView == null) return;
        FrameLayout frameLayout = itemView.findViewById(R.id.container);
        ViewParent parent = playViewControl.getParent();
        if (parent != null && parent instanceof FrameLayout) {
            ((FrameLayout) parent).removeView(playViewControl);
        }
        binding.recyclerView.setVisibility(View.VISIBLE);
        frameLayout.addView(playViewControl);
        playViewControl.setVideoData(data);
        playViewControl.hideBgImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isVisible = true;
        if (mFuture != null) {
            mFuture.onResume();
        }
        try {
            if (mVideoView != null && playViewControl != null) {
                playViewControl.setFragmentVisible(true);
                if (mDatas.size() > 0) {
                    if (!mDatas.get(currentPosition).isPause()) {
                        playViewControl.resumeVideo();
//                        playViewControl.resumePlayTimer();
                    }
                }
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFuture != null) {
            mFuture.onDestroy();
        }
//        if (mVideoView != null){
//            mVideoView.stopPlayback();
//            mVideoView.release();
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isVisible = false;
        if (playViewControl != null) {
            if (mVideoView != null){
                mVideoView.pause();
            }
//            playViewControl.pausePlayTimer();
            playViewControl.setFragmentVisible(false);
        }
        if (mFuture != null) {
            mFuture.onPause();
        }
        binding.xrefreshview.stopRefresh();
        if (animatorIv != null) {
            animatorIv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ll_back) {
            onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
