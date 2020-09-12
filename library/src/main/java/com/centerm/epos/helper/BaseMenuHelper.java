package com.centerm.epos.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.SecurityModifyPwdActivity;
import com.centerm.epos.activity.msn.BaseAppUpgradeSettingActivity;
import com.centerm.epos.activity.msn.BaseCommunicationSettingsActivity;
import com.centerm.epos.activity.msn.BaseECashExceptionSettingsActivity;
import com.centerm.epos.activity.msn.BaseMerchantSettingsActivity;
import com.centerm.epos.activity.msn.BaseModifyPwdActivity;
import com.centerm.epos.activity.msn.BaseNetSettingsActivity;
import com.centerm.epos.activity.msn.BasePrintSettingsActivity;
import com.centerm.epos.activity.msn.BaseQueryOperatorActivity;
import com.centerm.epos.activity.msn.BaseQueryTradeActivity;
import com.centerm.epos.activity.msn.BaseSystemSettingsActivity;
import com.centerm.epos.activity.msn.BaseTMKByICActivity;
import com.centerm.epos.activity.msn.BaseTMKSettingsActivity;
import com.centerm.epos.activity.msn.BaseTradeOtherControlActivity;
import com.centerm.epos.activity.msn.BaseTradeSettingsActivity;
import com.centerm.epos.activity.msn.BaseTradeSummaryActivity;
import com.centerm.epos.activity.msn.ElectronicSignatureSettingsActivity;
import com.centerm.epos.activity.msn.LogUploadActivity;
import com.centerm.epos.activity.msn.ShowAppVersionActivity;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.event.MessageCode;
import com.centerm.epos.event.PrinteEvent;
import com.centerm.epos.event.SimpleMessageEvent;
import com.centerm.epos.event.TradeMessage;
import com.centerm.epos.mvp.presenter.IMenuPresenter;
import com.centerm.epos.mvp.tag.LocalFunctionTags;
import com.centerm.epos.task.AsyncBatchSettleDown;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.xml.bean.menu.MenuItem;
import com.centerm.epos.xml.keys.MenuTag;

import org.apache.log4j.Logger;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.xml.keys.MenuTag.PRINT_LAST;

/**
 * author:wanliang527</br>
 * date:2016/11/11</br>
 */
public class BaseMenuHelper implements IMenuHelper {

