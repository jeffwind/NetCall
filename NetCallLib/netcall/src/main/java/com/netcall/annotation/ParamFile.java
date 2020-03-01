package com.netcall.annotation;

import com.netcall.core.NetCallConstant;

/**
 * 提交文件
 */
public @interface ParamFile {
    String mediaType() default NetCallConstant.MEDIA_TEXT_PLAIN;
}
