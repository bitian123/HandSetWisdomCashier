package com.centerm.epos.ebi.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.ReceivedQueryBean;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.R;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.OnTimeOutListener;
import com.centerm.epos.view.AlertDialog;

import java.util.Map;

/**
 * 选择业务类型
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class BusinessTypeFragment extends BaseTradeFragment implements View.OnClickListener {
    private ImageView mIvTip;

    @Override
    protected ITradePresent newTradePresent() {
        return new BaseTradePresent(this);
    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        view.findViewById(R.id.mBtnSale).setOnClickListener(this);
        view.findViewById(R.id.mBtnRefund).setOnClickListener(this);
        view.findViewById(R.id.mBtnPrint).setOnClickListener(this);

        //非身份证验证进入
        if(mTradePresent.getTransData().get("isOther")!=null){
            mIvTip = (ImageView) view.findViewById(R.id.mIvTip);
            mIvTip.setBackground(getActivity().getResources().getDrawable(R.drawable.auth_step_5));
        }

        showingTimeout((TextView) view.findViewById(R.id.mTvShowTimeOut));

    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick())
            return;
        if (v.getId() == R.id.mBtnSale) {
            if(CommonUtils.isK9()){
                mTradePresent.gotoNextStep("1");
            }else {
                mTradePresent.gotoNextStep("3");
            }
        }else if(v.getId() == R.id.mBtnRefund){

        }else if(v.getId() == R.id.mBtnPrint){
            if(CommonUtils.isK9()){
                mTradePresent.gotoNextStep("2");
            }else {
                mTradePresent.gotoNextStep("4");
            }
        }else {
            super.onClick(v);
        }
    }

    @Override
    public boolean onBacKeyPressed() {
        logger.info("实体键返回");
        tipToExit();
        return true;
    }

    @Override
    public boolean onBackPressed() {
        logger.info("左上角返回");
        tipToExit();
        return true;
    }

    private void tipToExit(){
        DialogFactory.showSelectDialog(getActivity(), getString(com.centerm.epos.R.string.tip_notification), "确认退出业务？", new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        getHostActivity().finish();
                        break;
                }
            }
        });
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_business;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("选择业务类型");
    }


}
