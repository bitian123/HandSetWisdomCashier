package com.centerm.epos.utils;

import java.io.ByteArrayOutputStream;

/**
 * Created by zhouzhihua on 2017/12/7.
 */

public class NewEmvTag {
    public static byte[] getF55Tags1() {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            e.write(combine(new int[]{159, 38}));
            e.write(combine(new int[]{159, 39}));
            e.write(combine(new int[]{159, 16}));
            e.write(combine(new int[]{159, 55}));
            e.write(combine(new int[]{159, 54}));
            e.write(combine(new int[]{149}));
            e.write(combine(new int[]{154}));
            e.write(combine(new int[]{156}));
            e.write(combine(new int[]{159, 2}));
            e.write(combine(new int[]{95, 42}));
            e.write(combine(new int[]{130}));
            e.write(combine(new int[]{159, 26}));
            e.write(combine(new int[]{159, 3}));
            e.write(combine(new int[]{159, 51}));
            e.write(combine(new int[]{159, 52}));
            e.write(combine(new int[]{159, 53}));
            e.write(combine(new int[]{159, 30}));
            e.write(combine(new int[]{132}));
            e.write(combine(new int[]{159, 9}));
            e.write(combine(new int[]{159, 65}));
            e.write(combine(new int[]{159, 99}));
            e.write(combine(new int[]{223, 50}));
            e.write(combine(new int[]{223, 51}));
            e.write(combine(new int[]{223, 52}));
            //脚本结果DF31
            e.write(combine(new int[]{223, 49}));
            return e.toByteArray();
        } catch (Exception var1) {
            return null;
        }
    }

    public static byte[] getF55TagsOffLine() {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            e.write(combine(new int[]{0x9F, 0x26}));
            e.write(combine(new int[]{0x9F, 0x27}));
            e.write(combine(new int[]{0x9F, 0x10}));
            e.write(combine(new int[]{0x9F, 0x37}));
            e.write(combine(new int[]{0x9F, 0x36}));
            e.write(combine(new int[]{0x95}));
            e.write(combine(new int[]{0x9A}));
            e.write(combine(new int[]{0x9C}));
            e.write(combine(new int[]{0x9F, 0x02}));
            e.write(combine(new int[]{0x5F, 0x2A}));
            e.write(combine(new int[]{0x82}));
            e.write(combine(new int[]{0x9F, 0x1A}));
            e.write(combine(new int[]{0x9F, 0x03}));
            e.write(combine(new int[]{0x9F, 0x33}));
            e.write(combine(new int[]{0x9F, 0x1E}));
            e.write(combine(new int[]{0x84}));
            e.write(combine(new int[]{0x9F, 0x09}));
            e.write(combine(new int[]{0x9F, 0x41}));
            e.write(combine(new int[]{0x9F, 0x34}));
            e.write(combine(new int[]{0x9F, 0x35}));
            e.write(combine(new int[]{0x9F, 0x63}));
            e.write(combine(new int[]{0x9F, 0x74}));

            return e.toByteArray();
        } catch (Exception var1) {
            return null;
        }
    }
    public static byte[] getF55TagsRevesal() {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            e.write(combine(new int[]{0x95}));
            e.write(combine(new int[]{0x9F,0x1E}));
            e.write(combine(new int[]{0x9F,0x10}));
            e.write(combine(new int[]{0x9F,0x36}));
            e.write(combine(new int[]{0xDF,0x31}));
            return e.toByteArray();
        } catch (Exception var1) {
            return null;
        }
    }
    //9F33 95 9F37 9F1E 9F10 9F26 9F36 82 DF31 9F1A 9A
    /*
    * 脚本上送的55域
    * */
    public static byte[] getF55TagsScript() {
        try {
            ByteArrayOutputStream e = new ByteArrayOutputStream();
            e.write(combine(new int[]{0x9f,0x33}));//C
            e.write(combine(new int[]{0x95}));
            e.write(combine(new int[]{0x9f,0x37}));//C
            e.write(combine(new int[]{0x9F,0x1E}));//C
            e.write(combine(new int[]{0x9F,0x10}));
            e.write(combine(new int[]{0x9F,0x26}));
            e.write(combine(new int[]{0x9F,0x36}));
            e.write(combine(new int[]{0x82}));
            e.write(combine(new int[]{0xDF,0x31}));
            e.write(combine(new int[]{0x9F,0x1A}));
            e.write(combine(new int[]{0x9A})); //M
            return e.toByteArray();
        } catch (Exception var1) {
            return null;
        }
    }

    public static byte[] combine(int... bytes) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(4);

        int i;
        for(i = 0; i < bytes.length; ++i) {
            bout.write(bytes[bytes.length - i - 1]);
        }

        for(i = 0; i < 4 - bytes.length; ++i) {
            bout.write(0);
        }

        return bout.toByteArray();
    }
}
