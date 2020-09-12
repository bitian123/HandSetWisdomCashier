package com.centerm.epos.ebi.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.present.ContractQueryPresent;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.view.AlertDialog;

/**
 * 合同信息查询
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class ContractInfoQueryFragment extends BaseTradeFragment implements View.OnClickListener {
    private ImageView mIvTip;

    private ContractQueryPresent present;

    private EditText et_contract;
    private String contractInfo ;


    @Override
    protected ITradePresent newTradePresent() {
        present = new ContractQueryPresent(this);
        return present;
    }

    @Override
    protected void onInitView(View view) {
        initFinishBtnlistener(view);
        view.findViewById(R.id.imgbtn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTradePresent.gotoNextStep("2");
            }
        });

        view.findViewById(R.id.btn_sure).setOnClickListener(this);
        view.findViewById(R.id.rl_scan_contract).setOnClickListener(this);
        et_contract = view.findViewById(R.id.et_contract_no);

        //非身份证验证进入
        if (mTradePresent.getTransData().get("isOther") != null) {
            mIvTip = (ImageView) view.findViewById(R.id.mIvTip);
            mIvTip.setBackground(getActivity().getResources().getDrawable(R.drawable.auth_step_6));
        }

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
            et_contract.setText(content);
            present.onGetScanCode(content);
//            if (content.matches("[0-9]+")) {
//
//            } else {
//                Toast.makeText(getContext(), "只支持数字！", Toast.LENGTH_SHORT).show();
//                mTradePresent.gotoPreStep();
//            }
        }
    }


    @Override
    public void onClick(View v) {
        if (CommonUtils.isFastClick()) {
            return;
        }
        switch (v.getId()){
            case R.id.rl_scan_contract:
                present.onStartScanCode();
                break;
            case R.id.btn_sure:
                queryContractInfo();
                break;
            default:
        }

    }

    public void  queryContractInfo(){
        contractInfo = et_contract.getText().toString().trim();
        if(!TextUtils.isEmpty(contractInfo)){
            present.getTransData().put(JsonKeyGT.contractNo,contractInfo);
            present.queryContractInfo();
        }else{
            popToast("请输入合同号");
        }

    }




    @Override
    public boolean onBacKeyPressed() {
        logger.info("实体键返回");
        tipToExit();
        return true;
    }

    private void tipToExit() {
        DialogFactory.showSelectDialog(getActivity(), getString(com.centerm.epos.R.string.tip_notification), "确认退出业务？", new AlertDialog
                .ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                switch (button) {
                    case POSITIVE:
                        getHostActivity().finish();
                        break;
                    default:
                }
            }
        });
    }


    @Override
    public int onLayoutId() {
        return R.layout.fragment_contract_query;
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("请输入合同编码");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
