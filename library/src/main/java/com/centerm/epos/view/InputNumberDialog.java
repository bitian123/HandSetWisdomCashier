package com.centerm.epos.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.R;

import org.apache.log4j.Logger;

/**
 * Created by 94437 on 2017/6/28.
 */

public class InputNumberDialog extends Dialog implements View.OnClickListener {
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected Logger logger = Logger.getLogger(this.getClass());
    private EditText etNumber;
    private TextView title;
    private Button posBtn,nagBtn;
    private Context context;
    private ButtonClickListener clickListener;
    public InputNumberDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public InputNumberDialog(Context context, String tip) {
        super(context, R.style.CustomDialog);
        init(context, tip, null);
    }

    public InputNumberDialog(Context context, String tip, String initText) {
        super(context, R.style.CustomDialog);
        init(context, tip, initText);
    }

    public InputNumberDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected InputNumberDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context, String tip, String initText){
        this.context = context;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.inputsecuritypwd, null);
        etNumber = (EditText) v.findViewById(R.id.securitypwd);
        etNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (!TextUtils.isEmpty(initText))
            etNumber.setText(initText);
        title = (TextView) v.findViewById(R.id.dialog_title);
        posBtn = (Button) v.findViewById(R.id.negative_btn);
        nagBtn = (Button) v.findViewById(R.id.positive_btn);
        if (TextUtils.isEmpty(tip))
            title.setText("请输入");
        else
            title.setText(tip);
        posBtn.setOnClickListener(this);
        nagBtn.setOnClickListener(this);
        setCanceledOnTouchOutside(false);
        setContentView(v);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_HOME:
                    case KeyEvent.KEYCODE_BACK:

                        return true;
                }
                return false;
            }
        });
    }

    private void init(Context context) {
        init(context, null, null);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.positive_btn) {
            if (clickListener != null) {
                clickListener.onClick(ButtonType.POSITIVE, v);
            }else
                dismiss();

        } else if (i == R.id.negative_btn) {
            getWindow().isFloating();
            dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.NEGATIVE, v);
            }

        }
    }

    public void setInputText(String text){
        if (etNumber != null)
            etNumber.setText(text);
    }

    public String getInputText(){
        return etNumber.getText().toString();
    }
    public void clearPwdText(){
        etNumber.setText("");
    }

    public ButtonClickListener getClickListener() {
        return clickListener;
    }

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ButtonClickListener {
        void onClick(ButtonType button, View v);
    }

    public enum ButtonType {
        POSITIVE,
        NEGATIVE
    }
}