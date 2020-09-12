package com.centerm.epos.fragment.trade;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.present.transaction.CheckCardPresent;
import com.centerm.epos.present.transaction.IScanQRCode;
import com.centerm.epos.present.transaction.ScanQRCodePresent;


/**
 * Created by yuhc on 2017/4/22.
 */

public class ScanQRCodeFragment extends BaseTradeFragment {
    private IScanQRCode mScanQRCodePresent;

    @Override
    protected void onInitView(View view) {

    }

    @Override
    protected ITradePresent newTradePresent() {
        ScanQRCodePresent present = new ScanQRCodePresent(this);
        mScanQRCodePresent = present;
        return present;
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_scan_qrcode;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //显示扫描到的内容
        if (data == null) {
            mTradePresent.gotoPreStep();
            return;
        }
        String content = data.getStringExtra("txtResult");
        if (!TextUtils.isEmpty(content)) {
            if (content.matches("[0-9]+")) {
                mScanQRCodePresent.onGetScanCode(content);
            } else {
                Toast.makeText(getContext(), "只支持数字！", Toast.LENGTH_SHORT).show();
                mTradePresent.gotoPreStep();
            }
        }
    }
}
