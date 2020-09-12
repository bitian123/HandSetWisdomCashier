package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.present.transaction.ShowEcCardNumPresent;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;


/**
 * 确认原始交易信息界面。用于消费撤销和预授权完成撤销的原始交易信息展示。
 * 主要显示的要素有：原交易类型、流水号、交易金额、交易卡号、检索参考号、交易时间
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class ShowEcCardNumFragment extends BaseTradeFragment implements View.OnClickListener{
    ShowEcCardNumPresent showEcCardNumPresent = null;
    TextView show_card_num = null;
    Button positive_btn = null;
    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    protected ITradePresent newTradePresent() {
        showEcCardNumPresent = new ShowEcCardNumPresent(this);
        return showEcCardNumPresent;
    }

    @Override
    protected void onInitView(View view) {
        show_card_num =(TextView)view.findViewById(R.id.show_card_num);
        positive_btn = (Button)view.findViewById(R.id.positive_btn);
        show_card_num.setText((String)showEcCardNumPresent.mTradeInformation.getTransDatas().get(TradeInformationTag.BANK_CARD_NUM));

        positive_btn.setOnClickListener(this);

        setTitlePicture(view,R.drawable.pic_pinzheng);
    }

    @Override
    public void onClick(View v){
        if(v.getId() == R.id.positive_btn){
            showEcCardNumPresent.onConfirm();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBackBtn();
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_show_ec_num;
    }


}
