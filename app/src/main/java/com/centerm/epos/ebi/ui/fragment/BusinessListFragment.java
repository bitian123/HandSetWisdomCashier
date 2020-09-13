package com.centerm.epos.ebi.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.adapter.ObjectBaseAdapter;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.present.BusinessPresent;
import com.centerm.epos.transcation.pos.constant.CommonConstant;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.KeyBoardShowListener;
import com.centerm.epos.utils.MoneyTextWatcher;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.OnEnteListener;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.MyEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

/**
 * 选择缴费款项
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class BusinessListFragment extends BaseTradeFragment implements View.OnClickListener {
    private ImageView mIvTip;
    private CheckBox mCheckBox;
    private ListView mListView;
    private List<GtBusinessListBean.MoneyDetailListBean> list = new ArrayList<>();
    private BusinessAdapter adapter;
    private TextView mAmtDetail, mTotalAmountReceivable, mTotalAmountReceived, mTotalUnpaidAmount, mTvTotalAmt;
    private TextView mTvRoomId;
    private LinearLayout mLlDetail;
    private RelativeLayout mRlTotal;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;
    private KeyBoardShowListener keyBoardShowListener;
    private String amt, totalReceivable, totalReceived, totalUnpaidAmount;
    private BusinessPresent present;
    private GtBusinessListBean data;
    private String projectName = "";
    private GtBusinessListBean.MoneyDetailListBean checkedItem;
    private List<String> HB=new ArrayList<>();
    private String strTemp = "{" +
            "\"companyId\": \"96d550d42d0211e8af88005056b44833\"," +
            "\"moneyDetailList\": [" +
            "{" +
            "\"amountReceivable\": 190.0," +
            "\"amountReceived\": 0.0," +
            "\"area\": \"area1\"," +
            "\"areaCode\": \"areaCode1\"," +
            "\"businessId\": \"businessId1\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"customList\": [" +
            "{" +
            "\"idNo\": \"idNo1\"," +
            "\"idType\": 0," +
            "\"name\": \"name1\"" +
            "}," +
            "{" +
            "\"idNo\": \"idNo2\"," +
            "\"idType\": 0," +
            "\"name\": \"name2\"" +
            "}" +
            "]," +
            "\"payMethod\": \"交付合并收款\"," +
            "\"paymentItemName\": \"交付合并收款\"," +
            "\"paymentName\": \"交付合并收款\"," +
            "\"paymentPlanId\": \"paymentPlanId1\"," +
            "\"receivableDate\": \"2020-07-18\"," +
            "\"roomFullName\": \"roomFullName1\"," +
            "\"roomId\": \"roomId1\"," +
            "\"sign\": \"1\"," +
            "\"subjectName\": \"999\"," +
            "\"superviseFlag\": \"1\"," +
            "\"unionList\": [" +
            "{" +
            "\"amountReceivable\": 210," +
            "\"amountReceived\": 0," +
            "\"businessId\": \"businessId2\"," +
            "\"businessType\": \"认购2\"," +
            "\"businessTypeCode\": \"Subscription2\"," +
            "\"payMethod\": \"按套内面积收取\"," +
            "\"paymentItemName\": \"公共物业维修基金\"," +
            "\"paymentName\": \"公共物业维修基金\"," +
            "\"paymentPlanId\": \"paymentPlanId2\"," +
            "\"receivableDate\": \"2020-07-17\"," +
            "\"unpaidAmount\": 210" +
            "}," +
            "{" +
            "\"amountReceivable\": -10," +
            "\"amountReceived\": 0," +
            "\"businessId\": \"businessId3\"," +
            "\"businessType\": \"认购3\"," +
            "\"businessTypeCode\": \"Subscription3\"," +
            "\"payMethod\": \"一次性付款\"," +
            "\"paymentItemName\": \"补差款\"," +
            "\"paymentName\": \"补差款\"," +
            "\"paymentPlanId\": \"paymentPlanId3\"," +
            "\"receivableDate\": \"2020-07-07\"," +
            "\"unpaidAmount\": -10" +
            "}," +
            "{" +
            "\"amountReceivable\": -10," +
            "\"amountReceived\": 0," +
            "\"businessId\": \"businessId3\"," +
            "\"businessType\": \"认购3\"," +
            "\"businessTypeCode\": \"Subscription3\"," +
            "\"payMethod\": \"一次性付款\"," +
            "\"paymentItemName\": \"补差款\"," +
            "\"paymentName\": \"补差款\"," +
            "\"paymentPlanId\": \"paymentPlanId3\"," +
            "\"receivableDate\": \"2020-07-07\"," +
            "\"unpaidAmount\": -10" +
            "}" +
            "]," +
            "\"unpaidAmount\": 190.0" +
            "}," +
            "{" +
            "\"amountReceivable\": 200000.0," +
            "\"amountReceived\": 0.0," +
            "\"area\": \"area4\"," +
            "\"areaCode\": \"areaCode4\"," +
            "\"businessId\": \"businessId4\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"customList\": [" +
            "{" +
            "\"idNo\": \"idNo4\"," +
            "\"idType\": 0," +
            "\"name\": \"name4\"" +
            "}," +
            "{" +
            "\"idNo\": \"idNo5\"," +
            "\"idType\": 0," +
            "\"name\": \"name5\"" +
            "}" +
            "]," +
            "\"payMethod\": \"定金\"," +
            "\"paymentItemName\": \"定金\"," +
            "\"paymentName\": \"定金\"," +
            "\"paymentPlanId\": \"paymentPlanId4\"," +
            "\"receivableDate\": \"2020-07-18\"," +
            "\"roomFullName\": \"roomFullName4\"," +
            "\"roomId\": \"roomId4\"," +
            "\"sign\": \"0\"," +
            "\"subjectName\": \"222222222222222222\"," +
            "\"superviseFlag\": \"0\"," +
            "\"unpaidAmount\": 200000.0" +
            "}," +
            "{" +
            "\"amountReceivable\": 200000.0," +
            "\"amountReceived\": 0.0," +
            "\"area\": \"area6\"," +
            "\"areaCode\": \"areaCode6\"," +
            "\"businessId\": \"businessId6\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"customList\": [" +
            "{" +
            "\"idNo\": \"idNo6\"," +
            "\"idType\": 0," +
            "\"name\": \"name6\"" +
            "}" +
            "]," +
            "\"payMethod\": \"定金\"," +
            "\"paymentItemName\": \"定金\"," +
            "\"paymentName\": \"定金\"," +
            "\"paymentPlanId\": \"paymentPlanId6\"," +
            "\"receivableDate\": \"2020-07-18\"," +
            "\"roomFullName\": \"roomFullName6\"," +
            "\"roomId\": \"roomId6\"," +
            "\"sign\": \"1\"," +
            "\"subjectName\": \"222222222222222222\"," +
            "\"superviseFlag\": \"0\"," +
            "\"unionList\": [" +
            "{" +
            "\"amountReceivable\": 210000," +
            "\"amountReceived\": 0," +
            "\"businessId\": \"businessId7\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"payMethod\": \"按套内面积收取\"," +
            "\"paymentItemName\": \"公共物业维修基金\"," +
            "\"paymentName\": \"公共物业维修基金\"," +
            "\"paymentPlanId\": \"paymentPlanId7\"," +
            "\"receivableDate\": \"2020-07-17\"," +
            "\"unpaidAmount\": 210000" +
            "}," +
            "{" +
            "\"amountReceivable\": -10000," +
            "\"amountReceived\": 0," +
            "\"businessId\": \"businessId8\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"payMethod\": \"一次性付款\"," +
            "\"paymentItemName\": \"补差款\"," +
            "\"paymentName\": \"补差款\"," +
            "\"paymentPlanId\": \"paymentPlanId8\"," +
            "\"receivableDate\": \"2020-07-07\"," +
            "\"unpaidAmount\": -10000" +
            "}" +
            "]," +
            "\"unpaidAmount\": 200000.0" +
            "}," +
            "{" +
            "\"amountReceivable\": 200000.0," +
            "\"amountReceived\": 0.0," +
            "\"area\": \"area9\"," +
            "\"areaCode\": \"areaCode9\"," +
            "\"businessId\": \"businessId9\"," +
            "\"businessType\": \"认购\"," +
            "\"businessTypeCode\": \"Subscription\"," +
            "\"customList\": [" +
            "{" +
            "\"idNo\": \"idNo9\"," +
            "\"idType\": 0," +
            "\"name\": \"name9\"" +
            "}" +
            "]," +
            "\"payMethod\": \"定金\"," +
            "\"paymentItemName\": \"定金\"," +
            "\"paymentName\": \"定金\"," +
            "\"paymentPlanId\": \"paymentPlanId9\"," +
            "\"receivableDate\": \"2020-07-18\"," +
            "\"roomFullName\": \"roomFullName9\"," +
            "\"roomId\": \"roomId9\"," +
            "\"sign\": \"0\"," +
            "\"subjectName\": \"222222222222222222\"," +
            "\"superviseFlag\": \"0\"," +
            "\"unpaidAmount\": 200000.0" +
            "}" +
            "]," +
            "\"projectId\": \"f2720bca97da11e9b6017cd30ab8ab74\"," +
            "\"projectName\": \"挡板测试项目\"," +
            "\"respCode\": \"0\"," +
            "\"respMsg\": \"SUCCESS\"" +
            "}";

    @Override
    protected ITradePresent newTradePresent() {
        present = new BusinessPresent(this);
        return present;
    }
    private boolean checkOrder(int arg){
        GtBusinessListBean.MoneyDetailListBean moneyDetailListBean = list.get(arg);
        int  falg=0;//黑色
       if ( moneyDetailListBean.getSuperviseFlag().equals("1")){
           falg=1;//红色
       }else if ("1".equals(moneyDetailListBean.getSign())){
           falg=2;//黄色
       }
       if (HB.size()>0){
           final GtBusinessListBean.MoneyDetailListBean moneyDetailListBean1 = list.get(Integer.parseInt(HB.get(0)));
           List<GtBusinessListBean.MoneyDetailListBean.CustomListBean> customList = moneyDetailListBean.getCustomList();
           List<GtBusinessListBean.MoneyDetailListBean.CustomListBean> customList1 = moneyDetailListBean1.getCustomList();
           if (customList.size() !=customList1.size()){
               return false;
           }
           List<String> customs=new ArrayList<>();
           List<String> loctCustoms=new ArrayList<>();

           for (GtBusinessListBean.MoneyDetailListBean.CustomListBean customListBean:customList) {
               customs.add(customListBean.getIdNo()+customListBean.getName());
           }
           for (GtBusinessListBean.MoneyDetailListBean.CustomListBean bean:customList1) {
               loctCustoms.add( bean.getIdNo()+bean.getName());
           }
           for (String custom:customs) {
                if (!loctCustoms.contains(custom)){
                    return false;
                }
           }
           int  falg1=0;
           if ( moneyDetailListBean1.getSuperviseFlag().equals("1")){
               falg1=1;
           }else if ("1".equals(moneyDetailListBean1.getSign())){
               falg1=2;
           }

               switch (falg1){
                   case 0:
                   case 2:
                      if (falg == 0|| falg == 2){
                          HB.add(arg+"");
                          return  true;
                      }else{
                          return  false;
                      }

                   case 1:
                       return false;

                   default:
                       return false;

               }
       }else{
           HB.add(arg+"");
           return  true;
       }

    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        view.findViewById(R.id.imgbtn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTradePresent.gotoNextStep("2");
            }
        });
        mCheckBox = (CheckBox) view.findViewById(R.id.mCheckBox);
        mListView = (ListView) view.findViewById(R.id.mListView);
        mLlDetail = (LinearLayout) view.findViewById(R.id.mLlDetail);
        mRlTotal = (RelativeLayout) view.findViewById(R.id.mRlTotal);
        mTotalAmountReceivable = (TextView) view.findViewById(R.id.mTotalAmountReceivable);
        mTotalAmountReceived = (TextView) view.findViewById(R.id.mTotalAmountReceived);
        mTotalUnpaidAmount = (TextView) view.findViewById(R.id.mTotalUnpaidAmount);
        mAmtDetail = (TextView) view.findViewById(R.id.mAmtDetail);
        mTvTotalAmt = (TextView) view.findViewById(R.id.mTvTotalAmt);
        mTvRoomId = (TextView) view.findViewById(R.id.mTvRoomId);
        mAmtDetail.setOnClickListener(this);
        view.findViewById(R.id.mTvCardSale).setOnClickListener(this);

        //非身份证验证进入
        if (mTradePresent.getTransData().get("isOther") != null) {
            mIvTip = (ImageView) view.findViewById(R.id.mIvTip);
            mIvTip.setBackground(getActivity().getResources().getDrawable(R.drawable.auth_step_6));
        }

        onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (list != null && list.size() > 0) {
                    if (checkedItem == null) {
                        for (int i = 0; i < list.size(); i++) {
                            if (!present.isEmpty(list.get(i).getSubjectName())) {
                                checkedItem = list.get(i);
                                break;
                            }
                        }
                    }
                    boolean isContainSuper = false;
                    boolean isInconsistent = false;
                    for (int i = 0; i < list.size(); i++) {
                        if (b) {
                            if ("1".equals(checkedItem.getSuperviseFlag()) || "1".equals(list.get(i).getSuperviseFlag())) {
                                isContainSuper = true;
                            } else {
                                if (!present.isEmpty(list.get(i).getSubjectName()) && present.checkMsg(list.get(i), checkedItem)) {
                                    list.get(i).setChecked(true);
                                }else{
                                    isInconsistent = true;
                                }
                            }

                        } else {
                            list.get(i).setChecked(b);
                        }
                    }
                    if(isInconsistent){
                        popToast("缴费款项信息不一致，无法同时缴费！");
                    }
                    if(isContainSuper){
                        popToast("包含监管项不支持合并支付");
                        for(int a =0 ;a<list.size(); a++){
                            list.get(a).setChecked(false);
                            HB.clear();
                        }
                        mCheckBox.setChecked(false);
                    }
                } else {
                    mRlTotal.setVisibility(View.GONE);
                }
                updateList();
            }
        };
        mCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);

        initKeyBoardShowListener();
        unpaidQuery();
    }

    private void initKeyBoardShowListener() {
        keyBoardShowListener = new KeyBoardShowListener(getActivity());
        keyBoardShowListener.setKeyboardListener(new KeyBoardShowListener.OnKeyboardVisibilityListener() {
            @Override
            public void onVisibilityChanged(boolean visible) {
                if (!visible) {
                    updateList();
                }
            }
        }, getActivity());
    }

    private void initListView() {
        adapter = new BusinessAdapter(getActivity());
        adapter.addAll(list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                boolean pre = list.get(position).isChecked();
                if (!pre){
                    if (!checkOrder(position)){
                        popToast("缴费款项信息不一致，无法同时缴费！");
                        list.get(position).setChecked(false);
                        HB.remove(position+"");
                    }else{
                        list.get(position).setChecked(true);
                    }
                }else{
                    list.get(position).setChecked(false);
                    HB.remove(position+"");
                }

                //showDetailDialog(i);
             /*   boolean pre = list.get(i).isChecked();
                if (!pre) {
                    if (present.isEmpty(list.get(i).getSubjectName())) {
                        popToast("本账单需联系工作人员处理后方可收款！");
                        return;
                    }
                    if (checkedItem == null) {
                        checkedItem = list.get(i);
                    } else {
                        if("1".equals(checkedItem.getSuperviseFlag())){
                            popToast("监管项不支持合并支付");
                            return;
                        }else{
                            if ("0".equals(list.get(i).getSuperviseFlag())) {
                                if (!present.checkMsg(list.get(i), checkedItem)) {
                                    popToast("缴费款项信息不一致，无法同时缴费！");
                                    return;
                                }
                            } else {
                                popToast("监管项不支持合并支付");
                                return;
                            }
                        }

                    }
                }
                list.get(i).setChecked(!pre);*/
                updateList();
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //当不滚动时
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
                        params.addRule(RelativeLayout.ABOVE, R.id.mRlTotal);
                        mListView.setLayoutParams(params); //使layout更新
                    } else {
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mListView.getLayoutParams();
                        params.addRule(RelativeLayout.ABOVE, R.id.mView);
                        mListView.setLayoutParams(params); //使layout更新
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
    }

    private void unpaidQuery() {
        mTradePresent.getTransData().put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        mTradePresent.getTransData().put(JsonKeyGT.termSn, GetRequestData.getSn());
        sendData(true, TransCode.unpaidQuery, mTradePresent.getTransData(), new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if (result != null) {
                    data = (GtBusinessListBean) result.get(JsonKeyGT.returnData);
//                    Gson gson = new Gson();
//                    data = gson.fromJson(strTemp, new TypeToken<GtBusinessListBean>() {}.getType());
                    projectName = data.getProjectName();
                    mTvRoomId.setText(projectName);
                    if ("0".equals(data.getRespCode())) {
                        list = data.getMoneyDetailList();
                        initListView();
                    } else {
                        popToast(data.getRespMsg());
                    }
                } else {
                    popToast("通讯异常，请重试");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            return;
        }
        if (v.getId() == R.id.mAmtDetail) {
            if (mLlDetail.getVisibility() == View.VISIBLE) {
                mLlDetail.setVisibility(View.GONE);
                Drawable drawable = getActivity().getResources().getDrawable(R.drawable.business_icon1);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mAmtDetail.setCompoundDrawables(null, null, drawable, null);
            } else {
                mLlDetail.setVisibility(View.VISIBLE);
                Drawable drawable = getActivity().getResources().getDrawable(R.drawable.business_icon2);
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                mAmtDetail.setCompoundDrawables(null, null, drawable, null);
            }
        } else if (v.getId() == R.id.mTvCardSale) {
            String amt = mTvTotalAmt.getText().toString().trim().replace("元", "");
            if ("0.00".equals(amt)) {
                popToast("付款金额不能为0");
                return;
            }
            boolean isHangzhouJg = false;
            boolean isSuperVesionCheck = false;
            for (GtBusinessListBean.MoneyDetailListBean bean : data.getMoneyDetailList()) {
                if (bean.isChecked()) {
                    StringBuilder builder = new StringBuilder();
                    StringBuilder builderIdNo = new StringBuilder();
                    for (GtBusinessListBean.MoneyDetailListBean.CustomListBean custom : bean.getCustomList()) {
                        builder.append(" " + custom.getName());
                        builderIdNo.append(" " + custom.getIdNo());
                    }
                    mTradePresent.getTransData().put(JsonKeyGT.checkCardShowName, builder.toString());
                    mTradePresent.getTransData().put(JsonKeyGT.checkCardShowIdNo, builderIdNo.toString());
                    mTradePresent.getTransData().put(JsonKeyGT.subjectName, bean.getSubjectName());
                    mTradePresent.getTransData().put(TradeInformationTag.SUPERVISE_FLAG, bean.getSuperviseFlag());
                    mTradePresent.getTransData().put(TradeInformationTag.AREA_CODE, bean.getAreaCode());
                    logger.info("结算帐号" + bean.getSubjectName());
                    logger.info("监管地区码：" + bean.getAreaCode());
                    mTradePresent.getTransData().put(JsonKeyGT.customList, bean.getCustomList());
                    if(bean.getAreaCode()!=null){
                        if(CommonConstant.AreaCode.HANGZHOU_CODE.equals(bean.getAreaCode())){
                            isHangzhouJg = true;
                        }
                    }
                    if(bean.getSuperviseFlag()!=null){
                        if("1".equals(bean.getSuperviseFlag())){
                            isSuperVesionCheck = true;
                        }
                    }
                }
            }
            mTradePresent.getTransData().put(TradeInformationTag.TEMPLATE_ID, data.getTemplateId());
            mTradePresent.getTransData().put(JsonKeyGT.BusinessListData, data);
            String additionalData = present.getAdditionalPrintData(data);
            mTradePresent.getTransData().put(JsonKeyGT.additionalData, additionalData);
            mTradePresent.getTransData().put(TradeInformationTag.UNICOM_SCAN_TYPE, additionalData);
            mTradePresent.getTransData().put(TradeInformationTag.TRANS_MONEY, amt);
            mTradePresent.getTransData().put(TradeInformationTag.totalReceivable, totalReceivable);
            mTradePresent.getTransData().put(TradeInformationTag.totalReceived, totalReceived);
            mTradePresent.getTransData().put(TradeInformationTag.totalUnpaidAmount, totalUnpaidAmount);

            if(isSuperVesionCheck){
                if(isHangzhouJg){
                    mTradePresent.gotoNextStep("1");
                }else{
                    showMessageDialog(R.string.tip_dialog_title, R.string.tip_super_flag_wrong, new AlertDialog.ButtonClickListener() {
                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            DialogFactory.hideAll();
                        }
                    });
                }
            }else{
                mTradePresent.gotoNextStep("3");
            }

        } else {
            super.onClick(v);
        }
    }

    @Override
    public boolean onBacKeyPressed() {
        logger.info("实体键返回");
        tipToExit();
        return true;
    }

    private void tipToExit() {
        DialogFactory.showSelectDialog(getActivity(), getString(com.centerm.epos.R.string.tip_notification), "确认退出业务？", new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        HB.clear();
                        getHostActivity().finish();
                        break;
                }
            }
        });
    }

    private void updateList() {
        boolean isAll = true;
        boolean showRl = false;
        double payAmt = 0;
        double amountReceivable = 0;
        double amountReceived = 0;
        double unpaidAmount = 0;
        if (list != null && list.size() > 0) {
            for (GtBusinessListBean.MoneyDetailListBean bean : list) {
                if (bean.isChecked()) {
                    amountReceivable += bean.getAmountReceivable();
                    amountReceived += bean.getAmountReceived();
                    unpaidAmount += bean.getUnpaidAmount();
                    if (bean.getReadyPayAmt() > 0) {
                        payAmt += bean.getReadyPayAmt();
                    } else {
                        payAmt += bean.getUnpaidAmount();
                    }
                    showRl = true;
                } else {
                    isAll = false;
                }
            }
            adapter.notifyDataSetChanged();
        }
//        if(!isAll){
//            mCheckBox.setOnCheckedChangeListener(null);
//            mCheckBox.setChecked(false);
//            mCheckBox.setOnCheckedChangeListener(onCheckedChangeListener);
//        }
        if (showRl) {
            mRlTotal.setVisibility(View.VISIBLE);
        } else {
            mRlTotal.setVisibility(View.GONE);
            checkedItem = null;
        }

        amt = DataHelper.saved2Decimal(payAmt);
        totalReceivable = DataHelper.saved2Decimal(amountReceivable);
        totalReceived = DataHelper.saved2Decimal(amountReceived);
        totalUnpaidAmount = DataHelper.saved2Decimal(unpaidAmount);
        mTvTotalAmt.setText(amt + "元");
        mTotalAmountReceivable.setText("应收总金额: " + totalReceivable + "元");
        mTotalAmountReceived.setText("已收总金额: " + totalReceived + "元");
        mTotalUnpaidAmount.setText("本次应收总金额: " + totalUnpaidAmount + "元");
    }

    private class BusinessAdapter extends ObjectBaseAdapter<GtBusinessListBean.MoneyDetailListBean> {
        public BusinessAdapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final GtBusinessListBean.MoneyDetailListBean bean = getItem(position);
            final ViewHolder holder;
//            if (convertView == null) {
                int layoutId = R.layout.v_business_item;
                convertView = getHostActivity().getLayoutInflater().inflate(layoutId, null);
                holder = new ViewHolder();
                holder.mTvSettlement = (TextView) convertView.findViewById(R.id.mTvSettlement);
                holder.mMoneyType = (TextView) convertView.findViewById(R.id.mMoneyType);
                holder.mBillId = (TextView) convertView.findViewById(R.id.mBillId);
                holder.mName = (TextView) convertView.findViewById(R.id.mName);
                holder.mUnpaidAmount = (TextView) convertView.findViewById(R.id.mUnpaidAmount);
                holder.mEtAmt = (TextView) convertView.findViewById(R.id.mEtAmt);
                holder.mBtnClear = (ImageView) convertView.findViewById(R.id.mBtnClear);
                holder.mTvDeail = (TextView) convertView.findViewById(R.id.mTvDeail);
                holder.mTvTip2 = (TextView) convertView.findViewById(R.id.mTvTip2);
                holder.mCbItem = (CheckBox) convertView.findViewById(R.id.mCbItem);
                holder.mTvTip1 = convertView.findViewById(R.id.mTvTip1);
                convertView.setTag(holder);
//            }
//            else {
//                holder = (ViewHolder) convertView.getTag();
//            }
            // 监管项目颜色设置为红色
            if ("1".equals(bean.getSuperviseFlag())) {
                holder.mBillId.setTextColor(Color.RED);
                holder.mMoneyType.setTextColor(Color.RED);
                holder.mName.setTextColor(Color.RED);
                holder.mTvTip1.setTextColor(Color.RED);
                holder.mTvTip2.setTextColor(Color.RED);
                holder.mUnpaidAmount.setTextColor(Color.RED);
                holder.mEtAmt.setTextColor(Color.RED);
                holder.mTvSettlement.setTextColor(Color.RED);
                if("1".equals(bean.getSign())){
                    holder.mTvTip2.setVisibility(View.GONE);
                }
            }else if ("1".equals(bean.getSign())) {
                holder.mBillId.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mMoneyType.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mName.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mTvTip1.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mTvTip2.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mUnpaidAmount.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mEtAmt.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mTvSettlement.setTextColor(getResources().getColor(R.color.color_yellow));
                holder.mEtAmt.setClickable(false);
                holder.mTvTip2.setVisibility(View.GONE);
            }else{
                holder.mBillId.setTextColor(Color.BLACK);
                holder.mMoneyType.setTextColor(Color.BLACK);
                holder.mName.setTextColor(Color.BLACK);
                holder.mTvTip1.setTextColor(Color.BLACK);
                holder.mTvTip2.setTextColor(Color.BLACK);
                holder.mUnpaidAmount.setTextColor(Color.BLACK);
                holder.mEtAmt.setTextColor(Color.BLACK);
                holder.mTvSettlement.setTextColor(getResources().getColor(R.color.font_tip));
            }
            holder.mMoneyType.setText(bean.getPaymentItemName());
            holder.mBillId.setText(bean.getRoomFullName());
            StringBuilder builder = new StringBuilder("姓名:");
            for (GtBusinessListBean.MoneyDetailListBean.CustomListBean custom : bean.getCustomList()) {
                builder.append(" " + custom.getName());
            }
            holder.mName.setText(builder.toString());
            holder.mTvSettlement.setText("结算账户: " + bean.getSubjectName());
            //合并项目要合并金额
            if ("1".equals(bean.getSign())) {
                List<GtBusinessListBean.MoneyDetailListBean.UnionListBean> list = bean.getUnionList();
                double amountSum = 0;
                for (GtBusinessListBean.MoneyDetailListBean.UnionListBean unionListBean : bean.getUnionList()) {
                    amountSum += unionListBean.getAmountReceivable();
                }
                holder.mUnpaidAmount.setText("本次应收: " + DataHelper.saved2Decimal(amountSum) + "元");
            } else {
                holder.mUnpaidAmount.setText("本次应收: " + DataHelper.saved2Decimal(bean.getAmountReceivable()) + "元");
            }


            Double amt = Double.valueOf(bean.getReadyPayAmt());
            if (amt != null && amt > 0) {
                holder.mEtAmt.setText(DataHelper.saved2Decimal(bean.getReadyPayAmt()) );
            } else {
                //合并项目要合并金额
                if ("1".equals(bean.getSign())) {
                    double amountSum = 0;
                    for (GtBusinessListBean.MoneyDetailListBean.UnionListBean unionListBean : bean.getUnionList()) {
                        amountSum += unionListBean.getUnpaidAmount();
                    }
                    holder.mEtAmt.setText(DataHelper.saved2Decimal(amountSum));
                } else {
                    holder.mEtAmt.setText(DataHelper.saved2Decimal(bean.getUnpaidAmount()));
                }

            }

            holder.mCbItem.setChecked(bean.isChecked());
            holder.mCbItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!b){
                          HB.remove(position+"");
                    }else{
                        list.get(position).setChecked(checkOrder(position));
                    }

                }
            });
            holder.mTvDeail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if("1".equals(list.get(position).getSign())){
                        showDetailDialog(position,2);
                    }else{
                        showDetailDialog(position,1);
                    }

                }
            });
            holder.mTvTip2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if("1".equals(list.get(position).getSign())){
                        showDetailDialog(position,2);
                    }else{
                        showDetailDialog(position,1);
                    }
