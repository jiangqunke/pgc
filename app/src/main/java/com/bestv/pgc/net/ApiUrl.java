package com.bestv.pgc.net;

import com.bestv.pgc.util.BestvAgent;

public class ApiUrl {


    public static final String pgc_list = BestvAgent.getInstance().getUrl()+"/content/metroCity/internal/media/list";
    public static final String pgc_line_list = BestvAgent.getInstance().getUrl()+"/content/metroCity/internal/media/lineDataList";


    /**
     * 用户取消点赞
     */
    public static final String URl_video_cancel_praise = BestvAgent.getInstance().getUrl() + "/log/metroCity/cancelPraise";

    /**
     * 用户点赞
     */
    public static final String URl_video_praise = BestvAgent.getInstance().getUrl() + "/log/metroCity/praise";
}
