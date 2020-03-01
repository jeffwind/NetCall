package com.netcall.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射工具
 */
public class ReflectUtil {

    public static List<Field> findFieldsByAnno(Class clazz, Class<? extends Annotation> annoCls) {
        Field[] fields = clazz.getDeclaredFields();
        List<Field> returnList = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annoCls)) {
                returnList.add(field);
            }
        }
        return returnList;
    }

    /**
     * 找到第一个含有annoCls的类变量
     */
    public static Field findFieldByAnno(Class clazz, Class<? extends Annotation> annoCls) {

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annoCls)) {
                return field;
            }
        }
        return null;
    }

    public static String getFieldString(Object obj, Field field) {
        try {
            field.setAccessible(true);
            Object fieldObj = field.get(obj);
            if (fieldObj == null) {
                return null;
            }
            return fieldObj.toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
