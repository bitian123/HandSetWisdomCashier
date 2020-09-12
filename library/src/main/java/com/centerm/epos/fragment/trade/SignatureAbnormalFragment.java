package com.centerm.epos.fragment.trade;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.centerm.dev.printer.PrinterDataAdvBuilder;
import com.centerm.dev.printer.PrinterDataParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.ResultQueryActivity;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.GtBean2;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.present.transaction.SignatureAbnormalPresent;
import com.centerm.epos.present.transaction.SignaturePresent;
import com.centerm.epos.transcation.pos.constant.BankNameMap;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.HandwrittenPad;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import config.BusinessConfig;
import config.Config;

/**
 * 电子签名界面。
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */
public class SignatureAbnormalFragment extends BaseTradeFragment {
    private HandwrittenPad writePad;
    private SignatureAbnormalPresent mSignaturePresent;
    private TradeInfoRecord tradeInfo;
    private TextView mTvTip,positive_btn;
    private static final int TicketUpload = 1;
    private static final int PrintReceipt = 2;
    private Gson gson = new Gson();
    private int isPay = 1;//是否代付标识 0代付 1不代付
    Map<String, Object> map = new HashMap<>();
    private boolean isConfirm = false;//是否点击确定

    @Override
    public int onLayoutId() {
        return R.layout.fragment_signature;
    }

    @Override
    public void onInitView(View view) {
        initFinishBtnlistener(view);
        mTvTip = (TextView) view.findViewById(R.id.mTvTip);
        view.findViewById(R.id.mIvTip).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.resign_btn).setOnClickListener(this);
        positive_btn = (TextView) view.findViewById(R.id.positive_btn);
        positive_btn.setOnClickListener(this);
        writePad = (HandwrittenPad) view.findViewById(R.id.hand_write_pad);
        writePad.setContionCode(mSignaturePresent.getContionCode());

        Set<String> keySet = getArguments().keySet();
        for(String key : keySet) {
            map.put(key, getArguments().get(key));
        }
        tradeInfo = new TradeInfoRecord(TransCode.SALE, map);

