package com.netcall.https;

import android.content.Context;

import com.netcall.BaseCall;
import com.netcall.NetCall;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class NetCallHttps {

    private BaseCall call;
    private IHttpsData httpsData;

    public NetCallHttps(BaseCall call) {
        this.call = call;
    }

    public void setHttpData(IHttpsData httpData) {
        this.httpsData = httpData;
    }

    public void setHttpClient(OkHttpClient.Builder clientBuilder) {

        Context context = NetCall.getContext();
        if (context == null) {
            throw new IllegalStateException("context is null! Please invoke NetCall.init(Context) first");
        }

        IHttpsData httpsData = this.httpsData;
        if (!NetCall.isHttpsAuthEnable()) {
            httpsData = new HttpsDataDisable();
        } else if (httpsData == null) {
            httpsData = new HttpsDataDef(call);
            setHttpData(httpsData);
        }

        // 适用于okhttp的https逻辑代码
        HttpsUtil httpsUtil = new HttpsUtil(httpsData);
        X509TrustManager trustManager = httpsUtil.getTrustManager();
        SSLSocketFactory sslSocketFactory = httpsUtil.getSslSocketFactory(trustManager);
        if (sslSocketFactory != null) {
            clientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
                    .hostnameVerifier(httpsUtil.getHostnameVerifier());
        }
    }
}
