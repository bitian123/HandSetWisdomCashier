package com.centerm.epos.base;

import android.content.Context;

import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.ContractInfoDialog;
import java.util.Map;

/**
 * Created by yuhc on 2017/2/15.
 * 交易界面UI操作相关接口
 */
public interface ITradeView {

    /**
     * 显示界面
     */
    public void show();

    /**
     * 更新界面显示内容
     */
    public void refresh();

    /**
     * 销毁界面
     */
    public void destroy();

    /**
     * 弹出提示信息
     *
     * @param content     提示信息
     * @param displayTime 显示时间
     */
    public void popToast(final String content, final int displayTime);

    void popToast(int resId);

    void popToast(int resId, int displayTime);

    void popToast(String content);

    /**
     * 弹出提示对话框，无按钮模式
     *
     * @param title   标题
     * @param content 内容
     */
    public void popMessageBox(final String title, final String content);

    void popMessageBox(String title, String content, AlertDialog.ButtonClickListener listener);

    void popLoading(String message);

    void popLoading(int resId);

    /**
     * 显示选择对话框，默认选择按钮为“确认”和“取消”
     *
     * @param title    标题
     * @param message  信息
     * @param listener 按钮监听器
     */
    public void showSelectDialog(String title, String message, AlertDialog
            .ButtonClickListener listener);

    public void showSelectDialog(int titleId, int messageId, AlertDialog
            .ButtonClickListener listener);

    public void showContractInfoDialog(int titleId, Map<String, String> map,ContractInfoDialog.ButtonClickListener listener);

    /**
     * 显示信息提示类对话框，含有一个确认按钮，点击确认按钮后，对话框消失
     *
     * @param titleId
     * @param messageId
     * @param listener
     */

    public void showMessageDialog(int titleId, int messageId, AlertDialog
            .ButtonClickListener listener);




    /**
     * 注册组件，广播、服务等
     *
     * @param type 组件类型
     * @param clz  类信息
     */
    public void registerComponent(int type, Class clz);

    /**
     * 注解或销毁已注册的组件
     *
     * @param type 组件类型
     */
    public void registerComponent(int type);

    /**
     * 获取字符串资源
     *
     * @param resID 资源ID
     */
    public String getStringFromResource(int resID);

    /**
     * 获取宿主的Activity
     *
     * @return activity
     */
    public TradeFragmentContainer getHostActivity();

    /**
     * 显示对话框提示信息
     * @param tip   提示信息
     */
    public void showTipDialog(String tip);

    Context getContext();

    /**
     * 返回按钮处理
     * @return true已处理，不需要再进一步处理。false需要继续处理
     */
    public boolean onBackPressed();

    public boolean onExistPressed();

    /**
     * 返回按键处理
     */
    boolean onBacKeyPressed();
}
