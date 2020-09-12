package com.centerm.epos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.centerm.epos.EposApplication;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.utils.XLogUtil;

/**
 * Created by yuhc on 2017/7/19.
 */

public class UpdateDBCompleteReceiver extends BroadcastReceiver {
    private static final String TAG = UpdateDBCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        PrintManager printManager = new PrintManager(EposApplication.getAppContext());
        if(printManager.isTemplateEmpty()) {
            XLogUtil.i(TAG, "^_^ 打印模板为空，正在导入... ^_^");
            printManager.importTemplate();
        }

    }
}
