package com.centerm.epos.ebi.transaction;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.View;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.ContrantInfoBean;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.task.ContractInfoQueryTask;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.ContractInfoDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yuhc
 * @date 2017/4/1
 * 合同信息查询
 */

public class EbiContractQueryTrade implements ManageTransaction {
    @SuppressLint("StaticFieldLeak")
    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        new ContractInfoQueryTask(tradeView.getContext(), tradePresent.getTransData(), null) {

            @Override
            public void onStart() {
                tradeView.showTipDialog("正在查询合同信息");
            }

            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                DialogFactory.hideAll();
                tradePresent.putResponseCode(strings[0], strings[1]);
                XLogUtil.e("field63:", strings[0] + strings[1]);
                if ("00".equals(strings[0])) {
                    tradePresent.getTransData().put(TradeInformationTag.TRACE_NUMBER,strings[2]);
                    Map<String, String> map = new HashMap<>();
                    String field63 = strings[1];
                    String idNo = field63.substring(12, field63.indexOf("PS"));
                    String amount, name;
                    if (field63.contains("元")) {
                        amount = field63.substring(field63.indexOf("PS") + 2, field63.indexOf("元"));
                        name = field63.substring(field63.indexOf("元") + 1, field63.indexOf("#")).trim();
                    } else {
                        amount = field63.substring(field63.indexOf("PS") + 2, 47);
                        name = field63.substring(47, field63.indexOf("#")).trim();
                    }
                    String contractInfo = (String) tradePresent.getTransData().get(JsonKeyGT.contractNo);
                    double oldAmount = Double.parseDouble(amount);

                    XLogUtil.d("queryName", name);
                    XLogUtil.d("queryIdNo", idNo);
                    String[] names = name.split(",");
                    String[] idNos = idNo.split(",");
                    final List<String> listQuery = new ArrayList<>();
                    for (int i = 0; i < names.length; i++) {
                        listQuery.add(idNos[i].trim()+names[i].trim());
                    }
                    String tradeName = (String) tradePresent.getTransData().get(JsonKeyGT.checkCardShowName);
                    XLogUtil.d("tradeName", tradeName);
                    String tradeIdNo =(String) tradePresent.getTransData().get(JsonKeyGT.checkCardShowIdNo);
                    XLogUtil.d("tradeIdNo", tradeIdNo);

                    String[] nameTrades = tradeName.trim().split(" ");
                    String[] idNosTrades = tradeIdNo.trim().split(" ");
                    List<String> listTrades = new ArrayList<>();
                    for (int i = 0; i < nameTrades.length; i++) {
                        listTrades.add(idNosTrades[i]+nameTrades[i]);
                    }
                    boolean isSame=true;

                    if (listQuery == null){
                        isSame=false;
                    }
                    if(listTrades.size() !=listQuery.size() ){
                        isSame=false;
                    }else{
                        for (String custominfo:listQuery) {
                            if (!listTrades.contains(custominfo)){
                                isSame= false;
                            }
                        }
                    }

                    map.put("name", name);
                    map.put("idNo", idNo);
                    map.put("contractNo", contractInfo);
                    map.put("contractPrice", "" + oldAmount);
                    final boolean isGoNext = isSame;
                    tradeView.showContractInfoDialog(R.string.tip_confirm_info, map,new ContractInfoDialog.ButtonClickListener() {

                        @Override
                        public void onClick(ContractInfoDialog.ButtonType button, View v) {
                            switch (button) {
                                case POSITIVE:
                                    if (isGoNext) {
                                        tradePresent.gotoNextStep("1");
                                    }else{
                                        tradeView.showMessageDialog(R.string.tip_dialog_title, R.string.tip_contract_query_wrong, new AlertDialog.ButtonClickListener() {
                                            @Override
                                            public void onClick(AlertDialog.ButtonType button, View v) {
                                                DialogFactory.hideAll();
                                                tradePresent.gotoPreStep();
                                            }
                                        });
                                    }
                                    break;
                                case NEGATIVE:
                                    break;
                                default:
                            }
                        }
                    });

//                    if (listQuery.size() == 0) {
//
//                    } else {
//                        tradeView.showMessageDialog(R.string.tip_dialog_title, R.string.tip_contract_query_wrong, new AlertDialog.ButtonClickListener() {
//                            @Override
//                            public void onClick(AlertDialog.ButtonType button, View v) {
//                                DialogFactory.hideAll();
//                            }
//                        });
//                    }

                } else {
                    tradeView.showMessageDialog(R.string.tip_dialog_title, R.string.tip_contract_query_failed, new AlertDialog.ButtonClickListener() {
                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            DialogFactory.hideAll();
                        }
                    });


                }


            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());


    }


}
