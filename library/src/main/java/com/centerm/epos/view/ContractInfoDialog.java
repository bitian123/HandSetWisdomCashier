package com.centerm.epos.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.centerm.cloudsys.sdk.common.log.Log4d;
import com.centerm.epos.EposApplication;
import com.centerm.epos.R;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import config.BusinessConfig;


/**
 * author: wanliang527</br>
 * date:2016/7/30</br>
 */
public class ContractInfoDialog extends Dialog implements View.OnClickListener {

    /**
     * 默认的倒计时时间
     */
    private final static int DEFAULT_COUNTDOWN_TIME = 60;
    /**
     * 计时秒数
     */
    private int countdownTime = DEFAULT_COUNTDOWN_TIME;
    /**
     * 倒计时剩余秒数
     */
    private int remainSecond = countdownTime;
    private Log4d logger = Log4d.getDefaultInstance();
    private String tag = ContractInfoDialog.class.getSimpleName();
    private String name,idNo,contractNo,getContractPrice;
    private TextView titleShow;
    private boolean autoDismiss = true;
    private boolean autoPerformPositive = false;
    private TextView tv_name,tv_idNo,tv_contractNo,tv_contractPrice;
    private Button negativeBtn, positiveBtn;
    private ButtonClickListener clickListener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

        }
    };
    private Timer timer;
    private TimerTask task;

    public ContractInfoDialog(Context context, Map<String, String> map) {
        super(context, R.style.CustomDialog);
        init(context,map);
    }

    public ContractInfoDialog(Context context, int themeResId, Map<String, String> map) {
        super(context, themeResId);
        init(context,map);
    }

    protected ContractInfoDialog(Context context, boolean cancelable, Map<String, String> map , OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context,map);
    }

    private void init(Context context,Map<String, String> map) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.contract_info_dialog, null);
        titleShow = (TextView) v.findViewById(R.id.dialog_title);
        tv_name = (TextView) v.findViewById(R.id.tv_name);
        tv_idNo = v.findViewById(R.id.tv_id_no);
        tv_contractNo = v.findViewById(R.id.tv_contract_no);
        tv_contractPrice = v.findViewById(R.id.tv_contract_price);
        tv_name.setText(map.get("name"));
        tv_idNo.setText(map.get("idNo"));
        tv_contractNo.setText(map.get("contractNo"));
        DecimalFormat df = new DecimalFormat("0.00");
        double price = Double.parseDouble(map.get("contractPrice"));
        tv_contractPrice.setText(""+price);
        negativeBtn =  v.findViewById(R.id.negative_btn);
        positiveBtn =  v.findViewById(R.id.positive_btn);
        negativeBtn.setOnClickListener(this);
        positiveBtn.setOnClickListener(this);

        setCanceledOnTouchOutside(false);
        setContentView(v);
//        setOnKeyListener(new OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                return true;
//            }
//        });

    }

    public void hideTitle() {
        titleShow.setVisibility(View.INVISIBLE);
    }

    public void showTitle() {
        titleShow.setVisibility(View.VISIBLE);
    }

    public ContractInfoDialog setDialogTitle(String title) {
        if (titleShow != null) {
            titleShow.setText(title);
        }
        return this;
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
        if (v.getId() == R.id.positive_btn){
            //dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.POSITIVE, v);
                dismiss();
            }
        }else if (v.getId() == R.id.negative_btn){
            //getWindow().isFloating();
            //dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.NEGATIVE, v);
                dismiss();
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
        /**
         * 确定
         */
        POSITIVE,
        /**
         * 取消
         */
        NEGATIVE
    }

}
