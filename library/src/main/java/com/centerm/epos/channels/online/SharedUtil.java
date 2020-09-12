package com.centerm.epos.channels.online;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class SharedUtil {

    public static final String PREFERENT_NAME = "preferentname_centerm_policy";

    public static void putString(Context context, String key, String val) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFERENT_NAME, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static void putBoolean(Context context, String key, boolean val) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFERENT_NAME, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(key, val);
        editor.commit();
    }

    public static String getString(Context context, String key,
                                   String defaultVal) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFERENT_NAME, Context.MODE_PRIVATE);
        return prefs.getString(key, defaultVal);
    }

    public static boolean getBoolean(Context context, String key,
                                     boolean defaultVal) {
        SharedPreferences prefs = context.getSharedPreferences(
                PREFERENT_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, defaultVal);
    }

    public static String getVersion(Context context) {
        String appVersion = "1.00.00.000";
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            appVersion = info.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return appVersion;
    }
}
