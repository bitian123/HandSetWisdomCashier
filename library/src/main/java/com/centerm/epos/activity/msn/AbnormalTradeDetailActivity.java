package com.centerm.epos.activity.msn;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.utils.NetUtils;
import com.centerm.epos.ActivityStack;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.ResultQueryActivity;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;
import static com.centerm.epos.common.TransCode.fingerRegister;

/**
 * 《基础版本》
 * 交易详情界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */
public class AbnormalTradeDetailActivity extends BaseActivity implements PrintManager.StatusInterpolator{
    private LinearLayout itemContainer;
    private ReverseInfo tradeInfo;
    private Button mBtnPrint;
    protected CommonDao<TradeInfoRecord> tradeDao;
    protected CommonDao<ReverseInfo> reverseDao;
    protected ITransactionMessage factory2;
    private Map<String, Object> mapData;
    private boolean isTradeSuccess = false;

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        tradeDao = new CommonDao<>(TradeInfoRecord.class,
                OpenHelperManager.getHelper(this.getApplicationContext(), DbHelper.class));
        reverseDao = new CommonDao<>(ReverseInfo.class,
                OpenHelperManager.getHelper(this.getApplicationContext(), DbHelper.class));
        tradeInfo = (ReverseInfo) getIntent().getSerializableExtra(KEY_TRADE_INFO);
        if (tradeInfo == null) {
            logger.warn("交易信息为空==>请传递交易信息到此界面");
            tradeInfo = new ReverseInfo();
        }
        logger.debug(tradeInfo);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_detail;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_trade_detail);
        mBtnPrint = (Button) findViewById(R.id.mBtnPrint);
        mBtnPrint.setText("查询");

        itemContainer = (LinearLayout) findViewById(R.id.trade_info_block);
        addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransCode())), true);
        addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(tradeInfo.getIso_f2()), true);
        addItemView(getString(R.string.label_trans_amt2), DataHelper.formatAmountForShow(tradeInfo.getIso_f4())+"元", true);
        addItemView(getString(R.string.label_serial_num_num), tradeInfo.getIso_f11(), true);
        addItemView("外部订单号", tradeInfo.getIso_f61(), true);
        addItemView(getString(R.string.label_trans_time), tradeInfo.getTransTime(), false);

    }

    public void onReprintSlip(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        if (!NetUtils.isNetConnected(this)) {
            ViewUtils.showToast(EposApplication.getAppContext(), "网络未连接！");
            return;
        }
        if(tradeInfo!=null){
            Map<String,Object> dataMap = new HashMap<>();
            try {
                String time = tradeInfo.getTransTime();
                dataMap.put(TradeInformationTag.TRANS_YEAR, time.substring(0,4));
            }catch (Exception e){}
            dataMap.put(TradeInformationTag.TRACE_NUMBER, tradeInfo.getIso_f11());
            send8583Data(true,TransCode.SALE_RESULT_QUERY,dataMap);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("===","onResume");
        if(isTradeSuccess){
            EventBus.getDefault().post(new PrinteEvent(TradeMessage.EXIT));
            finish();
        }
    }

    /**
     * 开始发送8583数据，单个请求。
     */
    public void send8583Data(boolean showDialog, final String transCode, final Map<String,Object> dataMap) {
        if(showDialog){
            DialogFactory.showLoadingDialog(this, "通讯中，请稍侯");
        }
        new Thread() {
            @Override
            public void run() {
                if(factory2==null){
                    factory2 = new PosISO8583Message(new HashMap<String, Object>());
                }
                final Object msgPacket = factory2.packMessage(transCode, dataMap);
                if (msgPacket == null) {
                    DialogFactory.hideAll();
                    logger.warn("请求报文为空，退出");
                    return;
                }

                if (msgPacket instanceof byte[]) {
                    try {
                        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
                        sleep(200);
                        byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
                        DialogFactory.hideAll();
                        if (receivedData == null) {
                            logger.error("^_^ 接收数据失败！receivedData = null ^_^");
                        } else {
                            mapData = factory2.unPackMessage(transCode, receivedData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(mapData==null){
                                        mapData = new HashMap<>();
                                    }
                                    mapData.putAll(tradeInfo.convert2Map());
                                    mapData.put(TradeInformationTag.TRACE_NUMBER,mapData.get(TransDataKey.iso_f11));
                                    mapData.put(TradeInformationTag.BATCH_NUMBER,BusinessConfig.getInstance().getBatchNo(EposApplication.getAppContext()));
                                    mapData.put(TradeInformationTag.UNICOM_SCAN_TYPE,mapData.get(TransDataKey.iso_f64));
                                    mapData.put(TradeInformationTag.TRANSFER_INTO_CARD,mapData.get(TransDataKey.iso_f61));
                                    mapData.put(TradeInformationTag.OPERATOR_CODE,"01");
                                    mapData.put(TradeInformationTag.TRANS_YEAR, String.format(Locale.CHINA, "%04d", Calendar.getInstance().get(Calendar.YEAR)));
                                    beginOnlineProcess(mapData);
                                    //print();
                                }
                            });
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                        DialogFactory.hideAll();
                    }
                } else {
                    logger.warn("报文格式非字节数组");
                    DialogFactory.hideAll();
                }
            }
        }.start();
    }

    private void beginOnlineProcess(Map<String, Object> mapData){
        if(!"00".equals(mapData.get(TradeInformationTag.RESPONSE_CODE))){
            ViewUtils.showToast(this, "交易失败");
            return;
        }
        isTradeSuccess = true;
        ViewUtils.showToast(this, "交易成功，请签名");
        Bundle bundle = new Bundle();
        for(Map.Entry<String, Object> entry : mapData.entrySet()){
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            bundle.putString(mapKey, (String) mapValue);
            logger.info(mapKey+":"+mapValue);
        }

        Intent intent = new Intent(this, ResultQueryActivity.class);
        intent.putExtra(ITradeParameter.KEY_TRANS_PARAM, bundle);
        startActivityForResult(intent,0);

    }

    private int printTime = 0;
    private void printData() {
        if (null != tradeInfo) {
            printTime++;
            PrintManager printManager = new PrintManager(this);
            IPrintSlipHelper printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new
                    BasePrintSlipHelper());
            PrinterProxy printerProxy;
            Map<String, String> slipItemContent;
            String tranCode = TransCode.SALE;

            DialogFactory.showLoadingDialog(this, this.getString(R.string.tip_printing));
            if(printTime==1){
                printerProxy = printManager.prepare(PrintManager.SlipOwner.MERCHANT);
            }else {
                printerProxy = printManager.prepare(PrintManager.SlipOwner.CONSUMER);
            }

            Map<String, String> content = new HashMap<>();
            for(Map.Entry<String, Object> entry : mapData.entrySet()){
                String mapKey = entry.getKey();
                Object mapValue = entry.getValue();
                content.put(mapKey, (String) mapValue);
            }

            content.put(TradeInformationTag.TRANSACTION_TYPE, tranCode);
            content.put(TradeInformationTag.TRACE_NUMBER, (String) mapData.get("iso_f11"));
            slipItemContent = printSlipHelper.trade2PrintData(content);
            printerProxy.setValue(slipItemContent)
                    .setICTrade(PRINT_IC_INFO.contains(tranCode))
                    .setReprint(true)
                    .addInterpolator(this)
                    .print();

        } else {
            ViewUtils.showToast(context, "无订单数据");
            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
        }
    }

    private void addItemView(String key, String value, boolean addDivider) {
        View view = getLayoutInflater().inflate(R.layout.result_info_item, null);
        TextView keyShow = (TextView) view.findViewById(R.id.item_name_show);
        TextView valueShow = (TextView) view.findViewById(R.id.item_value_show);
        keyShow.setText(key);
        valueShow.setText(value);
        itemContainer.addView(view, -1, -2);
        itemContainer.invalidate();
        if (addDivider) {
            float size = getResources().getDimension(R.dimen.common_divider_size);
            if (size < 1) {
                size = 1;
            }
            View divider = new View(context);
            divider.setBackgroundColor(getResources().getColor(Settings.bIsSettingBlueTheme() ? R.color.result_divider : R.color.common_divider));
            itemContainer.addView(divider, -1, (int) size);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onPrinting() {

    }

    @Override
    public void onFinish() {
        if(printTime==1){
            printData();
        }
        if(printTime>1){
            DialogFactory.hideAll();
            printTime = 0;
            mapData.clear();
        }
        ActivityStack.getInstance().pop();
        ActivityStack.getInstance().pop();
    }

    @Override
    public void onError(int errorCode, String errorInfo) {
        DialogFactory.hideAll();
        printTime = 0;
        mapData.clear();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==88){
            TradeInfoRecord record = new TradeInfoRecord(TransCode.SALE,mapData);
            tradeDao.save(record);
            reverseDao.delete(tradeInfo);
            printData();
        }
    }
}
