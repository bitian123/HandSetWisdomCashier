package com.centerm.epos.fragment.sys;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.e10.EEPROM;

import config.BusinessConfig;

/**
 * create by liubit on 2019-09-06
 */
public class MerchantInfoFragment extends BaseFragment {
    private TextView merchantNo;
    private TextView bankTermNo;
    private TextView merchantName;
    private TextView extxt_term_no;
    private Handler mHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            extxt_term_no.setText(bundle.getString("num"));
        }
    };

    @Override
    protected void onInitView(View view) {
        merchantNo = (TextView) view.findViewById(R.id.extxt_marchant_number);
        bankTermNo = (TextView) view.findViewById(R.id.extxt_bank_term_no);
        merchantName = (TextView) view.findViewById(R.id.extxt_merchant_name);
        extxt_term_no = (TextView) view.findViewById(R.id.extxt_term_no);

        BusinessConfig config = BusinessConfig.getInstance();
        String iso41 = config.getIsoField(getActivity(), 41);
        String iso42 = config.getIsoField(getActivity(), 42);
        String iso43 = config.getValue(getActivity(), BusinessConfig.Key.KEY_MCHNT_NAME);
        bankTermNo.setText(iso41);//终端号
        merchantNo.setText(iso42);//商户号
        merchantName.setText(iso43);//商户名

        extxt_term_no.setText(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.E10_SN));
        //mThread.start();
    }

    Thread mThread = new Thread() {
        @Override
        public void run() {
            Message msg = new Message();
            Bundle bundle = new Bundle();

            try {
                bundle.putString( "date", EEPROM.read(EEPROM.PRODUCDATE));
                bundle.putString( "num", EEPROM.read(EEPROM.PRODUCTSN));
            } catch (Exception e) {
                e.printStackTrace();
            }

            msg.setData( bundle );
            mHanlder.sendMessage(msg);
        }
    };

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_mer_info;
    }
}
