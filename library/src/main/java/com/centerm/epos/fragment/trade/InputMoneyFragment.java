package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.present.transaction.CheckCardPresent;
import com.centerm.epos.present.transaction.InputMoneyPresent;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TradeTempInfoTag;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.NumberPad;

import java.util.Locale;

/**
 * 输入金额界面
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */
public class InputMoneyFragment extends BaseTradeFragment implements View.OnClickListener {
    private NumberPad numberPad;
    private TextView amtShow;
    InputMoneyPresent present;
    @Override
    protected ITradePresent newTradePresent() {
        InputMoneyPresent present = (InputMoneyPresent) super.newTradePresent();
        if (present == null) {
            present = new InputMoneyPresent(this);
        }
        this.present = present;
        return present;
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_input_money;
    }


    private void getLoadAmount(){
        if( TransCode.MAG_CASH_LOAD.equals(mTradePresent.getTradeCode())
                || TransCode.MAG_ACCOUNT_LOAD.equals(mTradePresent.getTradeCode()) ){
//            String amount = mTradePresent.getTempData(TradeInformationTag.ISO62_RES);
//            if( amount != null ) {
////                amtShow.setText(String.format(Locale.CHINA, "%d.%02d", Long.parseLong(amount) / 100, Long.parseLong(amount) % 100));
//            }
            amtShow.setText(mTradePresent.getTempData(TradeInformationTag.TRANS_MONEY));
        }

    }
    @Override
    public void onInitView(View rootView) {

        setTitlePicture(rootView,R.drawable.pic_money);

       // mTradePresent.getTradeCode()
        numberPad = (NumberPad) rootView.findViewById(R.id.number_pad_show);
        amtShow = (TextView) rootView.findViewById(R.id.money_show);
        getLoadAmount();
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
    public void onResume() {
        super.onResume();
        if( TransCode.MAG_CASH_LOAD.equals(mTradePresent.getTradeCode())
                || TransCode.MAG_ACCOUNT_LOAD.equals(mTradePresent.getTradeCode()) ) {
            hideBackBtn();
            mTradePresent.onConfirm(amtShow.getText().toString());
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
