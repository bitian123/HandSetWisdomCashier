package com.centerm.epos.model;

import android.os.Bundle;

/**
 * Created by yuhc on 2017/8/14.
 *
 */

public interface ITradeParameter {

    String KEY_TRANS_PARAM = "KEY_TRANS_PARAM"; //交易参数

    String KEY_MSG_TAGS = "KEY_MSG_TAGS";   //交易报文组包时，用到的报文标识

    Bundle getParam(String tradeTag);   //获取交易参数

}
