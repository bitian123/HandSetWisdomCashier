package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.transcation.IccRecordsInfo;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.centerm.cpay.midsdk.dev.define.pboc.PbocEventAction.KEY_TRANS_LOG;
import static com.centerm.epos.base.BaseActivity.KEY_ORIGIN_INFO;
import static com.centerm.epos.common.TransDataKey.iso_f2;
import static com.centerm.epos.common.TransDataKey.iso_f37;
import static com.centerm.epos.common.TransDataKey.iso_f4;
import static com.centerm.epos.common.TransDataKey.key_oriAuthCode;
import static com.centerm.epos.common.TransDataKey.key_oriTransTime;
import static com.centerm.epos.common.TransDataKey.key_oriVoucherNumber;

/**
 * 确认原始交易信息界面。用于消费撤销和预授权完成撤销的原始交易信息展示。
 * 主要显示的要素有：原交易类型、流水号、交易金额、交易卡号、检索参考号、交易时间
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class ShowIccRecordsFragment extends BaseTradeFragment {
    private ScrollView scrollViewIccRecordsInfo;
    private List<IccRecordsInfo> iccRecordsInfoList = null;
    private IccRecordsInfo iccRecordsInfo;
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

        if( getArguments() != null ){
            iccRecordsInfoList = (List<IccRecordsInfo>)getArguments().getSerializable(KEY_TRANS_LOG);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBackBtn();

        for(int i = 0 ; i < iccRecordsInfoList.size() ; i++ ) {
            iccRecordsInfo = iccRecordsInfoList.get(i);
            addItemView("交易记录",String.format(Locale.CHINA,"%02d",i+1),false);
            addItemView(getString(R.string.label_trans_amt2),iccRecordsInfo.getAuthAmt(),false);
            addItemView("其它金额",iccRecordsInfo.getOtherAmt(),false);
            addItemView("商户名称",iccRecordsInfo.getMerchName(),false);

            //SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            addItemView("交易时间","20"+iccRecordsInfo.getTransDate()+iccRecordsInfo.getTransTime(),false);
            addItemView("ATC",iccRecordsInfo.getAtc(),false);
            addItemView("交易类型",iccRecordsInfo.getTransType(),false);
            String s = iccRecordsInfo.getCurCode();
            addItemView("货币代码",s.replaceFirst("^0*",""),false);
            s = iccRecordsInfo.getTermCountryCode();
            addItemView("终端国家代码",s.replaceFirst("^0*",""),false);
            this.addDivider(-1,-1,getResources().getColor(R.color.common_divider));
        }

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
