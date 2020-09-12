package com.centerm.epos.fragment.trade;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.GtBannerBean;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.present.transaction.InputPasswordPresent;
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

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */

public class InputPwdFragment extends BaseTradeFragment {

    private CheckBox[] indicatorArr;
    private TextView transType, transAmt, transCardNo;
    private EditText mEtPw;
    private InputPasswordPresent mInputPasswordPresent;
    private String amt,cardNo;
    private final static int AuthCheck = 1;
    private final static int OrderSync = 2;
    private String orderNo = "";
    private int isPay = 1;//0代付 1不代付

    @Override
    protected ITradePresent newTradePresent() {
        InputPasswordPresent inputPasswordPresent = new InputPasswordPresent(this);
        mInputPasswordPresent = inputPasswordPresent;
        return inputPasswordPresent;
    }

    @Override
    protected void afterInitView() {

        String transCode = mTradePresent.getTradeCode();
        if(transCode.equals(TransCode.SALE)){
            authCheck();
        }else{
            mTradePresent.beginTransaction();
        }

    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_input_pwd;
    }

    @Override
    public void onInitView(View rootView) {
        hideBackBtn();
        initFinishBtnlistener(rootView);
        /*@author zhouzhihua 2017.11.28
        *修复接触式IC交易，在密码界面点击返回，导致的各种异常问题
        * 在密码界面是无法响应返回键，故隐藏该按键
        * */
        ImageButton button = (ImageButton) rootView.findViewById(R.id.imgbtn_back);
        if(button!=null){
            button.setVisibility(View.GONE);
        }

        indicatorArr = new CheckBox[]{
                (CheckBox) rootView.findViewById(R.id.indicator1),
                (CheckBox) rootView.findViewById(R.id.indicator2),
                (CheckBox) rootView.findViewById(R.id.indicator3),
                (CheckBox) rootView.findViewById(R.id.indicator4),
                (CheckBox) rootView.findViewById(R.id.indicator5),
                (CheckBox) rootView.findViewById(R.id.indicator6)};
        transType = (TextView) rootView.findViewById(R.id.trans_type_show);
        transAmt = (TextView) rootView.findViewById(R.id.trans_money_show);
        transCardNo = (TextView) rootView.findViewById(R.id.trans_card_show);
        mTvShowTimeOut = (TextView) rootView.findViewById(R.id.mTvShowTimeOut);
        transType.setText(mTradePresent.getTradeName());
        amt = DataHelper.formatAmountForShow(mTradePresent.getTransData(TradeInformationTag.TRANS_MONEY));
        if (mInputPasswordPresent.isShowAmount()) {
            transAmt.setText(amt+"元");
        } else {
            rootView.findViewById(R.id.trans_money_block).setVisibility(View.GONE);
        }
        String cardNum = mTradePresent.getTransData(TradeInformationTag.BANK_CARD_NUM);
        cardNo = cardNum;
        if (!TextUtils.isEmpty(cardNum) && !TransCode.RESERVATION_SALE.equals(mTradePresent.getTradeCode())){
            transCardNo.setText(mInputPasswordPresent.isShowBankCardNum()?DataHelper.formatCardno(cardNum):cardNum);
        }
        /*
         * @author zhouzhihua
         *
         */
        setTitlePicture(rootView,R.drawable.pic_password);

        if(TransCode.VOID.equals(mTradePresent.getTradeCode())){
            rootView.findViewById(R.id.mIvTip).setVisibility(View.INVISIBLE);
        }
    }

    public void authCheck(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.bankNo, mTradePresent.getTransData(TradeInformationTag.BANK_CARD_NUM));
        dataMap.put(JsonKeyGT.idNm, mTradePresent.getTransData(JsonKeyGT.name));
        dataMap.put(JsonKeyGT.idNo, mTradePresent.getTransData(JsonKeyGT.idNo));
        dataMap.put(JsonKeyGT.customList, mTradePresent.getTransData().get(JsonKeyGT.customList));
        sendData(true, TransCode.authCheck, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBannerBean bean = (GtBannerBean) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getCode())){
                        isPay = 1;
                        goNext();
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
        DialogFactory.showTipDialog(getActivity(), "", "卡号与登录人主体不一致，请确认是否继续", new TipDialog.ButtonClickListener() {
            @Override
            public void onClick(TipDialog.ButtonType button, View v) {
                if(button==TipDialog.ButtonType.POSITIVE){
                    goNext();
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

    private void goNext(){
        mTradePresent.getTransData().put(JsonKeyGT.isPay, isPay);
        orderNo = creatOrderNo();
        mTradePresent.getTransData().put(TradeInformationTag.TRANSFER_INTO_CARD, orderNo);
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
        dataMap.put(JsonKeyGT.termSn, CommonUtils.getSn());
        for(GtBusinessListBean.MoneyDetailListBean moneyDetailBean:data.getMoneyDetailList()){
            if(moneyDetailBean.isChecked()){
                //新增监管及面积补差参数
                dataMap.put(JsonKeyGT.superviseFlag, moneyDetailBean.getSuperviseFlag());
                dataMap.put(JsonKeyGT.area, moneyDetailBean.getArea());
                dataMap.put(JsonKeyGT.areaCode, moneyDetailBean.getAreaCode());
                dataMap.put(JsonKeyGT.contractNo, moneyDetailBean.getContractNo());
                break;
            }
        }
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
                    detail.put(JsonKeyGT.paymentName, moneyDetailBean.getPaymentName());

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
        boolean isDf = isPay==1;
        sendData(isDf, TransCode.orderSync, dataMap, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> resultMap) {
                if(resultMap!=null){
                    GtBean2 bean = (GtBean2) resultMap.get(JsonKeyGT.returnData);
                    if("0".equals(bean.getRespCode())){
                        showingTimeout(mTvShowTimeOut, new OnTimeOutListener() {
                            @Override
                            public void onCall(int time) {
                                if(time==0){
                                    cancelTimeout();
                                    mInputPasswordPresent.onPbocTradeTerminated();
                                    getHostActivity().jumpToResultActivity(StatusCode.PIN_TIMEOUT);
                                }
                            }
                        });
                        if(CommonUtils.isK9()){
                            mTradePresent.beginTransaction();
                        }else {

                        }
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
    public void refresh() {
        changeIndicator(mInputPasswordPresent.getPinLen());
    }

    private void changeIndicator(int pinLen) {
        switch (pinLen) {
            case 0:
                for (int i = 0; i < 6; i++) {
                    indicatorArr[i].setChecked(false);
                }
                break;
            case 1:
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 6:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        //防止界面切换到通讯界面瞬间，响应了返回键，导致Activity被销毁，进而出现异常
        return true;
    }

    @Override
    public boolean onBacKeyPressed() {//实体键取消
        //防止界面切换到通讯界面瞬间，响应了返回键，导致Activity被销毁，进而出现异常
        logger.info("onBacKeyPressed");
        if(!CommonUtils.isK9()){
            popToast("交易取消");
            getHostActivity().finish();
        }
        return true;
    }



    @Override
    public void onResume() {
        super.onResume();
        setTitle("输入密码");
    }

    @Override
    public void onDestroy() {
        cancelTimeout();
        mInputPasswordPresent.unbind();
        super.onDestroy();
    }
}
