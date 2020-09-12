package com.centerm.epos.present.transaction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinListener;
import com.centerm.cpay.midsdk.dev.define.pinpad.PinParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.aidl.pinpad.AidlPinPad;
import com.centerm.smartpos.aidl.pinpad.PinInfo;
import com.centerm.smartpos.aidl.pinpad.PinPadBuilder;
import com.centerm.smartpos.aidl.pinpad.PinPadInputPinCallback;
import com.centerm.smartpos.aidl.sys.AidlDeviceManager;
import com.centerm.smartpos.constant.Constant;
import com.centerm.smartpos.util.HexUtil;
import com.centerm.smartpos.util.LogUtil;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.FLAG_IMPORT_PIN;
import static com.centerm.epos.common.TransDataKey.iso_f2;
import static com.centerm.epos.common.TransDataKey.iso_f22;
import static com.centerm.epos.common.TransDataKey.iso_f52;
import static com.centerm.epos.common.TransDataKey.keyFlagNoPin;

/**
 * Created by yuhc on 2017/2/21.
 * 密码输入界面业务逻辑控制
 */

public class InputPasswordPresent extends BaseTradePresent implements IInputPassword {
    private PinParams pinParams;
    private int pinLen;
    private int retryTimes;

    private AidlDeviceManager deviceManager;
    private AidlPinPad curpinPad = null;// 当前选中的密码键盘

    public InputPasswordPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        pinParams = new PinParams();

