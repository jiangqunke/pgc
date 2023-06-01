package com.bestv.pgc.net;

public class ApiUrl {
    /*
     * 正式环境
     */
//    public static final String url = "https://bp-api.bestv.com.cn/";
    /*
     * 测试环境
     */
    public static final String url = "http://121.41.196.103:32016";

    public static final String pgc_list = url+"/content/metroCity/internal/media/list";
    /**
     * 用户取消点赞
     */
    public static final String URl_video_cancel_praise = url + "/log/metroCity/cancelPraise";

    /**
     * 用户点赞
     */
    public static final String URl_video_praise = url + "/log/metroCity/praise";
}
