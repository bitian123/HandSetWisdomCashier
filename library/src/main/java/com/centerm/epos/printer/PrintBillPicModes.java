package com.centerm.epos.printer;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.centerm.dev.printer.PrinterDataAdvBuilder;
import com.centerm.dev.printer.PrinterDataParams;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.transcation.pos.constant.BankNameMap;
import com.centerm.epos.transcation.pos.constant.JsonKeyGT;
import com.centerm.epos.transcation.pos.constant.PrintType;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;
import com.centerm.epos.utils.XLogUtil;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;
import config.Config;

public class PrintBillPicModes {



    private static PrintBillPicModes instance;

    public static PrintBillPicModes getInstance() {
        if (instance == null) {
            synchronized (BusinessConfig.class) {
                if (instance == null) {
                    instance = new PrintBillPicModes();
                }
            }
        }
        return instance;
    }

    protected Logger logger = Logger.getLogger(this.getClass());


    /**
     * 创建电子签购单图片
     *
     * @param signBitmap 图片路径
     * @param tradeInfo  交易记录信息
     * @param type       打印类型，根据打印类型定制打印不同数据
     * @return
     */
    public boolean createCommonPic(Bitmap signBitmap, TradeInfoRecord tradeInfo, String type, Map<String ,Object> map) {
        PrinterDataAdvBuilder advBuilder = new PrinterDataAdvBuilder();
        try {
            advBuilder.setMaxWidth(480);
            PrinterDataParams params = new PrinterDataParams();
            params.setMarginLeft(30);
            advBuilder.addText("           交易签购单", params);
            advBuilder.addText("商户存根                   请妥善保管", params);
            advBuilder.addText("-----------------------------------", params);
            advBuilder.addText("商户名称", params);
            advBuilder.addText(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME), params);
            advBuilder.addText("商户编号:" + BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42), params);
            advBuilder.addText("终端编号:" + BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41), params);
            advBuilder.addText("操作员号:" + BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_OPER_ID), params);
            advBuilder.addText("发 卡 行:" + getBankName(tradeInfo.getIssueInstituteID()), params);
            advBuilder.addText("收 单 行:" + tradeInfo.getAcquireInstituteID(), params);
            advBuilder.addText("卡     号:\n" + DataHelper.formatCardno(tradeInfo.getCardNo()), params);
            advBuilder.addText("有效日期:" + tradeInfo.getCardExpiredDate(), params);
            advBuilder.addText("日期/时间:" + getDate(tradeInfo), params);
            advBuilder.addText("参 考 号:" + tradeInfo.getReferenceNo(), params);
            advBuilder.addText("批 次 号:" + tradeInfo.getBatchNo(), params);
            advBuilder.addText("凭 证 号:" + tradeInfo.getVoucherNo(), params);
            advBuilder.addText("外部订单号:" + tradeInfo.getIntoAccount(), params);
            advBuilder.addText("交易类型:\n消费", params);
            advBuilder.addText("金    额: \nRMB " + tradeInfo.getAmount(), params);
            addBusinessType(type,advBuilder,map,params);
            advBuilder.addText("-----------------------------------", params);
            advBuilder.addText("备注:\n", params);
            if (!TextUtils.isEmpty(tradeInfo.getUnicom_scna_type())) {
                try {
                    JSONObject object = new JSONObject(tradeInfo.getUnicom_scna_type());
                    advBuilder.addText("项目名称:" + object.optString("projectName"), params);
                    advBuilder.addText("姓名:" + object.optString("name"), params);
                    advBuilder.addText("证件号:" + object.optString("idNo"), params);
                    advBuilder.addText("\n", params);

                    if (object.optJSONArray("array") != null) {
                        JSONArray array = object.optJSONArray("array");
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject item = array.getJSONObject(i);
                            String[] strs = DataHelper.segmentation(item.optString("billId"), Config.ROW_MAX_LENTH);
                            if (strs != null) {
                                for (int j = 0; j < strs.length; j++) {
                                    advBuilder.addText(strs[j], params);
                                }
                            } else {
                                advBuilder.addText(item.optString("billId"), params);
                            }
                            advBuilder.addText(item.optString("amt"), params);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            advBuilder.addText("\n", params);
            advBuilder.addText("持卡人签名:\n", params);
            advBuilder.addBitmap(signBitmap);
            advBuilder.addText("本人确认以上交易，同意将其计入", params);
            advBuilder.addText("本人账户", params);
            advBuilder.addText("I ACKNOWLEDGE SATISFACTORY", params);
            advBuilder.addText("RECEIPT OF RELATIVE", params);
            advBuilder.addText("GOODS/SERVICES CENTERM K9", params);
            Bitmap bmp = advBuilder.build();
            return saveFile(bmp, tradeInfo.getIntoAccount() + ".png");
        } catch (Exception e) {
            logger.error(e.toString());
        }
        return false;
    }


    private  void  addBusinessType(String type, PrinterDataAdvBuilder advBuilder,Map<String ,Object> map,PrinterDataParams params){
        XLogUtil.d("业务类型：", type);
        String settlementInfo= "业务类型："+ (String)map.get(TradeInformationTag.SETTLEMENT_INFO) ;
        List<String> strs = DataHelper.getStrList(settlementInfo, 18);

        if(PrintType.TYPE_2.equals(type)){
            for(int i =0 ;i < strs.size();i++){
                advBuilder.addText(strs.get(i), params);
            }

        }
    }


    protected String getBankName(String tempString) {
        if (!TextUtils.isEmpty(tempString) && tempString.length() > 3) {
            String bankName;
            bankName = BankNameMap.getBankName(tempString.substring(0, 4));
            if (!TextUtils.isEmpty(bankName)) {
                tempString = bankName;
            }
        }
        return tempString;
    }


    private String getDate(TradeInfoRecord tradeInfo) {
        if (!TextUtils.isEmpty(tradeInfo.getTransYear())
                && !TextUtils.isEmpty(tradeInfo.getTransDate())
                && !TextUtils.isEmpty(tradeInfo.getTransTime())) {
            String yearStr = tradeInfo.getTransYear();
            String dateStr = tradeInfo.getTransDate();
            String timeStr = tradeInfo.getTransTime();
            String dateTimeStr = String.format(Locale.CHINA, "%s/%s/%s %s:%s:%s", yearStr, dateStr.substring(0, 2), dateStr
                    .substring(2, 4), timeStr.substring(0, 2), timeStr.substring(2, 4), timeStr.substring(4, 6));
            return dateTimeStr;
        }
        return "";
    }


    private boolean saveFile(Bitmap bitmap, String fileName) {
        File file = new File(Config.Path.VOUCHER_PATH, fileName);//将要保存图片的路径
        try {
            File dic = new File(Config.Path.VOUCHER_PATH);
            if (!dic.exists()) {
                dic.mkdirs();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