    protected Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    @Override
    public boolean onTriggerMenuItem(IMenuPresenter presenter, MenuItem item) {
        logger.info(getClass().getSimpleName() + "==>onTriggerMenuItem");
        String tag = item.getEnTag();
        switch (tag) {
            case MenuTag.COMMUNICATION_SETTINGS://通讯设置
                presenter.jumpToActivity(BaseCommunicationSettingsActivity.class);
                return true;
            case MenuTag.NET_SETTINGS://网络设置
                if (BaseNetSettingsActivity.checkEnvironment())
                    presenter.jumpToActivity(BaseNetSettingsActivity.class);
                else
                    ViewUtils.showToast(presenter.getContext(), "请先插入SIM卡！");
                return true;
            case MenuTag.APP_UPGRADE_SETTING:
                presenter.jumpToActivity(BaseAppUpgradeSettingActivity.class);
                return true;
            case MenuTag.MASTER_MODIFY_PWD://修改密码
                Map<String,String> paramMaster = new HashMap<>(1);
                paramMaster.put(BaseModifyPwdActivity.PARAM_TAG, "00");
                presenter.jumpToActivity(BaseModifyPwdActivity.class, paramMaster);
                return true;
            case MenuTag.ADMIN_MODIFY_PWD://管理员修改密码
                Map<String,String> paramAdmin = new HashMap<>(1);
                paramAdmin.put(BaseModifyPwdActivity.PARAM_TAG, "99");
                presenter.jumpToActivity(BaseModifyPwdActivity.class, paramAdmin);
                return true;
            case MenuTag.OPERATOR_MANAGE://操作员管理
                presenter.jumpToActivity(BaseQueryOperatorActivity.class);
                return true;
            case MenuTag.MERCHANT_SETTINGS://商户设置
                presenter.jumpToActivity(BaseMerchantSettingsActivity.class);
                return true;
            case MenuTag.SYSTEM_SETTINGS://系统设置
                presenter.jumpToActivity(BaseSystemSettingsActivity.class);
                return true;
            case MenuTag.TRADE_PARAMES://交易设置
                presenter.jumpToActivity(BaseTradeSettingsActivity.class);
                return true;
            case MenuTag.CLEAR_TRADE_RECORDS://清除交易流水
                presenter.doClearTradeRecords();
                return true;
            case MenuTag.TRADE_OTHER_CONTROL://其它交易参数设置
                presenter.jumpToActivity(BaseTradeOtherControlActivity.class);
                return true;
            case MenuTag.TOGGLE_AUTH:
            case MenuTag.TOGGLE_AUTH_COMPLETE:
            case MenuTag.TOGGLE_CANCEL:
            case MenuTag.TOGGLE_COMPLETE_VOID:
            case MenuTag.TOGGLE_REFUND:
            case MenuTag.TOGGLE_BALANCE:
            case MenuTag.TOGGLE_SALE:
            case MenuTag.TOGGLE_VOID:
            case MenuTag.TOGGLE_ECASH:
            case MenuTag.TOGGLE_COMPLETE_VOID_CHECKCARD:
            case MenuTag.TOGGLE_AUTH_COMPLETE_CHECKCARD:
            case MenuTag.TOGGLE_VOID_CHECKCARD:
            case MenuTag.TOGGLE_VOID_INPUTWD:
            case MenuTag.TOGGLE_AUTH_VOID_INPUTWD:
            case MenuTag.TOGGLE_AUTH_COMPLETE_INPUTWD:
            case MenuTag.TOGGLE_COMPLETE_VOID_INPUTWD:
            case MenuTag.TRACKSWTICH:
            case MenuTag.TOGGLE_REFUND_INPUTWD:
            case MenuTag.TOGGLE_KEEP_CONNECT_ALIVE:
            case MenuTag.TOGGLE_TRACK_ENCRYPT:
            case MenuTag.TOGGLE_TIP_SUPPORT:
            case MenuTag.TOGGLE_REVERSE_NOW:
            case MenuTag.TOGGLE_SLIP_ENGLISH:
            case MenuTag.TOGGLE_BATCH_UPLOAD:
            case MenuTag.TOGGLE_SYNC_TIME:
            case MenuTag.TOGGLE_EMV_SM:
            case MenuTag.TOGGLE_OFFLINE_AUTH: //离线交易 小额代授权
            case MenuTag.TOGGLE_OFFLINE_SETTLEMENT:
            case MenuTag.TOGGLE_OFFLINE_ADJUST:
            case MenuTag.TOGGLE_E_COMMON              :
            case MenuTag.TOGGLE_E_QUICK               :
            case MenuTag.TOGGLE_EC_LOAD_INNER         :
            case MenuTag.TOGGLE_EC_LOAD_OUTER         :
            case MenuTag.TOGGLE_EC_LOAD_CASH          :
            case MenuTag.TOGGLE_EC_VOID_CASH_LOAD     :
            case MenuTag.TOGGLE_E_REFUND              :
            case MenuTag.TOGGLE_SALE_INSTALLMENT      :
            case MenuTag.TOGGLE_VOID_INSTALLMENT      :
            case MenuTag.TOGGLE_UNION_INTEGRAL_SALE   :
            case MenuTag.TOGGLE_ISS_INTEGRAL_SALE     :
            case MenuTag.TOGGLE_UNION_INTEGRAL_VOID   :
            case MenuTag.TOGGLE_ISS_INTEGRAL_VOID     :
            case MenuTag.TOGGLE_UNION_INTEGRAL_BALANCE:
            case MenuTag.TOGGLE_UNION_INTEGRAL_REFUND :
            case MenuTag.TOGGLE_RESERVATION_SALE      :
            case MenuTag.TOGGLE_RESERVATION_VOID      :
            case MenuTag.TOGGLE_MOTO_SALE             :
            case MenuTag.TOGGLE_MOTO_VOID             :
            case MenuTag.TOGGLE_MOTO_REFUND           :
            case MenuTag.TOGGLE_MOTO_AUTH             :
            case MenuTag.TOGGLE_MOTO_CANCEL           :
            case MenuTag.TOGGLE_MOTO_AUTH_COMPLETE    :
            case MenuTag.TOGGLE_MOTO_AUTH_SETTLEMENT  :
            case MenuTag.TOGGLE_MAG_ACCOUNT_VERIFY    :
            case MenuTag.TOGGLE_MAG_ACCOUNT_LOAD_VERIFY:
            case MenuTag.TOGGLE_ECASH_MENU:
            case MenuTag.TOGGLE_SALE_PROPERTY:
                boolean value = presenter.getBizFlag(tag);
                presenter.setBizFlag(tag, !value);
                return true;
            case MenuTag.MANUAL_SET_TMK:    //手工设置主密钥
                presenter.jumpToActivity(BaseTMKSettingsActivity.class);
                return true;
            case MenuTag.SECURITY_MODIFY_PWD:
                presenter.jumpToActivity(SecurityModifyPwdActivity.class);
                return true;
            case MenuTag.IC_IMPORT_TMK:     //IC卡导入主密钥
                Bundle paramIC = new Bundle();
                paramIC.putBoolean(BaseTMKByICActivity.IS_MANUAL_INPUT_TMK, false);
                presenter.jumpToActivity(BaseTMKByICActivity.class, paramIC);
                return true;
            case MenuTag.MANUAL_IC_IMPORT_TMK:
                Bundle param = new Bundle();
                param.putBoolean(BaseTMKByICActivity.IS_MANUAL_INPUT_TMK, true);
                presenter.jumpToActivity(BaseTMKByICActivity.class, param);
                return true;
            case MenuTag.ENCRYPT_ALGORITHM:
                presenter.doLocalFunction(LocalFunctionTags.ENCRYPT_ALGORITHM_CONFIG);
                return true;
            case MenuTag.MAIN_KEY_INDEX:
                presenter.doLocalFunction(LocalFunctionTags.MAIN_KEY_INDEX_CONFIG);
                return true;
            case MenuTag.NFC_TRADE_CHANNEL:
                presenter.doLocalFunction(LocalFunctionTags.NFC_TRADE_CHANNEL_CONFIG);
                return true;
            case MenuTag.IMPORT_POS_PARAMETER:
                presenter.doLocalFunction(LocalFunctionTags.IMPORT_TERMINAL_PARAMETER);
                return true;
            case MenuTag.SIGN_SETTINGS:
                presenter.jumpToActivity(ElectronicSignatureSettingsActivity.class);
                return true;
            case MenuTag.ECASH_PARAM_SETTINGS:
                presenter.jumpToActivity(BaseECashExceptionSettingsActivity.class);
                return true;
            case MenuTag.TOGGLE_PREFER_CLSS:
                String key = BusinessConfig.Key.FLAG_PREFER_CLSS;
                boolean flag = presenter.getBizFlag(key);
                presenter.setBizFlag(key, !flag);
                return true;
            case MenuTag.TRADE_SETTLEMENT:
                if (!CommonUtils.tradeEnvironmentCheck(EposApplication.getAppContext())) {
                    logger.debug("^_^ 电池电量低 ^_^");
                    ViewUtils.showToast(EposApplication.getAppContext(), "电量低，请充电！");
                    return true;
                }
                readyToSettleDown(presenter.getContext(), presenter);
                return true;
            case MenuTag.TRADE_QUERY:   //交易查询
                presenter.jumpToActivity(BaseQueryTradeActivity.class);
                return true;
            case MenuTag.LOCK:
                BusinessConfig.getInstance().setFlag(presenter.getContext(), BusinessConfig.Key.KEY_IS_LOCK, true);
                DialogFactory.showLockDialog(presenter.getContext());
                return true;
            case PRINT_LAST:
                EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_SLIP_LAST));
                return true;
            case MenuTag.PRINT_ANY:
                presenter.jumpToActivity(BaseQueryTradeActivity.class);
                return true;
            case MenuTag.PRINT_SUMMARY:
                presenter.jumpToActivity(BaseTradeSummaryActivity.class);
                return true;
            case MenuTag.PRINT_DETAIL:
//                printData(presenter.getContext(), PRINT_DETAIL);
                EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_DETAIL));
                return true;
            case MenuTag.PRINT_BATCH_SUMMARY:
