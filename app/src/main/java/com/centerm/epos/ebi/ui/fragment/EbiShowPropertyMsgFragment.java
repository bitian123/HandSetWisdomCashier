package com.centerm.epos.ebi.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.present.EbiShowPropertyMsgPresent;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class EbiShowPropertyMsgFragment extends BaseTradeFragment{
    public EbiShowPropertyMsgPresent presenter;
    private LinearLayout itemContainer;
    private JSONObject data;

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new EbiShowPropertyMsgPresent(this);
        return presenter;
    }

    @Override
    protected void onInitView(View view) {
        itemContainer = (LinearLayout) view.findViewById(R.id.trade_info_block);
        view.findViewById(R.id.mBtnCard).setOnClickListener(this);
        view.findViewById(R.id.mBtnScan).setOnClickListener(this);

        try {
            data = new JSONObject((String) presenter.getTransData().get(TradeInformationTag.PROPERTY_MSG));
            addItemView("商户号", GetRequestData.getMercode(), true);
            addItemView("订单号", data.optString("mer_order_no"), true);
            addItemView("交易金额", DataHelper.formatAmount2(data.optString("pay_amount")) +"元", false);
        } catch (JSONException e) {
            e.printStackTrace();
            popToast("订单数据异常");
            getHostActivity().finish();
        }

    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if(v.getId()==R.id.mBtnCard){
            presenter.goCardNexStep(DataHelper.formatAmount2(data.optString("pay_amount")), data.optString("mer_order_no"));
        }else if(v.getId()==R.id.mBtnScan){
            presenter.goScanNexStep(DataHelper.formatAmount2(data.optString("pay_amount")), data.optString("mer_order_no"));
        }else if(v.getId()==R.id.imgbtn_back){
            getHostActivity().finish();
        }

    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_show_property_msg;
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getActivity().getLayoutInflater().inflate(com.centerm.epos.R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(com.centerm.epos.R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(com.centerm.epos.R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(com.centerm.epos.R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(getActivity());
            divider.setBackgroundColor(getResources().getColor(com.centerm.epos.R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }
}
