package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallCache;
import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallHttps;
import com.netcall.annotation.CallResp;
import com.netcall.annotation.ParamCache;
import com.netcall.annotation.ParamQuery;
import com.netcall.test.bean.BeanTest;

/**
 * 具有缓存机制
 */
@CallGet(value = "rest/greeting", baseUrl = "https://192.168.31.159:8443/")
@CallResp(value = BeanTest.class)
@CallCache(cacheOnlyTime = -1, deleteTime = 10)
@CallHttps(trustCrtPaths = {"server.crt"}, clientBksPath = "client.bks", clientBksPsw = "123456")
public class CallTestCache extends BaseCall {

    @ParamQuery("name")
    @ParamCache("name")
    private String id;

    @ParamCache
    private int type;

    public CallTestCache(String name, int type) {
        this.id = name;
        this.type = type;
    }
}
