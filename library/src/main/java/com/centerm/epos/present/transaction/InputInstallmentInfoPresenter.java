package com.centerm.epos.present.transaction;

import android.text.TextUtils;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class InputInstallmentInfoPresenter extends BaseTradePresent implements IInputInstallmentInfoPresenter {

    protected ITradeView view;

    public InputInstallmentInfoPresenter(ITradeView mTradeView) {
        super(mTradeView);
        view = mTradeView;
    }

    @Override
    public boolean onConfirmClicked(String installmentPeriod, String code, int installmentType) {
        if (TextUtils.isEmpty(installmentPeriod)) {
            view.popToast("请输入分期期数");
            return false;
        }

        if (Integer.parseInt(installmentPeriod) == 0){
            view.popToast("分期期数不能为0");
            return false;
        }

        //modify by yuhc 允许编码为空（徽商银行项目测试结论）
//        if (TextUtils.isEmpty(code)) {
//            view.popToast("请输入商品项目编码");
//            return false;
//        }

        mTradeInformation.getTransDatas().put(TradeInformationTag.INSTALLMENT_PERIOD, installmentPeriod);
        if (!TextUtils.isEmpty(code)) {
            mTradeInformation.getTransDatas().put(TradeInformationTag.INSTALLMENT_CODE, code);
        }
        mTradeInformation.getTransDatas().put(TradeInformationTag.INSTALLMENT_PAY_MODE, installmentType);
        gotoNextStep();
        return true;
    }
}
