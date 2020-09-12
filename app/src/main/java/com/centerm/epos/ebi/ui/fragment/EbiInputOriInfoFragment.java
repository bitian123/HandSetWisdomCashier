package com.centerm.epos.ebi.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IBarCodeScanner;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.present.EbiInputOriginInfoPresenter;
import com.centerm.epos.mvp.presenter.IInputOriginInfoPresenter;
import com.centerm.epos.mvp.view.IInputOriginInfoView;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.view.NumberPad;

import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;


/**
 * 输入交易信息界面。例如消费撤销时要求输入原始交易流水；退货时要求输入检索参考号和交易日期；预授权完成和预授权撤销要求输入授权码。
 * author:wanliang527</br>
 * date:2017/3/2</br>
 * mo
 */
public class EbiInputOriInfoFragment extends BaseTradeFragment implements IInputOriginInfoView {
    public IInputOriginInfoPresenter presenter;
    private EditText posScanVoucherEdit, posSerialEdit, tradeRefNoEdit,posSerialEdit2,posBatchEdit,
            dateEdit, authCodeEdit, authDateEdit,cardDateEdit,oriPosOrderEdit;
    private ImageButton scanBtn,imgbtn_scan;
    private StringBuilder sNumberPad = new StringBuilder();

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new EbiInputOriginInfoPresenter(this);
        return presenter;
    }
    /*
    * @param view
    *
    * */
    private void initTopBarViewForInputVoucherNo(View view) {
        TextView txtView;
        RelativeLayout  layout_title;
        ImageButton imgbtnScan;

        if( !Settings.bIsSettingBlueTheme() ){
            return ;
        }

        txtView = (TextView)view.findViewById(R.id.txtvw_title);
        layout_title = (RelativeLayout)view.findViewById(R.id.layout_title);

        layout_title.setBackgroundResource(R.drawable.bg_topbar);

        ViewGroup.LayoutParams layoutParams = layout_title.getLayoutParams();
        layoutParams.height = getResources().getDimensionPixelSize(R.dimen.common_title_height);
        layout_title.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams txtViewReLayoutParams = new RelativeLayout.LayoutParams(txtView.getLayoutParams());
        txtViewReLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        txtViewReLayoutParams.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_80);
        txtView.setLayoutParams(txtViewReLayoutParams);

        ImageButton imgbtn_back = (ImageButton)view.findViewById(R.id.imgbtn_back);
        RelativeLayout.LayoutParams imgbtn_backParam = new RelativeLayout.LayoutParams(imgbtn_back.getLayoutParams());
        imgbtn_backParam.height = RelativeLayout.LayoutParams.WRAP_CONTENT;

        imgbtn_backParam.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_76);
        imgbtn_back.setLayoutParams(imgbtn_backParam);
    }

    private void numberPadForInputVoucherNo(View view,NumberPad numberPad) {
        final EditText ori_pos_serial_edit;
        ImageView titlePicShow = null;
        if( numberPad == null || !Settings.bIsSettingBlueTheme() ){
            return ;
        }

        titlePicShow = (ImageView)view.findViewById(R.id.title_pic_show);
        if( null != titlePicShow ) {
            titlePicShow.setImageResource(R.drawable.pic_voucher_no);
            titlePicShow.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.positive_btn).setVisibility(View.GONE);
        ori_pos_serial_edit = (EditText)view.findViewById(R.id.void_input_block).findViewById(R.id.ori_pos_serial_edit);
        ori_pos_serial_edit.clearFocus();
        ori_pos_serial_edit.setCursorVisible(false);

        ori_pos_serial_edit.setInputType(InputType.TYPE_NULL);

        numberPad.setVisibility(View.VISIBLE);
        numberPad.setCallback(new NumberPad.KeyCallback(){
            public void onPressKey(char i)
            {
                if( i == 'L' || i == '.' ){
                    return ;
                }
                if( i == (char)-1 ){
                    int len = ori_pos_serial_edit.getText().length();
                    if( len > 0 ) {
                        sNumberPad.deleteCharAt(len - 1);
                    }
                    else{
                        sNumberPad.setLength(0);
                    }
                }
                else if('\r' == i ){
                    if( ori_pos_serial_edit.getText().length() > 0 ){
                        inputData();
                    }
                }
                else{
                    if( ori_pos_serial_edit.getText().length() < 6 )
                        sNumberPad.append(i);
                }

                ori_pos_serial_edit.setText(sNumberPad.toString());
            }
        });

    }

    private void numberPadForInputVoucherNo(View view,NumberPad numberPad , EditText inputEditText, final int maxLen) {
        final EditText ori_pos_serial_edit;
        ImageView titlePicShow = null;
        if( numberPad == null ){
            return ;
        }

        titlePicShow = (ImageView)view.findViewById(R.id.title_pic_show);

        if( null != titlePicShow ) {
            titlePicShow.setImageResource(R.drawable.pic_voucher_no);
            titlePicShow.setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.positive_btn).setVisibility(View.GONE);
        ori_pos_serial_edit = inputEditText;
        ori_pos_serial_edit.clearFocus();
        ori_pos_serial_edit.setCursorVisible(false);

        ori_pos_serial_edit.setInputType(InputType.TYPE_NULL);

        numberPad.setVisibility(View.VISIBLE);
        numberPad.setCallback(new NumberPad.KeyCallback(){
            public void onPressKey(char i)
            {
                if( i == 'L' || i == '.' ){
                    return ;
                }
                if( i == (char)-1 ){
                    int len = ori_pos_serial_edit.getText().length();
                    if( len > 0 ) {
                        sNumberPad.deleteCharAt(len - 1);
                    }
                    else{
                        sNumberPad.setLength(0);
                    }
                }
                else if('\r' == i ){
                    if( ori_pos_serial_edit.getText().length() > 0 ){
                        inputData();
                    }
                }
                else{
                    if( ori_pos_serial_edit.getText().length() < maxLen )
                        sNumberPad.append(i);
                }

                ori_pos_serial_edit.setText(sNumberPad.toString());
            }
        });

    }

    @Override
    protected void onInitView(View view) {
        String transCode = presenter.getTradeCode();
        NumberPad numberPad = null;

        if( Settings.bIsSettingBlueTheme() ) {
            numberPad = (NumberPad) view.findViewById(R.id.number_pad_show);
        }

        if( !TransCode.VOID.equals(transCode)
            && !TransCode.COMPLETE_VOID.equals(transCode)
            &&!TransCode.VOID_SCAN.equals(transCode)
            &&!TransCode.SCAN_VOID.equals(transCode)
            &&!TransCode.VOID_INSTALLMENT.equals(transCode)){
            initTopBarViewForInputVoucherNo(view);
            if( null != numberPad ){
                numberPad.setVisibility(View.GONE);
            }
        }
        switch (transCode) {
            case TransCode.VOID:
			case TransCode.VOID_INSTALLMENT:
                numberPadForInputVoucherNo(view,numberPad);
                view.findViewById(R.id.void_input_block).setVisibility(View.VISIBLE);
                view.findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                break;
            case TransCode.VOID_SCAN:
            case TransCode.SCAN_VOID:
                scanBtn = (ImageButton) view.findViewById(R.id.imgbtn_scan);
                view.findViewById(R.id.void_input_scan).setVisibility(View.VISIBLE);
                posScanVoucherEdit = (EditText) view.findViewById(R.id.ori_pos_scan_code_edit);
                numberPadForInputVoucherNo(view,numberPad,posScanVoucherEdit,getResources().getInteger(R.integer.scan_code_max_len));
                break;
            case TransCode.REFUND_SCAN:
                scanBtn = (ImageButton) view.findViewById(R.id.imgbtn_scan);
                view.findViewById(R.id.refund_input_scan).setVisibility(View.VISIBLE);
                posScanVoucherEdit = (EditText) view.findViewById(R.id.et_ori_pos_scan_code);
                tradeRefNoEdit = (EditText) view.findViewById(R.id.et_trade_ref_no);
                dateEdit = (EditText) view.findViewById(R.id.et_origin_date);
                if( Settings.bIsSettingBlueTheme() ) {
                    ((RelativeLayout) view.findViewById(R.id.calendarRefundScan)).setOnClickListener(this);
                }
                break;
            case TransCode.REFUND:
                view.findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.void_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.refund_input_block).setVisibility(View.VISIBLE);
                if( Settings.bIsSettingBlueTheme() ) {
                    ((RelativeLayout) view.findViewById(R.id.calendarRefund)).setOnClickListener(this);
                }
                break;
            case TransCode.CANCEL:
            case TransCode.AUTH_COMPLETE:
            case TransCode.AUTH_SETTLEMENT:
                view.findViewById(R.id.auth_input_block).setVisibility(View.VISIBLE);
                view.findViewById(R.id.void_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                if(presenter.isInputCardValideDate()) {
                    view.findViewById(R.id.tv_card_date).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.orig_card_date_edit).setVisibility(View.VISIBLE);
                }
                break;
            case TransCode.COMPLETE_VOID:
                numberPadForInputVoucherNo(view,numberPad);
                view.findViewById(R.id.void_input_block).setVisibility(View.VISIBLE);
                view.findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                break;
            case SALE_SCAN_VOID://订单撤销
                view.findViewById(R.id.imgbtn_scan).setVisibility(View.VISIBLE);
                view.findViewById(R.id.imgbtn_scan).setOnClickListener(this);
                view.findViewById(R.id.scan_void_input_block).setVisibility(View.VISIBLE);
                break;
            case SALE_SCAN_REFUND://订单退货
                view.findViewById(R.id.imgbtn_scan).setVisibility(View.VISIBLE);
                view.findViewById(R.id.imgbtn_scan).setOnClickListener(this);
                view.findViewById(R.id.scan_void_input_block).setVisibility(View.VISIBLE);
                break;
        }

        if (scanBtn != null) {
            scanBtn.setVisibility(View.VISIBLE);
            scanBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if(!Settings.bIsSettingBlueTheme()){
                        IBarCodeScanner dev = (IBarCodeScanner) DeviceFactory.getInstance().getDevice(EnumDeviceType
                                .BAR_CODE_SCANNER_DEV);
                        if (dev == null) {
                            return;
                        }
                        dev.scanBarCode(getHostActivity());
                    }else{
                        new com.centerm.epos.transcation.pos.manager.ScanCodeTrade().scanBarCode(getHostActivity());
                    }
                }
            });
        }


        posSerialEdit = (EditText) view.findViewById(R.id.ori_pos_serial_edit);
        if (tradeRefNoEdit == null)
            tradeRefNoEdit = (EditText) view.findViewById(R.id.trade_ref_no_edit);
        if (dateEdit == null) {
            dateEdit = (EditText) view.findViewById(R.id.origin_date_edit);
            dateEdit.setOnClickListener(this);
        }
        authCodeEdit = (EditText) view.findViewById(R.id.orig_auth_code_edit);
        authDateEdit = (EditText) view.findViewById(R.id.orig_auth_date_edit);
        cardDateEdit = (EditText) view.findViewById(R.id.orig_card_date_edit);
        oriPosOrderEdit = (EditText) view.findViewById(R.id.ori_pos_order_edit);
        if( Settings.bIsSettingBlueTheme() ) {
            ((RelativeLayout) view.findViewById(R.id.calendarRelativeLayout)).setOnClickListener(this);
        }
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
        authDateEdit.setOnClickListener(this);

        if(TransCode.AUTH_COMPLETE.equals(transCode)||TransCode.CANCEL.equals(transCode)){
            initAuthView(view);
        }
    }

    private void initAuthView(View view){
        posSerialEdit2 = (EditText) view.findViewById(R.id.orig_pos_serial2);
        posBatchEdit = (EditText) view.findViewById(R.id.orig_pos_batch_edit);
        posSerialEdit2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length()==6){
                    EbiInputOriginInfoPresenter iPresenter = (EbiInputOriginInfoPresenter) presenter;
                    TradeInfoRecord oriInfo = iPresenter.onQuery(posSerialEdit2.getText().toString().trim());
                    if(oriInfo!=null){
                        posSerialEdit.setText(oriInfo.getVoucherNo());
                        posBatchEdit.setText(oriInfo.getBatchNo());
                        authCodeEdit.setText(oriInfo.getAuthorizeNo());
                        authDateEdit.setText(oriInfo.getTransDate());
                    }
                }
            }
        });
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_origin_info_ebi;
    }
    /*
    * zhouzhihua
    * */
    private void inputData() {
        String scanVoucherNo="";
        if (posScanVoucherEdit != null)
            scanVoucherNo = posScanVoucherEdit.getText().toString();
        String posSerial = posSerialEdit.getText().toString();
        String platSerial = tradeRefNoEdit.getText().toString();
        String date = dateEdit.getText().toString();
        String authCode = authCodeEdit.getText().toString();
        String authDate = authDateEdit.getText().toString();
        String cardDate = cardDateEdit.getText().toString();
        presenter.onConfirmClicked(scanVoucherNo, posSerial, platSerial, date, authCode, authDate,cardDate);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.positive_btn) {
            String scanVoucherNo="";
            if (posScanVoucherEdit != null)
                scanVoucherNo = posScanVoucherEdit.getText().toString();
            if (oriPosOrderEdit != null)
                scanVoucherNo = oriPosOrderEdit.getText().toString();
            String posSerial = posSerialEdit.getText().toString();
            if(TextUtils.isEmpty(posSerial)&&posSerialEdit2!=null){
                posSerial = posSerialEdit2.getText().toString();
            }
            String platSerial = tradeRefNoEdit.getText().toString();
            String date = dateEdit.getText().toString();
            String authCode = authCodeEdit.getText().toString();
            String authDate = authDateEdit.getText().toString();
            String cardDate = cardDateEdit.getText().toString();
            mTradePresent.getTransData().put(TransDataKey.key_oriVoucherNumber, posSerialEdit.getText().toString().trim());
            if(posBatchEdit!=null){
                mTradePresent.getTransData().put(JsonKey.ORI_BATCH_NO, posBatchEdit.getText().toString().trim());
            }
            presenter.onConfirmClicked(scanVoucherNo, posSerial, platSerial, date, authCode, authDate,cardDate);
        }else if(R.id.imgbtn_scan==v.getId()){
//            IBarCodeScanner dev = (IBarCodeScanner) DeviceFactory.getInstance().getDevice(EnumDeviceType
//                    .BAR_CODE_SCANNER_DEV);
//            if (dev == null) {
//                return;
//            }
//            dev.scanBarCode(getHostActivity());
            new com.centerm.epos.transcation.pos.manager.ScanCodeTrade().scanBarCode(getHostActivity());
        }else if( Settings.bIsSettingBlueTheme()
                && (v.getId() == R.id.calendarRelativeLayout
                || R.id.calendarRefund == v.getId()
                || R.id.calendarRefundScan == v.getId()) ) {
            getDate();
        }
        else if(R.id.orig_auth_date_edit == v.getId()) {
            authDateEdit.setCursorVisible(true);
        }
        else {
            super.onClick(v);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //显示扫描到的内容
        if (data == null)
            return;
        String content = data.getStringExtra("txtResult");
        if (!TextUtils.isEmpty(content)) {
            oriPosOrderEdit.setText(content);
        }
    }

    private void getDate() {
        java.util.Calendar dateAndTime = java.util.Calendar.getInstance();

        new DatePickerDialog(EbiInputOriInfoFragment.this.getContext(),android.app.AlertDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener(){
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){

                        String mm = String.format("%02d",month+1);
                        String dd = String.format("%02d",dayOfMonth);

                        if( TransCode.REFUND.equals(presenter.getTradeCode())
                                ||TransCode.REFUND_SCAN.equals(presenter.getTradeCode()))
                        {
                            EbiInputOriInfoFragment.this.dateEdit.setText(mm + dd);
                            EbiInputOriInfoFragment.this.dateEdit.setCursorVisible(false);
                            EbiInputOriInfoFragment.this.dateEdit.setSelection(EbiInputOriInfoFragment.this.dateEdit.getText().length());
                        }
                        else {
                            EbiInputOriInfoFragment.this.authDateEdit.setText(mm + dd);
                            EbiInputOriInfoFragment.this.authDateEdit.setCursorVisible(false);
                            EbiInputOriInfoFragment.this.authDateEdit.setSelection(EbiInputOriInfoFragment.this.authDateEdit.getText().length());
                        }


                    }
                },
                dateAndTime.get(java.util.Calendar.YEAR),
                dateAndTime.get(java.util.Calendar.MONTH),
                dateAndTime.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public boolean onBackPressed() {
        ActivityStack.getInstance().pop();
        return true;
    }

}
