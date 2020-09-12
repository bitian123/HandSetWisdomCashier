package com.centerm.epos.ebi.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.present.AuthPresent;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.view.ClearEditText;

/**
 * 输入证件号
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class AuthVerifyFragment extends BaseTradeFragment implements View.OnClickListener {
    private AuthPresent present;
    private ClearEditText account_edit,pwd_edit;

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

        mTvShowTimeOut = (TextView) view.findViewById(R.id.mTvShowTimeOut);
        showingTimeout(mTvShowTimeOut);
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

    private void login(){
        String idNo = account_edit.getText().toString().trim();
        if(TextUtils.isEmpty(idNo)){
            popToast("请输入证件号");
            return;
        }
        String name = pwd_edit.getText().toString().trim();
        if(TextUtils.isEmpty(name)){
            popToast("请输入姓名");
            return;
        }
        mTradePresent.getTransData().put(JsonKeyGT.idNo, idNo);
        mTradePresent.getTransData().put(JsonKeyGT.name, name);
        present.gotoNextStep("1");

    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_auth_verify;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("输入证件号");
    }


}
