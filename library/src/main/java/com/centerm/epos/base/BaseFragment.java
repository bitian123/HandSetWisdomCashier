package com.centerm.epos.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.StatusCode;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.msg.GTHttpJsonMessage;
import com.centerm.epos.msg.ITransactionMessage;
import com.centerm.epos.msg.PosISO8583Message;
import com.centerm.epos.present.communication.DataExchanger;
import com.centerm.epos.present.communication.DataExchangerFactory;
import com.centerm.epos.present.communication.HttpCommParameterGT;
import com.centerm.epos.present.communication.ICommunication;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.OnTimeOutListener;
import com.centerm.epos.utils.ViewUtils;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import config.Config;

/**
 * Fragment的基础类。所有的Fragment都必须继承该类
 * author:wanliang527</br>
 * date:2017/2/9</br>
 */
public abstract class BaseFragment extends Fragment implements View.OnClickListener {
    protected static final String KEY_MENU = "KEY_MENU";
    protected static final int REQ_TRANSACTION = 0x01;
    protected ITransactionMessage factory;
    protected ITransactionMessage factory2;
    private Timer timer;
    private TimerTask task;
    private int time = Config.TIME_OUT;
    public TextView mTvShowTimeOut;

    protected Logger logger = Logger.getLogger(this.getClass());
    /**
     * 初始化本地数据，在创建该Fragment的时候进行{@method onCreate}。
     *
     * @param savedInstanceState 上一次的状态保存，用于状态恢复
     */
    protected void onInitLocalData(Bundle savedInstanceState) {
    }

    /**
     * 初始化界面，每次{@method onCreateView}时，都会伴随该方法的调用
     *
     * @param view View
     */
    protected abstract void onInitView(View view);

    /**
     * 返回布局ID，由子类实现，指明了该片段用到的布局ID。
     * 相关方法{@method onView}
     *
     * @return 界面的布局ID
     */
    protected abstract int onLayoutId();

    /**
     * 直接返回布局视图，如果子类复写了该方法，且返回的视图对象不为空，则{@method onLayoutId}返回的布局ID将被忽略
     *
     * @return 该界面的视图对象
     */
    protected View onView() {
        return null;
    }

    /**
     * 在{@link #onInitView(View)}之后，需要执行的业务逻辑代码
     */
    protected void afterInitView() {

    }

    /**
     * 设置标题
     *
     * @param title 标题
     */
    protected void setTitle(CharSequence title) {
        View view = getView();
        if (view == null) {
            return;
        }
        TextView titleShow = (TextView) getView().findViewById(R.id.txtvw_title);
        if (titleShow != null) {
            titleShow.setText(title);
        }
    }

    /**
     * 设置标题
     *
     * @param titleId 标题
     */
    protected void setTitle(int titleId) {
        View view = getView();
        if (view == null) {
            return;
        }
        TextView titleShow = (TextView) getView().findViewById(R.id.txtvw_title);
        if (titleShow != null) {
            titleShow.setText(titleId);
        }
    }

