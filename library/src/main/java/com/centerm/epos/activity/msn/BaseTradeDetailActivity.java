package com.centerm.epos.activity.msn;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.keys.Keys;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;

/**
 * 《基础版本》
 * 交易详情界面
 * author:wanliang527</br>
 * date:2016/11/13</br>
 */
public class BaseTradeDetailActivity extends BaseActivity implements PrintManager.StatusInterpolator {

    private LinearLayout itemContainer;
    private TradeInfoRecord tradeInfo;

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
        logger.error(tradeInfo);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_trade_detail;
    }

    @Override
    public void onInitView() {
        setTitle(R.string.title_trade_detail);
        itemContainer = (LinearLayout) findViewById(R.id.trade_info_block);
        if (tradeInfo.getStateFlag() == 1) {
            addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransType
                    ())) + "（已撤销）", true);
        } else {
            addItemView(getString(R.string.label_trans_type2), getString(TransCode.codeMapName(tradeInfo.getTransType
                    ())), true);
        }

        if (TransCode.SALE_SCAN.equals(tradeInfo.getTransType()) || TransCode.VOID_SCAN.equals(tradeInfo
                .getTransType()) || TransCode.REFUND_SCAN.equals(tradeInfo.getTransType())) {
            addItemView(getString(R.string.label_scan_voucher), DataHelper.shieldCardNo(tradeInfo.getScanVoucherNo())
                    , true);
        } else if (!TransCode.AUTH.equals(tradeInfo.getTransType())) {
            addItemView(getString(R.string.label_card_no), DataHelper.shieldCardNo(tradeInfo.getCardNo()), true);
        } else {
            addItemView(getString(R.string.label_card_no), tradeInfo.getCardNo(), true);
        }
        addItemView(getString(R.string.label_trans_amt2), DataHelper.formatAmountForShow(tradeInfo.getAmount())+"元", true);
        addItemView(getString(R.string.label_serial_num_num), tradeInfo.getVoucherNo(), true);
        //addItemView(getString(R.string.label_auth_num), tradeInfo.getAuthorizeNo(), true);
        addItemView(getString(R.string.label_sys_ref_no), tradeInfo.getReferenceNo(), true);
        addItemView("外部订单号", tradeInfo.getIntoAccount(), true);
        addItemView(getString(R.string.label_trans_time), DataHelper.formatIsoF12F13(tradeInfo.getTransTime(), tradeInfo.getTransDate()), false);

        EventBus.getDefault().register(this);// add zhouzhihua 2017.11.06
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

    public void onReprintSlip(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }
        //printData();
        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_ANY));

    }

