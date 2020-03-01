package com.netcall.annotation;

import com.netcall.BaseCall;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link com.netcall.BaseCall}添加该注解后将使用get方法进行数据发送
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CallGet {

    /** baseUrl，也可通过{@link BaseCall#onBaseUrl()}设置 */
    String baseUrl() default "";
    /** url后缀 */
    String value();
}
