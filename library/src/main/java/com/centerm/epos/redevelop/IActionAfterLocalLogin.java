package com.centerm.epos.redevelop;

import com.centerm.epos.base.BaseFragmentActivity;

/**
 * Created by ysd on 2017/12/6.
 */

public interface IActionAfterLocalLogin {
    void doAction(BaseFragmentActivity hostActivity, String tagAccount, IActionCallBack iActionCallBack);

    interface IActionCallBack {
        void resumeAfterAction();
    }
}
