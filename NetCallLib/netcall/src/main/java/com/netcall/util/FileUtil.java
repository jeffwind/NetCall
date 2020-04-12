package com.netcall.util;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 具有线程同步机制的文件读写工具
 */
public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();
    /** 同步锁 */
    private static SyncLock<String> syncLock = new SyncLock<>();

    public static boolean writeFile(String path, String text) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "writeFile path is null!");
            return false;
        }
        return writeFile(new File(path), text);
    }

    public static boolean writeFile(File file, String text) {

        String path = file.getPath();
        syncLock.lock(path);
        FileOutputStream fos = null;
        try {
            file.delete();
            file.getParentFile().mkdirs();
            fos = new FileOutputStream(file);
            fos.write(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            StreamUtil.closeStream(fos);
        }
        syncLock.unlock(path);
        return true;
    }

    public static String readFile(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "readFile path is null!");
            return null;
        }
        return readFile(new File(path));
    }

    public static String readFile(File file) {

        if (file == null) {
            return null;
        }

        if (!file.exists()) {
            return null;
        }
        String path = file.getPath();
        syncLock.lock(path);
        StringBuilder sb = new StringBuilder();
        //打开文件输入流
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while (len > 0) {
                sb.append(new String(buffer, 0, len));
                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            StreamUtil.closeStream(inputStream);
        }
        syncLock.unlock(path);
        return sb.toString();
    }

    public static InputStream readStream(String path) {

        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "readFile path is null!");
            return null;
        }
        return readStream(new File(path));
    }

    public static InputStream readStream(File file) {

        if (file == null) {
            return null;
        }

        if (!file.exists()) {
            return null;
        }
        //打开文件输入流
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
        }

        return inputStream;
    }

    public static boolean delete(String path) {
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "delete path is null!");
            return false;
        }
        return delete(new File(path));
    }

    public static boolean delete(File file) {
        if (file == null) {
            return false;
        }

        if (!file.exists()) {
            return true;
        }
        String path = file.getPath();
        syncLock.lock(path);
        boolean isSuc = file.delete();
        syncLock.unlock(path);
        return isSuc;
    }

}
