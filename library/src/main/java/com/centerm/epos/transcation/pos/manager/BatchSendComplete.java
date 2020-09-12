package com.centerm.epos.transcation.pos.manager;

import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.centerm.cloudsys.sdk.common.utils.FileUtils;
import com.centerm.cpay.midsdk.dev.common.exception.ErrorCode;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.Settings;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.printer.BasePrintTransData;
import com.centerm.epos.printer.IPrintRransData;
import com.centerm.epos.printer.IPrinterCallBack;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.redevelop.ITradeRecordInformation;
import com.centerm.epos.redevelop.TradeRecordInfoImpl;
import com.centerm.epos.task.AsyncAutoSignOut;
import com.centerm.epos.task.AsyncBatchUploadDown;
import com.centerm.epos.task.AsyncQueryPrintDataTask;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.keys.Keys;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import config.BusinessConfig;
import config.Config;

import static com.centerm.epos.common.TransDataKey.key_batch_upload_count;
import static com.centerm.epos.common.TransDataKey.key_is_amount_ok;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle_foreign;

/**
 * Created by yuhc on 2017/4/3.
 */

public class BatchSendComplete implements ManageTransaction, IPrinterCallBack {
    protected Logger logger = Logger.getLogger(this.getClass());
    private List<TradeInfoRecord> jiejiList;
    private List<TradeInfoRecord> daijiList;
    private List<TradeInfoRecord> rejestList;
    private List<TradeInfoRecord> failList;
    private List<TradeInfoRecord> batchDetailList;
    private List<TradeInfoRecord> scanSaleList,scanVoidList,scanRefundList;
    private List<List<String>> lists;
    private double jiejiAmount;
    private double daijiAmount;
    private BasePrintTransData printTransData;

