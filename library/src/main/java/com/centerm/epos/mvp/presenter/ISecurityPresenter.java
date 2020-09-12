package com.centerm.epos.mvp.presenter;

/**
 * Created by 94437 on 2017/7/4.
 */

public interface ISecurityPresenter {
    public void setTerminalParam(String TermNo, String MerchantNo, String MerchantName, String pwd, String
            merchantEnglishName);

    public boolean isNeedInputPwd(String TermNo, String MerchantNo, String MerchantName, String merchantEnglishName);

    boolean isInputDataValidate(String TermNo, String MerchantNo, String MerchantName);
}
