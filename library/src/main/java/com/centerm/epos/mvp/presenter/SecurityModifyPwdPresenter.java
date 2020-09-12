package com.centerm.epos.mvp.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.epos.R;
import com.centerm.epos.mvp.view.IModifySecurityPwdView;
import com.centerm.epos.utils.ViewUtils;

import config.BusinessConfig;

/**
 * Created by 94437 on 2017/7/4.
 */

public class SecurityModifyPwdPresenter implements ISecurityModifyPwdPresenter{
    private Context context;
    private IModifySecurityPwdView iModifySecurityPwdView;
    private int maxLenth = 6;

    public SecurityModifyPwdPresenter(Context context, IModifySecurityPwdView iModifySecurityPwdView) {
        this.context = context;
        this.iModifySecurityPwdView = iModifySecurityPwdView;
    }

    @Override
    public void changeSecurityPwd(String editOldPwd, String editNewPwd, String editNewRePwd) {
        if (TextUtils.isEmpty(editOldPwd)) {
            iModifySecurityPwdView.showResult(R.string.tip_old_pwd_empty, false);
            return;
        }
        if (TextUtils.isEmpty(editNewPwd)) {
            iModifySecurityPwdView.showResult(R.string.tip_new_pwd_empty, false);
            return;
        }
        if (TextUtils.isEmpty(editNewRePwd)) {
            iModifySecurityPwdView.showResult(R.string.tip_renew_pwd_empty, false);
            return;
        }
        if (!editNewPwd.equals(editNewRePwd)) {
            iModifySecurityPwdView.showResult(R.string.tip_pwd_not_same, false);
            return;
        }
        if (editNewPwd.length() != maxLenth) {
            iModifySecurityPwdView.showResult("新密码需为" + maxLenth + "位", false);
            return;
        }
        if (editNewRePwd.length() != maxLenth) {
            iModifySecurityPwdView.showResult("新密码需为" + maxLenth + "位",false);
            return;
        }

        String oldpwd = BusinessConfig.getInstance().getSecurityPwd(context);

        if (editOldPwd != null) {
            if(!editOldPwd.equals(oldpwd)){
                iModifySecurityPwdView.showResult(R.string.tip_pwd_old_pwd_error, false);
            }else{
                BusinessConfig.getInstance().setSecurityPwd(context, editNewPwd);
                iModifySecurityPwdView.showResult(R.string.tip_pwd_suc, true);
            }
        }
    }
}
