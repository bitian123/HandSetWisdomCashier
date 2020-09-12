package com.centerm.epos.redevelop;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumMacType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.CommonUtils;

import org.apache.log4j.Logger;

/**
 * Created by yuhc on 2017/6/6.
 */

public class UnionPayMacAlgorithm implements IMacAlgorithm {
    protected Logger logger = Logger.getLogger(getClass());

    @Override
    public String calculateMessageMac(String msg) {

        if (!TextUtils.isEmpty(msg)) {
            IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
            if (pinPadDev != null) {
                String macData = null;
                if(EncryptAlgorithmEnum.SM4 == Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext())) {
                    macData = pinPadDev.getMac(EnumMacType.CUP_SM4, msg, true);
                    if (!TextUtils.isEmpty(macData))
                        macData = macData.substring(0, 16);
                }
                else
                    macData = pinPadDev.getMac(EnumMacType.CUP_ECB, msg, null);

                logger.info("==>Local MAC==>" + macData);
                return macData;
            } else {
                logger.warn("==>计算本地MAC失败==>pinPadDev is null");
            }
        }
        return null;
    }
}
