package com.centerm.epos.ebi.ui.fragment;

import android.view.View;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.present.EbiInputMoneyPresent;
import com.centerm.epos.present.transaction.InputMoneyPresent;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.NumberPad;

/**
 * 输入金额界面
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */
public class EbiInputMoneyFragment extends BaseTradeFragment implements View.OnClickListener {
    private NumberPad numberPad;
    private TextView amtShow;

    @Override
    protected ITradePresent newTradePresent() {
        EbiInputMoneyPresent present = (EbiInputMoneyPresent) super.newTradePresent();
        if (present == null) {
            present = new EbiInputMoneyPresent(this);
        }
        return present;
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_input_money;
    }

    @Override
    public void onInitView(View rootView) {
        setTitlePicture(rootView,R.drawable.pic_money);

        numberPad = (NumberPad) rootView.findViewById(R.id.number_pad_show);
        amtShow = (TextView) rootView.findViewById(R.id.money_show);
        numberPad.bindShowView(amtShow);
        if( bIsBlueTheme() ){
            rootView.findViewById(R.id.positive_btn).setVisibility(View.INVISIBLE);

            numberPad.setCallback(new NumberPad.KeyCallback() {
                @Override
                public void onPressKey(char i) {
                    XLogUtil.d("InputMoneyFragment", "setCallback:" + i);
                    if( i == '\r'){
                        mTradePresent.onConfirm(amtShow.getText().toString());
                    }
                }
            });
        }
        else {
            rootView.findViewById(R.id.positive_btn).setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.positive_btn) {
            mTradePresent.onConfirm(amtShow.getText().toString());
        }else {
            super.onClick(v);
        }
    }
}
