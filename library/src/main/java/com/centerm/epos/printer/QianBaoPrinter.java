package com.centerm.epos.printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.epos.R;
import com.centerm.epos.bean.ElecSignInfo;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.net.ResponseHandler;
import com.centerm.epos.net.htttp.DefaultHttpClient;
import com.centerm.epos.net.htttp.request.ESignRequest;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ImageUtils;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.common.TransDataKey.iso_f11;
import static com.centerm.epos.common.TransDataKey.iso_f37;
import static com.centerm.epos.common.TransDataKey.iso_f41;
import static com.centerm.epos.common.TransDataKey.iso_f42;

/**
 * Created by ysd on 2016/11/23.
 */

public class QianBaoPrinter implements PrintTransData.PrinterCallBack {
    private Logger logger = Logger.getLogger(QianBaoPrinter.class);
    private static QianBaoPrinter instance;
    private Context context;
    private Map<String, String> mapData;
    private CommonDao<TradePrintData> printDataCommonDao;
    private String transCode;
    private PrintTransData printTransData;
    private Bitmap bitmap;
    private String date;
    private String time;
    private String unKnown;
    private String aid;
    private String arqc;
    private String iad;
    private String atc;
    private String tvr;
    private String tsi;
    private String aip;

    private QianBaoPrinter() {
    }

