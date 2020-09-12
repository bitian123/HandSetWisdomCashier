package com.centerm.epos.fragment.trade;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;
import java.util.Map;

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
public class ShowTradeInfoFragment extends BaseTradeFragment {
    private LinearLayout itemContainer;
    private TradeInfoRecord oriTradeInfo;
    Button mPostButton = null;

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
            oriTradeInfo = (TradeInfoRecord) getArguments().getSerializable(KEY_ORIGIN_INFO);
        }
        else{
            Map<String, Object> transDate = getHostActivity().mTradeInformation.getTransDatas();
            OriginalMessage originalMessage = (OriginalMessage)transDate.get(TradeInformationTag.ORIGINAL_MESSAGE);

            oriTradeInfo = new TradeInfoRecord(getHostActivity().mTradeInformation.getTransCode(),transDate);

            String oriTraceNo = String.format(Locale.CHINA, "%06d", originalMessage.getTraceNumber());//原始流水号

            oriTradeInfo.setVoucherNo(oriTraceNo);
            oriTradeInfo.setTransDate(originalMessage.getDate());
            oriTradeInfo.setTransTime(originalMessage.getTime());
            oriTradeInfo.setAuthorizeNo(originalMessage.getAuthCode());
        }
    }

    @Override
    protected ITradePresent newTradePresent() {
        return new BaseTradePresent(this);
    }

    @Override
    protected void onInitView(View view) {
        itemContainer = (LinearLayout) view.findViewById(R.id.info_block);
        String transCode = mTradePresent.getTradeCode();
        if (TransCode.VOID.equals(transCode) || TransCode.VOID_SCAN.equals(transCode)
                || TransCode.COMPLETE_VOID.equals(transCode)
                || TransCode.VOID_INSTALLMENT.equals(transCode)
                || TransCode.EC_VOID_CASH_LOAD.equals(transCode)
                || TransCode.ISS_INTEGRAL_VOID.equals(transCode)
                || TransCode.UNION_INTEGRAL_VOID.equals(transCode) ) {
            addItemView(getString(R.string.label_org_trans_type2), getString(TransCode.codeMapName(oriTradeInfo
                    .getTransType())), true);
        }
        addItemView(getString(R.string.label_pos_serial), oriTradeInfo.getVoucherNo(), true);
        addItemView(getString(R.string.label_trans_amt2), DataHelper.formatAmountForShow(oriTradeInfo.getAmount())+"元",
                true);
        if (transCode.equals(TransCode.VOID_SCAN))
            addItemView(getString(R.string.label_scan_no), DataHelper.shieldCardNo(oriTradeInfo.getScanCode()), true);
        else
            addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(oriTradeInfo.getCardNo()), true);
        if ( TransCode.VOID.equals(transCode)
             || TransCode.COMPLETE_VOID.equals(transCode)
             || TransCode.VOID_INSTALLMENT.equals(transCode)
             || TransCode.EC_VOID_CASH_LOAD.equals(transCode)
             || TransCode.ISS_INTEGRAL_VOID.equals(transCode)
             || TransCode.UNION_INTEGRAL_VOID.equals(transCode) || transCode.endsWith(TransCode.TRANS_VOID_ENDWITH)) {
            addItemView(getString(R.string.label_sys_ref_no), oriTradeInfo.getReferenceNo(), true);
        }
        addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(oriTradeInfo.getTransTime(),
                oriTradeInfo.getTransDate()), false);
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
        mPostButton = (Button)view.findViewById(R.id.positive_btn);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPostButton.setClickable(true);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_show_trade_info;
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getHostActivity().getLayoutInflater().inflate(Settings.bIsSettingBlueTheme() ? R.layout.trans_info_item: R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -1);
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(getContext());
            divider.setBackgroundColor(getResources().getColor(Settings.bIsSettingBlueTheme() ? R.color.info_divider : R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    @Override
    public void onClick(View v) {
        /*@author zhouzhihua 2017.11.29
        * 扫码撤销交易快速点击，间隔时间较长
        * */
        if (CommonUtils.isFastClick())
            return;
        String transCode = mTradePresent.getTradeCode();
        Map<String, String> dataMap = getHostActivity().mTradeInformation.getDataMap();
        if (v.getId() == R.id.positive_btn) {
            /*@author zhouzhihua 2017.11.29
            *扫码撤销交易信息显示，快速点击，导致交易无法打印签购单
            * 在此出增加响应点击后，Button设置为不可点击状态
            * */
            mPostButton.setClickable(false);
            if (transCode.equals(TransCode.COMPLETE_VOID)) {
                dataMap.put(iso_f37, oriTradeInfo.getReferenceNo());
                dataMap.put(iso_f2, oriTradeInfo.getCardNo());
                dataMap.put(iso_f4, DataHelper.formatAmountForShow(oriTradeInfo.getAmount()));
                dataMap.put(key_oriTransTime, oriTradeInfo.getTransTime());
                dataMap.put(key_oriAuthCode, oriTradeInfo.getAuthorizeNo());
                dataMap.put(key_oriVoucherNumber, oriTradeInfo.getVoucherNo());
            }
            else if ( transCode.equals(TransCode.VOID)
                      || transCode.equals(TransCode.VOID_SCAN)
                      || transCode.equals(TransCode.VOID_INSTALLMENT)
                      || transCode.equals(TransCode.EC_VOID_CASH_LOAD)
                      || TransCode.ISS_INTEGRAL_VOID.equals(transCode)
                      || TransCode.UNION_INTEGRAL_VOID.equals(transCode)
                      || transCode.endsWith(TransCode.TRANS_VOID_ENDWITH) ) {
                dataMap.put(iso_f2, oriTradeInfo.getCardNo());
                dataMap.put(key_oriVoucherNumber, oriTradeInfo.getVoucherNo());
                XLogUtil.w("getServiceEntryMode",oriTradeInfo.getServiceEntryMode());
            }
            if (transCode.equals(TransCode.VOID_SCAN)) {
                getHostActivity().jumpToNext("2");
            } else
                getHostActivity().jumpToNext();
        } else {
            super.onClick(v);
        }
    }
}
