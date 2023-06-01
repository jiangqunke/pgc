package com.bestv.pgc.preloader.videocache;

import static com.bestv.pgc.preloader.videocache.Preconditions.checkNotNull;

import android.annotation.SuppressLint;
import android.text.TextUtils;


import com.bestv.pgc.preloader.videocache.headers.EmptyHeadersInjector;
import com.bestv.pgc.preloader.videocache.headers.HeaderInjector;
import com.bestv.pgc.preloader.videocache.sourcestorage.SourceInfoStorage;
import com.bestv.pgc.preloader.videocache.sourcestorage.SourceInfoStorageFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;


public class OKhttpUrlSource implements UrlSource {
    private static final int MAX_REDIRECTS = 5;
    private final SourceInfoStorage sourceInfoStorage;
    private final HeaderInjector headerInjector;
    private SourceInfo sourceInfo;
    private OkHttpClient mOkHttpClient;
    private Request.Builder mRequestBuilder;
    private Request.Builder mRequestBuilder2;
    private Response mResponse;
    private Response mResponse2;
    private String mUrl;
    private long mTimeout = 10000;
    private boolean mIgnoreCert = true;
    private int mRedirectCount = 0;
    private int mRedirectCount2 = 0;
    private long mOffset = 0;

    public OKhttpUrlSource(String url) {
        this(url, SourceInfoStorageFactory.newEmptySourceInfoStorage());
    }

    public OKhttpUrlSource(String url, SourceInfoStorage sourceInfoStorage) {
        this(url, sourceInfoStorage, new EmptyHeadersInjector());
    }

    public OKhttpUrlSource(String url, SourceInfoStorage sourceInfoStorage, HeaderInjector headerInjector) {
        this.sourceInfoStorage = checkNotNull(sourceInfoStorage);
        this.headerInjector = checkNotNull(headerInjector);
        SourceInfo sourceInfo = sourceInfoStorage.get(url);
        this.sourceInfo = sourceInfo != null ? sourceInfo :
                new SourceInfo(url, Integer.MIN_VALUE, ProxyCacheUtils.getSupposablyMime(url));
        this.mUrl = url;
        mOkHttpClient = createOkHttpClient(mUrl, mTimeout, mTimeout, mIgnoreCert);
    }

    public OKhttpUrlSource(OKhttpUrlSource source) {
    //    this.sourceInfo = source.sourceInfo;
        this.sourceInfoStorage = source.sourceInfoStorage;
        this.headerInjector = source.headerInjector;
        this.mUrl = source.mUrl;
        this.mOkHttpClient = source.mOkHttpClient;
        SourceInfo sourceInfo = sourceInfoStorage.get(mUrl);
        this.sourceInfo = sourceInfo != null ? sourceInfo : source.sourceInfo;
    }

    @Override
    public synchronized long length() throws ProxyCacheException {
        if (sourceInfo.length == Integer.MIN_VALUE) {
            fetchContentInfo();
        }
        return sourceInfo.length;
    }

    public synchronized String getMime() throws ProxyCacheException {
        if (TextUtils.isEmpty(sourceInfo.mime)) {
            fetchContentInfo();
        }
        return sourceInfo.mime;
    }

    @Override
    public String getUrl() {
        return sourceInfo.url;
    }

    @Override
    public String toString() {
        return "OKhttpUrlSource{sourceInfo='" + sourceInfo + "}";
    }

    @Override
    public void open(long offset) throws ProxyCacheException {
        mOffset = offset;
        try {
            mRequestBuilder2 = createRequestBuilder2(mUrl, null, offset);
            markRequest2();
            long length = parseContentLengthFromContentRange(mResponse2);
            String mime = getContentType(mResponse2);
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
        } catch (IOException e) {
            throw new ProxyCacheException("Error opening connection for " + sourceInfo.url + " with offset " + offset, e);
        }
    }

    @Override
    public void close() throws ProxyCacheException {
        if (mResponse != null) {
            mResponse.close();
            mResponse = null;
        }
        if (mResponse2 != null) {
            mResponse2.close();
            mResponse2 = null;
        }
    }

