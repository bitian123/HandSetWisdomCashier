package com.centerm.epos.fragment.sys;

import android.view.View;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseFragment;

/**
 * create by liubit on 2019-09-06
 */
public class SysManageFragment extends BaseFragment {
    
    @Override
    protected void onInitView(View view) {
        view.findViewById(R.id.mBtnBack).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.mBtnBack){
            ActivityStack.getInstance().RemoveAll();
        }
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_sys_manage;
    }
}
