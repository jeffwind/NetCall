package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamForm;
import com.netcall.annotation.ParamQuery;
import com.netcall.test.bean.BeanTest;

@CallPost(value = "rest/post", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTestPost extends BaseCall {

    @ParamQuery
    private long id;

    @ParamForm(value = "name")
    private String nickName;

    public CallTestPost(long id, String name) {
        this.id = id;
        this.nickName = name;
    }
}
