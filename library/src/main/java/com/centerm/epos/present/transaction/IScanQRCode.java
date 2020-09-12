package com.centerm.epos.present.transaction;

/**
 * Created by yuhc on 2017/4/22.
 */

public interface IScanQRCode {

    void onStartScanCode();

    void onGetScanCode(String code);

}
