package com.netcall.core;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.netcall.BaseCall;
import com.netcall.IBean;
import com.netcall.NetCall;
import com.netcall.Response;
import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamFile;
import com.netcall.annotation.ParamForm;
import com.netcall.annotation.ParamHeader;
import com.netcall.annotation.ParamQuery;
import com.netcall.annotation.ParamString;
import com.netcall.cache.NetCallCache;
import com.netcall.https.IHttpsData;
import com.netcall.https.NetCallHttps;
import com.netcall.util.EmptyUtil;
import com.netcall.util.ReflectUtil;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * BaseCall处理者
 */
public class NetCallProc {

    private static final Class<? extends Annotation>[] CALL_METHODS = new Class[]{CallGet.class, CallPost.class};
    private BaseCall call;
    private NetCallCache netCallCache;
    private NetCallHttps netCallHttps;

    public NetCallProc(BaseCall call) {
        this.call = call;
        netCallCache = new NetCallCache(call);
        netCallHttps = new NetCallHttps(call);
    }

    public String getBaseUrl() {
        String baseUrl = null;
        Class<? extends BaseCall> clazz = call.getClass();
        if (clazz.isAnnotationPresent(CallGet.class)) {
            CallGet callGet = clazz.getAnnotation(CallGet.class);
            baseUrl = callGet.baseUrl();
        } else if (clazz.isAnnotationPresent(CallPost.class)) {
            CallPost callGet = clazz.getAnnotation(CallPost.class);
            baseUrl = callGet.baseUrl();
        }
        if (!EmptyUtil.isEmpty(baseUrl)) {
            return baseUrl;
        }
        baseUrl = NetCall.getDefaultBaseUrl();
        if (!EmptyUtil.isEmpty(baseUrl)) {
            return baseUrl;
        }
        throw new IllegalStateException("base url is null! You can set a default baseUrl by NetCall.setDefaultBaseUrl(String).");
    }

    public String getSuffixUrl() {
        String suffixUrl;
        Class<? extends BaseCall> clazz = call.getClass();
        if (clazz.isAnnotationPresent(CallGet.class)) {
            CallGet callGet = clazz.getAnnotation(CallGet.class);
            suffixUrl = callGet.value();
        } else if (clazz.isAnnotationPresent(CallPost.class)) {
            CallPost callGet = clazz.getAnnotation(CallPost.class);
            suffixUrl = callGet.baseUrl();
        } else {
            throw new IllegalStateException("There is no CallGet or CallPost in " + clazz.getSimpleName());
        }
        return suffixUrl;
    }

    public Annotation getCallMethod() {

        for (Class<? extends Annotation> clazz : CALL_METHODS) {
            if (call.getClass().isAnnotationPresent(clazz)) {
                Annotation callMethod = call.getClass().getAnnotation(clazz);
                return callMethod;
            }
        }

        throw new IllegalStateException("There is no CallMethod Annotation defined in Call!");
    }

    public Params createUrlMap() {

        Params params = new Params();
        List<Field> fieldList = ReflectUtil.findFieldsByAnno(call.getClass(), ParamQuery.class);
        for (Field field : fieldList) {
            ParamQuery param = field.getAnnotation(ParamQuery.class);
            String name = TextUtils.isEmpty(param.value()) ? field.getName() : param.value();
            String value = ReflectUtil.getFieldString(call, field);
            boolean encoded = param.encoded();
            params.add(new Param(name, value, encoded));
        }
        return params;
    }

    public Params createFormMap() {

        Params params = new Params();
        List<Field> fieldList = ReflectUtil.findFieldsByAnno(call.getClass(), ParamForm.class);
        for (Field field : fieldList) {
            ParamForm param = field.getAnnotation(ParamForm.class);
            String name = TextUtils.isEmpty(param.value()) ? field.getName() : param.value();
            String value = ReflectUtil.getFieldString(call, field);
            boolean encoded = param.encoded();
            params.add(new Param(name, value, encoded));
        }
        return params;
    }

