package com.centerm.epos.ebi.ui.fragment;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.common.TransCode;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.present.transaction.IScanQRCode;
import com.centerm.epos.present.transaction.ScanQRCodePresent;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by yuhc on 2017/4/22.
 * 扫码界面
 */

public class EbiScanQRCodeFragment extends BaseTradeFragment {
    private ScanQRCodePresent mScanQRCodePresent;
    private boolean isScanOk = false;

    @Override
    protected void onInitView(View view) {

    }

    @Override
    protected ITradePresent newTradePresent() {
        ScanQRCodePresent present = new ScanQRCodePresent(this);
        mScanQRCodePresent = present;
        return present;
    }

    @Override
    protected int onLayoutId() {
        return com.centerm.epos.R.layout.fragment_scan_qrcode;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //显示扫描到的内容
        if (data == null) {
            if(TransCode.SALE_PROPERTY.equals(mTradePresent.getTradeCode())){
                mTradePresent.gotoPreStep();
            }else {
                ActivityStack.getInstance().pop();
            }
            return;
        }
        String content = data.getStringExtra("txtResult");
        if (!TextUtils.isEmpty(content)) {
            if(TransCode.SALE_PROPERTY.equals(mTradePresent.getTradeCode())){
                if(mTradePresent.getTransData().get(JsonKey.mer_order_no)!=null){
                    if (content.matches("[0-9]+")) {
                        isScanOk = true;
                        mScanQRCodePresent.onGetScanCodePeopertyStep2(content);
                    } else {
                        Toast.makeText(getContext(), "只支持数字！", Toast.LENGTH_SHORT).show();
                        mTradePresent.gotoPreStep();
                    }
                }else {
                    try {
                        JSONObject json = new JSONObject(content);
                        if(!TextUtils.isEmpty(content)&&content.contains("errcode")){
                            if("0".equals(json.optString("errcode"))){
                                initOrderInfo(json.optString("values"));
                            }else {
                                exitWithToast("订单状态异常");
                            }
                        }else {
                            initOrderInfo(content);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        exitWithToast("订单解析出错");
                    }
                }
                return;
            }
            if (content.matches("[0-9]+")) {
                isScanOk = true;
                mScanQRCodePresent.onGetScanCode(content);
            } else {
                Toast.makeText(getContext(), "只支持数字！", Toast.LENGTH_SHORT).show();
                mTradePresent.gotoPreStep();
            }
        }
    }

    private void initOrderInfo(String data) throws JSONException {
        JSONObject propertyJson = new JSONObject(data);
        if(!"02".equals(propertyJson.optString("channel"))
                &&!"03".equals(propertyJson.optString("channel"))
                &&!"05".equals(propertyJson.optString("channel"))){
            exitWithToast("渠道信息有误");
            return;
        }
        if(!TextUtils.equals(GetRequestData.getMercode(), propertyJson.optString("merc_id"))){
            exitWithToast("商户号不匹配");
            return;
        }
        if("0".equals(propertyJson.optString("pay_amount"))||TextUtils.isEmpty(propertyJson.optString("pay_amount"))){
            exitWithToast("订单金额不能为空");
            return;
        }
        if(TextUtils.isEmpty(propertyJson.optString("mer_order_no"))){
            exitWithToast("订单号不能为空");
            return;
        }
        isScanOk = true;
        mTradePresent.getTransData().put(JsonKey.mer_order_no, propertyJson.optString("mer_order_no"));
        mTradePresent.getTransData().put(JsonKey.pay_amount, propertyJson.optString("pay_amount"));
        mTradePresent.getTransData().put(JsonKey.notice_url, propertyJson.optString("notice_url"));
        mTradePresent.getTransData().put(JsonKey.channel, propertyJson.optString("channel"));
        mTradePresent.getTransData().put(JsonKey.order_sign, propertyJson.optString("order_sign"));
        mTradePresent.getTransData().put(JsonKey.tm_smp, propertyJson.optString("tm_smp"));
        mScanQRCodePresent.onGetScanCodePeoperty(propertyJson.toString());
    }

    private void exitWithToast(String msg){
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        ActivityStack.getInstance().pop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(!isScanOk){
            //ActivityStack.getInstance().pop();
        }
    }

}