    @Override
    public int read(byte[] buffer) throws ProxyCacheException {
        InputStream inputStream = getResponseBody(mResponse2);
        try {
            return inputStream.read(buffer, 0, buffer.length);
        } catch (InterruptedIOException e) {
            throw new InterruptedProxyCacheException("Reading source " + sourceInfo.url + " is interrupted", e);
        } catch (IOException e) {
            throw new ProxyCacheException("Error reading data from " + sourceInfo.url, e);
        }
    }

    private void fetchContentInfo() throws ProxyCacheException {
        mRequestBuilder = createRequestBuilder(mUrl, null, true);
        try {
            markRequest();
            long length = getContentLength(mResponse);
            String mime = getContentType(mResponse);
            this.sourceInfo = new SourceInfo(sourceInfo.url, length, mime);
            this.sourceInfoStorage.put(sourceInfo.url, sourceInfo);
            if (mResponse != null) {
                mResponse.close();
                mResponse = null;
            }
        } catch (IOException ioe) {
        }
    }

    private void markRequest() throws IOException, ProxyCacheException {
        mResponse = mOkHttpClient.newCall(mRequestBuilder.build()).execute();
        if (shouldRedirect(mResponse)) {
            mRedirectCount++;
            if (mRedirectCount > MAX_REDIRECTS)
                throw new ProxyCacheException("Too many redirects: " + mRedirectCount);
            //mUrl已经发生了变化了
            mOkHttpClient = createOkHttpClient(mUrl, mTimeout, mTimeout, mIgnoreCert);
            mRequestBuilder = createRequestBuilder(mUrl, null, true);

            //重新请求
            markRequest();
        }
    }

    private void markRequest2() throws IOException, ProxyCacheException {
        mResponse2 = mOkHttpClient.newCall(mRequestBuilder2.build()).execute();
        if (shouldRedirect(mResponse2)) {
            mRedirectCount2++;
            if (mRedirectCount > MAX_REDIRECTS)
                throw new ProxyCacheException("Too many redirects: " + mRedirectCount2);
            //mUrl已经发生了变化了
            mOkHttpClient = createOkHttpClient(mUrl, mTimeout, mTimeout, mIgnoreCert);
            mRequestBuilder = createRequestBuilder2(mUrl, null, mOffset);

            //重新请求
            markRequest2();
        }
    }

    private boolean shouldRedirect(Response response) {
        if (response == null) return false;
        int code = response.code();
        if (code == 300 || code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
            String url = response.header("Location");
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            mUrl = url;
            return true;
        }
        return false;
    }

    private long getContentLength(Response response) {
        if (response == null) {
            return -1;
        }
        if (response.code() == 200 || response.code() == 206) {
            String contentLength = response.header("content-length");
            if (response.code() == 206) {
            }
            if (TextUtils.isEmpty(contentLength)) {
                return -1;
            }
            return Long.parseLong(contentLength);
        }
        return -1;
    }

    private String getContentType(Response response) {
        if (response == null) {
            return null;
        }
        if (response.code() == 200 || response.code() == 206) {
            return response.header("content-type");
        }
        return null;
    }

    private InputStream getResponseBody(Response response) {
        if (response == null) {
            return null;
        }
        if (response.code() == 200 || response.code() == 206) {
            return response.body().byteStream();
        } else {
            try {
                response.body().byteStream().close();
            } catch (IOException ioe) {

            }
            return null;
        }
    }

