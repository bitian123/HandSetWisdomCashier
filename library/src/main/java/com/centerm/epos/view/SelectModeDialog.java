package com.centerm.epos.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.log.Log4d;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;

import java.util.Timer;
import java.util.TimerTask;

import config.BusinessConfig;


/**
 * author: wanliang527</br>
 * date:2016/7/30</br>
 */
public class SelectModeDialog extends Dialog implements View.OnClickListener {

    private final static int DEFAULT_COUNTDOWN_TIME = 60;//默认的倒计时时间
    private int countdownTime = DEFAULT_COUNTDOWN_TIME;//计时秒数
    private int remainSecond = countdownTime;//倒计时剩余秒数
    private Log4d logger = Log4d.getDefaultInstance();
    private String tag = SelectModeDialog.class.getSimpleName();

    private boolean autoDismiss = true;
    private boolean autoPerformPositive = false;
    private TextView mHotLine;
    private ButtonClickListener clickListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    private Timer timer;
    private TimerTask task;

    public SelectModeDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public SelectModeDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected SelectModeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.common_alert_dialog_select_mode, null);
        mHotLine = (TextView) v.findViewById(R.id.mHotLine);
        v.findViewById(R.id.mBtnCommonPay).setOnClickListener(this);
        v.findViewById(R.id.mBtnEbiPay).setOnClickListener(this);
        String hotLine = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.HOTLINE_KEY);
        if(!TextUtils.isEmpty(hotLine)){
            mHotLine.setText(hotLine);
        }
        setCanceledOnTouchOutside(false);
        setContentView(v);
//        setOnKeyListener(new OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });

        //全屏显示
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    public void setKeyListener(OnKeyListener listener){
        setOnKeyListener(listener);
    }

    public boolean isAutoDismiss() {
        return autoDismiss;
    }

    public boolean isAutoPerformPositive() {
        return autoPerformPositive;
    }

    public void setAutoPerformPositive(boolean autoPerformPositive) {
        this.autoPerformPositive = autoPerformPositive;
    }

    public void setAutoDismiss(boolean autoDismiss) {
        this.autoDismiss = autoDismiss;
    }

    public void setTimeout(int timeout) {
        if (timeout > 0) {
            this.countdownTime = timeout;
        }
    }

    public ButtonClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mBtnCommonPay){
            //dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.POSITIVE, v);
            }
        }else if (v.getId() == R.id.mBtnEbiPay){
            //getWindow().isFloating();
            //dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.NEGATIVE, v);
            }
        }
    }

    @Override
    public void show() {
        super.show();
        reset();
        if (autoDismiss) {
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
    }

    public void show(int time) {
        super.show();
        remainSecond = time;
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        if (autoDismiss) {
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
        } else {

        }
    }

    @Override
    public void hide() {
        reset();
        super.hide();
    }

    @Override
    protected void onStop() {
        reset();
        super.onStop();
    }

    private void reset() {
        remainSecond = countdownTime;
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }


    public interface ButtonClickListener {
        void onClick(ButtonType button, View v);
    }

    public enum ButtonType {
        POSITIVE,
        NEGATIVE
    }

}
