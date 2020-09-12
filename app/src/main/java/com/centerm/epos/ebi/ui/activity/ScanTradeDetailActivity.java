package com.centerm.epos.ebi.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.base.TradeFragmentContainer;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.bean.SaleScanResult;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.model.BaseTradeParameter;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.process.TradeProcess;
import com.centerm.epos.xml.keys.Keys;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.stmt.Where;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID_QUERY;
import static java.lang.Thread.sleep;

/**
 * 《基础版本》
 * 交易详情界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */
public class ScanTradeDetailActivity extends BaseActivity implements PrintManager.StatusInterpolator {

    private LinearLayout itemContainer;
    private Button printBtn,searchBtn,mBtnSendNotice;
    private TradeInfoRecord tradeInfo;
    private CommonDao<TradeInfoRecord> tradeDao;
    private boolean isPrintComplete = false;
    private int printNum = 0;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        tradeInfo = (TradeInfoRecord) getIntent().getSerializableExtra(KEY_TRADE_INFO);
        if (tradeInfo == null) {
            logger.warn("交易信息为空==>请传递交易信息到此界面");
            tradeInfo = new TradeInfoRecord();
        }
        logger.debug("tradeInfo=>"+tradeInfo.toString());
        dbHelper = OpenHelperManager.getHelper(this, DbHelper.class);
        tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        try {
            checkScanVoidState();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        tradeInfo = tradeDao.queryForId(tradeInfo.getVoucherNo());
        itemContainer.removeAllViews();
        initData();
    }

    /**
     * 检查扫码撤销状态，防止扫码撤销通讯时断电，重启后再做扫码撤销，出现多笔扫码撤销交易记录导致单边帐问题
     * */
    public void checkScanVoidState() throws SQLException {
        if(!TextUtils.isEmpty(tradeInfo.getTransType())&&tradeInfo.getTransType().contains(SALE_SCAN_VOID)){
            Where<TradeInfoRecord, String> where = tradeDao.queryBuilder().where();
            where.eq("scanVoucherNo", tradeInfo.getScanVoucherNo());
            List<TradeInfoRecord> tradeInfos = where.query();
            boolean stateSuccess = false;
            for(TradeInfoRecord info : tradeInfos){
                if(!info.getVoucherNo().equals(tradeInfo.getVoucherNo())&&info.getTransType().contains(SALE_SCAN_VOID)&&"S".equals(info.getCardNo())){
                    stateSuccess = true;
                }
            }
            if(stateSuccess){
                tradeInfo.setCardNo("F");
                tradeDao.update(tradeInfo);
            }
        }
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_scan_trade_detail;
    }

    @Override
    public void onInitView() {
        setTitle(com.centerm.epos.R.string.title_trade_detail);
        itemContainer = (LinearLayout) findViewById(com.centerm.epos.R.id.trade_info_block);
        printBtn = (Button) findViewById(R.id.print_btn);
        searchBtn = (Button) findViewById(R.id.search_btn);
        mBtnSendNotice = (Button) findViewById(R.id.mBtnSendNotice);

        EventBus.getDefault().register(this);// add zhouzhihua 2017.11.06
    }

    private void initData(){
        if (tradeInfo.getStateFlag() == 1) {
            addItemView(getString(com.centerm.epos.R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransType())) + "（已撤销）", true);
        } else {
            addItemView(getString(com.centerm.epos.R.string.label_trans_type2), GetRequestData.getTransName(tradeInfo.getTransType(), tradeInfo.getUnicom_scna_type()), true);
        }
        String payStatus = tradeInfo.getCardNo();

