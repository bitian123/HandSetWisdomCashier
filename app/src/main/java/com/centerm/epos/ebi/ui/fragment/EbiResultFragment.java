package com.centerm.epos.ebi.ui.fragment;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.PackageUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.present.DocumentPresent;
import com.centerm.epos.ebi.present.EbiBaseResultPresent;
import com.centerm.epos.ebi.present.EbiResultPresent;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.present.transaction.IResult;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.manager.UploadESignatureTrade;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.view.AlertDialog;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.common.TransDataKey.iso_f39;

/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */

public class EbiResultFragment extends BaseTradeFragment implements View.OnClickListener {
    private static final String TAG = EbiResultFragment.class.getSimpleName();

    private LinearLayout itemContainer;
    private ImageView flagIconShow,mIvTip,mIvTipPic;
    private TextView mBtnNext,mBtnPrint,mTvResultTip,mBtnQuery;
    private IResult mResultPresent;
    private DocumentPresent documentPresent;
    private String errorMsg = "";
    private List<String> printList = new ArrayList<>();
    private int total = 0;
    private int successNum = 0;
    private int failNum = 0;
    private boolean isSuccess = false;

    @Override
    public int onLayoutId() {
        return R.layout.fragment_result_gt;
    }

    @Override
    public void onInitView(View rootView) {
        hideBackBtn();
        initFinishBtnlistener(rootView);
        mIvTip = (ImageView) rootView.findViewById(R.id.mIvTip);
        flagIconShow = (ImageView) rootView.findViewById(R.id.flagIconShow);
        itemContainer = (LinearLayout) rootView.findViewById(R.id.result_info_block);
        mTvShowTimeOut = (TextView) rootView.findViewById(R.id.mTvShowTimeOut);
        mBtnNext = (TextView) rootView.findViewById(R.id.mBtnNext);
        mBtnPrint = (TextView) rootView.findViewById(R.id.mBtnPrint);
        mBtnQuery = (TextView) rootView.findViewById(R.id.mBtnQuery);
        mBtnNext.setOnClickListener(this);
        mBtnPrint.setOnClickListener(this);
        mBtnQuery.setOnClickListener(this);

        //打印
        TradeInfoRecord record = (TradeInfoRecord) mTradePresent.getTransData().get(JsonKeyGT.curTradeInfo);
        if(record!=null&&record.getTransType()!=null){
            if(!TransCode.SALE.equals(record.getTransType())){
                mIvTip.setVisibility(View.GONE);
            }
        }
        if (mTradePresent.isICInsertTrade()) {
            rootView.findViewById(R.id.tip_take_out).setVisibility(View.VISIBLE);
        }

        isSuccess = mResultPresent.isSuccess();
        if (mResultPresent.isSuccess()) {
            if(TransCode.VOID.equals(mTradePresent.getTradeCode())){
                rootView.findViewById(R.id.mIvTip).setVisibility(View.INVISIBLE);
                rootView.findViewById(R.id.mBtnNext).setVisibility(View.INVISIBLE);
                addItemView("本次撤销金额", mTradePresent.getTransData().get(TradeInformationTag.TRANS_MONEY)+"元",false);
                return;
            }
            if(mTradePresent.getTransData().get(JsonKey.PROPERTY_FLAG)!=null){
                //发送交易结果通知回调
                ((EbiBaseResultPresent)mResultPresent).sendNotice();
            }
            String received = "0.00";
            try {
                double received1 = Double.parseDouble((String) mTradePresent.getTransData().get(TradeInformationTag.totalReceived));
                double received2 = Double.parseDouble((String) mTradePresent.getTransData().get(TradeInformationTag.TRANS_MONEY));
                received = DataHelper.formatDouble(received1+received2)+"元";
            }catch (Exception e){}
            if(mTradePresent.getTransData()==null||mTradePresent.getTransData().get(TradeInformationTag.totalReceivable)==null){
                addItemView("应收总金额", "0.00"+"元",false);
            }else{
                addItemView("应收总金额", mTradePresent.getTransData().get(TradeInformationTag.totalReceivable)+"元",false);
            }

            addItemView("已收总金额", received,false);
            if(mTradePresent.getTransData()==null||mTradePresent.getTransData().get(TradeInformationTag.totalUnpaidAmount)==null){
                addItemView("本次应收总金额", "0.00"+"元",false);
            }else{
                addItemView("本次应收总金额", mTradePresent.getTransData().get(TradeInformationTag.totalUnpaidAmount)+"元",false);
            }

            addItemView("本次付款总金额", mTradePresent.getTransData().get(TradeInformationTag.TRANS_MONEY)+"元",false);
            mBtnQuery.setVisibility(View.GONE);
        } else {
            if(CommonUtils.isK9()){
                flagIconShow.setBackgroundResource(R.drawable.result_fail);
            }else {
                mIvTipPic = (ImageView) rootView.findViewById(R.id.mIvTipPic);
                mIvTipPic.setBackgroundResource(R.drawable.result_fail);
                mTvResultTip = (TextView) rootView.findViewById(R.id.mTvResultTip);
                mTvResultTip.setText("交易失败");
                flagIconShow.setBackgroundResource(R.drawable.icon_tip_fail);
            }
            mIvTip.setVisibility(View.GONE);
            mBtnNext.setVisibility(View.GONE);
            mBtnPrint.setVisibility(View.GONE);

            //有冲正则显示查询按钮
            if(((EbiBaseResultPresent)mResultPresent).getReverseInfo()!=null){
                mBtnQuery.setVisibility(View.VISIBLE);
            }

            errorMsg = mResultPresent.getResponseMessage();
            addItemView(getString(R.string.label_resp_code), mResultPresent.getResponseCode(), Settings.bIsSettingBlueTheme() ? false : true);//状态码
            addItemView(getString(R.string.label_resp_msg), errorMsg, false);//提示信息
            addItemView(getString(R.string.printer_term_num), BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41), false);
            addItemView(getString(R.string.tip_sn), CommonUtils.getSn(), false);
            addItemView(getString(R.string.tip_version),
                    PackageUtils.getInstalledVersionName(EposApplication.getAppContext(), EposApplication.getAppContext().getPackageName()), false);

            //添加倒计时
            showingTimeout(mTvShowTimeOut);
        }

    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.result_info_item2, null);
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
        setTitle(R.string.title_result);
        hideBackBtn();
    }

    @Override
    public void onDestroy() {
        cancelTimeout();
        super.onDestroy();
    }

    @Override
    protected ITradePresent newTradePresent() {
        EbiResultPresent resultPresent = new EbiBaseResultPresent(this);
        mResultPresent = resultPresent;
        documentPresent = new DocumentPresent(this);
        return resultPresent;
    }

    public void printPDF(){
        if(!BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),BusinessConfig.Key.TOGGLE_PRINT_DOCUMENT)){
            showingTimeout(mTvShowTimeOut);
            return;
        }
        printList.clear();
        total = 0;
        PrintReceiptBean data = (PrintReceiptBean)mTradePresent.getTransData().get(JsonKeyGT.printData);
        if(data!=null){
            DialogFactory.showLoadingDialog(getActivity(), "正在下载单据\n请稍侯");
            for(PrintReceiptBean.BodyBean bodyBean:data.getBody()){
                for(int i=0;i<bodyBean.getPrint().size();i++){
                    total++;
                    PrintReceiptBean.BodyBean.PrintBean printBean = bodyBean.getPrint().get(i);
                    String suffix = ".pdf";
                    try {
                        String[] strs = printBean.getImage().split("\\.");
                        if(strs!=null&&strs.length>1){
                            suffix = "."+strs[strs.length-1];
                        }
                    }catch (Exception e){
                        logger.error(e.toString());
                    }
                    getPDF(bodyBean.getSubOrderId()+"_print_"+(i+1)+suffix, printBean.getImage());
                }
            }
        }
    }

    public void printPDF_Old(){
        if(!BusinessConfig.getInstance().getToggle(EposApplication.getAppContext(),BusinessConfig.Key.TOGGLE_PRINT_DOCUMENT)){
            showingTimeout(mTvShowTimeOut);
            return;
        }
        printList.clear();
        PrintReceiptBean data = (PrintReceiptBean)mTradePresent.getTransData().get(JsonKeyGT.printData);
        if(data!=null){
            total = data.getBody().size();
            if(total>0){
                DialogFactory.showLoadingDialog(getActivity(), "正在下载单据\n请稍侯");
            }
            for(PrintReceiptBean.BodyBean bodyBean:data.getBody()){
                String suffix = ".pdf";
                try {
                    String[] strs = bodyBean.getImage().split("\\.");
                    if(strs!=null&&strs.length>1){
                        suffix = "."+strs[strs.length-1];
                    }
                }catch (Exception e){
                    logger.error(e.toString());
                }
                getPDF(bodyBean.getSubOrderId()+suffix, bodyBean.getImage());
            }
        }
    }

    private void getPDF(final String fileName, final String url){
        File file = new File(Config.Path.PDF_PATH, fileName);
        OkHttpUtils.getInstance().downloadFile(file, url, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                try {
                    if(result!=null){
                        successNum++;
                        logger.info(fileName+" -> 下载完成 -> "+result.get("path"));
                        printList.add((String) result.get("path"));
                    }else {
                        failNum++;
                        logger.error("单据下载失败");
                    }
                    logger.debug("successNum:"+successNum);
                    logger.debug("failNum:"+failNum);
                    logger.debug("total:"+total);
                    Log.e("===","total:" + total);
                    if(successNum+failNum==total){
                        DialogFactory.hideAll();
                        //添加倒计时
                        showingTimeout(mTvShowTimeOut);
                        if(successNum>0){
                            documentPresent.addPrintTask(printList,1);
                            documentPresent.print2();
                        }else {
                            popToast("无可打印单据");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    popToast("通讯异常");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if(v.getId()==R.id.mBtnNext){
            cancelTimeout();
            Map<String, Object> map = new ArrayMap<>();
            map.put(JsonKeyGT.idType, mTradePresent.getTransData().get(JsonKeyGT.idType));
            map.put(JsonKeyGT.name, mTradePresent.getTransData().get(JsonKeyGT.name));
            map.put(JsonKeyGT.idNo, mTradePresent.getTransData().get(JsonKeyGT.idNo));
            map.put(JsonKeyGT.termSn, mTradePresent.getTransData().get(JsonKeyGT.termSn));
            map.put(JsonKeyGT.isOther, mTradePresent.getTransData().get(JsonKeyGT.isOther));
            ((BaseTradePresent)mTradePresent).getTempData().clear();
            mTradePresent.getTransData().clear();
            getHostActivity().mTradeInformation.getRespDataMap().clear();
            mTradePresent.getTransData().putAll(map);
            mTradePresent.gotoNextStep("2");
            //uploadESign(false);
        }else if(v.getId()==R.id.mBtnPrint){
            mTradePresent.gotoNextStep("3");
        }else if(v.getId()==R.id.mBtnQuery){
            queryResult();
        }else {
            super.onClick(v);
        }
    }

    private void queryResult(){
        send8583Data(true, TransCode.SALE_RESULT_QUERY, mTradePresent.getTransData(), new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                if(result!=null){
                    mTradePresent.getTransData().putAll(result);
                    getHostActivity().mTradeInformation.setRespDataMap(result);
                    if("00".equals(result.get(TradeInformationTag.RESPONSE_CODE))){
                        getHostActivity().mTradeInformation.getTempMap().put(TransDataKey.key_resp_code, "00");
                        //保存交易记录
                        HashMap<String, Object> map = new HashMap<>();
                        map.putAll(mTradePresent.getTransData());
                        map.putAll(getHostActivity().mTradeInformation.getRespDataMap());
                        TradeInfoRecord tradeInfo = new TradeInfoRecord(TransCode.SALE, map);
                        String out_order_no = (String) mTradePresent.getTransData().get("out_order_no");
                        tradeInfo.setIntoAccount(out_order_no);
                        if(!TextUtils.isEmpty(out_order_no)&&out_order_no.length()==19){
                            tradeInfo.setTransYear(out_order_no.substring(1,5));
                        }
                        tradeInfo.setOperatorNo(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_OPER_ID));
                        DbHelper dbHelper = OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
                        CommonDao<TradeInfoRecord> tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
                        CommonDao<ReverseInfo> reverseDao = new CommonDao<>(ReverseInfo.class, dbHelper);
                        try {
                            tradeDao.save(tradeInfo);
                            reverseDao.deleteById(tradeInfo.getVoucherNo());
                        }catch (Exception e){
                            logger.error(e.toString());
                        }
                        OpenHelperManager.releaseHelper();
                        dbHelper = null;
                        mTradePresent.getTransData().put(JsonKeyGT.successFlag, JsonKeyGT.successFlag);
                        mTradePresent.gotoNextStep("555");
                    }else {
                        popToast(ISORespCode.codeMap((String) result.get(TradeInformationTag.RESPONSE_CODE)).getResId());
                    }
                }

            }
        });

    }

    private void uploadESign(boolean exit){
        cancelTimeout();
        DialogFactory.showLoadingDialog(getActivity(), "正在上传电子签名\n请稍候");
        File file = new File((String) mTradePresent.getTransData().get(JsonKeyGT.signFileName));
        new UploadESignatureTrade.UploadESignature(this, (BaseTradePresent) mTradePresent,file,exit)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mTradePresent.getTradeCode());
    }

    @Override
    public boolean onBacKeyPressed() {
        mTradePresent.gotoNextStep("999");
        return true;
    }

}
