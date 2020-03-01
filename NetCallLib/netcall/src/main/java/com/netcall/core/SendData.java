package com.netcall.core;

import android.text.TextUtils;

import com.netcall.NetCall;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class SendData {

    public static String METHOD_GET = "GET";
    public static String METHOD_POST = "POST";

    private String method = METHOD_GET;
    private String baseUrl;
    private String suffixUrl;
    private Params headerParams;
    private Params urlParams;
    private Params formParams;
    private String postText;
    private File postFile;
    private String mediaType;

    private String urlWithoutParam;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getSuffixUrl() {
        return suffixUrl;
    }

    public void setSuffixUrl(String suffixUrl) {
        this.suffixUrl = suffixUrl;
    }

    public Params getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(Params headerParams) {
        this.headerParams = headerParams;
    }

    public Params getUrlParams() {
        return urlParams;
    }

    public void setQueryParams(Params urlParams) {
        this.urlParams = urlParams;
    }

    public Params getFormParams() {
        return formParams;
    }

    public void setFormParams(Params formParams) {
        this.formParams = formParams;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public File getPostFile() {
        return postFile;
    }

    public void setPostFile(File postFile) {
        this.postFile = postFile;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getUrlWithoutParam() {
        if (TextUtils.isEmpty(urlWithoutParam)) {
            urlWithoutParam = combineUrl(getBaseUrl(), getSuffixUrl());
        }
        return urlWithoutParam;
    }

    public boolean isNeedHttps() {
        return baseUrl == null ? false : baseUrl.startsWith("https://");
    }

    public Request toRequest() {

        urlWithoutParam = combineUrl(getBaseUrl(), getSuffixUrl());
        String url = combineUrl(urlWithoutParam, getUrlParams());
        Headers headers = Headers.of(getHeaderParams().toMap());
        Request request;

        if (SendData.METHOD_POST.equals(getMethod())) {

            RequestBody requestBody;
            if (getPostFile() != null) {
                MediaType mediaType = MediaType.parse(getMediaType());
                requestBody = RequestBody.create(getPostFile(), mediaType);
            } else if (getPostText() != null) {
                MediaType mediaType = MediaType.parse(getMediaType());
                requestBody = RequestBody.create(getPostText(), mediaType);
            } else {
                FormBody.Builder builder = new FormBody.Builder();
                if (getFormParams() != null) {
                    for (Param param : getFormParams()) {
                        builder.add(param.getName(), param.getValueEncoded());
                    }
                }
                requestBody = builder.build();
            }

            request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .post(requestBody)
                    .build();
        } else {

            request = new Request.Builder()
                    .url(url)
                    .headers(headers)
                    .get()
                    .build();
        }

        return request;
    }

    public void clear() {

        method = METHOD_GET;
        baseUrl = null;
        suffixUrl = null;
        headerParams = null;
        urlParams = null;
        formParams = null;
        postText = null;
        postFile = null;
        mediaType = null;
        urlWithoutParam = null;
    }

    public String combineUrl(String baseUrl, String suffixUrl) {

        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalStateException("base Url is empty");
        }

        StringBuilder urlBuilder = new StringBuilder();

        boolean slash1 = baseUrl.endsWith("/");
        boolean slash2 = suffixUrl.startsWith("/");
        if (slash1 ^ slash2) {
            urlBuilder.append(baseUrl);
            urlBuilder.append(suffixUrl);
        } else if (slash1 & slash2) {
            urlBuilder.append(baseUrl);
            urlBuilder.append(suffixUrl.substring(1));
        } else {
            urlBuilder.append(baseUrl);
            urlBuilder.append("/");
            urlBuilder.append(suffixUrl);
        }
        return urlBuilder.toString();
    }

    public String combineUrl(String frontUrl, Params queryParams) {

        if (frontUrl == null || frontUrl.isEmpty()) {
            throw new IllegalStateException("base Url is empty");
        }

        StringBuilder urlBuilder = new StringBuilder(frontUrl);
        String paramString = queryParams.toParamString();
        if (!paramString.isEmpty()) {
            urlBuilder.append("?");
            urlBuilder.append(paramString);
        }
        return urlBuilder.toString();
    }
}
