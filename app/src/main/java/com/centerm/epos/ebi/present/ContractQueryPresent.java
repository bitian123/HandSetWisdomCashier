package com.centerm.epos.ebi.present;

import android.annotation.SuppressLint;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IBarCodeScanner;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.Settings;
import com.centerm.epos.ebi.transaction.EbiContractQueryTrade;
import com.centerm.epos.present.transaction.IScanQRCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * author:liubit</br>
 * date:2019/9/2</br>
 * @author 16240
 */
public class ContractQueryPresent extends BaseTradePresent implements IScanQRCode {
    private  String tradeDate;

    public ContractQueryPresent(ITradeView mTradeView) {
        super(mTradeView);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        tradeDate = formatter.format(new Date());
    }



    @SuppressLint("StaticFieldLeak")
    public void queryContractInfo() {
        transDatas.put(TradeInformationTag.TRANS_TIME, tradeDate.substring(8, 14));
        transDatas.put(TradeInformationTag.TRANS_DATE, tradeDate.substring(4, 8));
        new EbiContractQueryTrade().execute(mTradeView,ContractQueryPresent.this);
    }


    @Override
    public void onStartScanCode() {
        if (!Settings.bIsSettingBlueTheme()) {
            IBarCodeScanner dev = (IBarCodeScanner) DeviceFactory.getInstance().getDevice(EnumDeviceType.BAR_CODE_SCANNER_DEV);
            if (dev == null) {
                return;
            }
            dev.scanBarCode(mTradeView.getHostActivity());
        } else {
            new com.centerm.epos.transcation.pos.manager.ScanCodeTrade().scanBarCode(mTradeView.getHostActivity());
        }

    }

    @Override
    public void onGetScanCode(String code) {
        transDatas.put(TradeInformationTag.SCAN_CODE, code);
    }


}
