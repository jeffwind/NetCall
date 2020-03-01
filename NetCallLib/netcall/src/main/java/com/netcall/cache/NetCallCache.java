package com.netcall.cache;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.netcall.BaseCall;
import com.netcall.IBean;
import com.netcall.NetCall;
import com.netcall.Response;
import com.netcall.annotation.CallCache;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamCache;
import com.netcall.core.Param;
import com.netcall.core.Params;
import com.netcall.core.SendData;
import com.netcall.util.FileUtil;
import com.netcall.util.LogUtil;
import com.netcall.util.ReflectUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 缓存机制处理者
 */
public class NetCallCache {

    public static final String SP_NAME = "net_cache";
    public static final String FILE_SUFFIX = ".json";

    private static final String NO_PARAM_NAME = "0" + FILE_SUFFIX;

    private BaseCall call;
    private ICacheCompare cacheCompare;

    private String url;
    private boolean cacheAble;
    private Response cacheResponse;
    private String path;

    public NetCallCache(BaseCall call) {
        this.call = call;
        setCacheCompare(ICacheCompare.Holder.getDefault());
        if (call == null || !call.getClass().isAnnotationPresent(CallCache.class)) {
            cacheAble = false;
        } else {
            cacheAble = true;
        }
    }

    public void setCacheCompare(ICacheCompare cacheCompare) {
        this.cacheCompare = cacheCompare;
    }

    public Response getCacheResponse() {
        return getCacheResult(true);
    }

    public Response getCacheResult(boolean initResultIfNull) {

        if (!cacheAble) {
            return null;
        }
        if (cacheResponse != null) {
            return cacheResponse;
        }
        if (!initResultIfNull) {
            return null;
        }
        cacheResponse = generateResultFromCache();
        return cacheResponse;
    }

    public boolean isEqualCache(Response cacheResponse, Response netResponse) {
        return cacheCompare.isEqual(cacheResponse, netResponse);
    }

    public ICacheCompare getCacheCompare() {
        return cacheCompare;
    }

    public boolean saveCache(Response response) {
        if (!cacheAble) {
            return false;
        }
        if (!response.isAfterNet()) {
            return false;
        }

        if (response.getRespStr() == null) {
            LogUtil.e("Response respStr is null!");
            return false;
        }
        initPathIfNeed();

        CacheMsgFile.saveMsgFile(new File(path).getParent(), url, call);
        if (response.isCacheEqual()) {
            NetCall.logCache("Saving [" + url + "] modify time [" + path + "]");
            // 与缓存相同，则直接更新文件最近修改日期，不再保存缓存
            return new File(path).setLastModified(System.currentTimeMillis());
        }

        NetCall.logCache("Saving [" + url + "] to [" + path + "]");
        return FileUtil.writeFile(path, response.getRespStr());
    }

    /**
     * 从缓存中读取Result
     * @return
     */
    private Response generateResultFromCache() {
        if (!cacheAble) {
            return null;
        }
        initPathIfNeed();
//        NetCall.logCache("Getting [" + url + "] from [" + path + "]");
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        CallResp callResp = call.getClass().getAnnotation(CallResp.class);
        Class<? extends IBean> beanClass = callResp.value();

        String respStr = FileUtil.readFile(file);
        IBean bean = new Gson().fromJson(respStr, beanClass);

        Response response = new Response(call);
        response.setAfterNet(false);
        response.setBean(bean);
        response.setRespStr(respStr);
        response.setCacheSuccess(true);

        // 是否只使用缓存
        CallCache callCache = call.getClass().getAnnotation(CallCache.class);
        int cacheOnlyTime = callCache.cacheOnlyTime() * 1000;
        if (cacheOnlyTime < 0) {
            response.setCacheOnly(false);
        } else {
            response.setCacheOnly(System.currentTimeMillis() < file.lastModified() + cacheOnlyTime);
        }

        CacheAutoDelete.startIfNeedDelay();
        return response;
    }

    /**
     * 获取保存的路径
     */
    private String initPathIfNeed() {

        if (path != null) {
            return path;
        }

        String rootPath = NetCall.getCacheRootPath();
        if (TextUtils.isEmpty(rootPath)) {
            throw new IllegalStateException("You should invoke NetCall.init(...) first or" +
                    " NetCall.setCacheRootPath(...) to set root path first!");
        }

        SendData sendData = call.getSendData();
        if (sendData == null) {
            // 还没有生成SendData则临时新建一个
            call.newSendData();
        }
        url = sendData.getUrlWithoutParam();
        if (TextUtils.isEmpty(url)) {
            throw new IllegalStateException("url is null!");
        }

        String parentPath = rootPath + url.hashCode() + "/";
        Params params = new Params();

        List<Field> fields = ReflectUtil.findFieldsByAnno(call.getClass(), ParamCache.class);
        if (fields.isEmpty()) {
            this.path = parentPath + NO_PARAM_NAME;
            return path;
        }

        for (int i = 0; i < fields.size(); i++) {

            Field field = fields.get(i);
            field.setAccessible(true);
            ParamCache paramCache = field.getAnnotation(ParamCache.class);

            String name = paramCache.value();
            if (TextUtils.isEmpty(name)) {
                name = field.getName();
            }
            String value = null;
            try {
                Object valueObj = field.get(call);
                if (valueObj != null) {
                    value = valueObj.toString();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

            params.add(new Param(name, value, false));
        }
        params.sortByName();
        String fileName = params.toParamString().hashCode() + FILE_SUFFIX;

        this.path = parentPath + fileName;
        return path;
    }
}
