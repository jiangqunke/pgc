package com.bestv.pgc.ui;

import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bestv.pgc.beans.Entity;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.net.ApiUrl;
import com.bestv.pgc.net.OkHttpUtils;
import com.bestv.pgc.net.OnResultCallBack;
import com.bestv.pgc.util.BestvAgent;

import java.util.HashMap;
import java.util.Map;

public class PlayHotViewModel extends ViewModel implements LifecycleObserver {
    MutableLiveData<SpotBean> pgcData = new MutableLiveData<>();
    MutableLiveData<Void> failData = new MutableLiveData<>();
    MutableLiveData<Boolean> praiseData = new MutableLiveData<>();
    private String openId;
    private String poi;
    private String scene;
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void init(String openId, String poi, String scene){
        this.openId = openId;
        this.poi = poi;
        this.scene = scene;
    }

    public void loadSpotDatas() {
        Map<String, Object> params = new HashMap<>();
        params.put("openId", openId);
        params.put("poi", poi);
        params.put("scene", scene);
        params.put("limit", "10");
        requestId = openId+System.currentTimeMillis();
        params.put("requestId", requestId);

        OkHttpUtils.getInstance().post(ApiUrl.pgc_list, params, new OnResultCallBack<SpotBean>() {
            @Override
            public void onResponse(SpotBean data) {
                pgcData.setValue(data);
            }

            @Override
            public void onError(int code, String errorMsg) {
                failData.setValue(null);
            }
        });
    }

    public void praise(boolean isPraise, String titleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("mcUserId", openId);
        params.put("titleId", titleId);
        OkHttpUtils.getInstance().post(isPraise ? ApiUrl.URl_video_cancel_praise : ApiUrl.URl_video_praise, params, new OnResultCallBack<Entity>() {
            @Override
            public void onResponse(Entity data) {
                praiseData.setValue(!isPraise);
                if (BestvAgent.getInstance().getPariseListening() != null){
                    BestvAgent.getInstance().getPariseListening().pariseState(titleId,!isPraise);
                }
            }

            @Override
            public void onError(int code, String errorMsg) {
            }
        });
    }
}
