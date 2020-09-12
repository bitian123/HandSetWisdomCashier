package com.centerm.epos.base;

import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.centerm.epos.R;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.view.AlertDialog;
import com.centerm.epos.view.ContractInfoDialog;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import config.Config;

/**
 * author:wanliang527</br>
 * date:2017/2/9</br>
 */

public abstract class BaseTradeFragment extends BaseFragment implements ITradeView {
    private static final String TAG = BaseTradeFragment.class.getSimpleName();
    //生成业务逻辑的实现
    public ITradePresent mTradePresent;

    private String presentClzName;

    public BaseTradeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle param = getArguments();
        if (param != null){
            presentClzName = param.getString(TradeFragmentContainer.PRESENT_CLASS_NAME);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
       mTradePresent.onCancel();
       return true;
    }

    /*protected void hideBackBtn(){
        getHostActivity().hideBackBtn();
    }*/

    @Override
    protected void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
        mTradePresent = newTradePresent();
        mTradePresent.OpenDatabase();
        mTradePresent.onInitLocalData(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    protected ITradePresent newTradePresent(){
        if (TextUtils.isEmpty(presentClzName))
            return null;
        try {
            Class[] paramsType = {ITradeView.class};
            Class clz = Class.forName(presentClzName);
            Constructor constructor = clz.getDeclaredConstructor(paramsType);
            return (ITradePresent) constructor.newInstance(this);
        } catch (Exception e) {
            XLogUtil.i(TAG, "^_^ " + e.getMessage() + " ^_^");
            return null;
        }
    }

    @Override
    protected void afterInitView() {
        super.afterInitView();
        mTradePresent.beginTransaction();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(TransCode.codeMapName(mTradePresent.getTradeCode()));
        if (mTradePresent!=null&&mTradePresent.isEnableShowingTimeout()) {
            getHostActivity().openPageTimeout();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTradePresent.isEnableShowingTimeout()) {
            getHostActivity().closePageTimeout();
        }
    }

    @Override
    public void onDestroy() {
        destroy();
        super.onDestroy();
    }

    @Override
    public void show() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void destroy() {
        mTradePresent.release();
        mTradePresent = null;
    }

    @Override
    public void popToast(String content, int displayTime) {

    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    @Override
    public void popToast(final int resId) {
        if (isMainThread())
            ViewUtils.showToast(getActivity(), resId);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.showToast(getActivity(), resId);
                }
            });
        }
    }

    @Override
    public void popToast(int resId, int displayTime) {

    }

    @Override
    public void popToast(final String content){
        if (isMainThread())
            ViewUtils.showToast(getActivity(), content);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewUtils.showToast(getActivity(), content);
                }
            });
        }
    }

    @Override
    public void popMessageBox(String title, String content) {

    }

    @Override
    public boolean onExistPressed() {
        return false;
    }

    @Override
    public void showTipDialog(String tip) {
        DialogFactory.showLoadingDialog(getActivity(), tip);
    }

    @Override
    public void popMessageBox(String title, String content, AlertDialog.ButtonClickListener listener) {

    }

    @Override
    public void popLoading(String message) {

    }

    @Override
    public void popLoading(int resId) {
        DialogFactory.showLoadingDialog(getActivity(), getString(resId));
    }

    @Override
    public void registerComponent(int type, Class clz) {

    }

    @Override
    public void registerComponent(int type) {

    }

    @Override
    public String getStringFromResource(int resID) {
        return getActivity().getString(resID);
    }

    @Override
    public TradeFragmentContainer getHostActivity() {
        return (TradeFragmentContainer) getActivity();
    }


    @Override
    public void showSelectDialog(String title, String message, AlertDialog.ButtonClickListener listener) {
        DialogFactory.showSelectDialog(getActivity(), title, message, listener);
    }

    @Override
    public void showSelectDialog(int titleId, int messageId, AlertDialog.ButtonClickListener listener) {
        DialogFactory.showSelectDialog(getActivity(), getString(titleId), getString(messageId), listener);
    }

    @Override
    public void showMessageDialog(int titleId, int messageId, AlertDialog.ButtonClickListener listener) {
        DialogFactory.showMessageDialog(getActivity(), getString(titleId), getString(messageId), listener);
    }

    @Override
    public void showContractInfoDialog(int titleId, Map<String, String> map, ContractInfoDialog.ButtonClickListener listener) {
        DialogFactory.showContractInfoDialog(getActivity(), getString(titleId), map, listener,false);
    }



    @Override
    public boolean onBacKeyPressed() {
        return false;
    }


/*@author:zhouzhihua
* 蓝色UI 金额 流水号 主管密码 结果显示 需要设置title的image
* */
    public void setTitlePicture(View rootView, int drawableId){
        if( !Settings.bIsSettingBlueTheme() ){
            return ;
        }
        ImageView titleImage = (ImageView)rootView.findViewById(R.id.title_pic_show);
        if( titleImage != null ){
            titleImage.setImageResource(drawableId);
        }
    }

    public void clearTitlePicture(View rootView){
        if( !Settings.bIsSettingBlueTheme() ){
            return ;
        }
        ImageView titleImage = (ImageView)rootView.findViewById(R.id.title_pic_show);
        if( titleImage != null ){
            titleImage.setImageDrawable(null);
        }
    }

    public void initFinishBtnlistener(View view){
        if (view == null) {
            return;
        }
        ImageButton button = (ImageButton) view.findViewById(R.id.mBtnFinish);
        if(button!=null){
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(getHostActivity()!=null){
                        getHostActivity().finish();
                    }
                }
            });
        }
    }

    /**
     * 生成订单号
     * */
    public String creatOrderNo(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String time= formatter.format(new Date());
        StringBuilder strRand = new StringBuilder();
        for(int i=0;i<4;i++){
            strRand.append((int)(Math.random() * 10));
        }
        String random = strRand.toString();
        String orderNo = "C"+ time+random;
        return orderNo;
    }
}
