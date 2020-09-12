package com.centerm.epos.ebi.present;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.base.SimpleStringTag;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.ebi.bean.SaleScanResult;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.EbiMessageFactory;
import com.centerm.epos.ebi.ui.fragment.EbiResultFragment;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.HttpCommParameter;
import com.centerm.epos.present.communication.HttpCommParameterWY;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.PrinterProxy;
import com.centerm.epos.printer.BasePrintSlipHelper;
import com.centerm.epos.printer.IPrintSlipHelper;
import com.centerm.epos.redevelop.BasePullCardTip;
import com.centerm.epos.redevelop.IPullCardTip;
import com.centerm.epos.task.AsyncUploadESignatureTask;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.keys.Keys;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.common.TransCode.PRINT_IC_INFO;
import static java.lang.Thread.sleep;

/**
 * Created by yuhc on 2017/4/5.
 */

public class EbiBaseResultPresent extends EbiResultPresent {
    private PrintManager printManager;
    private IPrintSlipHelper printSlipHelper;
    private Map<String, String> slipItemContent;
    private boolean isKeepResultPage = true;
    private int printnum = 0;
    protected ITransactionMessage factory;
    protected CommonDao<ReverseInfo> reverseDao;

    public EbiBaseResultPresent(ITradeView mTradeView) {
        super(mTradeView);
        EventBus.getDefault().register(this);
        factory = EbiMessageFactory.createMessageByType(EbiMessageFactory.MESSAGE_HTTP_JSON, transDatas);
        reverseDao = new CommonDao<>(ReverseInfo.class,
                OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class));
        logger.debug("使用EbiBaseResultPresent");
    }

    @Override
    public void release() {
        EventBus.getDefault().unregister(this);
        super.release();
    }

    @Override
    public boolean isEnableShowingTimeout() {
        return false;
    }

    @Override
    protected void afterInitView() {
        if (isSuccess && (TransCode.NEED_INSERT_TABLE_SETS.contains(mTradeInformation.getTransCode()))) {
            boolean isPicOk = isESignPicOK();
            printManager = new PrintManager(mTradeView.getContext());
            printSlipHelper = (IPrintSlipHelper) ConfigureManager.getSubPrjClassInstance(new BasePrintSlipHelper());
            addTradeInfo();
            slipItemContent = printSlipHelper.trade2PrintData(tempMap);
            if (isPicOk) {
                EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_SLIP, 0, null,
                        PrintManager.SlipOwner.MERCHANT));
            }else {
                DialogFactory.showMessageDialog(mTradeView.getContext(), "提示", "签名图像转换失败，打印原签购单", new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_SLIP, 0, null,
                                PrintManager.SlipOwner.MERCHANT));
                    }
                }, 6);
            }
        }else{
            if (isICInsertTrade()) {
                IPullCardTip cardTip = (IPullCardTip) ConfigureManager.getSubPrjClassInstance(new BasePullCardTip());
                cardTip.start(mTradeView.getHostActivity());
            }
            openTimeOutController();
        }
        isKeepResultPage = ConfigureManager.getInstance(mTradeView.getContext()).isOptionFuncEnable(mTradeView
                .getContext(), Keys.obj().keep_result_page);

    }

    public void sendNotice(){
        if(respDataMap!=null){
            transDatas.put(TradeInformationTag.TRANS_TIME, respDataMap.get(TradeInformationTag.TRANS_TIME));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                final Object msgPacket = factory.packMessage(com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE, transDatas);
                if (msgPacket != null) {
                    try {
                        DataExchanger dataExchanger = new DataExchanger(ICommunication.COMM_HTTP, new HttpCommParameter());
                        sleep(200);
                        byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
                        if (receivedData == null) {
                            logger.error("^_^ 接收数据失败！ ^_^");
                        } else {
                            Map<String, Object> mapData = factory.unPackMessage(com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE, receivedData);
                            if (mapData == null || mapData.size() == 0) {
                                logger.error("mapData is null");
                            } else {
                                if(transDatas.get(JsonKey.isTradeDetail)!=null){
                                    SaleScanResult returnData = (SaleScanResult) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mTradeView.popToast("交易结果上送成功");
                                    }else {
                                        mTradeView.popToast(returnData.getBody().getResponse().getStatus_msg());
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(PrinteEvent event) {
        logger.debug("^_^ EVENT what:" + event.getWhat() + " code:" + event.getCode() + " message:" + event.getMsg() + " ^_^");
        int printState = checkPrinterState();
        if(printState == -101||printState == 4099){
            logger.error("打印异常，退出程序，请做末笔打印");
            mTradeView.popToast("打印异常，退出程序，请做末笔打印");
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            return;
        }
        switch (event.getWhat()) {
            case TradeMessage.PRINT_TRADE_SLIP:
                if (printState < 0) {
                    if (printState == -2) {
                        DialogFactory.showSelectDialog(mTradeView.getContext(), "错误", mTradeView
                                .getStringFromResource(R.string
                                        .no_paper_tips), new AlertDialog.ButtonClickListener() {

                            @Override
                            public void onClick(AlertDialog.ButtonType button, View v) {
                                if (AlertDialog.ButtonType.POSITIVE == button)
                                    EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_SLIP));
                                else {
                                    //releaseAndGoNext();
                                    ((EbiResultFragment)mTradeView).printPDF();
                                }
                            }
                        });
                    } else if(printState == -101||printState == 4099){
                        logger.error("打印异常，退出程序，请做末笔打印");
                        mTradeView.popToast("打印异常，退出程序，请做末笔打印");
                        android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                        System.exit(0);
                    }else
                        EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_ERROR, "状态异常"));
                    break;
                }
                if (1 == BusinessConfig.getInstance().getNumber(mTradeView.getHostActivity(), Keys.obj().printnum)) {
                    printSlipHelper.setPrintComplete(true);
                }
                printnum++;
                DialogFactory.showLoadingDialog(mTradeView.getContext(), mTradeView.getStringFromResource(R.string
                        .tip_printing));
                PrinterProxy printerProxy;
                String tranCode = (String) transDatas.get(TradeInformationTag.TRANSACTION_TYPE);
                if (tranCode.contains("SCAN"))
                    printerProxy = printManager.prepare(event.getSlipOwner(), "saleScanSlip");
                else
                    printerProxy = printManager.prepare(event.getSlipOwner());
                printerProxy.setTransCode(tranCode)
                        .setValue(slipItemContent)
                        .setICTrade(PRINT_IC_INFO.contains(tranCode))
                        .addInterpolator(printSlipHelper)
                        .print();
                break;
            case TradeMessage.PRINT_NEXT_CONFIRM:
                DialogFactory.showPrintDialog(mTradeView.getContext(), new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        if(AlertDialog.ButtonType.NEGATIVE == button){
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_COMPLETE));
                        }else {
                            printnum++;
                            if (printnum >= BusinessConfig.getInstance().getNumber(mTradeView.getHostActivity(), Keys.obj().printnum)) {
                                printSlipHelper.setPrintComplete(true);
                            }
                            EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_SLIP, 0, null,
                                    PrintManager.SlipOwner.CONSUMER));
                        }
                    }
                });
                break;
            case TradeMessage.PRINT_SLIP_COMPLETE:
                DialogFactory.hideAll();
                if (isICInsertTrade()) {
                    IPullCardTip cardTip = (IPullCardTip) ConfigureManager.getSubPrjClassInstance(new BasePullCardTip());
                    cardTip.start(mTradeView.getHostActivity());
                }
                //控制打印成功完成之后是否停留在结果页
                if (!isKeepResultPage) {
                    releaseAndGoNext();
                }else {
                    openTimeOutController();
                }
                ((EbiResultFragment)mTradeView).printPDF();
                break;
            case TradeMessage.PRINT_ERROR:
                DialogFactory.hideAll();
                mTradeView.popToast("打印错误：" + event.getMsg());
                //控制打印失败之后是否停留在结果页
                if (!isKeepResultPage) {
                    releaseAndGoNext();
                }else {
                    openTimeOutController();
                }
                ((EbiResultFragment)mTradeView).printPDF();
                break;
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
            if (EnumPrinterStatus.BUSY == status) {
                return -101;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Object onConfirm(Object paramObj) {
        releaseAndGoNext();
        return null;
    }

    /**
     * 判断手写签名文件是否存在。如果分包功能关闭，而签名图片转换后的数据超过了最大报文限制，签名文件会被删除，此时要提示"打印原签购单"
     * @return  true 存在  false 不存在
     */
    private boolean isESignPicOK(){
        BusinessConfig config = BusinessConfig.getInstance();
        Context context = mTradeView.getContext();
        boolean support = config.getToggle(context, SimpleStringTag.TOGGLE_ESIGN_SUPPORT);
        boolean mul = config.getFlag(context, SimpleStringTag.TOGGLE_ESIGN_MUL_PACKAGE);
        if (support && !mul) {
            String filePath = Config.Path.SIGN_PATH + File.separator + transDatas.get(TradeInformationTag
                    .BATCH_NUMBER) + "_" + transDatas.get(TradeInformationTag.TRACE_NUMBER) + ".png";
            File esignFile = new File(filePath);
            if (esignFile.exists()) {
                String jbigStr = AsyncUploadESignatureTask.bitmaptoJBJGString(filePath);
                int packageLenMax = config.getNumber(context, SimpleStringTag.ESIGN_PACKAGE_LEN_MAX)*2;
                //超过单包允许的最大长度，则放弃签名
                if (jbigStr.length() > packageLenMax) {
                    esignFile.delete();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 打开超时控制，超时后继续执行下一步
     */
    private void openTimeOutController(){
        int timeOutS = BusinessConfig.getInstance().getNumber(mTradeView.getContext(), BusinessConfig.Key.KEY_TRADE_VIEW_OP_TIMEOUT);
        mTradeView.getHostActivity().openPageTimeout(timeOutS, null);
    }

    /**
     * 关闭超时控制
     */
    private void releaseAndGoNext(){
        mTradeView.getHostActivity().clearPageTimeout();
        gotoNextStep();
    }

    public ReverseInfo getReverseInfo(){
        String iso11 = "9999999";
        if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null){
            iso11 = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
        }
        ReverseInfo reverseInfo = reverseDao.queryForId(iso11);
        return reverseInfo;
    }
}
