package com.centerm.epos.activity.msn;

import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ISystemService;
import com.centerm.epos.BuildConfig;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.BasePlatform;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.configure.EposProject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/7/31.
 *
 */

public class ShowAppVersionActivity extends BaseActivity {

    LinearLayout mContentLayout;

    @Override
    public int onLayoutId() {
        return R.layout.activity_app_version;
    }

    @Override
    public void onInitView() {
        TextView textView;
        textView = (TextView) findViewById(R.id.txtvw_title);
        textView.setText("版本信息");
        mContentLayout = (LinearLayout) findViewById(R.id.app_version_block);
        Set<Map.Entry<String, String>> contents = getVersionContent().entrySet();
        for (Map.Entry<String, String> content :
                contents) {
                addItemView(mContentLayout, content.getKey(), content.getValue(), true);
        }
    }

    private Map<String, String> getVersionContent() {
        Map<String, String> contentList = new LinkedHashMap<>();

        contentList.put("商户号", BusinessConfig.getInstance().getIsoField(this, 42));
        contentList.put("终端号", BusinessConfig.getInstance().getIsoField(this, 41));
        contentList.put("商户名称", BusinessConfig.getInstance().getValue(this, BusinessConfig.Key.KEY_MCHNT_NAME));
        contentList.put("项目标识" , EposProject.getInstance().getId(ConfigureManager.getInstance(this).getProject()));
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            contentList.put("APP版本", "V" + packageInfo.versionName);
            ISystemService systemService = DeviceFactory.getInstance().getSystemDev();
            contentList.put("SDK版本", "V" + systemService.getSDKVersion());
            contentList.put("基础版本", "V" + BasePlatform.getVersion());
            contentList.put("中间件版本", "V" + systemService.getCpayMidLayerVersion());
            contentList.put("金融模块版本", "V" + systemService.readEmvVer());
            contentList.put("终端序列号" , systemService.getTerminalSn());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }

    private void addItemView(LinearLayout itemContainer, String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate( Settings.bIsSettingBlueTheme() ? R.layout.trans_info_item:R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        if( Settings.bIsSettingBlueTheme() ){
            keyShow.setTextColor(getResources().getColor(R.color.font_tip_info));
            valueShow.setTextColor(Color.BLACK);
        }
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(Settings.bIsSettingBlueTheme() ?  R.color.result_divider: R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }
}
