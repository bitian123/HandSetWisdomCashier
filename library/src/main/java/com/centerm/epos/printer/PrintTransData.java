package com.centerm.epos.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.os.RemoteException;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.cpay.midsdk.dev.define.IPrinterDev;
import com.centerm.cpay.midsdk.dev.define.printer.EnumPrinterStatus;
import com.centerm.cpay.midsdk.dev.define.printer.PrintListener;
import com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem;
import com.centerm.epos.R;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.common.PrinterParamEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.EreceiptCreator;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_HEIGHT_LARGE;
import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_SMALL;
import static com.centerm.epos.common.TransDataKey.KEY_HOLDER_NAME;
import static com.centerm.epos.common.TransDataKey.iso_f11;
import static java.lang.Double.parseDouble;

/**
 * v8打印机控制类  这里使用cpay的sdk
 * Created by ysd on 2016/4/20.
 */
public class PrintTransData {
    private static PrintTransData instance;
    private Intent broadcastIntent;
    private Context context;
    private CpayPrintHelper printHelper;
    private boolean isPrintThree = true;
    private static Logger logger = Logger.getLogger(CommonUtils.class);
    private PrinterCallBack callBack;
    private String tranCardType, tranType;
    private List<PrinterItem> printerItems;
    private Bitmap bitmap;
    private TradePrintData tradePrintData;
    private PrintTransHelper printTransHelper;
    private String clientName;
    private EreceiptCreator creator;
    private String batchNum;
    private String flowNo;

    private PrintTransData() {
    }

    public static PrintTransData getMenuPrinter() {
        if (instance == null) {
            instance = new PrintTransData();
        }
        return instance;
    }

