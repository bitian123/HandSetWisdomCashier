package com.centerm.epos.printer;

/**
 * Created by ysd on 2017/6/14.
 */

public interface IPrinterCallBack {
    void onPrinterFirstSuccess();

    void onPrinterSecondSuccess();

    void onPrinterThreeSuccess();

    void onPrinterFirstFail(int errorCode, String errorMsg);

    void onPrinterSecondFail(int errorCode, String errorMsg);

    void onPrinterThreeFail(int errorCode, String errorMsg);
}
