package com.netcall.https;

import com.netcall.NetCall;

import java.io.InputStream;

import javax.net.ssl.HostnameVerifier;

/**
 * {@link NetCall#isHttpsAuthEnable()} 为false时，会使用该类作为https数据生成者
 */
public class HttpsDataDisable implements IHttpsData {

    private HostnameVerifier hostnameVerifier;

    @Override
    public HostnameVerifier getHostNameVerifier() {
        if (hostnameVerifier != null) {
            return hostnameVerifier;
        }
        hostnameVerifier = new HostnameVerifierDef();
        return hostnameVerifier;
    }

    @Override
    public InputStream[] getTrustCertStream() {
        return new InputStream[0];
    }

    @Override
    public InputStream getClientCertStream() {
        return null;
    }

    @Override
    public String getClientCertPsw() {
        return null;
    }
}
