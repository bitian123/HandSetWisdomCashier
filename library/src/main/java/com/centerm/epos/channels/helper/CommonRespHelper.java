package com.centerm.epos.channels.helper;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.bean.transcation.BalancAmount;
import com.centerm.epos.bean.transcation.RequestMessage;
import com.centerm.epos.common.ISORespCode;
import com.centerm.epos.common.PrinterParamEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.present.transaction.TradingPresent;
import com.centerm.epos.redevelop.ActionForResultImpl;
import com.centerm.epos.redevelop.IActionForResult;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.transcation.pos.constant.TradeTempInfoTag;
import com.centerm.epos.transcation.pos.constant.TranscationConstant;
import com.centerm.epos.utils.ViewUtils;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.centerm.epos.common.TransCode.BALANCE;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_VERIFY;
import static com.centerm.epos.common.TransCode.NOTIFY_TRADE_SETS;
import static com.centerm.epos.common.TransCode.SALE;
import static com.centerm.epos.common.TransCode.SETTLEMENT;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_BALANCE;
import static com.centerm.epos.common.TransDataKey.keyBalanceAmt;

/**
 * 报文解析帮助类，此类只针对不同的渠道、不同的返回数据做解析，不做UI层处理和业务处理
 * UI层和业务层处理直接透传到上层{@link }进行。
 * author:wanliang527</br>
 * date:2016/10/29</br>
 */

public class CommonRespHelper extends BaseRespHelper {
    private Logger logger = Logger.getLogger(CommonRespHelper.class);
    private Context context;
    private Map<String, String> stringMap = new HashMap<>();

    public CommonRespHelper(Context context, String transCode) {
        super(transCode);
        this.context = context;
        initParamTip();
    }

    public void setTransCode(String transCode) {
        super.setTransCode(transCode);
    }

    @Override
    public void onRespSuccess(Object present, Map<String, Object> data) {
        TradingPresent activity = (TradingPresent) present;
        String respCode = (String) data.get(TradeInformationTag.RESPONSE_CODE);
        String iso11 = (String) data.get(TradeInformationTag.TRACE_NUMBER);

        //添加二次开发接口，子项目处理定制需求
        new ActionForResultImpl().doAction(activity.getTransData(), data);
        IActionForResult actionForResult = (IActionForResult) ConfigureManager.getProjectClassInstance
                (ActionForResultImpl.class);
        if (actionForResult != null)
            actionForResult.doAction(activity.getTransData(), data);

        if ("00".equals(respCode) || "10".equals(respCode) || "11".equals(respCode) || "A2".equals(respCode) || "A4"
                .equals(respCode) || "A5".equals(respCode) || "A6".equals(respCode)) {

            if (NOTIFY_TRADE_SETS.contains(getTransCode())) {
                String requestMsg = (String) data.get(TradeTempInfoTag.REQUEST_MSG);
                //saveNotifyTradeMsg((String) data.get(TradeInformationTag.TRACE_NUMBER), requestMsg);
                /*
                * 流水号需要从发送包中获取
                * */
                saveNotifyTradeMsg( (String)activity.getTransData().get(TradeInformationTag.TRACE_NUMBER), requestMsg );
            }
            /*
            * 根据规范要求凡是有返回余额的交易需要显示或者打印余额值,
            * 目前60.6域配置是0，不会出现部分扣款和非查询类的交易返回余额值
            * @author zhouzhihua
            * */
            BalancAmount balanceAmount = (BalancAmount) data.get(TradeInformationTag.BALANC_AMOUNT);
            if( null != balanceAmount){
                data.put(keyBalanceAmt,
                        //字符C为正值，其它为负值
                        (TranscationConstant.BALANCE_POSITIVE_CHAR == balanceAmount.getAmountSign() ? "" : "—") +
                                String.format(Locale.CHINA, "%d.%02d", balanceAmount.getAmount() / 100, balanceAmount
                                        .getAmount() % 100));
            }
            /*
            * 圈存类交易正常应答，如果 没有55域则交易失败
            * */
            if( activity.bIsEcLoadTrans() ){
                String resIso55 = (String)data.get(TradeInformationTag.IC_DATA);
                if(  resIso55 == null || resIso55.length() == 0 ){
                    activity.onTradeFailed(null, StatusCode.IC_PROCESS_ERROR);
                    return ;
                }
            }
            //请求成功后，保留结算商户名称，结算商户号，结算终端号
            switch (getTransCode()) {
                case BALANCE:
                case UNION_INTEGRAL_BALANCE:
                    BalancAmount balance = (BalancAmount) data.get(TradeInformationTag.BALANC_AMOUNT);
                    data.put(keyBalanceAmt,
                            //字符C为正值，其它为负值
                            (TranscationConstant.BALANCE_POSITIVE_CHAR == balance.getAmountSign() ? "" : "—") +
                                    String.format(Locale.CHINA, "%d.%02d", balance.getAmount() / 100, balance
                                            .getAmount() % 100));
                    activity.onTradeSuccess(data);
                    break;
                case SALE:
                    activity.onTradeSuccess(data);
                    break;
                case SETTLEMENT:
                    dealWithSettlement(data);
                    break;
                default:
                    activity.onTradeSuccess(data);
                    break;
            }
        } else if ("H9".equals(respCode) && TransCode.SCAN_PAY.equals(getTransCode())) {
            activity.getTransData().putAll(data);
            EventBus.getDefault().post(new SimpleMessageEvent<String>(TradeMessage.QUERY_SCAN_PAY));
        } else if ("98".equals(respCode) && TransCode.MAG_CASH_LOAD.equals(getTransCode())) {
            activity.getTransData().putAll(data);
            logger.warn("MAG_CASH_LOAD come here TradeMessage.MAG_LOAD_CONFIRM" );
            ISORespCode code = ISORespCode.codeMap(respCode);
            /*
            * 先将错误码 设置，防止异常，跳到结果界面 交易成功
            * */
            activity.putResponseCode(code.getCode(), getString(this.context,code.getResId()));
            EventBus.getDefault().post(new SimpleMessageEvent<String>(TradeMessage.MAG_LOAD_CONFIRM));
        } else if (null == respCode) {
            dealWithSettlement(data);
        } else {
            logger.warn("交易失败，后台返回码：" + respCode);
            ISORespCode code = ISORespCode.codeMap(respCode);
            activity.onTradeFailed(iso11, code);
        }
    }

