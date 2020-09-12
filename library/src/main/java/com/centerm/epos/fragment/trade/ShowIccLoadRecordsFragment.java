package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.define.pboc.CardLoadLog;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.transcation.IccRecordsInfo;
import com.centerm.epos.common.Settings;
import com.centerm.smartpos.util.HexUtil;

import java.util.List;
import java.util.Locale;

import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_LOAD_LOG;
import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TRANS_LOG;

/**
 * 确认原始交易信息界面。用于消费撤销和预授权完成撤销的原始交易信息展示。
 * 主要显示的要素有：原交易类型、流水号、交易金额、交易卡号、检索参考号、交易时间
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class ShowIccLoadRecordsFragment extends BaseTradeFragment {
    private ScrollView scrollViewIccRecordsInfo;
    private List<CardLoadLog> iccRecordsInfoList = null;
    private CardLoadLog iccRecordsInfo;
    private LinearLayout linearLayout;

    private void addDivider(int width, int height , int color)
    {
        View divider = new View(getActivity());

        divider.setBackground(getResources().getDrawable(R.drawable.dashed_line));
        height = 5;//高度太小，导致线画不出，暂时设定为5
        /*
        zhouzhihua 因安卓4.0以上版本开启硬件加速导致，画虚线会显示为实线
        此处画虚线关闭硬件加速
        */
        divider.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        linearLayout.addView(divider, width,height);
    }

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        /*@author zhouzhihua 2017.11.22
        * 刷卡界面返回getArgument()=null导致应用崩溃，最简单的改法是将oriTradeInfo 申明成static即可
        * 因fragment 切换时并未调用onSaveInstanceState 导致无法保存savedInstanceState
        * getArgument和fragment同生命周期无法保存数据
        *采用如下方式恢复数据
        * */
        if( getArguments() != null ){
            iccRecordsInfoList = (List<CardLoadLog>)getArguments().getSerializable(KEY_LOAD_LOG);
        }

    }

    @Override
    protected ITradePresent newTradePresent() {
        return new BaseTradePresent(this);
    }

    @Override
    protected void onInitView(View view) {
        scrollViewIccRecordsInfo = (ScrollView)view.findViewById(R.id.scrollViewIccRecordsInfo);
        linearLayout = (LinearLayout)view.findViewById(R.id.linearLayout);

        for(int i = 0 ; i < iccRecordsInfoList.size() ; i++ ) {
            iccRecordsInfo = iccRecordsInfoList.get(i);
            addItemView("圈存记录",String.format(Locale.CHINA,"%02d",i+1),false);
            addItemView("P1",iccRecordsInfo.getPutdata_p1(),false);
            addItemView("P2",iccRecordsInfo.getPutdata_p2(),false);
            addItemView("圈存前金额",iccRecordsInfo.getBefore_putdata(),false);
            addItemView("圈存后金额",iccRecordsInfo.getAfter_putdata(),false);
            addItemView("交易时间","20"+iccRecordsInfo.getTransDate()+iccRecordsInfo.getTransTime(),false);
            addItemView("ATC", HexUtil.bcd2str(iccRecordsInfo.getAppTransCount()),false);

            this.addDivider(-1,-1,getResources().getColor(R.color.common_divider));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBackBtn();
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_show_icc_records;
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getHostActivity().getLayoutInflater().inflate( R.layout.trans_info_item , null );
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        linearLayout.addView(view, -1, -1);
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(getContext());
            divider.setBackgroundColor(getResources().getColor(Settings.bIsSettingBlueTheme() ? R.color.info_divider : R.color.common_divider));
            linearLayout.addView(divider, -1, (int) size);
        }
    }

}
