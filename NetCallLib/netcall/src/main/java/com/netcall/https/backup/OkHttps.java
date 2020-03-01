package com.netcall.https.backup;

import android.content.Context;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class OkHttps {

    private Context context;
    private OkHttpClient.Builder clientBuilder;
    private String[] hostNames;

    public static void toHttps(Context context, OkHttpClient.Builder clientBuilder, String... hostName) {
        toHttps(context, HttpsUtil2.TYPE_HTTPS_BOTH_AUTH, clientBuilder, hostName);
    }

    public static void toHttps(Context context, int type, OkHttpClient.Builder clientBuilder, String... hostName) {
        new OkHttps(context, type, clientBuilder, hostName);
    }

    private OkHttps(Context context, int type, OkHttpClient.Builder clientBuilder, String... hostName) {
        this.context = context;
        if (hostName != null && hostName.length != 0) {
            hostNames = hostName;
        }
        this.clientBuilder = clientBuilder;
        initHttps(type);
    }

    private void initHttps(int type) {

        HttpsUtil2 httpsUtil = new HttpsUtil2(context, type);
        X509TrustManager trustManager = httpsUtil.newTrustManager();
        SSLSocketFactory sslSocketFactory = httpsUtil.newSslSocketFactory(trustManager);
        if (sslSocketFactory != null) {
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(new HttpsUtil2.MyHostnameVerifier(hostNames));
        }
    }

}
