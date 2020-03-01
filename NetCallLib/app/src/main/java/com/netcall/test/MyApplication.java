package com.netcall.test;

import android.app.Application;

import com.netcall.NetCall;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NetCall.setNeedLog(true);
        NetCall.setNeedLogCache(true);
        NetCall.setCacheRootPath(getExternalCacheDir().getPath());
    }
}