//                printData(presenter.getContext(), PRINT_BATCH_SUMMARY);
                EventBus.getDefault().post(new PrinteEvent(TradeMessage.PRINT_TRADE_SUMMARY));
                return true;
            case MenuTag.SIGN_OUT:
                readyToSignOut(presenter);
                return true;
            case MenuTag.PRINT_SETTING:
                presenter.jumpToActivity(BasePrintSettingsActivity.class);
                return true;
            case MenuTag.RESTORE_CONFIG:
                presenter.restoreAppConfig();
                return true;
            case MenuTag.QPS_PARAM_CONFIG:
                presenter.doLocalFunction(LocalFunctionTags.QPS_PARAM_CONFIG);
                return true;
            case MenuTag.POS_VERSION:
                presenter.jumpToActivity(ShowAppVersionActivity.class);
                return true;
            case MenuTag.OPERATOR_SIGN_IN:
//                BusinessConfig.getInstance().setValue(presenter.getContext(), BusinessConfig.Key.KEY_OPER_ID, null);
                EventBus.getDefault().post(new SimpleMessageEvent<>(MessageCode.SHOW_LOGIN_VIEW));
                return true;
            case MenuTag.UPLOAD_LOG://日志上传
                presenter.jumpToActivity(LogUploadActivity.class);
                return true;
        }
        return false;
    }


    /**
     * 批结算
     */
    private void readyToSettleDown(final Context contextActivity, final IMenuPresenter presenter) {
        String isBatch = Settings.getValue(contextActivity, Settings.KEY.BATCH_SEND_STATUS, "0");
        boolean isSignIn = BusinessConfig.getInstance().getFlag(contextActivity, BusinessConfig.Key
                .FLAG_SIGN_IN);

        if (isSignIn) {
            if ("2".equals(isBatch)) {
                ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                        .tip_batch_over_please_sign_out));
            } else if ("1".equals(isBatch)) {
                presenter.beginOnlineProcess(TransCode.SETTLEMENT);
            }else {
                new AsyncBatchSettleDown(contextActivity) {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        DialogFactory.showLoadingDialog(contextActivity, contextActivity.getString(com.centerm.epos.R
                                .string
                                .tip_query_flow));
                    }

                    @Override
                    public void onFinish(Object o) {
                        super.onFinish(o);
                        if (o instanceof Boolean && (Boolean) o) {
                            DialogFactory.showSelectDialog(contextActivity, contextActivity.getString(com.centerm
                                    .epos.R.string
                                    .tip_notification), contextActivity.getString(com.centerm.epos.R.string
                                    .tip_comfirm_batch), new AlertDialog.ButtonClickListener() {
                                @Override
                                public void onClick(AlertDialog.ButtonType button, View v) {
                                    switch (button) {
                                        case POSITIVE:
//                                            Settings.setValue(contextActivity, Settings.KEY.BATCH_SEND_STATUS, "1");
                                            presenter.beginOnlineProcess(TransCode.SETTLEMENT);
                                            break;
                                    }
                                }
                            });
                        } else {
                            DialogFactory.hideAll();
                            ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                                    .tip_no_trans_flow));
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            ViewUtils.showToast(contextActivity, contextActivity.getString(com.centerm.epos.R.string
                    .tip_no_sign_in_status));
        }
    }

    /**
     * 签退
     */
    private void readyToSignOut(final IMenuPresenter presenter) {
        Context context = presenter.getContext();
        String isBatchSucc = Settings.getValue(context, Settings.KEY.BATCH_SEND_STATUS, "0");
        final boolean isSignIn = BusinessConfig.getInstance().getFlag(context, BusinessConfig.Key
                .FLAG_SIGN_IN);
        if ("2".equals(isBatchSucc)) {
            //fix bug，批结算完成时，必然是已签到状态
//            if (isSignIn) {
                presenter.beginOnlineProcess(TransCode.SIGN_OUT, "online.xml");
//            } else {
//                ViewUtils.showToast(context, "未签到状态，无需签退！");
//            }
        } else if ("0".equals(isBatchSucc)) {
            new AsyncBatchSettleDown(context) {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    DialogFactory.showLoadingDialog(presenter.getContext(), context.getString(R.string
                            .tip_query_flow));
                }

                @Override
                public void onFinish(Object o) {
                    super.onFinish(o);
                    if (o instanceof Boolean && (Boolean) o) {
                        DialogFactory.hideAll();
                        ViewUtils.showToast(presenter.getContext(), "请先完成批结算！");
                    } else {
                        if (isSignIn) {
                            presenter.beginOnlineProcess(TransCode.SIGN_OUT, "online.xml");
                        } else {
                            ViewUtils.showToast(presenter.getContext(), "未签到状态，无需签退！");
                        }
                        DialogFactory.hideAll();
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            ViewUtils.showToast(context, "请先完成批结算！");
        }
    }

}
