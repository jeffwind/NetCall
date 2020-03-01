package com.netcall.cache;

import android.annotation.SuppressLint;

import com.netcall.NetCall;
import com.netcall.core.NetCallThreadDo;
import com.netcall.util.FileUtil;
import com.netcall.util.JsonSp;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 自动清除缓存数据
 * 如果修改了{@link NetCall#setCacheRootPath(String)}则旧的根目录下的数据将不会被自动删除。
 */
public class CacheAutoDelete {

    /** 每隔8天检查一次 */
    private static final long AUTO_CHECK_GAP_TIME = 8 * 24 * 3600 * 1000;
    private static final String SP_LAST_CHECK_TIME = "last_delete_time";
    private static boolean sHasCheck;
    private static boolean sChecking;

    public static void startIfNeedDelay() {
        if (needCheck()) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        // 10秒后开始
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    CacheAutoDelete.start();
                }
            }.start();
        }
        sHasCheck = true;
    }

    public static void startIfNeed() {
        if (needCheck()) {
            start();
        }
        sHasCheck = true;
    }

    /**
     * 开始执行缓存清理线程
     */
    public static void start() {

        NetCallThreadDo.execute(new Runnable() {
            @SuppressLint("ApplySharedPref")
            @Override
            public void run() {
                NetCall.logCache("AutoDelete start!");
                sChecking = true;
                CacheAutoDelete.run();
                JsonSp.getSp(NetCallCache.SP_NAME).edit()
                        .putLong(SP_LAST_CHECK_TIME, System.currentTimeMillis()).commit();
                sChecking = false;
            }
        });
    }

    private static boolean needCheck() {
        if (sHasCheck) {
            return false;
        }
        if (sChecking) {
            return false;
        }
        long lastCheckTime = JsonSp.getSp(NetCallCache.SP_NAME).getLong(SP_LAST_CHECK_TIME, -1);

        if (lastCheckTime != -1 && System.currentTimeMillis() < lastCheckTime + AUTO_CHECK_GAP_TIME) {
            return false;
        }
        return true;
    }

    private static void run() {

        String rootPath = NetCall.getCacheRootPath();
        String[] dirPaths = new File(rootPath).list(new DirFilenameFilter());
        for (String dirName : dirPaths) {
            String dirPath = rootPath + dirName;
            NetCall.logCache("checking [" + dirPath + "]");
            long deleteTime = CacheMsgFile.getDeleteTime(dirPath);
            if (deleteTime < 0) {
                continue;
            }
            long deleteTimeBefore = System.currentTimeMillis() - deleteTime * 1000;
            File[] files = new File(dirPath).listFiles(new DataFilenameFilter());
            if (files == null) {
                continue;
            }
            for (File file : files) {
                if (file.lastModified() < deleteTimeBefore) {
                    FileUtil.delete(file);
                    NetCall.logCache("!!!delete [" + file.getPath() + "]!!!");
                }
            }
        }
    }

    private static class DirFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            if (new File(dir.getPath(), name).isDirectory()) {
                return true;
            }
            return false;
        }
    }

    private static class DataFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {
            if (CacheMsgFile.FILE_NAME.equals(name)) {
                return false;
            }
            return true;
        }
    }

}