    public static QianBaoPrinter getMenuPrinter() {
        if (instance == null) {
            instance = new QianBaoPrinter();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void printData(Map<String, String> mapData, String transCode, boolean isRePrint) {
        logger.debug("开始执行打印方法");
        DbHelper dbHelper = new DbHelper(context);
        printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        this.mapData = mapData;
        this.transCode = transCode;
        unKnown = null;
        aid = null;
        arqc = null;
        iad = null;
        atc = null;
        tvr = null;
        tsi = null;
        aip = null;
        try {
            printTransData = PrintTransData.getMenuPrinter();
            printTransData.open(context);
            ifElecSignThenGono();
            List<TradePrintData> printDatas = null;
            try {
                printDatas = printDataCommonDao.queryBuilder().where().eq("iso_f11", mapData.get(TradeInformationTag
                        .TRACE_NUMBER)).query();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            if (mapData.get(TradeInformationTag.MERCHANT_NAME) == null)
                mapData.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(context,
                        BusinessConfig.Key.KEY_MCHNT_NAME));
            if (null != printDatas && printDatas.size() > 0) {
                printDatas.get(0).setRePrint(isRePrint);
                printTransData.printData(mapData, printDatas.get(0), context.getString(TransCode.codeMapName
                        (transCode)), this);
            } else {
                printTransData.printData(mapData, null, context.getString(TransCode.codeMapName(transCode)), this);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 是否支持电子签名，且能读取到图片文件
     */
    private void ifElecSignThenGono() {
        if (Settings.getValue(context, Settings.KEY.CAN_USE_ELECTRONIC_SIGN, BusinessConfig.CAN_USE_ELECTRONIC_SIGN)) {
            String path = Config.Path.SIGN_PATH + File.separator + BusinessConfig.getInstance().getValue(context,
                    BusinessConfig.Key.KEY_BATCH_NO) + "_" + mapData.get(iso_f11) + ".png";
            if (FileUtils.getFileSize(path) > 0) {
                logger.debug("签名图片存在");
                try {
                    FileInputStream inputStream = new FileInputStream(path);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    printTransData.setElecBitMap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                logger.debug("签名图片不存在");
                printTransData.setElecBitMap(null);
            }
        } else {
            logger.debug("电子签名不开启");
            printTransData.setElecBitMap(null);
        }
    }

    @Override
    public void onPrinterFirstSuccess() {
        DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                if (!"Y".equals(Settings.getValue(context, Settings.KEY.PRINT_THREE, "N"))) {
                    printTransData.printThird();
                } else {
                    printTransData.printSecond();
                }
            }
        });
    }

    @Override
    public void onPrinterSecondSuccess() {
        DialogFactory.showPrintDialog(context, new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                printTransData.printThird();
            }
        });
    }

    @Override
    public void onPrinterThreeSuccess() {
        DialogFactory.hideAll();
        requestSendBitmap();
        ViewUtils.showToast(context, context.getString(R.string.tip_print_over));
    }

    private void requestSendBitmap() {
        logger.debug("进入电子签名上送方法");
        DbHelper dbHelper = new DbHelper(context);
        final CommonDao<ElecSignInfo> commonDao = new CommonDao<>(ElecSignInfo.class, dbHelper);
        final String path = Config.Path.RECEIPT_PATH + File.separator + BusinessConfig.getInstance().getValue
                (context, BusinessConfig.Key.KEY_BATCH_NO) + "_" + mapData.get(iso_f11) + ".png";
        if (FileUtils.getFileSize(path) < 0) {
            logger.debug("未找到该电子签名");
            return;
        }
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (null == inputStream) {
            logger.debug("未读取到该电子签名");
            return;
        }
        Bitmap ebitmap = BitmapFactory.decodeStream(inputStream);
        String pic = ImageUtils.bitmaptoString(ebitmap, 100);
        final String termNO = mapData.get(iso_f41);//终端号
        final String merchantNo = mapData.get(iso_f42);//商户号
        String dateTime = printTransData.getTranTime();
        logger.debug("日期时间为：" + dateTime);
        if (dateTime.length() == 14) {
            date = dateTime.substring(0, 8);
            time = dateTime.substring(8, dateTime.length());
        } else {
            date = "";
            time = "";
        }
        final String tranNum = mapData.get(iso_f37);

        final ESignRequest request = new ESignRequest(merchantNo, termNO, date, time, tranNum, pic);
        request.setUrl(Settings.getSlipUploadUrl(context));
        ResponseHandler handler = new ResponseHandler() {
            @Override
            public void onSuccess(String statusCode, String msg, byte[] data) {
                try {
                    JSONObject object = new JSONObject(msg);
                    if (object.has("success") && object.get("success") instanceof Boolean) {
                        boolean result = (boolean) object.get("success");
                        String mms = (String) object.get("msg");
                        logger.debug("签购单上送返回信息：" + mms);
                        if (!result) {
                            ElecSignInfo info = new ElecSignInfo();
                            info.setMchtId(merchantNo);
                            info.setTermId(termNO);
                            info.setTransDate(date);
                            info.setTransTime(time);
                            info.setTransNum(tranNum);
                            info.setPicName(path);
                            commonDao.save(info);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                logger.debug("电子签名上送成功");
            }

            @Override
            public void onFailure(String code, String msg, Throwable error) {
                ElecSignInfo info = new ElecSignInfo();
                info.setMchtId(merchantNo);
                info.setTermId(termNO);
                info.setTransDate(date);
                info.setTransTime(time);
                info.setTransNum(tranNum);
                info.setPicName(path);
                commonDao.save(info);
                logger.error("电子签名上送失败");
            }
        };
        DefaultHttpClient.getInstance().post(context, request, handler);
    }

    @Override
    public void onPrinterFirstFail(int errorCode, String errorMsg) {
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
                                printTransData.printFirst();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    requestSendBitmap();
                                    bitmap.recycle();
                                }
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterSecondFail(int errorCode, String errorMsg) {
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
                                printTransData.printSecond();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    requestSendBitmap();
                                    bitmap.recycle();
                                }
                                break;
                        }
                    }
                });
    }

    @Override
    public void onPrinterThreeFail(int errorCode, String errorMsg) {
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
                                printTransData.printThird();
                                break;
                            case NEGATIVE:
                                DialogFactory.hideAll();
                                if (bitmap != null && !bitmap.isRecycled()) {
                                    requestSendBitmap();
                                    bitmap.recycle();
                                }
                                break;
                        }
                    }
                });

    }
}
