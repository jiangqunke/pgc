package com.bestv.pgc.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;

import com.analysys.AnalysysAgent;
import com.bestv.pgc.R;
import com.bestv.pgc.beans.AnalysysBean;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.databinding.ActivityPlayListBinding;
import com.bestv.pgc.player.ExoVideoView;
import com.bestv.pgc.preloader.ui.BestTVPreloadFuture;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayHotlistActivity extends SlideBackActivity implements View.OnClickListener {
    ActivityPlayListBinding binding;
    private PlayHotViewModel viewModel;
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
    private String analysysInfo;
    private AnalysysBean analysysBean;
    private String requestId;
    private long count;
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
        viewModel = new ViewModelProvider(this).get(PlayHotViewModel.class);
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
        analysysInfo = getIntent().getStringExtra("analysysInfo");
        viewModel.init(openId, poi, scene);
        Gson gson = new Gson();
        if (!TextUtils.isEmpty(videoInfo)) {
            Log.e("analysysBean", "videoInfo=" + videoInfo);
            SpotBean bean = gson.fromJson(videoInfo, SpotBean.class);
            String qualityUrl = bean.getQualityUrl();
            if (!TextUtils.isEmpty(qualityUrl) && qualityUrl.toLowerCase().contains(".mp4")) {
                mFuture.addUrl(qualityUrl);
            }
            mDatas.add(bean);
        }
        if (!TextUtils.isEmpty(analysysInfo)) {
            Log.e("analysysBean", "analysysInfo=" + analysysInfo);
            analysysBean = gson.fromJson(analysysInfo, AnalysysBean.class);
            requestId = analysysBean.getRequest_id();
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
//                    releaseVideo(view);
                    if (playViewControl != null) {
                        if (!CollectionUtils.isEmpty(mDatas) && mDatas.size() > currentPosition) {
                            try {
                                videoClose(mDatas.get(currentPosition));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        if (playViewControl != null) {
                            playViewControl.resetSeekProgress();
                        }
                    }
                    stopPlay();
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
                viewModel.praise(isPraise, titleId);
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
                if (isNoMore ) {
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
                if (playViewControl != null) {
                    playViewControl.updatePraiseState(isPraise);
                }
            }
        });
        viewModel.failData.observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                try {
                    if (binding.xrefreshview != null) {
                        binding.xrefreshview.stopRefresh();
                        binding.xrefreshview.stopLoadMore();
                    }
                    if (binding.llNo != null && CollectionUtils.isEmpty(mDatas)) {
                        binding.tvNo.setTextColor(Color.parseColor("#FFFFFF"));
                        binding.ivNo.setImageResource(R.mipmap.bczy);
                        binding.tvNo.setText("这里空空如也，去其他地方转转吧～");
                        binding.llNo.setVisibility(View.VISIBLE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void scrollToOffsetPostion(int position) {
        binding.recyclerView.scrollToPosition(position);
        LinearLayoutManager mLayoutManager = (LinearLayoutManager) binding.recyclerView.getLayoutManager();
        mLayoutManager.scrollToPositionWithOffset(position, 0);
    }

    private void dealWithData(SpotBean bean) {
        try {
            if (binding.xrefreshview != null) {
                binding.xrefreshview.stopRefresh();
                binding.xrefreshview.stopLoadMore();
            }
            if (CollectionUtils.isEmpty(bean.dt)) {
                if ( page == 0 &&CollectionUtils.isEmpty(mDatas)) {
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
                removeDuplicate(mDatas);
                tiktokPageAdapter.notifyDataSetChanged();
                for (SpotBean spotBean : mDatas) {
                    String qualityUrl = spotBean.getQualityUrl();
                    if (!TextUtils.isEmpty(qualityUrl) && qualityUrl.toLowerCase().contains(".mp4")) {
                        mFuture.addUrl(qualityUrl);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * 去重
     * @param list
     */
    public void removeDuplicate(List<SpotBean> list) {
        try {
            List<SpotBean> listTemp = new ArrayList();
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = list.size() - 1; j > i; j--) {
                    if (list.get(j).getTitleId().equals(list.get(i).getTitleId())) {
                        list.remove(j);
                    }
                }
            }
            listTemp.addAll(list);
            mDatas.clear();
            mDatas.addAll(listTemp);
        } catch (Exception e) {
            e.printStackTrace();
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
                switchVideo(mDatas, position, position > currentPosition);
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
                if (count>0||TextUtils.isEmpty(videoInfo)){
                    videoOpen(bean);
                }
                count++;
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
        if (playViewControl != null) {
            playViewControl.stopPlayTimer();
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
            if (mVideoView != null) {
                mVideoView.pause();
            }
            playViewControl.pausePlayTimer();
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
        if (playViewControl != null) {
            if (!CollectionUtils.isEmpty(mDatas) && mDatas.size() > currentPosition) {
                try {
                    videoClose(mDatas.get(currentPosition));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        stopPlay();
    }


    //埋点-开始播放
    private void videoOpen(SpotBean spotBean) {
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("$title", "Metro大都会播放页");
            if (analysysBean != null) {
                map.put("refer_tab", !TextUtils.isEmpty(analysysBean.getRefer_tab()) ? analysysBean.getRefer_tab() : "0");
                map.put("ex_id", !TextUtils.isEmpty(analysysBean.getEx_id()) ? analysysBean.getEx_id() : "0");
                if (TextUtils.isEmpty(requestId)) {
                    map.put("request_item_rank", currentPosition % 10);
                } else {
                    map.put("request_item_rank", analysysBean.getRequest_item_rank());
                }
            } else {
                map.put("refer_tab", "0");
                map.put("ex_id", "0");
                map.put("request_item_rank", (currentPosition + 1) % 10);
                map.put("request_id", "0");
            }
            map.put("request_id", TextUtils.isEmpty(requestId) ? viewModel.getRequestId() : requestId);
            if (!TextUtils.isEmpty(spotBean.getAlgoInfo())){
                map.put("algo_info",spotBean.getAlgoInfo());
            }
            map.put("item_type", "单片视频");
            map.put("item_id", TextUtils.isEmpty(spotBean.getTitleId()) ? "" : spotBean.getTitleId());
            map.put("item_name", TextUtils.isEmpty(spotBean.getTitle()) ? "" : spotBean.getTitle());
            map.put("video_id", TextUtils.isEmpty(spotBean.getTitleId()) ? "" : spotBean.getTitleId());
            map.put("video_name", TextUtils.isEmpty(spotBean.getTitle()) ? "" : spotBean.getTitle());
            map.put("video_length", spotBean.getDuration());
            map.put("play_module", "点播");
            map.put("start_video_length", 0);
            Log.e("video_open", "video_open" + new Gson().toJson(map));
            AnalysysAgent.track(this, "video_open", map);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PlayListActivity", "video_open" + e.getMessage());
        }
    }

    //埋点-结束播放
    private void videoClose(SpotBean spotBean) {
        try {
            long play_length = playViewControl.getPlay_length();
            if (play_length == 0) return;
            Map<String, Object> map = new HashMap<>();
            map.put("$title", "Metro大都会播放页");
            if (analysysBean != null) {
                map.put("refer_tab", !TextUtils.isEmpty(analysysBean.getRefer_tab()) ? analysysBean.getRefer_tab() : "0");
                map.put("ex_id", !TextUtils.isEmpty(analysysBean.getEx_id()) ? analysysBean.getEx_id() : "0");
                if (TextUtils.isEmpty(requestId)) {
                    map.put("request_item_rank", currentPosition % 10);
                } else {
                    map.put("request_item_rank", analysysBean.getRequest_item_rank());
                }

            } else {
                map.put("refer_tab", "0");
                map.put("ex_id", "0");
                map.put("request_id", "0");
                map.put("request_item_rank", (currentPosition + 1) % 10);
            }
            map.put("request_id", TextUtils.isEmpty(requestId) ? viewModel.getRequestId() : requestId);
            if (!TextUtils.isEmpty(spotBean.getAlgoInfo())){
                map.put("algo_info",spotBean.getAlgoInfo());
            }
            map.put("item_type", "单片视频");
            map.put("item_id", TextUtils.isEmpty(spotBean.getTitleId()) ? "" : spotBean.getTitleId());
            map.put("item_name", TextUtils.isEmpty(spotBean.getTitle()) ? "" : spotBean.getTitle());
            map.put("video_id", TextUtils.isEmpty(spotBean.getTitleId()) ? "" : spotBean.getTitleId());
            map.put("video_name", TextUtils.isEmpty(spotBean.getTitle()) ? "" : spotBean.getTitle());
            map.put("video_length", spotBean.getDuration());
            map.put("play_module", "点播");
            map.put("start_video_length", 0);
            int end_video_length = (int) (mVideoView.getCurrentPosition() / 1000.0f);
            map.put("end_video_length", end_video_length);

            map.put("play_length", playViewControl.getPlay_length());
            map.put("play_percent", (int) ((end_video_length * 1.0f / spotBean.getDuration()) * 100));
            Log.e("video_close", "video_close" + new Gson().toJson(map));

            AnalysysAgent.track(this, "video_close", map);
            requestId = "";
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PlayListActivity", "video_close" + e.getMessage());
        }
    }

    //埋点-切换视频
    protected boolean isChangeVideo;//是否切换视频

    private void switchVideo(List<SpotBean> datas, int currentPosition, boolean isUp) {
        try {
            if (CollectionUtils.isEmpty(datas)) return;
            Map<String, Object> map = new HashMap<>();
            if (currentPosition == 0 && !isChangeVideo) {
                return;
            } else {
                if (datas.size() > currentPosition) {
                    SpotBean preData = datas.get(isUp ? (currentPosition - 1) : (currentPosition + 1));
                    if (preData != null) {
                        map.put("video_id_before", preData.getTitleId());
                        map.put("video_name_before", preData.getTitle());
                    } else {
                        map.put("video_id_before", "0");
                        map.put("video_name_before", "0");
                    }
                    SpotBean curData = datas.get(currentPosition);
                    if (curData != null) {
                        map.put("video_id", curData.getTitleId());
                        map.put("video_name", curData.getTitle());
                    } else {
                        map.put("video_id", "0");
                        map.put("video_name", "0");
                    }
                } else {
                    map.put("video_id_before", "0");
                    map.put("video_name_before", "0");
                }
                isChangeVideo = true;
                map.put("type", isUp ? "向上滑动" : "向下滑动");
            }
            map.put("switch_id", openId + "_" + System.currentTimeMillis());
            Log.e("PlayListActivity", "switch_video" + new Gson().toJson(map));
            AnalysysAgent.track(this, "switch_video", map);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("PlayListActivity", "switch_video" + e.getMessage());
        }
    }
}
