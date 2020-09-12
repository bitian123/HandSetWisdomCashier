package com.centerm.epos.utils;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.adapter.BusinessUnionAdapter;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.common.Settings;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.ClearEditText;
import com.centerm.epos.view.ContractInfoDialog;
import com.centerm.epos.view.LockDialog;
import com.centerm.epos.view.SelectModeDialog;
import com.centerm.epos.view.TipDialog;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import config.Config;

/**
 * 对话框工厂
 * author:wanliang527</br>
 * date:2016/10/26</br>
 */

public class DialogFactory {
    private static Logger logger = Logger.getLogger(DialogFactory.class);
    private static Dialog current;
    private static long lastKeyEventTime;//上一次实体按键的时间
    private static StringBuilder secretCode = new StringBuilder();//暗码组合器

    /**
     * 显示信息提示类对话框，含有一个确认按钮，点击确认按钮后，对话框消失
     *
     * @param context context
     * @param title
     * @param message
     */
    public static void showMessageDialog(Context context, String title, String message) {
        showMessageDialog(context, title, message, null);
    }

    public static void showMessageDialog(Context context, String title, String message, boolean autoDismiss) {
        showMessageDialog(context, title, message, null, autoDismiss);
    }

    /**
     * 显示信息提示类对话框，含有一个确认按钮，点击确认按钮后，对话框消失
     *
     * @param context
     * @param title
     * @param message
     * @param listener
     */

    public static void showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        showMessageDialog(context, title, message, listener, true);
    }

    public static void showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, boolean autoDismiss) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.hideNegative();
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        current = dialog;
        current.show();
    }

    public static AlertDialog showMessageDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, int timeout) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.hideNegative();
        dialog.setTimeout(timeout);
        dialog.setAutoDismiss(true);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        current = dialog;
        current.show();
        return dialog;
    }


    /**
     * 显示打印的提示框
     *
     * @param context
     * @param listener
     */

    public static void showPrintDialog(Context context, AlertDialog.ButtonClickListener listener) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
