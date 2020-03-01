package com.netcall.https;

import android.content.Context;
import android.text.TextUtils;

import com.netcall.BaseCall;
import com.netcall.NetCall;
import com.netcall.annotation.CallHttps;
import com.netcall.annotation.CallHttps.E_PATH;
import com.netcall.util.EmptyUtil;
import com.netcall.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;

import javax.net.ssl.HostnameVerifier;

/**
 * 默认的Https数据生成者
 */
public class HttpsDataDef implements IHttpsData {

    private BaseCall call;
    private CallHttps callHttps;
    private HostnameVerifier hostnameVerifier;

    public HttpsDataDef(BaseCall call) {
        this.call = call;
        callHttps = call.getClass().getAnnotation(CallHttps.class);
    }

    @Override
    public HostnameVerifier getHostNameVerifier() {
        if (hostnameVerifier != null) {
            return hostnameVerifier;
        }
        Class<? extends HostnameVerifier> clazz = callHttps == null ? null : callHttps.hostnameVerifier();
        if (clazz == null) {
            hostnameVerifier = new HostnameVerifierDef();
            return hostnameVerifier;
        }
        try {
            hostnameVerifier = clazz.newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        }
        return hostnameVerifier;
    }

    @Override
    public InputStream[] getTrustCertStream() {
        if (callHttps == null) {
            return new InputStream[0];
        }
        String[] trustCertPaths = callHttps.trustCrtPaths();
        if (EmptyUtil.isEmpty(trustCertPaths)) {
            return new InputStream[0];
        }

        InputStream[] inputStreams = new InputStream[trustCertPaths.length];
        E_PATH pathType = callHttps.pathType();
        if (pathType == E_PATH.ASSETS) {
            Context context = NetCall.getContext();
            for (int i = 0; i < trustCertPaths.length; i++) {
                String path = trustCertPaths[i];
                InputStream inputStream = getCertInputStreamAssets(context, path);
                if (inputStream == null) {
                    throw new IllegalStateException("Assets path [" + path + "] data is not right");
                }
                inputStreams[i] = inputStream;
            }
        } else if (pathType == E_PATH.FILE) {

            for (int i = 0; i < trustCertPaths.length; i++) {
                String path = trustCertPaths[i];
                InputStream inputStream = getCertInputStreamFile(path);
                if (inputStream == null) {
                    throw new IllegalStateException("File path [" + path + "] data is not right");
                }
                inputStreams[i] = inputStream;
            }
        } else {
            throw new IllegalStateException("Class [" + call.getClass().getSimpleName() + "] " +
                    "CallHttps's path type " + pathType + " not legal");
        }
        return inputStreams;
    }

    @Override
    public InputStream getClientCertStream() {

        if (callHttps == null) {
            return null;
        }
        String path = callHttps.clientBksPath();

        if (TextUtils.isEmpty(path)) {
            return null;
        }

        InputStream inputStream;
        E_PATH pathType = callHttps.pathType();
        if (pathType == E_PATH.ASSETS) {
            Context context = NetCall.getContext();
            inputStream = getCertInputStreamAssets(context, path);
            if (inputStream == null) {
                throw new IllegalStateException("Assets path [" + path + "] data is not right");
            }
        } else if (pathType == E_PATH.FILE) {
            inputStream = getCertInputStreamFile(path);
            if (inputStream == null) {
                throw new IllegalStateException("File path [" + path + "] data is not right");
            }
        } else {
            throw new IllegalStateException("Class [" + call.getClass().getSimpleName() + "] " +
                    "CallHttps's path type " + pathType + " not legal");
        }

        return inputStream;
    }

    @Override
    public String getClientCertPsw() {
        if (callHttps == null) {
            return null;
        }
        return callHttps.clientBksPsw();
    }

    private InputStream getCertInputStreamAssets(Context context, String certPath) {
        try {
            InputStream input = context.getResources().getAssets().open(certPath);
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getCertInputStreamFile(String certPath) {
        return FileUtil.readStream(certPath);
    }
}
