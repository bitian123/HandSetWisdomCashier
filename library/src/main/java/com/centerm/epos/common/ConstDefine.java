package com.centerm.epos.common;

import android.view.View;

/**
 * Created by zhozhihua
 * on 2018/1/15.
 * 用来定义使用的常量
 */

public class ConstDefine {
    /**
     * {@link com.centerm.epos.fragment.trade.InputCommonInfoFragment#onClick(View)} <br/>
     * {@link com.centerm.epos.present.transaction.InputCommonInfoPresenter#onConfirmClicked(String...)} <br/>
     * {@link com.centerm.epos.bean.TradeInfoRecord#originalAuthMode} <br/>
     * */
    public static final String OFFLINE_AUTH_CODE_POS = "00"; //00—POS 预授权，01—电话预授权，02—代授权
    public static final String OFFLINE_AUTH_CODE_TELEPHONE = "01"; //00—POS 预授权，01—电话预授权，02—代授权
    public static final String OFFLINE_AUTH_CODE_AUTH = "02"; //00—POS 预授权，01—电话预授权，02—代授权

    public static final int TRANS_STATE_NORMAL = 0x00;//交易正常状态
    public static final int TRANS_STATE_VOID = 0x01; // 交易被撤销
    public static final int TRANS_STATE_ADJUST = 0x02; // 交易被调整
    public static final int TRANS_STATE_ADJUST_UNUSE = 0x03; // 已经上送的交易被调整 会产生新的交易原交易 不参加统计 交易明细不打印

    /**
     * {@link com.centerm.epos.bean.TradeInfoRecord#transStatus} <br/>
     * */
    public static final int TRANS_STATUS_NORMAL = 0x00;//交易正常状态
    public static final int TRANS_STATUS_ARPC_ERR = 0x0400; //ic卡arpc错误仍然承兑的交易
    public static final int TRANS_STATUS_REFUSED = 0x0800; // IC卡脱机交易拒绝

    //0x1000-离线交易上送成功，0x2000-离线交易上送失败,0x4000-后台无应答 用来交易上送查询需要上送的数据
    public static final int OFFLINE_TRANS_STATU_UPLOAD_SUCCESS = 0x1000;
    public static final int OFFLINE_TRANS_STATU_UPLOAD_FAIL = 0x2000;
    public static final int OFFLINE_TRANS_STATU_UPLOAD_NORES = 0x4000;

    /**
     * "1"-离线交易上送成功，"2"-离线交易上送失败,"4"-后台无应答<br/>
     * {@link com.centerm.epos.bean.TradeInfoRecord#offlineTransUploadStatus} <br/>
     * */
    public static final String OFFLINE_TRANS_STATUS_UPLOAD_NORMAL = "0";
    public static final String OFFLINE_TRANS_STATUS_UPLOAD_SUCCESS = "1";
    public static final String OFFLINE_TRANS_STATUS_UPLOAD_FAIL = "2";
    public static final String OFFLINE_TRANS_STATUS_UPLOAD_NORES = "4";

    /**
     * {@link com.centerm.epos.task.AsyncUploadESignatureTask#doInBackground(String...)} <br/>
     * {@link com.centerm.epos.task.AsyncUploadESignatureTask#setUploadMode(int uploadMode)}<br/>
     * */
    public static final int ELEC_SIGN_UPLOAD_MODE_0 = 0; //默认暂未使用
    public static final int ELEC_SIGN_UPLOAD_MODE_1 = 1; //保留最后一笔联机交易数据，其它签名数据上送
    public static final int ELEC_SIGN_UPLOAD_MODE_2 = 2; //上送所有签名数据（注意：和结算时上送的差别）


}
