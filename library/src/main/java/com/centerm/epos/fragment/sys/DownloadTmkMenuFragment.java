package com.centerm.epos.fragment.sys;

import android.view.View;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ViewUtils;

/**
 * create by liubit on 2019-09-06
 */
public class DownloadTmkMenuFragment extends BaseFragment {

    @Override
    protected void onInitView(View view) {
        view.findViewById(R.id.mBtnDy).setOnClickListener(this);
        view.findViewById(R.id.mBtnUnion).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if(v.getId()==R.id.mBtnDy){
            ((E10SysMenuActivity)getActivity()).gotoFragment(new DownloadTmkFragment());
        }else if(v.getId()==R.id.mBtnUnion){
            ViewUtils.showToast(getActivity(), "此功能暂未开通，敬请期待");
        }
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_downloadtmk_menu;
    }


}
