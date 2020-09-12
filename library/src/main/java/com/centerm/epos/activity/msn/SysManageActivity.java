package com.centerm.epos.activity.msn;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.mvp.presenter.ISecurityPresenter;
import com.centerm.epos.mvp.presenter.SecurityPresenter;
import com.centerm.epos.mvp.view.ISecurityView;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.inputSecurityDialog;

import config.BusinessConfig;

/**
 * 系统管理
 * Created by liubit on 2019/9/5.
 */
public class SysManageActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int onLayoutId() {
        return R.layout.activity_sys_manage;
    }

    @Override
    public void onInitView() {
        initBackBtn();

        findViewById(R.id.mBtnShutdown).setVisibility(View.GONE);

        findViewById(R.id.mBtnBack).setOnClickListener(this);
        findViewById(R.id.mBtnShutdown).setOnClickListener(this);
        findViewById(R.id.mBtnReboot).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if( v.getId() == R.id.mBtnBack){
            for(Activity activity : ActivityStack.getInstance().getActivityStack()){
                activity.finish();
            }
        }else if( v.getId() == R.id.mBtnShutdown){

        }else if( v.getId() == R.id.mBtnReboot){
            reboot();
        }
    }

    private void reboot(){
        try {
            DeviceFactory factory = DeviceFactory.getInstance();
            factory.getSystemDev().reboot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
