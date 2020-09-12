package com.centerm.epos.mvp.presenter;

import android.os.Handler;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.mvp.model.IVerifyPwdBiz;
import com.centerm.epos.mvp.model.VerifyPwdBiz;
import com.centerm.epos.mvp.view.IVerifyPwdView;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */

public class VerifyPwdPresenter extends BaseTradePresent implements IVerifyPwdPresenter {

    private StringBuilder stringBuilder;
    private IVerifyPwdView view;
    private IVerifyPwdBiz model;

    public VerifyPwdPresenter(IVerifyPwdView view) {
        super(view);
        this.view = view;
        stringBuilder = new StringBuilder();
        model = new VerifyPwdBiz();
    }

    @Override
    public void onHandleKey(char i) {
        if (i == '.' || i=='\r' || i == 'L') {
            return;
        }
        if (i == (char) -1) {
            int len = stringBuilder.length();
            if (len > 0) {
                stringBuilder.deleteCharAt(len - 1);
                view.changeIndicator(stringBuilder.length());
            }
            return;
        }
        stringBuilder.append(i);
        int len = stringBuilder.length();
        view.changeIndicator(len);
        String content = stringBuilder.toString();
        if (len == 6) {
            if (content.equals(model.get00Pwd(DbHelper.getInstance()))) {
                if (TransCode.REFUND_SCAN.equals(mTradeInformation.getTransCode()))
                    gotoNextStep("2");
                else
                    gotoNextStep();
            } else {
                view.popToast(R.string.tip_pwd_illegal);
                stringBuilder.setLength(0);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.changeIndicator(0);
                    }
                }, 50);
            }
            DbHelper.releaseInstance();
        }
    }
}