//    public void onUploadSlip(View view){
//        if (CommonUtils.isFastClick()) {
//            logger.debug("==>快速点击事件，不响应！");
//            return;
//        }
//        uploadESign();
//    }
//
//    private void uploadESign() {
//        String path = Config.Path.SIGN_PATH + File.separator + tradeInfo.getBatchNo() + "_"
//                + tradeInfo.getVoucherNo() + ".png";
//        if (FileUtils.getFileSize(path) > 0){
//            String transCode = TransCode.UPLOAD_ESIGN;
//            TradeProcess tradeProcess = ConfigureManager.getInstance(EposApplication.getAppContext()).getTradeProcess
//                    (this, "online.xml");
//            Intent intent = new Intent(this, TradeFragmentContainer.class);
//            intent.putExtra(BaseActivity.KEY_TRANSCODE, transCode);
//            intent.putExtra(BaseActivity.KEY_PROCESS, tradeProcess);
//            intent.putExtra("batch", tradeInfo.getBatchNo());
//            intent.putExtra("index", tradeInfo.getVoucherNo());
//            startActivityForResult(intent, REQ_TRANSACTION);
//        }else
//            Toast.makeText(context, "签名图片不存在或已经上送", Toast.LENGTH_SHORT).show();
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void printData() {
        if (null != tradeInfo) {
            PrintManager printManager = new PrintManager(this);
            IPrintSlipHelper printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new
                    BasePrintSlipHelper());
            PrinterProxy printerProxy;
            Map<String, String> slipItemContent;
            String tranCode = tradeInfo.getTransType();
            if (tranCode.equals(TransCode.SALE_SCAN) || tranCode.equals(TransCode.VOID_SCAN) || tranCode.equals
                    (TransCode.REFUND_SCAN) || tranCode.equals(TransCode.SCAN_PAY) || tranCode.equals(TransCode
                    .SCAN_VOID))
                printerProxy = printManager.prepare(PrintManager.SlipOwner.MERCHANT, "saleScanSlip");
            else
                printerProxy = actionWithChildProj(tranCode, printManager, PrintManager.SlipOwner.MERCHANT);
            slipItemContent = printSlipHelper.trade2PrintData(tradeInfo.convert2Map());
            printerProxy.setValue(slipItemContent)
                    .setICTrade(PRINT_IC_INFO.contains(tranCode))
                    .setReprint(true)
                    .addInterpolator(this)
                    .print();
        } else {
            ViewUtils.showToast(context, "无订单数据");
        }
    }

    /**
     * 检测子项目是否有实现
     */
    private PrinterProxy actionWithChildProj(String tranCode, PrintManager printManager, PrintManager.SlipOwner owner) {
        IRedevelopAction iPrintSlipDefine = ConfigureManager.getRedevelopAction(Keys.obj().redevelop_print_slip);
        if (iPrintSlipDefine != null) {
            return (PrinterProxy) iPrintSlipDefine.doAction(tranCode, printManager, owner);
        } else {
            return printManager.prepare(owner);
        }
    }

    /*
    *BUGID:0002278 ,解决重打印只打印一张签购单的问题
    * */
    private void printData(PrintManager.SlipOwner owner) {
        if (null != tradeInfo) {
            PrintManager printManager = new PrintManager(this);
            IPrintSlipHelper printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new
                    BasePrintSlipHelper());
            PrinterProxy printerProxy;
            Map<String, String> slipItemContent;
            String tranCode = tradeInfo.getTransType();

            DialogFactory.showLoadingDialog(this, this.getString(R.string.tip_printing));

            if (tranCode.equals(TransCode.SALE_SCAN) || tranCode.equals(TransCode.VOID_SCAN) || tranCode.equals
                    (TransCode.REFUND_SCAN) || tranCode.equals(TransCode.SCAN_PAY) || tranCode.equals(TransCode
                    .SCAN_VOID))
                printerProxy = printManager.prepare(owner, "saleScanSlip");
            else
                printerProxy = actionWithChildProj(tranCode, printManager, owner);
            slipItemContent = printSlipHelper.trade2PrintData(tradeInfo.convert2Map());
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

    @Override
    public void onPrinting() {

    }

    @Override
    public void onFinish() {
        PrinteEvent event = new PrinteEvent();
        if (isPrintComplete) {
            event.setWhat(TradeMessage.PRINT_SLIP_COMPLETE);
        } else {
            event.setWhat(TradeMessage.PRINT_NEXT_CONFIRM);
        }
        EventBus.getDefault().post(event);
    }

    @Override
    public void onError(int errorCode, String errorInfo) {
        if (errorCode == 513) {
            DialogFactory.showSelectDialog(BaseTradeDetailActivity.this, getString(R.string.error_title_tip),
                    getString(R.string.no_paper_tips),

                    new AlertDialog.ButtonClickListener() {

                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            if (AlertDialog.ButtonType.POSITIVE == button)
                                //printData();
                                EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_ANY));
                        }
                    });
        } else {
            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_ERROR));
            ViewUtils.showToast(context, "打印错误：" + errorInfo);
        }

    }

    private static class SaleSlipOwner {
        static final int SALE_SLIP_OWNER_MERCHANTER = 0x01;
        static final int SALE_SLIP_OWNER_BANK = 0x02;
        static final int SALE_SLIP_OWNER_CARDHOLDER = 0x04;


        static int getSaleSlipOwner(int iPrintNum) {
            if (iPrintNum == 1) {
                return SALE_SLIP_OWNER_MERCHANTER;
            } else if (iPrintNum == 2) {
                return SALE_SLIP_OWNER_MERCHANTER | SALE_SLIP_OWNER_CARDHOLDER;
            } else {
                return SALE_SLIP_OWNER_MERCHANTER | SALE_SLIP_OWNER_BANK | SALE_SLIP_OWNER_CARDHOLDER;
            }
        }

    }

    private int getPrintNum() {
        return BusinessConfig.getInstance().getNumber(this, Keys.obj().printnum);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        switch (event.getWhat()) {
            case TradeMessage.PRINT_SLIP_ANY:
                if ((printNum == 0) && SaleSlipOwner.SALE_SLIP_OWNER_MERCHANTER == (SaleSlipOwner.getSaleSlipOwner
                        (getPrintNum()) & SaleSlipOwner.SALE_SLIP_OWNER_MERCHANTER)) {
                    printData(PrintManager.SlipOwner.MERCHANT);
                } else if ((printNum == 1) && SaleSlipOwner.SALE_SLIP_OWNER_BANK == (SaleSlipOwner.getSaleSlipOwner
                        (getPrintNum()) & SaleSlipOwner.SALE_SLIP_OWNER_BANK)) {
                    printData(PrintManager.SlipOwner.MERCHANT);//因没有 银行联 暂时使用商户联
                } else {
                    printData(PrintManager.SlipOwner.CONSUMER);
                }

                break;
            case TradeMessage.PRINT_NEXT_CONFIRM:
                printNum++;
                isPrintComplete = ((printNum) >= getPrintNum());
                if (isPrintComplete) {
                    EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                    return;
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
                printNum = 0;
                isPrintComplete = false;
                DialogFactory.hideAll();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);// add zhouzhihua 2017.11.06
        super.onDestroy();
        printNum = 0;
        isPrintComplete = false;
    }
}
