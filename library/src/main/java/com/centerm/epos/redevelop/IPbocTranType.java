package com.centerm.epos.redevelop;

import com.centerm.cpay.midsdk.dev.define.pboc.EnumTransType;

/**
 * Created by yuhc on 2017/8/22.
 *
 */

public interface IPbocTranType {

    EnumTransType getTranTypeMap(String tranType);

}
