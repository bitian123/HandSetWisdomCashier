package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.mvp.view.IInputOriginInfoView;
import com.centerm.epos.present.transaction.InputPhoneNumberPresenter;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import config.BusinessConfig;

/**
 * 输入交易信息界面。例如消费撤销时要求输入原始交易流水；退货时要求输入检索参考号和交易日期；预授权完成和预授权撤销要求输入授权码。
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class InputPhoneNumberFragment extends BaseTradeFragment implements IInputOriginInfoView {

    public InputPhoneNumberPresenter presenter;
    private EditText etPhoneNumber;

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new InputPhoneNumberPresenter(this);
        return presenter;
    }

    @Override
    protected void onInitView(View view) {
        etPhoneNumber = (EditText) view.findViewById(R.id.et_phone_number);
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_phone_number;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.positive_btn) {
            String phoneNumber = etPhoneNumber.getText().toString();
            presenter.onConfirmClicked(phoneNumber);
        } else {
            popToast(R.string.tip_input_phone);
        }
    }

    @Override
    public boolean onBacKeyPressed() {
        popToast(R.string.tip_input_phone);
        return true;
    }
}
