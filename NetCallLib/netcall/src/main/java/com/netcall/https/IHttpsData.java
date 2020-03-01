package com.netcall.https;

import java.io.InputStream;

import javax.net.ssl.HostnameVerifier;

/**
 * https信息
 */
public interface IHttpsData {

    /** 域名过滤 */
    HostnameVerifier getHostNameVerifier();

    /** 返回服务器认证证书，用作客户端认证服务器，可为null或空数组 */
    InputStream[] getTrustCertStream();

    /** 返回客户端认证证书，用作服务器认证客户端，可为null */
    InputStream getClientCertStream();

    /** 返回客户端认证密码，用作服务器认证客户端，可为null */
    String getClientCertPsw();

}
