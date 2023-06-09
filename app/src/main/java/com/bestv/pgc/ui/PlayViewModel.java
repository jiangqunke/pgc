package com.bestv.pgc.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bestv.pgc.beans.Entity;
import com.bestv.pgc.beans.SpotBean;
import com.bestv.pgc.net.ApiUrl;
import com.bestv.pgc.net.OkHttpUtils;
import com.bestv.pgc.net.OnResultCallBack;
import com.bestv.pgc.util.BestvAgent;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class PlayViewModel extends ViewModel implements LifecycleObserver {
    MutableLiveData<SpotBean> pgcData = new MutableLiveData<>();
    MutableLiveData<Void> failData = new MutableLiveData<>();
    MutableLiveData<Boolean> praiseData = new MutableLiveData<>();
    private String openId;
    private String line;
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void init(String openId, String line){
        this.openId = openId;
        this.line = line;
    }

    public void loadSpotDatas(int page) {
        Map<String, Object> params = new HashMap<>();
        params.put("openId", openId);
        params.put("line", line);
        params.put("offset", page);
        params.put("limit", "15");
        requestId = openId+System.currentTimeMillis();
        params.put("requestId", requestId);

        OkHttpUtils.getInstance().post(ApiUrl.pgc_line_list, params, new OnResultCallBack<SpotBean>() {
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
        params.put("mcUserId",openId);
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
