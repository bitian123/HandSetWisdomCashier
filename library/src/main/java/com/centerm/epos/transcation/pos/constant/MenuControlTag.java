package com.centerm.epos.transcation.pos.constant;

import com.centerm.epos.common.TransCode;

/**
 * Created by yuhc on 2017/4/27.
 */

public interface MenuControlTag {

    String PREFIX = "TOGGLE_";
    String SALE = PREFIX + TransCode.SALE;
    String AUTH = PREFIX + TransCode.AUTH;
    String CANCEL = PREFIX + TransCode.CANCEL;
    String AUTH_COMPLETE = PREFIX + TransCode.AUTH_COMPLETE;
    String COMPLETE_VOID = PREFIX + TransCode.COMPLETE_VOID;
    String VOID = PREFIX + TransCode.VOID;
    String REFUND = PREFIX + TransCode.REFUND;
    String BALANCE = PREFIX + TransCode.BALANCE;
}
