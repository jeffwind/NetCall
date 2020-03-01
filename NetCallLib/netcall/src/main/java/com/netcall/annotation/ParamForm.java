package com.netcall.annotation;


import com.netcall.core.NetCallConstant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 发送的参数。
 * 将作为报文表单内容发送，只用于post发送方式。
 * 参数名value不填则用变量名作为参数名
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamForm {
    // key值
    String value() default "";
    boolean encoded() default NetCallConstant.PARAM_ENCODED;
}
