package com.centerm.epos.present.transaction;

import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IBarCodeScanner;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.ViewUtils;

/**
 * Created by yuhc on 2017/4/22.
 */

public class ScanQRCodePresent extends BaseTradePresent implements IScanQRCode{

    public ScanQRCodePresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void beginTransaction() {
        onStartScanCode();
    }


    @Override
    public void onStartScanCode() {
        if(!Settings.bIsSettingBlueTheme()) {
            IBarCodeScanner dev = (IBarCodeScanner) DeviceFactory.getInstance().getDevice(EnumDeviceType
                    .BAR_CODE_SCANNER_DEV);
            if (dev == null) {
                return;
            }
            dev.scanBarCode(mTradeView.getHostActivity());
        }
        else{
            new com.centerm.epos.transcation.pos.manager.ScanCodeTrade().scanBarCode(mTradeView.getHostActivity());
        }

    }

    public void onGetScanCodePeoperty(String code) {
        transDatas.put(TradeInformationTag.PROPERTY_MSG, code);
        gotoNextStep("1");
    }

    public void onGetScanCodePeopertyStep2(String code) {
        if(!checkType(code)){
            gotoPreStep();
            return;
        }
        transDatas.put(TradeInformationTag.SCAN_CODE, code);
        gotoNextStep("2");
    }

    @Override
    public void onGetScanCode(String code) {
        if(!checkType(code)){
            gotoPreStep();
            return;
        }
        transDatas.put(TradeInformationTag.SCAN_CODE, code);
        transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "03");
        if (TransCode.SALE.equals(mTradeInformation.getTransCode())) {
            transDatas.put(TradeInformationTag.TRANSACTION_TYPE, TransCode.SALE_SCAN);
            mTradeInformation.setTransCode(TransCode.SALE_SCAN);
        }else
            transDatas.put(TradeInformationTag.SCAN_VOUCHER_NO, code);
        gotoNextStep("1");
    }

    public boolean checkType(String code){
        boolean checktype = true;
        String codeStr = code.substring(0, 2);
        int codeHead = Integer.parseInt(codeStr, 10);
        if (10 <= codeHead && codeHead <= 15 && code.length() == 18) {
            //微信
            transDatas.put("pay_type", "01");
        } else if ((25 <= codeHead && codeHead <= 30) && (16 <= code.length() && code.length() <= 32)) {
            //支付宝
            transDatas.put("pay_type", "02");
        } else if (62 == codeHead || 63 == codeHead){
            //银联
            transDatas.put("pay_type", "03");
        }else {
            ViewUtils.showToast(mTradeView.getContext(), "无效二维码");
            checktype = false;
        }
        return checktype;
    }
}
