package com.centerm.epos.utils;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.epos.R;
import com.centerm.epos.common.Settings;

import java.lang.reflect.Method;

import static com.baidu.location.h.j.H;

/**
 * 跟View相关的工具类
 *
 * @author wanliang527
 * @date 2014-1-17
 */
public class ViewUtils {
    public static final String STATUS_BAR_SERVICE = "statusbar";
    static Toast toast = null;

    public final static LayoutParams MATCH_MATCH = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    public final static LayoutParams MATCH_WRAP = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    public final static LayoutParams WRAP_MATCH = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    public final static LayoutParams WRAP_WRAP = new LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    /**
     * 显示Toast
     *
     * @param c
     * @param txt
     */
    public static void showToast(Context c, String txt) {
//        if (toast != null) {
//            toast.cancel();
//        }
        //toast持有context引用会导致内存泄露
//        toast = Toast.makeText(c, txt, Toast.LENGTH_SHORT);
//        toast.show();

        if( !Settings.bIsSettingBlueTheme() ) {
            Toast.makeText(c, txt, Toast.LENGTH_SHORT).show();
        }
        else{
            TipToast.makeText(c, txt, Toast.LENGTH_SHORT).show();
        }
        //Toast.makeText(c, txt, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示Toast
     *
     * @param c
     * @param stringId
     */
    public static void showToast(Context c, int stringId) {
        if( !Settings.bIsSettingBlueTheme() ) {
            Toast.makeText(c, stringId, Toast.LENGTH_SHORT).show();
        }
        else {
            TipToast.makeText(c, stringId, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取LayoutInflater对象
     *
     * @param context
     * @return
     */
    public static LayoutInflater getInflater(Context context) {
        return (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * 设置View的背景图
     *
     * @param v
     * @param obj 可以是Drawable对象也可以是colorid、resId
     * @return 设置成功返回true，否则返回false
     */
    public static boolean setViewBackground(View v, Object obj) {
        if (v == null || obj == null)
            return false;
        if (obj instanceof Drawable) {
            v.setBackgroundDrawable((Drawable) obj);
            return true;
        }
        if (obj instanceof Integer) {
            try {
                v.setBackgroundResource((Integer) obj);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    v.setBackgroundColor((Integer) obj);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    public static int getThemeResource(Context context, int attrName) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attrName, value, true);
        return value.resourceId;
    }

    public static void enableStatusBar(Context context) {
        if(!CommonUtils.isK9()){
            return;
        }
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disableStatusBar(Context context) {
        if(!CommonUtils.isK9()){
            return;
        }
        Object service = context.getSystemService("statusbar");
        try {
            Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusBarManager.getMethod("disable", int.class);
            expand.invoke(service, 0x00010000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TipToast extends Toast
    {

        public TipToast(Context context){
            super(context);
        }
        public static Toast makeText(Context context, CharSequence text, int duration) {
            Toast result = new Toast(context);

            LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflate.inflate(R.layout.toast_tip, null);
            TextView tv = (TextView)v.findViewById(R.id.message);
            tv.setText(text);
            result.setView(v);
            result.setDuration(duration);
            return result;
        }

        public static Toast makeText(Context context, @StringRes int resId, int duration)
                throws Resources.NotFoundException {
            XLogUtil.w("TipToast","TipToast:"+context.getResources().getText(resId));
            return makeText(context, context.getResources().getText(resId), duration);
        }

    }

    public static void showKeyBoard(Activity activity){
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);    //InputMethodManager.SHOW_FORCED
    }

    public static void hintKeyBoard(Activity activity) {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && activity.getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (activity.getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

}
