package com.netcall.cache;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.netcall.BaseCall;
import com.netcall.NetCall;
import com.netcall.annotation.CallCache;
import com.netcall.util.JsonSp;

/**
 * 保存缓存信息的文件
 * 每一个url前缀会保存一份文件
 */
public class CacheMsgFile {

    public static final String FILE_NAME = "msg.txt";

    private static final String SP_URL = "url";
    private static final String SP_CALL = "call";

    /**
     * 保存url以及BaseCall信息到parentPath文件夹，会在该文件夹内生成一个文件用于保存信息
     */
    public static boolean saveMsgFile(String parentPath, String url, BaseCall call) {
        if (TextUtils.isEmpty(parentPath)) {
            NetCall.logAlways("Error saveMsgFile parentPath is empty");
            return false;
        }

        SharedPreferences sp = JsonSp.getSp(parentPath, FILE_NAME);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SP_URL, url);
        editor.putString(SP_CALL, call.getClass().getName());
        return editor.commit();

    }

    public static Class<? extends BaseCall> getCall(String parentPath) {

        String callName = null;
        try {

            SharedPreferences sp = JsonSp.getSp(parentPath, FILE_NAME);
            callName = sp.getString(SP_CALL, null);
            Class<? extends BaseCall> clazz = (Class<? extends BaseCall>)Class.forName(callName);
//            Class<? extends BaseCall> clazz = (Class<? extends BaseCall>)ClassLoader.getSystemClassLoader().loadClass(callName);
            return clazz;
        } catch (Exception e) {
            e.printStackTrace();
            NetCall.logCache("can not load Class " + callName + " of path [" + parentPath + "/"
                    + FILE_NAME + "]");
            NetCall.logError(e);
        }
        return null;
    }

    public static int getDeleteTime(String parentPath) {
        Class<? extends BaseCall> callClass = getCall(parentPath);
        if (callClass == null) {
            return -1;
        }
        CallCache callCache = callClass.getAnnotation(CallCache.class);
        if (callCache == null) {
            NetCall.logAlways("Error CacheMsgFile getDeleteTime CallCache is null!");
            return -1;
        }
        return callCache.deleteTime();
    }
}
