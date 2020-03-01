package com.netcall.util;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmptyUtil {

    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            return ((String)o).trim().isEmpty();
        } else if (o instanceof List) {
            return ((List)o).isEmpty();
        } else if (o instanceof Reference) {
            return EmptyUtil.isEmpty(((Reference)o).get());
        } else if (o instanceof Map) {
            return ((Map)o).isEmpty();
        } else if (o.getClass().isArray()) {
            return ((Object[])o).length == 0;
        }
        return false;
    }

    public static boolean isEmptyOr(Object... objs) {
        if (objs == null) {
            return true;
        }
        for (Object o : objs) {
            if (isEmpty(o)) {
                return true;
            }
        }
        return false;
    }

}
