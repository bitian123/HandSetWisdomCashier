package com.centerm.epos.ebi.present;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.ebi.common.PayTypeEnum;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.ui.view.PayTypeDialog;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.view.AlertDialog;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.REFUND;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;

/**
 * author:liubit</br>
 * date:2017/12/26</br>
 */
public class EbiInputMoneyPresent extends BaseTradePresent {
    public EbiInputMoneyPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        if(TextUtils.equals(SALE_SCAN_REFUND, getTradeCode())){
            //selectPayType();
        }
    }

    private void selectPayType(){
        final PayTypeDialog payTypeDialog = new PayTypeDialog(mTradeView.getContext(), false);
        payTypeDialog.setItemOnClick(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                payTypeDialog.dismiss();
                switch ((PayTypeEnum) adapterView.getItemAtPosition(i)) {
                    case CARD:
                        transDatas.put(JsonKey.pay_type, "00");
                        break;
                    case WEI:
                        transDatas.put(JsonKey.pay_type, "01");
                        break;
                    case ALI:
                        transDatas.put(JsonKey.pay_type, "02");
                        break;
                    case UNION:
                        transDatas.put(JsonKey.pay_type, "03");
                        break;

                }
            }
        });
        payTypeDialog.setOnCancel(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                EbiInputMoneyPresent.this.onCancel();
            }
        });
        payTypeDialog.show();

    }

    public void onConfirmClick(final String amt) {
        if (Double.valueOf(amt) == 0) {
            mTradeView.popToast(R.string.tip_input_money2);
            return;
        }
        if (REFUND.equals(mTradeInformation.getTransCode()) && Double.valueOf(amt) > BusinessConfig
                .REFUND_AMOUNT_LIMITED) {
            mTradeView.popToast(R.string.tip_refund_over_limited);
            return;
        }
        if (REFUND.equals(mTradeInformation.getTransCode())||SALE_SCAN_REFUND.equals(mTradeInformation.getTransCode())) {
            mTradeView.popMessageBox("请确认", "退货金额：" + amt+"元", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                        goNexStep(amt);
                    }
                }
            });
        } else {
            goNexStep(amt);
        }
    }

    @Override
    public boolean isEnableShowingTimeout() {
        return true;
    }

    @Override
    public Object onConfirm(Object paramObj) {
        final String amt = (String) paramObj;
        if (Double.valueOf(amt) == 0) {
            mTradeView.popToast(R.string.tip_input_money2);
            return null;
        }
        double amountLimit = Double.parseDouble(BusinessConfig.getInstance().getValue(mTradeView.getHostActivity(),
                BusinessConfig.Key.REFUND_AMOUNT_LIMITED));
        if ((REFUND.equals(mTradeInformation.getTransCode())||SALE_SCAN_REFUND.equals(mTradeInformation.getTransCode())) && Double.valueOf(amt) > amountLimit) {
            mTradeView.popToast(R.string.tip_refund_over_limited);
            return null;
        }
        if (REFUND.equals(mTradeInformation.getTransCode())) {
            mTradeView.showSelectDialog("请确认", "退货金额：" + amt+"元", new AlertDialog.ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                        goNexStep(amt);
                    }
                }
            });
        } else if(SALE_SCAN.equals(mTradeInformation.getTransCode())){
            goNexStep(amt, "3");
        }else if(SALE_SCAN_REFUND.equals(mTradeInformation.getTransCode())){
            logger.debug("扫码退货直接联机");
            transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
            gotoNextStep("2");
        }else {
            goNexStep(amt);
        }

        return null;
    }

    private void goNexStep(String amt) {
        if ("1".equals(transDatas.get(FLAG_IMPORT_AMOUNT))) {
            try {
                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                pbocService.importAmount(amt);
                transDatas.put(FLAG_IMPORT_AMOUNT, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
        if (AUTH_COMPLETE.equals(mTradeInformation.getTransCode())&& !BusinessConfig.getInstance().getFlag(mTradeView.getHostActivity(), BusinessConfig.Key.TOGGLE_AUTH_COMPLETE_INPUTWD)){
            gotoNextStep("2");
        }else {
            gotoNextStep();
        }
    }

    private void goNexStep(String amt, String step) {
        if ("1".equals(transDatas.get(FLAG_IMPORT_AMOUNT))) {
            try {
                IPbocService pbocService = DeviceFactory.getInstance().getPbocService();
                pbocService.importAmount(amt);
                transDatas.put(FLAG_IMPORT_AMOUNT, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        transDatas.put(TradeInformationTag.TRANS_MONEY, amt);
        gotoNextStep(step);

    }
}
