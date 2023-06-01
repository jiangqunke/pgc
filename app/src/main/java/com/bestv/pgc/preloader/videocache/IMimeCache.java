package com.bestv.pgc.preloader.videocache;

public interface IMimeCache {

    void putMime(String url, long length, String mime);

    UrlMime getMime(String url);
}
