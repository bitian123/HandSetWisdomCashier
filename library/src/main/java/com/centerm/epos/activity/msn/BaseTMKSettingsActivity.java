package com.centerm.epos.activity.msn;

import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.epay.keyboard.BuildConfig;
import com.centerm.epay.keyboard.HexNumberKeyboard;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;

import java.lang.reflect.Method;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 商户设置界面
 * Created by ysd on 2016/11/30.
 */
public class BaseTMKSettingsActivity extends BaseActivity {

    private EditText etTMKIndex;
    private EditText etTMKContent;
    private int MainKeyIndex;

    @Override
    public int onLayoutId() {
        return R.layout.activity_tmk_setting;
    }

    @Override
    public void onInitView() {
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_tmk);
        etTMKIndex = (EditText) findViewById(R.id.extxt_tmk_index);
        etTMKContent = (EditText) findViewById(R.id.extxt_tmk_data);
        etTMKContent.requestFocus();
        initEditWithKeyboard(this, findViewById(R.id.keyboard_view));
    }

    HexNumberKeyboard kb;

    private void initEditWithKeyboard(Context context, View view) {
        kb = new HexNumberKeyboard(context, view, etTMKContent);
        etTMKContent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (android.os.Build.VERSION.SDK_INT <= 10)
                    etTMKContent.setInputType(InputType.TYPE_NULL);
                else {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    try {
                        Class<EditText> cls = EditText.class;
                        Method setShowSoftInputOnFocus;
                        setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                        setShowSoftInputOnFocus.setAccessible(true);
                        setShowSoftInputOnFocus.invoke(etTMKContent, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                kb.showKeyboard();
                return false;
            }
        });

    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        MainKeyIndex = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.MAINKEYINDEX);
        String strMkIndex = String.format("%02d", MainKeyIndex);
        etTMKIndex.setText(strMkIndex);//密钥索引
//        String digists = "0123456789abcdefABCDEF";
//        etTMKContent.setKeyListener(DigitsKeyListener.getInstance(digists));
    }

    public void onSureClick(View v) {
        String tmkIndexStr = etTMKIndex.getText().toString();
        if (TextUtils.isEmpty(tmkIndexStr) || tmkIndexStr.length() != 2) {
            ViewUtils.showToast(this, "请输入2位密钥索引！");
            return;
        }
        BusinessConfig.getInstance().setNumber(context, BusinessConfig.Key.MAINKEYINDEX, Integer.parseInt(tmkIndexStr));
        String tmkContentStr = etTMKContent.getText().toString();
        if (TextUtils.isEmpty(tmkContentStr) && tmkContentStr.length() != 32) {
            ViewUtils.showToast(this, "请输入32位密钥明文数据！");
            return;
        }
        IPinPadDev pinPad = CommonUtils.getPinPadDev();
        if (pinPad == null ) {
            ViewUtils.showToast(this, "密码键盘异常！");
            logger.warn("密码键盘异常!");
            return;
        }
        boolean result;
        EncryptAlgorithmEnum encAlg = Settings.getEncryptAlgorithmEnum(context);
        if (encAlg == EncryptAlgorithmEnum.SM4)
            result = pinPad.loadSM4TMK(Integer.parseInt(tmkIndexStr), tmkContentStr, null);
        else
            result = pinPad.loadTMK(Integer.parseInt(tmkIndexStr), tmkContentStr, null);

        ViewUtils.showToast(context, "设置"+(result?"成功":"失败"));
        activityStack.pop();
    }
}
