package com.centerm.epos.fragment.trade;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.present.transaction.IInputCommonInfoPresenter;
import com.centerm.epos.present.transaction.InputCommonInfoPresenter;
import com.centerm.epos.utils.XLogUtil;

import java.util.Locale;

/**
 * 输入交易信息界面。积分消费商品编码。
 * author:zhouzhihua</br>
 * date:2017/1/2</br>
 */
public class InputCommonInfoFragment extends BaseTradeFragment implements RadioGroup.OnCheckedChangeListener {

    public IInputCommonInfoPresenter presenter;
    private EditText editText;
    private LinearLayout integralLayout,linearLayout,reservationLayout,offlineLayout,offline_pos_layout,offline_tel_layout;
    private RadioGroup radioGroup1,radioGroup2,offline_auth_mode;
    private String cardHolderCertNo;
    private EditText edit_cardholder,edit_ori_auth_code,edit_auth_org_code;
    private EditText phoneNumberEdit,reservationNumberEdit;
    private String companyCode;

    @Override
    protected ITradePresent newTradePresent() {

        presenter = new InputCommonInfoPresenter(this);
        return presenter;
    }


    private void initIntegralUI(View view){
        integralLayout = (LinearLayout)view.findViewById(R.id.integral_input_layout);
        if( presenter.bIsIntegralTrans() ) {
            integralLayout.setVisibility(View.VISIBLE);
            editText = (EditText) integralLayout.findViewById(R.id.edit_integral_code);
        }
        else{
            integralLayout.setVisibility(View.GONE);
        }
    }