//                    holder.mEtAmt.setFocusableInTouchMode(true);
//                    holder.mEtAmt.setFocusable(true);
//                    holder.mEtAmt.requestFocus();
//                    holder.mEtAmt.setSelection(holder.mEtAmt.getText().toString().length());
//                    holder.mBtnClear.setVisibility(View.VISIBLE);
//                    ViewUtils.showKeyBoard(getActivity());
                }
            });
            holder.mEtAmt.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if (KeyEvent.KEYCODE_ENTER == keyCode) {
                        String amtStr = holder.mEtAmt.getText().toString().trim();
                        if (TextUtils.isEmpty(amtStr)) {
                            popToast("请输入金额");
                            return true;
                        }
                        if (amtStr.endsWith(".")) {
                            amtStr = amtStr.replace(".", "");
                        }
                        double amt = Double.parseDouble(amtStr);
                        if (amt == 0) {
                            popToast("请输入金额");
                            return true;
                        }
                        if (amt > bean.getUnpaidAmount()) {
                            popToast("本次付款金额不能大于本次应收金额");
                            return true;
                        }
                        list.get(position).setReadyPayAmt(amt);

                        if (!checkOrder(position)){
                            popToast("缴费款项信息不一致，无法同时缴费！");
                            list.get(position).setChecked(false);
                            holder.mCbItem.setChecked(false);
                              HB.remove(position+"");
                        }else{
                            list.get(position).setChecked(true);
                            holder.mCbItem.setChecked(true);
                        }
                        ViewUtils.hintKeyBoard(getActivity());
                        return true;
                    }
                    return false;
                }
            });
            holder.mEtAmt.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (editable != null && editable.length() > 0) {
                        holder.mBtnClear.setVisibility(View.VISIBLE);
                        holder.mBtnClear.setFocusable(true);
                    }
                }
            });
            holder.mBtnClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    logger.error("onClick");
                    holder.mEtAmt.setText("");
                    holder.mEtAmt.setFocusableInTouchMode(true);
                    holder.mEtAmt.setFocusable(true);
                    holder.mEtAmt.requestFocus();
                    //holder.mEtAmt.setSelection(holder.mEtAmt.getText().toString().length());
                    //holder.mBtnClear.setVisibility(View.VISIBLE);
                    ViewUtils.showKeyBoard(getActivity());
                }
            });
            holder.mBtnClear.setVisibility(View.INVISIBLE);
            holder.mEtAmt.setFocusable(false);
            holder.mEtAmt.setFocusableInTouchMode(false);
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView mTvSettlement;
        TextView mMoneyType;
        TextView mBillId;
        TextView mName;
        TextView mUnpaidAmount;
        TextView mEtAmt;
        CheckBox mCbItem;
        TextView mTvDeail;
        TextView mTvTip1;
        TextView mTvTip2;
        ImageView mBtnClear;
    }

    private void showDetailDialog(final int position, final int type) {
        GtBusinessListBean.MoneyDetailListBean bean = list.get(position);
        bean.setProjectName(projectName);
        DialogFactory.showDetailDialog(type,getActivity(), bean, new OnEnteListener() {
            @Override
            public void onEnter(double d) {
                //修改后的金额
                list.get(position).setReadyPayAmt(d);
                update( position);
            }

            @Override
            public void onEnter() {
                //不修改金额
                update( position);

            }
        });
    }

    public void update(final int position){
    /*    boolean actionFlag = true;
        if (present.isEmpty(list.get(position).getSubjectName())) {
            popToast("本账单需联系工作人员处理后方可收款！");
            actionFlag = false;
        }
        if (checkedItem != null && !present.checkMsg(list.get(position), checkedItem)) {
            popToast("缴费款项信息不一致，无法同时缴费！");
            actionFlag = false;
        }
        if (checkedItem == null && actionFlag) {
            checkedItem = list.get(position);
        }
        if (actionFlag) {
            list.get(position).setChecked(true);
        }*/
        boolean checked = list.get(position).isChecked();
        if (!checked){
            if (!checkOrder(position)){
                popToast("缴费款项信息不一致，无法同时缴费！");
                list.get(position).setChecked(false);
                HB.remove(position+"");
            }else{
                list.get(position).setChecked(true);
            }
        }
        updateList();
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_business_list;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("选择缴费款项");
        getHostActivity().closePageTimeout();
    }

    @Override
    public void onPause() {
        super.onPause();
        getHostActivity().openPageTimeout();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (keyBoardShowListener != null) {
            keyBoardShowListener.removeListener();
            keyBoardShowListener = null;
        }
    }
}
