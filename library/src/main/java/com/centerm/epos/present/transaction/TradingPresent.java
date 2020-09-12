package com.centerm.epos.present.transaction;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pboc.EmvTag;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumOnlineResult;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.cpay.midsdk.dev.define.pinpad.EnumWorkKeyType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.ScriptInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradePrintData;
import com.centerm.epos.bean.TradeRecordForUpload;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.channels.helper.BaseRespHelper;
import com.centerm.epos.channels.helper.CommonRespHelper;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.EmvTagKey;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.CommonManager;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.model.ITradeModel;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.model.TradeModelImpl;
import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.msg.MessageFactoryPlus;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.redevelop.BaseIsUpdateOriginInfo;
import com.centerm.epos.redevelop.ICommonManager;
import com.centerm.epos.redevelop.IIsNeedReverse;
import com.centerm.epos.redevelop.IIsUpdateOriginInfo;
import com.centerm.epos.redevelop.ISaveExtInfo;
import com.centerm.epos.redevelop.IsNeedReverse;
import com.centerm.epos.redevelop.SaveExtInfoImpl;
import com.centerm.epos.task.AsyncMagLoadConfirmTask;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.ReverseReasonCode;
import com.centerm.epos.transcation.pos.constant.RuntimeExceptionCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TradeTempInfoTag;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.manager.BatchSendComplete;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;
import com.centerm.epos.transcation.pos.manager.QueryScanResultTrade;
import com.centerm.epos.transcation.pos.manager.ReverseTrade;
import com.centerm.epos.transcation.pos.manager.RunTimeChecker;
import com.centerm.epos.transcation.pos.manager.SettlementTrade;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.NewEmvTag;
import com.centerm.epos.utils.TlvUtil;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.TradeItem;
import com.centerm.epos.xml.keys.Keys;
import com.centerm.smartpos.util.HexUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH;
import static com.centerm.epos.common.TransCode.CAUSE_REVERSE_SETS;
import static com.centerm.epos.common.TransCode.NEED_INSERT_TABLE_SETS;
import static com.centerm.epos.common.TransCode.NOTIFY_TRADE_SETS;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.SIGN_OUT;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_CARD_CONFIRM_RESULT;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_PIN;
import static com.centerm.epos.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_IC_CONTINUE_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.epos.common.TransDataKey.iso_f39;
import static com.centerm.epos.common.TransDataKey.iso_f55;

/**
 * Created by yuhc on 2017/2/22.
 * 数据交易业务处理，包含报文的组织，连接服务器、发送接收数据，报文解析
 */

public class TradingPresent extends BaseTradePresent {
    protected ITradingView mTradingView;
    protected ITransactionMessage factory;
    protected BaseRespHelper respHelper;
    protected CommonDao<ReverseInfo> reverseDao;
    protected CommonDao<TradeInfoRecord> tradeDao;
    protected CommonDao<TradePrintData> printDataCommonDao;
    protected CommonDao<ScriptInfo> scriptInfoScriptDao = null;
    protected String msgTag;

    private ITradeModel mTradeModel;

    private boolean priorResponseResult;
    protected String transCode = mTradeInformation.getTransCode();

    public TradingPresent(TradingFragment mTradeView) {
        super(mTradeView);
        mTradingView = mTradeView;
        mTradeModel = TradeModelImpl.getInstance(this);
        logger.warn("TradingPresent register ");
        EventBus.getDefault().register(this);
    }

    @Override
    public void release() {
        logger.warn("TradingPresent unregister ");
        EventBus.getDefault().unregister(this);
        super.release();
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        factory = MessageFactoryPlus.createMessageByType(MessageFactoryPlus.MESSAGE_ISO8583_POS, transDatas);
        respHelper = new CommonRespHelper(mTradeView.getHostActivity(), transCode);
        reverseDao = new CommonDao<>(ReverseInfo.class, dbHelper);
        tradeDao = new CommonDao<>(TradeInfoRecord.class, dbHelper);
        printDataCommonDao = new CommonDao<>(TradePrintData.class, dbHelper);
        priorResponseResult = ConfigureManager.getInstance(getContext()).isOptionFuncEnable(getContext(), Keys.obj()
                .prior_response_result);

        scriptInfoScriptDao  = new CommonDao<>(ScriptInfo.class, dbHelper);
    }

    @Override
    public boolean isOpenDataBase() {
        return true;
    }

    @Override
    public void beginTransaction() {
        String transCode = mTradeInformation.getTransCode();

        if (preProcess(transCode)) {
            return;
        }
        Map<String, TradeItem> tradeItemMap = ConfigureManager.getInstance(EposApplication.getAppContext()).getTradeItemMap();
            XLogUtil.w("beginTransaction tradeItemMap:",transCode+" "+tradeItemMap);
        TradeItem managerTrade = tradeItemMap.get(transCode);
            XLogUtil.w("beginTransaction managerTrade:"," "+managerTrade);
        if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
            executeManagerTrade(managerTrade);
        } else {
            if (preTradeBeforeOnline(tradeItemMap)) return;
            beginOnline();
        }
    }

    /**
     * 联机交易前需要执行的业务
     *
     * @param tradeItemMap 管理业务
     * @return true 执行 false 不执行
     */
    private boolean preTradeBeforeOnline(Map<String, TradeItem> tradeItemMap) {
        TradeItem managerTrade;

        if (ConfigureManager.getInstance(getContext()).isOptionFuncEnable(getContext(), com.centerm.epos.xml.keys.Keys.obj().script_update)) {
            managerTrade = tradeItemMap.get(TransCode.UPLOAD_SCRIPT_RESULT);
            if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
                //检测到当前有IC卡脚本结果需要上送，则先进行联机脚本上送，再发起交易
                executeManagerTrade(managerTrade);
                return true;
            }
        }

        managerTrade = tradeItemMap.get(TransCode.REVERSE);
        XLogUtil.w("preTradeBeforeOnline","preTradeBeforeOnline REVERSE "+managerTrade);
