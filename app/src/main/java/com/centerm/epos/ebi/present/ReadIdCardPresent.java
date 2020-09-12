package com.centerm.epos.ebi.present;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import com.centerm.cpay.ai.lib.CpayAiService;
import com.centerm.cpay.ai.lib.IdInfo;
import com.centerm.cpay.ai.lib.OnResultCallbackListener;
import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.utils.DialogFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import config.Config;

/**
 * author:liubit</br>
 * date:2019/9/2</br>
 */
public class ReadIdCardPresent extends BaseTradePresent {
    public static final int SHOW_MSG = 1;

    public ReadIdCardPresent(ITradeView mTradeView) {
        super(mTradeView);
    }

    public void readIDCard(final CpayAiService cpayaiService) throws RemoteException {
        Bundle data = new Bundle();
        data.putString("appId", Config.appId);
        data.putString("appSecret", Config.appSecret);
        boolean result = false;
        try {
            result = cpayaiService.login(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        logger.info("授权登录结果 -> "+result);
        if(result){
            data.putInt("timeout", 65);
            data.putString("type", "0");
            cpayaiService.detectIDCard(data, new OnResultCallbackListener.Stub() {
                @Override
                public void onResult(int i, Bundle bundle) throws RemoteException {
                    logger.info("result -> "+i);
                    if(i==1){//身份证读取成功
                        DialogFactory.hideAll();
                        bundle.setClassLoader(getClass().getClassLoader());
                        IdInfo idInfo = bundle.getParcelable("idInfo");
                        if(idInfo==null|| TextUtils.isEmpty(idInfo.name)||TextUtils.isEmpty(idInfo.idnum)){
                            tipError(0);
                            gotoNextStep("99");
                            return;
                        }
                        transDatas.put(JsonKeyGT.idNo, idInfo.idnum);
                        transDatas.put(JsonKeyGT.name, idInfo.name);
                        transDatas.put(JsonKeyGT.idType, "0");
                        gotoNextStep("1");
                    }else if(i==15){
                        if(mTradeView!=null&&mTradeView.getHostActivity()!=null) {
                            mTradeView.getHostActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mTradeView.showTipDialog("读取中\n请勿将身份证移开");
                                }
                            });
                        }
                    }else {
                        DialogFactory.hideAll();
//                        tipError(i);
//                        gotoNextStep("99");
                        // todo 临时写死数据
                        transDatas.put(JsonKeyGT.idNo, "410523199001016039");
                        transDatas.put(JsonKeyGT.name, "刘艳伟");
                        transDatas.put(JsonKeyGT.idType, "0");
                        gotoNextStep("1");
                    }
                }

            });
        }else {
            DialogFactory.hideAll();
            mTradeView.popToast("授权登录失败");
            gotoNextStep("99");
        }
    }

    public void stopReadIDCard(CpayAiService cpayaiService){
        try {
            if(cpayaiService!=null) {
                cpayaiService.stopDetectIDCard();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void tipError(int error){
        switch (error){
        case 0:
            mTradeView.popToast("身份证读取错误");
            break;
        case 2:
            mTradeView.popToast("参数异常");
            break;
        case 3:
            mTradeView.popToast("库中无数据");
            break;
        case 4:
            mTradeView.popToast("系统异常");
            break;
        case 5:
            mTradeView.popToast("读卡超时");
            break;
        case 6:
            mTradeView.popToast("网络错误");
            break;
        case 7:
            mTradeView.popToast("手动取消");
            break;
        case 8:
            mTradeView.popToast("识别传入的图片有误");
            break;
        case 9:
            mTradeView.popToast("传入的图片为空或者不存在");
            break;
        case 10:
            mTradeView.popToast("未匹配到人像");
            break;
        case 11:
            mTradeView.popToast("超时错误");
            break;
        case 12:
            mTradeView.popToast("非法终端");
            break;
        case 13:
            mTradeView.popToast("次数用尽");
            break;
        case 14:
            mTradeView.popToast("查询异常");
            break;
        default:
            break;
        }
    }

    private String format(String msg){
        Pattern pattern = Pattern.compile("([^\u0000]*)");
        Matcher matcher = pattern.matcher(msg);
        if(matcher.find(0)){
            msg = matcher.group(1);
        }
        return msg;
    }

}
