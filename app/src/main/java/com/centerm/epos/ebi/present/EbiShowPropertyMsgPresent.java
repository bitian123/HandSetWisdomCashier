package com.centerm.epos.ebi.present;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;

/**
 * author:liubit</br>
 * date:2017/12/26</br>
 */
public class EbiShowPropertyMsgPresent extends BaseTradePresent {
    public EbiShowPropertyMsgPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    public void goCardNexStep(String amt, String orderNO) {
//        if (!"1".equals(transDatas.get(FLAG_IMPORT_AMOUNT))) {
//            try {
//                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
//                pbocService.importAmount(amt);
//                transDatas.put(FLAG_IMPORT_AMOUNT, "1");
//                logger.error("importAmount: "+amt);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
        transDatas.put(JsonKey.out_order_no, orderNO);

        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, com.centerm.epos.common.TransCode.SALE);
        mTradeInformation.setTransCode(TransCode.SALE);
        gotoNextStep("2");
    }

    public void goScanNexStep(String amt, String orderNO) {
        transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
        transDatas.put(JsonKey.out_order_no, orderNO);

        transDatas.put(TradeInformationTag.TRANSACTION_TYPE, TransCode.SALE_SCAN);
        mTradeInformation.setTransCode(TransCode.SALE_SCAN);
        gotoNextStep("1");
    }
}
