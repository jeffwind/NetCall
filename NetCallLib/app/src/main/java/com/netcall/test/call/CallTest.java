package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamQuery;
import com.netcall.test.bean.BeanTest;

//@CallGet(value = "rest/greeting", baseUrl = "http://192.168.31.159:8080/")
@CallGet(value = "rest/greeting", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
public class CallTest extends BaseCall {

    @ParamQuery("name")
    private String id;

    public CallTest(String name) {
        this.id = name;
    }
}
