package com.netcall.util;

import java.io.File;

/**
 * 文件夹工具
 */
public class DirUtil {

    /**
     * 保证某文件夹存在
     */
    public static boolean ensureDirExists(String path) {

        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 保证父文件夹存在
     */
    public static boolean ensureParentDirExists(String path) {
        return ensureDirExists(new File(path).getParent());
    }
}
