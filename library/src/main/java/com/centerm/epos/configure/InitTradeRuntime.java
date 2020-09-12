package com.centerm.epos.configure;

import android.content.Context;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.printer.EnumGrayLevel;
import com.centerm.epos.EposApplication;
import com.centerm.epos.annotation.GroupConstant;
import com.centerm.epos.common.ITransCode;
import com.centerm.epos.common.Settings;
import com.centerm.epos.common.TransCode;
import com.centerm.epos.print.PrintManager;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.UseMobileNetwork;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.epos.xml.bean.RedevelopItem;
import com.centerm.epos.xml.keys.Keys;

import java.util.Map;
import java.util.Set;

/**
 * Created by yuhc on 2017/3/9.
 * 初始化业务运行时需要的环境
 */

public class InitTradeRuntime extends Thread {
    private static final String TAG = InitTradeRuntime.class.getSimpleName();
    Context context = EposApplication.getAppContext();

    @Override
    public void run() {
        ConfigureManager.getInstance(context).getFieldProcessClz();
        try {
            //是否只使用移动网络
            if (Settings.isMobileNetworkOnly(context))
                new UseMobileNetwork(context).start();
            projectRuntimeInit();
            initTradeInfo();
            //如果数据库版本号提升，触发数据库更新
            new PrintManager(context).isTemplateEmpty();
            initAnnotationConfig();
            TranscationFactorTable.putTradeFactor(ConfigureManager.getInstance(context).getTranscationFactorTable
                    (context));
            if(CommonUtils.isK9()) {
                DeviceFactory.getInstance().getPrinterDev().setPrintGray(EnumGrayLevel.LEVEL1);
            }
            XLogUtil.i(TAG, "^_^ 设置打印机灰度为1级 ^_^");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAnnotationConfig() {
        ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_MODEL_TAG);
        ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_VIEW_TAG);
        ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_CONTROLLE_TAG);
        ConfigureManager.getTradeComponentMapFromAnnotation(GroupConstant.TRADE_PRESENT_TAG);
    }

    /**
     * 初始化项目的交易信息，添加需要打印IC卡信息，管理类交易，冲正交易，贷记/贷记交易，需要保存交易记录的业务，交易显示或打印的名称
     * {@link ITransCode#registerTradeNameEn()}<br/>
     * author zhouzhihua 2018.01.11 新增打印英文交易名称的注册函数
     */
    private void initTradeInfo() {
        Object obj = ConfigureManager.getInstance(context).getSubPrjClassInstance(TransCode.class);
        if (obj == null || !(obj instanceof ITransCode))
            return;
        ITransCode prjTransCode = (ITransCode) obj;
        Set<String> tranSet = prjTransCode.registerPrintICInfoTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.PRINT_IC_INFO.addAll(tranSet);
        tranSet = prjTransCode.removePrintICInfoTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.PRINT_IC_INFO.removeAll(tranSet);

        tranSet = prjTransCode.registerManagerTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.NO_MAC_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeManagerTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.NO_MAC_SETS.removeAll(tranSet);

        tranSet = prjTransCode.registerReverseTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.CAUSE_REVERSE_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeReverseTrade();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.CAUSE_REVERSE_SETS.removeAll(tranSet);

        tranSet = prjTransCode.registerTradeForCredit();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.CREDIT_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeTradeForCredit();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.CREDIT_SETS.removeAll(tranSet);

        tranSet = prjTransCode.registerTradeForDebit();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.DEBIT_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeTradeForDebit();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.DEBIT_SETS.removeAll(tranSet);

        tranSet = prjTransCode.registerTradeForRecord();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.NEED_INSERT_TABLE_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeTradeForRecord();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.NEED_INSERT_TABLE_SETS.removeAll(tranSet);

        tranSet = prjTransCode.registerTradeDiscardAutoSign();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.NO_AUTOSIGN_TRADE_SETS.addAll(tranSet);

        tranSet = prjTransCode.registerTradeForFullPboc();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.FULL_PBOC_SETS.addAll(tranSet);
        tranSet = prjTransCode.removeTradeForFullPboc();
        if (tranSet != null && tranSet.size() > 0)
            TransCode.FULL_PBOC_SETS.removeAll(tranSet);

        Map<String, Integer> nameMap = prjTransCode.registerTradeName();
        if (nameMap != null && nameMap.size() > 0)
            TransCode.TRANS_NAME_MAP.putAll(nameMap);

        Map<String, Integer> nameMapEn = prjTransCode.registerTradeNameEn();
        if (nameMapEn != null && nameMapEn.size() > 0)
            TransCode.TRANS_ENGLISH_NAME_MAP.putAll(nameMapEn);
    }

    /**
     * 版本更新接口的调用
     */
    public void projectRuntimeInit() {
        ConfigureManager config = ConfigureManager.getInstance(context);
        RedevelopItem redevelop = config.getRedevelopItem(context, Keys.obj().runtime_init);
        if (null == redevelop)
            return;
        String clzName = redevelop.getClassName();
        if (!StringUtils.isStrNull(clzName)) {
            try {
                Class clz = Class.forName(clzName);
                Object obj = clz.newInstance();
                if (obj instanceof ProjectRuntimeInit) {
                    ((ProjectRuntimeInit) obj).doInit();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
