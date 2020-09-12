package com.centerm.epos.activity.msn;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.mvp.presenter.ISecurityPresenter;
import com.centerm.epos.mvp.presenter.SecurityPresenter;
import com.centerm.epos.mvp.view.ISecurityView;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.inputSecurityDialog;

import config.BusinessConfig;

/**
 * 绿城商户设置界面
 * Created by liubit on 2019/9/5.
 */
public class GtMerchantSettingsActivity extends BaseActivity implements ISecurityView,View.OnClickListener {

    private EditText merchantNo;
    private EditText bankTermNo;
    private EditText merchantName, etMerchantEnglishName;

    private String iso41;
    private String iso42;
    private String iso43;
    private String merchantEnglishName;

    private inputSecurityDialog dialog;
    private ISecurityPresenter iSecurityPresenter;

    @Override
    public int onLayoutId() {
        return R.layout.activity_machant_setting_gt;
    }

    @Override
    public void onInitView() {
        initBackBtn();
        merchantNo = (EditText) findViewById(R.id.extxt_marchant_number);
        bankTermNo = (EditText) findViewById(R.id.extxt_bank_term_no);
        merchantName = (EditText) findViewById(R.id.extxt_merchant_name);
        etMerchantEnglishName = (EditText) findViewById(R.id.extxt_merchant_name_english);
        iSecurityPresenter = new SecurityPresenter(this, this);

        dialog = new inputSecurityDialog(context);
        dialog.setClickListener(new inputSecurityDialog.ButtonClickListener() {
            @Override
            public void onClick(inputSecurityDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        iSecurityPresenter.setTerminalParam(bankTermNo.getText().toString(),
                                merchantNo.getText().toString(),
                                merchantName.getText().toString(),
                                dialog.getInputText(), etMerchantEnglishName.getText().toString());
                        break;
                    case NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        });
    }
    /*
    *@author:zhouzhihua add 2017.11.08
    * 增加是否存在交易流水的判断
    * */
    private void doneTransElement()
    {
        if(bIsHaveVoucher()){
            bankTermNo.setFocusable(false);
            merchantNo.setFocusable(false);

            bankTermNo.setCursorVisible(false);
            merchantNo.setCursorVisible(false);

            //bankTermNo.setTextColor(getResources().getColor(R.color.font_hint2));
            //merchantNo.setTextColor(getResources().getColor(R.color.font_hint2));

            bankTermNo.setOnClickListener(this);
            merchantNo.setOnClickListener(this);
        }
        else{
            bankTermNo.setFocusable(true);
            merchantNo.setFocusable(true);

            bankTermNo.setCursorVisible(true);
            merchantNo.setCursorVisible(true);
            //bankTermNo.setTextColor(getResources().getColor(R.color.font_black));
            //merchantNo.setTextColor(getResources().getColor(R.color.font_black));
        }
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        BusinessConfig config = BusinessConfig.getInstance();
        iso41 = config.getIsoField(context, 41);
        iso42 = config.getIsoField(context, 42);
        iso43 = config.getValue(context, BusinessConfig.Key.KEY_MCHNT_NAME);
        merchantEnglishName = config.getValue(context, BusinessConfig.Key.KEY_MCHNT_ENGLISH_NAME);
        bankTermNo.setText(iso41);//终端号
        merchantNo.setText(iso42);//商户号
        merchantName.setText(iso43);//商户名
        etMerchantEnglishName.setText(merchantEnglishName);//商户英文名称
        /*
        *@author:zhouzhihua add 2017.11.08
        * 增加是否存在交易流水的判断
        * */
        //doneTransElement();
    }

    public void onSureClick(View v) {
        if (!iSecurityPresenter.isInputDataValidate(bankTermNo.getText().toString(),
                merchantNo.getText().toString(),
                merchantName.getText().toString()))
            return;
        if (iSecurityPresenter.isNeedInputPwd(bankTermNo.getText().toString(),
                merchantNo.getText().toString(),
                merchantName.getText().toString(), etMerchantEnglishName.getText().toString())) {
            dialog.show();
        } else {
            activityStack.pop();
        }
    }


    @Override
    public void showResult(String result, boolean isSuccess) {
        ViewUtils.showToast(context, result);

        if (isSuccess) {
            dialog.dismiss();
            activityStack.pop();
        } else {
            dialog.clearPwdText();
        }
    }

    /*
    *@author:zhouzhihua add 2017.11.08
    * 增加是否存在交易流水的判断
    * */
    @Override
    public void onClick(View v)
    {
        if( v.getId() == R.id.extxt_marchant_number || v.getId() == R.id.extxt_bank_term_no ){
            //ViewUtils.showToast(this,"存在交易流水,请先结算后再试!");
        }
    }
}
