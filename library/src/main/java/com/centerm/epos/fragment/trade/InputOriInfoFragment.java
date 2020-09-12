package com.centerm.epos.fragment.trade;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.EnumDeviceType;
import com.centerm.cpay.midsdk.dev.define.IBarCodeScanner;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.mvp.presenter.IInputOriginInfoPresenter;
import com.centerm.epos.mvp.presenter.InputOriginInfoPresenter;
import com.centerm.epos.mvp.view.IInputOriginInfoView;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.NumberPad;
import com.centerm.epos.utils.CommonUtils;


/**
 * 输入交易信息界面。例如消费撤销时要求输入原始交易流水；退货时要求输入检索参考号和交易日期；预授权完成和预授权撤销要求输入授权码。
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class InputOriInfoFragment extends BaseTradeFragment implements IInputOriginInfoView {

    public IInputOriginInfoPresenter presenter;
    private EditText posScanVoucherEdit, posSerialEdit, tradeRefNoEdit, dateEdit, authCodeEdit, authDateEdit,cardDateEdit;
    private ImageButton scanBtn;
    private StringBuilder sNumberPad = new StringBuilder();
    private EditText ecPosSerialEdit, ecDateEdit, ecBatchEdit, ecOriTermNo;

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new InputOriginInfoPresenter(this);
        return presenter;
    }
    /*
    * @param view
    *
    * */
    private void initTopBarViewForInputVoucherNo(View view)
    {
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

        imgbtn_backParam.topMargin = getResources().getDimensionPixelSize(R.dimen.margin_70);
        imgbtn_back.setLayoutParams(imgbtn_backParam);
    }
    private void numberPadForInputVoucherNo(View view,NumberPad numberPad)
    {
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

    private void numberPadForInputVoucherNo(View view,NumberPad numberPad , EditText inputEditText, final int maxLen)
    {
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

    /**
     * author zhouzhihua
     * {@link TransCode#TRANS_VOID_ENDWITH } 用来表示所有的撤销类的交易 所有撤销类交易 只输入凭证号 <br/>
     * @param view View
     */
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
            &&!TransCode.VOID_INSTALLMENT.equals(transCode)
            &&!TransCode.EC_VOID_CASH_LOAD.equals(transCode)
            &&!TransCode.ISS_INTEGRAL_VOID.equals(transCode)
            &&!TransCode.UNION_INTEGRAL_VOID.equals(transCode)
            && !transCode.endsWith(TransCode.TRANS_VOID_ENDWITH)
            && !TransCode.OFFLINE_ADJUST.equals(transCode) ){
            initTopBarViewForInputVoucherNo(view);
            if( null != numberPad ){
                numberPad.setVisibility(View.GONE);
            }
        }
        switch (transCode) {
            case TransCode.VOID:
			case TransCode.VOID_INSTALLMENT:
            case TransCode.EC_VOID_CASH_LOAD:
            case TransCode.ISS_INTEGRAL_VOID:
            case TransCode.UNION_INTEGRAL_VOID:
            case TransCode.RESERVATION_VOID:
            case TransCode.OFFLINE_ADJUST:
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
            case TransCode.UNION_INTEGRAL_REFUND:
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
            case TransCode.E_REFUND:
                view.findViewById(R.id.void_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.refund_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.auth_input_block).setVisibility(View.GONE);
                view.findViewById(R.id.ec_refund_input_block).setVisibility(View.VISIBLE);
                ecBatchEdit = (EditText) view.findViewById(R.id.orig_pos_batch_edit);
                ecPosSerialEdit = (EditText) view.findViewById(R.id.orig_pos_serial_edit);
                ecOriTermNo = (EditText) view.findViewById(R.id.ori_pos_term_no_edit);
                ecDateEdit = (EditText) view.findViewById(R.id.ec_origin_date_edit);

                if( Settings.bIsSettingBlueTheme() ) {
                    ((RelativeLayout) view.findViewById(R.id.ec_calendarRefund)).setOnClickListener(this);
                }
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
        if( Settings.bIsSettingBlueTheme() ) {
            ((RelativeLayout) view.findViewById(R.id.calendarRelativeLayout)).setOnClickListener(this);
        }
        view.findViewById(R.id.positive_btn).setOnClickListener(this);
        authDateEdit.setOnClickListener(this);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_input_origin_info;
    }
    /*
    * zhouzhihua
    * */
    private void inputData()
    {
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
            String posSerial = posSerialEdit.getText().toString();
            String platSerial = tradeRefNoEdit.getText().toString();
            String date = dateEdit.getText().toString();
            String authCode = authCodeEdit.getText().toString();
            String authDate = authDateEdit.getText().toString();
            String cardDate = cardDateEdit.getText().toString();
            if( TransCode.E_REFUND.equals(presenter.getTradeCode()) ){
                scanVoucherNo = ecOriTermNo.getText().toString();
                posSerial = ecPosSerialEdit.getText().toString();
                date = ecDateEdit.getText().toString();
                platSerial = ecBatchEdit.getText().toString();
            }
            presenter.onConfirmClicked(scanVoucherNo, posSerial, platSerial, date, authCode, authDate,cardDate);
        }
        else if( Settings.bIsSettingBlueTheme()
                && (v.getId() == R.id.calendarRelativeLayout
                || R.id.calendarRefund == v.getId()
                || R.id.calendarRefundScan == v.getId()
                || R.id.ec_calendarRefund == v.getId()) ) {
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
            if (content.matches("[0-9]+")) {
                posScanVoucherEdit.setText(content);
            } else
                Toast.makeText(getContext(), "只支持数字！", Toast.LENGTH_SHORT).show();
        }
    }
    private void getDate()
    {
        java.util.Calendar dateAndTime = java.util.Calendar.getInstance();


        new DatePickerDialog(InputOriInfoFragment.this.getContext(),android.app.AlertDialog.THEME_HOLO_LIGHT,
                new DatePickerDialog.OnDateSetListener(){
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth){

                        String mm = String.format("%02d",month+1);
                        String dd = String.format("%02d",dayOfMonth);

                        if( TransCode.REFUND.equals(presenter.getTradeCode())
                                || TransCode.REFUND_SCAN.equals(presenter.getTradeCode())
                                || TransCode.UNION_INTEGRAL_REFUND.equals(presenter.getTradeCode()))
                        {
                            InputOriInfoFragment.this.dateEdit.setText(mm + dd);
                            InputOriInfoFragment.this.dateEdit.setCursorVisible(false);
                            InputOriInfoFragment.this.dateEdit.setSelection(InputOriInfoFragment.this.dateEdit.getText().length());
                        }
                        else if( TransCode.E_REFUND.equals(presenter.getTradeCode()) ){
                            InputOriInfoFragment.this.ecDateEdit.setText(mm + dd);
                            InputOriInfoFragment.this.ecDateEdit.setCursorVisible(false);
                            InputOriInfoFragment.this.ecDateEdit.setSelection(InputOriInfoFragment.this.ecDateEdit.getText().length());
                        }
                        else {
                            InputOriInfoFragment.this.authDateEdit.setText(mm + dd);
                            InputOriInfoFragment.this.authDateEdit.setCursorVisible(false);
                            InputOriInfoFragment.this.authDateEdit.setSelection(InputOriInfoFragment.this.authDateEdit.getText().length());
                        }


                    }
                },
                dateAndTime.get(java.util.Calendar.YEAR),
                dateAndTime.get(java.util.Calendar.MONTH),
                dateAndTime.get(java.util.Calendar.DAY_OF_MONTH)).show();
    }



}
