package com.netcall;

/**
 * 获取数据返回的数据
 */
public class Response {

    private BaseCall call;
    /** 网络状态码 */
    private int statusCode;
    /** 本次是否是获取网络数据后返回的内容 */
    private boolean isAfterNet;
    /** 本次是否获取数据成功 */
    private boolean isSuccess;
    /** 返回的数据 */
    private String respStr;
    /** 返回的数据类 */
    private IBean bean;
    /** 异常信息 */
    private Exception exception;
    /** 是否已经缓存已经成功返回数据 */
    private boolean isCacheSuccess;
    /** 是否只使用缓存，false才需要获取网络数据，该值由CallCache.cacheOnlyTime()决定 */
    private boolean isCacheOnly;
    /** 是否缓存与服务器数据相同 */
    private boolean isCacheEqual;

    public Response(BaseCall call) {
        setCall(call);
        refreshSuccess();
    }

    public BaseCall getCall() {
        return call;
    }

    public void setCall(BaseCall call) {
        this.call = call;
    }

    public boolean isAfterNet() {
        return isAfterNet;
    }

    public void setAfterNet(boolean afterNet) {
        isAfterNet = afterNet;
        refreshSuccess();
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public String getRespStr() {
        return respStr;
    }

    public void setRespStr(String respStr) {
        this.respStr = respStr;
    }

    public IBean getBean() {
        return bean;
    }

    public void setBean(IBean bean) {
        this.bean = bean;
        refreshSuccess();
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
        refreshSuccess();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        refreshSuccess();
    }

    public boolean isCacheSuccess() {
        return isCacheSuccess;
    }

    public void setCacheSuccess(boolean cacheSuccess) {
        isCacheSuccess = cacheSuccess;
    }

    public boolean isCacheOnly() {
        return isCacheOnly;
    }

    public void setCacheOnly(boolean cacheOnly) {
        isCacheOnly = cacheOnly;
    }

    public boolean isCacheEqual() {
        return isCacheEqual;
    }

    public void setCacheEqual(boolean cacheEqual) {
        isCacheEqual = cacheEqual;
    }

    private void refreshSuccess() {

        if (bean == null) {
            isSuccess = false;
        } else if (exception != null) {
            isSuccess = false;
        } else if (isAfterNet) {
            isSuccess = statusCode >= 200 || statusCode < 300;
        } else {
            isSuccess = true;
        }
    }
}
