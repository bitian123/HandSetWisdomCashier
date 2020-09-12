package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.keys.Keys;

import config.BusinessConfig;


/**
 * Created by 94437 on 2017/7/5.
 */

public class BasePrintSettingsActivity extends BaseActivity {
    private EditText etPrintNum;
    private int intPrintNum;
    @Override
    public int onLayoutId() {
        return R.layout.activity_print_setting;
    }

    @Override
    public void onInitView() {

        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.printnumsetting);
        etPrintNum = (EditText) findViewById(R.id.extxt_print_num);
        intPrintNum = BusinessConfig.getInstance().getNumber(context, Keys.obj().printnum);
        etPrintNum.setText(intPrintNum + "");
    }

    public void onSureClick(View v){
        String printNumStr = etPrintNum.getText().toString();
        BusinessConfig.getInstance().setNumber(context, Keys.obj().printnum, Integer.parseInt(printNumStr));
        activityStack.pop();
    }
}
