package com.centerm.epos.fragment.sys;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.centerm.epos.R;
import com.centerm.epos.activity.E10SysMenuActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.bean.GtBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.ViewUtils;

import java.util.HashMap;
import java.util.Map;

import static com.centerm.epos.activity.msn.BaseModifyPwdActivity.PARAM_TAG;

/**
 * create by liubit on 2019-09-06
 */
public class FingerStep1Fragment extends BaseFragment {
    private EditText edtxt_account;
    private EditText edtxt_pwd;


    @Override
    protected void onInitView(View view) {
        edtxt_account = (EditText) view.findViewById(R.id.edtxt_account);
        edtxt_pwd = (EditText) view.findViewById(R.id.edtxt_pwd);
        view.findViewById(R.id.mBtnModify).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }

    private void login(){
        final String name = edtxt_account.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            ViewUtils.showToast(getActivity(), "请输入账户名");
            return;
        }
        final String pw = edtxt_pwd.getText().toString().trim();
        if(TextUtils.isEmpty(pw)){
            ViewUtils.showToast(getActivity(), "请输入密码");
            return;
        }
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.oaAccount, name);
        dataMap.put(JsonKeyGT.oaPassword, pw);
        DialogFactory.showLoadingDialog(getActivity(), "正在进行帐号密码认证\n请稍侯");
        sendData(false, TransCode.staffVerify, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    GtBean bean = (GtBean) result.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){
                        ViewUtils.showToast(getActivity(), "认证成功");
                        goNext(name,pw);
                    }else {
                        ViewUtils.showToast(getActivity(), bean.getMsg());
                    }
                }else {
                    ViewUtils.showToast(getActivity(), "通讯异常，请重试");
                }
            }
        });

    }

    private void goNext(String oaAccount, String pw){
        FingerStep2Fragment fragment = new FingerStep2Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(JsonKeyGT.oaAccount, oaAccount);
        bundle.putString(JsonKeyGT.oaPassword, pw);
        fragment.setArguments(bundle);
        ((E10SysMenuActivity)getActivity()).gotoFragment(fragment);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_finger_step1;
    }
}
