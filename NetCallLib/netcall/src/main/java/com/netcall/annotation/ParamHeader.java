package com.netcall.annotation;

import com.netcall.core.NetCallConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 发送的参数。
 * 将体现在发送的header中
 * 参数名value不填则用变量名作为参数名
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamHeader {
    String value();
    boolean encoded() default NetCallConstant.PARAM_ENCODED;
}