        if("S".equals(tradeInfo.getCardNo())){
            printBtn.setVisibility(View.VISIBLE);
            searchBtn.setVisibility(View.GONE);
            if(!TextUtils.isEmpty(tradeInfo.getIntoAccount())){
                mBtnSendNotice.setVisibility(View.VISIBLE);
            }
        }else if("F".equals(tradeInfo.getCardNo())//交易失败
                ||"N".equals(tradeInfo.getCardNo())//订单不存在
                ||"P3".equals(tradeInfo.getCardNo())//订单已关闭
                ||"P5".equals(tradeInfo.getCardNo())){//订单不存在
            printBtn.setVisibility(View.GONE);
            searchBtn.setVisibility(View.GONE);
        }else {
            printBtn.setVisibility(View.GONE);
            searchBtn.setVisibility(View.VISIBLE);
        }

        addItemView("交易状态", GetRequestData.getPayStatus(tradeInfo.getTransType(), payStatus), true);
        addItemView(getString(com.centerm.epos.R.string.label_trans_amt2), DataHelper.formatAmountForShow(tradeInfo.getAmount())+"元", true);
        addItemView(getString(com.centerm.epos.R.string.label_serial_num_num), tradeInfo.getVoucherNo(), true);
        addItemView(getString(R.string.label_orderid), tradeInfo.getScanVoucherNo(), true);

