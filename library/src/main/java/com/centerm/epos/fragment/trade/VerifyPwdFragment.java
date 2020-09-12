package com.centerm.epos.fragment.trade;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.mvp.presenter.VerifyPwdPresenter;
import com.centerm.epos.mvp.view.IVerifyPwdView;
import com.centerm.epos.view.NumberPad;

/**
 * 验证主管密码的界面
 * author:wanliang527</br>
 * date:2017/3/2</br>
 */
public class VerifyPwdFragment extends BaseTradeFragment implements IVerifyPwdView {

    public VerifyPwdPresenter presenter;
    private CheckBox[] indicatorArr;

    @Override
    protected ITradePresent newTradePresent() {
        presenter = new VerifyPwdPresenter(this);
        return presenter;
    }

    @Override
    protected void onInitView(View view) {
        indicatorArr = new CheckBox[]{
                (CheckBox) view.findViewById(R.id.indicator1),
                (CheckBox) view.findViewById(R.id.indicator2),
                (CheckBox) view.findViewById(R.id.indicator3),
                (CheckBox) view.findViewById(R.id.indicator4),
                (CheckBox) view.findViewById(R.id.indicator5),
                (CheckBox) view.findViewById(R.id.indicator6)};
        NumberPad numPad = (NumberPad) view.findViewById(R.id.number_pad_show);
        numPad.setCallback(new NumberPad.KeyCallback() {
            @Override
            public void onPressKey(char i) {
                presenter.onHandleKey(i);
            }
        });

        setTitlePicture(view,R.drawable.pic_password);
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_verify_pwd;
    }

    @Override
    public void changeIndicator(int pinLen) {
        switch (pinLen) {
            case 0:
                for (int i = 0; i < 6; i++) {
                    indicatorArr[i].setChecked(false);
                }
                break;
            case 1:
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                indicatorArr[pinLen].setChecked(false);
                break;
            case 6:
                indicatorArr[pinLen - 2].setChecked(true);
                indicatorArr[pinLen - 1].setChecked(true);
                break;
        }
    }
}
