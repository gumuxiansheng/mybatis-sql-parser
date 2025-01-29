package cn.mikezhu.mysqlparser.utils;

import java.lang.reflect.Field;
import java.util.Arrays;

public class ObjectUtil {
    public static Object getPrivateField(Object obj, String fieldName) throws Exception {
        final Class<?> clazz = obj.getClass();
        final Field privateField;
        if (Arrays.stream(clazz.getDeclaredFields()).distinct().anyMatch(field -> field.getName().equals(fieldName))) {
            privateField = clazz.getDeclaredField(fieldName);
        } else {
            privateField = clazz.getSuperclass().getDeclaredField(fieldName);
        }
        privateField.setAccessible(true);
        return privateField.get(obj);
    }
}
