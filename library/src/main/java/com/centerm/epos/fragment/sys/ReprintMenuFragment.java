package com.centerm.epos.fragment.sys;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.centerm.dev.printer.PrinterDataAdvBuilder;
import com.centerm.dev.printer.PrinterDataParams;
import com.centerm.epos.R;
import com.centerm.epos.base.BaseFragment;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.ImgTools;

/**
 * create by liubit on 2019-09-06
 */
public class ReprintMenuFragment extends BaseFragment{
    //private DeviceAPI device = null;
    private PrintThread printThread;//

    @Override
    protected void onInitView(View view) {
        //device = new DeviceAPI();

        view.findViewById(R.id.mBtnPrintLast).setOnClickListener(this);
        view.findViewById(R.id.mBtnPrintAny).setOnClickListener(this);

        printThread = new PrintThread();
        printThread.start();
    }

    @Override
    public void onClick(View v) {
        if(CommonUtils.isFastClick()){
            return;
        }
        if(v.getId()==R.id.mBtnPrintLast){
            //device.printString("RQC:4A4E5CF386E52A31\nRQC:4A4E5CF386E52A31\nRQC:4A4E5CF386E52A31");
            //printLast();
        }else if(v.getId()==R.id.mBtnPrintAny){

        }
    }

    private void printLast() {
        PrinterDataAdvBuilder advBuilder = new PrinterDataAdvBuilder();
        try {
            PrinterDataParams p = new PrinterDataParams();
            p.setAlign(PrinterDataParams.Align.right);
            advBuilder.addText("--------------------------------");
            advBuilder.addText("商户存根", p);
            advBuilder.addText("--------------------------------");
            advBuilder.addText("商户名称        福州市永元建材店");
            advBuilder.addText("商户编号         847391057120192");
            advBuilder.addText("终端编号:66072763    操作员号:01");
            advBuilder.addText("--------------------------------");
            advBuilder.addText("收 单 行:               48478710");
            advBuilder.addText("发 卡 行:               兴业银行");
            advBuilder.addText("卡    号");
            p.setYScale(2.0f);
            p.setBold(true);
            advBuilder.addText("622908********5588 /I", p);
            advBuilder.addText("卡序列号:                    001");
            advBuilder.addText("交易类型:");
            advBuilder.addText("消费", p);
            advBuilder.addText("批次号:000006      凭证号:000058");
            advBuilder.addText("参考号:051018210647");
            advBuilder.addText("日期时间:    2018/05/10 18:02:46");
            advBuilder.addText("金  额:");
            advBuilder.addText("RMB 88000.00", p);
            advBuilder.addText("--------------------------------");
            advBuilder.addText("备注:");
            advBuilder.addText("ARQC:4A4E5CF386E52A31", 16);
            advBuilder.addText("AID:A000000333010101", 16);
            advBuilder.addText("CSH:001 CUM:420300", 16);
            advBuilder.addText("TSI:F800 TVR:008004E800", 16);
            advBuilder.addText("ATC:0009 UNPR NO:D23FE3FA", 16);
            advBuilder.addText("ATP:7C00 TermCap:E0E9C8", 16);
            advBuilder.addText("IAD:0701010360A002010A01000000000026FC94FD", 16);
            advBuilder.addText("APPLAB:PBOC DEBIT",16);
            advBuilder.addText("APPNAME:PBOC DEBIT",16);
            advBuilder.addText("--------------------------------");
            advBuilder.addText("持卡人签名:",16);
            advBuilder.addText("\n\n\n");
            advBuilder.addText("--------------------------------");
            advBuilder.addText("本人确认以上交易,同意将其计入本卡账户\n\n",16);
            Bitmap bmp = advBuilder.build();
            //device.printImage(ImgTools.getInstance().Bitmap2InputStream(bmp));
            //device.printImage(getActivity().getResources().openRawResource(R.drawable.print_logo2));
            //device.printString("RQC:4A4E5CF386E52A31\nRQC:4A4E5CF386E52A31\nRQC:4A4E5CF386E52A31");
        }catch (Exception e){
            logger.error(e.toString());
        }
    }

    private class PrintThread extends Thread{
        Handler handler;
        boolean hasPaper;//�û����ж��Ƿ�ȱֽ
        public PrintThread() {
            hasPaper =true;
        }

        private void setHandler() {
            handler = new Handler(){

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }

            };
        }

        @Override
        public void run() {
            super.run();
            //��Ϣѭ��
            Looper.prepare();
            setHandler();
            Looper.loop();
            Log.e("===", "quitHandler");
        }


        public void exitLoop()
        {
            handler.getLooper().quit();//�˳���Ϣѭ��
        }

        public void putData()
        {
            handler.sendEmptyMessage(0);
        }
    }

    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            logger.error("msg.what ->" + msg.what);
            switch (msg.what) {
                case -1://δ֪
                    logger.error("print errer Unknown");
                    break;
                default:
                    logger.error("print errer Unknown");
                    break;
            }

        }
    };

    @Override
    public void onDestroy() {
        printThread.exitLoop();
        printThread = null;
        super.onDestroy();
    }

    @Override
    protected int onLayoutId() {
        return R.layout.fragment_reprint_menu;
    }
}
