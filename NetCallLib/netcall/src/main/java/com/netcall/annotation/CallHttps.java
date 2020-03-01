package com.netcall.annotation;

import com.netcall.https.HostnameVerifierDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.net.ssl.HostnameVerifier;

/**
 * 注册https认证信息。
 * 普通https连接不需要该注解,只有单向、双向认证才需要添加该注解。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CallHttps {

    /**  认证文件从哪里取 */
    E_PATH pathType() default E_PATH.ASSETS;

    /**
     * <font color="orange">服务器CRT证书路径</font>
     * 用于单、双向认证，作用是客户端认证服务器，如果为空表示不认证服务器，只使用https加密。
     */
    String[] trustCrtPaths() default {};

    /**
     * <font color="orange">客户端BKS证书路径</font>
     * 用于双向认证，作用是服务器认证客户端。
     */
    String clientBksPath() default "";

    /**
     * <font color="orange">客户端BKS证书密码</font>
     * 用于双向认证，作用是服务器认证客户端
     */
    String clientBksPsw() default "";

    Class<? extends HostnameVerifier> hostnameVerifier() default HostnameVerifierDef.class;

    enum E_PATH {
        ASSETS, FILE
    }
    /** 路径为assets文件 */
    int PATH_ASSETS = 0;
    /** 文件路径为文件系统的文件 */
    int PATH_FILE = 1;

}
