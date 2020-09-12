package com.centerm.epos.base;

import android.os.Bundle;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;


/**
 * Created by yuhc on 2017/2/15.
 * 交易界面业务处理接口
 */

public interface ITradePresent {


    /**
     * 获取界面显示需要的相关数据
     *
     * @return 相关数据
     */
    public Object getShowInfo();


    /**
     * 显示交易界面
     */
    public void displayUI();

    /**
     * 开始执行交易界面的业务逻辑
     */
    public void beginTransaction();

    /**
     * 结束交易界面的业务逻辑
     */
    public void endTransaction();

    /**
     * 释放资源和引用
     */
    public void release();

    /**
     * 下一步处理
     */
    public void gotoNextStep();

    public void gotoNextStep(String id);

    /**
     * 返回上一步处理
     */
    public void gotoPreStep();

    /**
     * 业务处理异常
     */
    public void onTransactionError();


    /**
     * 退出业务
     */
    public void onTransactionQuit();

    /**
     * 初始化数据对象，在{@method onInitView}和{@method onLayoutId}之前进行
     */
    public void onInitLocalData(Bundle savedInstanceState);


    /**
     * 打开数据库
     */
    public void OpenDatabase();

    /**
     * 获取当前业务的名称
     *
     * @return 业务名称
     */
    public String getTradeName();

    /**
     * 获取当前业务的代码
     *
     * @return 业务代码
     */
    public String getTradeCode();

    /**
     * 获取交易过程的业务数据
     *
     * @param tag 数据标签
     * @return 数据
     */
    public String getTransData(String tag);

    Map<String, Object> getTransData();

    /**
     * 获取交易过程的临时数据
     *
     * @param tag 数据标签
     * @return 数据
     */
    public String getTempData(String tag);

    /**
     * 判断PBOC是否被中止
     *
     * @return true 被中断
     */
    public boolean isPbocTerminated();

    /**
     * 判断是否是IC卡业务
     *
     * @return true是
     */
    public boolean isICInsertTrade();

    /**
     * 返回上一步操作的业务处理
     */
    public void onCancel();

    /**
     * 确认操作处理
     */
    public Object onConfirm(Object paramObj);

    /**
     * 退出操作处理
     */
    public void onExit();

    /**
     * 控制超时
     */
    public boolean isEnableShowingTimeout();


    /**
     * PBOC卡号确认
     *
     * @param cardNo 银行卡号
     * @return 是否继续执行
     */
    public boolean onPbocConfirmCardNo(String cardNo);

    /**
     * PBOC流程请求联机
     *
     * @return 是否继续执行
     */
    public boolean onPbocRequestOnline();

    /**
     * PBOC流程导入交易金额
     *
     * @return 是否继续执行
     */
    public boolean onPbocImportAmount();

    /**
     * PBOC交易接收处理
     *
     * @return true后续不用再继续处理，false后续继续处理
     */
    public boolean onPbocTradeApproved();

    /**
     * PBOC交易终止
     *
     * @return true后续不用再继续处理，false后续继续处理
     */
    public boolean onPbocTradeTerminated();

    /**
     * PBOC交易拒绝
     *
     * @return true后续不用再继续处理，false后续继续处理
     */
    public boolean onPbocTradeRefused();

    /**
     * PBOC异常
     *
     * @return true后续不用再继续处理，false后续继续处理
     */
    public boolean onPbocTradeError();

    /**
     * 更新用户界面
     *
     * @return true后续不用再继续处理，false后续继续处理
     */
    boolean onPbocChangeUserInterface();

    /**
     * 是否可以重复执行
     *
     * @return true
     */
    boolean isRepeatable();

    /**
     * 电子现金余额
     *
     * @return true
     */
    boolean onReturnOfflineBalance(String code1, String balance1, String code2, String balance2);

    /**
     * 卡片圈存日志
     *
     * @return true
     */
    boolean onReturnCardTransLog(List<Parcelable> data);

    /**
     * 卡片圈存日志
     *
     * @return true
     */
    boolean onReturnCardLoadLog(List<Parcelable> data);

    /*请求提示信息确认*/
    boolean onPbocRequestTipsConfirm(String tips);
    /*请求电子现金提示确认*/
    boolean onPbocRequestEcTipsConfirm();

    /**
     * PBOC流程导入密码
     *@param bIsOffLinePin  true-脱机pin，false-联机pin
     * @return 是否继续执行
     */
    public boolean onPbocImportPin(boolean bIsOffLinePin);

    /*
    * aid选择
    * */
    public boolean onPbocRequestUserAidSelect(String[] aidList);


}
