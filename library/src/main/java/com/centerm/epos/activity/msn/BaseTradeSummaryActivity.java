package com.centerm.epos.activity.msn;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.adapter.ObjectBaseAdapter;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.printer.BasePrintTransData;
import com.centerm.epos.printer.IPrintRransData;
import com.centerm.epos.task.AsyncQueryPrintDataTask;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.Keys;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.AUTH_SETTLEMENT;
import static com.centerm.epos.common.TransCode.COMPLETE_VOID;
import static com.centerm.epos.common.TransCode.EC_LOAD_CASH;
import static com.centerm.epos.common.TransCode.EC_LOAD_OUTER;
import static com.centerm.epos.common.TransCode.EC_VOID_CASH_LOAD;
import static com.centerm.epos.common.TransCode.E_COMMON;
import static com.centerm.epos.common.TransCode.E_QUICK;
import static com.centerm.epos.common.TransCode.E_REFUND;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_SALE;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.MAG_CASH_LOAD;
import static com.centerm.epos.common.TransCode.REFUND;
import static com.centerm.epos.common.TransCode.REFUND_SCAN;
import static com.centerm.epos.common.TransCode.RESERVATION_SALE;
import static com.centerm.epos.common.TransCode.RESERVATION_VOID;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.SALE_INSTALLMENT;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.common.TransCode.SCAN_PAY;
import static com.centerm.epos.common.TransCode.SCAN_VOID;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_REFUND;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_SALE;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_VOID;
import static com.centerm.epos.common.TransCode.VOID;
import static com.centerm.epos.common.TransCode.VOID_INSTALLMENT;
import static com.centerm.epos.common.TransCode.VOID_SCAN;


/**
 * 《基础版本》
 * 交易汇总界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */
public class BaseTradeSummaryActivity extends BaseActivity {

    private ListView listView1, listView2;
    private CommonDao<TradeInfoRecord> TradeInfoRecordDao;
    private View lableGroup;
    private List<TradeSummary> summaryList1, summaryList2;
    private TradeSummary saleSum, voidSum, authCompSum, compVoidSum, refundSum,
            scanSaleSum,scanVoidSum,scanRefundSum,
            ecSum,inDebit, inCredit, outDebit, outCredit;
    private List<TradeInfoRecord> jiejiList;
    private List<TradeInfoRecord> daijiList;
    private List<TradeInfoRecord> scanSaleList,scanVoidList,scanRefundList;
    private CommonDao<TradeInfoRecord> tradeDao;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Adapter adapter1 = new Adapter(context);
            adapter1.addAll(summaryList1);
            logger.debug(summaryList1);
            logger.debug(summaryList2);
            listView1.setAdapter(adapter1);
            Adapter adapter2 = new Adapter(context);
            adapter2.addAll(summaryList2);
            listView2.setAdapter(adapter2);
        }
    };
    private double jiejiAmount;
    private double daijiAmount;

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        //初始化需要汇总的交易类型
        TradeInfoRecordDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        summaryList1 = new ArrayList<>();
        summaryList2 = new ArrayList<>();
        summaryList1.add(saleSum = new TradeSummary("消费"));
        summaryList1.add(voidSum = new TradeSummary("消费撤销"));
        summaryList1.add(authCompSum = new TradeSummary("预授权完成"));
        summaryList1.add(compVoidSum = new TradeSummary("预授权完成撤销"));
        summaryList1.add(refundSum = new TradeSummary("退货"));
        summaryList1.add(ecSum = new TradeSummary("电子现金消费"));

        summaryList2.add(inDebit = new TradeSummary("内卡借记"));
        summaryList2.add(inCredit = new TradeSummary("内卡贷记"));
        summaryList2.add(outDebit = new TradeSummary("外卡借记"));
        summaryList2.add(outCredit = new TradeSummary("外卡贷记"));
        summaryList2.add(scanSaleSum = new TradeSummary("扫码消费"));
        summaryList2.add(scanVoidSum = new TradeSummary("扫码撤销"));
        summaryList2.add(scanRefundSum = new TradeSummary("扫码退货"));
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_summary;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_trade_summary);
        listView1 = (ListView) findViewById(R.id.list_v);
        listView2 = (ListView) findViewById(R.id.list_v2);
        lableGroup = findViewById(R.id.label_name_group);
