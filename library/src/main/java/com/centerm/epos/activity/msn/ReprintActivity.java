package com.centerm.epos.activity.msn;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.printer.IPrinterCallBack;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.redevelop.IRedevelopAction;
import com.centerm.epos.utils.CommonUtils;
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

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;

/**
 * 系统管理
 * Created by liubit on 2019/9/5.
 */
public class ReprintActivity extends BaseActivity implements View.OnClickListener{
    private Context context;
    private PrintManager printManager;
    private IPrintSlipHelper printSlipHelper;
    private Map<String, String> slipItemContent;
    private int printnum = 0;
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        logger.debug("^_^ EVENT code:" + event.getWhat() + " message:" + event.getMsg() + " ^_^");
        switch (event.getWhat()) {
            case TradeMessage.PRINT_NEXT_CONFIRM:
                DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        if (AlertDialog.ButtonType.NEGATIVE == button) {
                            printSlipHelper.setPrintComplete(true);
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                        } else {
                            printLast();
                        }
                    }
                });
                break;
            case TradeMessage.PRINT_SLIP_COMPLETE:
                DialogFactory.hideAll();
                if(printSlipHelper!=null){
                    printSlipHelper.setPrintComplete(false);
                }
                printnum = 0;
                break;
            case TradeMessage.PRINT_ERROR:
                printnum = 0;
                DialogFactory.hideAll();
                if(printSlipHelper!=null){
                    printSlipHelper.setPrintComplete(false);
                }
                ViewUtils.showToast(context, "打印错误：" + event.getMsg());
                break;
        }
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_reprint;
    }

    @Override
    public void onInitView() {
        initBackBtn();
        EventBus.getDefault().register(this);
        context = ReprintActivity.this;

        findViewById(R.id.mBtnPrintLast).setOnClickListener(this);
        findViewById(R.id.mBtnPrintAny).setOnClickListener(this);
        findViewById(R.id.mBtnQuery).setOnClickListener(this);

    }

    //查询
    public void onReprintSlip(View view) {
        if (CommonUtils.isFastClick()) {
            logger.debug("==>快速点击事件，不响应！");
            return;
        }

    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if( v.getId() == R.id.mBtnPrintLast){
            printLast();
        }else if( v.getId() == R.id.mBtnPrintAny){
            startActivity(new Intent(context, BaseQueryTradeActivity.class));
        }else if( v.getId() == R.id.mBtnQuery){
            startActivity(new Intent(context, AbnormalQueryTradeActivity.class));
        }
    }

    private void printLast(){
        final PrinterProxy printerProxy;
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

            if (1 == BusinessConfig.getInstance().getNumber(context, Keys.obj().printnum)) {
                printSlipHelper.setPrintComplete(true);
            }
            printnum++;
            TradeInfoRecord info = tradeInfos.get(0);
            String tranCode = info.getTransType();

            int printState = checkPrinterState();
            if (printState < 0) {
                if (printState == -2) {
                    DialogFactory.showSelectDialog(context, "错误", context.getString(R.string
                                    .no_paper_tips),
                            new AlertDialog.ButtonClickListener() {

                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    if (AlertDialog.ButtonType.POSITIVE == button)
                                        EventBus.getDefault().post(new PrinteEvent(TradeMessage
                                                .PRINT_SLIP_LAST));
                                }
                            });
                } else {
                    EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_ERROR, "状态异常"));
                }
                return;
            }

            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            if(printnum==1){
                printerProxy = actionWithChildProj(tranCode, printManager, PrintManager.SlipOwner.MERCHANT);
            }else {
                printerProxy = actionWithChildProj(tranCode, printManager, PrintManager.SlipOwner.CONSUMER);
            }
            if (printnum >= BusinessConfig.getInstance().getNumber(context, Keys.obj().printnum)) {
                printSlipHelper.setPrintComplete(true);
            }
            Map<String, String> tradeData = info.convert2Map();
            if(!TextUtils.isEmpty(info.getUnicom_scna_type())){
                tradeData.put("pay_type", info.getUnicom_scna_type());
            }
            slipItemContent = printSlipHelper.trade2PrintData(tradeData);
            printerProxy.setTransCode(tranCode)
                    .setValue(slipItemContent)
                    .setICTrade(PRINT_IC_INFO.contains(tranCode))
                    .setReprint(true)
                    .addInterpolator(printSlipHelper)
                    .print();
        } else {
            ViewUtils.showToast(context, context.getString(com.centerm.epos.R.string
                    .tip_no_trade_info));
        }
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

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
