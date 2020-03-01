package com.netcall.annotation;

import com.netcall.core.NetCallConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 发送的参数。
 * 将体现在Url中，如http://localhost:8080?param=value
 * 参数名value不填则用变量名作为参数名
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamQuery {
    String value() default "";
    boolean encoded() default NetCallConstant.PARAM_ENCODED;
}
