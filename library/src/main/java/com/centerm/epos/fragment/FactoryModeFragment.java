package com.centerm.epos.fragment;

import android.os.Bundle;
import android.view.View;

import com.centerm.epos.R;
import com.centerm.epos.activity.MainActivity;
import com.centerm.epos.base.MenuFragment;
import com.centerm.epos.mvp.presenter.FactoryMenuPresenter;
import com.centerm.epos.mvp.view.IFactoryMenuView;

/**
 * author:wanliang527</br>
 * date:2017/2/19</br>
 */

public class FactoryModeFragment extends MenuFragment implements IFactoryMenuView {

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        setPresenter(new FactoryMenuPresenter(this));
    }

    @Override
    protected void onInitView(View view) {
        super.onInitView(view);
        getHostActivity().setTitle(R.string.title_factory_mode);
        getHostActivity().hideRightButton();
    }

    @Override
    public MainActivity getMainActivity() {
        return (MainActivity) getHostActivity();
    }
}
