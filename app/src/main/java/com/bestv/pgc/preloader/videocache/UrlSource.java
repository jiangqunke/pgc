package com.bestv.pgc.preloader.videocache;

public interface UrlSource extends Source{

    String getUrl();

    String getMime() throws ProxyCacheException;
}






