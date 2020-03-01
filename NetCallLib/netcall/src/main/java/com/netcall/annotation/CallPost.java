package com.netcall.annotation;

import com.netcall.BaseCall;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.netcall.annotation.CallPost.EType.FORM;

/**
 * {@link com.netcall.BaseCall}添加该注解后将使用post方法进行数据发送
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CallPost {

    /** baseUrl，也可通过{@link BaseCall#onBaseUrl()}设置 */
    String baseUrl() default "";
    /** url后缀 */
    String value();
    EType type() default FORM;

    enum EType {
        /** post 使用Map<key, value>方式上传 */
        FORM,
        /** post 使用文件流方式上传 */
        FILE,
        /** post 使用String方式上传 */
        STRING
    }
}
