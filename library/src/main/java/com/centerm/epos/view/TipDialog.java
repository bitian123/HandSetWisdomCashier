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
import android.view.Window;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.log.Log4d;
import com.centerm.epos.R;

import java.util.Timer;
import java.util.TimerTask;


/**
 * author: liubit</br>
 * date:2019/9/3</br>
 */
public class TipDialog extends Dialog implements View.OnClickListener {

    private final static int DEFAULT_COUNTDOWN_TIME = 60;//默认的倒计时时间
    private int countdownTime = DEFAULT_COUNTDOWN_TIME;//计时秒数
    private int remainSecond = countdownTime;//倒计时剩余秒数
    private Log4d logger = Log4d.getDefaultInstance();
    private String tag = TipDialog.class.getSimpleName();

    private boolean autoDismiss = true;
    private boolean autoPerformPositive = false;
    private TextView titleShow;
    private TextView msgShow,msgShow2;
    private TextView timeShow;
    private TextView negativeBtn, positiveBtn;
    private ButtonClickListener clickListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            logger.verbose(tag, "对话框倒计时," + remainSecond);
            if (--remainSecond < 0) {
                if (autoPerformPositive) {
                    positiveBtn.performClick();
                } else {
                    negativeBtn.performClick();
                }
                return;
            }
            timeShow.setText("（" + remainSecond + "）");
        }
    };
    private Timer timer;
    private TimerTask task;

    public TipDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public TipDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected TipDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.common_tip_dialog, null);
        titleShow = (TextView) v.findViewById(R.id.dialog_title);
        msgShow = (TextView) v.findViewById(R.id.dialog_msg);
        msgShow2 = (TextView) v.findViewById(R.id.dialog_msg2);
        timeShow = (TextView) v.findViewById(R.id.dialog_time);
        negativeBtn = (TextView) v.findViewById(R.id.negative_btn);
        positiveBtn = (TextView) v.findViewById(R.id.positive_btn);
        v.findViewById(R.id.negative_btn).setOnClickListener(this);
        v.findViewById(R.id.positive_btn).setOnClickListener(this);
        setCanceledOnTouchOutside(false);
        setContentView(v);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return true;
            }
        });
    }

    public TipDialog setDialogTitle(String title) {
        if (titleShow != null) {
            titleShow.setText(title);
        }
        return this;
    }

    public String getDialogTitle() {
        return titleShow == null ? null : titleShow.getText().toString();
    }

    public TipDialog setDialogTitle(int stringId) {
        if (titleShow != null) {
            titleShow.setText(stringId);
        }
        return this;
    }

    public TipDialog setDialogMsg(String msg) {
        if (msgShow != null) {
            msgShow.setText(msg);
            if(!TextUtils.isEmpty(msg)&&msg.contains("卡号与登录人主体不一致")){
                msgShow2.setVisibility(View.VISIBLE);
            }
        }
        return this;
    }

    public TipDialog setDialogMsg(int stringId) {
        if (msgShow != null) {
            msgShow.setText(stringId);
        }
        return this;
    }

    public TipDialog hideNegative() {
        negativeBtn.setVisibility(View.GONE);
        return this;
    }

    public TipDialog hidePositive() {
        positiveBtn.setVisibility(View.GONE);
        return this;
    }

    public TipDialog showNegative() {
        negativeBtn.setVisibility(View.VISIBLE);
        return this;
    }

    public void hideTitle() {
        titleShow.setVisibility(View.INVISIBLE);
    }

    public void showTitle() {
        titleShow.setVisibility(View.VISIBLE);
    }

    public TipDialog setButtonText(Context context, String positive, String negative) {
        positive = positive == null ? context.getString(R.string.label_message_label_sure) : positive;
        negative = negative == null ? context.getString(R.string.label_message_label_cancel) : negative;
        positiveBtn.setText(positive);
        negativeBtn.setText(negative);
        return this;
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
        if (v.getId() == R.id.positive_btn){
            dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.POSITIVE, v);
            }
        }else if (v.getId() == R.id.negative_btn){
            getWindow().isFloating();
            dismiss();
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
            timeShow.setVisibility(View.VISIBLE);
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
            timeShow.setVisibility(View.GONE);
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
            timeShow.setVisibility(View.VISIBLE);
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
            timeShow.setVisibility(View.GONE);
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
