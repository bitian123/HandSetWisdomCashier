package com.centerm.epos.utils;

import android.content.Context;

/**
 * author:wanliang527</br>
 * date:2017/2/19</br>
 */

public class ResourceUtils {

    public static int getStringId(Context context, String idName) {
        return context.getResources().getIdentifier(idName, "string",
                context.getPackageName());
    }

    public static int getDrawableId(Context context, String idName) {
        return context.getResources().getIdentifier(idName, "drawable",
                context.getPackageName());
    }

    public static int getLayoutId(Context context, String idName) {
        return context.getResources().getIdentifier(idName, "layout",
                context.getPackageName());
    }

}