//        dialog.hideNegative();
        dialog.hideTitle();
        dialog.setAutoPerformPositive(true);
        dialog.setDialogMsg(context.getString(R.string.tip_print_next));
        if (null != listener) {
            dialog.setClickListener(listener);
        }
        dialog.show(Config.PRINT_NEXT_TIME);
        current = dialog;
    }

    /**
     * 显示选择对话框，默认选择按钮为“确认”和“取消”
     *
     * @param context  context
     * @param title    标题
     * @param message  信息
     * @param listener 按钮监听器
     */
    public static void showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        showSelectDialog(context, title, message, listener, true);
    }

    public static void showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, boolean autoDismiss) {
        hideAll();
        if(context==null){
            return;
        }
        AlertDialog dialog = new AlertDialog(context);
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
    }

    public static void showSelectDialogNoCancel(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        hideAll();
        if(context==null){
            return;
        }
        AlertDialog dialog = new AlertDialog(context);
        dialog.setAutoDismiss(true);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.hideNegative();
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
    }

    public static void showContractInfoDialog(Context context, String title, Map<String,String > message, ContractInfoDialog.ButtonClickListener listener, boolean autoDismiss) {
        hideAll();
        if(context==null){
            return;
        }
        ContractInfoDialog dialog = new ContractInfoDialog(context,message);
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
    }

    public static void showSelectModeDialog(Context context, SelectModeDialog.ButtonClickListener listener,
                                            DialogInterface.OnKeyListener keyListener) {
        hideAll();
        SelectModeDialog dialog = new SelectModeDialog(context);
        dialog.setAutoDismiss(false);
        dialog.setClickListener(listener);
        dialog.setKeyListener(keyListener);
        current = dialog;
        current.show();
    }

    public static void showTipDialog(Context context, String title, String message, TipDialog.ButtonClickListener listener, boolean hasCancelBtn) {
        hideAll();
        if(context==null){
            return;
        }
        TipDialog dialog = new TipDialog(context);
        dialog.setAutoDismiss(false);
        dialog.setAutoPerformPositive(true);
        if(hasCancelBtn){
            dialog.showNegative();
        }
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
    }

    public static AlertDialog showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, int timeout) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.setTimeout(timeout);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();
        return dialog;
    }

    public static void showLockDialog(Context context) {
        hideAll();
        LockDialog lockDialog = new LockDialog(context);
        lockDialog.show();
    }
    public static void showtDialogTimeToPositive(Context context, String title, String message, AlertDialog.ButtonClickListener listener, int Time) {
        showSelectDialog(context, title, message, listener, true, Time, true);
    }
    public static void showSettleSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        showSelectDialog(context, title, message, listener, true, 10, false);
    }
    public static void showSelectDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener, boolean autoDismiss, int timeout , boolean endToPositive) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        dialog.setAutoDismiss(autoDismiss);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        if(timeout != 0){
            dialog.setTimeout(timeout);
        }
        if(endToPositive){
            dialog.setAutoPerformPositive(true);
        }
        current = dialog;
        current.show();
    }
    /**
     * 显示选择对话框，默认选择按钮为“确认”和“取消”
     *
     * @param context  context
     * @param title    标题
     * @param message  信息
     * @param listener 按钮监听器
     */
    public static void showSelectPirntDialog(Context context, String title, String message, AlertDialog.ButtonClickListener listener) {
        hideAll();
        AlertDialog dialog = new AlertDialog(context);
        if (title == null) {
            dialog.hideTitle();
        } else {
            dialog.showTitle();
            dialog.setDialogTitle(title);
        }
        dialog.setButtonText(context, "重试", "取消");
        dialog.setDialogMsg(message);
        dialog.setClickListener(listener);
        current = dialog;
        current.show();

    }

    /**
     * 显示加载对话框
     *
     * @param context context
     * @param message 提示信息
     */
    public static void showLoadingDialog(Context context, final String message) {
        if(!Settings.bIsSettingBlueTheme() ){

            hideAll();
            current = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Dialog);
            ((ProgressDialog) current).setMessage(message);
            ((ProgressDialog) current).setProgressStyle(ProgressDialog.STYLE_SPINNER);
            current.setCanceledOnTouchOutside(false);
            current.setCancelable(false);
            current.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(KeyEvent.KEYCODE_BACK==keyCode&&!TextUtils.isEmpty(message)&&message.contains("正在打印凭条")){
                        long now = System.currentTimeMillis();
                        if (now - lastKeyEventTime > 3000) {
                            secretCode.setLength(0);
                        }
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            return true;
                        }
                        lastKeyEventTime = now;
                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_BACK:
                                secretCode.append("3");
                                break;
                        }
                        String code = secretCode.toString();
                        logger.debug("暗码：" + code);
                        if (Config.SECRET_KEY_CODE2.equals(code)) {
                            secretCode.setLength(0);
                            hideAll();
                        }
                        return true;
                    }
                    return true;
                }
            });
            current.show();
        }
        else {
            showLoadingDialog(context, message,true);
        }
    }

    public static class ProgressDialogCustom extends Dialog {
        ImageView imageView = null;
        TextView textView = null;
        private Timer timer;
        private TimerTask task;
        private int time = Config.TIME_OUT;
        private CharSequence txt = "";
        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                time--;
                if (time<=0) {
                    if(timer!=null){
                        timer.cancel();
                        timer = null;
                    }
                    if(task!=null){
                        task.cancel();
                        task = null;
                    }
                    if(current!=null) {
                        current.dismiss();
                    }
                    return;
                }
                String tip = txt+"\n（"+time+"）";
                textView.setText(tip);
            }
        };


        public ProgressDialogCustom(Context context) {
            super(context);
        }

        public ProgressDialogCustom(Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.loading_dialog_process2);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            textView = ((TextView)findViewById(R.id.displayInfo));
            imageView = (ImageView)findViewById(R.id.loadingPic);
            imageView.setImageResource(R.drawable.anim_loading2);

            if (task == null) {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(0);
                    }
                };
            }
            if (timer == null) {
                timer = new Timer();
            }
            timer.schedule(task, 0, 1000);
        }

        @Override
        protected void onStart() {
            super.onStart();
            if( null == imageView ){
                return ;
            }
            imageView.setImageResource(R.drawable.anim_loading2);

            if( imageView.getDrawable() instanceof AnimationDrawable) {
                ((AnimationDrawable) imageView.getDrawable()).start();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            if( null == imageView ){
                return ;
            }
            if(timer!=null){
                timer.cancel();
                timer = null;
            }
            if(task!=null){
                task.cancel();
                task = null;
            }
            handler = null;
            if( imageView.getDrawable() instanceof AnimationDrawable) {
                logger.debug("ProgressDialogCustom ProgressDialogCustom stop" );
                ((AnimationDrawable) imageView.getDrawable()).stop();
            }
        }
        public final void setText(CharSequence text) {
            txt = text;
            if( null != textView){
                textView.setText(text);
            }
        }
    }

    public static class ProgressDialogCustom2 extends Dialog
    {
        ImageView imageView = null;
        TextView textView = null;

        public ProgressDialogCustom2(Context context) {
            super(context);
        }

        public ProgressDialogCustom2(Context context, int theme) {
            super(context, theme);
        }
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.loading_dialog_process);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            textView = ((TextView)findViewById(R.id.displayInfo));

            imageView = (ImageView)findViewById(R.id.loadingPic);

            imageView.setImageResource(R.drawable.anim_loading);
        }

        @Override
        protected void onStart() {
            super.onStart();
            if( null == imageView ){
                return ;
            }
            imageView.setImageResource(R.drawable.anim_loading);

            if( imageView.getDrawable() instanceof AnimationDrawable)
            {
                ((AnimationDrawable) imageView.getDrawable()).start();
            }
        }

        @Override
        protected void onStop() {
            super.onStop();
            if( null == imageView ){
                return ;
            }
            if( imageView.getDrawable() instanceof AnimationDrawable)
            {
                logger.debug("ProgressDialogCustom ProgressDialogCustom stop" );
                ((AnimationDrawable) imageView.getDrawable()).stop();
            }
        }
        public final void setText(CharSequence text)
        {
            if( null != textView ){
                textView.setText(text);
            }
        }
    }

    public static class DetailDialogCustom extends Dialog {
        Activity activity;
        TextView mTvRoomId = null;
        TextView mTvBillId = null;
        TextView mTvMoneyType = null;
        TextView mTvName = null;
        TextView mTvUnpaidAmount = null;
        TextView mTvPaidAmount = null;
        TextView mTvAmtReceivable = null;
        TextView mTvSettleNo = null;
        ClearEditText mEtPayAmount = null;
        OnEnteListener listener;
        GtBusinessListBean.MoneyDetailListBean data;

        public DetailDialogCustom(Context context) {
            super(context);
            activity = (Activity) context;
        }

        public DetailDialogCustom(Context context, int theme) {
            super(context, theme);
            activity = (Activity) context;
        }

        public void setOnEnteListener(OnEnteListener l){
            listener = l;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.loading_dialog_detail);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            mTvRoomId = ((TextView)findViewById(R.id.mTvRoomId));
            mTvBillId = ((TextView)findViewById(R.id.mTvBillId));
            mTvMoneyType = ((TextView)findViewById(R.id.mTvMoneyType));
            mTvName = ((TextView)findViewById(R.id.mTvName));
            mTvUnpaidAmount = ((TextView)findViewById(R.id.mTvUnpaidAmount));
            mTvPaidAmount = ((TextView)findViewById(R.id.mTvPaidAmount));
            mTvAmtReceivable = ((TextView)findViewById(R.id.mTvAmtReceivable));
            mTvSettleNo = ((TextView)findViewById(R.id.mTvSettleNo));
            mEtPayAmount = (ClearEditText) findViewById(R.id.mEtPayAmount);
            mEtPayAmount.addTextChangedListener(new MoneyTextWatcher(mEtPayAmount));

            findViewById(R.id.mBtnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            findViewById(R.id.mBtnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String amtStr = getPayAmount();
                    if(TextUtils.isEmpty(amtStr)){
                        ViewUtils.showToast(activity, "请输入金额");
                        return;
                    }
                    if(amtStr.endsWith(".")){
                        amtStr = amtStr.replace(".", "");
                    }
                    double amt = Double.parseDouble(amtStr);
                    if(amt==0){
                        ViewUtils.showToast(activity, "请输入金额");
                        return;
                    }
                    if(amt>data.getUnpaidAmount()){
                        ViewUtils.showToast(activity, "本次付款金额不能大于本次应收金额");
                        return;
                    }
                    listener.onEnter(amt);
                    current.dismiss();
                }
            });
        }

        public void setData(GtBusinessListBean.MoneyDetailListBean bean) {
            data = bean;
            mTvRoomId.setText(bean.getProjectName());
            mTvBillId.setText(bean.getRoomFullName());
            mTvMoneyType.setText("款项名称: "+bean.getPaymentItemName());
            StringBuilder builder = new StringBuilder("姓名:");
            for(GtBusinessListBean.MoneyDetailListBean.CustomListBean custom : bean.getCustomList()){
                builder.append(" "+custom.getName());
            }
            mTvName.setText(builder.toString());
            mTvUnpaidAmount.setText("应收金额: "+DataHelper.saved2Decimal(bean.getAmountReceivable())+"元");
            mTvPaidAmount.setText("已收金额: "+DataHelper.saved2Decimal(bean.getAmountReceived())+"元");
            mTvAmtReceivable.setText("本次应收: "+DataHelper.saved2Decimal(bean.getUnpaidAmount())+"元");
            mTvSettleNo.setText("结算帐户: "+bean.getSubjectName());
            if(bean.getReadyPayAmt()>0){
                mEtPayAmount.setText(DataHelper.saved2Decimal(bean.getReadyPayAmt())+"");
            }else {
                mEtPayAmount.setText(DataHelper.saved2Decimal(bean.getUnpaidAmount())+"");
            }
            if(mEtPayAmount!=null&&!TextUtils.isEmpty(mEtPayAmount.getText().toString())){
                mEtPayAmount.setSelection(mEtPayAmount.getText().toString().length());
            }

        }
        public String getPayAmount(){
            return mEtPayAmount.getText().toString().trim();
        }
    }



    public static class DetailDialogCustomUnion extends Dialog {
        Activity activity;
        ListView listView;
        TextView tv_page_num;
        OnEnteListener listener;
        GtBusinessListBean.MoneyDetailListBean data;
        GtBusinessListBean.MoneyDetailListBean mListBean;


        public DetailDialogCustomUnion(Context context) {
            super(context);
            activity = (Activity) context;
        }

        public DetailDialogCustomUnion(Context context, GtBusinessListBean.MoneyDetailListBean  listBean, int theme) {
            super(context, theme);
            activity = (Activity) context;
            this.mListBean = listBean;
        }

        public void setOnEnteListener(OnEnteListener l) {
            listener = l;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.loading_unionlist_dialog_detail);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }
            listView = (ListView) findViewById(R.id.list_union);
            tv_page_num= (TextView) findViewById(R.id.tv_page_number);
            BusinessUnionAdapter adapter = new BusinessUnionAdapter(getContext(),activity,mListBean);
            listView.setAdapter(adapter);
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    tv_page_num.setText("第"+(firstVisibleItem+1)+"/"+totalItemCount+"页");
                }
            });
            findViewById(R.id.mBtnCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            findViewById(R.id.mBtnConfirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onEnter();
                    current.dismiss();
                }
            });

        }

    }


    public static class EditPrintNumDialogCustom extends Dialog {
        Activity activity;
        OnConfirmListener listener;
        TextView mTvPrintNum = null;
        ImageView mBtnLess;

        public EditPrintNumDialogCustom(Context context) {
            super(context);
            activity = (Activity) context;
        }

        public EditPrintNumDialogCustom(Context context, int theme) {
            super(context, theme);
            activity = (Activity) context;
        }

        public EditPrintNumDialogCustom(Context context, int theme, OnConfirmListener l) {
            super(context, theme);
            activity = (Activity) context;
            listener = l;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.dialog_print_num);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            mTvPrintNum = ((TextView)findViewById(R.id.mTvPrintNum));
            mBtnLess = (ImageView) findViewById(R.id.mBtnLess);

            findViewById(R.id.mTvOK).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    listener.onConfirm(mTvPrintNum.getText().toString());
                }
            });
            findViewById(R.id.mTvCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            findViewById(R.id.mBtnLess).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String numStr = mTvPrintNum.getText().toString();
                    int num = Integer.parseInt(numStr);
                    if(num>1){
                        num--;
                        mTvPrintNum.setText(num+"");
                    }
                    if(CommonUtils.isK9()){
                        if(num==1){
                            mBtnLess.setBackgroundResource(R.drawable.icon_less_pre);
                        }else {
                            mBtnLess.setBackgroundResource(R.drawable.icon_less);
                        }
                    }

                }
            });
            findViewById(R.id.mBtnAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String numStr = mTvPrintNum.getText().toString();
                    int num = Integer.parseInt(numStr);
                    if(num<9){
                        num++;
                        mTvPrintNum.setText(num+"");
                    }
                    if(CommonUtils.isK9()){
                        if(num==1){
                            mBtnLess.setBackgroundResource(R.drawable.icon_less_pre);
                        }else {
                            mBtnLess.setBackgroundResource(R.drawable.icon_less);
                        }

                    }
                }
            });
        }

    }

    public static class DetailDialogCustomE10 extends Dialog {
        Activity activity;
        TextView mTvRoomId = null;
        TextView mTvBillId = null;
        TextView mTvMoneyType = null;
        TextView mTvName = null;
        TextView mTvUnpaidAmount = null;
        TextView mTvPaidAmount = null;
        TextView mTvAmtReceivable = null;
        TextView mTvSettlement = null;
        EditText mEtPayAmount = null;
        OnEnteListener onEnteListener;

        public DetailDialogCustomE10(Context context) {
            super(context);
            activity = (Activity) context;
        }

        public DetailDialogCustomE10(Context context, int theme) {
            super(context, theme);
            activity = (Activity) context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.loading_dialog_detail);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
            }

            mTvRoomId = ((TextView)findViewById(R.id.mTvRoomId));
            mTvBillId = ((TextView)findViewById(R.id.mTvBillId));
            mTvMoneyType = ((TextView)findViewById(R.id.mTvMoneyType));
            mTvName = ((TextView)findViewById(R.id.mTvName));
            mTvUnpaidAmount = ((TextView)findViewById(R.id.mTvUnpaidAmount));
            mTvPaidAmount = ((TextView)findViewById(R.id.mTvPaidAmount));
            mTvAmtReceivable = ((TextView)findViewById(R.id.mTvAmtReceivable));
            mTvSettlement = ((TextView)findViewById(R.id.mTvSettlement));
            mEtPayAmount = (EditText) findViewById(R.id.mEtPayAmount);
            mEtPayAmount.addTextChangedListener(new MoneyTextWatcher(mEtPayAmount));
            findViewById(R.id.mBtnCanel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });
            findViewById(R.id.mBtnOk).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String amtStr = mEtPayAmount.getText().toString().trim();
                    if(TextUtils.isEmpty(amtStr)){
                        ViewUtils.showToast(activity, "请输入金额");
                        return;
                    }
                    if(amtStr.endsWith(".")){
                        amtStr = amtStr.replace(".", "");
                    }
                    double amt = Double.parseDouble(amtStr);
                    if(amt==0){
                        ViewUtils.showToast(activity, "请输入金额");
                        return;
                    }
                    onEnteListener.onEnter(amt);
                    dismiss();
                }
            });

        }

        public void setData(GtBusinessListBean.MoneyDetailListBean bean, OnEnteListener listener) {
            onEnteListener = listener;
            mTvRoomId.setText("项目名称: "+bean.getProjectName());
            StringBuilder builder = new StringBuilder("姓名:");
            for(GtBusinessListBean.MoneyDetailListBean.CustomListBean custom : bean.getCustomList()){
                builder.append(" "+custom.getName());
            }
            mTvName.setText(builder.toString());
            mTvBillId.setText("房间名称: "+bean.getRoomFullName());
            mTvMoneyType.setText("款项名称: "+bean.getBusinessType());
            mTvSettlement.setText("结算账户: "+bean.getSubjectName());
            mTvAmtReceivable.setText("应收金额: "+DataHelper.saved2Decimal(bean.getAmountReceivable())+"元");
            mTvPaidAmount.setText("已收金额: "+DataHelper.saved2Decimal(bean.getAmountReceived())+"元");
            mTvUnpaidAmount.setText("本次应收: "+DataHelper.saved2Decimal(bean.getUnpaidAmount())+"元");
            if(bean.getReadyPayAmt()>0){
                mEtPayAmount.setText(DataHelper.saved2Decimal(bean.getReadyPayAmt())+"");
            }else {
                mEtPayAmount.setText(DataHelper.saved2Decimal(bean.getUnpaidAmount())+"");
            }


        }
        public String getPayAmount(){
            return mEtPayAmount.getText().toString().trim();
        }
    }

    /**
     * 显示加载对话框
     *
     * @param context context
     * @param message 提示信息
     */
    public static void showLoadingDialog(Context context, final String message, boolean bIsTrue) {
        hideAll();
        try {
            current = new ProgressDialogCustom(context,R.style.dialog_style);
            current.show();
            ((ProgressDialogCustom)current).setText(message);
            current.setCanceledOnTouchOutside(false);
            current.setCancelable(false);
            current.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if(KeyEvent.KEYCODE_BACK==keyCode&&!TextUtils.isEmpty(message)&&message.contains("正在打印凭条")){
                        long now = System.currentTimeMillis();
                        if (now - lastKeyEventTime > 3000) {
                            secretCode.setLength(0);
                        }
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            return true;
                        }
                        lastKeyEventTime = now;
                        switch (event.getKeyCode()) {
                            case KeyEvent.KEYCODE_BACK:
                                secretCode.append("3");
                                break;
                        }
                        String code = secretCode.toString();
                        logger.debug("暗码：" + code);
                        if (Config.SECRET_KEY_CODE2.equals(code)) {
                            secretCode.setLength(0);
                            hideAll();
                        }
                        return true;
                    }
                    return true;
                }
            });
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    /**
     * 显示加载对话框
     *
     * @param context context
     * @param data 信息
     */
    public static void showDetailDialog(int type ,final Context context, final GtBusinessListBean.MoneyDetailListBean data, final OnEnteListener listener) {
       if(type == 1){
           showDetailDialog(context,data,listener);
       }else{
           showUinonDetailDialog(context,data,listener);
       }

    }

    /**
     * 显示加载对话框
     *
     * @param context context
     * @param data 信息
     */
    public static void showDetailDialog(final Context context, final GtBusinessListBean.MoneyDetailListBean data, final OnEnteListener listener) {
        hideAll();
        current = new DetailDialogCustom(context,R.style.dialog_style);
        current.show();
        ((DetailDialogCustom)current).setData(data);
        ((DetailDialogCustom)current).setOnEnteListener(listener);
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        current.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_BACK==keyCode){
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * 显示加载对话框
     *
     * @param context context
     * @param data 信息
     */
    public static void showUinonDetailDialog(final Context context, final GtBusinessListBean.MoneyDetailListBean data, final OnEnteListener listener) {
        hideAll();
        current = new DetailDialogCustomUnion(context,data,R.style.dialog_style);
        current.show();
        ((DetailDialogCustomUnion)current).setOnEnteListener(listener);
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        current.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_BACK==keyCode){
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 显示选择打印份数对话框
     *
     * @param context context
     */
    public static void showEditPrintNumDialog(final Context context, final OnConfirmListener listener) {
        hideAll();
        current = new EditPrintNumDialogCustom(context,R.style.dialog_style,listener);
        current.show();
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        current.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_BACK==keyCode){
                    return true;
                }
                return false;
            }
        });
    }


    /**
     * 显示加载对话框
     *
     * @param context context
     * @param data 信息
     */
    public static void showDetailDialogE10(final Context context, GtBusinessListBean.MoneyDetailListBean data, final OnEnteListener listener) {
        hideAll();
        current = new DetailDialogCustomE10(context,R.style.dialog_style);
        current.show();
        ((DetailDialogCustomE10)current).setData(data,listener);
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        current.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(KeyEvent.KEYCODE_BACK==keyCode){
                    return true;
                }
                return false;
            }
        });
    }

    public static void showLoadingDialog(Context context, String message, DialogInterface.OnKeyListener listener) {
        hideAll();
        current = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Dialog);
        ((ProgressDialog) current).setMessage(message);
        ((ProgressDialog) current).setProgressStyle(ProgressDialog.STYLE_SPINNER);
        current.setCanceledOnTouchOutside(false);
        current.setCancelable(false);
        if (listener != null)
            current.setOnKeyListener(listener);
        else
            current.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return true;
                }
            });
        current.show();
    }

 /*   public static ProgressDialog showProgressDialog(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context, android.R.style.Theme_DeviceDefault_Light_Dialog);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.show();
        return dialog;
    }
*/

    public static void hideAll() {
        if (current != null) {
            try {
                current.dismiss();
                current = null;
                logger.info("隐藏所有对话框成功");
            } catch (Exception e) {
                logger.warn("隐藏所有对话框失败==>"+e.toString());
                android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                System.exit(0);
            }
        }
    }

    private Dialog createDialog(Context context) {
        return new Dialog(context);
    }


    public static Dialog createMessageDialog(final Context mCtx, String title) {
        final Dialog dialog = new Dialog(mCtx, R.style.DialogStyle);
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = mCtx.getResources().getDisplayMetrics().widthPixels * 540 / 720;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.setContentView(R.layout.dialog_message);
        TextView txtvwMessage = (TextView) dialog.findViewById(R.id.txtvw_message);
        txtvwMessage.setText(title);
        dialog.findViewById(R.id.btn_message_sure).setOnClickListener(new CloseDialog(dialog));
        dialog.findViewById(R.id.btn_message_cancel).setOnClickListener(new CloseDialog(dialog));
        dialog.setCancelable(false);
        return dialog;
    }

    private static class CloseDialog implements View.OnClickListener {

        private Dialog dialog;

        public CloseDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (dialog != null)
                dialog.dismiss();
        }
    }

/*    public interface ButtonClickListener {
        void onClick(View view);
    }*/

}
