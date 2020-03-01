package com.netcall.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于缓存的参数，此批注相当于数据库中的关键字。
 * 对于两个Call，他们所有被这个注解修饰的变量值都相同的话，两个Call将被视为完全相同网络请求，将获取到同一份缓存数据。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamCache {
    /** cache名字 */
    String value() default "";

//    // 分页第一种方式，通过第几页和每页多少个item获取数据
//    boolean pageIndex() default false;
//    boolean pageSize() default false;
//    // 分页第二种方式，通过item的开始index和结束index获取数据
//    boolean startIndex() default false;
//    boolean endIndex() default false;
}
