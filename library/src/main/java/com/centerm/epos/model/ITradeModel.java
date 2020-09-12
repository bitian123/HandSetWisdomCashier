package com.centerm.epos.model;

import android.content.Intent;
import android.os.Bundle;

import com.centerm.epos.bean.iso.Iso62Qps;

/**
 * Created by yuhc on 2017/4/23.
 */

public interface ITradeModel {


    boolean isBundleOfResultExist();

    void createBundleOfResult();

    boolean putDataToBundleOfResult(String key, Object value);

    void setResultCode(int code);

    int getResultCode();

    Bundle getResultBundle();

    Iso62Qps getQpsParams();

    void setQpsParams(Iso62Qps qpsParams);

    boolean isTradeNoPin();

    void setTradeNoPin(boolean noPin);

    boolean isTradeSlipNoSign();

    void setTradeSlipNoSign(boolean noSign);

    String getSlipNoSignAmount();

    void setSlipNoSignAmount(String amount);

    void setTradeParam(Bundle innerInvokerParams);

    Bundle getTradeParam();
}
