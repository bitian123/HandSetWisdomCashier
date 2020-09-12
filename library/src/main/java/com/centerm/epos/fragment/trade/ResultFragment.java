package com.centerm.epos.fragment.trade;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.present.transaction.BaseResultPresent;
import com.centerm.epos.present.transaction.IResult;
import com.centerm.epos.present.transaction.ResultPresent;
import com.centerm.epos.utils.CommonUtils;

import java.util.Iterator;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.keyBalanceAmt;

/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */

public class ResultFragment extends BaseTradeFragment implements View.OnClickListener {
    private static final String TAG = ResultFragment.class.getSimpleName();

    private LinearLayout itemContainer;
    private ImageView flagIconShow;
    private TextView flagTextShow;
    private Button returnBtn;
    private IResult mResultPresent;
    TextView result_text_show_value;

    @Override
    public int onLayoutId() {
        return R.layout.fragment_result;
    }
    /*
    * zhouzhihua
    * 蓝色版本UI分割线
    * */
    public void addDivider(int width, int height , int color)
    {
        View divider = new View(getActivity());

        divider.setBackground(getResources().getDrawable(R.drawable.dashed_line));
        height = 5;//高度太小，导致线画不出，暂时设定为5
        /*
        zhouzhihua 因安卓4.0以上版本开启硬件加速导致，画虚线会显示为实线
        此处画虚线关闭硬件加速
        */
        divider.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        itemContainer.addView(divider, width,height);
    }
    private boolean bIsBalanceInfoDisplay()
    {
        String transCode = getHostActivity().mTradeInformation.getTransCode();
        if( (TransCode.BALANCE.equals(transCode) || TransCode.UNION_INTEGRAL_BALANCE.equals(transCode))
                && (result_text_show_value != null) ) {

            flagTextShow.setTextColor(getResources().getColor(R.color.font_tip));
            String balanceValue = getHostActivity().mTradeInformation.getTempMap().get(keyBalanceAmt);
            result_text_show_value.setVisibility(View.VISIBLE);
            result_text_show_value.setText(mResultPresent.getTradeTitle());
            flagTextShow.setText(balanceValue);
            flagTextShow.setTextSize(getResources().getDimension(R.dimen.font_30));
            flagTextShow.setTextColor(getResources().getColor(R.color.font_black));
            return true;
        }
        return false;
    }
    private void relocationTitleInfo()
    {
        RelativeLayout.LayoutParams layoutParamsRe = new RelativeLayout.LayoutParams(flagTextShow.getLayoutParams());

        layoutParamsRe.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParamsRe.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_60);
        flagTextShow.setLayoutParams(layoutParamsRe);
    }
    /*
    *蓝色ui需要重新定位显示位置
    * */
    private void resultTitleInfo(){
        if( !Settings.bIsSettingBlueTheme() ) {

            flagIconShow.setImageResource(R.drawable.pic_success);
            if(result_text_show_value!=null){
                result_text_show_value.setVisibility(View.GONE);
            }
        }
        else{
            if( !bIsBalanceInfoDisplay() )
            {
                relocationTitleInfo();
                flagTextShow.setTextColor(getResources().getColor(R.color.font_blue_success));
                result_text_show_value.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onInitView(View rootView) {
        hideBackBtn();
        itemContainer = (LinearLayout) rootView.findViewById(R.id.result_info_block);
        flagIconShow = (ImageView) rootView.findViewById(R.id.result_pic_show);
        flagTextShow = (TextView) rootView.findViewById(R.id.result_text_show);
        returnBtn = (Button) rootView.findViewById(R.id.return_btn);
        returnBtn.setOnClickListener(this);

        if(Settings.bIsSettingBlueTheme()) {
            setTitlePicture(rootView,mResultPresent.isSuccess() ? R.drawable.pic_suc : R.drawable.pic_fail);
            result_text_show_value = (TextView) rootView.findViewById(R.id.result_text_show_value);
        }
		/*BUGID:0003158
         *在下一个fragment界面显示的时候上一个fragment界面还没被销毁，
         * 获取到返回键导致上个界面句柄使用异常
         * */
        returnBtn.setClickable(false);
        Handler mHandler = new Handler(){
            @Override
            public void dispatchMessage(Message msg) {
                returnBtn.setClickable(true);
            }
        };
        mHandler.sendMessageDelayed(mHandler.obtainMessage(),300);
        flagTextShow.setText(mResultPresent.getTradeTitle());
        if (mResultPresent.isSuccess()) {

            this.resultTitleInfo();

            List<IResult.ItemViewData> viewDatas = mResultPresent.getItemViewData();
            if (viewDatas != null && viewDatas.size() > 0) {
                Iterator<IResult.ItemViewData> iterator = viewDatas.iterator();
                IResult.ItemViewData itemViewData;
                if(Settings.bIsSettingBlueTheme()) {
                    addDivider(-1,1,getResources().getColor(R.color.common_divider));
                }
                while (iterator.hasNext()) {
                    itemViewData = iterator.next();
                    addItemView(itemViewData.getTip(), itemViewData.getContent(),Settings.bIsSettingBlueTheme() ? false : itemViewData.isAddDivider());
                }
                if(Settings.bIsSettingBlueTheme()) {
                    addDivider(-1,1,getResources().getColor(R.color.common_divider));
                }
            } else
                itemContainer.setVisibility(View.INVISIBLE);
        } else {

            if(Settings.bIsSettingBlueTheme()) {
                RelativeLayout relativeLayout = (RelativeLayout)rootView.findViewById(R.id.layout_title);
                relativeLayout.setBackgroundResource(R.drawable.bg_result_fail);
                returnBtn.setBackgroundResource(R.drawable.btn_fail);
                addDivider(-1,1,getResources().getColor(R.color.common_divider));
                flagTextShow.setTextColor(getResources().getColor(R.color.font_blue_fail));
            }
            else{
                flagIconShow.setImageResource(R.drawable.pic_jingao);
            }
            addItemView(getString(R.string.label_resp_code), mResultPresent.getResponseCode(), Settings.bIsSettingBlueTheme() ? false : true);//状态码
            addItemView(getString(R.string.label_resp_msg), mResultPresent.getResponseMessage(), false);//提示信息
            addItemView(getString(R.string.printer_term_num), BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41), false);
            addItemView(getString(R.string.tip_sn), CommonUtils.getSn(), false);
            addItemView(getString(R.string.tip_version),
                    PackageUtils.getInstalledVersionName(EposApplication.getAppContext(), EposApplication.getAppContext().getPackageName()), false);

            if(Settings.bIsSettingBlueTheme()) {
                addDivider(-1,1,getResources().getColor(R.color.common_divider));
            }
            if ("06".equals(mResultPresent.getResponseCode()) || "18".equals(mResultPresent.getResponseCode())) {
                returnBtn.setText(R.string.label_confirm);
            }
        }

        if (mTradePresent.isICInsertTrade()) {
            rootView.findViewById(R.id.tip_take_out).setVisibility(View.VISIBLE);
        }
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.result_info_item, null);
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
            View divider = new View(getActivity());
            divider.setBackgroundColor(getResources().getColor(R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if( !Settings.bIsSettingBlueTheme() ) {
            setTitle(R.string.title_result);
        }
        hideBackBtn();
    }

    @Override
    protected ITradePresent newTradePresent() {
        ResultPresent resultPresent = new BaseResultPresent(this);
        mResultPresent = resultPresent;
        return resultPresent;
    }

    @Override
    public void onClick(View v) {
        /*BUGID:0003158
         *在下一个fragment界面显示的时候上一个fragment界面还没被销毁，
         * 获取到返回键导致上个界面句柄使用异常
         * */
        mTradePresent.onConfirm(v);
    }

    /*
    * 交易结果界面按返回键导致app崩溃
    *BUGID:0002218
    * @author zhouzhihua
    * */
    @Override
    public boolean onBacKeyPressed() {
        return true;
    }
}
