package com.centerm.epos.ebi.ui.fragment;

import android.view.View;
import android.widget.TextView;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.R;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;

/**
 * 选择证件类型
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class AuthTypeFragment extends BaseTradeFragment implements View.OnClickListener {

    @Override
    protected ITradePresent newTradePresent() {
        return new BaseTradePresent(this);
    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        view.findViewById(R.id.mBtnType1).setOnClickListener(this);
        view.findViewById(R.id.mBtnType2).setOnClickListener(this);
        view.findViewById(R.id.mBtnType3).setOnClickListener(this);
        view.findViewById(R.id.mBtnType4).setOnClickListener(this);
        view.findViewById(R.id.mBtnType5).setOnClickListener(this);
        view.findViewById(R.id.mBtnType6).setOnClickListener(this);

        showingTimeout((TextView) view.findViewById(R.id.mTvShowTimeOut));
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick())
            return;
        if (v.getId() == R.id.mBtnType1) {
            goNext("1");
        }else if(v.getId() == R.id.mBtnType2){
            goNext("2");
        }else if(v.getId() == R.id.mBtnType3){
            goNext("5");
        }else if(v.getId() == R.id.mBtnType4){
            goNext("3");
        }else if(v.getId() == R.id.mBtnType5){
            goNext("4");
        }else if(v.getId() == R.id.mBtnType6){
            goNext("0");
        }else {
            super.onClick(v);
        }
    }

    private void goNext(String idType){
        mTradePresent.getTransData().put(JsonKeyGT.idType, idType);
        mTradePresent.getTransData().put("isOther", "isOther");
        mTradePresent.gotoNextStep("1");
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_auth_type;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("选择证件类型");
    }


}
