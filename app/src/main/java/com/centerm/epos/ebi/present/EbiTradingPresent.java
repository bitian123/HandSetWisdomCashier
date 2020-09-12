package com.centerm.epos.ebi.present;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.common.utils.TlvUtils;
import com.centerm.cpay.midsdk.dev.define.IPbocService;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumOnlineResult;
import com.centerm.cpay.midsdk.dev.define.pboc.EnumPbocResultType;
import com.centerm.epos.EposApplication;
import com.centerm.epos.base.BaseRuntimeException;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.ReverseInfo;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.TradePbocDetail;
import com.centerm.epos.bean.TradeRecordForUpload;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.ebi.bean.PropertyBean;
import com.centerm.epos.ebi.bean.SaleScanResult;
import com.centerm.epos.ebi.bean.ScanRefundBean;
import com.centerm.epos.ebi.bean.ScanVoidBean;
import com.centerm.epos.ebi.common.PayResult;
import com.centerm.epos.ebi.common.ScanRefundResult;
import com.centerm.epos.ebi.keys.JsonKey;
import com.centerm.epos.ebi.msg.EbiMessageFactory;
import com.centerm.epos.ebi.msg.GetRequestData;
import com.centerm.epos.ebi.msg.GetTransData;
import com.centerm.epos.ebi.utils.DateUtil;
import com.centerm.epos.fragment.trade.ITradingView;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.HttpCommParameter;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.present.transaction.TradingPresent;
import com.centerm.epos.redevelop.BaseIsUpdateOriginInfo;
import com.centerm.epos.redevelop.IIsUpdateOriginInfo;
import com.centerm.epos.redevelop.ISaveExtInfo;
import com.centerm.epos.redevelop.SaveExtInfoImpl;
import com.centerm.epos.transcation.pos.constant.ReverseReasonCode;
import com.centerm.epos.transcation.pos.constant.RuntimeExceptionCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TradeTempInfoTag;
import com.centerm.epos.transcation.pos.controller.ProcessRequestManager;
import com.centerm.epos.transcation.pos.manager.ManageTransaction;
import com.centerm.epos.transcation.pos.manager.RunTimeChecker;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.xml.bean.TradeItem;
import com.centerm.smartpos.util.HexUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.AUTH;
import static com.centerm.epos.common.TransCode.AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.CANCEL;
import static com.centerm.epos.common.TransCode.CAUSE_REVERSE_SETS;
import static com.centerm.epos.common.TransCode.COMPLETE_VOID;
import static com.centerm.epos.common.TransCode.NEED_INSERT_TABLE_SETS;
import static com.centerm.epos.common.TransCode.NOTIFY_TRADE_SETS;
import static com.centerm.epos.common.TransCode.REFUND;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.SALE_SCAN;
import static com.centerm.epos.common.TransCode.SCAN_VOID;
import static com.centerm.epos.common.TransCode.SIGN_OUT;
import static com.centerm.epos.common.TransCode.VOID;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_AMOUNT;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_CARD_CONFIRM_RESULT;
import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_PIN;
import static com.centerm.epos.common.TransDataKey.FLAG_REQUEST_ONLINE;
import static com.centerm.epos.common.TransDataKey.KEY_IC_DATA_PRINT;
import static com.centerm.epos.ebi.common.TransCode.PROPERTY_NOTICE;
import static com.centerm.epos.ebi.common.TransCode.SALE_PROPERTY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID;
import static com.centerm.epos.ebi.common.TransCode.SALE_SCAN_VOID_QUERY;

/**
 * Created by liubit on 2017/12/25.
 * 电银 重写组包方法
 */

public class EbiTradingPresent extends TradingPresent {
    private static int CHECK_MAX_NUM = 18;//扫码轮询次数
    private static int checkNum = 0;//扫码轮询次数

    public EbiTradingPresent(ITradeView mTradeView) {
        super((TradingFragment) mTradeView);
        mTradingView = (ITradingView) mTradeView;

    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        factory = EbiMessageFactory.createMessageByType(EbiMessageFactory.MESSAGE_HTTP_JSON, transDatas);
        logger.debug("使用EbiTradingPresent");
    }

