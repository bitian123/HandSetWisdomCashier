package com.centerm.epos.ebi.ui.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;

import com.centerm.cpay.ai.lib.CpayAiService;
import com.centerm.epos.base.BaseTradeFragment;
import com.centerm.epos.base.ITradePresent;
import com.centerm.epos.ebi.R;
import com.centerm.epos.ebi.present.ReadIdCardPresent;
import com.centerm.epos.utils.OnTimeOutListener;
import com.centerm.epos.utils.ViewUtils;

/**
 * 读身份证
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class ReadIdCardFragment extends BaseTradeFragment implements View.OnClickListener {
    private ReadIdCardPresent present;
    private CpayAiService cpayaiService;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.info("服务绑定失败");
            ViewUtils.showToast(getActivity(), "读取身份证服务绑定失败，请退出后重试");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            cpayaiService = CpayAiService.Stub.asInterface(service);
            logger.info("服务绑定成功");
            if(cpayaiService!=null){
                try {
                    present.readIDCard(cpayaiService);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }else {
                logger.info("服务绑定失败");
                ViewUtils.showToast(getActivity(), "读取身份证服务绑定失败，请退出后重试");
            }
        }
    };

    @Override
    protected ITradePresent newTradePresent() {
        present = new ReadIdCardPresent(this);
        return present;
    }

    @Override
    public int onLayoutId() {
        return R.layout.fragment_read_idcard;
    }

    @Override
    public void onInitView(View rootView) {
        bindService();

        showingTimeout((TextView) rootView.findViewById(R.id.mTvShowTimeOut), new OnTimeOutListener() {
            @Override
            public void onCall(int time) {
                if(time<=0){
                    cancelTimeout();
                    present.stopReadIDCard(cpayaiService);
                    if(getActivity()!=null){
                        getActivity().finish();
                    }
                }
            }
        });
    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setAction("com.centerm.cpay.ai.service");
        intent.setPackage("com.centerm.cpay.ai");
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        try {
            if(cpayaiService!=null) {
                getActivity().unbindService(conn);
                cpayaiService.stopDetectIDCard();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle("刷身份证");
    }


}