        XLogUtil.w(this.getClass().getSimpleName(),"getTransCode:"+mTradeInformation.getTransCode());
        if( TransCode.MAG_ACCOUNT_LOAD.equals(mTradeInformation.getTransCode()) ) {
            /*磁条卡账户充值，只支持有pin的卡 .... 暂时没有设置 不允许ByPass的参数，如果没输入密码 那么账户充值 会出现组包错误*/
        }
        if(EncryptAlgorithmEnum.SM4 == Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext()))
            pinParams.setUseSM4(true);

        if(!CommonUtils.isK9()){
            tobindService();
        }else {
            /*预约消费 pin block 是不带主账号的加密 ，默认将卡号设置为16个0*/
            if( TransCode.RESERVATION_SALE.equals(mTradeInformation.getTransCode()) ){
                pinParams.setPan("0000000000000000");
            } else{
                pinParams.setPan((String) mTradeInformation.getTransDatas().get(TradeInformationTag.BANK_CARD_NUM));
            }
        }
    }

    @Override
    public boolean isShowAmount() {
        return !(TransCode.BALANCE.equals(mTradeInformation.getTransCode())||TransCode.UNION_INTEGRAL_BALANCE.equals(mTradeInformation.getTransCode()));
    }

    @Override
    public boolean isShowBankCardNum() {
        return !TransCode.AUTH.equals(mTradeInformation.getTransCode());
    }

    @Override
    public int getPinLen() {
        return pinLen;
    }

    @Override
    public void beginTransaction() {
        delayBeginGetPin();
    }


    private void delayBeginGetPin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mTradeView == null) {
                    logger.warn("^_^ 密码输入界面已被销毁，不再调用输PIN接口 ^_^");
                    return;
                }
                if (!isPbocTerminated()) {
                    beginGetPin();
                } else {
                    logger.warn("交易终止，不进行输PIN操作");
                }
            }
        }, 300);
    }

    private void delayCancelGetPin() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
                if (pinPadDev != null) {
                    pinPadDev.cancelGetPin();
                }
            }
        }, 600);
    }

    private void beginGetPin() {
        IPinPadDev pinPadDev = CommonUtils.getPinPadDev();
        if (pinPadDev != null) {
            pinPadDev.getPin(pinParams, pinListener);
        } else {
            logger.warn("密码键盘获取为空，无法弹出密码键盘");
        }
    }

    private PinListener pinListener = new PinListener() {
        @Override
        public void onReadingPin(int i) {
            pinLen = i;
            mTradeView.refresh();
        }

        @Override
        public void onError(int i, String s) {
        }

        @Override
        public void onConfirm(byte[] bytes) {
            if (pinLen != 0 && pinLen < 4) {
                //密码位数不足4位
                if (retryTimes++ < BusinessConfig.PASSWD_RETRY_TIMES) {
                    mTradeView.popToast(R.string.tip_pwd_length_illegal);
                    beginGetPin();
                } else {
                    //输错次数超限，自动返回到主界面
                    mTradeView.getHostActivity().jumpToMain();
                    mTradeView.popToast(R.string.tip_pwd_times_over_limited);
                }
            } else {
                ((BaseFragment)mTradeView).cancelTimeout();
                //服务点输入方式码
                String entryMode = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);
                if (pinLen == 0) {
                    entryMode += "2";
                    transDatas.put(keyFlagNoPin, "1");
                } else {
                    entryMode += "1";
                    transDatas.put(TradeInformationTag.CUSTOMER_PASSWORD, HexUtils.bytesToHexString(bytes));
                }
                transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, entryMode);
                if (isICInsertTrade() && !"1".equals(transDatas.get(FLAG_IMPORT_PIN))) {
                    mTradeView.getHostActivity().delayJumpToNext();
                } else {
                    mTradeView.getHostActivity().jumpToNext();
                }
            }
        }


        @Override
        public void onCanceled() {
            /*zhouzhihua 在突然拔卡的时候，TradeFragmentContainer收到IC卡终止的广播，会直接跳转到结果界面，
            InputPwdFragment 界面被destory*/
            if( null == mTradeView){
                return ;
            }
            try {
                if (mTradeInformation.getPbocService() != null) {
                    mTradeInformation.getPbocService().abortProcess();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!mTradeView.getHostActivity().isPbocTerminated()) {
                mTradeView.getHostActivity().jumpToMain();
            }
            /*switch (transCode) {
                case TransCode.SALE:
                    //消费业务退货到输入金额界面
                    activityStack.backTo(InputMoneyActivity.class);
                    break;
                default:
                    activityStack.backTo(MainActivity.class);
                    break;
            }*/
        }

        @Override
        public void onTimeout() {
            mTradeView.getHostActivity().jumpToResultActivity(StatusCode.PIN_TIMEOUT);
        }
    };

    public void e10onConfirm(String amt,String cardNo,String pw) throws RemoteException {
        PinInfo pinInfo = new PinInfo((byte) 0,
                PinPadBuilder.DATAENCRYPT_MODE.DEFAULT, cardNo,
                amt, 6, 6, HexUtil.bytesToHexString(pw.getBytes()),
                PinPadBuilder.PIN_ENCRYPT_MODE.MODE_ZERO,
                PinPadBuilder.PIN_CARDCAL.NEED_CARD,
                PinPadBuilder.PIN_INPUT_TIMES.TIMES_ONCE);
        if(curpinPad==null){
            mTradeView.popToast("密码键盘异常，请重试");
            return;
        }
        curpinPad.getPin(pinInfo, new PinPadInputPinCallback.Stub() {
            @Override
            public void onReadingPin(int i, String s) throws RemoteException {

            }

            @Override
            public void onReadPinCancel() throws RemoteException {

            }

            @Override
            public void onReadPinException() throws RemoteException {

            }

            @Override
            public void onReadPinSuccess(byte[] bytes) throws RemoteException {
                logger.info("onReadPinSuccess:"+HexUtil.bytesToHexString(bytes));
                //服务点输入方式码
                String entryMode = (String) transDatas.get(TradeInformationTag.SERVICE_ENTRY_MODE);

                transDatas.put(TradeInformationTag.CUSTOMER_PASSWORD, HexUtil.bytesToHexString(bytes));
                transDatas.put(TradeInformationTag.SERVICE_ENTRY_MODE, entryMode);
                if (isICInsertTrade() && !"1".equals(transDatas.get(FLAG_IMPORT_PIN))) {
                    mTradeView.getHostActivity().delayJumpToNext();
                } else {
                    mTradeView.getHostActivity().jumpToNext();
                }
            }

            @Override
            public void onReadPinTimeout() throws RemoteException {

            }

            @Override
            public void onError(int i, String s) throws RemoteException {

            }

        });
    }

    public void tobindService() {
        Intent intent = new Intent();
        intent.setPackage("com.centerm.smartposservice");
        intent.setAction("com.centerm.smartpos.service.MANAGER_SERVICE");
        mTradeView.getHostActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 服务连接桥
     */
    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            deviceManager = null;
            logger.info("DEVICE_TYPE_PINPAD onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            logger.info("DEVICE_TYPE_PINPAD onServiceConnected");
            deviceManager = AidlDeviceManager.Stub.asInterface(service);
            if (null != deviceManager) {
                try {
                    curpinPad = AidlPinPad.Stub.asInterface(deviceManager.getDevice(Constant.DEVICE_TYPE.DEVICE_TYPE_PINPAD));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public void unbind(){
        if(conn!=null&&deviceManager!=null&&mTradeView.getHostActivity()!=null){
            mTradeView.getHostActivity().unbindService(conn);
        }
    }

    @Override
    public boolean onPbocTradeTerminated() {
        logger.info("^_^ PBOC中止，取消密码输入 ^_^");
        delayCancelGetPin();
        return super.onPbocTradeTerminated();
    }

    public void gotoNext66(){
        mTradeView.getHostActivity().jumpToNext("66");
    }

}
