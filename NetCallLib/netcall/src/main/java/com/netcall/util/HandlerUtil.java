package com.netcall.util;

import android.os.Handler;
import android.os.Looper;

public class HandlerUtil {

    private static Handler sMainHandler;

    public static boolean isMain() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static Handler getMain() {
        if (sMainHandler == null) {
            synchronized (HandlerUtil.class) {
                sMainHandler = new Handler(Looper.getMainLooper());
            }
        }
        return sMainHandler;
    }

}
