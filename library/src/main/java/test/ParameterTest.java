package test;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.utils.XLogUtil;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/22.
 */

public class ParameterTest {

    private static final String TAG = ParameterTest.class.getSimpleName();

    public static void initTestParam(Context context) {
        if (TextUtils.isEmpty(BusinessConfig.getInstance().getIsoField(context,41)))
        {
            XLogUtil.e(TAG, "^_^ Import Test Mechant Data ^_^");
            //青岛测试商户
//            BusinessConfig.getInstance().setIsoField(context, 41, "00937001");
//            BusinessConfig.getInstance().setIsoField(context, 42, "100053270110093");
            //东莞测试商户
//            BusinessConfig.getInstance().setIsoField(context, 41, "18075912");
//            BusinessConfig.getInstance().setIsoField(context, 42, "309441983980002");
            //广州银联
            BusinessConfig.getInstance().setIsoField(context, 41, "05106433");
            BusinessConfig.getInstance().setIsoField(context, 42, "102440153119090");
            BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_MCHNT_NAME, "我的测试商户");
        }
        new PrintManager(context).importTemplate();
    }
}