    public Params createHeaderMap() {

        Params params = new Params();
        List<Field> fieldList = ReflectUtil.findFieldsByAnno(call.getClass(), ParamHeader.class);
        for (Field field : fieldList) {
            ParamHeader param = field.getAnnotation(ParamHeader.class);
            String name = TextUtils.isEmpty(param.value()) ? field.getName() : param.value();
            String value = ReflectUtil.getFieldString(call, field);
            boolean encoded = param.encoded();
            params.add(new Param(name, value, encoded));
        }
        return params;
    }

    /**
     * 获取post的String参数，下标0代表mediaType，下标1代表发送的String内容
     */
    public String[] createParamStr() {
        Field field = ReflectUtil.findFieldByAnno(call.getClass(), ParamString.class);
        if (field == null) {
            throw new IllegalStateException("there is no ParamString in " + call.getClass().getSimpleName());
        }
        ParamString param = field.getAnnotation(ParamString.class);
        String mediaType = param.mediaType();
        String text = ReflectUtil.getFieldString(call, field);

        return new String[]{mediaType, text};
    }

    public String[] createParamFile() {

        Field field = ReflectUtil.findFieldByAnno(call.getClass(), ParamFile.class);
        if (field == null) {
            throw new IllegalStateException("there is no ParamFile in " + call.getClass().getSimpleName());
        }
        ParamFile param = field.getAnnotation(ParamFile.class);
        String mediaType = param.mediaType();
        String text = ReflectUtil.getFieldString(call, field);

        return new String[]{mediaType, text};
    }

    public Response createResponseFromNet(okhttp3.Response response, Exception ex) {

        Response result = new Response(call);
        result.setAfterNet(true);
        result.setBean(null);
        if (ex != null) {
            ex.printStackTrace();
            result.setException(ex);
        } else if (response == null) {
            result.setException(new IllegalStateException("response is null"));
        } else if (!response.isSuccessful()) {
            result.setStatusCode(response.code());
            result.setException(new IllegalStateException("code is " + response.code()));
        } else {
            result.setStatusCode(response.code());
            CallResp callResp = call.getClass().getAnnotation(CallResp.class);
            if (callResp == null) {
                throw new IllegalStateException("There should be a CallResp annotation in class "
                        + call.getClass().getSimpleName());
            }

            Class<? extends IBean> beanClass = callResp.value();
            try {
                result.setRespStr(response.body().string());
                // 跟缓存数据比较，如果跟缓存数据一致，直接取缓存数据，不需要再次生成Bean
                boolean needCompareBean = netCallCache.getCacheCompare().needCompareBean();
                if (needCompareBean) {

                    IBean bean = new Gson().fromJson(result.getRespStr(), beanClass);
                    result.setBean(bean);
                }

                Response cacheResponse = getCacheResult();
                boolean isEqualCache = netCallCache.isEqualCache(cacheResponse, result);
                result.setCacheEqual(isEqualCache);
                result.setCacheSuccess(cacheResponse == null ? false : cacheResponse.isSuccess());

                if (!needCompareBean) {

                    if (isEqualCache && result.getBean() == null && cacheResponse != null) {
                        result.setBean(cacheResponse.getBean());
                    } else {
                        IBean bean = new Gson().fromJson(result.getRespStr(), beanClass);
                        result.setBean(bean);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.setException(e);
            }
        }
        return result;
    }

    public Response getCacheResult() {
        return netCallCache.getCacheResponse();
    }

    public boolean saveResponse(Response response) {
        return netCallCache.saveCache(response);
    }

    public void toHttpsIfNeed(SendData sendData, OkHttpClient.Builder clientBuilder) {
        if (sendData.isNeedHttps()) {
            netCallHttps.setHttpClient(clientBuilder);
        }
    }

    public void setHttpsData(IHttpsData httpsData) {
        netCallHttps.setHttpData(httpsData);
    }

    public void printSendLog(Request request) {
        String url = request.url().uri().toString();
        NetCall.log("URL " + url);
        if (SendData.METHOD_GET.toLowerCase().equals(request.method().toLowerCase())) {
            // TODO
        } else {
        }
    }

    public void printRespLog(Response response) {
        if (!NetCall.isNeedLog()) {
            return;
        }
        if (response.getException() != null) {
            NetCall.log("Net Resp [" + response.getException().getMessage() + "]");
        } else {
            NetCall.log("Net Resp [" + response.getRespStr() + "]");
        }
    }

}