    public long parseContentLengthFromContentRange(Response response) {
        if (response == null) {
            return -1;
        }

        if (response.code() == 200 || response.code() ==206) {
            String contentRange = response.header("Content-Range");
            if (TextUtils.isEmpty(contentRange)) {
                return -1;
            }
            int index = contentRange.lastIndexOf("/");
            if (index == -1 || index + 1 >= contentRange.length()) {
                return -1;
            }
            String contentLength = contentRange.substring(index + 1).trim();
            try {
                return Long.parseLong(contentLength);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    private static OkHttpClient createOkHttpClient(String url, long readTimeout, long connTimeout, boolean ignoreCert) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(readTimeout, TimeUnit.MILLISECONDS);
        builder.connectTimeout(connTimeout, TimeUnit.MILLISECONDS);
        builder.eventListener(new OkHttpEventListener(url));
        if (HttpUrl.parse(url).isHttps() && ignoreCert) {
            trustCert(builder);
        }
        return builder.build();
    }

    public static Request.Builder createRequestBuilder(String url, Map<String, String> headers, boolean isHeadRequest) {
        Request.Builder requestBuilder;
        if (isHeadRequest) {
            requestBuilder = new Request.Builder().url(url).head();
        } else {
            requestBuilder = new Request.Builder().url(url);
        }
        if (headers != null) {
            Iterator iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return requestBuilder;
    }

    public static Request.Builder createRequestBuilder2(String url, Map<String, String> headers, long offset) {
        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (headers != null) {
            Iterator iterator = headers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        requestBuilder.addHeader("range", "bytes=" + offset + "-");
        return requestBuilder;
    }

    private static void trustCert(OkHttpClient.Builder builder) {
        X509TrustManager trustManager = new CustomTrustManager();
        SSLSocketFactory sslSocketFactory = null;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
        }
        if (trustManager != null && sslSocketFactory != null) {
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }
        HostnameVerifier hostnameVerifier = (hostname, session) -> true;
        builder.hostnameVerifier(hostnameVerifier);
    }

    private static class CustomTrustManager implements X509TrustManager {

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @SuppressLint("TrustAllX509TrustManager")
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class OkHttpEventListener extends EventListener {

        private final String mUrl;
        private long mStartTime;

        public OkHttpEventListener(String url) {
            mUrl = url;
        }

        private long getCurrentTimeDuration() {
            return (System.nanoTime() - mStartTime) / 1000000;
        }

        @Override
        public void callStart( Call call) {
            super.callStart(call);

            //获取当前的header头部
            String rangeHeader = call.request().header("Range");
            if (!TextUtils.isEmpty(rangeHeader)) {
                //mListener.onRequestStart(mUrl, rangeHeader);
            }
            mStartTime = System.nanoTime();
        }

        @Override
        public void dnsStart( Call call,  String domainName) {
            super.dnsStart(call, domainName);
        }

        @Override
        public void dnsEnd( Call call,  String domainName,  List<InetAddress> inetAddressList) {
            super.dnsEnd(call, domainName, inetAddressList);
        }

        @Override
        public void connectStart( Call call,  InetSocketAddress inetSocketAddress,  Proxy proxy) {
            super.connectStart(call, inetSocketAddress, proxy);
        }

        @Override
        public void secureConnectStart( Call call) {
            super.secureConnectStart(call);
        }

        @Override
        public void secureConnectEnd( Call call,  Handshake handshake) {
            super.secureConnectEnd(call, handshake);
        }

        @Override
        public void connectEnd( Call call,  InetSocketAddress inetSocketAddress,  Proxy proxy,  Protocol protocol) {
            super.connectEnd(call, inetSocketAddress, proxy, protocol);
        }

        @Override
        public void connectFailed( Call call,  InetSocketAddress inetSocketAddress,  Proxy proxy,  Protocol protocol,  IOException ioe) {
            super.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        }

        @Override
        public void connectionAcquired( Call call,  Connection connection) {
            super.connectionAcquired(call, connection);
        }

        @Override
        public void connectionReleased( Call call,  Connection connection) {
            super.connectionReleased(call, connection);
        }

        @Override
        public void requestHeadersStart( Call call) {
            super.requestHeadersStart(call);
        }

        @Override
        public void requestHeadersEnd( Call call,  Request request) {
            super.requestHeadersEnd(call, request);
        }

        @Override
        public void requestBodyStart( Call call) {
            super.requestBodyStart(call);
        }

        @Override
        public void requestBodyEnd( Call call, long byteCount) {
            super.requestBodyEnd(call, byteCount);
        }

        @Override
        public void responseHeadersStart( Call call) {
            super.responseHeadersStart(call);
        }

        @Override
        public void responseHeadersEnd( Call call,  Response response) {
            super.responseHeadersEnd(call, response);
        }

        @Override
        public void responseBodyStart( Call call) {
            super.responseBodyStart(call);
        }

        @Override
        public void responseBodyEnd( Call call, long byteCount) {
            super.responseBodyEnd(call, byteCount);
        }

        @Override
        public void callEnd( Call call) {
            super.callEnd(call);
        }

        @Override
        public void callFailed( Call call,  IOException ioe) {
            super.callFailed(call, ioe);
        }
    }

}