        if(!TextUtils.isEmpty(tradeInfo.getOriAuthCode())){
            addItemView(getString(R.string.label_serial_num_detail_ori_orderid), tradeInfo.getOriAuthCode(), true);
        }
        if(!TextUtils.isEmpty(tradeInfo.getIntoAccount())){
            addItemView("外部订单号", tradeInfo.getIntoAccount(), true);
        }
        String transTime = tradeInfo.getTransTime();
        if(!TextUtils.isEmpty(transTime)&&transTime.length()>8){
            transTime = transTime.substring(8);
        }
        addItemView(getString(com.centerm.epos.R.string.label_trans_time), DataHelper.formatIsoF12F13(transTime, tradeInfo.getTransDate()), false);
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate(com.centerm.epos.R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(com.centerm.epos.R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(com.centerm.epos.R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(com.centerm.epos.R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(com.centerm.epos.R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    public void onReprintSlip(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        if (!CommonUtils.tradeEnvironmentCheck(EposApplication.getAppContext())) {
            logger.debug("^_^ 电池电量低 ^_^");
            ViewUtils.showToast(EposApplication.getAppContext(), "电量低，请连接电源后重试！");
            return;
        }
        //printData();
        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_ANY));
    }

    public void onSearchClick(View view){
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        //联机查询
        sendGetRequest();
    }

    public void onSendNotice(View view){
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        beginOnlineProcess(PROPERTY_NOTICE);
    }

    private void sendGetRequest(){
        beginOnlineProcess(null);
    }

    private void beginOnlineProcess(String transC){
        String transCode = tradeInfo.getTransType();
        transCode = transCode + "_QUERY";
        ConfigureManager config = ConfigureManager.getInstance(this);
        TradeProcess process = config.getTradeProcess(this, "query_online");
        if (process == null) {
            logger.warn("通用联机流程未定义！");
            return;
        }

        process.getTransDatas().put(JsonKey.pay_type, tradeInfo.getUnicom_scna_type());
        process.getTransDatas().put(JsonKey.isOrderQueryAct, true);
        process.getTransDatas().put(TradeInformationTag.TRACE_NUMBER, tradeInfo.getVoucherNo());
        process.getTransDatas().put(TradeInformationTag.TRANS_MONEY, tradeInfo.getAmount());
        if(SALE_SCAN_QUERY.equals(transCode)){
            process.getTransDatas().put(JsonKey.mer_order_no, tradeInfo.getScanVoucherNo());
            process.getTransDatas().put(JsonKey.pay_no, tradeInfo.getReferenceNo());
            process.getTransDatas().put(JsonKey.QUERY_FLAG, JsonKey.QUERY_FLAG);
            process.getTransDatas().put(JsonKey.isTradeDetail, JsonKey.isTradeDetail);
            process.getTransDatas().put(JsonKey.PROPERTY_FLAG, JsonKey.PROPERTY_FLAG);
            if(tradeInfo.getIntoAccount()!=null){
                process.getTransDatas().put(JsonKey.out_order_no, tradeInfo.getIntoAccount());
            }
        }else if(SALE_SCAN_VOID_QUERY.equals(transCode)){
            process.getTransDatas().put(JsonKey.mer_order_no, tradeInfo.getScanVoucherNo());
            process.getTransDatas().put(TradeInformationTag.SCAN_VOUCHER_NO, tradeInfo.getScanVoucherNo());
            process.getTransDatas().put(JsonKey.QUERY_FLAG, JsonKey.QUERY_FLAG);
        }else if(SALE_SCAN_REFUND_QUERY.equals(transCode)){
            process.getTransDatas().put(JsonKey.mer_order_no, tradeInfo.getOriAuthCode());
            process.getTransDatas().put(JsonKey.mer_refund_order_no, tradeInfo.getScanVoucherNo());
            process.getTransDatas().put(JsonKey.QUERY_FLAG, JsonKey.QUERY_FLAG);
        }
        if(TextUtils.equals(PROPERTY_NOTICE, transC)){
            process.getTransDatas().put(JsonKey.out_order_no, tradeInfo.getIntoAccount());
            process.getTransDatas().put(JsonKey.isTradeDetail, JsonKey.isTradeDetail);
            transCode = transC;
        }

        //启动交易流程
        Intent intent = new Intent(this, TradeFragmentContainer.class);
        intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
        intent.putExtra(BaseActivity.KEY_PROCESS, process);
        ITradeParameter parameter = (ITradeParameter) ConfigureManager.getSubPrjClassInstance(new BaseTradeParameter());
        if (parameter.getParam(transCode) != null)
            intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, parameter.getParam(transCode));
        startActivityForResult(intent, REQ_TRANSACTION);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void printData() {
        if (null != tradeInfo) {
            PrintManager printManager = new PrintManager(this);
            IPrintSlipHelper printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new BasePrintSlipHelper());
            PrinterProxy printerProxy;
            Map<String, String> slipItemContent;
            String tranCode = tradeInfo.getTransType();
            if(tranCode.contains("SCAN"))
                printerProxy = printManager.prepare(PrintManager.SlipOwner.MERCHANT, "saleScanSlip");
            else
                printerProxy = printManager.prepare(PrintManager.SlipOwner.MERCHANT);
            Map<String, String> map = tradeInfo.convert2Map();
            map.put(JsonKey.pay_type, tradeInfo.getUnicom_scna_type());
            slipItemContent = printSlipHelper.trade2PrintData(map);
            printerProxy.setTransCode(tranCode)
                    .setValue(slipItemContent)
                    .setICTrade(PRINT_IC_INFO.contains(tranCode))
                    .setReprint(true)
                    .addInterpolator(this)
                    .print();
        } else {
            ViewUtils.showToast(context, "无订单数据");
        }
    }

    @Override
    public void onPrinting() {

    }

    @Override
    public void onFinish() {
        PrinteEvent event = new PrinteEvent();
        if (isPrintComplete) {
            event.setWhat(TradeMessage.PRINT_SLIP_COMPLETE);
        }
        else {
            event.setWhat(TradeMessage.PRINT_NEXT_CONFIRM);
        }
        EventBus.getDefault().post(event);
    }

    @Override
    public void onError(int errorCode, String errorInfo) {
        if (errorCode == 513) {
            DialogFactory.showSelectDialog(ScanTradeDetailActivity.this, getString(com.centerm.epos.R.string.error_title_tip),
                    getString(com.centerm.epos.R.string.no_paper_tips),

                    new AlertDialog.ButtonClickListener() {

                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            if (AlertDialog.ButtonType.POSITIVE == button)
                                printData();
                        }
                    });
        } else
            ViewUtils.showToast(context, "打印错误：" + errorInfo);

    }

    /*
    *BUGID:0002278 ,解决重打印只打印一张签购单的问题
    * */
    private void printData(PrintManager.SlipOwner owner) {
        if (null != tradeInfo) {
            PrintManager printManager = new PrintManager(this);
            IPrintSlipHelper printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new BasePrintSlipHelper());
            PrinterProxy printerProxy;
            Map<String, String> slipItemContent;
            String tranCode = tradeInfo.getTransType();

            DialogFactory.showLoadingDialog(this, this.getString(com.centerm.epos.R.string.tip_printing));

            if (tranCode.contains("SCAN"))
                printerProxy = printManager.prepare(owner, "saleScanSlip");
            else
                printerProxy = printManager.prepare(owner);
            Map<String, String> tradeData = tradeInfo.convert2Map();
            if(!TextUtils.isEmpty(tradeInfo.getUnicom_scna_type())){
                tradeData.put(JsonKey.pay_type, tradeInfo.getUnicom_scna_type());
            }
            slipItemContent = printSlipHelper.trade2PrintData(tradeData);
            printerProxy.setTransCode(tranCode)
                    .setValue(slipItemContent)
                    .setICTrade(PRINT_IC_INFO.contains(tranCode))
                    .setReprint(true)
                    .addInterpolator(this)
                    .print();

        } else {
            ViewUtils.showToast(context, "无订单数据");
            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        switch (event.getWhat()) {
            case TradeMessage.PRINT_SLIP_ANY:
                if( (printNum == 0) && SaleSlipOwner.SALE_SLIP_OWNER_MERCHANTER == (SaleSlipOwner.getSaleSlipOwner(getPrintNum()) & SaleSlipOwner.SALE_SLIP_OWNER_MERCHANTER) ) {
                    printData(PrintManager.SlipOwner.MERCHANT);
                }
                else if( (printNum == 1) && SaleSlipOwner.SALE_SLIP_OWNER_BANK == (SaleSlipOwner.getSaleSlipOwner(getPrintNum()) & SaleSlipOwner.SALE_SLIP_OWNER_BANK) ) {
                    printData(PrintManager.SlipOwner.MERCHANT);//因没有 银行联 暂时使用商户联
                }
                else{
                    printData(PrintManager.SlipOwner.CONSUMER);
                }

                break;
            case TradeMessage.PRINT_NEXT_CONFIRM:
                printNum++; isPrintComplete = ((printNum) >= getPrintNum());
                if(isPrintComplete){
                    EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                    return ;
                }
                DialogFactory.showPrintDialog(this, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {

                        if (AlertDialog.ButtonType.NEGATIVE == button) {
                            isPrintComplete = true;
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                        } else {
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_ANY));
                        }
                    }
                });
                break;
            case TradeMessage.PRINT_ERROR:
            case TradeMessage.PRINT_SLIP_COMPLETE:
                printNum = 0;   isPrintComplete = false;
                DialogFactory.hideAll();
                break;
        }
    }

    private static class SaleSlipOwner{
        static final int SALE_SLIP_OWNER_MERCHANTER = 0x01;
        static final int SALE_SLIP_OWNER_BANK = 0x02;
        static final int SALE_SLIP_OWNER_CARDHOLDER = 0x04;


        static int getSaleSlipOwner(int iPrintNum)
        {
            if( iPrintNum == 1){
                return SALE_SLIP_OWNER_MERCHANTER;
            }
            else if( iPrintNum == 2 ){
                return SALE_SLIP_OWNER_MERCHANTER|SALE_SLIP_OWNER_CARDHOLDER;
            }
            else{
                return SALE_SLIP_OWNER_MERCHANTER|SALE_SLIP_OWNER_BANK|SALE_SLIP_OWNER_CARDHOLDER;
            }
        }

    }
    private  int getPrintNum() {
        return BusinessConfig.getInstance().getNumber(this, Keys.obj().printnum);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);// add zhouzhihua 2017.11.06
        super.onDestroy();
        printNum = 0;
        isPrintComplete = false;
    }
}
