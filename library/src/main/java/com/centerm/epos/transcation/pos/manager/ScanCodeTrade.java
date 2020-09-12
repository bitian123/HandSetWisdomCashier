package com.centerm.epos.transcation.pos.manager;

import android.app.Activity;
import android.content.Intent;

import com.centerm.epos.activity.msn.BaseScanCodeActivity;

/**
 * Created by zhouzhihua on 2017/11/23.
 * 扫码模块可以自定义
 */

public class ScanCodeTrade {
    public static final String SCAN_LINE_IMAGE_NAME = "SCAN_LINE_IMAGE_NAME";/*扫描线的image*/
    public static final String SCAN_SQUARE_IMAGE_NAME = "SCAN_SQUARE_IMAGE_NAME";
    public static final String SCAN_OVER_TIME = "SCAN_OVER_TIME";/*data type long*/

    public void scanBarCode(Activity activity)
    {
        Intent intent = new Intent();
//        intent.putExtra(SCAN_LINE_IMAGE_NAME,"scan_line_green");
//        intent.putExtra(SCAN_SQUARE_IMAGE_NAME,"pic_saoyisao");
//        intent.putExtra(SCAN_OVER_TIME,60L);
        intent.setClass(activity, BaseScanCodeActivity.class);
        activity.startActivityForResult(intent, 1);
    }
}
