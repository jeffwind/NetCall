package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamString;
import com.netcall.core.NetCallConstant;
import com.netcall.test.bean.BeanTest;

@CallPost(value = "rest/str", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTestString extends BaseCall {

    @ParamString(mediaType = NetCallConstant.MEDIA_APPLICATION_JSON)
    private String data;

    public CallTestString(String data) {
        this.data = data;
    }
}
