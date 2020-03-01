package com.netcall.cache;

import com.netcall.Response;

public interface ICacheCompare {


    boolean isEqual(Response response1, Response response2);

    /** 是否需要比较{@link Response#getBean()}, 返回false则Bean将为空，以省略加载Bean这个耗时操作 */
    boolean needCompareBean();


    class Holder {

        private static ICacheCompare defaultCompare = new CacheCompareText();
        public static ICacheCompare getDefault() {
            return defaultCompare;
        }
    }

    class CacheCompareText implements ICacheCompare {

        @Override
        public boolean isEqual(Response response1, Response response2) {
            if (response1 == null && response2 == null) {
                return true;
            }
            if (response1 == null || response2 == null) {
                return false;
            }
            if (response1.getRespStr() == null) {
                return false;
            }

            return response1.getRespStr().equals(response2.getRespStr());
        }

        @Override
        public boolean needCompareBean() {
            return false;
        }
    }
}