//        ViewGroup.LayoutParams params = lableGroup.getLayoutParams();
//        params.height = getResources().getDimensionPixelSize(R.dimen.trade_record_title_height);
        ((TextView) lableGroup.findViewById(R.id.pos_serial_show)).setText(R.string.label_trans_type2);//交易类型
        ((TextView) lableGroup.findViewById(R.id.trans_type_show)).setText(R.string.label_trans_total_counts);//总笔数
        ((TextView) lableGroup.findViewById(R.id.trans_money_show)).setText(R.string.label_trans_total_amt);//总金额
    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        onSummary();
    }

    private void onSummary() {
        new Thread(new SummaryThread()).start();
    }

    private class SummaryThread implements Runnable {

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            QueryBuilder<TradeInfoRecord, String> qb = TradeInfoRecordDao.queryBuilder();
            try {
                List<TradeInfoRecord> tradeList = qb.query();
//                logger.debug("查询数据结果==>" + tradeList.toString());
                for (int i = 0; i < tradeList.size(); i++) {
                    TradeInfoRecord data = tradeList.get(i);
                    if(checkPayFail(data.getCardNo())){
                        continue;
                    }
                    String transCode = data.getTransType();
                    String f4 = data.getAmount();//金额
                    String f49 = data.getCurrencyCode();//货币类型
                    double amt = 0;
                    try {
                        amt = Double.valueOf(DataHelper.formatIsoF4(f4));
                    } catch (Exception e) {
                        logger.warn("格式化4域数据异常==>" + f4 + "==>交易类型：" + data.getTransType());
                    }
                    switch (transCode) {
                        case SALE:
                        case SCAN_PAY:
                        case SALE_INSTALLMENT:
                        case ISS_INTEGRAL_SALE:
                        case UNION_INTEGRAL_SALE:
                        case RESERVATION_SALE:
                            saleSum.addUpTotoalNum();
                            saleSum.addUpTotalAmt(amt);
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inDebit.addUpTotoalNum();
                                inDebit.addUpTotalAmt(amt);
                            } else {
                                outDebit.addUpTotoalNum();
                                outDebit.addUpTotalAmt(amt);
                            }
                            break;
                        case SALE_SCAN:
                            saleSum.addUpTotoalNum();
                            saleSum.addUpTotalAmt(amt);
                            scanSaleSum.addUpTotoalNum();
                            scanSaleSum.addUpTotalAmt(amt);
                            break;
                        case "SALE_SCAN_VOID":
                        case "SALE_SCAN_VOID_QUERY":
                            voidSum.addUpTotoalNum();
                            voidSum.addUpTotalAmt(amt);
                            scanVoidSum.addUpTotoalNum();
                            scanVoidSum.addUpTotalAmt(amt);
                            break;
                        case "SALE_SCAN_REFUND":
                        case "SALE_SCAN_REFUND_QUERY":
                            refundSum.addUpTotoalNum();
                            refundSum.addUpTotalAmt(amt);
                            scanRefundSum.addUpTotoalNum();
                            scanRefundSum.addUpTotalAmt(amt);
                            break;
                        case VOID:
                        case VOID_SCAN:
                        case SCAN_VOID:
                        case VOID_INSTALLMENT:
                        case ISS_INTEGRAL_VOID:
                        case UNION_INTEGRAL_VOID:
                        case RESERVATION_VOID:
                            voidSum.addUpTotoalNum();
                            voidSum.addUpTotalAmt(amt);
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inCredit.addUpTotoalNum();
                                inCredit.addUpTotalAmt(amt);
                            } else {
                                outCredit.addUpTotoalNum();
                                outCredit.addUpTotalAmt(amt);
                            }
                            break;
                        case AUTH_SETTLEMENT:
                        case AUTH_COMPLETE:
                            authCompSum.addUpTotoalNum();
                            authCompSum.addUpTotalAmt(amt);
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inDebit.addUpTotoalNum();
                                inDebit.addUpTotalAmt(amt);
                            } else {
                                outDebit.addUpTotoalNum();
                                outDebit.addUpTotalAmt(amt);
                            }
                            break;
                        case COMPLETE_VOID:
                            compVoidSum.addUpTotoalNum();
                            compVoidSum.addUpTotalAmt(amt);
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inCredit.addUpTotoalNum();
                                inCredit.addUpTotalAmt(amt);
                            } else {
                                outCredit.addUpTotoalNum();
                                outCredit.addUpTotalAmt(amt);
                            }
                            break;
                        case REFUND:
                        case REFUND_SCAN:
                        case E_REFUND:
                        case UNION_INTEGRAL_REFUND:
                            refundSum.addUpTotoalNum();
                            refundSum.addUpTotalAmt(amt);
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inCredit.addUpTotoalNum();
                                inCredit.addUpTotalAmt(amt);
                            } else {
                                outCredit.addUpTotoalNum();
                                outCredit.addUpTotalAmt(amt);
                            }
                            break;
                        case E_COMMON: //电子现金默认都是内卡
                        case E_QUICK: //电子现金默认都是内卡
                            if( data.getTransStatus() != 0x0800 ) {
                                /*脱机拒绝不参与统计*/
                                ecSum.addUpTotoalNum();
                                ecSum.addUpTotalAmt(amt);
                                if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                    inDebit.addUpTotoalNum();
                                    inDebit.addUpTotalAmt(amt);
                                } else {
                                    outDebit.addUpTotoalNum();
                                    outDebit.addUpTotalAmt(amt);
                                }
                            }
                            break;
                        case EC_LOAD_CASH:
                        case MAG_CASH_LOAD:
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inCredit.addUpTotoalNum();
                                inCredit.addUpTotalAmt(amt);
                            } else {
                                outCredit.addUpTotoalNum();
                                outCredit.addUpTotalAmt(amt);
                            }
                            break;
                        case EC_LOAD_OUTER:
                            if ("156".equals(f49)||TextUtils.isEmpty(f49)) {
                                inDebit.addUpTotoalNum();
                                inDebit.addUpTotalAmt(amt);
                            } else {
                                outDebit.addUpTotoalNum();
                                outDebit.addUpTotalAmt(amt);
                            }
                            break;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            logger.info("交易汇总数据统计完成==>耗时==>" + (end - start) / 1000.0);
            handler.obtainMessage().sendToTarget();
        }
    }


    private class Adapter extends ObjectBaseAdapter<TradeSummary> {

        public Adapter(Context mCtx) {
            super(mCtx);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TradeSummary data = getItem(position);
            int layoutId = R.layout.v_trade_record_item2;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(layoutId, null);
            }
            TextView posSerial = (TextView) convertView.findViewById(R.id.pos_serial_show);
            TextView transType = (TextView) convertView.findViewById(R.id.trans_type_show);
            TextView transAmt = (TextView) convertView.findViewById(R.id.trans_money_show);
            posSerial.setTextColor(getResources().getColor(R.color.font_black));
            transType.setTextColor(getResources().getColor(R.color.font_black));
            transAmt.setTextColor(getResources().getColor(R.color.font_black));
            posSerial.setText(data.getTypeName());//交易类型
            transType.setText("" + data.getTotalNum());//总笔数
            transAmt.setText("" + DataHelper.saved2Decimal(data.getTotalAmt()));//总金额
            return convertView;
        }
    }

    public class TradeSummary {
        private String typeName;
        private int totalNum;
        private double totalAmt;

        public TradeSummary(String typeName) {
            this.typeName = typeName;
        }

        public TradeSummary(String typeName, int totalNum, double totalAmt) {
            this.typeName = typeName;
            this.totalNum = totalNum;
            this.totalAmt = totalAmt;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public int getTotalNum() {
            return totalNum;
        }

        public void setTotalNum(int totalNum) {
            this.totalNum = totalNum;
        }

        public double getTotalAmt() {
            return totalAmt;
        }

        public void setTotalAmt(double totalAmt) {
            this.totalAmt = totalAmt;
        }

        public int addUpTotoalNum() {
            return ++totalNum;
        }

        public double addUpTotalAmt(double amt) {
            return totalAmt += amt;
        }

        @Override
        public String toString() {
            return "TradeSummary{" +
                    "typeName='" + typeName + '\'' +
                    ", totalNum=" + totalNum +
                    ", totalAmt=" + totalAmt +
                    '}';
        }
    }


    public void onPrintBtnClick(View view) {
        printTotalData();
    }

    private void printTotalData() {
        tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        jiejiAmount = 0.0;
        daijiAmount = 0.0;
        new AsyncQueryPrintDataTask(context) {
            @Override
            public void onStart() {
                super.onStart();
                DialogFactory.showLoadingDialog(BaseTradeSummaryActivity.this, "正在查询终端数据……");
            }

            @Override
            public void onFinish(List<List<TradeInfoRecord>> lists) {
                super.onFinish(lists);
                DialogFactory.hideAll();
                jiejiList = lists.get(0);
                daijiList = lists.get(1);
                scanSaleList = lists.get(6);
                scanVoidList = lists.get(7);
                scanRefundList = lists.get(8);
                BasePrintTransData printTransData = (BasePrintTransData) getPrint();
                if (null != printTransData) {
                    printTransData.open(context);
                    printTransData.printDataALLTrans(jiejiList, daijiList,scanSaleList,scanVoidList,scanRefundList);
                } else {
                    logger.error("printTransData获取为空");
                    ViewUtils.showToast(BaseTradeSummaryActivity.this, "获取汇总凭条打印处理方法失败！");
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    //扫码交易卡号为00表示交易成功
    public boolean checkPayFail(String cardNo){
        boolean result = true;
        if(TextUtils.isEmpty(cardNo)){

        }else if(cardNo.length()>2){
            result = false;
        }else if("S".equals(cardNo)){
            result = false;
        }
        return result;
    }

    /*获取打印类的接口*/
    public IPrintRransData getPrint() {
        ConfigureManager config = getConfigureManager();
        RedevelopItem redevelop = config.getRedevelopItem(context, Keys.obj().redevelop_print_data);
        String clzName = redevelop.getClassName();
        try {
            Class clz = Class.forName(clzName);
            Object obj = clz.newInstance();
            if (obj instanceof IPrintRransData) {
                return (IPrintRransData) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
