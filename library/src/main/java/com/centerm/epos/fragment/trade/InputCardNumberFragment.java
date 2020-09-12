package com.centerm.epos.fragment.trade;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.present.transaction.IInputCommonInfoPresenter;
import com.centerm.epos.present.transaction.InputCardNumberPresenter;
import com.centerm.epos.present.transaction.InputCommonInfoPresenter;
import com.centerm.epos.utils.XLogUtil;

/**
 * 输入交易信息界面。积分消费商品编码。
 * author:zhouzhihua</br>
 * date:2017/1/2</br>
 */
public class InputCardNumberFragment extends BaseTradeFragment  {

    public InputCardNumberPresenter presenter;
    private EditText cardEditText;
    private EditText validDateEditText;

    @Override
    protected ITradePresent newTradePresent() {

        presenter = new InputCardNumberPresenter(this);
        return presenter;
    }

    @Override
    protected void onInitView(View view) {
        cardEditText = (EditText) view.findViewById(R.id.et_card_number);
        validDateEditText = (EditText) view.findViewById(R.id.et_card_valid);

        view.findViewById(R.id.positive_btn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.positive_btn) {
                    ConfirmClicked();
                }
            }
        });
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_card_number;
    }

    private void ConfirmClicked(){
        String[] mCardData = new String[2];
        mCardData[0] = cardEditText.getText().toString().trim();
        mCardData[1] = validDateEditText.getText().toString().trim();
        presenter.onConfirmClicked(mCardData);
    }
}