    /**
     * 连接打印机服务，获取打印机设备
     *
     * @return
     */
    public boolean open(Context context) {
        this.context = context;
        try {
            DeviceFactory factory = DeviceFactory.getInstance();
            IPrinterDev iPrinterDev = factory.getPrinterDev();
            EnumPrinterStatus status = iPrinterDev.getPrinterStatus();
            printHelper = new CpayPrintHelper(iPrinterDev);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    /**
     * 打印流程控制
     *
     * @param tranType
     * @throws RemoteException
     */
    public void printData(Map<String, String> mapData, TradePrintData tradePrintData, final String tranType, PrinterCallBack callBack) throws RemoteException {
        this.tradePrintData = tradePrintData;
        this.callBack = callBack;
        this.tranType = tranType;
        this.clientName = mapData.get(KEY_HOLDER_NAME);
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printTransHelper = new PrintTransHelper(mapData, context);
        List<PrinterItem> items = printTransHelper.getPrinterItems();
        tranCardType = printTransHelper.getTranCardType();
        printerItems = sortData(items);
        batchNum = BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_BATCH_NO);
        flowNo = mapData.get(TradeInformationTag.TRACE_NUMBER);
        printFirst();
    }

    public String getTranTime() {
        if (null != printTransHelper) {
            return printTransHelper.getField12();
        }
        return null;
    }

    public void printFirst() {
        creator = EreceiptCreator.newInstance(Config.Path.RECEIPT_PATH, batchNum, flowNo);
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printShop(printerItems, tranType, tranCardType);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterFirstSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterFirstFail(i, s);
                }
            });
            //如果是重新打印的就不创建
            if (!tradePrintData.isRePrint()) {
                String path = creator.create(context);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    public void printSecond() {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printShop(printerItems, tranType, tranCardType);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    callBack.onPrinterSecondSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterSecondFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void printThird() {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        try {
            printHelper.init();
            printPersion(printerItems, tranType, tranCardType);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    callBack.onPrinterThreeSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    printHelper.release();
                    callBack.onPrinterThreeFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打印商户联数据
     *
     * @param items
     * @param tranType
     * @throws RemoteException
     */
    private void printShop(List<PrinterItem> items, String tranType, String tranCardType) throws RemoteException, UnsupportedEncodingException {
        boolean isPrintCode = false;
        boolean isNeedSpace = false;
        String describeStr = null;
        String batchNo = null;
        String permision = null;
        String sendCardBrank = null;
        int desTextSize = 0;
        for (PrinterItem printItem : items) {
            String paramId = printItem.getParamId();
            if (paramId.equals(PrinterParamEnum.SHOP_HEADER.getParamId())) {//头部
                printHelper.addString(printItem.getParamValue(), PrinterDataItem.Align.CENTER,converTextSize(printItem.getTextSize()));
                addShopDivider();
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamValue(), converTextSizeForPic(printItem.getTextSize()), Paint.Align.CENTER));
                creator.addElement(new EreceiptCreator.TextItem("------------商户存根------------", EreceiptCreator.FontSize.MEDIUM, Paint.Align.CENTER));
            } else if (paramId.equals(PrinterParamEnum.SHOP_CARD_NUM.getParamId())) {//卡号
                if ("预授权".equals(tranType)) {
                    printHelper.addString(printItem.getParamTip() + ":" + printItem.getParamValue() + "  /" + tranCardType,converTextSize(printItem.getTextSize()));
                    printHelper.addString("交易类型：" + tranType,converTextSize(printItem.getTextSize()));
                    creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip() + ":" + printItem.getParamValue()+ "  /" + tranCardType, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                    creator.addElement(new EreceiptCreator.TextItem("交易类型：" + tranType, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                } else {
                    printHelper.addString(printItem.getParamTip() + ":" + DataHelper.formatCardno(printItem.getParamValue()) + "  /" + tranCardType,converTextSize(printItem.getTextSize()));
                    printHelper.addString("交易类型：" + tranType,converTextSize(printItem.getTextSize()));
                    creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip() + ":" + DataHelper.formatCardno(printItem.getParamValue()) + "  /" + tranCardType, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                    creator.addElement(new EreceiptCreator.TextItem("交易类型：" + tranType, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                }

            } else if (paramId.equals(PrinterParamEnum.SHOP_DATE_TIME.getParamId())) {//日期时间
                printHelper.addString(printItem.getParamTip()+":"+DataHelper.formatDateAndTime(printItem.getParamValue()),converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip()+":"+DataHelper.formatDateAndTime(printItem.getParamValue()), converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            } else if (paramId.equals(PrinterParamEnum.SHOP_AMOUNT.getParamId())) {//消费金额
                //printHelper.addAmount(printItem,converTextSize(printItem.getTextSize()));
                printHelper.addString(printItem.getParamTip()+": RMB "+DataHelper.formatIsoF4(printItem.getParamValue()),converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip()+": RMB "+DataHelper.formatIsoF4(printItem.getParamValue()), converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                printHelper.addString("操作员：" + BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID),converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem("操作员：" + BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID), converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            } else if (paramId.equals(PrinterParamEnum.SHOP_COMMENT.getParamId())) {//备注
                printHelper.addString(printItem.getParamTip() + ":",converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip() + ":", converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                if (null != clientName && !"".equals(clientName)) {
                    printHelper.addString("持卡人姓名：" + clientName,converTextSize(printItem.getTextSize()));
                    creator.addElement(new EreceiptCreator.TextItem("持卡人姓名：" + clientName, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                }
                printHelper.addString(printItem.getParamValue(),converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamValue(), converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            } else if (paramId.equals(PrinterParamEnum.SHOP_DESCRIBE.getParamId())) {
                describeStr = printItem.getParamValue();
                desTextSize = printItem.getTextSize();
            } else if (paramId.equals(PrinterParamEnum.SHOP_BATCH_NUM.getParamId())) {
                batchNo = printItem.getParamTip() + ":" + printItem.getParamValue();
            } else if (paramId.equals(PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamId())) {
                String flowNo = printItem.getParamTip() + ":" + printItem.getParamValue();
                if ("0".equals(flowNo.substring(flowNo.length() - 1, flowNo.length()))) {
                    isPrintCode = true;
                } else {
                    isPrintCode = false;
                }
                //printHelper.addTowItem(batchNo, flowNo, 13, 19, false);
                printHelper.addString(batchNo + "  " + flowNo,converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(batchNo + "  " + flowNo, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            } else if (paramId.equals(PrinterParamEnum.SHOP_PERMISION_CODE.getParamId())) {
                permision = printItem.getParamTip() + ":" + printItem.getParamValue();
                if (printItem.getParamId().length() > 3) {
                    isNeedSpace = true;
                }
            } else if (paramId.equals(PrinterParamEnum.SHOP_REFERENCE_CODE.getParamId())) {
                String reference = printItem.getParamTip() + ":" + printItem.getParamValue();
                if (printItem.getParamId().length() > 3) {
                    isNeedSpace = true;
                }
                //printHelper.addTowItem(permision, reference, 13, 19, false);
                if (isNeedSpace) {
                    printHelper.addString(permision + "  " + reference,converTextSize(printItem.getTextSize()));
                    creator.addElement(new EreceiptCreator.TextItem(permision + "  " + reference, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                } else {
                    printHelper.addString(permision + reference,converTextSize(printItem.getTextSize()));
                    creator.addElement(new EreceiptCreator.TextItem(permision + reference, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
                }
            } else if (paramId.equals(PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamId())) {
                sendCardBrank = printItem.getParamTip() + ":" + printItem.getParamValue();
            } else if (paramId.equals(PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamId())) {
                String receiveBrank = printItem.getParamTip() + ":" + printItem.getParamValue();
                printHelper.addString(sendCardBrank + "  " + receiveBrank,converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(sendCardBrank + "  " + receiveBrank, converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            } else if (paramId.contains("9F0") || paramId.contains("9F1") || paramId.contains("9F2")) {
                printHelper.addString(printItem.getParamTip() + ":" + printItem.getParamValue(),converTextSize(printItem.getTextSize()));
                creator.addElement(new EreceiptCreator.TextItem(printItem.getParamTip() + ":" + printItem.getParamValue(), converTextSizeForPic(printItem.getTextSize()), Paint.Align.LEFT));
            }
        }
        addICCardParam();
        if (tradePrintData.isRePrint()) {
            printHelper.addString("***重打印凭证***", PrinterDataItem.Align.CENTER);
        }
        if (tradePrintData.isNoNeedSign() && tradePrintData.isNoNeedPin()) {
            printHelper.addString("交易金额未超" + DataHelper.formatAmountForShow(tradePrintData.getAmount()) + "元，免密免签");
            creator.addElement(new EreceiptCreator.TextItem("交易金额未超" + DataHelper.formatAmountForShow(tradePrintData.getAmount()) + "元，免密免签", EreceiptCreator.FontSize.MEDIUM, Paint.Align.LEFT));
        } else if (tradePrintData.isNoNeedSign()) {
            printHelper.addString("交易金额未超" + DataHelper.formatAmountForShow(tradePrintData.getAmount()) + "元，无需签名");
            creator.addElement(new EreceiptCreator.TextItem("交易金额未超" + DataHelper.formatAmountForShow(tradePrintData.getAmount()) + "元，无需签名", EreceiptCreator.FontSize.MEDIUM, Paint.Align.LEFT));
        }
        if (!tradePrintData.isNoNeedSign()) {
            printHelper.addString("持卡人签名：");
            creator.addElement(new EreceiptCreator.TextItem("持卡人签名：", EreceiptCreator.FontSize.MEDIUM, Paint.Align.LEFT));
            if (null != bitmap) {
                logger.debug("打印时，bitmap是存在的");
                printHelper.reSetTast();
                Bitmap resultBit = DataHelper.resize(bitmap, 210, 140);
                printHelper.addPrinterTask(resultBit, 50, 0, 0);
                creator.addElement(new EreceiptCreator.PictureItem(Config.Path.SIGN_PATH + File.separator +batchNum+"_"+flowNo+".png"));
            }
        }
        addFooter();
        creator.addElement(new EreceiptCreator.TextItem("--------------------------------", EreceiptCreator.FontSize.MEDIUM, Paint.Align.LEFT));
        creator.addElement(new EreceiptCreator.TextItem("本人确认以上交易，同意记入本卡账户", EreceiptCreator.FontSize.MEDIUM, Paint.Align.CENTER));
      /*  else {
            printHelper.addFooter();
        }*/
        if (null != describeStr) {
            printHelper.addString(describeStr,converTextSize(desTextSize));
            creator.addElement(new EreceiptCreator.TextItem(describeStr, converTextSizeForPic(desTextSize), Paint.Align.LEFT));
        }
        if (isPrintCode) {
            try {
                addDivider();
                printHelper.reSetTast();
                String path = Config.Path.CODE_PATH + File.separator + Config.Path.CODE_FILE_NAME;
                if (FileUtils.getFileSize(path)>0) {
                    FileInputStream inputStream = new FileInputStream(path);
                    Bitmap code = BitmapFactory.decodeStream(inputStream);
                    printHelper.addPrinterTask(code, 100, 150, 150);
                    printHelper.addString("扫描下载商户端APP会有更多惊喜", PrinterDataItem.Align.CENTER);
                    creator.addElement(new EreceiptCreator.TextItem("--------------------------------", EreceiptCreator.FontSize.MEDIUM, Paint.Align.LEFT));
                    creator.addElement(context, new EreceiptCreator.PictureItem(path));
                    creator.addElement(new EreceiptCreator.TextItem("扫描下载商户端APP会有更多惊喜", EreceiptCreator.FontSize.MEDIUM, Paint.Align.CENTER));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 打印ic卡参数
     * @throws RemoteException
     */
    private void addICCardParam() throws RemoteException {
        if (tradePrintData != null) {
            if (tradePrintData.getArqc() != null) {
                printHelper.addString("ARQC:" + tradePrintData.getArqc(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("ARQC:" + tradePrintData.getArqc(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (tradePrintData.getTvr() != null) {
                printHelper.addString("TVR:" + tradePrintData.getTvr(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("TVR:" + tradePrintData.getTvr(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (tradePrintData.getAid() != null) {
                printHelper.addString("AID:" + tradePrintData.getAid(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("AID:" + tradePrintData.getAid(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (null != tradePrintData.getAtc() && null != tradePrintData.getTsi()) {
                printHelper.addString("ATC:" + tradePrintData.getAtc() + "  TSI:" + tradePrintData.getTsi(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("ATC:" + tradePrintData.getAtc() + "  TSI:" + tradePrintData.getTsi(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (null != tradePrintData.getUmpr_num()) {
                printHelper.addString("UMPR MUM:" + tradePrintData.getUmpr_num(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("UMPR MUM:" + tradePrintData.getUmpr_num(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (null != tradePrintData.getAip()) {
                printHelper.addString("AIP:" + tradePrintData.getAip(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("AIP:" + tradePrintData.getAip(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
            if (null != tradePrintData.getIad()) {
                printHelper.addString("IAD:" + tradePrintData.getIad(), FONT_SIZE_SMALL);
                creator.addElement(new EreceiptCreator.TextItem("IAD:" + tradePrintData.getIad(), EreceiptCreator.FontSize.SMALL, Paint.Align.LEFT));
            }
        }
    }

    /**
     * 打印持卡人联
     *
     * @param items
     * @param tranType
     * @throws RemoteException
     */
    private void printPersion(List<PrinterItem> items, String tranType, String tranCardType) throws RemoteException, UnsupportedEncodingException {
        boolean isNeedSpace = false;
        String describeStr = null;
        int desTextSize = 0;
        String batchNo = null;
        String permision = null;
        String sendCardBrank = null;
        for (PrinterItem printItem :
                items) {
            String paramId = printItem.getParamId();
            if (paramId.equals(PrinterParamEnum.PERSON_HEADER.getParamId())) {
                printHelper.addString(printItem.getParamValue(), PrinterDataItem.Align.CENTER,converTextSize(printItem.getTextSize()));
                printPersonDivider();
            } else if (paramId.equals(PrinterParamEnum.PERSON_CARD_NUM.getParamId())) {
                if ("预授权".equals(tranType)) {
                    printHelper.addString(printItem.getParamTip() + ":" + printItem.getParamValue()+ "  /" + tranCardType, converTextSize(printItem.getTextSize()));
                    printHelper.addString("交易类型：" + tranType, converTextSize(printItem.getTextSize()));
                } else {
                    printHelper.addString(printItem.getParamTip() + ":" + DataHelper.formatCardno(printItem.getParamValue()) + "  /" + tranCardType,converTextSize(printItem.getTextSize()));
                    printHelper.addString("交易类型：" + tranType,converTextSize(printItem.getTextSize()));
                }
            } else if (paramId.equals(PrinterParamEnum.PERSON_DATE_TIME.getParamId())) {
                printHelper.addString(printItem.getParamTip()+":"+DataHelper.formatDateAndTime(printItem.getParamValue()),converTextSize(printItem.getTextSize()));
            } else if (paramId.equals(PrinterParamEnum.PERSON_AMOUNT.getParamId())) {
                printHelper.addString(printItem.getParamTip()+": RMB "+DataHelper.formatIsoF4(printItem.getParamValue()),converTextSize(printItem.getTextSize()));
                printHelper.addString("操作员：" + BusinessConfig.getInstance().getValue(context, BusinessConfig.Key.KEY_OPER_ID),converTextSize(printItem.getTextSize()));
            } else if (paramId.equals(PrinterParamEnum.PERSON_COMMENT.getParamId())) {
                printHelper.addString(printItem.getParamTip() + ":",converTextSize(printItem.getTextSize()));
                if (null != clientName && !"".equals(clientName)) {
                    printHelper.addString("持卡人姓名：" + clientName,converTextSize(printItem.getTextSize()));
                }
                printHelper.addString(printItem.getParamValue(),converTextSize(printItem.getTextSize()));
            } else if (paramId.equals(PrinterParamEnum.PERSON_DESCRIBE.getParamId())) {
                describeStr = printItem.getParamValue();
                desTextSize = printItem.getTextSize();
            } else if (paramId.equals(PrinterParamEnum.PERSON_BATCH_NUM.getParamId())) {
                batchNo = printItem.getParamTip() + ":" + printItem.getParamValue();
            } else if (paramId.equals(PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamId())) {
                String flowNo = printItem.getParamTip() + ":" + printItem.getParamValue();
                //printHelper.addTowItem(batchNo, flowNo, 13, 19, false);
                printHelper.addString(batchNo + "  " + flowNo,converTextSize(printItem.getTextSize()));
            } else if (paramId.equals(PrinterParamEnum.PERSON_PERMISION_CODE.getParamId())) {
                permision = printItem.getParamTip() + ":" + printItem.getParamValue();
                if (printItem.getParamId().length() > 3) {
                    isNeedSpace = true;
                }
            } else if (paramId.equals(PrinterParamEnum.PERSON_REFERENCE_CODE.getParamId())) {
                String reference = printItem.getParamTip() + ":" + printItem.getParamValue();
                //printHelper.addTowItem(permision, reference, 13, 19, false);
                if (printItem.getParamId().length() > 3) {
                    isNeedSpace = true;
                }
                if (isNeedSpace) {
                    printHelper.addString(permision + "  " + reference,converTextSize(printItem.getTextSize()));
                } else {
                    printHelper.addString(permision + reference,converTextSize(printItem.getTextSize()));
                }
            } else if (paramId.equals(PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamId())) {
                sendCardBrank = printItem.getParamTip() + ":" + printItem.getParamValue();
            } else if (paramId.equals(PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamId())) {
                String receiveBrank = printItem.getParamTip() + ":" + printItem.getParamValue();
                printHelper.addString(sendCardBrank + "  " + receiveBrank,converTextSize(printItem.getTextSize()));
            } else if (paramId.contains("9F5") || paramId.contains("9F6") || paramId.contains("9F7")) {
                printHelper.addString(printItem.getParamTip() + ":" + printItem.getParamValue(),converTextSize(printItem.getTextSize()));
            }
        }
        addICCardParam();
        if (tradePrintData.isRePrint()) {
            printHelper.addString("***重打印凭证***", PrinterDataItem.Align.CENTER);
        }
        addFooter();
        if (null != describeStr) {
            printHelper.addString(describeStr,converTextSize(desTextSize));
        }
    }

    /**
     * list数据排序
     *
     * @param printItems
     * @return
     */
    private List<PrinterItem> sortData(List<PrinterItem> printItems) {
        Comparator<PrinterItem> comparator = new Comparator<PrinterItem>() {
            public int compare(PrinterItem s1, PrinterItem s2) {
                return s1.getPrintRange() - s2.getPrintRange();
            }
        };
        List<PrinterItem> items = new ArrayList<>();
        items.addAll(printItems);
        Collections.sort(items, comparator);
        return items;
    }



    public void setBatchListener(PrinterCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * 批结算总计单
     *
     * @param lists
     */
    public void printBatchTotalData(List<List<String>> lists, boolean isPre) {
        if (null == lists) {
            logger.error("批结算打印汇总时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            if (isPre) {
                printHelper.addString("上批POS结算总计",PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
                printHelper.addString("--- 重打印 ---", PrinterDataItem.Align.LEFT, 10);
            } else {
                printHelper.addString("POS结算总计",PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            }
            if (isPre) {
                printHeader(true);
            } else {
                printHeader(false);
            }
            printHelper.addString("交易类型      总笔数     总金额", PrinterDataItem.Align.LEFT, 10);
            printHelper.addString("--------------------------------", PrinterDataItem.Align.LEFT, 10);
            String isEquels = Settings.getValue(context, Settings.KEY.IS_BATCH_EQUELS, "-1");
            if ("-1".equals(isEquels)) {
                logger.error("没有读取到对账数据");
            } else if ("0".equals(isEquels)) {
                logger.error("对账数据为0异常");
            } else if ("1".equals(isEquels)) {
                printHelper.addString("内卡对账平", PrinterDataItem.Align.LEFT, 10);
            } else if ("2".equals(isEquels)) {
                printHelper.addString("内卡对账不平", PrinterDataItem.Align.LEFT, 10);
            }
            JSONArray jsonarray = new JSONArray();//json数组，里面包含的内容为pet的所有对象
            for (List<String> strings :
                    lists) {
                JSONObject jsonObj = new JSONObject();//pet对象，json形式
                jsonObj.put("type", strings.get(0));
                jsonObj.put("count", strings.get(1));
                jsonObj.put("amount", strings.get(2));
                jsonarray.put(jsonObj);
                printHelper.addItem(strings.get(0), strings.get(1), DataHelper.saved2Decimal(parseDouble(strings.get(2))), 10, 8, 14, 0, false);
            }
            printHelper.addString("外卡对账平", PrinterDataItem.Align.LEFT, 10);
            printHelper.addItem("借记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.addItem("贷记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.printNewLine(1);
            String result = DataHelper.saved2Decimal(Double.parseDouble(lists.get(0).get(2)) - Double.parseDouble(lists.get(1).get(2)));
            if (null != result) {
                result = result.replace("-", "");
            }
            printHelper.addItem("金额总计：", "", result + "", 10, 8, 14, 0, false);
            String gsonStr = jsonarray.toString();
            Settings.setValue(context, Settings.KEY.PREV_BATCH_TOTAL, gsonStr);
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    DialogFactory.hideAll();
                    callBack.onPrinterFirstSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    DialogFactory.hideAll();
                    callBack.onPrinterFirstFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 汇总打印
     *
     * @return
     */
    public void printDataALLTrans(final List<List<String>> lists) {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printHelper.addString("交易汇总", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHelper.addString("--- 重打印 ---", PrinterDataItem.Align.LEFT, 10);
            printHeader(false);
            printHelper.addString("交易类型      总笔数     总金额", PrinterDataItem.Align.LEFT, 10);
            printHelper.addString("--------------------------------", PrinterDataItem.Align.LEFT, 10);
            JSONArray jsonarray = new JSONArray();//json数组，里面包含的内容为pet的所有对象
            printHelper.addString("内卡统计", PrinterDataItem.Align.LEFT, 10);
            for (List<String> strings :
                    lists) {
                printHelper.addItem(strings.get(0), strings.get(1), DataHelper.saved2Decimal(parseDouble(strings.get(2))), 10, 8, 14, 0, false);
            }
            printHelper.addString("外卡统计", PrinterDataItem.Align.LEFT, 10);
            printHelper.addItem("借记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.addItem("贷记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.printNewLine(1);
            String result = DataHelper.saved2Decimal(Double.parseDouble(lists.get(0).get(2)) - Double.parseDouble(lists.get(1).get(2)));
            if (null != result) {
                result = result.replace("-", "");
            }
            printHelper.addItem("金额总计：", "", result + "", 10, 8, 14, 0, false);
//            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    DialogFactory.hideAll();
                    ViewUtils.showToast(context, R.string.tip_print_over);
                }

                @Override
                public void onError(int errorCode, String errorMsg) {
                    if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
                        errorMsg = "打印机缺纸，请放入打印纸";
                    final int eCode = errorCode;
                    final String eMsg = errorMsg;
                    DialogFactory.showSelectPirntDialog(context,
                            "提示",
                            errorMsg,
                            new AlertDialog.ButtonClickListener() {
                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
                                            printDataALLTrans(lists);
                                            break;
                                        case NEGATIVE:
                                            printHelper.release();
                                            DialogFactory.hideAll();
                                            break;
                                    }
                                }
                            });

                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            DialogFactory.hideAll();
        }
    }

    // 打印交易明细数据
    public void printDetails(final List<TradeInfoRecord> tradeInfos) {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printHelper.addString("交易明细",PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHelper.addString("--- 重打印 ---", PrinterDataItem.Align.LEFT, 10);
            printHeader(false);
            addDetailData(tradeInfos);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    printHelper.release();
                    DialogFactory.hideAll();
                    ViewUtils.showToast(context, R.string.tip_print_over);
                }
                @Override
                public void onError(int errorCode, String errorMsg) {
                    if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
                        errorMsg = "打印机缺纸，请放入打印纸";
                    final int eCode = errorCode;
                    final String eMsg = errorMsg;
                    DialogFactory.showSelectPirntDialog(context,
                            "提示",
                            errorMsg,
                            new AlertDialog.ButtonClickListener() {
                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
                                            printDetails(tradeInfos);
                                            break;
                                        case NEGATIVE:
                                            printHelper.release();
                                            DialogFactory.hideAll();
                                            break;
                                    }
                                }
                            });

                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批结算明细打印
     *
     * @param tradeInfos
     */
    public void printBatchDetailData(List<TradeInfoRecord> tradeInfos) {
        if (null == tradeInfos) {
            logger.error("批结算打印明细时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算明细",PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            printHelper.printNewLine(1);
            addDetailData(tradeInfos);
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterSecondSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterSecondFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 交易明细数据构造
     * @param tradeInfos
     * @throws RemoteException
     */
    private void addDetailData(List<TradeInfoRecord> tradeInfos) throws RemoteException {
        printHelper.addString("凭证号    类型         卡号", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
        printHelper.addString("金额     授权码", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
        addDivider();
        for (int i = 1; i <= tradeInfos.size(); i++) {
                /*凭证号 11域  类型   卡号  金额   授权码*/
            String formatCardNum = null;
            TradeInfoRecord info = tradeInfos.get(i - 1);
            String posFlowNo = info.getVoucherNo() == null ? "" : info.getVoucherNo();//凭证号
            String tranType = info.getTransType() == null ? "" : info.getTransType();
            String cardNum = info.getCardNo() == null ? "" : info.getCardNo();
            String amount = info.getAmount() == null ? "" : info.getAmount();
            String permisionNo = info.getAuthorizeNo() == null ? "" : info.getAuthorizeNo();
            String transtype = context.getString(TransCode.codeMapName(tranType));
            if (!TransCode.AUTH.equals(tranType)) {
                formatCardNum = DataHelper.formatCardno(cardNum);
            } else {
                formatCardNum= cardNum;
            }

            if (transtype.length() == 2) {
                printHelper.addString(posFlowNo+"   "+transtype+"           "+formatCardNum, FONT_SIZE_SMALL);
            } else if (transtype.length() == 3) {
                printHelper.addString(posFlowNo+"   "+transtype+"         "+formatCardNum, FONT_SIZE_SMALL);
            } else if (transtype.length() == 4) {
                printHelper.addString(posFlowNo+"   "+transtype+"       "+formatCardNum, FONT_SIZE_SMALL);
            } else if (transtype.length() == 5) {
                printHelper.addString(posFlowNo+"   "+transtype+"     "+formatCardNum, FONT_SIZE_SMALL);
            } else if (transtype.length() == 6) {
                printHelper.addString(posFlowNo+"   "+transtype+"   "+formatCardNum, FONT_SIZE_SMALL);
            } else if (transtype.length() == 7) {
                printHelper.addString(posFlowNo+"   "+transtype+" "+formatCardNum, FONT_SIZE_SMALL);
            }
            printHelper.addString(DataHelper.formatAmountForShow(amount)+"    "+permisionNo, FONT_SIZE_SMALL);
        }
    }

    /**
     * 批结算未上送订单详细打印
     *
     * @param rejuseItems
     * @param failItems
     */
    public void printFailDetailData(List<TradeInfoRecord> rejuseItems, List<TradeInfoRecord> failItems) {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算未上送明细",PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
            printHeader(false);
            if (null != failItems && failItems.size() > 0) {
                printHelper.addString("未成功上送交易明细", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
                toolMethod(failItems);
            }
            if (null != rejuseItems && rejuseItems.size() > 0) {
                printHelper.addString("上送后被平台拒绝交易明细", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
                toolMethod(rejuseItems);
            }
            printHelper.printNewLine(2);
            printHelper.print(new PrintListener() {
                @Override
                public void onFinish() {
                    callBack.onPrinterThreeSuccess();
                }

                @Override
                public void onError(int i, String s) {
                    callBack.onPrinterThreeFail(i, s);
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void toolMethod(List<TradeInfoRecord> infos) {
        try {
            printHelper.addString("凭证号    类型          卡号", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            printHelper.addString("金额", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
            addDivider();
            for (int i = 1; i <= infos.size(); i++) {
                /*凭证号    类型       卡号     金额*/
                TradeInfoRecord info = infos.get(i - 1);
                String posFlowNo = info.getVoucherNo() == null ? "" : info.getVoucherNo();//凭证号
                String tranType = info.getTransType() == null ? "" : info.getTransType();
                String cardNum = info.getCardNo() == null ? "" : info.getCardNo();
                String amount = info.getAmount() == null ? "" : info.getAmount();
                String transtype = context.getString(TransCode.codeMapName(tranType));
                String formatCardNum = DataHelper.formatCardno(cardNum);
                if (transtype.length() == 2) {
                    printHelper.addString(posFlowNo+"   "+transtype+"           "+formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 3) {
                    printHelper.addString(posFlowNo+"   "+transtype+"         "+formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 4) {
                    printHelper.addString(posFlowNo+"   "+transtype+"       "+formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 5) {
                    printHelper.addString(posFlowNo+"   "+transtype+"     "+formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 6) {
                    printHelper.addString(posFlowNo+"   "+transtype+"   "+formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 7) {
                    printHelper.addString(posFlowNo+"   "+transtype+" "+formatCardNum, FONT_SIZE_SMALL);
                }
                printHelper.addString(DataHelper.formatAmountForShow(amount), FONT_SIZE_SMALL);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印签购单头部
     *
     * @param isLastBatch 是否是上一批交易
     */
    private void printHeader(boolean isLastBatch) {
        BusinessConfig config = BusinessConfig.getInstance();
        try {
            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String time = newFormat.format(new Date());
            String merName = config.getValue(context, BusinessConfig.Key.KEY_MCHNT_NAME);
            String merCode = BusinessConfig.getInstance().getIsoField(context, 42);
            String terCode = BusinessConfig.getInstance().getIsoField(context, 41);
            printHelper.addString("商户名称:" + merName, 10);
            printHelper.addString("商户编号:" + merCode, 10);
            printHelper.addString("终端号:" + terCode, 10);
            if (isLastBatch) {
                printHelper.addString("批次号:" + config.getValue(context, BusinessConfig.Key.KEY_LAST_BATCH_NO) + "  操作员：" + config.getValue(context, BusinessConfig.Key.KEY_OPER_ID), PrinterDataItem.Align.LEFT, 10);
            } else {
                printHelper.addString("批次号:" + config.getBatchNo(context) + "  操作员：" + config.getValue(context, BusinessConfig.Key.KEY_OPER_ID), PrinterDataItem.Align.LEFT, 10);
            }
            printHelper.addString("日期时间：" + time, PrinterDataItem.Align.LEFT, 10);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setElecBitMap(Bitmap bitmap) {
        this.bitmap = null;
        this.bitmap = bitmap;
    }

    //商户存根
    public void addShopDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append('-');
        }
        sb.append("商户存根");
        for (int i = 0; i < 12; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }
    //持卡人存根
    public void printPersonDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            sb.append('-');
        }
        sb.append("持卡人存根");
        for (int i = 0; i < 11; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }

    public void addFooter() throws RemoteException {
        addDivider();
        printHelper.addString("本人确认以上交易，同意记入本卡账户", PrinterDataItem.Align.CENTER);
    }

    // 分割线-------
    public void addDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }
    public interface PrinterCallBack {
        void onPrinterFirstSuccess();

        void onPrinterSecondSuccess();

        void onPrinterThreeSuccess();

        void onPrinterFirstFail(int errorCode, String errorMsg);

        void onPrinterSecondFail(int errorCode, String errorMsg);

        void onPrinterThreeFail(int errorCode, String errorMsg);
    }
    public int converTextSize(int textSize){
        if (textSize == 1) {
            return FONT_SIZE_SMALL;
        } else if (textSize == 2) {
            return 0;
        } else if (textSize == 3) {
            return FONT_SIZE_HEIGHT_LARGE;
        }
        return 0;
    }
    public EreceiptCreator.FontSize converTextSizeForPic(int textSize){
        if (textSize == 1) {
            return EreceiptCreator.FontSize.SMALL;
        } else if (textSize == 2) {
            return EreceiptCreator.FontSize.MEDIUM;
        } else if (textSize == 3) {
            return EreceiptCreator.FontSize.LARGE;
        }
        return EreceiptCreator.FontSize.MEDIUM;
    }
}