    private void initMagCashLoadUI(View view){
        linearLayout = (LinearLayout)view.findViewById(R.id.mag_input_layout);
        if( presenter.bIsMagCashLoad() ) {
            linearLayout.setVisibility(View.VISIBLE);
            radioGroup1 = (RadioGroup) linearLayout.findViewById(R.id.radiogroup1);
            radioGroup2 = (RadioGroup) linearLayout.findViewById(R.id.radiogroup2);
            radioGroup2.setOnCheckedChangeListener(this);
            radioGroup1.setOnCheckedChangeListener(this);
            edit_cardholder  = (EditText) linearLayout.findViewById(R.id.edit_cardholder);
        }
        else{
            linearLayout.setVisibility(View.GONE);
        }
    }
    public boolean bIsReservationSale()
    {
        return TransCode.RESERVATION_SALE.equals(mTradePresent.getTradeCode());
    }
    private void initReservationUI(View view)
    {
        reservationLayout = (LinearLayout) view.findViewById(R.id.reservation_input_layout);
        if( bIsReservationSale() ){
            reservationLayout.setVisibility(View.VISIBLE);
            phoneNumberEdit = (EditText) reservationLayout.findViewById(R.id.edit_number);
            reservationNumberEdit = (EditText) reservationLayout.findViewById(R.id.edit_reservation_number);
        }
        else{
            reservationLayout.setVisibility(View.GONE);
        }
    }
    /**
     *判断是否是离线交易
     * @return false or true <br/>
     * */
    public boolean bIsOfflineTrans()
    {
        return TransCode.OFFLINE_SETTLEMENT.equals(mTradePresent.getTradeCode())||TransCode.OFFLINE_ADJUST.equals(mTradePresent.getTradeCode());
    }
    private void initOfflineUI(View view)
    {
        offlineLayout = (LinearLayout) view.findViewById(R.id.offline_input_layout);
        if( bIsOfflineTrans() ){
            offlineLayout.setVisibility(View.VISIBLE);
            radioGroup1 = (RadioGroup)offlineLayout.findViewById(R.id.credit_card_company_code1);
            radioGroup2 = (RadioGroup)offlineLayout.findViewById(R.id.credit_card_company_code2);
            offline_auth_mode = (RadioGroup)offlineLayout.findViewById(R.id.offline_auth_mode);

            offline_pos_layout = (LinearLayout) view.findViewById(R.id.offline_pos_layout);

            offline_tel_layout = (LinearLayout) view.findViewById(R.id.offline_telephone_layout);

            edit_ori_auth_code = (EditText) offlineLayout.findViewById(R.id.edit_ori_auth_code);
            edit_auth_org_code = (EditText) offlineLayout.findViewById(R.id.edit_auth_org_code);
            offline_auth_mode.setOnCheckedChangeListener(this);
            radioGroup2.setOnCheckedChangeListener(this);
            radioGroup1.setOnCheckedChangeListener(this);
        }
        else{
            offlineLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId){
        RadioGroup radioGroup = null;
        boolean bIsChecked = false;
        if( group.getId() == R.id.offline_auth_mode ){

            if( checkedId == R.id.offline_auth_mode_auth ){
                offline_pos_layout.setVisibility(View.GONE);
                offline_tel_layout.setVisibility(View.GONE);
            }
            else if(checkedId == R.id.offline_auth_mode_telephone){
                offline_pos_layout.setVisibility(View.VISIBLE);
                offline_tel_layout.setVisibility(View.VISIBLE);
                edit_auth_org_code.requestFocus();
            }
            else{
                offline_pos_layout.setVisibility(View.VISIBLE);
                offline_tel_layout.setVisibility(View.GONE);
            }
            edit_ori_auth_code.setText("");
            edit_auth_org_code.setText("");
            radioGroup2.clearCheck();
            radioGroup1.clearCheck();
            return ;
        }
        /*
        * 磁条卡充值*/
        if( checkedId == R.id.id_id || checkedId==R.id.id_coo || checkedId==R.id.id_rp|| checkedId==R.id.id_passport
            || checkedId == R.id.id_mtp |checkedId==R.id.id_sc ||checkedId==R.id.id_poc ){

            radioGroup = group.getId() == R.id.radiogroup1 ? radioGroup2 : (group.getId()==R.id.radiogroup2 ? radioGroup1 :null);
            bIsChecked = ((RadioButton)linearLayout.findViewById(checkedId)).isChecked();
            if( bIsChecked ) {
                cardHolderCertNo = presenter.getMagCertNo(checkedId);
             }
        }
        if( checkedId == R.id.cupRadioButton || checkedId==R.id.visRadioButton || checkedId==R.id.mccRadioButton|| checkedId==R.id.maeRadioButton
                || checkedId == R.id.jcbRadioButton |checkedId==R.id.dccRadioButton ||checkedId==R.id.amxRadioButton ){

            radioGroup = group.getId() == R.id.credit_card_company_code1 ? radioGroup2 : (group.getId()==R.id.credit_card_company_code2 ? radioGroup1 :null);
            bIsChecked = ((RadioButton)offlineLayout.findViewById(checkedId)).isChecked();
            if( bIsChecked ) {
                companyCode = ((RadioButton)offlineLayout.findViewById(checkedId)).getText().toString().trim();
            }
        }
        if( radioGroup!=null && bIsChecked) {
            XLogUtil.w("onCheckedChanged","getId:"+group.getId() + " radioGroup1:"+R.id.radiogroup1 + " radioGroup2:"+R.id.radiogroup2);
            radioGroup.clearCheck();
        }
    }

    @Override
    protected void onInitView(View view) {
        XLogUtil.w("onCheckedChanged","getTradeCode():"+presenter.getTradeCode());
        initIntegralUI(view);
        initMagCashLoadUI(view);
        initReservationUI(view);
        initOfflineUI(view);
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_common_info;
    }
    /**
     * {@link ConstDefine#OFFLINE_AUTH_CODE_POS} 离线授权方式 pos<br/>
     * {@link ConstDefine#OFFLINE_AUTH_CODE_TELEPHONE}离线授权方式 电话<br/>
     * {@link ConstDefine#OFFLINE_AUTH_CODE_AUTH}离线授权方式 代授权<br/>
     */
    @Override
    public void onClick(View v) {
        String[] code;
        if (v.getId() == R.id.positive_btn) {
            if( presenter.bIsIntegralTrans() ){
                String goodsCode = editText.getText().toString();
                presenter.onConfirmClicked(goodsCode);
            }
            else if( presenter.bIsMagCashLoad() ){
                code = new String[2];
                code[0] = cardHolderCertNo;
                code[1] = edit_cardholder.getText().toString();
                presenter.onConfirmClicked(code);
            }
            else if( bIsReservationSale() ){
                code = new String[2];
                code[0] = phoneNumberEdit.getText().toString();
                code[1] = reservationNumberEdit.getText().toString();
                presenter.onConfirmClicked(code);
            }
            else if( bIsOfflineTrans() ){
                code = new String[4];
                if(((RadioButton)offline_auth_mode.findViewById(R.id.offline_auth_mode_pos)).isChecked()){
                    code[0] = ConstDefine.OFFLINE_AUTH_CODE_POS;
                    code[1] = String.format(Locale.CHINA,"%-6.6s",edit_ori_auth_code.getText().toString().trim());
                }
                else if(((RadioButton)offline_auth_mode.findViewById(R.id.offline_auth_mode_telephone)).isChecked()){
                    code[0] = ConstDefine.OFFLINE_AUTH_CODE_TELEPHONE;
                    code[1] = String.format(Locale.CHINA,"%-6.6s",edit_ori_auth_code.getText().toString().trim());
                    code[2] = edit_auth_org_code.getText().toString().trim() ;
                }
                else if(((RadioButton)offline_auth_mode.findViewById(R.id.offline_auth_mode_auth)).isChecked()){
                    code[0] = ConstDefine.OFFLINE_AUTH_CODE_AUTH;
                    code[1] = "";
                    code[2] = "";
                }
                code[3] = companyCode;
                XLogUtil.w("onCheckedChanged","companyCode:"+companyCode + " auth code:"+code[0] + " code[1]:"+code[1]);
                presenter.onConfirmClicked(code);
            }
        } else {
            super.onClick(v);
        }
    }
}
