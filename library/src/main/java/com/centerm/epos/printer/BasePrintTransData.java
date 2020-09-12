package com.centerm.epos.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.view.View;

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
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
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

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_HEIGHT_LARGE;
import static com.centerm.cpay.midsdk.dev.define.printer.PrinterDataItem.FONT_SIZE_SMALL;
import static java.lang.Double.parseDouble;

/**
 * 《基础版本》
 * 汇总及明细数据打印类
 * Created by ysd on 2016/4/20.
 */
public class BasePrintTransData implements IPrintRransData {
    private static BasePrintTransData instance;
    private Intent broadcastIntent;
    public Context context;
    public CpayPrintHelper printHelper;
    private boolean isPrintThree = true;
    private static Logger logger = Logger.getLogger(CommonUtils.class);
    public IPrinterCallBack callBack;
    private String tranCardType, tranType;
    private List<PrinterItem> printerItems;
    private Bitmap bitmap;
    private TradePrintData tradePrintData;
    private String clientName;
    private EreceiptCreator creator;
    private String batchNum;
    private String flowNo;


    /**
     * 连接打印机服务，获取打印机设备
     */
    @Override
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

    @Override
    public void setBatchListener(IPrinterCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void printDataALLTrans(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis) {
        List<List<String>> list = getTotalData(jiejiLis, daijiLis);
        dataALLTrans(list);
    }

    @Override
    public void printDataALLTransEx(List<TradeInfoRecord> tradeInfos) {

    }

    @Override
    public void printBatchTotalData(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis, boolean isPre) {
        List<List<String>> list = getTotalData(jiejiLis, daijiLis);
        batchTotalData(list, isPre);
    }

    public void printDataALLTrans(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis,
                                  List<TradeInfoRecord> scanSaleList, List<TradeInfoRecord> scanVoidList,
                                  List<TradeInfoRecord> scanRefundList) {
        List<List<String>>  list = getTotalData(jiejiLis, daijiLis, scanSaleList, scanVoidList, scanRefundList);
        printDataALLTrans(list);
    }

    public void printBatchTotalData(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis,
                                    List<TradeInfoRecord> scanSaleList, List<TradeInfoRecord> scanVoidList,
                                    List<TradeInfoRecord> scanRefundList, boolean isPre) {
        List<List<String>>  list = getTotalData(jiejiLis, daijiLis, scanSaleList, scanVoidList, scanRefundList);
        printBatchTotalData(list,isPre);
    }

    @Override
    public void printBatchTotalDataEx(List<TradeInfoRecord> tradeInfos, boolean isPre) {

    }

    @Override
    public void printBatchTotalData(String gson, boolean isPre) throws JSONException {
        JSONArray jsonArray = new JSONArray(gson);
        List<List<String>> lists = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            List<String> strings = new ArrayList<>();
            strings.add(object.getString("type"));
            strings.add(object.getString("count"));
            strings.add(object.getString("amount"));
            lists.add(strings);
        }
        batchTotalData(lists, isPre);
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
            for (int i=0;i<lists.size();i++) {
                List<String> strings = lists.get(i);
                JSONObject jsonObj = new JSONObject();//pet对象，json形式
                jsonObj.put("type", strings.get(0));
                jsonObj.put("count", strings.get(1));
                jsonObj.put("amount", strings.get(2));
                jsonarray.put(jsonObj);
                printHelper.addItem(strings.get(0), strings.get(1),
                        DataHelper.saved2Decimal(parseDouble(strings.get(2))), 10, 8, 14, 0, false);
            }
            Double amt = Double.parseDouble(lists.get(0).get(2))
                    - Double.parseDouble(lists.get(1).get(2));
            String result = DataHelper.saved2Decimal(amt);
            if (null != result) {
                //result = result.replace("-", "");
            }
            printHelper.printNewLine(1);
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
            for (int i=0;i<lists.size();i++) {
                List<String> strings = lists.get(i);
                printHelper.addItem(strings.get(0), strings.get(1), DataHelper.saved2Decimal(parseDouble(strings.get(2))), 10, 8, 14, 0, false);
                if(i==1){
                    printHelper.addString("外卡统计", PrinterDataItem.Align.LEFT, 10);
                    printHelper.addItem("借记", "0", "0.00", 10, 8, 14, 0, false);
                    printHelper.addItem("贷记", "0", "0.00", 10, 8, 14, 0, false);
                    printHelper.printNewLine(1);
                    printHelper.addString("扫码类交易统计", PrinterDataItem.Align.LEFT, 10);
                }
            }
            Double amt = Double.parseDouble(lists.get(0).get(2))
                    - Double.parseDouble(lists.get(1).get(2))
                    + Double.parseDouble(lists.get(2).get(2))
                    - Double.parseDouble(lists.get(3).get(2))
                    - Double.parseDouble(lists.get(4).get(2));
            String result = DataHelper.saved2Decimal(amt);
            /*BUGID:0002197
            *@author:zhouzhihua 2017.12.1
            * 当贷记金额比借记金额大时，交易汇总打印金额总计应该为负数
            * */
//            if ( null != result ) {
//                result = result.replace("-", "");
//            }
            printHelper.printNewLine(1);
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

    private List<List<String>> getTotalData(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis) {
        double jiejiAmount = 0.0;
        double daijiAmount = 0.0;
        if (null != jiejiLis) {
            for (TradeInfoRecord jiejiInfo :
                    jiejiLis) {
                String amountStr = DataHelper.formatAmountForShow(jiejiInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    jiejiAmount = DataHelper.formatDouble(jiejiAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        if (null != daijiLis) {
            for (TradeInfoRecord daijiInfo :
                    daijiLis) {
                String amountStr = DataHelper.formatAmountForShow(daijiInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    daijiAmount = DataHelper.formatDouble(daijiAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        List<List<String>> lists = new ArrayList<>();
        List<String> saleStrings = new ArrayList<>();
        saleStrings.add("借记");
        saleStrings.add(null == jiejiLis ? "0" : jiejiLis.size() + "");
        saleStrings.add(jiejiAmount + "");
        lists.add(saleStrings);
        List<String> refundStrings = new ArrayList<>();
        refundStrings.add("贷记");
        refundStrings.add(null == daijiLis ? "0" : daijiLis.size() + "");
        refundStrings.add(daijiAmount + "");
        lists.add(refundStrings);
        return lists;
    }


    /**
     * 批结算总计单
     */
    public void batchTotalData(List<List<String>> lists, boolean isPre) {
        if (null == lists) {
            logger.error("批结算打印汇总时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            if (isPre) {
                printHelper.addString("上批POS结算总计", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
                printHelper.addString("--- 重打印 ---", PrinterDataItem.Align.LEFT, 10);
            } else {
                printHelper.addString("POS结算总计", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
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
                printHelper.addItem(strings.get(0), strings.get(1), DataHelper.saved2Decimal(parseDouble(strings.get
                        (2))), 10, 8, 14, 0, false);
            }
            printHelper.addString("外卡对账平", PrinterDataItem.Align.LEFT, 10);
            printHelper.addItem("借记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.addItem("贷记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.printNewLine(1);
            String result = DataHelper.saved2Decimal(Double.parseDouble(lists.get(0).get(2)) - Double.parseDouble
                    (lists.get(1).get(2)));
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

    public List<List<String>>  getTotalData(List<TradeInfoRecord> jiejiLis, List<TradeInfoRecord> daijiLis,
                                            List<TradeInfoRecord> scanSaleList, List<TradeInfoRecord> scanVoidList,
                                            List<TradeInfoRecord> scanRefundList) {
        double jiejiAmount = 0.0;
        double daijiAmount = 0.0;
        double scanSaleAmount = 0.0;
        double scanVoidAmount = 0.0;
        double scanRefundAmount = 0.0;
        if (null != jiejiLis) {
            for (TradeInfoRecord jiejiInfo :
                    jiejiLis) {
                String amountStr = DataHelper.formatAmountForShow(jiejiInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    jiejiAmount = DataHelper.formatDouble(jiejiAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        if (null != daijiLis) {
            for (TradeInfoRecord daijiInfo :
                    daijiLis) {
                String amountStr = DataHelper.formatAmountForShow(daijiInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    daijiAmount = DataHelper.formatDouble(daijiAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        if (null != scanSaleList) {
            for (TradeInfoRecord scanSaleInfo : scanSaleList) {
                String amountStr = DataHelper.formatAmountForShow(scanSaleInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    scanSaleAmount = DataHelper.formatDouble(scanSaleAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        if (null != scanVoidList) {
            for (TradeInfoRecord scanVoidInfo : scanVoidList) {
                String amountStr = DataHelper.formatAmountForShow(scanVoidInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    scanVoidAmount = DataHelper.formatDouble(scanVoidAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        if (null != scanRefundList) {
            for (TradeInfoRecord scanRefundInfo : scanRefundList) {
                String amountStr = DataHelper.formatAmountForShow(scanRefundInfo.getAmount());
                if (null != amountStr && !"".equals(amountStr)) {
                    scanRefundAmount = DataHelper.formatDouble(scanRefundAmount + DataHelper.parseIsoF4(amountStr));
                }
            }
        }
        List<List<String>> lists = new ArrayList<>();

        List<String> saleStrings = new ArrayList<>();
        saleStrings.add("借记");
        saleStrings.add(null == jiejiLis ? "0" : jiejiLis.size() + "");
        saleStrings.add(jiejiAmount + "");
        lists.add(saleStrings);

        List<String> refundStrings = new ArrayList<>();
        refundStrings.add("贷记");
        refundStrings.add(null == daijiLis ? "0" : daijiLis.size() + "");
        refundStrings.add(daijiAmount + "");
        lists.add(refundStrings);

//        List<String> scanSaleStrings = new ArrayList<>();
//        scanSaleStrings.add("扫码消费");
//        scanSaleStrings.add(null == scanSaleList ? "0" : scanSaleList.size() + "");
//        scanSaleStrings.add(scanSaleAmount + "");
//        lists.add(scanSaleStrings);
//
//        List<String> scanVoidStrings = new ArrayList<>();
//        scanVoidStrings.add("扫码撤销");
//        scanVoidStrings.add(null == scanVoidList ? "0" : scanVoidList.size() + "");
//        scanVoidStrings.add(scanVoidAmount + "");
//        lists.add(scanVoidStrings);
//
//        List<String> scanRefundStrings = new ArrayList<>();
//        scanRefundStrings.add("扫码退货");
//        scanRefundStrings.add(null == scanRefundList ? "0" : scanRefundList.size() + "");
//        scanRefundStrings.add(scanRefundAmount + "");
//        lists.add(scanRefundStrings);

        return lists;
    }


    /**
     * 汇总打印
     */
    public void dataALLTrans(final List<List<String>> lists) {
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
                printHelper.addItem(strings.get(0), strings.get(1), DataHelper.saved2Decimal(parseDouble(strings.get
                        (2))), 10, 8, 14, 0, false);
            }
            printHelper.addString("外卡统计", PrinterDataItem.Align.LEFT, 10);
            printHelper.addItem("借记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.addItem("贷记", "0", "0.00", 10, 8, 14, 0, false);
            printHelper.printNewLine(1);

            String result = DataHelper.saved2Decimal(Double.parseDouble(lists.get(0).get(2)) - Double.parseDouble
                    (lists.get(1).get(2)));
            /*BUGID:0002197
            *@author:zhouzhihua 2017.12.1
            * 当贷记金额比借记金额大时，交易汇总打印金额总计应该为负数
            * */
//            if ( null != result ) {
//                result = result.replace("-", "");
//            }
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
                                            dataALLTrans(lists);
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
    @Override
    public void printDetails(final List<TradeInfoRecord> tradeInfos) {
        try {
            DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
            printHelper.init();
            printHelper.addString("交易明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
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
     */
    @Override
    public void printBatchDetailData(List<TradeInfoRecord> tradeInfos) {
        if (null == tradeInfos) {
            logger.error("批结算打印明细时lists数据异常");
            return;
        }
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
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
        printHelper.addString("凭证号    类型         卡号(订单号)", PrinterDataItem.Align.LEFT, FONT_SIZE_SMALL);
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
            String transtype = TransCode.getTransName(tranType, info.getUnicom_scna_type());
            if (!TransCode.AUTH.equals(tranType)) {
                formatCardNum = DataHelper.formatCardno(cardNum);
            } else {
                formatCardNum= cardNum;
            }
            if(tranType.contains("SCAN")){
                formatCardNum = info.getScanVoucherNo();
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
            } else {
                printHelper.addString(posFlowNo+transtype+formatCardNum, FONT_SIZE_SMALL);
            }
            printHelper.addString(DataHelper.formatAmountForShow(amount)+"    "+permisionNo, FONT_SIZE_SMALL);
        }
    }

    /**
     * 批结算未上送订单详细打印
     */
    @Override
    public void printFailDetailData(List<TradeInfoRecord> rejuseItems, List<TradeInfoRecord> failItems) {
        DialogFactory.showLoadingDialog(context, context.getString(R.string.tip_printing));
        printHelper.init();
        try {
            printHelper.addString("POS结算未上送明细", PrinterDataItem.Align.CENTER, FONT_SIZE_HEIGHT_LARGE);
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
                    printHelper.addString(posFlowNo + "   " + transtype + "           " + formatCardNum,
                            FONT_SIZE_SMALL);
                } else if (transtype.length() == 3) {
                    printHelper.addString(posFlowNo + "   " + transtype + "         " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 4) {
                    printHelper.addString(posFlowNo + "   " + transtype + "       " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 5) {
                    printHelper.addString(posFlowNo + "   " + transtype + "     " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 6) {
                    printHelper.addString(posFlowNo + "   " + transtype + "   " + formatCardNum, FONT_SIZE_SMALL);
                } else if (transtype.length() == 7) {
                    printHelper.addString(posFlowNo + "   " + transtype + " " + formatCardNum, FONT_SIZE_SMALL);
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
    public void printHeader(boolean isLastBatch) {
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
            printHelper.addString("批次号:" + config.getBatchNo(context));
//            if (isLastBatch) {
//                printHelper.addString("批次号:" + config.getValue(context, BusinessConfig.Key.KEY_LAST_BATCH_NO) + "  " +
//                        "操作员：" + config.getValue(context, BusinessConfig.Key.KEY_OPER_ID), PrinterDataItem.Align
//                        .LEFT, 10);
//            } else {
//                printHelper.addString("批次号:" + config.getBatchNo(context) + "  操作员：" + config.getValue(context,
//                        BusinessConfig.Key.KEY_OPER_ID), PrinterDataItem.Align.LEFT, 10);
//            }
            printHelper.addString("日期时间：" + time, PrinterDataItem.Align.LEFT, 10);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 分割线-------
    public void addDivider() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append('-');
        }
        printHelper.addString(sb.toString());
    }
}
