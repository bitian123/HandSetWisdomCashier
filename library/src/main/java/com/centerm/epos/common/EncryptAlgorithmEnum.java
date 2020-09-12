package com.centerm.epos.common;

import android.text.TextUtils;

/**
 * Created by yuhc on 2017/8/11.
 *
 */

public enum  EncryptAlgorithmEnum{

    DES(0,"DES"),
    TRIPLE_DES(1,"3DES"),
    SM4(2,"SM4");

    private int index;
    private String name;

    EncryptAlgorithmEnum(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static EncryptAlgorithmEnum indexOf(int index){
        EncryptAlgorithmEnum[] enums = values();
        for (EncryptAlgorithmEnum item : enums) {
            if (item.getIndex() == index)
                return item;
        }
        return null;
    }

    public static String index2Name(int index){
        if (index < 0)
            return null;
        return indexOf(index).getName();
    }

    public static EncryptAlgorithmEnum nameOf(String name){
        if (TextUtils.isEmpty(name))
            return null;
        EncryptAlgorithmEnum[] enums = values();
        for (EncryptAlgorithmEnum item : enums) {
            if (item.getName().equals(name))
                return item;
        }
        return null;
    }

    public static int name2Index(String name){
        if (TextUtils.isEmpty(name))
            return -1;
        return nameOf(name).getIndex();
    }

    public static String[] names(){
        String[] retNames = new String[values().length];
        EncryptAlgorithmEnum[] enums = values();
        for (int i = 0; i < enums.length; i ++){
            retNames[i] = enums[i].getName();
        }
        return retNames;
    }
}
