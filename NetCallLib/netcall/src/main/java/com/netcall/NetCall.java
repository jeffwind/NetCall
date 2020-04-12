package com.netcall;

import android.content.Context;
import android.util.Log;

import com.netcall.cache.CacheAutoDelete;
import com.netcall.core.NetCallThreadDo;
import com.netcall.util.EmptyUtil;
import com.netcall.util.JsonSp;

/**
 * 网络设置
 */
public class NetCall {

    private static Context sAppContext;
    private static boolean sHttpsAuthEnable = true;
    private static boolean sNeedLog;
    private static boolean sNeedCacheLog;
    private static String sLogTag = "NetCall";
    private static String sBaseUrlDefault;
    private static String sCacheRootPath;
    private static int sConnectTimeout = -1;
    private static int sReadTimeout = -1;

    public static void init(Context context) {
        sAppContext = context.getApplicationContext();
        if (EmptyUtil.isEmpty(sCacheRootPath)) {
            setCacheRootPath(sAppContext.getCacheDir() + "/netcall/");
        }
    }

    public static Context getContext() {
        return sAppContext;
    }

    /**
     * 设置https认证是否启用
     */
    public static void setHttpsAuthEnable(boolean enable) {
        sHttpsAuthEnable = enable;
    }

    public static boolean isHttpsAuthEnable() {
        return sHttpsAuthEnable;
    }

    public static String getLogTag() {
        return sLogTag;
    }

    public static void setLogTag(String logTag) {
        NetCall.sLogTag = logTag;
    }

    public static boolean isNeedLog() {
        return sNeedLog;
    }

    public static void setNeedLog(boolean needLog) {
        NetCall.sNeedLog = needLog;
    }

    public static void setNeedLogCache(boolean need) {
        sNeedCacheLog = need;
    }

    public static boolean isNeedCacheLog() {
        return sNeedCacheLog;
    }

    /**
     * 设置线程池
     * @param coreThreadSize 核心线程数量
     * @param maxThreadSize 最大线程数量
     * @param aliveTime 非核心线程空闲时存活时间
     * @param queueCapacity 任务队列容量
     */
    public static void setThreadPool(int coreThreadSize, int maxThreadSize, int aliveTime, int queueCapacity) {
        NetCallThreadDo.init(coreThreadSize, maxThreadSize, aliveTime, queueCapacity);
    }

    public static void setDefaultBaseUrl(String baseUrl) {
        sBaseUrlDefault = baseUrl;
    }

    public static String getDefaultBaseUrl() {
        return sBaseUrlDefault;
    }

    public static void setCacheRootPath(String path) {
        if (EmptyUtil.isEmpty(path)) {
            throw new IllegalStateException("path is empty");
        }
        sCacheRootPath = path;
        if (!path.endsWith("/")) {
            sCacheRootPath = sCacheRootPath + "/";
        }
        JsonSp.setRootPath(sCacheRootPath);
        logCache("set root path " + sCacheRootPath);
    }

    public static String getCacheRootPath() {
        return sCacheRootPath;
    }

    /**
     * 全局设置连接超时时间，单位秒
     */
    public static void setDefaultConnectTimeout(int timeout) {
        sConnectTimeout = timeout;
    }

    public static int getConnectTimeout() {
        return sConnectTimeout;
    }

    /**
     * 全局设置读超时时间，单位秒
     */
    public static void setDefaultReadTimeout(int timeout) {
        sReadTimeout = timeout;
    }

    public static int getReadTimeout() {
        return sReadTimeout;
    }

    /**
     * 清除所有过期的Cache
     */
    public static void clearExpriredCache() {
        CacheAutoDelete.start();
    }

    public static void logError(Throwable throwable) {
        if (throwable.getMessage() != null) {
            logAlways("Error: " + throwable.getMessage());
        } else if (throwable.getStackTrace().length > 0) {
            logAlways("Error: " + throwable.getStackTrace()[0].toString());
        }
    }

    public static void logAlways(String text) {
        Log.e(getLogTag(), text);
    }

    public static void log(String text) {
        if (isNeedLog()) {
            Log.e(getLogTag(), text);
        }
    }

    public static void logCache(String text) {
        if (isNeedLog() && isNeedCacheLog()) {
            Log.e(getLogTag(), "Cache " + text);
        }
    }
}
