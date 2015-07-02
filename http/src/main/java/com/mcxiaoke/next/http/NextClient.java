package com.mcxiaoke.next.http;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * User: mcxiaoke
 * Date: 15/7/1
 * Time: 14:13
 */
public final class NextClient {

    static class SingletonHolder {
        static NextClient INSTANCE = new NextClient();
    }

    public static NextClient getDefault() {
        return SingletonHolder.INSTANCE;
    }

    public static final String TAG = NextClient.class.getSimpleName();
    private boolean mDebug;
    private final OkHttpClient mClient;
    private Map<String, String> mParams;
    private Map<String, String> mHeaders;

    public NextClient() {
        mClient = new OkHttpClient();
        mClient.setFollowRedirects(true);
        mParams = new HashMap<String, String>();
        mHeaders = new HashMap<String, String>();
    }

    /***********************************************************
     * CLIENT PARAMS AND HEADERS
     * **********************************************************
     */

    public NextClient addParam(final String key, final String value) {
        mParams.put(key, value);
        return this;
    }

    public NextClient addParams(final Map<String, String> params) {
        mParams.putAll(params);
        return this;
    }

    public NextClient addHeader(final String key, final String value) {
        mHeaders.put(key, value);
        return this;
    }

    public NextClient addHeaders(final Map<String, String> headers) {
        mHeaders.putAll(headers);
        return this;
    }

    public NextClient setDebug(final boolean debug) {
        mDebug = debug;
        return this;
    }

    public NextClient setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        mClient.setHostnameVerifier(hostnameVerifier);
        return this;
    }

    public NextClient setSocketFactory(SocketFactory socketFactory) {
        mClient.setSocketFactory(socketFactory);
        return this;
    }

    public NextClient setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
        mClient.setSslSocketFactory(sslSocketFactory);
        return this;
    }

    public NextClient setFollowRedirects(boolean followRedirects) {
        mClient.setFollowRedirects(followRedirects);
        return this;
    }

    public NextClient setFollowSslRedirects(boolean followProtocolRedirects) {
        mClient.setFollowSslRedirects(followProtocolRedirects);
        return this;
    }

    public NextClient setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        mClient.setRetryOnConnectionFailure(retryOnConnectionFailure);
        return this;
    }

    public void setConnectTimeout(long timeout, TimeUnit unit) {
        mClient.setConnectTimeout(timeout, unit);
    }

    public void setReadTimeout(long timeout, TimeUnit unit) {
        mClient.setReadTimeout(timeout, unit);
    }

    public void setWriteTimeout(long timeout, TimeUnit unit) {
        mClient.setWriteTimeout(timeout, unit);
    }

    public void acceptGzipEncoding() {
        addHeader(HttpConsts.ACCEPT_ENCODING, HttpConsts.ENCODING_GZIP);
    }

    public void setUserAgent(final String userAgent) {
        addHeader(HttpConsts.USER_AGENT, userAgent);
    }

    public void setAuthorization(final String authorization) {
        addHeader(HttpConsts.AUTHORIZATION, authorization);
    }

    public void setReferer(final String referer) {
        addHeader(HttpConsts.REFERER, referer);
    }

    /***********************************************************
     * HTTP REQUEST METHODS
     * **********************************************************
     */

    public NextResponse head(final String url) throws IOException {
        return head(url, null);
    }

    public NextResponse head(final String url, final Map<String, String> queries) throws IOException {
        return head(url, queries, null);
    }

    public NextResponse head(final String url, final Map<String, String> queries, final Map<String, String> headers)
            throws IOException {
        return request(HttpMethod.HEAD, url, queries, headers);
    }

    public NextResponse get(final String url) throws IOException {
        return get(url, null, null);
    }

    public NextResponse get(final String url, final Map<String, String> queries) throws IOException {
        return get(url, queries, null);
    }

    public NextResponse get(final String url, final Map<String, String> queries, final Map<String, String> headers)
            throws IOException {
        return request(HttpMethod.GET, url, queries, headers);
    }

    public NextResponse delete(final String url) throws IOException {
        return delete(url, null, null);
    }

    public NextResponse delete(final String url, final Map<String, String> queries) throws IOException {
        return delete(url, queries, null);
    }

    public NextResponse delete(final String url, final Map<String, String> queries, final Map<String, String> headers)
            throws IOException {
        return request(HttpMethod.DELETE, url, queries, headers);
    }

    public NextResponse post(final String url, final Map<String, String> forms) throws IOException {
        return post(url, forms, null);
    }

    public NextResponse post(final String url, final Map<String, String> forms, final Map<String, String> headers)
            throws IOException {
        return request(HttpMethod.POST, url, forms, headers);
    }

    public NextResponse put(final String url, final Map<String, String> forms) throws IOException {
        return put(url, forms, null);
    }

    public NextResponse put(final String url, final Map<String, String> forms, final Map<String, String> headers)
            throws IOException {
        return request(HttpMethod.PUT, url, forms, headers);
    }

    public NextResponse request(final HttpMethod method, final String url,
                                final Map<String, String> params,
                                final Map<String, String> headers)
            throws IOException {
        final NextRequest request = new NextRequest(method, url).headers(headers);
        if (HttpMethod.supportBody(method)) {
            // be careful, only POST/PUT/PATCH support bodies
            request.form(params);
        } else {
            // add params to queries for HEAD/GET/DELETE
            request.queries(params);
        }
        return execute(request);
    }

    public NextResponse get(final String url, final NextParams params) throws IOException {
        return request(HttpMethod.GET, url, params);
    }

    public NextResponse delete(final String url, final NextParams params) throws IOException {
        return request(HttpMethod.DELETE, url, params);
    }

    public NextResponse post(final String url, final NextParams params) throws IOException {
        return request(HttpMethod.POST, url, params);
    }

    public NextResponse put(final String url, final NextParams params) throws IOException {
        return request(HttpMethod.PUT, url, params);
    }

    public NextResponse request(final HttpMethod method, final String url,
                                final NextParams params)
            throws IOException {
        return request(method, url, params, null);
    }

    public NextResponse request(final HttpMethod method, final String url,
                                final NextParams params,
                                final Map<String, String> headers)
            throws IOException {
        final NextRequest request = new NextRequest(method, url)
                .headers(headers).params(params);
        return execute(request);
    }

    public NextResponse execute(final NextRequest nr)
            throws IOException {
        return new NextResponse(executeRequest(nr));
    }

    protected Response executeRequest(final NextRequest nr)
            throws IOException {
        // add client params and headers to request
        nr.form(mParams).headers(mHeaders);
        return executeInternal(nr);
    }

    protected Response executeInternal(final NextRequest nr)
            throws IOException {
        final Request request = new Request.Builder()
                .url(nr.getUrl())
                .headers(Headers.of(nr.headers()))
                .method(nr.method().name(), nr.getRequestBody()).build();
        final OkHttpClient client = mClient.clone();
        if (mDebug || nr.debug()) {
            // intercept for logging
            client.networkInterceptors().add(new LoggingInterceptor());
        }
        // intercept for progress callback
        if (nr.listener() != null) {
            client.interceptors().add(new ProgressInterceptor(nr.listener()));
        }
        return client.newCall(request).execute();

    }

}
