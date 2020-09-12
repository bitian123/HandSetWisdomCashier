package com.centerm.epos.fragment.sys;

import android.view.View;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.OnCallListener;

import java.util.HashMap;
import java.util.Map;

/**
 * create by liubit on 2019-09-06
 */
public class FingerStep2Fragment extends BaseFragment {
    private TextView mTvNum;
    private String name,pw;

    @Override
    protected void onInitView(View view) {
        name = getArguments().getString(JsonKeyGT.oaAccount);
        pw = getArguments().getString(JsonKeyGT.oaPassword);

        mTvNum = (TextView) view.findViewById(R.id.edtxt_account);
        view.findViewById(R.id.mBtnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishFragment();
            }
        });

    }

    private void fingerRegister(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.oaAccount, name);
        dataMap.put(JsonKeyGT.oaPassword, pw);
        dataMap.put(JsonKeyGT.userId, "123");
        dataMap.put(JsonKeyGT.oaName, "aaa");
        dataMap.put(JsonKeyGT.fingerVer, "FMZ+IouBymCgVwB0DWuUgc6B0FO0Dw0wJiGieL4+xCmH3YIZBTy2IwAYJLeA1JuPJhRxqwKvXN5gxhPNKQV5tAAdN6QHJ/wBwQmUX6EDIRRd+PyQyhUS95VCiDCdU28TiwTy8W5ZQa7wweYCeHbHvJddFtMz+vhBQw==");
        dataMap.put(JsonKeyGT.fingerReg, "FGZ/IJtE6oHZsw5jGWjKRByBLFyiEBYSw9HOSKZXTCsUZEWS8MywH3cXxJBh9ReiX6KFZ0KmdWxWBCbUvWWZxAyBD+8OJ60gmD2MKSHw5RduEWYbAJLa98Gawv4SOWCS6wWYbUqZPizzCCB+8ZBPe2kAFMaBICACPAE4cxyxDoAP4h3ZrE6qJItT5hGWkHQ5yitIABKBByy6JYQYxSfAdKyZOJWzhQK6ZWRYBQ3NWJBxxwSvOQINx50hNQ+IPCL0i5RYmUyjIYWUicSyl7AtV+ASKxWp2WmcV69OJGa19JbsxENr");
        sendData(false, TransCode.fingerRegister, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBannerBean bean = (GtBannerBean) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){

                    }else {

                    }
                }else {

                }
            }
        });
    }

    private void finishFragment(){
        ((E10SysMenuActivity)getActivity()).gotoFragment(new FingerStep1Fragment());
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_finger_step2;
    }

}
