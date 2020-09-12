package com.centerm.epos.task;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.widget.Toast;

import com.centerm.epos.function.TmkParameterImport;
import com.centerm.epos.utils.DialogFactory;

/**
 * 串口通讯
 */

public class AsyncImportTmkTask extends BaseAsyncTask {
    private Context context;
    private TmkParameterImport mTerminalParameter;
    private String tradeCode;

    public AsyncImportTmkTask(Context context) {
        super(context);
        this.context = context;
        mTerminalParameter = new TmkParameterImport(context);
    }

    public AsyncImportTmkTask(Context context,String tradeCode) {
        super(context);
        this.context = context;
        mTerminalParameter = new TmkParameterImport(context);
        this.tradeCode = tradeCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        DialogFactory.showLoadingDialog(context, "通讯中，请稍候", new DialogInterface
                .OnKeyListener(){

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                    if(!mTerminalParameter.stopImport()){
                        Toast.makeText(context, "正在通讯中，请稍候！", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected Object doInBackground(Object[] params) {
        return mTerminalParameter.importParameter(tradeCode);
    }

    @Override
    public void onFinish(Object o) {
        super.onFinish(o);
        DialogFactory.hideAll();
        if (o instanceof Boolean) {
            if((Boolean) o){
                Toast.makeText(context, "通讯成功！", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, "通讯失败！", Toast.LENGTH_SHORT).show();
            }
        } else {
            String errorCode = "-1";
            if(o!=null){
                errorCode = (String) o;
            }

            Toast.makeText(context, mTerminalParameter.isTerminalByUser() ? "取消通讯" : getMsgByCode(errorCode), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMsgByCode(String code){
        String msg = "通讯失败！";
        switch (code){
            case "O1":
                msg = "签到成功";
                break;
            case "O2":
                msg = "交易成功";
                break;
            case "O3":
                msg = "查询成功";
                break;
            case "Y4":
                msg = "找不到原交易";
                break;
            case "A3":
                msg = "校验密钥错";
                break;
            case "A4":
                msg = "未签到,请重新签到";
                break;
            case "A5":
                msg = "脱机交易失败";
                break;
            case "A6":
                msg = "卡片拒绝";
                break;
            case "XX":
                msg = "交易异常";
                break;
            case "XY":
                msg = "交易人为取消";
                break;
            case "X4":
                msg = "无交易流水";
                break;
            case "XB":
                msg = "交易流水满，请结算";
                break;
            case "X5":
                msg = "签到校验错";
                break;
            case "X6":
                msg = "数据发送失败";
                break;
            case "X7":
                msg = "数据接收失败";
                break;
            case "X8":
                msg = "数据接收有误";
                break;
            case "X9":
                msg = "结算前脱机交易批上送失败，须打故障单";
                break;
            case "XP":
                msg = "校验workkey错";
                break;
            case "L1":
                msg = "LRC校验错";
                break;
            case "40":
                msg = "查询失败";
                break;
        }
        return msg;
    }
}