    ITradeView mTradingView;
    BaseTradePresent mTradePresent;

    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {

        String innerBalance = (String) tradePresent.getTransData().get(key_is_balance_settle);
        String foreignBalance = (String) tradePresent.getTransData().get(key_is_balance_settle_foreign);

        tradePresent.getTransData().clear();
        /*
        *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
        *@author zhouzhihua 2017.11.07
        * */
        tradePresent.getTransData().put(key_is_balance_settle, innerBalance);
        tradePresent.getTransData().put(key_is_balance_settle_foreign, foreignBalance);

        tradePresent.getTransData().put(key_is_amount_ok, "1");
        initTradeCount(tradePresent);
        new AsyncBatchUploadDown(tradeView.getHostActivity(), tradePresent.getTransData()) {
            @Override
            public void onStart() {
                super.onStart();
                if (tradeView instanceof ITradingView)
                    ((ITradingView) tradeView).updateHint("正在请求批上送完成");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(strings[0])) {
                    if (tradeView instanceof ITradingView)
                        ((ITradingView) tradeView).updateHint(R.string.tip_batch_send_down);
                    //开始打印
                    printTotalData();
                } else {
                    tradePresent.gotoNextStep();
                    logger.error("上送完成请求返失败（有收到平台返回值）");
                    ViewUtils.showToast(tradeView.getHostActivity(), R.string.tip_batch_down_fail);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (!(tradeView instanceof ITradingView))
            return;
        mTradingView = tradeView;
        mTradePresent = tradePresent;
    }

    private void initTradeCount(BaseTradePresent tradePresent) {
        ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext())
                .getSubPrjClassInstance(new CommonManager());
        int count = 0;
        try {
            count = commonManager.getBatchSendRecordCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tradePresent.getTransData().put(key_batch_upload_count, "" + count);
    }

    private void printTotalData() {
        jiejiAmount = 0.0;
        daijiAmount = 0.0;
        new AsyncQueryPrintDataTask(mTradingView.getHostActivity()) {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish(List<List<TradeInfoRecord>> lists) {
                super.onFinish(lists);
                jiejiList = lists.get(0);
                daijiList = lists.get(1);
                rejestList = lists.get(3);
                failList = lists.get(4);
                batchDetailList = lists.get(2);
                scanSaleList = lists.get(6);
                scanVoidList = lists.get(7);
                scanRefundList = lists.get(8);
                beginToPrint();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void beginToPrint() {
        printTransData = (BasePrintTransData) getPrint();
        if (null != printTransData) {
            printTransData.open(mTradingView.getHostActivity());
            printTransData.setBatchListener(this);
            printTransData.printBatchTotalData(jiejiList, daijiList, scanSaleList, scanVoidList, scanRefundList, false);
        } else {
            logger.error("printTransData为空");
            ViewUtils.showToast(mTradingView.getContext(), "获取结算单打印方法失败！");
        }

    }

    /*获取打印类的接口*/
    public IPrintRransData getPrint() {
        return (IPrintRransData) ConfigureManager.getRedevelopAction(Keys.obj().redevelop_print_data, IPrintRransData
                .class);
    }

    private void printDetailData() {
        if (null != batchDetailList && batchDetailList.size() > 0) {
            boolean isAutoPrintDetail = BusinessConfig.getInstance().getFlag(mTradingView.getContext(),
                    BusinessConfig.Key.TOGGLE_AUTO_PRINT_DETAILS);
            if (isAutoPrintDetail)
                printTransData.printBatchDetailData(batchDetailList);
            else
                DialogFactory.showSelectDialog(mTradingView.getHostActivity(), "提示", "是否打印批结算明细数据？", new AlertDialog
                        .ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printBatchDetailData(batchDetailList);
                                break;
                            case NEGATIVE:
                                printFailData();
                                break;
                        }
                    }
                });
        } else {
            printFailData();
        }

    }

    private void printFailData() {
        if ((null != rejestList && rejestList.size() > 0) || (null != failList && failList.size() > 0)) {
            DialogFactory.showSelectDialog(mTradingView.getHostActivity(), "提示", "是否打印未上送明细数据？", new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            printTransData.printFailDetailData(rejestList, failList);
                            break;
                        case NEGATIVE:
                            otherForSettleDown();
                            break;
                    }
                }
            });
        } else {
            otherForSettleDown();
        }

    }


    @Override
    public void onPrinterFirstSuccess() {
        printDetailData();
    }

    @Override
    public void onPrinterSecondSuccess() {
        printFailData();
    }

    @Override
    public void onPrinterThreeSuccess() {
        otherForSettleDown();
    }

    @Override
    public void onPrinterFirstFail(int errorCode, String errorMsg) {
        if (errorCode == ErrorCode.PRINTER_ERROR.ERR_NO_PAPER)
            errorMsg = "打印机缺纸，请放入打印纸";
        final int eCode = errorCode;
        final String eMsg = errorMsg;
        DialogFactory.showSelectPirntDialog(mTradingView.getHostActivity(),
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                printTransData.printBatchTotalData(jiejiList, daijiList, false);
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
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
        DialogFactory.showSelectPirntDialog(mTradingView.getHostActivity(),
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                if (null != batchDetailList && batchDetailList.size() > 0) {
                                    printTransData.printBatchDetailData(batchDetailList);
                                } else {
                                    printFailData();
                                }
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
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
        DialogFactory.showSelectPirntDialog(mTradingView.getHostActivity(),
                "提示",
                errorMsg,
                new AlertDialog.ButtonClickListener() {
                    @Override
                    public void onClick(AlertDialog.ButtonType button, View v) {
                        switch (button) {
                            case POSITIVE:
                                if ((null != rejestList && rejestList.size() > 0) || (null != failList && failList
                                        .size() > 0)) {
                                    printTransData.printFailDetailData(rejestList, failList);
                                } else {
                                    otherForSettleDown();
                                }
                                break;
                            case NEGATIVE:
                                otherForSettleDown();
                                DialogFactory.hideAll();
                                break;
                        }
                    }
                });
    }

    private void otherForSettleDown() {
        //更新批次号+1
        DialogFactory.hideAll();
        updateBatchNo();
        //清空所有表数据
        deleteAllData();
        //通知其它APP完成批结算
//        try {
//            final Uri uri = Uri.parse("content://com.centerm.epos.provider.trade/record_clear");
//            mTradingView.getContext().getContentResolver().notifyChange(uri, null);
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }

        Settings.setValue(mTradingView.getHostActivity(), Settings.KEY.BATCH_SEND_STATUS, "0");
        Settings.setValue(mTradingView.getHostActivity(), Settings.KEY.BATCH_SEND_RETURN_DATA, "");//对账数据初始化
        BusinessConfig.getInstance().setFlag(mTradingView.getHostActivity(), BusinessConfig.Key
                .FLAG_TRADE_STORAGE_WARNING, false);//设置记录上限标识为false
        BusinessConfig.getInstance().setFlag(mTradingView.getHostActivity(), BusinessConfig.Key
                .KEY_IS_BATCH_BUT_NOT_OUT, true);
        BusinessConfig.getInstance().setFlag(mTradingView.getHostActivity(), BusinessConfig.Key.FLAG_ESIGN_STORAGE_WARNING,false);

        String path = Config.Path.SIGN_PATH;
        if (FileUtils.getFileSize(path) > 0) {
            FileUtils.deleteAllFiles(path);
        }
        EventBus.getDefault().post(new PrinteEvent(TradeMessage.GO_LOGIN));
        mTradePresent.gotoNextStep("1234");

//        boolean isAutoSignOut = BusinessConfig.getInstance().getFlag(mTradingView.getHostActivity(), BusinessConfig.Key
//                .FLAG_AUTO_SIGN_OUT);
//        if (isAutoSignOut) {
//            autoSignOut();
//        } else {
//            logger.debug("没有自动签退");
//            mTradingView.popToast(R.string.tip_batch_over_please_sign_out);
//            mTradePresent.jumpToMain();
//        }
    }

    /**
     * 更新批次号+1
     */
    private void updateBatchNo() {
        String batchNo = BusinessConfig.getInstance().getBatchNo(mTradingView.getHostActivity());
        int batchNum = Integer.parseInt(batchNo);
        String newBatch = DataHelper.formatToXLen(++batchNum, 6);
        BusinessConfig.getInstance().setValue(mTradingView.getHostActivity(), BusinessConfig.Key.KEY_LAST_BATCH_NO,
                batchNo);
        BusinessConfig.getInstance().setBatchNo(mTradingView.getHostActivity(), newBatch);
    }

    /**
     * 批结算完成后，删除所有数据
     */
    private void deleteAllData() {
        boolean isDel;
        ITradeRecordInformation tradeRecordInformation = (ITradeRecordInformation) ConfigureManager
                .getSubPrjClassInstance(new TradeRecordInfoImpl());
        isDel = tradeRecordInformation.clearRecord();
        if (isDel) {
            logger.debug("该批次数据清空完成！");
        } else {
            logger.debug("该批次数据清空失败！");
        }

        //删除签名文件文件
        File fileVoucher = new File(Config.Path.SIGN_PATH);
        if(fileVoucher.isDirectory()&&fileVoucher.exists()){
            File[] subFile = fileVoucher.listFiles();
            for(int i=0;i<subFile.length;i++){
                File deleteFile = subFile[i];
                if(deleteFile.exists()){
                    deleteFile.delete();
                }
            }
        }
    }

    /**
     * 发起自动签退
     */
    private void autoSignOut() {
        logger.debug("发起自动签退");
        mTradePresent.getTransData().clear();
        new AsyncAutoSignOut(mTradingView.getHostActivity(), mTradePresent.getTransData()) {
            @Override
            public void onStart() {
                super.onStart();
                if (mTradingView instanceof ITradingView)
                    ((ITradingView) mTradingView).updateHint(R.string.tip_auto_sign_out);
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                if ("00".equals(strings[0])) {
                    BusinessConfig.getInstance().setFlag(mTradingView.getHostActivity(), BusinessConfig.Key
                            .FLAG_SIGN_IN, false);
                    BusinessConfig.getInstance().setFlag(context, BusinessConfig.Key.KEY_IS_BATCH_BUT_NOT_OUT, false);
                    BusinessConfig.getInstance().setNumber(context, BusinessConfig.Key.KEY_POS_SERIAL, 1);
                    if ("2".equals(Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0")))
                        Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
                    mTradingView.popToast(R.string.tip_sign_out);
                    logger.debug("签退成功");
                    mTradePresent.jumpToLogin();
                } else {
                    mTradePresent.jumpToMain();
                    logger.error("签退请求失败");
                    ViewUtils.showToast(mTradingView.getHostActivity(), R.string.tip_sign_out_fail);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