    private void dealWithSettlement(Map<String, Object> data) {
        String amountResp = (String) data.get(TradeInformationTag.SETTLEMENT_RESULT);
        if (null != amountResp && !"".equals(amountResp)) {
            //批结算请求返回成功时，要设置参数状态
            Settings.setValue(context, Settings.KEY.BATCH_SEND_RETURN_DATA, amountResp);
            Settings.setValue(context, Settings.KEY.BATCH_SEND_STATUS, "1");
//                activity.onAccountCheckSuccess(amountResp);
            EventBus.getDefault().post(new SimpleMessageEvent<>(TradeMessage.BATCH_CHECK_SUCCESS, "批结算成功",
                    amountResp));
        } else {
            logger.error("批结算返回的48域对账情况为空！");
            activityStack.pop();
            ViewUtils.showToast(context, "批结算请求失败，请重试！");
        }
    }

    /**
     * 保存通知交易的发送报文，用于批上送时使用
     *
     * @param tradeIndex 终端交易流水号
     * @param requestMsg 发送报文
     */
    private void saveNotifyTradeMsg(String tradeIndex, String requestMsg) {
        if (TextUtils.isEmpty(tradeIndex) || TextUtils.isEmpty(requestMsg))
            return;
        RequestMessage requestMessage = new RequestMessage(tradeIndex, requestMsg);
        DbHelper dbHelper = OpenHelperManager.getHelper(EposApplication.getAppContext(), DbHelper.class);
        CommonDao<RequestMessage> reqMsgDao;
        reqMsgDao = new CommonDao<>(RequestMessage.class, dbHelper);
        reqMsgDao.save(requestMessage);
    }


    @Override
    public void onRespFailed(Object present, String statusCode, String msg) {
        TradingPresent activity = (TradingPresent) present;
        activity.onTradeFailed(null, statusCode, msg);
    }

