package com.netcall.annotation;

import com.netcall.IBean;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link com.netcall.BaseCall}使用该注解用于设置返回的数据类是什么。
 * 该注解为必须项。
 */
@Target({TYPE})
@Retention(RUNTIME)
@Inherited
public @interface CallResp {
    Class<? extends IBean> value();
}
