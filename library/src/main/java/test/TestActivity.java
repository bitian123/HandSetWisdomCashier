package test;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import com.centerm.epos.R;
import com.centerm.epos.base.BaseActivity;
import com.centerm.epos.configure.ConfigureManager;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.print.receipt.EleReceiptCreator;
import com.centerm.epos.xml.bean.slip.SlipElement;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * author:wanliang527</br>
 * date:2017/2/8</br>
 */

public class TestActivity extends BaseActivity {

    @Override
    public void onInitLocalData(Bundle savedInstanceState) {
        super.onInitLocalData(savedInstanceState);
    }

    @Override
    public int onLayoutId() {
        return R.layout.activity_app_update;
    }

    @Override
    public void onInitView() {

    }

    @Override
    public void afterInitView() {
        super.afterInitView();
        EleReceiptCreator creator = EleReceiptCreator.newInstance(Environment.getExternalStorageDirectory().toString(), "lwl", "00001");
        creator.demo(context);

//        XmlParser.parseConfigCatalog(context,"BASE/ConfigCatalog.xml");
//        XmlParser.parseMenu(context,"BASE/menu/primary_menu.xml");
      /*  ConfigureManager manager = ConfigureManager.getInstance(this);
        final PrintManager printManager = new PrintManager(context);
        printManager.importTemplate();

        final Map<String, String> map = new HashMap<>();
        map.put("1F04", "星网锐捷科技园大宝剑足疗店");
        map.put("1F05", "123456789012");*/
//        map.put("1F06", "111111");
//        map.put("1F07", "01");
//        map.put("1F08", "12345678");
//        map.put("1F09", "87654321");
//        map.put("1F10", "622200*********5978 /C");
//        map.put("1F11", "24/11");
//        map.put("1F12", "消费(SALE)");
//        map.put("1F13", "000033");
//        map.put("1F14", "019909");
//        map.put("1F15", "542342");
//        map.put("1F16", "323455552234");
//        map.put("1F17", "2017-01-01 12:00:01");
//        map.put("1F18", "RMB 20.89");
//        map.put("1F19", "App Label:PBOC CREDIT\nARQC:DFCCBB1E34LLSDFNL\nAID:A0000000002349234\nTVR:008004E00 TSI:FC00  ATC:0166\nCSN:001 AIP:7C00 CVMR:4230300\nIAD:093043289SDJEKWU89977489j");

/*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                printManager
                        .prepare(PrintManager.SlipOwner.MERCHANT)
                        .setValue(map)
                        *//*.setBold("1F03", true)
                        .setBold("1F04", true)
                        .setValue("1F14", "我是凭证号")
                        .addElementAtFront(new SlipElement("2F01", null, "2F01添加最前面"))
                        .addElementAtLast(new SlipElement("2F02", "2F02", "添加最后面"))
                        .addElementAhead("1F11", new SlipElement("2F03", "2F03", "添加在有效期前面"))*//*
                        .addInterpolator(new PrintManager.StatusInterpolator() {
                            @Override
                            public void onPrinting() {
                                logger.warn("打印中");
                            }

                            @Override
                            public void onFinish() {
                                logger.warn("打印完成");
                            }

                            @Override
                            public void onError(int errorCode, String errorInfo) {
                                logger.warn("打印错误，错误码：" + errorCode + "==>错误信息：" + errorInfo);
                            }
                        })
                        .print();
            }
        }, 2000);*/


//        printManager.prepare().setValue()


//        logger.warn(manager.getSlipTemplate(context));
//        logger.warn(manager.getRedevelopMap(context));
//        logger.warn(manager.getTradeProcess(context,"saLe.xml"));
//        logger.warn(manager.getBaseParamsMap(context));
//        logger.warn(manager.getBaseFuncToggle(context));
//        logger.warn(manager.getDefaultParams(context));

//        manager.setProject(context, EnumProject.QIANBAO);
       /* Menu menu1 = manager.getPrimaryMenu(context);
        if (menu1!=null){
            logger.warn(menu1);
        }
        Menu menu2 = manager.getSecondaryMenu(context);
        if (menu2!=null){
            logger.warn(menu2);
        }
        Menu menu3 = manager.getThirdlyMenu(context);
        if (menu3!=null){
            logger.warn(menu3);
        }*/
    }
}
