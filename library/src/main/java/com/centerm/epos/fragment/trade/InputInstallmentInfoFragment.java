package com.centerm.epos.fragment.trade;

import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.present.transaction.IInputInstallmentInfoPresenter;
import com.centerm.epos.present.transaction.InputInstallmentInfoPresenter;

/**
 * 输入交易信息界面。例如消费撤销时要求输入原始交易流水；退货时要求输入检索参考号和交易日期；预授权完成和预授权撤销要求输入授权码。
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class InputInstallmentInfoFragment extends BaseTradeFragment {

    public IInputInstallmentInfoPresenter presenter;
    private EditText etPeriod, etCode;
    private RadioGroup rgPayMode;

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new InputInstallmentInfoPresenter(this);
        return presenter;
    }

    @Override
    protected void onInitView(View view) {
        etPeriod = (EditText) view.findViewById(R.id.et_installment_period);
        etCode = (EditText) view.findViewById(R.id.et_installment_code);
        rgPayMode = (RadioGroup) view.findViewById(R.id.rg_pay_mode);
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_installment_info;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.positive_btn) {
            String period = etPeriod.getText().toString();
            String code = etCode.getText().toString();
            //0为一次性支付手续费，1为分期支付手续费
            int payMode = rgPayMode.getCheckedRadioButtonId() == R.id.pay_all ? 0 : 1;
            presenter.onConfirmClicked(period, code, payMode);
        } else {
            super.onClick(v);
        }
    }
}
