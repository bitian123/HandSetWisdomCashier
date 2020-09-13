package com.centerm.epos.fragment.trade;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.fragment.LoginFragment;
import com.centerm.epos.present.transaction.CheckCardPresent;
import com.centerm.epos.present.transaction.ICheckCard;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.OnTimeOutListener;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.TipDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/2/15.
 * 检卡界面，读取卡片信息，卡号确认
 */

public class CheckCardFragment extends BaseTradeFragment {
    private static final String TAG = CheckCardFragment.class.getSimpleName();

    //检卡相关的业务逻辑操作接口
    protected CheckCardPresent mCheckCardPresent;
    private TextView mTvPayAmt,mTvReceivable,mTvReceived,mTvUnpaidAmount;
    private TextView mTvShowAmtTip1,mTvShowAmtTip2,mTvShowName;
    private ImageView mIvTip;
    private LinearLayout mViewShowAmmt;
    private final static int AuthCheck = 1;
    private final static int OrderSync = 2;
    private String orderNo = "";
    private int isPay = 1;//0代付 1不代付

    public CheckCardFragment() {

    }

    @Override
    protected ITradePresent newTradePresent() {
        CheckCardPresent present = (CheckCardPresent) super.newTradePresent();
        if (present == null) {
            present = new CheckCardPresent(this);
        }
        mCheckCardPresent = present;
        return present;
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_check_card;
    }

    @Override
    public void onInitView(View rootView) {
        initFinishBtnlistener(rootView);
        mIvTip = rootView.findViewById(R.id.mIvTip);
        mViewShowAmmt = rootView.findViewById(R.id.mViewShowAmmt);
        mTvPayAmt = (TextView) rootView.findViewById(R.id.mTvPayAmt);
        mTvReceivable = (TextView) rootView.findViewById(R.id.mTvReceivable);
        mTvReceived = (TextView) rootView.findViewById(R.id.mTvReceived);
        mTvUnpaidAmount = (TextView) rootView.findViewById(R.id.mTvUnpaidAmount);
        mTvShowName = (TextView) rootView.findViewById(R.id.mTvShowName);
        if(mTradePresent.getTransData().get(TradeInformationTag.TRANS_MONEY)==null){
            mTvPayAmt.setText("0.00"+"元");
            mTvReceivable.setText("0.00"+"元");
            mTvReceived.setText("0.00"+"元");
            mTvUnpaidAmount.setText("0.00"+"元");
            mTvShowName.setText("姓名:"+"未知");
        }else{
            mTvPayAmt.setText(mTradePresent.getTransData().get(TradeInformationTag.TRANS_MONEY)+"元");
            mTvReceivable.setText(mTradePresent.getTransData().get(TradeInformationTag.totalReceivable)+"元");
            mTvReceived.setText(mTradePresent.getTransData().get(TradeInformationTag.totalReceived)+"元");
            mTvUnpaidAmount.setText(mTradePresent.getTransData().get(TradeInformationTag.totalUnpaidAmount)+"元");
            mTvShowName.setText("姓名:"+mTradePresent.getTransData().get(JsonKeyGT.checkCardShowName));
        }

        showingTimeout((TextView) rootView.findViewById(R.id.mTvShowTimeOut), new OnTimeOutListener() {
            @Override
            public void onCall(int time) {
                if(time==0){
                    cancelTimeout();
                    mCheckCardPresent.onCancel2();
                    mTradePresent.gotoNextStep("9876");
                }
            }
        });

        if(TransCode.VOID.equals(mTradePresent.getTradeCode())){
            mIvTip.setVisibility(View.GONE);
            mViewShowAmmt.setVisibility(View.GONE);
            mTvShowName.setVisibility(View.GONE);
            rootView.findViewById(R.id.mViewLine).setVisibility(View.GONE);
        }

    }

