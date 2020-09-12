package com.centerm.epos.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.centerm.epos.ActivityStack;
import com.centerm.epos.R;
import com.centerm.epos.db.DbHelper;

import org.apache.log4j.Logger;

/**
 * Created by 94437 on 2017/6/28.
 */

public class inputSecurityDialog extends Dialog implements View.OnClickListener {
    protected ActivityStack activityStack = ActivityStack.getInstance();
    protected Logger logger = Logger.getLogger(this.getClass());
    private EditText securitypwd;
    private TextView title;
    private Button posBtn,nagBtn;
    private Context context;
    private ButtonClickListener clickListener;
    public inputSecurityDialog(Context context) {
        super(context, R.style.CustomDialog);
        init(context);
    }

    public inputSecurityDialog(Context context, int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected inputSecurityDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.inputsecuritypwd, null);
        securitypwd = (EditText) v.findViewById(R.id.securitypwd);
        securitypwd.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        securitypwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        title = (TextView) v.findViewById(R.id.dialog_title);
        posBtn = (Button) v.findViewById(R.id.negative_btn);
        nagBtn = (Button) v.findViewById(R.id.positive_btn);
        title.setText("请输入安全密码");
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


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.positive_btn) {
            dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.POSITIVE, v);
            }

        } else if (i == R.id.negative_btn) {
            getWindow().isFloating();
            dismiss();
            if (clickListener != null) {
                clickListener.onClick(ButtonType.NEGATIVE, v);
            }

        }
    }

    public String getInputText(){
        return securitypwd.getText().toString();
    }
    public void clearPwdText(){
        securitypwd.setText("");
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