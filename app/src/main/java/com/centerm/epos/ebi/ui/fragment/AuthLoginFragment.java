package com.centerm.epos.ebi.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.present.AuthPresent;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.view.ClearEditText;

import java.util.HashMap;
import java.util.Map;

/**
 * 选择业务类型
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class AuthLoginFragment extends BaseTradeFragment implements View.OnClickListener {
    private AuthPresent present;
    private ClearEditText account_edit,pwd_edit;
    //e10 view
    private RadioGroup mRadioGroup;
    private RelativeLayout mViewFinger;
    private LinearLayout mViewAccount;

    @Override
    protected ITradePresent newTradePresent() {
        present = new AuthPresent(this);
        return present;
    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        account_edit = (ClearEditText) view.findViewById(R.id.account_edit);
        pwd_edit = (ClearEditText) view.findViewById(R.id.pwd_edit);

        view.findViewById(R.id.btn_login).setOnClickListener(this);
        if(!CommonUtils.isK9()){
            initE10View(view);
        }

    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick())
            return;
        if (v.getId() == R.id.btn_login) {
            login();
        }else {
            super.onClick(v);
        }
    }

    private void initE10View(View view){
        mRadioGroup = (RadioGroup) view.findViewById(R.id.mRadioGroup);
        mViewFinger = (RelativeLayout) view.findViewById(R.id.mViewFinger);
        mViewAccount = (LinearLayout) view.findViewById(R.id.mViewAccount);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i==R.id.mRbFinger){
                    mViewFinger.setVisibility(View.VISIBLE);
                    mViewAccount.setVisibility(View.GONE);
                }else {
                    mViewFinger.setVisibility(View.GONE);
                    mViewAccount.setVisibility(View.VISIBLE);
                }
            }
        });
        mRadioGroup.check(R.id.mRbFinger);


    }

    private void fingerVerify(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.oaAccount, "666666");
        dataMap.put(JsonKeyGT.fingerVer, "FMZ+IouBymCgVwB0DWuUgc6B0FO0Dw0wJiGieL4+xCmH3YIZBTy2IwAYJLeA1JuPJhRxqwKvXN5gxhPNKQV5tAAdN6QHJ/wBwQmUX6EDIRRd+PyQyhUS95VCiDCdU28TiwTy8W5ZQa7wweYCeHbHvJddFtMz+vhBQw==");
        sendData(false, TransCode.fingerVerify, dataMap, new OnCallListener() {
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

    private void login(){
        String name = account_edit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            popToast("请输入账户名");
            return;
        }
        String pw = pwd_edit.getText().toString().trim();
        if(TextUtils.isEmpty(pw)){
            popToast("请输入密码");
            return;
        }
        present.getTransData().put(JsonKeyGT.oaAccount, name);
        present.getTransData().put(JsonKeyGT.oaPassword, pw);
        DialogFactory.showLoadingDialog(getActivity(), "正在进行帐号密码认证\n请稍侯");
        sendData(false, TransCode.staffVerify, present.getTransData(), new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    GtBean bean = (GtBean) result.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){
                        popToast("认证成功");
                        present.gotoNextStep("1");
                    }else {
                        popToast(bean.getMsg());
                    }
                }else {
                    popToast("通讯异常，请重试");
                }
            }
        });

    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_auth;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("账户授权");
    }

}
