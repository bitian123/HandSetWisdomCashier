package com.centerm.epos.activity.msn;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.iso.Iso62Qps;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.utils.ViewUtils;

import java.util.Locale;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/7/18.
 * QPS参数设置
 */

public class BaseQPSSettingActivity extends BaseActivity {

    private EditText etNoPwdAmount;
    private EditText etNoSignAmount;

    private CheckBox cbQPSToggle;
    private CheckBox cbCDCVMToggle;
    private CheckBox cbNoSignToggle;
    private CheckBox mCBWavePreferToggle;

    private RadioGroup mQpsBINRadionGroup;
    private RelativeLayout mQpsBINLayout;

    Iso62Qps mQpsParam;

    @Override
    public int onLayoutId() {
        return R.layout.activity_configure_qps;
    }

    @Override
    public void onInitView() {
        mQpsParam = TradeModelImpl.getInstance().getQpsParams();
        holdView();
        initViewData();
    }

    private void initViewData() {
        TextView tv_title = (TextView) findViewById(R.id.txtvw_title);
        tv_title.setText(R.string.label_configure_qps);

        if (mQpsParam == null)
            return;
        etNoPwdAmount.setText(String.format(Locale.CHINA, "%.2f", mQpsParam.getNoPinLimit()));
        etNoSignAmount.setText(String.format(Locale.CHINA, "%.2f", mQpsParam.getNoSignLimit()));

        cbQPSToggle.setChecked(mQpsParam.isNoPinOn());
        cbQPSToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mQpsParam.setFF8054("1");
                    if (mQpsBINLayout.getVisibility() == View.GONE) {
                        mQpsBINLayout.setVisibility(View.VISIBLE);
                        initBINChecked();
                    }
                } else {
                    if (mQpsBINLayout.getVisibility() == View.VISIBLE)
                        mQpsBINLayout.setVisibility(View.GONE);
                }
            }
        });

        cbCDCVMToggle.setChecked("1".equals(mQpsParam.getFF8057()));
        cbNoSignToggle.setChecked(mQpsParam.isNoSignOn());
        mCBWavePreferToggle.setChecked(BusinessConfig.getInstance().getFlag(this, BusinessConfig.Key.FLAG_PREFER_CLSS));
        initBINChecked();
    }

    private void initBINChecked() {
        int stage = mQpsParam.getPromotionStage();
        switch (stage) {
            case 1:
                mQpsBINRadionGroup.check(R.id.rb_qps_bin_a);
                break;
            case 2:
                mQpsBINRadionGroup.check(R.id.rb_qps_bin_b);
                break;
            case 3:
                mQpsBINRadionGroup.check(R.id.rb_qps_bin_c);
                break;
            default:
                //不支持，隐藏BIN表选择
                mQpsBINLayout.setVisibility(View.GONE);
                break;
        }
    }

    private void holdView() {
        etNoPwdAmount = (EditText) findViewById(R.id.et_confiure_amount_no_pwd);
        etNoSignAmount = (EditText) findViewById(R.id.et_confiure_amount_no_sign);

        cbCDCVMToggle = (CheckBox) findViewById(R.id.cb_qps_cdcvm);
        cbQPSToggle = (CheckBox) findViewById(R.id.cb_qps_toggle);
        cbNoSignToggle = (CheckBox) findViewById(R.id.cb_qps_no_sign);
        mCBWavePreferToggle = (CheckBox) findViewById(R.id.cb_wave_prefer_toggle);

        mQpsBINRadionGroup = (RadioGroup) findViewById(R.id.rg_qps_bin);
        mQpsBINLayout = (RelativeLayout) findViewById(R.id.rl_qps_bin);

    }

    public void onSureClick(View v) {
        String etContent = etNoPwdAmount.getText().toString().trim();
        String amount = formatMoney(etContent);
        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(this, "请输入正确的免密限额!", Toast.LENGTH_SHORT).show();
            return;
        }
        mQpsParam.setFF8058(amount);

        etContent = etNoSignAmount.getText().toString().trim();
        amount = formatMoney(etContent);
        if (TextUtils.isEmpty(amount)) {
            Toast.makeText(this, "请输入正确的免签限额!", Toast.LENGTH_SHORT).show();
            return;
        }
        mQpsParam.setFF8059(amount);

        mQpsParam.setFF8055("0");
        mQpsParam.setFF8056("0");
        int checkedId = mQpsBINRadionGroup.getCheckedRadioButtonId();
        if (R.id.rb_qps_bin_a == checkedId)
            mQpsParam.setFF8055("1");
        else if (R.id.rb_qps_bin_b == checkedId)
            mQpsParam.setFF8056("1");

        boolean checked = cbCDCVMToggle.isChecked();
        mQpsParam.setFF8057(checked ? "1" : "0");
        checked = cbNoSignToggle.isChecked();
        mQpsParam.setFF805A(checked ? "1" : "0");
        checked = cbQPSToggle.isChecked();
        mQpsParam.setFF8054(checked ? "1" : "0");
        checked = mCBWavePreferToggle.isChecked();
        BusinessConfig.getInstance().setFlag(this, BusinessConfig.Key.FLAG_PREFER_CLSS, checked);

        if (storeQpsParam()) {
            TradeModelImpl.getInstance().setQpsParams(mQpsParam);
            ViewUtils.showToast(context, "设置成功");
            activityStack.pop();
        } else
            ViewUtils.showToast(context, "设置失败");
    }

    private String formatMoney(String etContent) {
        if (TextUtils.isEmpty(etContent))
            return etContent;
        double moneyDouble = 0;
        try {
            moneyDouble = Double.parseDouble(etContent);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return String.format(Locale.CHINA, "%.2f", moneyDouble);
    }

    private boolean storeQpsParam() {
        CommonDao<Iso62Qps> dao = new CommonDao<>(Iso62Qps.class, DbHelper.getInstance());
        boolean dbResult = dao.deleteByWhere("id IS NOT NULL") && dao.save(mQpsParam);
        DbHelper.releaseInstance();
        return dbResult;
    }
}
