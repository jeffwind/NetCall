package com.netcall.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Params implements Iterable<Param> {

    private List<Param> paramList = new ArrayList<>();

    public void add(Param param) {
        paramList.add(param);
    }

    public void addAll(Params params) {
        if (params == null) {
            return;
        }
        paramList.addAll(params.paramList);
    }

    public boolean remove(Param param) {
        return paramList.remove(param);
    }

    public boolean remove(String name) {
        return remove(get(name));
    }

    public Param get(String name) {
        if (name == null) {
            return null;
        }
        for (Param param : paramList) {
            if (name.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }

    /**
     * 转换为map，value如果需要encode则首先会encode再添加进map。
     */
    public Map<String, String> toMap() {

        Map<String, String> map = new HashMap<>();
        for (Param param : paramList) {
            map.put(param.getName(), param.getValueEncoded());
        }
        return map;
    }

    @Override
    public Iterator<Param> iterator() {
        return paramList.iterator();
    }

    /**
     * 转化为String，可作为url后缀
     * 转化后的形式为name=value&name=value
     */
    public String toParamString() {

        Iterator<Param> keyIter = iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (keyIter.hasNext()) {
            Param param = keyIter.next();

            stringBuilder.append(param.getName());
            stringBuilder.append("=");
            if (param.isEncoded()) {
                stringBuilder.append(param.getValue());
            } else {
                stringBuilder.append(param.getValue());
            }
            if (!keyIter.hasNext()) {
                break;
            }
            stringBuilder.append("&");
        }
        return stringBuilder.toString();
    }

    public void sortByName() {
        Collections.sort(paramList, new Comparator<Param>() {
            @Override
            public int compare(Param o1, Param o2) {
                if (o2 == null) {
                    return -1;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2.getName() == null) {
                    return -1;
                }
                if (o1.getName() == null) {
                    return -1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
}
