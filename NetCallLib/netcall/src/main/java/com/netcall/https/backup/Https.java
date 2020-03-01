package com.netcall.https.backup;

import java.util.HashMap;
import java.util.Map;

public class Https {

    public static final int TYPE_NO_AUTH = 0;
    public static final int TYPE_ONE_AUTH = 1;
    public static final int TYPE_TWO_AUTH = 1;

    /** 路径为assets文件 */
    public static final int PATH_ASSETS = 0;
    /** 文件路径为文件系统的文件 */
    public static final int PATH_FILE = 1;

    private static Https instance;

    private static Map<String, HttpsItemData> dataMap = new HashMap<>();
    private static int type;

    public static Https getInstance() {
        if (instance == null) {
            synchronized (Https.class) {
                if (instance == null) {
                    instance = new Https();
                }
            }
        }
        return instance;
    }

    /**
     * 注册https认证信息，普通https连接不需要注册。只有单向、双向认证才需要注册。
     */
    public void registHttps(String baseUrl, int type) {

    }

    public static Builder builder(String baseUrl) {
        return new Builder();
    }

    public static class Builder {

        HttpsItemData itemData = new HttpsItemData();


        public void regist() {

        }
    }

    private static class HttpsItemData {
        private String baseUrl;
        private int type;
        // 文件路径，
        private int pathType;
        // 服务器证书路径，用于单、双向认证
        private String trustCertPath;
        // 客户端证书路径，用于双向认证
        private String clientCertPath;
    }
}
