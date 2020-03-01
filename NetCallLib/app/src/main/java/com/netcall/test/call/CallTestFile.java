package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamFile;
import com.netcall.core.NetCallConstant;
import com.netcall.test.bean.BeanTest;

@CallPost(value = "rest/file", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTestFile extends BaseCall {

    @ParamFile(mediaType = NetCallConstant.MEDIA_IMG_JPG)
    private String path;

    public CallTestFile(String path) {
        this.path = path;
    }
}
