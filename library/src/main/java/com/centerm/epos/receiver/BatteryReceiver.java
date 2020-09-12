package com.centerm.epos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.annotation.NonNull;

import com.centerm.epos.utils.XLogUtil;

/**
 * Created by ll on 16/8/15.
 * 电池电量低提示
 */
public class BatteryReceiver extends BroadcastReceiver {

    private static final String TAG = BatteryReceiver.class.getSimpleName();
    Context mContext;
    BatteryAction mBatteryAction;

    private static final float LOW_POWER_VALUE = 0.15f; //低电量阈值
    private static final float WARN_POWER_VALUE = 0.1f; //警告低电量阈值

    public BatteryReceiver(Context mContext) {
        this.mContext = mContext;
    }

    public BatteryReceiver(BatteryAction mBatteryAction, Context mContext) {
        this.mBatteryAction = mBatteryAction;
        this.mContext = mContext;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //获取当前电量
        int current = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //获取总电量，一般是100
        int total = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        //获取充电状态
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager
                .BATTERY_STATUS_FULL;

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            XLogUtil.d(TAG, "^_^ battery changed ^_^");
            if (mBatteryAction != null) {
                mBatteryAction.onBatteryChanged(current, isCharging);
                //如果当前电量小于总电量
                if (current * 1.0 / total < WARN_POWER_VALUE) {
                    XLogUtil.d(TAG, "电量低，当前电量:" + current + " 总电量:" + total);
                    mBatteryAction.onLowPower(current, isCharging);
                }
            }
        } else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
            XLogUtil.d(TAG, "^_^ battery low ^_^");
            //电量小于15%的时候
//            if (mBatteryAction != null)
//                mBatteryAction.onLowPower(current, isCharging);
        }
    }

    public BatteryAction getmBatteryAction() {
        return mBatteryAction;
    }

    public void setmBatteryAction(BatteryAction mBatteryAction) {
        this.mBatteryAction = mBatteryAction;
    }

    /**
     * 广播回调函数
     */
    public interface BatteryAction {
        /**
         * 低电量的回调处理
         *
         * @param powerLevel 当前电量值
         */
        void onLowPower(int powerLevel, boolean isCharging);

        /**
         * 电量变化时的回调
         *
         * @param powerLevel 当前电量值
         */
        void onBatteryChanged(int powerLevel, boolean isCharging);
    }

    /**
     * 注册电量变化的接收器
     */
    public void register() {
        XLogUtil.d(TAG, "^_^ register battery receiver ^_^");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_LOW);  //低电量
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);  //电量变化
        intentFilter.addAction(Intent.ACTION_BATTERY_OKAY); //从低电量恢复正常
        mContext.registerReceiver(this, intentFilter);
    }

    /**
     * 检测终端是否在充电
     *
     * @param context 上下文环境
     * @return TRUE 在充电；FALSE 不在充电
     * @throws Exception 获取电池信息异常
     */
    public static boolean isCharging(Context context) throws Exception {
        XLogUtil.d(TAG, "^_^ check is charging ^_^");
        Intent batteryStatus = getBatteryIntent(context);

        // 充电方式，电源适配器或USB
//        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
//        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
//        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL);
    }

    @NonNull
    private static Intent getBatteryIntent(Context context) throws Exception {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null)
            throw new Exception("电池信息获取失败");
        return batteryStatus;
    }

    /**
     * 检测电量是否过低
     *
     * @param context 上下文
     * @return 是否低电量
     * @throws Exception 低电量信息获取失败
     */
    public static boolean isLowPower(Context context) throws Exception {
        XLogUtil.d(TAG, "^_^ check is low power ^_^");
        Intent batteryStatus = getBatteryIntent(context);
        int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int fullLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (currentLevel < 0 || fullLevel < 0)
            throw new Exception("获取电量值错误");
        //如果当前电量小于总电量的5%
        if (currentLevel * 1.0 / fullLevel < LOW_POWER_VALUE) {
            XLogUtil.d(TAG, "电量低，当前电量:" + currentLevel + " 总电量:" + fullLevel);
            return true;
        }
        return false;
    }

    /**
     * 检测电量是否过低
     *
     * @param context 上下文
     * @return 是否低电量
     * @throws Exception 低电量信息获取失败
     */
    public static boolean isDownPowerLevel(Context context, int powerValue) throws Exception {
        XLogUtil.d(TAG, "^_^ check is low power ^_^");
        Intent batteryStatus = getBatteryIntent(context);
        int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int fullLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (currentLevel < 0 || fullLevel < 0)
            throw new Exception("获取电量值错误");
        //如果当前电量小于总电量的5%
        if (currentLevel <= powerValue) {
            XLogUtil.d(TAG, "电量低，当前电量:" + currentLevel + " 总电量:" + fullLevel);
            return true;
        }
        return false;
    }
}