    @Override
    public void beginTransaction() {
        String transCode = mTradeInformation.getTransCode();

        if (preProcess(transCode)) {
            return;
        }

        Map<String, TradeItem> tradeItemMap = ConfigureManager.getInstance(EposApplication.getAppContext())
                .getTradeItemMap();
        TradeItem managerTrade = tradeItemMap.get(transCode);
        if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
            executeManagerTrade(managerTrade);
        } else {

            if (preTradeBeforeOnline(tradeItemMap)) return;
            beginOnline();
        }
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

    private void executeManagerTrade(TradeItem managerTrade) {
        try {
            Class clz = Class.forName(managerTrade.getTradeClz());
            ManageTransaction manageTransaction = (ManageTransaction) clz.newInstance();
            manageTransaction.execute(mTradeView, this);
        } catch (Exception e) {
            e.printStackTrace();
            putResponseCode("55", e.getMessage());
            gotoNextStep();
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
        if (ConfigureManager.getInstance(getContext()).isOptionFuncEnable(getContext(), com.centerm.epos.xml.keys
                .Keys.obj().script_update)) {
            managerTrade = tradeItemMap.get(TransCode.UPLOAD_SCRIPT_RESULT);
            if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
                //检测到当前有IC卡脚本结果需要上送，则先进行联机脚本上送，再发起交易
                executeManagerTrade(managerTrade);
                return true;
            }
        }

        managerTrade = tradeItemMap.get(TransCode.REVERSE);
        if (managerTrade != null && checkBeforExecute(managerTrade.getCheckerClz())) {
            //如果当前有冲正信息，需要先进行冲正后才开始进行交易
            executeManagerTrade(managerTrade);
            return true;
        }

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

    /**
     * 除了特殊联机交易类型外，其它交易都调用该方法，开启联机交易的一系列步骤。
     * 包含对内核时间的处理和应答，组报文发起网络请求等等。
     */
    private void beginOnline() {
        if (isICInsertTrade()) {
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
                sendData();
            } else {
                logger.warn("内核流程异常，交易终止，不进行联机交易");
                mTradeView.getHostActivity().jumpToResultActivity(StatusCode.TRADING_REFUSED);
                pbocService.abortProcess();
            }
        } else {
            sendData();
        }
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
                transDatas.put(TransDataKey.KEY_TRANS_TIME, transDate);
                transDatas.put(JsonKey.sendTime, DateUtil.getToday("yyyyMMddHHmmss"));
                if(transDatas.get(TradeInformationTag.TRACE_NUMBER)==null){
                    transDatas.put(TradeInformationTag.TRACE_NUMBER, GetRequestData.getTraceNumber(transDatas));
                }
                if(TextUtils.equals(SALE_SCAN, transCode)
                        ||TextUtils.equals(SALE_SCAN_VOID, transCode)
                        ||TextUtils.equals(SALE_SCAN_REFUND, transCode)){
                    if(TextUtils.equals(SALE_SCAN, transCode)){
                        transDatas.put(JsonKey.mer_order_no, GetRequestData.creatOrderNo());//生成订单号
                    }
                    if(TextUtils.equals(SALE_SCAN_REFUND, transCode)){
                        transDatas.put(JsonKey.mer_refund_order_no, transDatas.get(JsonKey.mer_order_no));//原订单号
                        transDatas.put(JsonKey.mer_order_no, GetRequestData.creatOrderNo());//生成退货订单号
                    }
                    TradeInfoRecord record = new TradeInfoRecord(transCode, transDatas);
                    record.setTransType(transCode);
                    record.setUnicom_scna_type((String) transDatas.get(JsonKey.pay_type));
                    record.setVoucherNo((String) transDatas.get(TradeInformationTag.TRACE_NUMBER));
                    record.setBatchNo(GetRequestData.getBatchNo());
                    record.setMerchantNo(GetRequestData.getMercode());
                    record.setTerminalNo(GetRequestData.getTermcde());
                    record.setOperatorNo(GetRequestData.getOperatorCode());
                    record.setScanVoucherNo((String) transDatas.get(JsonKey.mer_order_no));
                    record.setOriAuthCode((String) transDatas.get(JsonKey.mer_refund_order_no));
                    String time = (String) transDatas.get(JsonKey.sendTime);
                    record.setTransYear(time.substring(0,4));
                    record.setTransDate(time.substring(4,8));
                    record.setTransTime(time.substring(8,14));
                    record.setMerchantName(GetRequestData.getMerName());
                    record.setAmount((String)transDatas.get(TradeInformationTag.TRANS_MONEY));
                    record.setCardNo("-1");
                    if(transDatas.get(JsonKey.out_order_no)!=null){
                        //外部订单号保存在 传入卡卡号 中
                        record.setIntoAccount((String) transDatas.get(JsonKey.out_order_no));
                    }
                    tradeDao.save(record);
                }

                final Object msgPacket = factory.packMessage(transCode, transDatas);
                if (msgPacket == null) {
                    putResponseCode(StatusCode.PACKAGE_ERROR);
                    logger.warn("请求报文为空，退出");
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
                                record.setIso_f39(ReverseReasonCode.TIME_OUT);    //默认原因为接收数据超时
                                boolean r = reverseDao.save(record);
                                logger.info(iso11 + "==>" + msgTag + "==>插入冲正表中==>" + r);
                            }
                        }
                    }

                    if((SALE_SCAN_QUERY.equals(transCode)
                            ||SALE_SCAN_VOID_QUERY.equals(transCode)
                            ||SALE_SCAN_REFUND_QUERY.equals(transCode))&&checkNum!=0){
                        try {//扫码查询间隔5s
                            sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        DataExchanger dataExchanger;
                        if(transCode.contains("SCAN")||transCode.contains("PROPERTY")){
                            //扫码交易采用http方式
                            dataExchanger = new DataExchanger(ICommunication.COMM_HTTP, new HttpCommParameter());
                        }else {
                            dataExchanger = DataExchangerFactory.getInstance();
                        }
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
                                if(TextUtils.equals(TransCode.SALE_SCAN, transCode)){
                                    SaleScanResult returnData = (SaleScanResult) mapData.get(JsonKey.returnData);
                                    if(com.centerm.epos.ebi.common.TransCode.checkTradeState(
                                            returnData.getBody().getResponse().getStatus())){
                                    //if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getPay_result());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        //电银流水号-保存在 referenceNo 中
                                        transDatas.put(TradeInformationTag.REFERENCE_NUMBER, returnData.getBody().getResponse().getResult().getPay_no());
                                        //支付状态保存在cardNo中
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getPay_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_SCAN_QUERY, transCode)){
                                    SaleScanResult returnData = (SaleScanResult) mapData.get(JsonKey.returnData);
                                    if(com.centerm.epos.ebi.common.TransCode.checkTradeState(
                                            returnData.getBody().getResponse().getStatus())){
                                    //if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getPay_result());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        //电银流水号-保存在 referenceNo 中
                                        transDatas.put(TradeInformationTag.REFERENCE_NUMBER, returnData.getBody().getResponse().getResult().getPay_no());
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getPay_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_SCAN_VOID, transCode)){
                                    ScanVoidBean returnData = (ScanVoidBean) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getRevoke_result());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getRevoke_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_SCAN_VOID_QUERY, transCode)){
                                    ScanVoidBean returnData = (ScanVoidBean) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getRevoke_result());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getRevoke_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_SCAN_REFUND, transCode)){
                                    ScanRefundBean returnData = (ScanRefundBean) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getRefund_result());
                                        transDatas.put(TradeInformationTag.SCAN_VOUCHER_NO, returnData.getBody().getResponse().getResult().getMer_refund_order_no());
                                        //原订单号保存在oriAuthCode中
                                        transDatas.put(TransDataKey.key_oriAuthCode, returnData.getBody().getResponse().getResult().getMer_order_no());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getRefund_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_SCAN_REFUND_QUERY, transCode)){
                                    ScanRefundBean returnData = (ScanRefundBean) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        mapData.put(JsonKey.TRANS_RESULT_FLAG,
                                                returnData.getBody().getResponse().getResult().getRefund_result());
                                        transDatas.put(TradeInformationTag.SCAN_VOUCHER_NO, returnData.getBody().getResponse().getResult().getMer_refund_order_no());
                                        //原订单号保存在oriAuthCode中
                                        transDatas.put(TransDataKey.key_oriAuthCode, returnData.getBody().getResponse().getResult().getMer_order_no());
                                        transDatas.put(JsonKey.sendTime, returnData.getHead().getSendTime());
                                        transDatas.put(TradeInformationTag.BANK_CARD_NUM, returnData.getBody().getResponse().getResult().getRefund_result());
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        refreshPayResult(returnData.getBody().getResponse().getStatus());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(SALE_PROPERTY, transCode)){
                                    PropertyBean returnData = (PropertyBean) mapData.get(JsonKey.returnData);
                                    if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                        mapData.put(TradeInformationTag.RESPONSE_CODE, "00");
                                        respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
                                        return;
                                    }else {
                                        putResponseCode(returnData.getBody().getResponse().getStatus(), returnData.getBody().getResponse().getStatus_msg());
                                        gotoNextStep("99");
                                        return;
                                    }
                                }else if(TextUtils.equals(PROPERTY_NOTICE, transCode)){
                                    if(transDatas.get(JsonKey.isTradeDetail)!=null){
                                        SaleScanResult returnData = (SaleScanResult) mapData.get(JsonKey.returnData);
                                        if(TextUtils.equals("00", returnData.getBody().getResponse().getStatus())){
                                            mTradeView.popToast("交易结果上送成功");
                                            gotoNextStep("100");
                                            return;
                                        }else {
                                            mTradeView.popToast(returnData.getBody().getResponse().getStatus_msg());
                                            gotoNextStep("100");
                                            return;
                                        }
                                    }
                                }
                                mTradeInformation.setRespDataMap(mapData);
                                if (NOTIFY_TRADE_SETS.contains(msgTag)) {
                                    mapData.put(TradeTempInfoTag.REQUEST_MSG, HexUtil.bytesToHexString((byte[]) msgPacket));
                                }

                                respHelper.onRespSuccess(EbiTradingPresent.this, mapData);
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
                        if(mTradeView!=null&&mTradeView.getHostActivity()!=null){
                            if (e instanceof BaseRuntimeException) {
                                BaseRuntimeException baseRuntimeException = (BaseRuntimeException) e;
                                putResponseCode(baseRuntimeException.getErrCode() + "", baseRuntimeException.getMessage());
                            } else
                                putResponseCode(StatusCode.DATA_EXCHANGE_ERROR);
                            gotoNextStep("99");
                        }
                    }
                } else {
                    logger.warn("报文格式非字节数组");
                }
            }
        }.start();
    }

    //更新交易结果
    private void refreshPayResult(String status){
        if("P3".equals(status) ||"P5".equals(status) ||"PB".equals(status) ||"PA".equals(status)){
            if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
                curTradeInfo = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
                curTradeInfo.setCardNo(status);
                tradeDao.update(curTradeInfo);
            }
        }
    }

    @Override
    public void onTradeFailed(String iso11, String code, String msg) {
        super.onTradeFailed(iso11, code, msg);
        checkNum = 0;
    }

    @Override
    public void onTradeFailed(String iso11, StatusCode status) {
        super.onTradeFailed(iso11, status);
        checkNum = 0;
    }

    @Override
    public void onTradeFailed(String iso11, ISORespCode iso) {
        super.onTradeFailed(iso11, iso);
        checkNum = 0;
    }

    /**
     * 交易成功。删除冲正表的数据，插入到交易流水
     */
    public void onTradeSuccess(Map<String, Object> returnData) {
        //==================================验证没问题后请删除已注释的原有代码===================================
        boolean dbResult;
        Map<String, Object> combinMap = new HashMap<>();
        if(TransCode.SALE_SCAN.equals(transCode)){
            SaleScanResult data = (SaleScanResult) returnData.get(JsonKey.returnData);
            if("03".equals(transDatas.get(JsonKey.pay_type))){//扫码支付完成后，银联的需要查询交易状态
                checkNum++;
                logger.debug("支付状态查询次数："+checkNum);
                mTradeInformation.setTransCode(SALE_SCAN_QUERY);
                transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
                transDatas.put(JsonKey.pay_no, data.getBody().getResponse().getResult().getPay_no());
                gotoNextStep("3");
                return;
            }else {
                String payResult = data.getBody().getResponse().getResult().getPay_result();
                logger.debug("扫码支付状态=>"+payResult);
                if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
                    curTradeInfo = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
                    curTradeInfo.setCardNo(payResult);
                    tradeDao.update(curTradeInfo);
                }
                //I("I", "待支付") R("R", "正在执行"),
                if(PayResult.I.getCode().equals(payResult)||PayResult.R.getCode().equals(payResult)||TextUtils.isEmpty(payResult)||"null".equals(payResult)){
                    checkNum++;
                    logger.debug("支付状态查询次数："+checkNum);
                    mTradeInformation.setTransCode(SALE_SCAN_QUERY);
                    transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
                    transDatas.put(JsonKey.pay_no, data.getBody().getResponse().getResult().getPay_no());
                    gotoNextStep("3");
                    return;
                }else if(PayResult.F.getCode().equals(payResult)){//F("F", "失败")
                    onTradeFailed(null, payResult, PayResult.F.getDes());
                    return;
                }else if(PayResult.O.getCode().equals(payResult)){//O("O", "交易关闭")
                    onTradeFailed(null, payResult, PayResult.O.getDes());
                    return;
                }else if(PayResult.S.getCode().equals(payResult)){//S("S", "交易成功")
                    logger.debug("交易成功");
                    transDatas.put(JsonKey.pay_time, data.getBody().getResponse().getResult().getPay_time());
                }else {//payResult为空，银行通道异常
                    onTradeFailed(null, payResult, PayResult.F.getDes());
                    return;
                }
            }
        }
        if(SALE_SCAN_QUERY.equals(transCode)){//查询支付状态
            SaleScanResult data = (SaleScanResult) returnData.get(JsonKey.returnData);
            if(data!=null){
                String payResult = data.getBody().getResponse().getResult().getPay_result();
                logger.debug("扫码支付状态=>"+payResult);
                if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
                    curTradeInfo = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
                    curTradeInfo.setCardNo(payResult);
                    tradeDao.update(curTradeInfo);
                }
                //I("I", "待支付") R("R", "正在执行"),
                if(PayResult.I.getCode().equals(payResult)||PayResult.R.getCode().equals(payResult)||TextUtils.isEmpty(payResult)||"null".equals(payResult)){
                    if(checkNum==0){//订单查询中的查询交易，只查询一次
                        if(PayResult.I.getCode().equals(payResult)){
                            onTradeFailed(null, payResult, PayResult.I.getDes());
                            return;
                        }else if(PayResult.R.getCode().equals(payResult)){
                            onTradeFailed(null, payResult, PayResult.R.getDes());
                            return;
                        }else {
                            onTradeFailed(null, payResult, PayResult.R.getDes());
                            return;
                        }
                    }
                    if(checkNum < CHECK_MAX_NUM){
                        checkNum++;
                        logger.debug("支付状态查询次数："+checkNum);
                        mTradeInformation.setTransCode(SALE_SCAN_QUERY);
                        transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
                        transDatas.put(JsonKey.pay_no, data.getBody().getResponse().getResult().getPay_no());
                        gotoNextStep("3");
                        return;
                    }else {
                        onTradeFailed(null, payResult, "查询超时");
                        return;
                    }
                }else if(PayResult.F.getCode().equals(payResult)){//F("F", "失败")
                    onTradeFailed(null, payResult, PayResult.F.getDes());
                    return;
                }else if(PayResult.O.getCode().equals(payResult)){//O("O", "交易关闭")
                    onTradeFailed(null, payResult, PayResult.O.getDes());
                    return;
                }else if(PayResult.S.getCode().equals(payResult)){//S("S", "交易成功")
                    logger.debug("交易成功");
                    transDatas.put(JsonKey.pay_time, data.getBody().getResponse().getResult().getPay_time());
                }else {//payResult为空，银行通道异常
                    onTradeFailed(null, payResult, PayResult.F.getDes());
                    return;
                }
            }else {
                onTradeFailed(null, "-1", PayResult.F.getDes());
                return;
            }
        }
        if(SALE_SCAN_VOID.equals(transCode)||SALE_SCAN_VOID_QUERY.equals(transCode)){//银联-订单撤销完成后后，需要查询交易状态
            ScanVoidBean data = (ScanVoidBean) returnData.get(JsonKey.returnData);
            String revokeResult = data.getBody().getResponse().getResult().getRevoke_result();
            if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
                curTradeInfo = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
                curTradeInfo.setCardNo(revokeResult);
                tradeDao.update(curTradeInfo);
            }
            if(ScanRefundResult.R.getCode().equals(revokeResult)||TextUtils.isEmpty(revokeResult)||"null".equals(revokeResult)){
                //订单查询中的撤销如果交易状态为R，则不再继续查询
                if(getTransData().get(JsonKey.QUERY_FLAG)!=null){
                    onTradeFailed(null, revokeResult, ScanRefundResult.R.getDes());
                    return;
                }
                if(checkNum < CHECK_MAX_NUM) {
                    checkNum++;
                    logger.debug("扫码撤销状态查询次数：" + checkNum);
                    mTradeInformation.setTransCode(SALE_SCAN_VOID_QUERY);
                    transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
                    gotoNextStep("3");
                    return;
                }else {
                    onTradeFailed(null, revokeResult, "查询超时");
                    return;
                }
            }else if(ScanRefundResult.S.getCode().equals(revokeResult)){
                logger.debug("交易成功");
            }else if(ScanRefundResult.F.getCode().equals(revokeResult)){
                onTradeFailed(null, revokeResult, ScanRefundResult.F.getDes());
                return;
            }else {
                onTradeFailed(null, revokeResult, ScanRefundResult.F.getDes());
                return;
            }
        }
        if(SALE_SCAN_REFUND.equals(transCode)||SALE_SCAN_REFUND_QUERY.equals(transCode)){
            ScanRefundBean data = (ScanRefundBean) returnData.get(JsonKey.returnData);
            String refundResult = data.getBody().getResponse().getResult().getRefund_result();
            if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
                curTradeInfo = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
                curTradeInfo.setCardNo(refundResult);
                curTradeInfo.setUnicom_scna_type(data.getBody().getResponse().getResult().getPay_type());
                transDatas.put(JsonKey.pay_type, data.getBody().getResponse().getResult().getPay_type());
                tradeDao.update(curTradeInfo);
            }
            if(ScanRefundResult.R.getCode().equals(refundResult)||TextUtils.isEmpty(refundResult)||"null".equals(refundResult)){
                //订单查询中的退货如果交易状态为R，则不再继续查询
                if(getTransData().get(JsonKey.QUERY_FLAG)!=null){
                    onTradeFailed(null, refundResult, ScanRefundResult.R.getDes());
                    return;
                }
                if(checkNum < CHECK_MAX_NUM) {
                    checkNum++;
                    logger.debug("退货状态查询次数：" + checkNum);
                    mTradeInformation.setTransCode(SALE_SCAN_REFUND_QUERY);
                    transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
                    transDatas.put(JsonKey.mer_refund_order_no, data.getBody().getResponse().getResult().getMer_refund_order_no());
                    gotoNextStep("3");
                    return;
                }else {
                    onTradeFailed(null, refundResult, "查询超时");
                    return;
                }
            }else if(ScanRefundResult.S.getCode().equals(refundResult)){
                transDatas.put(JsonKey.refund_result, ScanRefundResult.S.getDes());
            }else if(ScanRefundResult.F.getCode().equals(refundResult)){
                onTradeFailed(null, refundResult, ScanRefundResult.F.getDes());
                return;
            }else {
                onTradeFailed(null, refundResult, ScanRefundResult.F.getDes());
                return;
            }
            transDatas.put(JsonKey.mer_refund_order_no, data.getBody().getResponse().getResult().getMer_refund_order_no());
            transDatas.put(JsonKey.mer_order_no, data.getBody().getResponse().getResult().getMer_order_no());
        }else if(TextUtils.equals(SALE_PROPERTY, transCode)){
            PropertyBean data = (PropertyBean) returnData.get(JsonKey.returnData);
            if("S".equals(data.getBody().getResponse().getResult().getResult())){
                logger.info("物业下单成功");
                //添加交易结果回调标志
                transDatas.put(JsonKey.PROPERTY_FLAG, JsonKey.PROPERTY_FLAG);
                //保存交易通知回调地址
                BusinessConfig.getInstance().setValue(EposApplication.getAppContext(), BusinessConfig.Key.WY_NOTICE_ADDRESS, (String) transDatas.get(JsonKey.notice_url));
                logger.error(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.WY_NOTICE_ADDRESS));
                gotoNextStep("4");
                return;
            }else {
                onTradeFailed(null, data.getBody().getResponse().getResult().getResult(), data.getBody().getResponse().getResult().getResult_desc());
                return;
            }
        }

        checkNum = 0;
        addLocalInfo(transDatas);
        combinMap.putAll(transDatas);
        combinMap.putAll(returnData);
        combinMap.putAll(mTradeInformation.getDataMap());

        curTradeInfo = new TradeInfoRecord(transCode, combinMap);
        tempMap.putAll(convertObject2String(returnData));
        putResponseCode((String) returnData.get(TradeInformationTag.RESPONSE_CODE), null);

        //**********************判断是否是需要导入联机响应数据的交易*********************//
        if (isImportOnlineRespTrade()) {
            //需要导入联机响应数据
            String iso55 = (String) returnData.get(TradeInformationTag.IC_DATA);
            if (TextUtils.isEmpty(iso55)) {
                logger.warn("^_^ 55域数据为空 ^_^");
                mTradeInformation.getPbocService().abortProcess();
                hasImportOnlineResp = true;
                onPbocTradeApproved();
                return;
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
                gotoNextStep();
            }
        } else {
            String tradeIndex = (String) transDatas.get(TradeInformationTag.TRACE_NUMBER);
            //********************更新冲正表信息***************************//
            if (CAUSE_REVERSE_SETS.contains(msgTag)) {
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
                        originInfo.setStateFlag(1);//已撤销
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
                BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig.Key
                        .FLAG_SIGN_IN, false);
                BusinessConfig.getInstance().setFlag(mTradeView.getHostActivity(), BusinessConfig.Key
                        .KEY_IS_BATCH_BUT_NOT_OUT, false);
                BusinessConfig.getInstance().setNumber(mTradeView.getHostActivity(), BusinessConfig.Key
                        .KEY_POS_SERIAL, 1);
                mTradeView.popToast(com.centerm.epos.R.string.tip_sign_out);
                GetTransData.clearAllData();
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
                logger.warn(tradeIndex + "==>交易类型==>该交易无需更新数据库");
            }
            //**********************跳转界面****************************//
            if (Settings.isSupportElesign()) {
                //支持电子签名跳转到电子签名页面
                switch (transCode) {
                    case SALE:
                    case AUTH:
                    case VOID:
                    case REFUND:
                    case COMPLETE_VOID:
                    case AUTH_COMPLETE:
                    case CANCEL:
                        boolean[] qpsCondition = mTradeView.getHostActivity().getQpsCondition();
                        if (qpsCondition[1]) {//免签
                            gotoNextStep("2");
                        } else {
                            gotoNextStep("1");
                        }
                        break;
                    case SALE_SCAN:
                    case SALE_SCAN_QUERY:
                    case SALE_SCAN_VOID:
                    case SALE_SCAN_VOID_QUERY:
                    case SALE_SCAN_REFUND:
                    case SALE_SCAN_REFUND_QUERY:
                        gotoNextStep("99");
                        break;
                    default:
                        gotoNextStep();
                        break;
                }
            } else {
                //不支持电子签名跳转到结果页面
                gotoNextStep("99");
            }
        }
    }

    /**
     * 添加本地存储的参数信息
     *
     * @param dataMap 交易记录
     */
    protected void addLocalInfo(Map<String, Object> dataMap) {
        dataMap.put(TradeInformationTag.MERCHANT_NAME, BusinessConfig.getInstance().getValue(EposApplication
                .getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME));
        String time = (String) transDatas.get(JsonKey.sendTime);
        dataMap.put(TradeInformationTag.TRANS_YEAR, time.substring(0,4));
        dataMap.put(TradeInformationTag.TRANS_DATE, time.substring(4,8));
        dataMap.put(TradeInformationTag.TRANS_TIME, time.substring(8,14));
    }

    protected TradeInfoRecord refreshData(TradeInfoRecord record){
        if(transCode.contains("SCAN")){
            //保存电银流水号
            curTradeInfo.setReferenceNo((String) transDatas.get(TradeInformationTag.REFERENCE_NUMBER));
            curTradeInfo.setCardNo((String) transDatas.get(TradeInformationTag.BANK_CARD_NUM));
        }
        if(transCode.equals(com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND)
                ||transCode.equals(com.centerm.epos.ebi.common.TransCode.SALE_SCAN_REFUND_QUERY)){
            curTradeInfo.setScanVoucherNo((String) transDatas.get(JsonKey.mer_refund_order_no));
            //原订单号存放在referenceNo中
            curTradeInfo.setReferenceNo((String) transDatas.get(JsonKey.mer_order_no));
        }

        //统一使用平台下发时间
        if(transDatas.get(JsonKey.sendTime)!=null&&transDatas.get(JsonKey.sendTime).toString().length()==14){
            curTradeInfo.setTransTime(((String)transDatas.get(JsonKey.sendTime)).substring(8));
        }
        return record;
    }

    /**
     * 存储交易结果相关记录
     */
    protected boolean storeTradeResult() {
        boolean dbResult;

        if(transDatas.get(TradeInformationTag.TRACE_NUMBER)!=null && tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER))!=null){
            TradeInfoRecord record = tradeDao.queryForId(transDatas.get(TradeInformationTag.TRACE_NUMBER));
            //保存原凭证号
            if(!TextUtils.isEmpty(curTradeInfo.getOriVoucherNum())){
                record.setOriVoucherNum(curTradeInfo.getOriVoucherNum());
            }
            curTradeInfo = record;
            curTradeInfo = refreshData(curTradeInfo);
            dbResult = tradeDao.update(curTradeInfo);
            logger.equals("update: "+curTradeInfo.toString());
        }else {
            dbResult = tradeDao.save(curTradeInfo);
            logger.equals("save: "+curTradeInfo.toString());
        }
        if(!getTradeCode().contains("SCAN")) {
            saveIcData(curTradeInfo.getPbocDetail() == null ? null : curTradeInfo.getPbocDetail().convert2Map());
        }
        if (TransCode.SALE_INSTALLMENT.equals(getTradeCode()))
            saveInstallmentData(mTradeInformation.getRespDataMap());
        //保存特定项目中需要记录的额外信息
        ISaveExtInfo saveExtInfo = (ISaveExtInfo) ConfigureManager.getProjectClassInstance(SaveExtInfoImpl.class);
        if (saveExtInfo != null)
            saveExtInfo.save(transDatas);

        checkTradeStorage();
        //保存用于上送到自建平台的交易记录
        new Thread() {
            @Override
            public void run() {
                TradeRecordForUpload tradeRecordForUpload = new TradeRecordForUpload(curTradeInfo);
                try {
                    tradeRecordForUpload.setTermSn(DeviceFactory.getInstance().getSystemDev().getTerminalSn());
                    tradeRecordForUpload.setTermType(Build.MODEL);
                    final Uri RECORD_ADD_URI = Uri.parse("content://com.centerm.epos.provider.trade/record");
                    EposApplication.getAppContext().getContentResolver().insert(RECORD_ADD_URI, tradeRecordForUpload
                            .convert2ContentValues());
                    EposApplication.getAppContext().getContentResolver().notifyChange(RECORD_ADD_URI, null);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
        }.start();
        return dbResult;
    }

}

