package com.centerm.epos.mvp.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.mvp.view.ISecurityView;

import config.BusinessConfig;

/**
 * Created by 94437 on 2017/7/4.
 */

public class SecurityPresenter implements ISecurityPresenter{
    private Context context;
    private ISecurityView iSecurityView;
    private BusinessConfig config = BusinessConfig.getInstance();

    public SecurityPresenter(Context context, ISecurityView iSecurityView) {
        this.context = context;
        this.iSecurityView = iSecurityView;
    }

    @Override
    public void setTerminalParam(String TermNo,String MerchantNo, String MerchantName, String pwd, String
            merchantEnglishName) {

        if(!config.getSecurityPwd(context).equals(pwd)){
            iSecurityView.showResult("安全密码错误", false);
        }else{
            if(!TextUtils.isEmpty(TermNo)){
                config.setIsoField(context, 41, TermNo);
            }else {
                iSecurityView.showResult("终端号不能为空", false);
                return;
            }
            if(!TextUtils.isEmpty(MerchantNo)){
                config.setIsoField(context, 42, MerchantNo);
            }else {
                iSecurityView.showResult("商户号不能为空", false);
                return;
            }
            if(!TextUtils.isEmpty(MerchantName)){
                config.setValue(context, BusinessConfig.Key.KEY_MCHNT_NAME, MerchantName);
            }else {
                iSecurityView.showResult("商户名不能为空", false);
                return;
            }
            //商户英文名称内容可选，所以不用校验检查，直接设置，且允许为空
            config.setValue(context, BusinessConfig.Key.KEY_MCHNT_ENGLISH_NAME, merchantEnglishName);
            iSecurityView.showResult("设置成功", true);
        }
    }

    @Override
    public boolean isNeedInputPwd(String TermNo, String MerchantNo, String MerchantName, String merchantEnglishName) {
        if (config.getIsoField(context, 41) == null ||
                config.getIsoField(context,42) == null ||
                config.getValue(context, BusinessConfig.Key.KEY_MCHNT_NAME) == null ||
                config.getValue(context, BusinessConfig.Key.KEY_MCHNT_ENGLISH_NAME) == null)
            return true;

        return !(config.getIsoField(context, 41).equals(TermNo)
                && config.getIsoField(context,42).equals(MerchantNo)
                && config.getValue(context, BusinessConfig.Key.KEY_MCHNT_NAME).equals(MerchantName)
                && config.getValue(context, BusinessConfig.Key.KEY_MCHNT_ENGLISH_NAME).equals(merchantEnglishName));
    }

    @Override
    public boolean isInputDataValidate(String TermNo, String MerchantNo, String MerchantName) {
        if(TextUtils.isEmpty(TermNo) || TextUtils.isEmpty(MerchantNo) || TextUtils.isEmpty(MerchantName)) {
            iSecurityView.showResult("商户号/终端号/商户名称，不能为空", false);
            return false;
        }
        return true;
    }
}