    /**
     * 对比签购单版本，如果版本不一致，获取最新版本，保存到数据库，并且保留最新版本号
     */
    private void compareAndSaveVersion(String version, String slipVersion) {
        String localVersion = Settings.getSlipVersion(context);
        if (!localVersion.equals(version)) {
            List<PrinterItem> printerItems = new ArrayList<>();
            String[] strings = slipVersion.split("(?=9F..)");
            logger.warn("解析结果==>" + strings.length);
            try {
                for (int i = 1; i < strings.length; i++) {
                    logger.warn(strings[i]);
                    String paramId = strings[i].substring(0, 4);
                    String paramValue = new String(HexUtils.hexStringToByte(strings[i].substring(6, strings[i].length
                            () - 4)), "GBK");
                    String textSize = strings[i].substring(strings[i].length() - 4, strings[i].length() - 2);
                    String range = strings[i].substring(strings[i].length() - 2, strings[i].length());
                    PrinterItem printerItem = new PrinterItem(stringMap.get(paramId), paramValue, paramId, Integer
                            .parseInt(textSize), Integer.parseInt(range));
                    printerItems.add(printerItem);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            CommonDao commonDao = new CommonDao(PrinterItem.class, DbHelper.getInstance());
            Settings.setSlipVersion(context, version);
            boolean isDel = commonDao.deleteByWhere(null);
            if (isDel) {
                logger.debug("签购单表清空成功！");
                boolean issave = commonDao.save(printerItems);
                if (issave) {
                    logger.debug("新版本签购单保存成功！");
                }
            }
            DbHelper.releaseInstance();
        }
    }


    public void initParamTip() {
        stringMap.clear();
        stringMap.put(PrinterParamEnum.SHOP_HEADER.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_HEADER.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NAME.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NAME.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NUM.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_TERM_NUM.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_TERM_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamId(), context.getResources().getString
                (PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamId(), context.getResources().getString
                (PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_CARD_NUM.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_CARD_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_BATCH_NUM.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_BATCH_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamId(), context.getResources().getString
                (PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_PERMISION_CODE.getParamId(), context.getResources().getString
                (PrinterParamEnum.SHOP_PERMISION_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_REFERENCE_CODE.getParamId(), context.getResources().getString
                (PrinterParamEnum.SHOP_REFERENCE_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_DATE_TIME.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_DATE_TIME.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_AMOUNT.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_AMOUNT.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_COMMENT.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_COMMENT.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_DESCRIBE.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_DESCRIBE.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED1.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NOT_USED1.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED2.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NOT_USED2.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED3.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NOT_USED3.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED4.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NOT_USED4.getParamTip()));
        stringMap.put(PrinterParamEnum.SHOP_NOT_USED5.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_NOT_USED5.getParamTip()));

        stringMap.put(PrinterParamEnum.PERSON_HEADER.getParamId(), context.getResources().getString(PrinterParamEnum
                .SHOP_HEADER.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NAME.getParamId(), context.getResources().getString(PrinterParamEnum
                .PERSON_NAME.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NUM.getParamId(), context.getResources().getString(PrinterParamEnum
                .PERSON_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_TERM_NUM.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_TERM_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_CARD_NUM.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_CARD_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_BATCH_NUM.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_BATCH_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_PERMISION_CODE.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_PERMISION_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_REFERENCE_CODE.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_REFERENCE_CODE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_DATE_TIME.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_DATE_TIME.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_AMOUNT.getParamId(), context.getResources().getString(PrinterParamEnum
                .PERSON_AMOUNT.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_COMMENT.getParamId(), context.getResources().getString(PrinterParamEnum
                .PERSON_COMMENT.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_DESCRIBE.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_DESCRIBE.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED1.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_NOT_USED1.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED2.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_NOT_USED2.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED3.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_NOT_USED3.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED4.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_NOT_USED4.getParamTip()));
        stringMap.put(PrinterParamEnum.PERSON_NOT_USED5.getParamId(), context.getResources().getString
                (PrinterParamEnum.PERSON_NOT_USED5.getParamTip()));
    }
}
