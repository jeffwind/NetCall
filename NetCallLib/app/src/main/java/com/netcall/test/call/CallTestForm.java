package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallPost;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamForm;
import com.netcall.test.bean.BeanTest;

@CallPost(value = "rest/form", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTestForm extends BaseCall {

    @ParamForm
    private long id;

    @ParamForm(value = "name", encoded = false)
    private String nickName;

    public CallTestForm(long id, String name) {
        this.id = id;
        this.nickName = name;
    }
}
