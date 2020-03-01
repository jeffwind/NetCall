package com.netcall.annotation;

import com.netcall.core.NetCallConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 作为post报文头String形式发送
 * 不能同时与{@link ParamForm}和{@link ParamFile}使用
 * 一个Call类中只能使用有第一个{@link ParamString}有效
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamString {
    String mediaType() default NetCallConstant.MEDIA_TEXT_PLAIN;
}
