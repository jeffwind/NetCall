package com.netcall.test.call;

import com.netcall.BaseCall;
import com.netcall.annotation.CallGet;
import com.netcall.annotation.CallHttps;
import com.netcall.annotation.CallResp;
import com.netcall.test.bean.BeanTest;

@CallGet(value = "rest/https", baseUrl = "https://192.168.31.159:8443/")
@CallResp(BeanTest.class)
@CallHttps(trustCrtPaths = {"server.crt"}, clientBksPath = "client.bks", clientBksPsw = "123456")
public class CallTestHttps extends BaseCall {

}
