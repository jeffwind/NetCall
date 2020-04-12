package com.netcall;

import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamFile;
import com.netcall.annotation.ParamForm;
import com.netcall.annotation.ParamHeader;
import com.netcall.annotation.ParamQuery;
import com.netcall.annotation.ParamString;
import com.netcall.core.NetCallProc;
import com.netcall.core.NetCallThreadDo;
import com.netcall.core.Params;
import com.netcall.core.SendData;
import com.netcall.https.IHttpsData;
import com.netcall.util.HandlerUtil;

import java.io.File;
import java.lang.annotation.Annotation;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 网络请求主类。
 *
 * 必须要有的【类注解】有{@link CallResp}
 * 以及{@link CallGet}或{@link CallPost}其中一个
 *
 * 可选的【参数注解】有{@link ParamQuery}、{@link ParamForm}、{@link ParamHeader}、
 * {@link ParamString}、{@link ParamFile}
 */
public abstract class BaseCall {

    private NetCallProc callProc = new NetCallProc(this);

    private Callback callback;
    private BaseCall nextCall;

    private SendData sendData = new SendData();

    public void call() {
        call(null);
    }

    public void call(Callback callback) {

        setCallback(callback);

        sendData = newSendData();
        onInterceptCall(sendData);

        NetCallThreadDo.execute(createExecuteRunnable());
    }

    /**
     * 从缓存中获取数据
     */
    public IBean callCache() {
        sendData = newSendData();
        onInterceptCall(sendData);

        Response result = callProc.getCacheResult();
        if (result == null) {
            return null;
        }
        return result.getBean();
    }

    /** 设置本Call完毕后，下一个要执行的Call */
    public void nextCall(BaseCall call) {
        nextCall = call;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public SendData getSendData() {
        return sendData;
    }

    /**
     * 重写https逻辑。
     * 默认会使用{@link com.netcall.https.HttpsDataDef}，如果只是稍微修改部分逻辑，可以重写该类。
     */
    public void setHttpsData(IHttpsData httpsData) {
        callProc.setHttpsData(httpsData);
    }

    /**
     * 设置超时时间，单位为秒
     */
    public void setConnectTimeout(int timeout) {
        callProc.setConnectTimeout(timeout);
    }

    /**
     * 设置读超时时间，单位为秒
     */
    public void setReadTimeout(int timeout) {
        callProc.setReadTimeout(timeout);
    }

    /** 获取baseUrl */
    protected String onBaseUrl() {
        return callProc.getBaseUrl();
    }

    /** 获取Url后缀 */
    protected String onSuffixUrl() {
        return callProc.getSuffixUrl();
    }

    /** 获取url中?后面的键值对 */
    protected Params onParamQuery() {
        return callProc.createUrlMap();
    }

    /** 获取post报文的键值对 */
    protected Params onParamForm() {
        return callProc.createFormMap();
    }

    /** 获取报文头的键值对 */
    protected Params onParamHeader() {
        return callProc.createHeaderMap();
    }

    /** 获取post的String参数，下标0代表mediaType，下标1代表发送的String内容 */
    protected String[] onParamString() {
        return callProc.createParamStr();
    }

    /** 获取post的File流参数，下标0代表mediaType，下标1代表发送的文件路径 */
    protected String[] onParamFile() {
        return callProc.createParamFile();
    }

    /** 可重写该方法修改发送内容 */
    protected void onInterceptCall(SendData data) {
        // DO Nothing
    }

    /** 保存返回的数据，在辅助线程中执行 */
    protected boolean onSaveCache(Response response) {
        return callProc.saveResponse(response);
    }

    /** 可重写该方法构造ClientBuilder */
    protected OkHttpClient.Builder onHttpClientBuilder() {
        return callProc.createOkHttpBuilder(sendData);
    }

    /** 将okhttp3的Response转化为NetCall的结果数据类 */
    protected Response getResponse(okhttp3.Response response, Exception ex) {
        return callProc.createResponseFromNet(response, ex);
    }

    /** 将okhttp3的Response转化为NetCall的结果数据类 */
    protected void onResponse(Response response) {
        if (callback == null) {
            return;
        }
        callback.onResp(response);
    }

    /** 本BaseCall作为{@link #nextCall(BaseCall)}的参数的话，当轮到自己请求网络，会回调该方法 */
    protected void onNextCallTurn(BaseCall lastCall, Response lastCallResponse) {
    }

    /**
     * 生成新的SendData
     */
    public SendData newSendData() {
        SendData sendData = new SendData();
        sendData.setBaseUrl(onBaseUrl());
        sendData.setSuffixUrl(onSuffixUrl());
        sendData.setHeaderParams(onParamHeader());
        sendData.setQueryParams(onParamQuery());

        Annotation callMethod = callProc.getCallMethod();

        if (callMethod.getClass() == CallGet.class) {
            sendData.setMethod(SendData.METHOD_GET);
        } else if (callMethod.getClass() == CallPost.class) {
            CallPost callPost = (CallPost) callMethod;
            CallPost.EType postType = callPost.type();
            switch (postType) {
                case FILE: {
                    String[] paramFile = onParamFile();
                    sendData.setMediaType(paramFile[0]);
                    sendData.setPostFile(new File(paramFile[1]));
                    break;
                }
                case STRING: {
                    String[] paramStr = onParamString();
                    sendData.setMediaType(paramStr[0]);
                    sendData.setPostText(paramStr[1]);
                    break;
                }
                case FORM:
                default: {
                    sendData.setFormParams(onParamForm());
                    break;
                }
            }
        }
        return sendData;
    }

    private Runnable createExecuteRunnable() {
        return new Runnable() {

            @Override
            public void run() {

                Request request = sendData.toRequest();
                callProc.printSendLog(request);

                Response result = callProc.getCacheResult();
                invokeResultIfNeed(result);

                if (result != null) {
                    NetCall.logCache("Data [" + result.getRespStr() + "]");
                    if (result.isCacheOnly()) {
                        // 只使用缓存
                        invokeNextCall(result);
                        return;
                    }
                }

                try {
                    OkHttpClient okHttpClient = onHttpClientBuilder().build();
                    Call call = okHttpClient.newCall(request);
                    okhttp3.Response response = call.execute();
                    result = invokeResponse(response, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    invokeResponse(null, e);
                }
                invokeNextCall(result);
            }
        };
    }

    private Response invokeResponse(okhttp3.Response response, Exception ex) {

        final Response result = getResponse(response, ex);
        callProc.printRespLog(result);
        if (result.getRespStr() != null) {
            onSaveCache(result);
        }
        invokeResultIfNeed(result);
        return result;
    }

    private void invokeResultIfNeed(final Response response) {
        if (response == null) {
            return;
        }
        if (response.isAfterNet() && response.isCacheEqual()) {
            return;
        }
        if (HandlerUtil.isMain()) {
            onResponse(response);
        } else {
            HandlerUtil.getMain().post(new Runnable() {
                @Override
                public void run() {
                    onResponse(response);
                }
            });
        }
    }

    private void invokeNextCall(final Response response) {
        if (nextCall == null) {
            return;
        }

        Runnable nextRunnable = new Runnable() {
            @Override
            public void run() {
                nextCall.onNextCallTurn(BaseCall.this, response);
                nextCall.call();
            }
        };

        if (HandlerUtil.isMain()) {
            nextRunnable.run();
        } else {
            HandlerUtil.getMain().post(nextRunnable);
        }
    }
}
