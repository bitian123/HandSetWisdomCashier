package com.centerm.epos.common;

import android.text.TextUtils;

/**
 * Created by yuhc on 2017/8/11.
 */

public enum NFCTradeChannel{

    ONLINE(0,"联机借贷记"),
    OFFLINE(1,"电子现金");

    private int index;
    private String name;

    NFCTradeChannel(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public static NFCTradeChannel indexOf(int index){
        NFCTradeChannel[] enums = values();
        for (NFCTradeChannel item : enums) {
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

    public static NFCTradeChannel nameOf(String name){
        if (TextUtils.isEmpty(name))
            return null;
        NFCTradeChannel[] enums = values();
        for (NFCTradeChannel item : enums) {
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
        NFCTradeChannel[] enums = values();
        for (int i = 0; i < enums.length; i ++){
            retNames[i] = enums[i].getName();
        }
        return retNames;
    }
}
