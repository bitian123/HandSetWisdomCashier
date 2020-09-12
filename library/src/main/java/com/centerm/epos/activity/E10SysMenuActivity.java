package com.centerm.epos.activity;

import android.app.Fragment;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.fragment.sys.DownloadTmkMenuFragment;
import com.centerm.epos.fragment.sys.FingerStep1Fragment;
import com.centerm.epos.fragment.sys.MerchantInfoFragment;
import com.centerm.epos.fragment.sys.ModifyPwFragment;
import com.centerm.epos.fragment.sys.QueryOperatorFragment;
import com.centerm.epos.fragment.sys.ReprintMenuFragment;
import com.centerm.epos.fragment.sys.SysManageFragment;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.view.AlertDialog;

import static com.centerm.epos.activity.msn.BaseModifyPwdActivity.PARAM_TAG;

/**
 * E10系统管理界面
 * author:liubit</br>
 * date:2019/9/6</br>
 * */
public class E10SysMenuActivity extends BaseFragmentActivity {
    private RadioGroup mRadioGroup;
    private Fragment sysManageFragment,merchantInfoFragment,modifyPwFragment,
            fingerStep1Fragment,downloadTmkMenuFragment,queryOperatorFragment,
            reprintMenuFragment,iCSetTabHost;

    @Override
    public int onLayoutId() {
        return R.layout.activity_e10_sys_menu;
    }

    @Override
    public void onInitView() {
        mRadioGroup = (RadioGroup) findViewById(R.id.mRadioGroup);
        findViewById(R.id.mBtnExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tipToExit();
            }
        });
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.mRb1){
                    if(sysManageFragment==null){
                        sysManageFragment = new SysManageFragment();
                    }
                    replace(sysManageFragment).commit();
                }else if(i==R.id.mRb2){
                    if(merchantInfoFragment==null){
                        merchantInfoFragment = new MerchantInfoFragment();
                    }
                    replace(merchantInfoFragment).commit();
                }else if(i==R.id.mRb3){
                    if(modifyPwFragment==null){
                        modifyPwFragment = new ModifyPwFragment();
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString(PARAM_TAG, "99");
                    modifyPwFragment.setArguments(bundle);
                    replace(modifyPwFragment).commit();
                }else if(i==R.id.mRb4){
                    if(fingerStep1Fragment==null){
                        fingerStep1Fragment = new FingerStep1Fragment();
                    }
                    replace(fingerStep1Fragment).commit();
                }else if(i==R.id.mRb5){
                    if(downloadTmkMenuFragment==null){
                        downloadTmkMenuFragment = new DownloadTmkMenuFragment();
                    }
                    replace(downloadTmkMenuFragment).commit();
                }else if(i==R.id.mRb6){
                    if(queryOperatorFragment==null){
                        queryOperatorFragment = new QueryOperatorFragment();
                    }
                    replace(queryOperatorFragment).commit();
                }else if(i==R.id.mRb7){
                    if(reprintMenuFragment==null){
                        reprintMenuFragment = new ReprintMenuFragment();
                    }
                    replace(reprintMenuFragment).commit();
                }
            }
        });
        mRadioGroup.check(R.id.mRb1);
    }

    public void gotoFragment(Fragment fragment){
        replace(fragment).commit();
    }

    private void tipToExit() {
        DialogFactory.showSelectDialog(context,Settings.bIsSettingBlueTheme() ? getString(R.string.tip_notification) : null, getString(R.string.tip_confirm_exit), new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        finish();
                        break;
                }
            }
        });
    }

}




