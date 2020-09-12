package com.centerm.epos.fragment.trade;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.present.transaction.TradingPresent;

/**
 * author:wanliang527</br>
 * date:2017/2/20</br>
 */

public class TradingFragment extends BaseTradeFragment implements ITradingView {
    private ImageView loadIcon;//加载图标
    private TextView hintShow;//提示文字
    TradingPresent mTradingPresent;

    @Override
    public int onLayoutId() {
        return getLayoutId("fragment_data_exchange", R.layout.fragment_data_exchange);
    }

    @Override
    public void onInitView(View rootView) {
        loadIcon = (ImageView) rootView.findViewById(getDrawableId("trading_logo_show", R.id.trading_logo_show));
        startAnim();
        hintShow = (TextView) rootView.findViewById(getDrawableId("hint_text_show", R.id.hint_text_show));
    }

    @Override
    protected ITradePresent newTradePresent() {
        mTradingPresent = (TradingPresent) super.newTradePresent();
        if (mTradingPresent == null) {
            mTradingPresent = new TradingPresent(this);
        }
        return mTradingPresent;
    }

    private void startAnim() {
        if (loadIcon != null && loadIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) loadIcon.getDrawable()).start();
        }
    }

    private void stopAnim() {
        if (loadIcon != null && loadIcon.getDrawable() instanceof AnimationDrawable) {
            ((AnimationDrawable) loadIcon.getDrawable()).stop();
        }
    }

    @Override
    public void updateHint(String content) {
        if (content == null) {
            hintShow.setText(R.string.tip_trading_default);
        } else {
            hintShow.setText(content);
        }
    }

    @Override
    public void updateHint(int stringID) {
        updateHint(getString(stringID));
    }

    @Override
    public void refresh() {
        super.refresh();
    }
}
