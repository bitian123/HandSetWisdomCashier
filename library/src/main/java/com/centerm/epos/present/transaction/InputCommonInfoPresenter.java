package com.centerm.epos.present.transaction;

import android.content.res.Resources;
import android.text.TextUtils;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.Locale;
import java.util.Map;


public class InputCommonInfoPresenter extends BaseTradePresent implements IInputCommonInfoPresenter {

    protected ITradeView view;
    protected Map<String, Object> dataMap ;

    public InputCommonInfoPresenter(ITradeView mTradeView) {
        super(mTradeView);
        view = mTradeView;
        dataMap = mTradeInformation.getTransDatas();
    }
    @Override
    public boolean bIsIntegralTrans(){
        String code = mTradeInformation.getTransCode();
        return ( TransCode.ISS_INTEGRAL_SALE.equals(code) || TransCode.UNION_INTEGRAL_SALE.equals(code) );
    }
    @Override
    public boolean bIsMagCashLoad(){
        String code = mTradeInformation.getTransCode();
        return ( TransCode.MAG_CASH_LOAD.equals(code)
                || TransCode.MAG_ACCOUNT_VERIFY.equals(code)
                || TransCode.MAG_ACCOUNT_LOAD.equals(code)
                || TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(code) );
    }

    @Override
    public String getMagCertNo(int id){
        String code = view.getStringFromResource(R.string.tip_id_id_value);
        if( R.id.id_id == id){
            code = view.getStringFromResource(R.string.tip_id_id_value);
        }else if( R.id.id_coo == id){
            code = view.getStringFromResource(R.string.tip_id_coo_value);
        }else if( R.id.id_passport == id){
            code = view.getStringFromResource(R.string.tip_id_passport_value);
        }else if( R.id.id_rp == id){
            code = view.getStringFromResource(R.string.tip_id_rp_value);
        }else if( R.id.id_mtp == id){
            code = view.getStringFromResource(R.string.tip_id_mtp_value);
        }else if( R.id.id_poc == id){
            code = view.getStringFromResource(R.string.tip_id_poc_value);
        }else if( R.id.id_sc == id){
            code = view.getStringFromResource(R.string.tip_id_sc_value);
        }
        return code;
    }

    @Override
    public boolean onConfirmClicked(String goodsCode) {
        int maxLen = EposApplication.getAppContext().getResources().getInteger(R.integer.integral_goods_code_max_len);
        if (TextUtils.isEmpty(goodsCode)) {
            view.popToast(view.getStringFromResource(R.string.pls_input_goods_code));
            return false;
        }
        String format = "%-"+ maxLen +"s"; /*格式化为右补空格*/
        String s = String.format(Locale.CHINA,format,goodsCode);

        dataMap.put(TradeInformationTag.ISO62_REQ,s);

        gotoNextStep();
        return true;
    }

    @Override
    public boolean onConfirmClicked(String... param) {
        Resources resources = view.getContext().getResources();
        if( bIsMagCashLoad() ){
            if ( null == param || param.length != 2 ){
                return false;
            }
            if( TextUtils.isEmpty(param[0]) ){
                view.popToast(view.getStringFromResource(R.string.label_cert_type));
                return false;
            }
            if(TextUtils.isEmpty(param[1]) ){
                view.popToast(view.getStringFromResource(R.string.label_plz_input_cert_no));
                return false;
            }
            int maxLen = view.getContext().getResources().getInteger(R.integer.cardholder_info_max_len);
            String format = "%-"+ maxLen +"s"; /*格式化为右补空格*/
            dataMap.put(TradeInformationTag.ISO62_REQ,param[0]+String.format(Locale.CHINA,format,param[1]));
            gotoNextStep();
        }
        else if( TransCode.RESERVATION_SALE.equals(mTradeInformation.getTransCode()) ){
            if ( null == param || param.length != 2 ){
                return false;
            }
            if( TextUtils.isEmpty(param[0]) || (param[0].length() < resources.getInteger(R.integer.reservation_phone_number_min_len)) ){
                view.popToast(String.format(Locale.CHINA,resources.getString(R.string.tip_reservation_phone_num_less),resources.getInteger(R.integer.reservation_phone_number_min_len)));
                return false;
            }
            if(TextUtils.isEmpty(param[1]) ||  param[1].length() < resources.getInteger(R.integer.reservation_number_max_len) ){
                view.popToast(String.format(Locale.CHINA,resources.getString(R.string.tip_reservation_num_less),resources.getInteger(R.integer.reservation_number_max_len)));
                return false;
            }

            dataMap.put(TradeInformationTag.SERVICE_ENTRY_MODE,"92");
            int maxLen = resources.getInteger(R.integer.phone_number_max_len);
            String format = "%-"+ maxLen +"s"; /*格式化为右补空格*/
            dataMap.put(TradeInformationTag.ISO62_REQ,"90"+String.format(Locale.CHINA,format,param[0])+param[1]);
            gotoNextStep();
        }
        else if( TransCode.OFFLINE_SETTLEMENT.equals(mTradeInformation.getTransCode())||TransCode.OFFLINE_ADJUST.equals(mTradeInformation.getTransCode()) ){
            if ( null == param || param.length != 4 ){
                return false;
            }
            if( param[0] == null ){
                view.popToast(R.string.tip_plz_select_auth_mode);
                return false;
            }
            if( param[3] == null ){
                view.popToast(R.string.tip_plz_select_credit_card_company_code);
                return false;
            }
            if( !(ConstDefine.OFFLINE_AUTH_CODE_POS.equals(param[0]) || ConstDefine.OFFLINE_AUTH_CODE_TELEPHONE.equals(param[0])
                    || ConstDefine.OFFLINE_AUTH_CODE_AUTH.equals(param[0])) ) {
                view.popToast(R.string.tip_plz_select_right_auth_mode);
                return false;
            }
            if( ConstDefine.OFFLINE_AUTH_CODE_POS.equals(param[0]) ){
                if( !(param[1].length() > 1 || param[1].length() <= resources.getInteger(R.integer.offline_auth_code_max_len))){
                    view.popToast("原授权码长度为2~6");
                    return false;
                }
                dataMap.put(TransDataKey.key_oriAuthCode,param[1]);
            }
            else if( ConstDefine.OFFLINE_AUTH_CODE_TELEPHONE.equals(param[0]) ){
                if(  resources.getInteger(R.integer.offline_original_code_max_len) != param[2].length() ){
                    view.popToast(String.format(Locale.CHINA,"原授权机构代码长度必须为%d",resources.getInteger(R.integer.offline_original_code_max_len)));
                    return false;
                }
                if( !(param[1].length() > 0 || param[1].length() <= resources.getInteger(R.integer.offline_auth_code_max_len))){
                    view.popToast("原授权码长度为2~6");
                    return false;
                }
                dataMap.put(TransDataKey.key_oriAuthCode,param[1]);
                dataMap.put(TradeInformationTag.ORIGINAL_AUTH_ORG_CODE,param[2]);
            }
            dataMap.put(TradeInformationTag.CREDIT_CARD_COMPANY_CODE,param[3]);
            dataMap.put(TradeInformationTag.ORIGINAL_AUTH_MODE,param[0]);
            gotoNextStep();
        }
        return true;
    }
}
