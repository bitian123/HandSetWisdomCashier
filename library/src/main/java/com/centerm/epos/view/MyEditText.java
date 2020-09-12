package com.centerm.epos.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

/**
 * create by liubit on 2019-09-04
 * 监听输入法软键盘关闭事件
 */
@SuppressLint("AppCompatCustomView")
public class MyEditText extends EditText {
    private BackListener listener;

    public MyEditText(Context context) {
        super(context);
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBackListener(BackListener listener) {
        this.listener = listener;
    }

    public interface BackListener {
        void back();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (listener != null) {
                listener.back();
            }
        }
        return false;
    }

}
