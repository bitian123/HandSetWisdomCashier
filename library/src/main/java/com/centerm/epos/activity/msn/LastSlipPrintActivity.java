package com.centerm.epos.activity.msn;

import android.content.Context;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.keys.Keys;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;

/**
 * Created by yuhc on 2017/5/22.
 */

public class  LastSlipPrintActivity extends BaseActivity {

    private PrintManager printManager;
    private IPrintSlipHelper printSlipHelper;
    private Map<String, String> slipItemContent;

    @Override
    public int onLayoutId() {
        return 0;
    }

    @Override
    public void onInitView() {
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_LAST));
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * 检测打印机状态，如果是缺纸，则提示装纸，其它错误则退出打印。
     *
     * @return 小于0则表示失败退出，-2表示缺纸，0表示状态正常
     */
    private int checkPrinterState() {
        EnumPrinterStatus status;
        try {
            IPrinterDev printer = DeviceFactory.getInstance().getPrinterDev();
            status = printer.getPrinterStatus();
            if (EnumPrinterStatus.OK == status)
                return 0;
            if (EnumPrinterStatus.NO_PAPER == status) {
                return -2;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        logger.debug("^_^ EVENT code:" + event.getWhat() + " message:" + event.getMsg() + " ^_^");
        Context context = LastSlipPrintActivity.this;
        switch (event.getWhat()) {
            case TradeMessage.PRINT_SLIP_LAST:
                PrinterProxy printerProxy;
                ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(context)
                        .getSubPrjClassInstance(new CommonManager());
                List<TradeInfoRecord> tradeInfos = null;
                try {
                    tradeInfos = commonManager.getLastTransItem();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return;
                }
                if (null != tradeInfos && tradeInfos.size() > 0) {
                    if (printManager == null)
                        printManager = new PrintManager(context);
                    if (printSlipHelper == null)
                        printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new
                                BasePrintSlipHelper());

                    TradeInfoRecord info = tradeInfos.get(0);
                    String tranCode = info.getTransType();

                    int printState = checkPrinterState();
                    if (printState < 0) {
                        if (printState == -2) {
                            DialogFactory.showSelectDialog(context, "错误", context.getString(R.string.no_paper_tips),
                                    new AlertDialog.ButtonClickListener() {

                                        @Override
                                        public void onClick(AlertDialog.ButtonType button, View v) {
                                            if (AlertDialog.ButtonType.POSITIVE == button)
                                                EventBus.getDefault().post(new PrinteEvent(TradeMessage
                                                        .PRINT_SLIP_LAST));
                                        }
                                    });
                        } else
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_ERROR, "状态异常"));
                        break;
                    }

                    DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
                    if (tranCode.equals(TransCode.SALE_SCAN) || tranCode.equals(TransCode.VOID_SCAN) || tranCode.equals
                            (TransCode.REFUND_SCAN) || tranCode.equals(TransCode.SCAN_PAY) || tranCode.equals
                            (TransCode.SCAN_VOID))
                        printerProxy = printManager.prepare(event.getSlipOwner(), "saleScanSlip");
                    else
                        printerProxy = actionWithChildProj(tranCode, printManager, event.getSlipOwner());
                    slipItemContent = printSlipHelper.trade2PrintData(info.convert2Map());
                    printerProxy.setValue(slipItemContent)
                            .setICTrade(PRINT_IC_INFO.contains(tranCode))
                            .setReprint(true)
                            .addInterpolator(printSlipHelper)
                            .print();
                } else {
                    ViewUtils.showToast(context, context.getString(com.centerm.epos.R.string.tip_no_trade_info));
                }
                break;
            case TradeMessage.PRINT_NEXT_CONFIRM:
                DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        printSlipHelper.setPrintComplete(true);
                        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_LAST, 0, null,
                                PrintManager.SlipOwner.CONSUMER));
                    }
                });
                break;
            case TradeMessage.PRINT_SLIP_COMPLETE:
                DialogFactory.hideAll();
                printSlipHelper.setPrintComplete(false);
                activityStack.pop();
                break;
            case TradeMessage.PRINT_ERROR:
                DialogFactory.hideAll();
                printSlipHelper.setPrintComplete(false);
                ViewUtils.showToast(context, "打印错误：" + event.getMsg());
                break;
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
}
