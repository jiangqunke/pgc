package com.bestv.pgc.player;

import static com.google.android.exoplayer2.util.Assertions.checkNotNull;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.util.Util;
import com.google.common.base.Ascii;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExoMediaSourceHelper {
    DrmSessionManager drmSessionManager;
    private final String mUserAgent;
    private DataSource.Factory mDataSourceFactory;
    private Context mAppContext;
    private Cache mCache;
    private HttpDataSource.Factory mHttpDataSourceFactory;
    private static final Pattern ISM_PATH_PATTERN =
            Pattern.compile("(?:.*\\.)?isml?(?:/(manifest(.*))?)?", Pattern.CASE_INSENSITIVE);
    private static final String ISM_HLS_FORMAT_EXTENSION = "format=m3u8-aapl";
    private static final String ISM_DASH_FORMAT_EXTENSION = "format=mpd-time-csf";

    public ExoMediaSourceHelper(Context context) {
        mAppContext = context.getApplicationContext();
        mUserAgent = Util.getUserAgent(mAppContext, mAppContext.getApplicationInfo().name);
    }

    public MediaSource getMediaSource(String uri) {
        return getMediaSource(uri, null, false);
    }

    public MediaSource getMediaSource(String uri, Map<String, String> headers) {
        return getMediaSource(uri, headers, false);
    }

    public MediaSource getMediaSource(String uri, boolean isCache) {
        return getMediaSource(uri, null, isCache);
    }

    public MediaSource getMediaSource(String uri, Map<String, String> headers, boolean isCache) {
        Uri contentUri = Uri.parse(uri);
//        if ("rtmp".equals(contentUri.getScheme())) {
//            return new ProgressiveMediaSource.Factory(new RtmpDataSource.Factory())
//                    .createMediaSource(MediaItem.fromUri(contentUri));
//        } else if ("rtsp".equals(contentUri.getScheme())) {
//            return new RtspMediaSource.Factory().createMediaSource(MediaItem.fromUri(contentUri));
//        }
        int contentType = inferContentType(uri);
        DataSource.Factory factory;
        factory = getDataSourceFactory();
        if (mHttpDataSourceFactory != null) {
            setHeaders(headers);
        }
        switch (contentType) {
            case C.CONTENT_TYPE_DASH:
                return new DashMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(contentUri));
            case C.CONTENT_TYPE_HLS:
                return new HlsMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(contentUri));
            default:
            case C.CONTENT_TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(factory).createMediaSource(MediaItem.fromUri(contentUri));
        }
    }

    private int inferContentType(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.contains(".mpd")) {
            return C.CONTENT_TYPE_DASH;
        } else if (fileName.contains(".m3u8")) {
            return C.CONTENT_TYPE_HLS;
        } else {
            return C.CONTENT_TYPE_OTHER;
        }
    }


    /**
     * Returns a new DataSource factory.
     *
     * @return A new DataSource factory.
     */
    private DataSource.Factory getDataSourceFactory() {
        return new DefaultDataSource.Factory(mAppContext, getHttpDataSourceFactory());
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @return A new HttpDataSource factory.
     */
    private DataSource.Factory getHttpDataSourceFactory() {
        if (mHttpDataSourceFactory == null) {
            mHttpDataSourceFactory = new DefaultHttpDataSource.Factory()
                    .setUserAgent(mUserAgent)
                    .setAllowCrossProtocolRedirects(true);
        }
        return mHttpDataSourceFactory;
    }

    private void setHeaders(Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            //如果发现用户通过header传递了UA，则强行将HttpDataSourceFactory里面的userAgent字段替换成用户的
            if (headers.containsKey("User-Agent")) {
                String value = headers.remove("User-Agent");
                if (!TextUtils.isEmpty(value)) {
                    try {
                        Field userAgentField = mHttpDataSourceFactory.getClass().getDeclaredField("userAgent");
                        userAgentField.setAccessible(true);
                        userAgentField.set(mHttpDataSourceFactory, value);
                    } catch (Exception e) {
                        //ignore
                    }
                }
            }
            mHttpDataSourceFactory.setDefaultRequestProperties(headers);
        }
    }

    public void setCache(Cache cache) {
        this.mCache = cache;
    }

//    public MediaSource getMediaSource(String uri) {
//        drmSessionManager = DrmSessionManager.DRM_UNSUPPORTED;
//        Uri contentUri = Uri.parse(uri);
//        int contentType = inferContentType(contentUri);
//        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(mAppContext);
//        switch (contentType) {
//            case C.CONTENT_TYPE_DASH:
//                return  new DashMediaSource.Factory(dataSourceFactory)
//                        .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
//                        .createMediaSource(MediaItem.fromUri(uri));
//            default:
//            case C.CONTENT_TYPE_OTHER:
//                return
//                        new ProgressiveMediaSource.Factory(dataSourceFactory)
//                                .setDrmSessionManagerProvider(unusedMediaItem -> drmSessionManager)
//                                .createMediaSource(MediaItem.fromUri(uri));
//        }
//    }
//
//    public static @C.ContentType int inferContentType(Uri uri) {
//        @Nullable String scheme = uri.getScheme();
//        if (scheme != null && Ascii.equalsIgnoreCase("rtsp", scheme)) {
//            return C.CONTENT_TYPE_RTSP;
//        }
//
//        @Nullable String lastPathSegment = uri.getLastPathSegment();
//        if (lastPathSegment == null) {
//            return C.CONTENT_TYPE_OTHER;
//        }
//        int lastDotIndex = lastPathSegment.lastIndexOf('.');
//        if (lastDotIndex >= 0) {
//            @C.ContentType
//            int contentType = inferContentTypeForExtension(lastPathSegment.substring(lastDotIndex + 1));
//            if (contentType != C.CONTENT_TYPE_OTHER) {
//                // If contentType is TYPE_SS that indicates the extension is .ism or .isml and shows the ISM
//                // URI is missing the "/manifest" suffix, which contains the information used to
//                // disambiguate between Smooth Streaming, HLS and DASH below - so we can just return TYPE_SS
//                // here without further checks.
//                return contentType;
//            }
//        }
//
//        Matcher ismMatcher = ISM_PATH_PATTERN.matcher(checkNotNull(uri.getPath()));
//        if (ismMatcher.matches()) {
//            @Nullable String extensions = ismMatcher.group(2);
//            if (extensions != null) {
//                if (extensions.contains(ISM_DASH_FORMAT_EXTENSION)) {
//                    return C.CONTENT_TYPE_DASH;
//                } else if (extensions.contains(ISM_HLS_FORMAT_EXTENSION)) {
//                    return C.CONTENT_TYPE_HLS;
//                }
//            }
//            return C.CONTENT_TYPE_SS;
//        }
//
//        return C.CONTENT_TYPE_OTHER;
//    }
//    public static @C.ContentType int inferContentTypeForExtension(String fileExtension) {
//        fileExtension = Ascii.toLowerCase(fileExtension);
//        switch (fileExtension) {
//            case "mpd":
//                return C.CONTENT_TYPE_DASH;
//            case "m3u8":
//                return C.CONTENT_TYPE_HLS;
//            case "ism":
//            case "isml":
//                return C.CONTENT_TYPE_SS;
//            default:
//                return C.CONTENT_TYPE_OTHER;
//        }
//    }
//    public void setDataSourceFactory(DataSource.Factory factory) {
//        mDataSourceFactory = factory;
//    }
//
//    public void setHttpDataSourceFactory(HttpDataSource.Factory factory) {
//        mHttpDataSourceFactory = factory;
//    }
}