    /**
     * 显示返回按钮
     */
    protected void showBackBtn() {
        View view = getView();
        if (view == null) {
            return;
        }
        ImageButton button = (ImageButton) view.findViewById(R.id.imgbtn_back);
        button.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏返回按钮
     */
    protected void hideBackBtn() {
        View view = getView();
        if (view == null) {
            return;
        }
        ImageButton button = (ImageButton) view.findViewById(R.id.imgbtn_back);
        if(button!=null){
            button.setVisibility(View.GONE);
        }
    }

    /**
     * 返回按钮点击事件
     */
    public boolean onBackPressed() {
        //Fragment的返回按钮事件默认跟宿主Activity的返回事件一致
//        getHostActivity().onBackPressed();
        return false;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onCreate");
        super.onCreate(savedInstanceState);
        onInitLocalData(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onCreateView");
        View view = onView();
        int layoutId = onLayoutId();
        if (view == null && layoutId > 0) {
            view = inflater.inflate(layoutId, null);
        }
        if (view == null) {
            view = super.onCreateView(inflater, container, savedInstanceState);
        }
        if (view != null) {
            ImageButton backBtn = (ImageButton) view.findViewById(R.id.imgbtn_back);
            if (backBtn != null) {
                backBtn.setOnClickListener(this);
            }
        }
        /*
        * fragment 叠加的时候防止下层响应点击事件
        * author zhouzhihua 2017.12.28
        * */
        if ( view != null ) { view.setClickable(true); }

        onInitView(view);
        afterInitView();
        return view;
    }

    @Override
    public void onStart() {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onPause");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        cancelTimeout();
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onDestroy");
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        logger.debug(this.getClass().getSimpleName() + "==>" + toString() + "==>onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }

    public BaseFragmentActivity getHostActivity() {
        Activity activity = super.getActivity();
        if (activity == null || !(activity instanceof BaseFragmentActivity)) {
            throw new IllegalStateException("All the activity that contains fragments must be extends BaseFragmentActivity!");
        }
        return (BaseFragmentActivity) activity;
    }

    @Override
    public Context getContext() {
        return getHostActivity();
    }

    /**
     * 根据当前项目获取布局文件的ID。
     * 布局文件命名规则：[项目代号]_[基础版本文件名称]
     * 例如：基础版本中文件命名为activity_login.xml，对应钱宝项目的布局文件命名为c001_activity_login.xml
     *
     * @param commonName 基础版本中布局文件名称
     * @return 当前项目的布局ID，可能为0，此时需要上层自行处理
     */
    protected int getLayoutId(String commonName) {
        return getHostActivity().getLayoutId(commonName);
    }

    /**
     * 获取项目的资源ID，如果未获取到则返回原ID
     * @param idTag 资源名称/标识
     * @param id    基础版本资源ID
     * @return  资源ID
     */
    protected int getLayoutId(String idTag, int id) {
        int layoutId = getLayoutId(idTag);
        if (layoutId <= 0){
            layoutId = id;
        }
        return layoutId;
    }

    protected int getDrawableId(String idTag, int id) {
        int drawableId = getHostActivity().getDrawableId(idTag);
        if (drawableId <= 0){
            drawableId = id;
        }
        return drawableId;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgbtn_back) {
            onBackPressed();
        }
    }

    public boolean bIsBlueTheme() {
        return Settings.bIsSettingBlueTheme();
    }

    public void showingTimeout(final TextView mTvShowTimeOut){
        if(mTvShowTimeOut!=null) {
            mTvShowTimeOut.setVisibility(View.VISIBLE);
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    time--;
                    if(getActivity()!=null){
                        if(time==0){
                            if(getActivity()!=null){
                                cancelTimeout();
                                ((TradeFragmentContainer)getHostActivity()).jumpToNext("9876");
                                return;
                            }
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(CommonUtils.isK9()){
                                    mTvShowTimeOut.setText("（ " + time + "s 未进行操作将自动退出账户）");
                                }else {
                                    mTvShowTimeOut.setText(time+"秒");
                                }
                            }
                        });

                    }
                }
            };
        }
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(task, 0, 1000);
    }

    public void showingTimeout(final TextView mTvShowTimeOut, final OnTimeOutListener listener){
        if(mTvShowTimeOut!=null) {
            mTvShowTimeOut.setVisibility(View.VISIBLE);
        }
        if (task == null) {
            task = new TimerTask() {
                @Override
                public void run() {
                    time--;
                    if(listener!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onCall(time);
                            }
                        });

                    }
                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mTvShowTimeOut!=null) {
                                    if(CommonUtils.isK9()){
                                        mTvShowTimeOut.setText("（ " + time + "s 未进行操作将自动退出账户）");
                                    }else {
                                        mTvShowTimeOut.setText(time+"秒");
                                    }
                                }
                            }
                        });

                    }
                }
            };
        }
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(task, 0, 1000);
    }

    public void cancelTimeout(){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        if(task!=null){
            task.cancel();
            task = null;
        }
    }

    /**
     * 开始发送数据，单个请求。
     */
    public void sendData(boolean showDialog, final String transCode, final Map<String,Object> dataMap, final OnCallListener listener) {
        if(showDialog){
            DialogFactory.showLoadingDialog(getActivity(), "通讯中，请稍侯");
        }
        new Thread() {
            @Override
            public void run() {
                if(factory==null){
                    factory = new GTHttpJsonMessage();
                }
                final Object msgPacket = factory.packMessage(transCode, dataMap);
                if (msgPacket == null) {
                    DialogFactory.hideAll();
                    logger.warn("请求报文为空，退出");
                    return;
                }

                if (msgPacket instanceof byte[]) {
                    try {
                        DataExchanger dataExchanger = new DataExchanger(ICommunication.COMM_HTTP, new HttpCommParameterGT(transCode));
                        sleep(200);
                        byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
                        if(!TransCode.authCheck.equals(transCode)){
                            DialogFactory.hideAll();
                        }

                        if (receivedData == null) {
                            logger.error("^_^ 接收数据失败！receivedData = null ^_^");
                        } else {
                            final Map<String, Object> mapData = factory.unPackMessage(transCode, receivedData);
                            if(getActivity()!=null){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        listener.onCall(mapData);
                                    }
                                });
                            }
                        }
                    } catch (Exception e) {
                        if(getActivity()!=null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DialogFactory.hideAll();
                                    listener.onCall(null);
                                }
                            });
                        }
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logger.warn("报文格式非字节数组");
                            DialogFactory.hideAll();
                        }
                    });
                }
            }
        }.start();
    }

    /**
     * 开始发送8583数据，单个请求。
     */
    public void send8583Data(boolean showDialog, final String transCode, final Map<String,Object> dataMap, final OnCallListener listener) {
        if(showDialog){
            DialogFactory.showLoadingDialog(getActivity(), "通讯中，请稍侯");
        }
        new Thread() {
            @Override
            public void run() {
                if(factory2==null){
                    factory2 = new PosISO8583Message(new HashMap<String, Object>());
                }
                final Object msgPacket = factory2.packMessage(transCode, dataMap);
                if (msgPacket == null) {
                    DialogFactory.hideAll();
                    logger.warn("请求报文为空，退出");
                    return;
                }

                if (msgPacket instanceof byte[]) {
                    try {
                        DataExchanger dataExchanger = DataExchangerFactory.getInstance();
                        sleep(200);
                        byte[] receivedData = dataExchanger.doExchange((byte[]) msgPacket);
                        DialogFactory.hideAll();
                        if (receivedData == null) {
                            logger.error("^_^ 接收数据失败！receivedData = null ^_^");
                        } else {
                            final Map<String, Object> mapData = factory2.unPackMessage(transCode, receivedData);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onCall(mapData);
                                }
                            });
                        }
                    } catch (Exception e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DialogFactory.hideAll();
                                listener.onCall(null);
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logger.warn("报文格式非字节数组");
                            DialogFactory.hideAll();
                        }
                    });
                }
            }
        }.start();
    }
}
