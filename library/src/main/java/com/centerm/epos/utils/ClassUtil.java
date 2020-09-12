package com.centerm.epos.utils;

import dalvik.system.DexFile;
import dalvik.system.PathClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by yuhc on 2017/10/16.
 *
 */

public class ClassUtil {

    private static final String TAG = ClassUtil.class.getSimpleName();

    /**
     * 扫描指定包下的所有类，包括子包，并返回类名称
     * @param packageName   需要扫描的名名
     * @return  类名称
     */
    public static List<String> scanPackage(String packageName) {
        List<String> classNameList = new ArrayList<>();
        try {
            PathClassLoader classLoader = (PathClassLoader) Thread
                    .currentThread().getContextClassLoader();
            Object dexElements = getDexElements(getPathList(classLoader));
            Class elementType = dexElements.getClass().getComponentType();
            int elementsSize = Array.getLength(dexElements);
            for (int i = 0; i < elementsSize; i++) {
                DexFile dexFile = (DexFile) getField(Array.get(dexElements, i), elementType, "dexFile");
                //获取包名+类名
                Enumeration<String> enumeration = dexFile.entries();
                while (enumeration.hasMoreElements()) {//遍历
                    String className = enumeration.nextElement();
                    //查找包含有该包名的所有类，不包含内部类
                    if (className.startsWith(packageName) && !className.contains("$")) {
                        classNameList.add(className);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classNameList;
    }


    /**
     * 通过反射先获取到pathList对象
     */

    private static Object getPathList(Object obj) throws ClassNotFoundException, NoSuchFieldException,
            IllegalAccessException {
        return getField(obj, Class.forName("dalvik.system.BaseDexClassLoader"), "pathList");
    }

    /**
     * 从上面获取到的PathList对象中，进一步反射获得dexElements对象
     */
    private static Object getDexElements(Object obj) throws NoSuchFieldException, IllegalAccessException {
        return getField(obj, obj.getClass(), "dexElements");
    }

    private static Object getField(Object obj, Class cls, String str)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);//设置为可访问
        return declaredField.get(obj);
    }

    private static void setField(Object obj, Class cls, String str, Object obj2)
            throws NoSuchFieldException, IllegalAccessException {
        Field declaredField = cls.getDeclaredField(str);
        declaredField.setAccessible(true);//设置为可访问
        declaredField.set(obj, obj2);
    }
}
