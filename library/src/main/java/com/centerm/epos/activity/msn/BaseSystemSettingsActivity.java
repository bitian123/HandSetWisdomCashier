package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.keys.Keys;

import config.BusinessConfig;

/**
 * 《基础版本》
 * 系统参数配置
 * Created by ysd on 2016/11/30.
 */
public class BaseSystemSettingsActivity extends BaseActivity {

    private EditText batchNo;
    private EditText batchFlowNo, etSlipCopys;
    private EditText maxCount;
    private Button modify;

    @Override
    public int onLayoutId() {
        return R.layout.activity_system_setting;
    }

    @Override
    public void onInitView() {
        final boolean hasSignined = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key.FLAG_SIGN_IN);
        TextView txtvw = (TextView) findViewById(R.id.txtvw_title);
        txtvw.setText(R.string.label_system);
        batchNo = (EditText) findViewById(R.id.extxt_batch_no);
        batchFlowNo = (EditText) findViewById(R.id.extxt_batch_flow_no);
        maxCount = (EditText) findViewById(R.id.extxt_max_count);
        if (hasSignined) {
            batchNo.setEnabled(false);
            batchNo.setTextColor(getResources().getColor(R.color.font_hint2));

            maxCount.setEnabled(false);
            maxCount.setTextColor(getResources().getColor(R.color.font_hint2));
        } else {
            batchNo.setEnabled(true);
            batchNo.setTextColor(getResources().getColor(R.color.font_black));

            maxCount.setEnabled(true);
            maxCount.setTextColor(getResources().getColor(R.color.font_black));
        }

        etSlipCopys = (EditText) findViewById(R.id.extxt_slip_copys);
        modify = (Button) findViewById(R.id.modify_system);
        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String batchNoStr = batchNo.getText().toString().trim();
                String batchFlowNoStr = batchFlowNo.getText().toString().trim();
                String slipCopys = etSlipCopys.getText().toString().trim();
                String maxCountStr = maxCount.getText().toString().trim();

                if (null == batchNoStr || "".equals(batchNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_null));
                    return;
                }
                if (null == batchFlowNoStr || "".equals(batchFlowNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_flow_not_null));
                    return;
                }
                if (null == maxCountStr || "".equals(maxCountStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_max_tran_count));
                    return;
                }
                if (null == maxCountStr || "0".equals(maxCountStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_max_tran_not_zero));
                    return;
                }
                if ( TextUtils.isEmpty(slipCopys) ){
                    ViewUtils.showToast(context, getString(R.string.tip_slip_copys_not_null));
                    return;
                }
                if (batchNoStr.length() < 6) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_number));
                    return;
                }
                if (batchFlowNoStr.length() < 6) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_flow));
                    return;
                }
                if ("000000".equals(batchNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_zero));
                    return;
                }

                if ("000000".equals(batchFlowNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_flow_not_zero));
                    return;
                }
                if ("999999".equals(batchNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_number_not_more));
                    return;
                }

                if ("999999".equals(batchFlowNoStr)) {
                    ViewUtils.showToast(context, getString(R.string.tip_batch_flow_not_more));
                    return;
                }

                BusinessConfig config = BusinessConfig.getInstance();
                config.setBatchNo(context, batchNo.getText().toString().trim());
                config.setPosSerial(context, batchFlowNo.getText().toString().trim());
                config.setNumber(context, Keys.obj().printnum, Integer.parseInt(slipCopys));
                config.setNumber(context, BusinessConfig.Key.KEY_MAX_TRANSACTIONS, Integer.parseInt(maxCountStr));
                ViewUtils.showToast(context, getString(R.string.tip_save_success));
                activityStack.pop();
            }
        });

    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        String batchNoStr = BusinessConfig.getInstance().getBatchNo(context);
        String terFlowStr = BusinessConfig.getInstance().getPosSerial(context, false, false);
        int maxRecords = BusinessConfig.getInstance().getNumber(context, BusinessConfig.Key.KEY_MAX_TRANSACTIONS);
        int slipCopys = BusinessConfig.getInstance().getNumber(context, Keys.obj().printnum);
        if (null != batchNoStr) {
            batchNo.setText(batchNoStr);
        }
        if (null != terFlowStr) {
            batchFlowNo.setText(terFlowStr);
        }
        etSlipCopys.setText(""+slipCopys);
        maxCount.setText(String.valueOf(maxRecords));
    }
}