//        if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
//            //如果当前有冲正信息，需要先进行冲正后才开始进行交易
//            executeManagerTrade(managerTrade);
//            return true;
//        }

        //批结算前的交易上送
        if (TransCode.SETTLEMENT.equals(transCode)) {
            managerTrade = tradeItemMap.get(TransCode.UPLOAD_TRADE_BEFORE_SETTLEMENT);
            if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
                //如果当前有冲正信息，需要先进行冲正后才开始进行交易
                executeManagerTrade(managerTrade);
                return true;
            }
        }
        return false;
    }

    private boolean preProcess(String transCode) {
        if (TextUtils.isEmpty(transCode))
            return false;
        if (TransCode.SIGN_IN.equals(transCode))
            return false;
        if (!ProcessRequestManager.isExistProcessRequest())
            return false;
        if (transCode.equals(ProcessRequestManager.getRequestTradeCode()))
            return false;
        ProcessRequestManager.activeRequestTrade(mTradeView, this);
        return true;
    }

    private void executeManagerTrade(TradeItem managerTrade) {
        try {
            Class clz = Class.forName(managerTrade.getTradeClz());
            ManageTransaction manageTransaction = (ManageTransaction) clz.newInstance();
            manageTransaction.execute(mTradeView, this);
        } catch (Exception e) {
            e.printStackTrace();
            putResponseCode("55", e.getMessage());
            gotoNextStep("99");
        }
    }

    private boolean checkBeforExecute(String checkerClz) {
        if (TextUtils.isEmpty(checkerClz))
            return true;
        try {
            Class clz = Class.forName(checkerClz);
            RunTimeChecker checker = (RunTimeChecker) clz.newInstance();
            return checker.check(mTradeView, this);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Context getContext() {
        return mTradeView.getHostActivity();
    }
    /*
    *电子现金圈存类交易
    * */
    private boolean bIsEcLoadTransType(String transCode )
    {
        return ( transCode.equals(TransCode.EC_LOAD_CASH)
                || transCode.equals(TransCode.EC_LOAD_INNER)
                || transCode.equals(TransCode.EC_LOAD_OUTER)
                || transCode.equals(TransCode.EC_VOID_CASH_LOAD));
    }
    /*
    * ic卡交易是否联机还是继续等待
    * */
    private boolean bIsIcContinueOnline(){
        String s = (String)transDatas.get(KEY_IC_CONTINUE_ONLINE);

        return "1".equals(s);

    }
    /**
     * 除了特殊联机交易类型外，其它交易都调用该方法，开启联机交易的一系列步骤。
     * 包含对内核时间的处理和应答，组报文发起网络请求等等。
     * {@link TransCode#MAG_ACCOUNT_LOAD}磁条卡账户充值，转出卡为IC卡时只执行到脱机数据认证，并不参与PBOC完成流程。<br/>
     */
    private void beginOnline() {
        if ( (isICInsertTrade() || bIsEcLoadTransType(transCode)) && (!TransCode.MAG_ACCOUNT_LOAD.equals(transCode)) ) {
            IPbocService pbocService = mTradeInformation.getPbocService();
            //联机前完成内核操作，在内核要求联机时才进行真实联机交易
            if ("1".equals(transDatas.get(FLAG_IMPORT_AMOUNT))) {
                pbocService.importAmount((String) transDatas.get(TradeInformationTag.TRANS_MONEY));
                transDatas.remove(FLAG_IMPORT_AMOUNT);
            } else if ("1".equals(transDatas.get(FLAG_IMPORT_CARD_CONFIRM_RESULT))) {
                pbocService.importResult(EnumPbocResultType.CARD_INFO_CONFIRM, true);
                transDatas.remove(FLAG_IMPORT_CARD_CONFIRM_RESULT);
            } else if ("1".equals(transDatas.get(FLAG_IMPORT_PIN))) {
                pbocService.importPIN(false, null);
                transDatas.remove(FLAG_IMPORT_PIN);
            } else if ("1".equals(transDatas.get(FLAG_REQUEST_ONLINE))) {
                transDatas.remove(FLAG_REQUEST_ONLINE);
                /*电子现金 非接触式 现金充值
                *接触式指定账户圈存
                *接触式非指定账户圈存
                * 非接触式电子现金充值撤销
                * */
                logger.warn("beginOnline FLAG_REQUEST_ONLINE，FLAG_REQUEST_ONLINE");
                if(!( transCode.equals(TransCode.EC_VOID_CASH_LOAD) && isICInsertTrade())) {
                    sendData();
                }
            } else if( bIsEcLoadTransType(transCode) ){
                /*电子现金充值交易走该流程
                * 接触式纯电子现金充值*/
                logger.warn("beginOnline 电子现金圈存交易，PBOC ONLINE");
                /*接触式电子现金撤销冲正后，重新启动交易，走该流程*/
                if( transCode.equals(TransCode.EC_VOID_CASH_LOAD) && isICInsertTrade() && bIsIcContinueOnline()) {
                    transDatas.put(KEY_IC_CONTINUE_ONLINE,null);
                    sendData();
                }

            } else {
                logger.warn("内核流程异常，交易终止，不进行联机交易");
                if(mTradeView!=null&&mTradeView.getHostActivity()!=null) {
                    mTradeView.getHostActivity().jumpToResultActivity(StatusCode.TRADING_REFUSED);
                }
                pbocService.abortProcess();
            }
        } else {
            sendData();
        }
    }
    /*
    * IC卡的冲正包需要重新打包
    * @params transCode 交易类型
    * @params iso55 iso55域
    * @params bIsOnline 是否联机
    * */
    private String repackF55Reverse(String transCode,String iso55,boolean bIsOnline){
        String iso_f55 = null;
        XLogUtil.w("repackF55Reverse iso55:",iso55+" transCode:"+transCode);
        switch (transCode){
            case TransCode.EC_VOID_CASH_LOAD:
                /*
                * 95（tag）终端验证结果b40 BINARY C
                该交易仅由终端发起，且该交易虽然
                被发卡方批准但被卡片拒绝，则本域
                出现。
                9F1E（tag）
                接口设备序列号an8 ASCII C
                如果终端标识不能隐含确定接口设备
                序列号，则出现；同原交易
                9F10（tag）
                发卡行应用数据b…256 VAR BINARY C
                该交易仅由终端发起，且该交易虽然
                被发卡方批准但被卡片拒绝，则本域
                出现
                9F36（tag）
                应用交易计数器b16 BINARY C C
                C：在交易已承兑、卡片拒绝的情况下，
                由终端发起的冲正需在请求中出现
                DF31（tag）
                发卡行脚本结果b…168 VAR BINARY C
                当原始交易的应答报文中出现发卡行
                脚本时，本域出现*/
                if( !bIsOnline ){
                    Map<String, String> mapIC = TlvUtil.tlvToMap(HexUtil.hexStringToByte(iso55));
                    XLogUtil.w("repackF55Reverse iso55:","mapIC:"+mapIC);
                    Map<String, String> iso55Map = new HashMap<String, String>();
                    iso55Map.put("95",mapIC.get("95"));
                    iso55Map.put("9F1E",mapIC.get("9F1E"));
                    iso55Map.put("9F10",mapIC.get("9F10"));
                    iso55Map.put("9F36",mapIC.get("9F36"));
                    iso_f55 = TlvUtil.mapToTlv(iso55Map);
                }
                else{
                    iso_f55 = iso55;
                }
                break;
        }
        return iso_f55;
    }

    /**
     * 开始发送数据，单个请求。
     */
    private void sendData() {

        new Thread() {
            @Override
            public void run() {
                 /*BUGID:0002222 隔日冲正问题
                *交易过程未导入 标签TransDataKey.KEY_TRANS_TIME的值
                * 数据包发送前保存一个交易时间，当收到服务器应答包后同步成系统时间，
                * 防止连接过程中跨日，实际交易时间却是当天（暂时不处理该过程）
                * @author zhouzhihua 2017.11.10
                **/
                String transDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                String transDate2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                transDatas.put(TransDataKey.KEY_TRANS_TIME, transDate);
                transDatas.put(JsonKeyGT.onlineFlag, JsonKeyGT.onlineFlag);

                final Object msgPacket = factory.packMessage(transCode, transDatas);
                if (msgPacket == null) {
                    putResponseCode(StatusCode.PACKAGE_ERROR);
                    logger.warn("请求报文为空，退出");
//            if (TransCode.REVERSE_SETS.contains(transCode)) {
//                activityStack.pop();
//            } else {
//                jumpToNext("99");
//            }
                    gotoNextStep("99");
                    return;
                }

                String iso11 = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
                if (msgPacket instanceof byte[]) {
                    msgTag = getMsgTagByTranTag(transDatas, transCode);
                    if (CAUSE_REVERSE_SETS.contains(msgTag)) {
                        //保存交易信息，用于后续冲正使用
                        if (factory instanceof PosISO8583Message) {
                            Map<String, String> requestData = ((PosISO8583Message) factory).getRequestDataForIso8583();
                            if (requestData != null) {
                                /*BUGID:0002222 隔日冲正问题
                                *交易过程未导入 标签TransDataKey.KEY_TRANS_TIME的值
                                * 数据包发送前保存一个交易时间，当收到服务器应答包后同步成系统时间
                                * 防止连接过程中跨日，实际交易时间却是当天（暂时不处理该过程）
                                * @author zhouzhihua 2017.11.10
                                **/
                                requestData.put(TransDataKey.KEY_TRANS_TIME, transDate);

                                ReverseInfo record = new ReverseInfo(msgTag, requestData);

                                String iso55Reverse = repackF55Reverse(transCode,requestData.get(iso_f55),false);
                                XLogUtil.w("repackF55Reverse iso55Reverse:",iso55Reverse);
                                if( null != iso55Reverse ){
                                    record.setIso_f55(iso55Reverse);
                                }
                                //保存绿城缴费备注信息及外部订单号
                                if(SALE.equals(transCode)&&transDatas.get(JsonKeyGT.additionalData)!=null){
                                    record.setIso_f64((String) transDatas.get(JsonKeyGT.additionalData));
                                }
                                //保存外部订单号
                                if(SALE.equals(transCode)&&transDatas.get(JsonKeyGT.out_order_no)!=null){
                                    record.setIso_f61((String) transDatas.get(JsonKeyGT.out_order_no));
                                }
                                //代付标志
                                if(SALE.equals(transCode)&&transDatas.get(JsonKeyGT.isPay)!=null){
                                    int isPay = (int) transDatas.get(JsonKeyGT.isPay);
                                    record.setIso_f62(isPay+"");
                                }
                                record.setTransTime(transDate2);
                                record.setIso_f39(ReverseReasonCode.TIME_OUT);    //默认原因为接收数据超时
                                boolean r = reverseDao.save(record);
                                logger.info(iso11 + "==>" + msgTag + "==>插入冲正表中==>" + r);
                            }
                        }
                    }

                    try {
                        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
                        sleep(200);
                        byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
                        if (receivedData == null) {
                            logger.error("^_^ 接收数据失败！ ^_^");
                            if (/*BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key
                                    .TOGGLE_REVERSE_NOW) && */doReverse(ReverseReasonCode.TIME_OUT, iso11)) {
                                return;
                            }
                            respHelper.onRespFailed(this, "99", "接收数据失败！");
                            gotoNextStep("99");
                        } else {
                            Map<String, Object> mapData = factory.unPackMessage(transCode, receivedData);

                            if (mapData == null || mapData.size() == 0) {
                                //无数据，说明是MAC校验错误了，所以不进行数据解析
                                if (/*BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key
                                        .TOGGLE_REVERSE_NOW) &&*/ doReverse(ReverseReasonCode.MAC_CHECK_FAILED,
                                        iso11)) {
                                    return;
                                }
                                putResponseCode(StatusCode.UNPACKAGE_ERROR);
                                gotoNextStep("99");
                            } else {
                                mTradeInformation.setRespDataMap(mapData);
                                if (NOTIFY_TRADE_SETS.contains(msgTag)) {
                                    mapData.put(TradeTempInfoTag.REQUEST_MSG, HexUtil.bytesToHexString((byte[])
                                            msgPacket));
                                }

                                if(mapData.get(TradeInformationTag.TRACE_NUMBER)==null){
                                    mapData.put(TradeInformationTag.TRACE_NUMBER, iso11);
                                }
                                respHelper.onRespSuccess(TradingPresent.this, mapData);
                                /*@author zhouzhihua 下个界面按返回可能导致程序奔溃，结果界面返回键已经进行延时处理
                                * respHelper.onRespSuccess 实现了界面的跳转，如果在respHelper.onRespSuccess
                                * 之后再执行操作，会导致一些异常。该函数之后不能执行耗时较长的操作。
                                * */
                                if (NOTIFY_TRADE_SETS.contains(msgTag)) {
                                    mapData.remove(TradeTempInfoTag.REQUEST_MSG);
                                }
                            }
                        }
                    } catch (Exception e) {
                        transDatas.put(JsonKeyGT.netErrorFlag,JsonKeyGT.netErrorFlag);
                        if (e instanceof BaseRuntimeException) {
                            BaseRuntimeException baseRuntimeException = (BaseRuntimeException) e;
                            if (baseRuntimeException.getErrCode() == RuntimeExceptionCode.CONNECT_SERVER_FAILED ||
                                    baseRuntimeException.getErrCode() == RuntimeExceptionCode.SEND_DATA_FAILED) {
                                //连接中心和发送数据失败，删除冲正信息，因为数据都没发出去，交易肯定是失败的
                                ReverseInfo reverseInfo = reverseDao.queryForId(iso11);
                                if (reverseInfo != null)
                                    reverseDao.delete(reverseInfo);
                            }
                        }
                        logger.error("^_^ 数据交换失败：" + e.getMessage() + " ^_^");
                        if (/*BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.TOGGLE_REVERSE_NOW)
                                && */doReverse(ReverseReasonCode.TIME_OUT, iso11)) {
                            //接收数据失败后，立即冲正
                            return;
                        }
                        if (e instanceof BaseRuntimeException) {
                            BaseRuntimeException baseRuntimeException = (BaseRuntimeException) e;
                            putResponseCode(baseRuntimeException.getErrCode() + "", baseRuntimeException.getMessage());
                        } else
                            putResponseCode(StatusCode.DATA_EXCHANGE_ERROR);
                        gotoNextStep("99");
                    }
                } else {
                    logger.warn("报文格式非字节数组");
                }
            }
        }.start();
    }
    /*BUGID:0002222 隔日冲正问题
    *交易过程未导入 标签TransDataKey.KEY_TRANS_TIME的值
    * 数据包发送前保存一个交易时间，当收到服务器应答包后同步成系统时间
    * 防止连接过程中跨日，实际交易时间却是当天（暂时不处理该过程）
    * 修复 未立即冲正，冲正原因未更新
    * @author zhouzhihua 2017.11.10
    **/

    protected boolean doReverse(String resend, String iso11) {
        boolean bisTrue = false;

        ReverseInfo reverseInfo = reverseDao.queryForId(iso11);
        if (reverseInfo != null) {
            reverseInfo.setIso_f39(resend);
            reverseDao.update(reverseInfo);
            bisTrue = true;
            try {
                if(tradeDao.queryForId(iso11)!=null){
                    logger.error("交易已成功，无需冲正，删除冲正");
                    reverseDao.deleteById(iso11);
                    return false;
                }
            }catch (Exception e){
                logger.error("冲正前检测该笔交易是否已经完成出现异常");
            }
            if (BusinessConfig.getInstance().getFlag(getContext(), BusinessConfig.Key.TOGGLE_REVERSE_NOW)) {
                //接收数据失败后，立即冲正
                new ReverseTrade().setIsAfterTrade(true).execute(mTradeView, TradingPresent.this);
            } else {
                bisTrue = false;
            }
        }
        return bisTrue;
    }


    /**
     * 交易失败处理
     * 如果新增加错误码引发冲正需要做相应的修改,修改 {@link com.centerm.epos.redevelop.IsNeedReverse} 方法 增加错误类型处理 <br/>
     * {@link com.centerm.epos.common.StatusCode} 状态码添加 <br/>
     * @param iso11  <br/>
     * @param code  <br/>
     * @param msg  <br/>
     * author zhouzhihua 冲正修改
     * */
    public void onTradeFailed(String iso11, String code, String msg) {
        if (iso11 == null) {
            iso11 = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
        }
        putResponseCode(code, msg);
        boolean dbResult;
        //********************更新本地流水表记录***********************//
//        TradeInfoRecord initialTrade = tradeDao.queryForId(iso11);
//        if (initialTrade != null) {
//            //保存响应码，该响应码可能是自定义也可能是后台返回的
//            initialTrade.setIso_f39(code);
//        } else {
//            logger.warn(iso11 + "==>交易类型==>" + transCode + "==>无法在数据库中查询到该数据模型");
//        }
//        logger.warn(iso11 + "==>交易失败==>" + code + "==>" + msg);
//        if (NEED_INSERT_TABLE_SETS.contains(transCode)) {
//            if (initialTrade != null) {
//                dbResult = tradeDao.update(initialTrade);
//                logger.warn(iso11 + "==>交易类型==>" + transCode + "==>交易失败==>更新交易流水表==>" + dbResult);
//            }
//        }
        /*圈存类交易，只要内核不终止，有脚本结果都需要保存
        * 凡是有IC脚本的 交易拒绝需要上送拒绝的脚本
        * @author zhouzhihua modified
        * */
        if( isImportOnlineRespTrade() && StatusCode.TRADING_REFUSED.getStatusCode().equals(code) ){
            saveScriptInfo(tempMap , 2 );
        }

        if (TransCode.CAUSE_REVERSE_SETS.contains(msgTag)) {
            //更新冲正表记录
            IIsNeedReverse iIsNeedReverse = (IIsNeedReverse) ConfigureManager.getSubPrjClassInstance(new
                    IsNeedReverse());
            ReverseInfo reverseInfo = reverseDao.queryForId(iso11);
            if (reverseInfo == null) {
                logger.warn(iso11 + "==>无法查找到对应的冲正表信息");
                //return;
            }
            if (StatusCode.SOCKET_TIMEOUT.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因98");
                reverseInfo.setIso_f39("98");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else if (StatusCode.MAC_INVALID.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因A0");
                reverseInfo.setIso_f39("A0");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else if (StatusCode.UNKNOWN_REASON.getStatusCode().equals(code)
                    || StatusCode.TRADING_TERMINATES.getStatusCode().equals(code)
                    || StatusCode.TRADING_REFUSED.getStatusCode().equals(code)
                    || StatusCode.EMV_KERNEL_EXCEPTION.getStatusCode().equals(code)
                    || StatusCode.IC_PROCESS_ERROR.getStatusCode().equals(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因06");
                /*
                * 电子现金现金充值撤销，后台批准卡片拒绝更新55域
                * */
                if( StatusCode.TRADING_REFUSED.getStatusCode().equals(code) && transCode.equals(TransCode.EC_VOID_CASH_LOAD) ){
                    String iso55 = mTradeInformation.getPbocService().readTlvKernelData(NewEmvTag.getF55TagsRevesal());
                    repackF55Reverse(transCode,iso55,true);
                    reverseInfo.setIso_f55(iso55);
                    logger.warn("TRADING_REFUSED:"+iso55);
                }
                reverseInfo.setIso_f39("06");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else if (iIsNeedReverse.getFlag(code)) {
                logger.warn(iso11 + "==>交易状态未知==>下一次交易前将发起冲正==>冲正原因98");
                reverseInfo.setIso_f39("98");
                dbResult = reverseDao.update(reverseInfo);
                logger.warn(iso11 + "==>更新冲正表39域信息==>" + dbResult);
            } else {
                //交易状态确定为失败的情况，需要删除冲正表信息
                dbResult = reverseDao.deleteById(iso11);
                logger.warn(iso11 + "==>交易失败==>删除冲正流水表记录==>" + dbResult + "==>交易类型：" + transCode);
            }
            gotoNextStep("99");
        } else if ( transCode.equals(TransCode.REFUND)
                    || transCode.equals(TransCode.EC_LOAD_CASH)
                    || transCode.equals(TransCode.EC_LOAD_INNER)
                    || transCode.equals(TransCode.EC_LOAD_OUTER)
                    || transCode.equals(TransCode.MAG_CASH_LOAD)
                    || transCode.equals(TransCode.MAG_ACCOUNT_LOAD)) {

            gotoNextStep("99");
        } else if (transCode.equals(TransCode.TRANS_IC_DETAIL) || transCode.equals(TransCode.TRANS_CARD_DETAIL) ||
                transCode.equals(TransCode.TRANS_FEFUND_DETAIL)) {
            mTradeView.showSelectDialog(R.string.tip_dialog_title, R.string.tip_send_data_fail,
                    new AlertDialog.ButtonClickListener() {
                        @Override
                        public void onClick(AlertDialog.ButtonType button, View v) {
                            switch (button) {
                                case POSITIVE:
                                    transCode = TransCode.SETTLEMENT;
                                    sendData();
                                    break;
                                case NEGATIVE:
                                    gotoPreStep();
                                    break;
                            }
                        }
                    });
        } else if (transCode.equals(TransCode.SETTLEMENT_DONE)) {
            mTradeView.showSelectDialog(R.string.tip_dialog_title, R.string.tip_batch_down_fail, new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            new BatchSendComplete().execute(mTradeView, TradingPresent.this);
                            break;
                        case NEGATIVE:
                            gotoPreStep();
                            break;
                    }
                }
            });
        } else if (transCode.equals(TransCode.SETTLEMENT)) {
            mTradeView.showSelectDialog(R.string.tip_dialog_title, R.string.tip_batch_fail, new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    switch (button) {
                        case POSITIVE:
                            transCode = TransCode.SETTLEMENT;
                            sendData();
                            break;
                        case NEGATIVE:
                            gotoPreStep();
                            break;
                    }
                }
            });
        } else if (transCode.equals(SIGN_OUT)) {
            mTradeView.showMessageDialog(R.string.tip_dialog_title, R.string.tip_sign_out_fail, new AlertDialog
                    .ButtonClickListener() {
                @Override
                public void onClick(AlertDialog.ButtonType button, View v) {
                    gotoPreStep();
                }
            });
        } else {
            gotoNextStep("99");
        }
        //jumpToNext();
    }

    /**
     * 交易失败
     *
     * @param status 自定义状态码（包含渠道自定义的）
     */
    public void onTradeFailed(String iso11, StatusCode status) {
        onTradeFailed(iso11, status.getStatusCode(), mTradeView.getStringFromResource(status.getMsgId()));
    }

    /**
     * 交易失败
     *
     * @param iso 规范定义状态码
     */
    public void onTradeFailed(String iso11, ISORespCode iso) {
        onTradeFailed(iso11, iso.getCode(), mTradeView.getStringFromResource(iso.getResId()));
    }

    /**
     * 发散主密钥
     *
     * @param value 密钥值
     * @param checkValue 校验值
     */
    public void loadTMK(String value, String checkValue) {
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        boolean result = false;
        if (pinPadDev != null) {
            result = pinPadDev.loadTMK2(value, checkValue);
        }
        if (result) {
            tempMap.put(iso_f39, "00");//下载主密钥并发散成功
            Settings.setTmkExist(EposApplication.getAppContext());//设置主密钥存在的标识
            BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_SIGN_IN,
                    false);//强制重新签到
        } else {
            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
            putResponseCode(code);
        }
        gotoNextStep();
    }

    /**
     * 发散工作密钥
     *
     * @param pik pik
     * @param pikCheck 校验值
     * @param mak mak
     * @param makCheck 校验值
     * @param tdk mak
     * @param tdkCheck 校验值
     */
    public void loadWorkKey(String pik, String pikCheck, String mak, String makCheck, String tdk, String tdkCheck) {
        boolean r1, r2, r3;
        boolean result = false;
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            r1 = pinPadDev.loadWorkKey(EnumWorkKeyType.PIK, pik, pikCheck);
            r2 = pinPadDev.loadWorkKey(EnumWorkKeyType.MAK, mak, makCheck);
            if (BusinessConfig.isTrackEncrypt()) {
                r3 = pinPadDev.loadWorkKey(EnumWorkKeyType.TDK, tdk, tdkCheck);
                result = r1 && r2 && r3;
            } else {
                result = r1 && r2;
            }
        }
        if (result) {
            BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_SIGN_IN, true);
            tempMap.put(iso_f39, "00");//下载工作密钥并发散成功
            mTradeView.popToast(R.string.tip_sign_in_success);
            gotoNextStep("2");
        } else {
            StatusCode code = StatusCode.KEY_VERIFY_FAILED;
            putResponseCode(code);
            gotoNextStep();
        }
    }
    /**
     * 电子现金圈存交易需要导入联机数据
     */
    public boolean bIsEcLoadTrans()
    {
        String mTransCode = mTradeInformation.getTransCode();
        XLogUtil.w(getClass().getSimpleName(),"bIsEcLoadTrans: mTransCode"+mTransCode);
        return ( TransCode.EC_LOAD_CASH.equals(mTransCode)
                 || TransCode.EC_LOAD_INNER.equals(mTransCode)
                 || TransCode.EC_LOAD_OUTER.equals(mTransCode)
                 || TransCode.EC_VOID_CASH_LOAD.equals(mTransCode) );
    }

    /**
     * 判断当前交易是否需要导入联机响应数据
     *
     * @return 是返回true，否则返回false
     */
    protected boolean isImportOnlineRespTrade() {
        return ( bIsEcLoadTrans()
                 || ( isICInsertTrade()
                 && ( TransCode.UNION_INTEGRAL_BALANCE.equals(transCode)
                      || TransCode.BALANCE.equals(transCode)
                      || TransCode.SALE.equals(transCode)
                      || TransCode.AUTH.equals(transCode)
                      || TransCode.ISS_INTEGRAL_SALE.equals(transCode)
                      || TransCode.UNION_INTEGRAL_SALE.equals(transCode)) ) );
    }

    protected TradeInfoRecord curTradeInfo;//当前交易信息
    protected boolean hasImportOnlineResp = false;

    protected Map<String, String> convertObject2String(Map<String, Object> objMap) {
        if (objMap == null || objMap.size() == 0)
            return null;
        Map<String, String> strMap = new HashMap<>();
        Iterator<Map.Entry<String, Object>> iterator = objMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() instanceof String)
                strMap.put(entry.getKey(), (String) entry.getValue());
        }
        return strMap;
    }

    public String getScriptInfo55(){

        String is055 = mTradeInformation.getPbocService().readTlvKernelData(NewEmvTag.getF55TagsScript());

        Map<String, String> iccMap = TlvUtil.tlvToMap(is055);
        XLogUtil.d("saveScriptInfo","iccMap:"+iccMap);
        if( !(iccMap.containsKey("95") && iccMap.containsKey("9F10")
            && iccMap.containsKey("9F26") && iccMap.containsKey("9F36")
            && iccMap.containsKey("82") && iccMap.containsKey("DF31")
            && iccMap.containsKey("9F1A") && iccMap.containsKey("9A") )){
            is055 = null;
        }
        return is055;
    }

    public boolean saveScriptInfo(Map<String, String> dataMap ,int scriptDoResult){

        IPbocService pbocService = mTradeInformation.getPbocService();
        if(pbocService == null ){ return false; }

        String df31 = pbocService.readTlvKernelData(EmvTag.combine(new int[]{0xdf,0x31}));

        return saveScriptInfo( df31, dataMap , scriptDoResult);
    }
    private String getTransAmount(String amt){
        if (TextUtils.isEmpty(amt)) {
            return null;
        }
        String amountFormat;    //对小数点进行处理

        if (amt.indexOf('.') == -1) {
            long moneyInt = Long.parseLong(amt, 10);
            amountFormat = String.format(Locale.CHINA,"%010d00", moneyInt);
        } else {
            String moneyParts[] = amt.split("\\.");
            if (moneyParts.length > 3) {
                return null;
            }
            long moneyIntegralPart = Long.parseLong(moneyParts[0], 10);//整数部分处理
            String fractionalPartStr = moneyParts[1];//小数部分处理
            if (fractionalPartStr.length() > 2)
                fractionalPartStr = fractionalPartStr.substring(0,2);
            Long moneyFractionalPart = Long.parseLong(fractionalPartStr,10);
            amountFormat = String.format(Locale.CHINA,"%010d%02d",moneyIntegralPart,moneyFractionalPart);//格式化输出数据：10位整数+2位小数
        }
        return amountFormat;
    }
    private boolean saveScriptInfo( String scriptResult, Map<String, String> dataMap ,int scriptDoResult){
        if( scriptResult == null  ){
            XLogUtil.d("saveScriptInfo","not ScriptInfo");
            return false;
        }
        /*9F33 95 9F37 9F1E 9F10 9F26 9F36 82 DF31 9F1A 9A*/
        String is055 = getScriptInfo55();
        if( null == is055 ){
            return false;
        }
        BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT, true);

        XLogUtil.d("saveScriptInfo","dataMap : "+ dataMap);
        Map<String, String> scriptMap = new HashMap<>();
        /*
        * 非指定账户圈存，脚本上送，填写转入卡卡号
        * */
        if( TransCode.EC_LOAD_OUTER.equals(transDatas.get(TradeInformationTag.TRANSACTION_TYPE)) ){
            scriptMap.put(TransDataKey.iso_f2,(String)transDatas.get(TradeInformationTag.TRANSFER_INTO_CARD));
        }
        else {
            scriptMap.put(TransDataKey.iso_f2, dataMap.get(TradeInformationTag.BANK_CARD_NUM));
        }
        scriptMap.put(TransDataKey.iso_f3,dataMap.get(TradeInformationTag.TRANSACTION_PROCESS_CODE));
        String amtS = this.getTransAmount(dataMap.get(TradeInformationTag.TRANS_MONEY));
        if( amtS != null ) {
            scriptMap.put(TransDataKey.iso_f4, amtS);
        }
        //scriptMap.put(TransDataKey.iso_f11,BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext()));
        scriptMap.put(TransDataKey.iso_f22,(String)transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE));
        scriptMap.put(TransDataKey.iso_f23,dataMap.get(TradeInformationTag.CARD_SEQUENCE_NUMBER));
        scriptMap.put(TransDataKey.iso_f25,dataMap.get(TradeInformationTag.SERVICE_CONDITION_MODE));

        scriptMap.put(TransDataKey.iso_f32,dataMap.get(TradeInformationTag.INSTITUTION_ID_CODE));
        scriptMap.put(TransDataKey.iso_f37,dataMap.get(TradeInformationTag.REFERENCE_NUMBER));
        scriptMap.put(TransDataKey.iso_f38,dataMap.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION));

        scriptMap.put(TransDataKey.iso_f41,dataMap.get(TradeInformationTag.TERMINAL_IDENTIFICATION));
        scriptMap.put(TransDataKey.iso_f42,dataMap.get(TradeInformationTag.MERCHANT_IDENTIFICATION));
        scriptMap.put(TransDataKey.iso_f49,"156");
        scriptMap.put(TransDataKey.iso_f64,"0000000000000000");

        ScriptInfo scriptInfo = new ScriptInfo(TransCode.UPLOAD_SCRIPT_RESULT,scriptMap);

        String iso60 = "00"+BusinessConfig.getInstance().getBatchNo(getContext())+"951"+"6"+"0";
        String iso61 = BusinessConfig.getInstance().getBatchNo(getContext())+transDatas.get(TradeInformationTag.TRACE_NUMBER)+dataMap.get(TradeInformationTag.TRANS_DATE);

        scriptInfo.setIso_f55(is055);
        scriptInfo.setIso_f60(iso60);
        scriptInfo.setIso_f61(iso61);
        scriptInfo.setScriptResult(scriptDoResult);
        scriptInfoScriptDao.save(scriptInfo);
        XLogUtil.d("saveScriptInfo","ScriptInfo"+scriptInfo);
        return true;
    }

    @Override
    public boolean onPbocTradeApproved() {
        if (hasImportOnlineResp && isImportOnlineRespTrade()) {
            //如果有脚本需要保存脚本执行结果（DF31）
            String finalF55 = mTradeInformation.getPbocService().readTlvKernelData(EmvTag.getF55Tags1());
            String df31 = null ;
            try{
                df31 = mTradeInformation.getPbocService().readTlvKernelData(EmvTag.combine(new int[]{0xdf,0x31}));
            }catch (NegativeArraySizeException e){
                e.printStackTrace();
            }
            String ecBalance = null;
            if (!TextUtils.isEmpty(finalF55)) {
                curTradeInfo.setPbocOriTlvData(finalF55);
                Map<String, String> tlvMap;
                if (TextUtils.isEmpty(tempMap.get(KEY_IC_DATA_PRINT)))
                    tlvMap = new HashMap<>();
                else
                    tlvMap = TlvUtils.tlvToMap(tempMap.get(KEY_IC_DATA_PRINT));

                /*
                * 增加arpc 错误的测试
                *Map<String, String> testMap = TlvUtils.tlvToMap(finalF55);
                *if("0.02".equals(transDatas.get(TradeInformationTag.TRANS_MONEY))) {
                *    testMap.put("95", "000004F840");
                *}
                *tlvMap.putAll(testMap);
                */

                if( null != df31 ) {
                    tlvMap.putAll(TlvUtils.tlvToMap(df31));
                }
                /*
                * 圈存类交易，圈存后的余额需要打印
                * */
                if( transCode.equals(TransCode.EC_LOAD_CASH)
                    || transCode.equals(TransCode.EC_LOAD_INNER)
                    || transCode.equals(TransCode.EC_LOAD_OUTER)){
                    Map<String, String> map = mTradeInformation.getPbocService().readKernelData(EmvTag.Tag._9F79.getByteValue());
                    if( null != map ) {
                        ecBalance = map.get(EmvTag.Tag._9F79.getStringValue());
                        ecBalance = String.format(Locale.CHINA, "%d.%02d", Long.parseLong(ecBalance) / 100, Long.parseLong(ecBalance) % 100);
                        tempMap.put(TradeInformationTag.EC_TRANS_BALANCE, ecBalance);
                        transDatas.put(TradeInformationTag.EC_TRANS_BALANCE, ecBalance);
                    }
                }
                logger.info("脚本执行结果==>" + df31);
                TradePbocDetail pbocDetail = new TradePbocDetail(tlvMap);
                curTradeInfo.setPbocDetail(pbocDetail);
                curTradeInfo.getPbocDetail().setECInfoEx(tempMap);
                if (tlvMap.containsKey(EmvTagKey.EMVTAG_SCRIPT_RESULT)) {
                    curTradeInfo.getPbocDetail().setPbocScriptResult(tlvMap.get(EmvTagKey.EMVTAG_SCRIPT_RESULT));
                    //下次联机时会进行脚本结果通知
                    BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key.FLAG_NEED_UPLOAD_SCRIPT, true);

                    saveScriptInfo(df31,tempMap,1);/*保存脚本*/
                }
            }
            String iso11 = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
            boolean dbResult;
            //**********更新冲正表信息*************//
            if (CAUSE_REVERSE_SETS.contains(msgTag)) {
                dbResult = reverseDao.deleteById(iso11);
                logger.info(iso11 + "交易成功==>删除冲正表记录==>" + dbResult);
            }
            //************更新数据库****************//
            if (NEED_INSERT_TABLE_SETS.contains(msgTag)) {
                boolean result = storeTradeResult();
                logger.info(iso11 + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + result);
            } else {
                logger.warn(iso11 + "==>交易类型==>该交易无需更新数据库");
            }
            UpdateOriginInfo(iso11); /*增加原交易数据更新*/
            //**************界面跳转*******************//
            if (Settings.isSupportElesign()) {
                //支持电子签名跳转到电子签名页面
                switch (transCode) {
                    case SALE:
                    case AUTH:
                        boolean[] qpsCondition = mTradeView.getHostActivity().getQpsCondition();
                        if (qpsCondition[1]) {//免签
                            gotoNextStep();
                        } else {
                            gotoNextStep();
                        }
                        break;
                    default:
                        gotoNextStep();
                        break;

                }
            } else {
                //不支持电子签名跳转到结果页面
                gotoNextStep();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 存储交易结果相关记录
     */
    private boolean storeTradeResult() {
        boolean dbResult;

        String operatorID = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key
                .KEY_OPER_ID);
        tempMap.put(TradeInformationTag.OPERATOR_CODE, operatorID);
        curTradeInfo.setOperatorNo(operatorID);
        if(transDatas.get("out_order_no")!=null){
            curTradeInfo.setIntoAccount((String) transDatas.get("out_order_no"));
        }
        //保存绿城缴费备注信息
        if(SALE.equals(transCode)&&transDatas.get(JsonKeyGT.additionalData)!=null){
            curTradeInfo.setUnicom_scna_type((String) transDatas.get(JsonKeyGT.additionalData));
        }

        if(SALE.equals(transCode)&&transDatas.get(TradeInformationTag.SUPERVISE_FLAG)!=null){
            curTradeInfo.setSuperviseFlag((String) transDatas.get(TradeInformationTag.SUPERVISE_FLAG));
        }
        if(SALE.equals(transCode)&&transDatas.get(TradeInformationTag.AREA_CODE)!=null){
            curTradeInfo.setAreaCode((String) transDatas.get(TradeInformationTag.AREA_CODE));
        }

        transDatas.put(JsonKeyGT.curTradeInfo, curTradeInfo);
        dbResult = tradeDao.save(curTradeInfo);

        saveIcData(curTradeInfo.getPbocDetail() == null ? null : curTradeInfo.getPbocDetail().convert2Map());
        if (TransCode.SALE_INSTALLMENT.equals(getTradeCode()))
            saveInstallmentData(mTradeInformation.getRespDataMap());
        //保存特定项目中需要记录的额外信息
        ISaveExtInfo saveExtInfo = (ISaveExtInfo) ConfigureManager.getProjectClassInstance(SaveExtInfoImpl.class);
        if (saveExtInfo != null)
            saveExtInfo.save(transDatas);

        checkTradeStorage();
        return dbResult;
    }

    protected void saveInstallmentData(Map<String, Object> transDatas) {
        InstallmentInformation information = (InstallmentInformation) transDatas.get(TradeInformationTag
                .INSTALLMENT_INFORMATION);
        if (information == null)
            return;
        CommonDao<InstallmentInformation> tradeDao = new CommonDao<>(InstallmentInformation.class, dbHelper);
        tradeDao.save(information);
    }

    /**
     * 检查交易记录是否已经存满了，如果满了则置位标志
     */
    public void checkTradeStorage() {
        try {
            ICommonManager commonManager = (ICommonManager) ConfigureManager.getInstance(EposApplication.getAppContext()).getSubPrjClassInstance(new CommonManager());
            long counts = commonManager.getBatchCount();
            long config = BusinessConfig.getInstance().getNumber(EposApplication.getAppContext(),
                    BusinessConfig.Key.KEY_MAX_TRANSACTIONS);
            logger.info("已存储成功流水数量==>" + counts + "==>终端最大存储数量==>" + config);
            if (counts >= config) {
                logger.warn("交易流水数量超限==>下次联机前将进行批结算");
                BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig
                        .Key.FLAG_TRADE_STORAGE_WARNING, true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onPbocTradeRefused() {

        String t95 = mTradeInformation.getPbocService().readTlvKernelData(EmvTag.combine(new int[]{0x95}));

        XLogUtil.w("onPbocTradeRefused","t95:"+t95);
        /*
        * 圈存类交易需要进行第二次GAC
        * */
        if ( priorResponseResult && !bIsEcLoadTrans() ) {
            return onPbocTradeApproved();
        }
        if (hasImportOnlineResp && isImportOnlineRespTrade()) {

            /*
            * 圈存类交易拒绝需要保存脚本结果
            * */
            onTradeFailed((String) transDatas.get(TradeInformationTag.TRACE_NUMBER), StatusCode.TRADING_REFUSED);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPbocTradeTerminated() {
        if (priorResponseResult && !bIsEcLoadTrans() ) {
            return onPbocTradeApproved();
        }
        if (hasImportOnlineResp && isImportOnlineRespTrade()) {
            onTradeFailed(null, StatusCode.TRADING_TERMINATES);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPbocRequestOnline() {
        //有冲正信息，说明正在冲正,有提前需要交易
        if ( reverseDao.countOf() > 0 || scriptInfoScriptDao.countOf() > 0  ){
            if( TransCode.EC_LOAD_CASH.equals(transCode) ) {
                transDatas.put(FLAG_REQUEST_ONLINE,"1");
            }
            else if( TransCode.EC_VOID_CASH_LOAD.equals(transCode) && isICInsertTrade() ){
                transDatas.put(KEY_IC_CONTINUE_ONLINE,"1");
            }
            return true;
        }
        sendData();
        return true;
    }

    @Override
    public boolean onPbocTradeError() {
        if (priorResponseResult && !bIsEcLoadTrans() ) {
            return onPbocTradeApproved();
        }
        if (hasImportOnlineResp && isImportOnlineRespTrade()) {
            onTradeFailed(null, StatusCode.EMV_KERNEL_EXCEPTION);
            return true;
        } else {
            return false;
        }
    }
    /*
    * 更新原交易记录
    * */
    private boolean UpdateOriginInfo(Object tradeIndex)
    {
        boolean dbResult = true;

        IIsUpdateOriginInfo iIsUpdateOriginInfo = (IIsUpdateOriginInfo) ConfigureManager.getSubPrjClassInstance(new BaseIsUpdateOriginInfo());
        if (iIsUpdateOriginInfo.getNeedUpdate(msgTag)) {
            String oriBatchNo = null;
            String oriTraceNo = null;
            OriginalMessage oriIso11 = (OriginalMessage) transDatas.get(TradeInformationTag.ORIGINAL_MESSAGE);
            if (oriIso11 != null) {
                oriBatchNo = String.format(Locale.CHINA, "%06d", oriIso11.getBatchNumber());//原始批次号
                oriTraceNo = String.format(Locale.CHINA, "%06d", oriIso11.getTraceNumber());//原始流水号
            }
            //从本地查找原始交易信息
            TradeInfoRecord originInfo = tradeDao.queryForId(oriTraceNo);
            if (originInfo != null) {
                String batchNo = originInfo.getBatchNo();
                if (batchNo.equals(oriBatchNo)) {
                    originInfo.setStateFlag(ConstDefine.TRANS_STATE_VOID);//已撤销
                    dbResult = tradeDao.update(originInfo);
                    logger.info(tradeIndex + "UpdateOriginInfo==>交易成功==>更新原始交易流水" + oriIso11 + "状态==>" + dbResult);
                } else {
                    logger.warn(tradeIndex + "UpdateOriginInfo==>更新原始交易状态失败==>批次号不符");
                    dbResult = false;
                }
            } else {
                logger.warn(tradeIndex + "UpdateOriginInfo==>更新原始交易状态失败==>无法找到原始交易流水==>" + oriIso11);
                dbResult = false;
            }
        }
        return dbResult;
    }
    /**
    *磁条卡充值身份验证后，是否继续充值
    *磁条卡现金充值 开始交易类型为 {@link TransCode#MAG_ACCOUNT_VERIFY}<br/>
    *磁条卡账户充值 开始交易类型为 {@link TransCode#MAG_ACCOUNT_LOAD_VERIFY}<br/>
    *磁条卡账户充值 流水号与持卡人验证流水号一致
    * */
    private void ContinueMagLoadDealProcessing(){

        String amount = getTempData(TradeInformationTag.ISO62_RES);
        logger.warn("ContinueMagLoadDealProcessing:" + amount);
        String loadAmount = getTransAmount(getTempData(TradeInformationTag.TRANS_MONEY));
        logger.warn("ContinueMagLoadDealProcessing:" + loadAmount);

        if (loadAmount == null || amount == null ) {
            putResponseCode(StatusCode.AMOUNT_GET_ERROR);
            gotoNextStep("99");
            return ;
        }

        String code = TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(transCode) ? TransCode.MAG_ACCOUNT_LOAD : TransCode.MAG_CASH_LOAD;
        mTradeInformation.setTransCode(code);
        transCode = code;
        if( TransCode.MAG_CASH_LOAD.equals(transCode) ) {
            /*
            * 磁条卡现金充值的流水号和身份验证的流水号不一致
            * */
            mTradeInformation.getTransDatas().put(TradeInformationTag.TRACE_NUMBER, null);
        }

        mTradeInformation.getRespDataMap().put(TradeInformationTag.RESPONSE_CODE,null);

        amount = String.format(Locale.CHINA, "%d.%02d", Long.parseLong(amount) / 100, Long.parseLong(amount) % 100);
        String amountTip = ("可充值金额:" + amount +"\n"+"充值金额:"+getTempData(TradeInformationTag.TRANS_MONEY));
        DialogFactory.showSelectDialog(getContext(),"请确认", amountTip , new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                    EventBus.getDefault().unregister(TradingPresent.this);/*防止多次连接 多次收到消息*/
                    gotoNextStep();
                }
                else{
                    putResponseCode(StatusCode.USER_CANCEL);
                    gotoNextStep("99");
                }
            }
        },false);
    }
    /**
     * 交易成功。删除冲正表的数据，插入到交易流水
     * 该方法在子线程中执行
     * {@link #handleMessage} <br/>
     *
     */
    public void onTradeSuccess(Map<String, Object> returnData) {
        //==================================验证没问题后请删除已注释的原有代码===================================
        boolean dbResult;
        Map<String, Object> combinMap = new HashMap<>();

        addLocalInfo(transDatas);
        combinMap.putAll(transDatas);
        combinMap.putAll(returnData);
        combinMap.putAll(mTradeInformation.getDataMap());
        //报文头处理要求记录，在交易结束后或交易前发起交易，交易成功后清除处理要求记录
//        dealWithMsgHeader((String) returnData.get(TradeInformationTag.MSG_HEADER));

        curTradeInfo = new TradeInfoRecord(transCode, combinMap);
        tempMap.putAll(convertObject2String(returnData));
        putResponseCode((String) returnData.get(TradeInformationTag.RESPONSE_CODE), null);

        //**********************判断是否是需要导入联机响应数据的交易*********************//
        if (isImportOnlineRespTrade() ) {
            //需要导入联机响应数据
            String iso55 = (String) returnData.get(TradeInformationTag.IC_DATA);
            if (TextUtils.isEmpty(iso55)) {
                logger.warn("^_^ 55域数据为空 ^_^");
                mTradeInformation.getPbocService().abortProcess();
                hasImportOnlineResp = true;
                onPbocTradeApproved();
                return;
//                gotoNextStep();
            }
            try {
                Map<String, String> f55Map = TlvUtils.tlvToMap(iso55);
                String tag91 = f55Map.get("91");
                if (!TextUtils.isEmpty(tag91) && tag91.length() > 3) {
                    String respCode = new String(HexUtils.hexStringToByte(tag91.substring(tag91.length() - 4, tag91
                            .length())));
                    logger.warn("发卡行认证数据：" + tag91 + "==>响应码：" + respCode);
                    EnumOnlineResult or = EnumOnlineResult.ONLINE_APPROVED;
                    if ("01".equals(respCode)) {//发卡行语音参考
                        or = EnumOnlineResult.ONLINE_VOICE_REFERENCE;
                    } else if ("05".equals(respCode)) {//交易拒绝
                        or = EnumOnlineResult.ONLINE_REFUSED;
                    }
                    mTradeInformation.getPbocService().importOnlineResult(true, or, iso55);
                } else {
                    mTradeInformation.getPbocService().importOnlineResult(true, EnumOnlineResult.ONLINE_APPROVED,
                            iso55);
                }
//                //如果有脚本需要保存脚本执行结果（DF31）
//                String finalF55 = mTradeInformation.getPbocService().readTlvKernelData(EmvTag.getF55Tags1());
//                if (!TextUtils.isEmpty(finalF55)) {
//                    curTradeInfo.setPbocOriTlvData(finalF55);
//                    Map<String, String> tlvMap = TlvUtils.tlvToMap(finalF55);
//
//                    TradePbocDetail pbocDetail = new TradePbocDetail(tlvMap);
//                    curTradeInfo.setPbocDetail(pbocDetail);
//                    curTradeInfo.getPbocDetail().setECInfo(returnData);
//                    if (tlvMap.containsKey(EmvTagKey.EMVTAG_SCRIPT_RESULT)) {
//                        curTradeInfo.getPbocDetail().setPbocScriptResult(tlvMap.get(EmvTagKey.EMVTAG_SCRIPT_RESULT));
//                        //下次联机时会进行脚本结果通知
//                        BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig.Key
//                                .FLAG_NEED_UPLOAD_SCRIPT, true);
//                    }
//                }
                hasImportOnlineResp = true;
            } catch (Exception e) {
                e.printStackTrace();
                mTradeInformation.getPbocService().abortProcess();
                putResponseCode(StatusCode.IC_PROCESS_ERROR);
                gotoNextStep("99");
            }
//            String finalF55 = mTradeInformation.getPbocService().readTlvKernelData(EmvTag.getF55Tags1());
//            curTradeInfo.setIso_f55_send(finalF55);
//            logger.debug("请求55域：" + dataMap.get(iso_f55));
//            logger.debug("返回55域：" + tempMap.get(iso_f55));
//            logger.debug("最终55域：" + finalF55);
        } else {
            String tradeIndex = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
            //********************更新冲正表信息***************************//
            if (CAUSE_REVERSE_SETS.contains(msgTag)||TransCode.SALE_RESULT_QUERY.equals(msgTag)) {
                dbResult = reverseDao.deleteById(tradeIndex);
                logger.info(tradeIndex + "交易成功==>删除冲正表记录==>" + dbResult);
            }
            //******************撤销、退货类交易，同步更新原始交易信息****************//
            IIsUpdateOriginInfo iIsUpdateOriginInfo = (IIsUpdateOriginInfo) ConfigureManager.getSubPrjClassInstance
                    (new BaseIsUpdateOriginInfo());
            if (iIsUpdateOriginInfo.getNeedUpdate(msgTag)) {
                String oriBatchNo = null;
                String oriTraceNo = null;
                OriginalMessage oriIso11 = (OriginalMessage) transDatas.get(TradeInformationTag.ORIGINAL_MESSAGE);
                if (oriIso11 != null) {
                    oriBatchNo = String.format(Locale.CHINA, "%06d", oriIso11.getBatchNumber());//原始批次号
                    oriTraceNo = String.format(Locale.CHINA, "%06d", oriIso11.getTraceNumber());//原始流水号
                }
                //从本地查找原始交易信息
                TradeInfoRecord originInfo = tradeDao.queryForId(oriTraceNo);
                if (originInfo != null) {
                    String batchNo = originInfo.getBatchNo();
                    if (batchNo.equals(oriBatchNo)) {
                        originInfo.setStateFlag(ConstDefine.TRANS_STATE_VOID);//已撤销
                        dbResult = tradeDao.update(originInfo);
                        logger.info(tradeIndex + "==>交易成功==>更新原始交易流水" + oriIso11 + "状态==>" + dbResult);
                    } else {
                        logger.warn(tradeIndex + "==>更新原始交易状态失败==>批次号不符");
                    }
                } else {
                    logger.warn(tradeIndex + "==>更新原始交易状态失败==>无法找到原始交易流水==>" + oriIso11);
                }
            }

            if (SIGN_OUT.equals(transCode)) {
                BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key
                        .FLAG_SIGN_IN, false);
                BusinessConfig.getInstance().setFlag(EposApplication.getAppContext(), BusinessConfig.Key
                        .KEY_IS_BATCH_BUT_NOT_OUT, false);
                BusinessConfig.getInstance().setNumber(EposApplication.getAppContext(), BusinessConfig.Key
                        .KEY_POS_SERIAL, 1);
                mTradeView.popToast(R.string.tip_sign_out);
                logger.debug("签退成功");
                jumpToLogin();
            }
            //**********************更新数据库****************************//
            if (NEED_INSERT_TABLE_SETS.contains(msgTag)) {
                if (!TextUtils.isEmpty(tempMap.get(KEY_IC_DATA_PRINT))) {
                    TradePbocDetail pbocDetail = new TradePbocDetail(TlvUtils.tlvToMap(tempMap.get(KEY_IC_DATA_PRINT)));
                    curTradeInfo.setPbocDetail(pbocDetail);
                }

                dbResult = storeTradeResult();
                logger.info(tradeIndex + "==>交易类型==>" + transCode + "==>交易成功==>更新交易流水表==>" + dbResult);
            } else {
                logger.warn(tradeIndex + "==>交易类型==>该交易无需更新数据库"+msgTag);
            }

            if( TransCode.MAG_ACCOUNT_VERIFY.equals(msgTag) || TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(msgTag) ){
                EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.MAG_LOAD_TASK_CONTINUE));
                return ;
            }
            //**********************跳转界面****************************//
            if (Settings.isSupportElesign()) {
                //支持电子签名跳转到电子签名页面
                switch (transCode) {
                    case SALE:
                    case AUTH:
                        boolean[] qpsCondition = mTradeView.getHostActivity().getQpsCondition();
                        if (qpsCondition[1]) {//免签
                            gotoNextStep();
                        } else {
                            gotoNextStep();
                        }
                        break;
                    default:
                        gotoNextStep();
                        break;

                }
            } else {
                //不支持电子签名跳转到结果页面
                gotoNextStep();
            }
        }

    }

    /**
     * 添加本地存储的参数信息
     *
     * @param dataMap 交易记录
     */
    private void addLocalInfo(Map<String, Object> dataMap) {
        dataMap.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(EposApplication
                .getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));
        dataMap.put(TradeInformationTag.TRANS_YEAR, String.format(Locale.CHINA, "%04d", Calendar.getInstance().get
                (Calendar.YEAR)));
    }

    /**
     * 将ic卡数据保存到数据库
     */
    protected void saveIcData(Map<String, String> kerlData) {
        String unKnown = "";
        String aid = "";
        String arqc = "";
        String iad = "";
        String atc = "";
        String tvr = "";
        String tsi = "";
        String aip = "";
        String cid = "";
        String tc = "";
        boolean isNotNeedSign = mTradeModel.isTradeSlipNoSign();
        boolean isNotNeedPin = mTradeModel.isTradeNoPin();
        String limitAmount = mTradeModel.getSlipNoSignAmount();
//        String icData = tempMap.get(TransDataKey.KEY_IC_DATA_PRINT);
        if (null != kerlData && kerlData.size() > 0) {
//            Map<String, String> stringMap = TlvUtils.tlvToMap(icData);
//            if (kerlData != null && kerlData.size() > 0) {
//                stringMap.putAll(kerlData);
//            }
            Map<String, String> stringMap = kerlData;
            //不可预知数
            unKnown = stringMap.get("9F37");
            aid = stringMap.get("4F");
            cid = stringMap.get("9F27");
            if ("40".equals(cid))
                tc = stringMap.get("9F26");
            else
                arqc = stringMap.get("9F26");
            iad = stringMap.get("9F10");
            atc = stringMap.get("9F36");
            tvr = stringMap.get("95");
            tsi = stringMap.get("9B");
            aip = stringMap.get("82");
        }
        TradePrintData tradePrintData = new TradePrintData();
        if (null != transDatas.get(TradeInformationTag.TRACE_NUMBER)) {
            tradePrintData.setIso_f11((String) transDatas.get(TradeInformationTag.TRACE_NUMBER));
        }
        if (null != arqc) {
            tradePrintData.setArqc(arqc);
        }
        if (null != tc) {
            tradePrintData.setTc(tc);
        }
        if (null != tvr) {
            tradePrintData.setTvr(tvr);
        }
        if (null != aid) {
            tradePrintData.setAid(aid);
        }
        if (null != atc) {
            tradePrintData.setAtc(atc);
        }
        if (null != tsi) {
            tradePrintData.setTsi(tsi);
        }
        if (null != unKnown) {
            tradePrintData.setUmpr_num(unKnown);
        }
        if (null != aip) {
            tradePrintData.setAip(aip);
        }
        if (null != iad) {
            tradePrintData.setIad(iad);
        }
        if (null != limitAmount) {
            tradePrintData.setAmount(limitAmount);
        }
        tradePrintData.setNoNeedPin(isNotNeedPin);
        tradePrintData.setNoNeedSign(isNotNeedSign);
        tradePrintData.setRePrint(false);
        printDataCommonDao.save(tradePrintData);
    }
    private void comfirmMagLoadDealProcessing(){

        String amount = getTempData(TradeInformationTag.ISO62_RES);
        logger.warn("ContinueMagLoadDealProcessing:" + amount);
        String loadAmount = getTransAmount(getTempData(TradeInformationTag.TRANS_MONEY));
        logger.warn("ContinueMagLoadDealProcessing:" + loadAmount);

        if (loadAmount == null || amount == null ) {
            putResponseCode(StatusCode.AMOUNT_GET_ERROR);
            gotoNextStep("99");
            return ;
        }

        String code = TransCode.MAG_ACCOUNT_LOAD_VERIFY.equals(transCode) ? TransCode.MAG_ACCOUNT_LOAD : TransCode.MAG_CASH_LOAD;
        mTradeInformation.setTransCode(code);
        transCode = code;
        if( TransCode.MAG_CASH_LOAD.equals(transCode) ) {
            /*
            * 磁条卡现金充值的流水号和身份验证的流水号不一致
            * */
            mTradeInformation.getTransDatas().put(TradeInformationTag.TRACE_NUMBER, null);
        }

        mTradeInformation.getRespDataMap().put(TradeInformationTag.RESPONSE_CODE,null);

        amount = String.format(Locale.CHINA, "%d.%02d", Long.parseLong(amount) / 100, Long.parseLong(amount) % 100);
        String amountTip = ("可充值金额:" + amount +"\n"+"充值金额:"+getTempData(TradeInformationTag.TRANS_MONEY));
        DialogFactory.showSelectDialog(getContext(),"请确认", amountTip , new AlertDialog.ButtonClickListener() {
            @Override
            public void onClick(AlertDialog.ButtonType button, View v) {
                if (button.equals(AlertDialog.ButtonType.POSITIVE)) {
                    EventBus.getDefault().unregister(TradingPresent.this);/*防止多次连接 多次收到消息*/
                    gotoNextStep();
                }
                else{
                    putResponseCode(StatusCode.USER_CANCEL);
                    gotoNextStep("99");
                }
            }
        },false);
    }
    /**
     * {@link #onTradeSuccess(Map)} TradeMessage.MAG_LOAD_TASK_CONTINUE <br/>
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleMessage(SimpleMessageEvent<String> event) {
        logger.debug("^_^ EVENT  code:" + event.getCode() + " message:" + event.getMessage() + " ^_^");
        switch (event.getCode()) {
            case TradeMessage.PRE_TASK_CONTINUE:
                beginTransaction();
                break;
            //批结算成功
            case TradeMessage.BATCH_CHECK_SUCCESS:
                new SettlementTrade().execute(mTradeView, this);
                break;
            case TradeMessage.QUERY_SCAN_PAY:
                new QueryScanResultTrade().execute(mTradeView, this);
                break;
            case TradeMessage.MAG_LOAD_TASK_CONTINUE:
                ContinueMagLoadDealProcessing();
                logger.debug("MAG_LOAD_TASK_CONTINUE MAG_LOAD_TASK_CONTINUE");
                break;
            case TradeMessage.MAG_LOAD_CONFIRM:
                logger.debug("MAG_LOAD_CONFIRM MAG_LOAD_CONFIRM 磁条卡充值确认");
                new MagLoadConfirmTrade().execute(mTradeView,this);
                break;
            /*
            * 脚本上送时无法连接后台需要终止当前交易
            * */
            case TradeMessage.PRE_TASK_COMM_TERMINATE:
                mTradeView.getHostActivity().jumpToResultActivity(StatusCode.SOCKET_TIMEOUT);
                break;
        }
    }

    protected String getMsgTagByTranTag(Map<String, Object> transData, String tranTag) {
        ArrayList<String> msgTags = (ArrayList<String>) transData.get(ITradeParameter.KEY_MSG_TAGS);
        if (msgTags != null && msgTags.size() > 0) {
            tranTag = msgTags.get(0);
        }
        return tranTag;
    }
    private class MagLoadConfirmTrade implements ManageTransaction {

        Map<String, Object> repData = new HashMap<>();
        String sTransCode = TransCode.MAG_CASH_LOAD;
        @Override
        public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {

            new AsyncMagLoadConfirmTask(tradeView.getHostActivity(), tradePresent.getTransData(),repData) {

                @Override
                public void onStart() {
                    if (tradeView instanceof ITradingView)
                        ((ITradingView) tradeView).updateHint("磁条卡充值确认...");

                    //sTransCode = TransCode.MAG_CASH_LOAD.equals(dataMap.get(TradeInformationTag.TRANSACTION_TYPE)) ? TransCode.MAG_CASH_LOAD : TransCode.MAG_ACCOUNT_LOAD;

                }

                @Override
                public void onFinish(String[] status) {
                    tradePresent.getTempData().put(iso_f39, status[0]);
                    tradePresent.putResponseCode(status[0], status[1]);
                    if( status[2] != null ) {
                        if(status[2].equals(EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId()))){
                            XLogUtil.w("AsyncMagLoadConfirmTask rep","SOCKET_TIMEOUT");
                            respHelper.onRespFailed(TradingPresent.this, StatusCode.SOCKET_TIMEOUT.getStatusCode(), EposApplication.getAppContext().getString(StatusCode.SOCKET_TIMEOUT.getMsgId()));
                        }
                        else{
                            XLogUtil.w("AsyncMagLoadConfirmTask rep","DATA_EXCHANGE_ERROR");
                            respHelper.onRespFailed(TradingPresent.this, StatusCode.DATA_EXCHANGE_ERROR.getStatusCode(), EposApplication.getAppContext().getString(StatusCode.DATA_EXCHANGE_ERROR.getMsgId()));
                        }
                    }
                    else{
                        mTradeView.getHostActivity().mTradeInformation.setTransCode(sTransCode);
                        mTradeView.getHostActivity().mTradeInformation.getRespDataMap().putAll(repData);
                        respHelper.onRespSuccess(TradingPresent.this, repData);
                        XLogUtil.w("AsyncMagLoadConfirmTask rep",""+repData);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tradePresent.getTradeCode());
        }
    }


}


