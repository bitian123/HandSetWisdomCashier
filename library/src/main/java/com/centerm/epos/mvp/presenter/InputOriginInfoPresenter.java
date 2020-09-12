package com.centerm.epos.mvp.presenter;

import android.text.TextUtils;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.ConstDefine;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.common.TransDataKey;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.mvp.model.IInputOriginInfoBiz;
import com.centerm.epos.mvp.model.InputOriginInfoBiz;
import com.centerm.epos.mvp.view.IInputOriginInfoView;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;

import java.util.Locale;
import java.util.Map;

import static com.centerm.epos.base.BaseActivity.KEY_ORIGIN_INFO;
import static com.centerm.epos.common.TransDataKey.key_oriAuthCode;
import static com.centerm.epos.common.TransDataKey.key_oriReferenceNumber;
import static com.centerm.epos.common.TransDataKey.key_oriTransDate;

/**
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class InputOriginInfoPresenter extends BaseTradePresent implements IInputOriginInfoPresenter {

    protected IInputOriginInfoBiz model;
    protected IInputOriginInfoView view;

    public InputOriginInfoPresenter(IInputOriginInfoView mTradeView) {
        super(mTradeView);
        model = new InputOriginInfoBiz();
        view = mTradeView;
    }
    /*
    *以下交易不能被撤销：
    * 指定账户圈存、非指定账户圈存、快速支付（脱机交易）、普通支付（脱机交易）
    * 电子现金充值撤销
    * */
    private boolean bIsTransUnVoid(String transCode){
        return ( transCode.equals(TransCode.EC_LOAD_OUTER)
                 || transCode.equals(TransCode.EC_LOAD_INNER)
                 || transCode.equals(TransCode.EC_VOID_CASH_LOAD)
                 || transCode.equals(TransCode.E_QUICK)
                 || transCode.equals(TransCode.E_COMMON) );
    }
    @Override
    public boolean onConfirmClicked(String scanVoucher, String posSerial, String platSerial, String date, String
            authCode, String authDate, String cardDate) {
        OriginalMessage originalMessage;
        String transCode = getTradeCode();
        switch (transCode) {
            case TransCode.VOID_SCAN:
            case TransCode.SCAN_VOID: {
                if (TextUtils.isEmpty(scanVoucher)) {
                    view.popToast("请输入扫码付款凭证码");
                    return false;
                }
//                if (scanVoucher.length() < 20) {
//                    view.popToast("长度不足");
//                    return false;
//                }
                TradeInfoRecord oriInfo = model.queryByPosScanVoucherNo(DbHelper.getInstance(), scanVoucher);
                DbHelper.releaseInstance();
                if (oriInfo != null) {
                    logger.debug("原交易信息：" + oriInfo.toString());
                    transDatas.putAll(oriInfo.convert2Map());
                    //清空原交易流水号，报文中不使用原交易流水号
                    transDatas.remove(TradeInformationTag.TRACE_NUMBER);
                    transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "032");//服务点输入码，默认为扫码
                    originalMessage = new OriginalMessage(Long.parseLong(oriInfo.getBatchNo()),
                            Long.parseLong(oriInfo.getVoucherNo()), oriInfo.getTransDate(), oriInfo.getTransTime());
                    transDatas.put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);
                    if ((TransCode.VOID_SCAN.equals(transCode) && !TransCode.SALE_SCAN.equals(oriInfo.getTransType()))
                            || (TransCode.SCAN_VOID.equals(transCode) && !TransCode.SCAN_PAY.equals(oriInfo.getTransType()))
                            || TransCode.EC_VOID_CASH_LOAD.equals(transCode) && !TransCode.EC_LOAD_CASH.equals(oriInfo.getTransType())
                            || TransCode.RESERVATION_VOID.equals(transCode) && !TransCode.RESERVATION_SALE.equals(oriInfo.getTransType())
                            || this.bIsTransUnVoid(oriInfo.getTransType()) ) {
                        view.popToast("该笔交易类型不符");
                    } else if (oriInfo.getStateFlag() == 1) {
                        view.popToast("该交易已撤销");
                    } else {
                        view.getHostActivity().jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                        return true;
                    }
                } else {
                    view.popToast("未找到对应交易流水");
                }
                return false;
            }
            case TransCode.COMPLETE_VOID:
            case TransCode.VOID:
            case TransCode.VOID_INSTALLMENT:
            case TransCode.EC_VOID_CASH_LOAD:
            case TransCode.ISS_INTEGRAL_VOID:
            case TransCode.UNION_INTEGRAL_VOID:
            case TransCode.RESERVATION_VOID:
            case TransCode.OFFLINE_ADJUST:
                if (TextUtils.isEmpty(posSerial)) {
                    view.popToast(R.string.tip_plz_input_voucher);
                    return false;
                }
                if (posSerial.length() != 6) {
                    view.popToast(R.string.tip_warn_voucher_max_len);
                    return false;
                }
                TradeInfoRecord oriInfo = model.queryByPosSerial(DbHelper.getInstance(), posSerial, mapSaleTypeName(transCode));
                DbHelper.releaseInstance();
                if ( oriInfo != null ) {
                    if ( (oriInfo.getStateFlag() == ConstDefine.TRANS_STATE_VOID) || (oriInfo.getStateFlag() == ConstDefine.TRANS_STATE_ADJUST) ) {
                        view.popToast( (oriInfo.getStateFlag() == ConstDefine.TRANS_STATE_VOID) ? R.string.tip_trans_void_yet : R.string.tip_trans_adjust_yet);
                        return false;
                    }

                    logger.debug("原交易信息：" + oriInfo.toString());
                    Map<String, Object> dataMap = mTradeInformation.getTransDatas();
                    dataMap.put(TradeInformationTag.BANK_CARD_NUM, oriInfo.getCardNo());
                    dataMap.put(TradeInformationTag.TRANS_MONEY, oriInfo.getAmount());//金额
                    dataMap.put(TradeInformationTag.SERVICE_ENTRY_MODE,TransCode.RESERVATION_VOID.equals(transCode) ? oriInfo.getServiceEntryMode().substring(0,2):"01");//服务点输入码，默认为手工输入
                    dataMap.put(TradeInformationTag.REFERENCE_NUMBER, oriInfo.getReferenceNo());//检索参考号
                    dataMap.put(TradeInformationTag.AUTHORIZATION_IDENTIFICATION, oriInfo.getAuthorizeNo());//授权标识应答码
                    dataMap.put(TradeInformationTag.TERMINAL_IDENTIFICATION, oriInfo.getTerminalNo());//受卡机终端标识码
                    dataMap.put(TradeInformationTag.MERCHANT_IDENTIFICATION, oriInfo.getMerchantNo());//受卡方标识码
                    dataMap.put(TradeInformationTag.PRIMARYVOUCHERNO, oriInfo.getVoucherNo());//原交易流水号
                    originalMessage = new OriginalMessage(Long.parseLong(oriInfo.getBatchNo()),
                            Long.parseLong(oriInfo.getVoucherNo()), oriInfo.getTransDate(), oriInfo.getTransTime());
                    dataMap.put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);

                    if ( TransCode.VOID.equals(transCode) && !TransCode.SALE.equals(oriInfo.getTransType()) ||
                            (TransCode.COMPLETE_VOID.equals(transCode) && !(TransCode.AUTH_COMPLETE.equals(oriInfo.getTransType())
                                    || TransCode.AUTH_SETTLEMENT.equals(transCode)))
                                    || TransCode.EC_VOID_CASH_LOAD.equals(transCode) && !TransCode.EC_LOAD_CASH.equals(oriInfo.getTransType())
                                    || this.bIsTransUnVoid(oriInfo.getTransType())
                                    || (TransCode.ISS_INTEGRAL_VOID.equals(transCode) && !TransCode.ISS_INTEGRAL_SALE.equals(oriInfo.getTransType()))
                                    || (TransCode.UNION_INTEGRAL_VOID.equals(transCode) && !TransCode.UNION_INTEGRAL_SALE.equals(oriInfo.getTransType()))
                                    || TransCode.RESERVATION_VOID.equals(transCode) && !TransCode.RESERVATION_SALE.equals(oriInfo.getTransType())) {
                        view.popToast("该笔交易类型不符");
                    } else if (oriInfo.getStateFlag() == ConstDefine.TRANS_STATE_VOID) {
                        view.popToast(R.string.tip_trans_void_yet);
                    } else {
                        view.getHostActivity().jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                        return true;
                    }
                } else {
                    view.popToast("未找到对应交易流水或交易类型不符");
                }
                return false;
            case TransCode.REFUND:
            case TransCode.UNION_INTEGRAL_REFUND:
                if (TextUtils.isEmpty(platSerial)) {
                    view.popToast("请输入检索参考号");
                    return false;
                }
                if (platSerial.length() != 12) {
                    view.popToast("检索参考号长度为12");
                    return false;
                }
                if (TextUtils.isEmpty(date)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (date.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }

                mTradeInformation.getTransDatas().put(TradeInformationTag.REFERENCE_NUMBER, platSerial);
                originalMessage = new OriginalMessage(0L, 0L, date);
                mTradeInformation.getTransDatas().put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);
                mTradeInformation.getTransDatas().put(TradeInformationTag.PRIMARYREFERENCENO, platSerial);
                addRemarkInfo(platSerial, date, null);
                gotoNextStep();
                return true;
            case TransCode.REFUND_SCAN:
                if (TextUtils.isEmpty(scanVoucher)) {
                    view.popToast("请输入扫码付款凭证码");
                    return false;
                }
                if (scanVoucher.length() < 20) {
                    view.popToast("凭证码长度不足");
                    return false;
                }
                if (platSerial.length() != 12) {
                    view.popToast("检索参考号长度为12");
                    return false;
                }
                if (TextUtils.isEmpty(date)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (date.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }
                mTradeInformation.getTransDatas().put(TradeInformationTag.REFERENCE_NUMBER, platSerial);
                originalMessage = new OriginalMessage(0L, 0L, date);
                transDatas.put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);
                transDatas.put(TradeInformationTag.SCAN_VOUCHER_NO, scanVoucher);
                transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, "032");//服务点输入码，默认为扫码
                mTradeInformation.getTransDatas().put(TradeInformationTag.REFERENCE_NUMBER, platSerial);
                addRemarkInfo(platSerial, date, null);
                gotoNextStep();
                break;
            case TransCode.AUTH_COMPLETE:
            case TransCode.AUTH_SETTLEMENT:
            case TransCode.CANCEL:
                if (TextUtils.isEmpty(authCode)) {
                    view.popToast("请输入原授权码");
                    return false;
                }
                if (authCode.length() != 6) {
                    view.popToast("原授权码长度为6");
                    return false;
                }
                if (TextUtils.isEmpty(authDate)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (authDate.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }
                if (isInputCardValideDate()) {
                    if (TextUtils.isEmpty(cardDate)) {
                        view.popToast("请输入卡有效期");
                        return false;
                    }
                    if (cardDate.length() != 4) {
                        view.popToast("有效期长度为4");
                        return false;
                    }
                    transDatas.put(TradeInformationTag.DATE_EXPIRED, cardDate);
                }
                originalMessage = new OriginalMessage(0L, 0L, authDate);
                mTradeInformation.getTransDatas().put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);
                mTradeInformation.getTransDatas().put(TradeInformationTag.AUTHORIZATION_IDENTIFICATION, authCode);
                addRemarkInfo(null, null, authCode);
                gotoNextStep();
                return true;

            case TransCode.E_REFUND:
                if (TextUtils.isEmpty(posSerial)) {
                    view.popToast("请输入凭证号");
                    return false;
                }
                if (posSerial.length() != 6) {
                    view.popToast("凭证号长度为6");
                    return false;
                }

                if (TextUtils.isEmpty(platSerial)) {
                    view.popToast("请输入批次号");
                    return false;
                }
                if (platSerial.length() != 6) {
                    view.popToast("批次号长度为6");
                    return false;
                }

                if (TextUtils.isEmpty(date)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (date.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }
                if (TextUtils.isEmpty(scanVoucher)) {
                    view.popToast("请输入原终端号");
                    return false;
                }
                if (scanVoucher.length() != 8 ) {
                    view.popToast("原终端号长度为8");
                    return false;
                }
                originalMessage = new OriginalMessage(Long.parseLong(platSerial),Long.parseLong(posSerial),date);

                originalMessage.setTermNo(scanVoucher);

                mTradeInformation.getTransDatas().put(TradeInformationTag.ORIGINAL_MESSAGE, originalMessage);
                addRemarkInfo(originalMessage);
                gotoNextStep();
                break;
            /*case TransCode.AUTH_COMPLETE:
                if (TextUtils.isEmpty(authCode)) {
                    view.popToast("请输入原授权码");
                    return false;
                }
                if (authCode.length() != 6) {
                    view.popToast("原授权码长度为6");
                    return false;
                }
                if (TextUtils.isEmpty(authDate)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (authDate.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }
                mTradeInformation.getDataMap().put(key_oriTransTime, authDate);
                mTradeInformation.getDataMap().put(key_oriAuthCode, authCode);
                gotoNextStep();
                return true;*/
          /*  case TransCode.AUTH_SETTLEMENT:
                if (TextUtils.isEmpty(authCode)) {
                    view.popToast("请输入原授权码");
                    return false;
                }
                if (authCode.length() != 6) {
                    view.popToast("原授权码长度为6");
                    return false;
                }
                if (TextUtils.isEmpty(authDate)) {
                    view.popToast("请输入交易日期");
                    return false;
                }
                if (authDate.length() != 4) {
                    view.popToast("日期长度为4");
                    return false;
                }
                mTradeInformation.getDataMap().put(key_oriTransTime, authDate);
                mTradeInformation.getDataMap().put(key_oriAuthCode, authCode);
                gotoNextStep();
                return true;*/
          /*  case TransCode.COMPLETE_VOID:
                if (TextUtils.isEmpty(posSerial)) {
                    view.popToast("请输入凭证号");
                    return false;
                }
                if (posSerial.length() != 6) {
                    view.popToast("凭证号长度为6");
                    return false;
                }
                oriInfo = model.queryByPosSerial(posSerial);
                if (oriInfo != null) {
                    view.getHostActivity().jumpToNext(KEY_ORIGIN_INFO, oriInfo);
                } else {
                    view.popToast("无法找到对应交易流水");
                }
                return true;*/
        }
        return false;
    }

    @Override
    public boolean isInputCardValideDate() {
        if (!transDatas.containsKey(TradeInformationTag.IS_CARD_NUM_MANUAL))
            return false;
        return (boolean) transDatas.get(TradeInformationTag.IS_CARD_NUM_MANUAL);
    }

    /**
     * 通过传入的撤销交易名称映射需要撤销的消费交易
     *
     * @param transCode 撤销交易名称
     * @return 消费交易名称
     */
    private String mapSaleTypeName(String transCode) {
        if (TransCode.VOID.equals(transCode))
            return TransCode.SALE;
        if (TransCode.VOID_INSTALLMENT.equals(transCode))
            return TransCode.SALE_INSTALLMENT;
        if (TransCode.COMPLETE_VOID.equals(transCode))
            return TransCode.AUTH_COMPLETE;
        if (TransCode.EC_VOID_CASH_LOAD.equals(transCode))
            return TransCode.EC_LOAD_CASH;
        if (TransCode.ISS_INTEGRAL_VOID.equals(transCode))
            return TransCode.ISS_INTEGRAL_SALE;
        if (TransCode.UNION_INTEGRAL_VOID.equals(transCode))
            return TransCode.UNION_INTEGRAL_SALE;

        return null;
    }

    /**
     * 保存备注信息，用于凭条打印
     *
     * @param platSerial 参数号
     * @param date 日期
     * @param authCode 授权码
     */
    private void addRemarkInfo(String platSerial, String date, String authCode) {
        Map<String, String> dataMap = mTradeInformation.getDataMap();
        if (!TextUtils.isEmpty(platSerial))
            dataMap.put(key_oriReferenceNumber, platSerial);
        if (!TextUtils.isEmpty(date))
            dataMap.put(key_oriTransDate, date);
        if (!TextUtils.isEmpty(authCode))
            dataMap.put(key_oriAuthCode, authCode);
    }
    /*
    * 备注打印信息
    * */
    private void addRemarkInfo(OriginalMessage oriMsg) {
        Map<String, String> dataMap = mTradeInformation.getDataMap();
        if( TransCode.E_REFUND.equals(getTradeCode()) ){
            dataMap.put(TransDataKey.KEY_ORI_TERMINAL_NO,oriMsg.getTermNo());
            dataMap.put(TransDataKey.key_oriVoucherNumber,String.format(Locale.CHINA,"%06d",oriMsg.getTraceNumber()));
            dataMap.put(TransDataKey.KEY_ORI_BATCH_NO,String.format(Locale.CHINA,"%06d",oriMsg.getBatchNumber()));
            dataMap.put(key_oriTransDate, oriMsg.getDate());
        }
    }

}