        String isPayStr = (String) map.get(TransDataKey.iso_f62);
        try {
            isPay = Integer.parseInt(isPayStr);
        }catch (Exception e){

        }
        logger.info("isPay:"+isPay);
        if(isPay==0){//代付
            mTvTip.setText("请在下方区域进行代付人（持卡人）签名\n（签名用于签购单）");
        }else {
            mTvTip.setText("请在下方区域进行签名\n（签名用于小票和收据）");
        }
        mSignaturePresent.init(this,map);

    }

    @Override
    protected ITradePresent newTradePresent() {
        mSignaturePresent = new SignatureAbnormalPresent(this);
        return mSignaturePresent;
    }

    public void ticketUpload(){
        logger.info("ticketUpload:"+mTradePresent.getTransData().get(JsonKeyGT.signFileName));
        logger.info("ticketUpload:"+map.get(TransDataKey.iso_f61));
        File file = new File(mTradePresent.getTransData().get(JsonKeyGT.signFileName)+"");
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        if(!createPic(bitmap)){
            popToast("签购单图片生成失败");
            mTradePresent.gotoNextStep();
            return;
        }
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        dataMap.put(JsonKeyGT.orderNo, map.get(TransDataKey.iso_f61));
        File voucherFile = new File(Config.Path.VOUCHER_PATH, map.get(TransDataKey.iso_f61)+".png");
        if(voucherFile.exists()) {
            dataMap.put(JsonKeyGT.file,voucherFile);
        }else {

        }
        DialogFactory.showLoadingDialog(getActivity(), "正在进行小票影像上传\n请稍侯");
        OkHttpUtils.getInstance().uploadFile("/shdy_greentown/rest/forPos/ticketUpload",
                dataMap, new OnCallListener() {
                    @Override
                    public void onCall(Map<String, Object> result) {
                        DialogFactory.hideAll();
                        try {
                            if(result!=null){
                                String resultMsg = (String) result.get(JsonKeyGT.returnData);
                                GtBean2 bean = gson.fromJson(resultMsg, new TypeToken<GtBean2>() {}.getType());
                                if("0".equals(bean.getRespCode())){
                                    next();
                                }else {
                                    tipToRetry(bean.getRespMsg()+"\n是否重试", TicketUpload);
                                }
                            }else {
                                tipToRetry("通讯异常，是否重试", TicketUpload);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            DialogFactory.hideAll();
                            popToast("通讯异常");
                        }
                    }
                });
    }

    private void next(){
        if(isPay==0){//代付
            mTvTip.setText("请在下方区域进行购房人签名\n（签名用于收据）");
            writePad.clear();
            writePad.setContionCode(mSignaturePresent.getContionCode());
            String filePath = Config.Path.ROOT + File.separator + "sign.png";
            mTradePresent.getTransData().put(JsonKeyGT.secondFlag, filePath);
        }else {
            printReceipt();
        }

    }

    public void printReceipt(){
        Map<String,Object> dataMap = new HashMap<>();
        dataMap.put(JsonKeyGT.projectId, BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.PROJECT_ID));
        dataMap.put(JsonKeyGT.orderNo, map.get(TransDataKey.iso_f61));
        File file = new File(mTradePresent.getTransData().get(JsonKeyGT.signFileName)+"");
        //购房人签名是第二次的签名，代理人签名是用第一次签名
        if(file.exists()) {
            //dataMap.put(JsonKeyGT.file,file);
            dataMap.put(JsonKeyGT.buyer, file);
        }
        if(isPay==0){//代付
            dataMap.put(JsonKeyGT.client, mSignaturePresent.getSignFile());
        }
        DialogFactory.showLoadingDialog(getActivity(), "正在获取回执单\n请稍侯");
        OkHttpUtils.getInstance().uploadFile("/shdy_greentown/rest/forPos/printReceipt",
                dataMap, new OnCallListener() {
                    @Override
                    public void onCall(Map<String, Object> result) {
                        try {
                            if(result!=null){
                                String resultMsg = (String) result.get(JsonKeyGT.returnData);
                                PrintReceiptBean bean = gson.fromJson(resultMsg, new TypeToken<PrintReceiptBean>() {}.getType());
                                if("0".equals(bean.getRespCode())){
                                    mTradePresent.getTransData().put(JsonKeyGT.printData, bean);
                                    requeryOk();
                                    ((ResultQueryActivity)getActivity()).signOK(bean);
                                    //mSignaturePresent.printPDF(bean);
                                }else {
                                    tipToRetry(bean.getRespMsg()+"\n是否重试", PrintReceipt);
                                }
                            }else {
                                DialogFactory.hideAll();
                                tipToRetry("通讯异常，是否重试", PrintReceipt);
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            DialogFactory.hideAll();
                            popToast("通讯异常");
                        }
                    }
                });
    }

    private void requeryOk(){
        writePad.setEnabled(false);
        positive_btn.setEnabled(false);
    }

    private void tipToRetry(String txt, final int type) {
        DialogFactory.showSelectDialog(getActivity(),getString(R.string.tip_notification), txt, new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        if(type==TicketUpload){
                            ticketUpload();
                        }else if(type==PrintReceipt){
                            printReceipt();
                        }
                        break;
                    case NEGATIVE:
                        if(type==TicketUpload){
                            printReceipt();
                        }else if(type==PrintReceipt){
                            //mTradePresent.gotoNextStep();
                            requeryOk();
                            ((ResultQueryActivity)getActivity()).signOK(null);
                        }
                        break;
                }
            }
        });
    }

    private boolean createPic(Bitmap signBitmap){
        PrinterDataAdvBuilder advBuilder  = new PrinterDataAdvBuilder();
        try {
            advBuilder.setMaxWidth(450);
            PrinterDataParams params = new PrinterDataParams();
            params.setMarginLeft(30);
            advBuilder.addText("           交易签购单",params);
            advBuilder.addText("商户存根                   请妥善保管",params);
            advBuilder.addText("-----------------------------------",params);
            advBuilder.addText("商户名称",params);
            advBuilder.addText(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME),params);
            advBuilder.addText("商户编号:"+BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42),params);
            advBuilder.addText("终端编号:"+BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41),params);
            advBuilder.addText("操作员号:"+BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),BusinessConfig.Key.KEY_OPER_ID),params);
            advBuilder.addText("发 卡 行:"+getBankName(tradeInfo.getIssueInstituteID()),params);
            advBuilder.addText("收 单 行:"+tradeInfo.getAcquireInstituteID(),params);
            advBuilder.addText("卡     号:\n"+ DataHelper.formatCardno(tradeInfo.getCardNo()),params);
            //advBuilder.addText("有效日期:"+ tradeInfo.getCardExpiredDate(),params);
            advBuilder.addText("日期/时间:"+getDate(tradeInfo),params);
            advBuilder.addText("参 考 号:"+tradeInfo.getReferenceNo(),params);
            advBuilder.addText("批 次 号:"+tradeInfo.getBatchNo(),params);
            advBuilder.addText("凭 证 号:"+tradeInfo.getVoucherNo(),params);
            advBuilder.addText("外部订单号:"+tradeInfo.getIntoAccount(),params);
            advBuilder.addText("交易类型:\n消费",params);
            advBuilder.addText("金    额: \nRMB "+tradeInfo.getAmount(),params);
            advBuilder.addText("-----------------------------------",params);
            advBuilder.addText("备注:\n",params);
            if(!TextUtils.isEmpty(tradeInfo.getUnicom_scna_type())){
                try {
                    JSONObject object = new JSONObject(tradeInfo.getUnicom_scna_type());
                    advBuilder.addText("项目名称:"+object.optString("projectName"),params);
                    advBuilder.addText("姓名:"+object.optString("name"),params);
                    advBuilder.addText("证件号:"+object.optString("idNo"),params);
                    advBuilder.addText("\n",params);

                    if(object.optJSONArray("array")!=null){
                        JSONArray array = object.optJSONArray("array");
                        for(int i=0;i<array.length();i++){
                            JSONObject item = array.getJSONObject(i);
                            String[] strs = DataHelper.segmentation(item.optString("billId"), Config.ROW_MAX_LENTH);
                            if(strs!=null){
                                for(int j=0;j<strs.length;j++){
                                    advBuilder.addText(strs[j],params);
                                }
                            }else {
                                advBuilder.addText(item.optString("billId"),params);
                            }
                            advBuilder.addText(item.optString("amt"),params);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            advBuilder.addText("\n",params);
            advBuilder.addText("持卡人签名:\n",params);
            advBuilder.addBitmap(signBitmap);
            advBuilder.addText("本人确认以上交易，同意将其计入",params);
            advBuilder.addText("本人账户",params);
            advBuilder.addText("I ACKNOWLEDGE SATISFACTORY",params);
            advBuilder.addText("RECEIPT OF RELATIVE",params);
            advBuilder.addText("GOODS/SERVICES CENTERM K9",params);
            advBuilder.addText("CENTERM K9",params);
            Bitmap bmp = advBuilder.build();
            return saveFile(bmp, tradeInfo.getIntoAccount()+".png");
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return false;
    }

    private boolean saveFile(Bitmap bitmap,String fileName){
        File file = new File(Config.Path.VOUCHER_PATH, fileName);//将要保存图片的路径
        try {
            File dic = new File(Config.Path.VOUCHER_PATH);
            if(!dic.exists()){
                dic.mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected String getBankName(String tempString) {
        if (!TextUtils.isEmpty(tempString) && tempString.length() > 3) {
            String bankName;
            bankName = BankNameMap.getBankName(tempString.substring(0, 4));
            if (!TextUtils.isEmpty(bankName))
                tempString = bankName;
        }
        return tempString;
    }

    private String getDate(TradeInfoRecord tradeInfo){
        if(!TextUtils.isEmpty(tradeInfo.getTransYear())
                &&!TextUtils.isEmpty(tradeInfo.getTransDate())
                &&!TextUtils.isEmpty(tradeInfo.getTransTime())){
            String yearStr = tradeInfo.getTransYear();
            String dateStr = tradeInfo.getTransDate();
            String timeStr = tradeInfo.getTransTime();
            String dateTimeStr = String.format(Locale.CHINA, "%s/%s/%s %s:%s:%s", yearStr, dateStr.substring(0, 2), dateStr
                    .substring(2, 4), timeStr.substring(0, 2), timeStr.substring(2, 4), timeStr.substring(4, 6));
            return dateTimeStr;
        }
        return "";
    }

    private String getAdditional(String additionalStr){
        if(!TextUtils.isEmpty(additionalStr)){
            try {
                JSONObject object = new JSONObject(additionalStr);
                StringBuilder builder = new StringBuilder();
                builder.append("\n项目名称:"+object.optString("projectName"))
                        .append("\n")
                        .append("姓名:"+object.optString("name"))
                        .append("\n")
                        .append("证件号:"+object.optString("idNo"))
                        .append("\n\n");
                if(object.optJSONArray("array")!=null){
                    JSONArray array = object.optJSONArray("array");
                    for(int i=0;i<array.length();i++){
                        JSONObject item = array.getJSONObject(i);
                        builder.append(item.optString("billId"))
                                .append("  ")
                                .append(item.optString("amt"))
                                .append("\n");
                    }
                }
                return builder.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.resign_btn) {
            writePad.clear();
        } else if (id == R.id.positive_btn) {
            if (writePad.getStrokeNumber() != 0)
                if(!isConfirm){
                    isConfirm = true;
                    mTradePresent.onConfirm(writePad.getCachebBitmapWithCode());
                }
            else{
                //getHostActivity().clearPageTimeout();
                //mTradePresent.gotoNextStep();
                popToast(R.string.tip_sign);
            }
        } else if (id == R.id.cancel_btn) {
            getHostActivity().clearPageTimeout();
            mTradePresent.gotoNextStep();
        } else {
            popToast(R.string.tip_sign);
        }
    }

    @Override
    public boolean onBacKeyPressed() {
        popToast(R.string.tip_sign);
        return true;
    }

    @Override
    public boolean onBackPressed() {
        popToast(R.string.tip_sign);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("签名");
    }

}
