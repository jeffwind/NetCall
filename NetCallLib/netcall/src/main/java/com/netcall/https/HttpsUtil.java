package com.netcall.https;

import com.netcall.util.EmptyUtil;
import com.netcall.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * https工具
 * 该类可脱离NetCall框架使用，内部不要有NetCall业务相关的逻辑。
 */
public class HttpsUtil {

    private IHttpsData httpsData;

    /** 是否需要认证服务器，单向认证 */
    private boolean needTrustCert;

    /** 是否需要认证客户端，双项认证 */
    private boolean needClientCert;

    public HttpsUtil(IHttpsData httpsData) {
        this.httpsData = httpsData;
    }

    public HostnameVerifier getHostnameVerifier() {
        return httpsData.getHostNameVerifier();
    }

    /**
     * 获取trustManager
     */
    public X509TrustManager getTrustManager() {

        X509TrustManager trustManager;
        InputStream[] inputStreams = httpsData.getTrustCertStream();
        try {
            needTrustCert = isNeedTrustCert(inputStreams);
            trustManager = trustManagerForCertificates(inputStreams);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } finally {
            StreamUtil.closeStreams(inputStreams);
        }
        return trustManager;
    }

    /**
     * ssl socket factory
     */
    public SSLSocketFactory getSslSocketFactory(X509TrustManager trustManager) {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
//            sslContext = SSLContext.getInstance("SSL");
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        TrustManager[] trustManagers = {};
        KeyManager[] keyManagers = null;
        InputStream clientStream = null;
        SSLSocketFactory sslSocketFactory = null;
        if (trustManager != null) {
            trustManagers = new TrustManager[] { trustManager };
        }

        clientStream = httpsData.getClientCertStream();
        if (clientStream != null) {
            needClientCert = true;
            keyManagers = getClientKeyManagers(clientStream);
        } else {
            needClientCert = false;
        }

        try {
            sslContext.init(keyManagers, trustManagers, null);
//            sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        StreamUtil.closeStream(clientStream);
        return sslSocketFactory;
    }

    /**
     * 是否需要认证服务器
     */
    private boolean isNeedTrustCert(InputStream[] inputStreams) {
        return inputStreams.length > 0;
    }

    private X509TrustManager trustManagerForCertificates(InputStream[] inputStreams)
            throws GeneralSecurityException {

        // 生成一个不认证的TrustManager
        if (EmptyUtil.isEmpty(inputStreams)) {
            return new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            };
        }

        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<Certificate> certificates = new ArrayList<>();
        for (InputStream in : inputStreams) {
            certificates.addAll(certificateFactory.generateCertificates(in));
        }
        if (certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        // Put the certificates a key store.
        char[] password = "password".toCharArray(); // Any password will work.
        KeyStore keyStore = newEmptyKeyStore(password);
        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }

    private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // By convention, 'null' creates an empty key store.
            InputStream in = null;
            keyStore.load(in, password);
            return keyStore;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private KeyManager[] getClientKeyManagers(InputStream clientStream) {

        try {
            String p = httpsData.getClientCertPsw();
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(clientStream, p.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, p.toCharArray());
            return keyManagerFactory.getKeyManagers();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CertificateException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnrecoverableKeyException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
