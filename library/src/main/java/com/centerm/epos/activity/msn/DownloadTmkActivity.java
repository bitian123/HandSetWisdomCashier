package com.centerm.epos.activity.msn;

import android.content.Intent;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;

/**
 * 下载主密钥
 * Created by liubit on 2019/9/5.
 */
public class DownloadTmkActivity extends BaseActivity implements View.OnClickListener {

    @Override
    public int onLayoutId() {
        return R.layout.activity_download_tmk;
    }

    @Override
    public void onInitView() {
        initBackBtn();

        findViewById(R.id.mBtnDyTmk).setOnClickListener(this);
        findViewById(R.id.mBtnUnionTmk).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if( v.getId() == R.id.mBtnDyTmk){
            startActivity(new Intent(DownloadTmkActivity.this, EbiTMKByICActivity.class));
        }else if( v.getId() == R.id.mBtnUnionTmk){
            ViewUtils.showToast(DownloadTmkActivity.this, "此功能暂未开通，敬请期待");
        }
    }

}