    public void authCheck(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.bankNo, mTradePresent.getTransData(TradeInformationTag.BANK_CARD_NUM));
        dataMap.put(JsonKeyGT.idNm, mTradePresent.getTransData(JsonKeyGT.name));
        dataMap.put(JsonKeyGT.idNo, mTradePresent.getTransData(JsonKeyGT.idNo));
        sendData(true, TransCode.authCheck, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBannerBean bean = (GtBannerBean) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){
                        goNext(1);
                    }else if("22".equals(bean.getCode())){
                        isPay = 0;
                        showTip();
                    }else {
                        tipToExit(bean.getMsg()+"\n是否重试", AuthCheck);
                    }
                }else {
                    tipToExit("三要素校验异常，是否重试", AuthCheck);
                }
            }
        });
    }

    private void showTip() {
        DialogFactory.showTipDialog(getActivity(), "温馨提示", "卡号与登录人主体不一致，请确认是否继续", new TipDialog.ButtonClickListener() {
            @Override
            public void onClick(TipDialog.ButtonType button, View v) {
                if(button==TipDialog.ButtonType.POSITIVE){
                    goNext(0);
                }else if(button==TipDialog.ButtonType.NEGATIVE){
                    getHostActivity().finish();
                }
            }
        },true);
    }

    private void tipToExit(String txt, final int type) {
        DialogFactory.showSelectDialog(getActivity(),getString(R.string.tip_notification), txt, new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        if(type==AuthCheck){
                            authCheck();
                        }else if(type==OrderSync){
                            orderSync();
                        }
                        break;
                    case NEGATIVE:
                        mTradePresent.gotoNextStep("999");
                        break;
                }
            }
        });
    }

    private void goNext(int isPay){
        mTradePresent.getTransData().put(JsonKeyGT.isPay, isPay);
        orderNo = creatOrderNo();
        mTradePresent.getTransData().put(JsonKeyGT.out_order_no, orderNo);
        mTradePresent.getTransData().put(JsonKeyGT.orderNo, orderNo);
        orderSync();
    }

    private void orderSync(){
        GtBusinessListBean data = (GtBusinessListBean) mTradePresent.getTransData().get(JsonKeyGT.BusinessListData);

        //DialogFactory.showLoadingDialog(getActivity(), "正在进行主子订单同步\n请稍后");
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.mainOrderId, orderNo);
        dataMap.put(JsonKeyGT.projectId, data.getProjectId());
        dataMap.put(JsonKeyGT.companyId, data.getCompanyId());
        dataMap.put(JsonKeyGT.projectName, data.getProjectName());
        dataMap.put(JsonKeyGT.merchantId, BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42));
        dataMap.put(JsonKeyGT.name, mTradePresent.getTransData(JsonKeyGT.name));
        dataMap.put(JsonKeyGT.idType, mTradePresent.getTransData(JsonKeyGT.idType));
        dataMap.put(JsonKeyGT.idNo , mTradePresent.getTransData(JsonKeyGT.idNo));
        dataMap.put(JsonKeyGT.cardNo, mTradePresent.getTransData(TradeInformationTag.BANK_CARD_NUM));
        try {
            JSONArray moneyDetailArray = new JSONArray();
            for(GtBusinessListBean.MoneyDetailListBean moneyDetailBean:data.getMoneyDetailList()){
                if(moneyDetailBean.isChecked()){
                    JSONObject detail = new JSONObject();
                    detail.put(JsonKeyGT.isPay, isPay);
                    JSONArray customList = new JSONArray();
                    for(GtBusinessListBean.MoneyDetailListBean.CustomListBean custom:moneyDetailBean.getCustomList()){
                        JSONObject customJson = new JSONObject();
                        customJson.put(JsonKeyGT.name, custom.getName());
                        customJson.put(JsonKeyGT.idType, custom.getIdType());
                        customJson.put(JsonKeyGT.idNo, custom.getIdNo());
                        customList.put(customJson);
                    }
                    detail.put(JsonKeyGT.customList, customList);
                    detail.put(JsonKeyGT.stlNo, moneyDetailBean.getSubjectName());
                    detail.put(JsonKeyGT.businessType, moneyDetailBean.getBusinessType());
                    detail.put(JsonKeyGT.businessTypeCode, moneyDetailBean.getBusinessTypeCode());
                    detail.put(JsonKeyGT.businessId, moneyDetailBean.getBusinessId());
                    detail.put(JsonKeyGT.roomId, moneyDetailBean.getRoomId());
                    detail.put(JsonKeyGT.roomFullName, moneyDetailBean.getRoomFullName());
                    detail.put(JsonKeyGT.paymentPlanId, moneyDetailBean.getPaymentPlanId());
                    detail.put(JsonKeyGT.payMethod, moneyDetailBean.getPayMethod());
                    detail.put(JsonKeyGT.paymentItemName, moneyDetailBean.getPaymentItemName());
                    detail.put(JsonKeyGT.subjectName, moneyDetailBean.getSubjectName());
                    detail.put(JsonKeyGT.billId, moneyDetailBean.getBillId());
                    detail.put(JsonKeyGT.moneyType, moneyDetailBean.getMoneyType());
                    detail.put(JsonKeyGT.receivableDate, moneyDetailBean.getReceivableDate());

                    detail.put(JsonKeyGT.amountReceivable, moneyDetailBean.getAmountReceivable());
                    detail.put(JsonKeyGT.amountReceived, moneyDetailBean.getAmountReceived());
                    detail.put(JsonKeyGT.unpaidAmount, moneyDetailBean.getUnpaidAmount());
                    Double amt = Double.valueOf(moneyDetailBean.getReadyPayAmt());
                    if(amt!=null&&amt>0){
                        detail.put(JsonKeyGT.payAmount, moneyDetailBean.getReadyPayAmt());
                    }else {
                        detail.put(JsonKeyGT.payAmount, moneyDetailBean.getUnpaidAmount());
                    }

                    //新增监管及面积补差参数
                    detail.put(JsonKeyGT.superviseFlag, moneyDetailBean.getSuperviseFlag());
                    detail.put(JsonKeyGT.area, moneyDetailBean.getArea());
                    detail.put(JsonKeyGT.areaCode, moneyDetailBean.getAreaCode());
                    detail.put(JsonKeyGT.contractNo, moneyDetailBean.getContractNo());
                    detail.put(JsonKeyGT.sign, moneyDetailBean.getSign());
                    JSONArray unionList = new JSONArray();
                    if ("1".equals(moneyDetailBean.getSign())) {
                        for (GtBusinessListBean.MoneyDetailListBean.UnionListBean unionListBean : moneyDetailBean.getUnionList()) {
                            //以下为分项数据
                            JSONObject unionObject = new JSONObject();
                            unionObject.put(JsonKeyGT.businessId, unionListBean.getBusinessId());
                            unionObject.put(JsonKeyGT.paymentPlanId, unionListBean.getPaymentPlanId());
                            unionObject.put(JsonKeyGT.businessType, unionListBean.getBusinessType());
                            unionObject.put(JsonKeyGT.businessTypeCode, unionListBean.getBusinessTypeCode());
                            unionObject.put(JsonKeyGT.unpaidAmount, unionListBean.getUnpaidAmount());
                            unionObject.put(JsonKeyGT.payAmount, unionListBean.getUnpaidAmount());
                            unionObject.put(JsonKeyGT.amountReceivable, unionListBean.getAmountReceivable());
                            unionObject.put(JsonKeyGT.amountReceived, unionListBean.getAmountReceived());
                            unionObject.put(JsonKeyGT.payMethod, unionListBean.getPayMethod());
                            unionObject.put(JsonKeyGT.paymentItemName, unionListBean.getPaymentItemName());
                            unionObject.put(JsonKeyGT.paymentName, unionListBean.getPaymentName());
                            unionObject.put(JsonKeyGT.receivableDate, unionListBean.getReceivableDate());
                            unionList.put(unionObject);
                        }
                    }
                    detail.put(JsonKeyGT.unionList, unionList);
                    moneyDetailArray.put(detail);
                }
            }
            dataMap.put(JsonKeyGT.moneyDetailList, moneyDetailArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendData(true, TransCode.orderSync, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBean2 bean = (GtBean2) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getRespCode())){
                        mTradePresent.gotoNextStep();
                    }else {
                        tipToExit(bean.getRespMsg()+"\n是否重试", OrderSync);
                    }
                }else {
                    tipToExit("主子订单同步异常，是否重试", OrderSync);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("银行卡支付");
    }

    @Override
    public void onPause() {
        super.onPause();
        if( TransCode.MAG_ACCOUNT_LOAD.equals(mTradePresent.getTradeCode())
                ||TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(mTradePresent.getTradeCode())
                ||( !getHostActivity().isIccSecondUseCard() && TransCode.EC_LOAD_OUTER.equals(mTradePresent.getTradeCode())) ){
            mTradePresent.onExit();
        }
    }

    @Override
    public void onDestroy() {
        cancelTimeout();
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        mTradePresent.onCancel();
        return false;
    }

    @Override
    public boolean onExistPressed() {
        mTradePresent.onExit();
        return false;
    }

    @Override
    public boolean onBacKeyPressed() {
        mTradePresent.onExit();
        return super.onBacKeyPressed();
    }

}