package com.netcall.annotation;

import com.netcall.Callback;
import com.netcall.BaseCall;
import com.netcall.Response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link BaseCall}添加该注解后将使用缓存。
 * 非必须。
 * 当通过{@link BaseCall#call(Callback)}发起网络请求，首先会从缓存获取数据通过
 * {@link Callback#onResp(Response)}返回数据。如果缓存超时，则重新从服务器获取数据，
 * 如果服务器数据与缓存不一致，则再一次调用{@link Callback#onResp(Response)}返回最新数据。
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CallCache {
    /**
     * 缓存有效时长，单位为秒。
     * 在有效期内如果获取到缓存数据，不再从网络请求数据。
     *
     * 如果缓存过期，将通过{@link Callback#onResp(Response)}首先返回旧的缓存数据，然后重新请求服务器
     * 获取最新数据，如果最新的数据相较缓存有变化，会再一次返回最新的数据。
     * 如果缓存过期，会将最新的数据保存到缓存，并重新更新过期时间。
     *
     * 如果超时时长为-1，则返回旧的缓存数据后，每次都重新请求服务器，最新数据与缓存数据有差异，
     * 将再一次返回最新数据。
     *
     * 只要从服务器获取数据成功，都会更新最近修改时间。
     */
    int cacheOnlyTime() default -1;

    /**
     * 超时时间，如果 现在时间-最近修改时间>该超时时间，缓存清理机制会将这个缓存删除。
     * 单位为秒。
     * 默认一个月，值为-1时不清除。
     *
     * 只要从服务器获取数据成功，都会更新最近修改时间。
     */
    int deleteTime() default 30 * 24 * 3600;
}
