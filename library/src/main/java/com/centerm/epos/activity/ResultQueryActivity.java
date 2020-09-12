package com.centerm.epos.activity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.centerm.epos.base.BaseFragmentActivity;
import com.centerm.epos.bean.PrintReceiptBean;
import com.centerm.epos.fragment.trade.SignatureAbnormalFragment;
import com.centerm.epos.model.ITradeParameter;
import com.centerm.epos.utils.DialogFactory;
import com.centerm.epos.utils.OkHttpUtils;
import com.centerm.epos.utils.OnCallListener;
import com.centerm.epos.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import config.Config;

/**
 * create by liubit on 2019-10-17
 */
public class ResultQueryActivity extends BaseFragmentActivity {
    private List<String> printList = new ArrayList<>();
    private int total = 0;
    private int successNum = 0;
    private int failNum = 0;

    @Override
    public void onInitView() {
        Bundle bundle = getIntent().getBundleExtra(ITradeParameter.KEY_TRANS_PARAM);
        SignatureAbnormalFragment fragment = new SignatureAbnormalFragment();
        fragment.setArguments(bundle);
        replace(fragment).commit();
    }

    public void signOK(PrintReceiptBean bean){
        if(bean!=null) {
            printPDF(bean);
        }

        setResult(88);
        finish();
    }

    public void printPDF(PrintReceiptBean data){
        printList.clear();
        total = 0;
        if(data!=null){
            DialogFactory.showLoadingDialog(this, "正在下载单据\n请稍侯");
            for(PrintReceiptBean.BodyBean bodyBean:data.getBody()){
                for(int i=0;i<bodyBean.getPrint().size();i++){
                    total++;
                    PrintReceiptBean.BodyBean.PrintBean printBean = bodyBean.getPrint().get(i);
                    String suffix = ".pdf";
                    try {
                        String[] strs = printBean.getImage().split("\\.");
                        if(strs!=null&&strs.length>1){
                            suffix = "."+strs[strs.length-1];
                        }
                    }catch (Exception e){
                        logger.error(e.toString());
                    }
                    getPDF(bodyBean.getSubOrderId()+"_print_"+(i+1)+suffix, printBean.getImage());
                }
            }
        }
    }

    private void getPDF(final String fileName, final String url){
        File file = new File(Config.Path.PDF_PATH, fileName);
        OkHttpUtils.getInstance().downloadFile(file, url, new OnCallListener() {
            @Override
            public void onCall(Map<String, Object> result) {
                try {
                    if(result!=null){
                        successNum++;
                        logger.info(fileName+" -> 下载完成 -> "+result.get("path"));
                        printList.add((String) result.get("path"));
                    }else {
                        failNum++;
                        logger.error("单据下载失败");
                    }
                    logger.debug("successNum:"+successNum);
                    logger.debug("failNum:"+failNum);
                    logger.debug("total:"+total);
                    if(successNum+failNum==total){
                        DialogFactory.hideAll();
                        if(successNum>0){
                            print();
                        }else {
                            ViewUtils.showToast(ResultQueryActivity.this, "无可打印单据");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    ViewUtils.showToast(ResultQueryActivity.this, "通讯异常");
                }

            }
        });
    }

    public void print(){
        if(printList!=null&&printList.size()>0){
            for(String p:printList){
                logger.error("打印数据："+p);
                openFile(this, p);
            }
        }
    }

    private void openFile(Context context, String path) {
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(context, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //设置intent的Action属性
            intent.setAction(Intent.ACTION_VIEW);
            //获取文件file的MIME类型
            String type = "application/pdf";
            //设置intent的data和Type属性。
            intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
            //跳转
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ViewUtils.showToast(context,"文件打开错误，单据打印失败！");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                logger.info("^_^ KEYCODE_BACK key is pressed but ignored by BaseActivity ^_^");
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
