package com.netcall.https.backup;

import android.content.Context;

import com.netcall.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class HttpsUtil2 {

    public static final int TYPE_HTTPS_NONE = 0;
    // https单向认证
    public static final int TYPE_HTTPS_ONE_AUTH = 1;
    // https双向认证
    public static final int TYPE_HTTPS_BOTH_AUTH = 2;

    private static final String SERVER_CRT = "yxpserver.crt";
    public static final String CLIENT_BKS = "yxpclient.bks";
    private static String P = "youxuepai";
    private static String[] HOST_NAMES = new String[]{"noahedu.com", "youxuepai.com"};

    // 是否正在使用测试服务器
    private static boolean isTestServer = false;
    private static String TEST_HOST_NAME = "192.168.";

    private Context context;
    private int type = TYPE_HTTPS_BOTH_AUTH;

//    private void a() {
//        final TrustManager[] trustAllCerts = new TrustManager[] {
//                new X509TrustManager() {
//                    @Override
//                    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
//                    }
//
//                    @Override
//                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//                        return new java.security.cert.X509Certificate[]{};
//                    }
//                }
//        };
//
//        // Install the all-trusting trust manager
//        final SSLContext sslContext = SSLContext.getInstance("SSL");
//        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
//        // Create an ssl socket factory with our all-trusting manager
//        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//
//        OkHttpClient.Builder builder = new OkHttpClient.Builder();
//        builder.sslSocketFactory(sslSocketFactory);
//        builder.hostnameVerifier(new HostnameVerifier() {
//            @Override
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        });
//
//        OkHttpClient okHttpClient = builder.build();
//
//    }

    public HttpsUtil2(Context context) {
        this.context = context;
    }

    public HttpsUtil2(Context context, int type) {
        this.context = context;
        this.type = type;
    }

    public X509TrustManager newTrustManager() {

        X509TrustManager trustManager = null;
        InputStream trustStream = null;
        try {
            trustStream = getCertificatesInputStream();
            trustManager = trustManagerForCertificates(trustStream);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } finally {
            StreamUtil.closeStream(trustStream);
        }
        return trustManager;
    }

    public SSLSocketFactory newSslSocketFactory(X509TrustManager trustManager) {

        if (type == TYPE_HTTPS_ONE_AUTH || type == TYPE_HTTPS_BOTH_AUTH) {
            SSLSocketFactory sslSocketFactory = null;
            InputStream clientStream = null;
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");

                TrustManager[] trustManagers = null;
                KeyManager[] keyManagers = null;
                if (trustManager != null) {
                    trustManagers = new TrustManager[] { trustManager };
                }

                if (type == TYPE_HTTPS_BOTH_AUTH) {
                    clientStream = getClientInputStream();
                    keyManagers = getClientKeyManagers(clientStream);
                }
                sslContext.init(keyManagers, trustManagers, null);
                sslSocketFactory = sslContext.getSocketFactory();
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            } finally {
                StreamUtil.closeStream(clientStream);
            }

            return sslSocketFactory;
        }
        return null;
    }

    private InputStream getCertificatesInputStream() {
//            // 使用xca生成的证书
//            String[] certs = CertGetter.getCrt();
//            Buffer buffer = new Buffer();
//            for (String cert : certs) {
//                buffer.writeUtf8(cert);
//            }
//            return buffer.inputStream();
        try {
            InputStream input = context.getResources().getAssets().open(SERVER_CRT);
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private InputStream getClientInputStream() {
        try {
            InputStream input = context.getResources().getAssets().open(CLIENT_BKS);
            return input;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private X509TrustManager trustManagerForCertificates(InputStream in)
            throws GeneralSecurityException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
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
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(clientStream, P.toCharArray());
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, P.toCharArray());
            return keyManagerFactory.getKeyManagers();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class MyHostnameVerifier implements HostnameVerifier {

        private String[] hostNames = HOST_NAMES;

        public MyHostnameVerifier() {
        }

        public MyHostnameVerifier(String[] hostNames) {
            if (hostNames != null && hostNames.length > 0) {
                this.hostNames = hostNames;
            }
        }

        @Override
        public boolean verify(String hostname, SSLSession session) {
            if (hostname == null) {
                return true;
            }
            for (String itemName : hostNames) {
                if (hostname.contains(itemName)) {
                    return true;
                }
            }
            if (isTestServer) {
                if (hostname.contains(TEST_HOST_NAME)) {
                    return true;
                }
            }
            return false;
        }
    }
}
