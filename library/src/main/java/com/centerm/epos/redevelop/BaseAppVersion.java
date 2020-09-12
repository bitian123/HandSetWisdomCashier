package com.centerm.epos.redevelop;

import android.content.Context;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.epos.EposApplication;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/7/11.
 */

public class BaseAppVersion implements IAppVersion {
    @Override
    public String getVersionName(Context context) {
        String version = PackageUtils.getInstalledVersionName(context, context.getPackageName());
        BusinessConfig.getInstance().setValue(EposApplication.getAppContext(),BusinessConfig.Key.KEY_APP_VERSION,version);
        return "程序版本：V" + version;
    }
}
