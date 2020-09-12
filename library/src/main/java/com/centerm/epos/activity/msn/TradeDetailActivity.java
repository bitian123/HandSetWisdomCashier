package com.centerm.epos.activity.msn;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.TradeInfo;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ConverUtil;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.printer.QianBaoPrinter;
import com.centerm.epos.utils.ViewUtils;

import java.util.Map;

/**
 * 交易详情界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */

public class TradeDetailActivity extends BaseActivity {

    private LinearLayout itemContainer;
    private TradeInfo tradeInfo;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        tradeInfo = (TradeInfo) getIntent().getSerializableExtra(KEY_TRADE_INFO);
        if (tradeInfo == null) {
            logger.warn("交易信息为空==>请传递交易信息到此界面");
            tradeInfo = new TradeInfo();
        }
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_detail;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_trade_detail);
        itemContainer = (LinearLayout) findViewById(R.id.trade_info_block);
        if (tradeInfo.getFlag() == 2) {
            addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransCode())) + "（已撤销）", true);
        } else if (tradeInfo.getFlag() == 4) {
            addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransCode())) + "（已退货）", true);
        } else {
            addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransCode())), true);
        }
        if (!TransCode.AUTH.equals(tradeInfo.getTransCode())) {
            addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(tradeInfo.getIso_f2()), true);
        } else {
            addItemView(getString(R.string.label_card_no), tradeInfo.getIso_f2(), true);
        }
        addItemView(getString(R.string.label_trans_amt2), DataHelper.formatAmountForShow(tradeInfo.getIso_f4()), true);
        addItemView(getString(R.string.label_serial_num_num), tradeInfo.getIso_f11(), true);
        addItemView(getString(R.string.label_auth_num), tradeInfo.getIso_f38(), true);
        addItemView(getString(R.string.label_sys_ref_no), tradeInfo.getIso_f37(), true);
        addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(tradeInfo.getIso_f12(), tradeInfo.getIso_f13()), false);
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate(R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    public void onReprintSlip(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        printData();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void printData() {
        if (null != tradeInfo) {
            Map<String, Object> mapData = tradeInfo.convert2Map();
            QianBaoPrinter qianBaoPrinter = QianBaoPrinter.getMenuPrinter();
            qianBaoPrinter.init(context);
            qianBaoPrinter.printData(ConverUtil.convertObject2String(mapData), tradeInfo.getTransCode(), true);
        } else {
            ViewUtils.showToast(context, "无订单数据");
        }
    }
}
