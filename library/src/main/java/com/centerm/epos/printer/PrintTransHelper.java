package com.centerm.epos.printer;

import android.content.Context;
import android.text.TextUtils;

import com.centerm.cloudsys.sdk.common.utils.StringUtils;
import com.centerm.epos.bean.PrinterItem;
import com.centerm.epos.common.PrinterParamEnum;
import com.centerm.epos.db.CommonDao;
import com.centerm.epos.db.DbHelper;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.DataHelper;

import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.iso_f11;
import static com.centerm.epos.common.TransDataKey.iso_f12;
import static com.centerm.epos.common.TransDataKey.iso_f13;
import static com.centerm.epos.common.TransDataKey.iso_f2;
import static com.centerm.epos.common.TransDataKey.iso_f22;
import static com.centerm.epos.common.TransDataKey.iso_f37;
import static com.centerm.epos.common.TransDataKey.iso_f38;
import static com.centerm.epos.common.TransDataKey.iso_f4;
import static com.centerm.epos.common.TransDataKey.iso_f43;
import static com.centerm.epos.common.TransDataKey.iso_f44;
import static com.centerm.epos.common.TransDataKey.iso_f59;
import static com.centerm.epos.common.TransDataKey.iso_f60;

/**
 * Created by ysd on 2016/11/9.
 * 打印模块帮助类，用于解析及转换数据
 */

public class PrintTransHelper {
    private Map<String, String> mapData;
    private Context context;
    private Map<String, String> fieldMap;
    private List<PrinterItem> printerItems;
    private Logger logger = Logger.getLogger(PrintTransHelper.class);

    public PrintTransHelper(Map<String, String> mapData, Context context) {
        this.mapData = mapData;
        this.context = context;
        String feild59 = mapData.get(TradeInformationTag.SLIP_VERSION);
        if (null != feild59) {
            fieldMap = parseFieldToMap(feild59);
        } else {
            logger.debug("没有59域数据");
        }

        if (null != fieldMap) {
            CommonDao commonDao = new CommonDao(PrinterItem.class, DbHelper.getInstance());
            printerItems = commonDao.query();
            if (null != printerItems && printerItems.size() > 0) {
                for (PrinterItem item : printerItems) {
                    setPrintItemData(item);
                }
            } else {
                logger.error("本机签购单模板为空");
            }
            DbHelper.releaseInstance();
        } else {
            logger.debug("59域数据解析出的map为空的");
        }

    }

    /**
     * 返回准备打印的模板及数据
     *
     * @return
     */
    public List<PrinterItem> getPrinterItems() {
        return printerItems;
    }

    /**
     * 设置模板数据
     *
     * @param item
     */
    private void setPrintItemData(PrinterItem item) {
        String strId = item.getParamId().substring(2, item.getParamId().length());
        int tmpId = Integer.parseInt(strId);
        switch (tmpId) {
            case 1:
                //item.setParamValue(getField1());
                //抬头内容，打印签购单模板中的信息
                break;
            case 2:
                item.setParamValue(getField2());
                break;
            case 3:
                item.setParamValue(getField3());
                break;
            case 4:
                item.setParamValue(getField4());
                break;
            case 5:
                item.setParamValue(getField5());
                break;
            case 6:
                item.setParamValue(getField6());
                break;
            case 7:
                item.setParamValue(getField7());
                break;
            case 8:
                item.setParamValue(getField8());
                break;
            case 9:
                item.setParamValue(getField9());
                break;
            case 10:
                item.setParamValue(getField10());
                break;
            case 11:
                item.setParamValue(getField11());
                break;
            case 12:
                item.setParamValue(getField12());
                break;
            case 13:
                item.setParamValue(getField13());
                break;
            case 14:
                item.setParamValue(getField14());
                break;
            case 15:
                item.setParamValue(getField15());
                break;
            case 16:
                 item.setParamValue(getField16());
                break;
            case 17:
                 item.setParamValue(getField17());
                break;
            case 18:
                 item.setParamValue(getField18());
                break;
            case 19:
                 item.setParamValue(getField19());
                break;
            case 20:
                 item.setParamValue(getField20());
                break;
            case 51:
                //抬头从本机获取
                //item.setParamValue(getField51());
                break;
            case 52:
                item.setParamValue(getField52());
                break;
            case 53:
                item.setParamValue(getField53());
                break;
            case 54:
                item.setParamValue(getField54());
                break;
            case 55:
                item.setParamValue(getField55());
                break;
            case 56:
                item.setParamValue(getField56());
                break;
            case 57:
                item.setParamValue(getField57());
                break;
            case 58:
                item.setParamValue(getField58());
                break;
            case 59:
                item.setParamValue(getField59());
                break;
            case 60:
                item.setParamValue(getField60());
                break;
            case 61:
                item.setParamValue(getField61());
                break;
            case 62:
                item.setParamValue(getField62());
                break;
            case 63:
                item.setParamValue(getField63());
                break;
            case 64:
                item.setParamValue(getField64());
                break;
            case 65:
                item.setParamValue(getField65());
                break;
            case 66:
                item.setParamValue(getField66());
                break;
            case 67:
                 item.setParamValue(getField67());
                break;
            case 68:
                  item.setParamValue(getField68());
                break;
            case 69:
                 item.setParamValue(getField69());
                break;
            case 70:
                 item.setParamValue(getField70());
                break;
            default:
                break;
        }
    }




    /**
     * 获取商户名称
     * 59域解析。不压缩ANS。
     * 如果获取不到，从43域获取，域43也没有，从POS机获取。
     *
     * @return
     */
    private String getField2() {
        String fieldMap_f2 = fieldMap.get(PrinterParamEnum.SHOP_NAME.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f2)) {
            BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_MERCHANT_NAME,fieldMap_f2);
            return fieldMap_f2;
        } else {
            String mapData_f2 = mapData.get(TradeInformationTag.MERCHANT_NAME);
            if (!StringUtils.isStrNull(mapData_f2)) {
                BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_MERCHANT_NAME,mapData_f2);
                return mapData_f2;
            } else {
                //读取本地的商户名称
                String merchantName = BusinessConfig.getInstance().getIsoField(context, 43);
                if (null != merchantName && !"".equals(merchantName)) {
                    BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_MERCHANT_NAME,merchantName);
                    return merchantName;
                } else {
                    return "";
                }
            }
        }
    }

    /**
     * 获取商户号
     * 59域解析。不压缩ANS
     * 如果获取不到，从POS本机参数获取。
     *
     * @return
     */
    private String getField3() {
        String fieldMap_f3 = fieldMap.get(PrinterParamEnum.SHOP_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f3)) {
            BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_MERCHANT_CD,fieldMap_f3);
            return fieldMap_f3;
        } else {
            //读取本地的商户号
            String merchantCode = BusinessConfig.getInstance().getIsoField(context, 42);
            if (null != merchantCode && !"".equals(merchantCode)) {
                BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_MERCHANT_CD,merchantCode);
                return merchantCode;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取终端号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从POS本机参数获取。
     *
     * @return
     */
    private String getField4() {
        String fieldMap_f4 = fieldMap.get(PrinterParamEnum.SHOP_TERM_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f4)) {
            BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_TERMINAL_CD,fieldMap_f4);
            return fieldMap_f4;
        } else {
            //读取本地的终端号
            String terminalCode = BusinessConfig.getInstance().getIsoField(context, 41);
            if (null != terminalCode && !"".equals(terminalCode)) {
                BusinessConfig.getInstance().setValue(context,BusinessConfig.Key.SETTLEMENT_TERMINAL_CD,terminalCode);
                return terminalCode;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取发卡行
     * 59域解析。8个汉字以内或者11发卡行机构号。不压缩ANS。
     * 如果获取不到，从44域获取。
     *
     * @return
     */
    private String getField5() {
        String fieldMap_f5 = fieldMap.get(PrinterParamEnum.SHOP_SEND_CARD_BRANK.getParamId());
        if (fieldMap_f5!=null) {
            return fieldMap_f5;
        } else {
            String mapData_f5 = mapData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
            if (!TextUtils.isEmpty(mapData_f5)) {
                int len = mapData_f5.length() / 2;
                return mapData_f5.substring(0, len).trim();
            } else {
                logger.error("没有获取到发卡行数据");
                return "";
            }
        }
    }


    /**
     * 获取收单行
     * 59域解析。不压缩ANS。
     * 如果获取不到，从44域获取。
     *
     * @return
     */
    private String getField6() {
        String fieldMap_f6 = fieldMap.get(PrinterParamEnum.SHOP_RECEIVE_BRANK.getParamId());
        if (fieldMap_f6!=null) {
            return fieldMap_f6;
        } else {
            String mapData_f6 = mapData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
            if (!TextUtils.isEmpty(mapData_f6)) {
                int len = mapData_f6.length() / 2;
                return mapData_f6.substring(len, mapData_f6.length()).trim();
            } else {
                logger.error("没有获取到发卡行数据");
                return "";
            }
        }
    }

    /**
     * 获取卡号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从2域获取，域2也没有，从POS机获取。
     *
     * @return
     */
    private String getField7() {
        String fieldMap_f7 = fieldMap.get(PrinterParamEnum.SHOP_CARD_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f7)) {
            return fieldMap_f7;
        } else {
            String mapData_f7 = mapData.get(TradeInformationTag.BANK_CARD_NUM);
            if (!StringUtils.isStrNull(mapData_f7)) {
                return mapData_f7;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取批次号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从60域获取，域60也没有，从POS机获取。
     *
     * @return
     */
    private String getField8() {
        String fieldMap_f8 = fieldMap.get(PrinterParamEnum.SHOP_BATCH_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f8)) {
            return fieldMap_f8;
        } else {
            String mapData_f8 = mapData.get(TradeInformationTag.BATCH_NUMBER);
            if (!StringUtils.isStrNull(mapData_f8)) {
                return mapData_f8;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取交易流水号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从11域获取，域11也没有，从POS机获取。
     *
     * @return
     */
    private String getField9() {
        String fieldMap_f9 = fieldMap.get(PrinterParamEnum.SHOP_TRAN_FLOW_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f9)) {
            return fieldMap_f9;
        } else {
            String mapData_f9 = mapData.get(TradeInformationTag.TRACE_NUMBER);
            if (!StringUtils.isStrNull(mapData_f9)) {
                return mapData_f9;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取授权码
     * 59域解析。不压缩ANS。
     * 如果获取不到，从38域获取，域38也没有，从POS机获取。
     *
     * @return
     */
    private String getField10() {
        String fieldMap_f10 = fieldMap.get(PrinterParamEnum.SHOP_PERMISION_CODE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f10)) {
            return fieldMap_f10;
        } else {
            String mapData_f10 = mapData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
            if (!StringUtils.isStrNull(mapData_f10)) {
                return mapData_f10;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取参考号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从37域获取，域37也没有，从POS机获取。
     *
     * @return
     */
    private String getField11() {
        String fieldMap_f11 = fieldMap.get(PrinterParamEnum.SHOP_REFERENCE_CODE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f11)) {
            return fieldMap_f11;
        } else {
            String mapData_f11 = mapData.get(TradeInformationTag.REFERENCE_NUMBER);
            if (!StringUtils.isStrNull(mapData_f11)) {
                return mapData_f11;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取时间
     * 59域解析。格式YYYYMMDDHHMMSS。不压缩ANS。
     * 如果获取不到，从12和13域获取，域12或13也没有，从POS机获取。
     *
     * @return
     */
    public String getField12() {
        String fieldMap_f12 = fieldMap.get(PrinterParamEnum.SHOP_DATE_TIME.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f12)) {
            return fieldMap_f12;
        } else {
            Calendar c = Calendar.getInstance();
            String year = c.get(Calendar.YEAR) + "";
            String mapData_f12 = mapData.get(TradeInformationTag.TRANS_TIME);
            String mapData_ff12 = mapData.get(TradeInformationTag.TRANS_DATE);
            if (!StringUtils.isStrNull(mapData_f12) && !StringUtils.isStrNull(mapData_ff12)) {
                return year + mapData_ff12 + mapData_f12;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取金额
     * 59域解析。不压缩ANS。
     * 如果获取不到，从4域获取，域4也没有，从POS机获取。
     *
     * @return
     */
    private String getField13() {
        String fieldMap_f13 = fieldMap.get(PrinterParamEnum.SHOP_AMOUNT.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f13)) {
            return fieldMap_f13;
        } else {
            String mapData_f13 = mapData.get(TradeInformationTag.TRANS_MONEY);
            if (!StringUtils.isStrNull(mapData_f13)) {
                return mapData_f13;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取备注
     * 第一行固定居中打印“本人确认以上交易同意记入本卡账户”。
     * 后续行的内容为：
     * 59域解析。不压缩ANS。
     *
     * @return
     */
    private String getField14() {
        String fieldMap_f14 = fieldMap.get(PrinterParamEnum.SHOP_COMMENT.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f14)) {
            return fieldMap_f14;
        } else {
            logger.debug("没有获取到备注数据");
            return "";
        }
    }

    /**
     * 获取说明文字
     * 59域解析。不压缩ANS。
     *
     * @return
     */
    private String getField15() {
        String fieldMap_f15 = fieldMap.get(PrinterParamEnum.SHOP_DESCRIBE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f15)) {
            return fieldMap_f15;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField16(){
        String fieldMap_f16 = fieldMap.get(PrinterParamEnum.SHOP_NOT_USED1.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f16)) {
            return fieldMap_f16;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField17() {
        String fieldMap_f17 = fieldMap.get(PrinterParamEnum.SHOP_NOT_USED2.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f17)) {
            return fieldMap_f17;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }


    private String getField18() {
        String fieldMap_f18 = fieldMap.get(PrinterParamEnum.SHOP_NOT_USED3.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f18)) {
            return fieldMap_f18;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField19() {
        String fieldMap_f19 = fieldMap.get(PrinterParamEnum.SHOP_NOT_USED4.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f19)) {
            return fieldMap_f19;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField20() {
        String fieldMap_f20 = fieldMap.get(PrinterParamEnum.SHOP_NOT_USED5.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f20)) {
            return fieldMap_f20;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    /**
     * 获取商户名称
     * 59域解析。不压缩ANS。
     * 如果获取不到，从43域获取，域43也没有，从POS机获取。
     *
     * @return
     */
    private String getField52() {
        String fieldMap_f52 = fieldMap.get(PrinterParamEnum.PERSON_NAME.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f52)) {
            return fieldMap_f52;
        } else {
            String mapData_f52 = mapData.get(TradeInformationTag.MERCHANT_NAME);
            if (!StringUtils.isStrNull(mapData_f52)) {
                return mapData_f52;
            } else {
                //读取本地的商户名称
                String merchantName = BusinessConfig.getInstance().getIsoField(context, 43);
                if (null != merchantName && !"".equals(merchantName)) {
                    return merchantName;
                } else {
                    return "";
                }
            }
        }
    }

    /**
     * 获取商户号
     * 59域解析。不压缩ANS
     * 如果获取不到，从POS本机参数获取。
     *
     * @return
     */
    private String getField53() {
        String fieldMap_f53 = fieldMap.get(PrinterParamEnum.PERSON_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f53)) {
            return fieldMap_f53;
        } else {
            //读取本地的商户号
//            String merchantCode = Settings.getValue(context, Settings.KEY.SETTLEMENT_MERCHANT_CD, "");
            String merchantCode = BusinessConfig.getInstance().getIsoField(context, 42);

            if (null != merchantCode && !"".equals(merchantCode)) {
                return merchantCode;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取终端号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从POS本机参数获取。
     *
     * @return
     */
    private String getField54() {
        String fieldMap_f54 = fieldMap.get(PrinterParamEnum.PERSON_TERM_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f54)) {
            return fieldMap_f54;
        } else {
            //读取本地的终端号
//            String terminalCode = Settings.getValue(context, Settings.KEY.SETTLEMENT_TERMIANL_CD, "");
            String terminalCode = BusinessConfig.getInstance().getIsoField(context, 41);
            if (null != terminalCode && !"".equals(terminalCode)) {
                return terminalCode;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取发卡行
     * 59域解析。8个汉字以内或者11发卡行机构号。不压缩ANS。
     * 如果获取不到，从44域获取。
     *
     * @return
     */
    private String getField55() {
        String fieldMap_f55 = fieldMap.get(PrinterParamEnum.PERSON_SEND_CARD_BRANK.getParamId());
        if (fieldMap_f55!=null) {
            return fieldMap_f55;
        } else {
            String mapData_f55 = mapData.get(TradeInformationTag.ISSUER_IDENTIFICATION);
            if (!TextUtils.isEmpty(mapData_f55)) {
                int len = mapData_f55.length() / 2;
                return mapData_f55.substring(0, len).trim();
            } else {
                logger.error("没有获取到发卡行数据");
                return "";
            }
        }
    }

    /**
     * 获取收单行
     * 59域解析。不压缩ANS。
     * 如果获取不到，从44域获取。
     *
     * @return
     */
    private String getField56() {
        String fieldMap_f56 = fieldMap.get(PrinterParamEnum.PERSON_RECEIVE_BRANK.getParamId());
        if (fieldMap_f56!=null) {
            return fieldMap_f56;
        } else {
            String mapData_f56 = mapData.get(TradeInformationTag.ACQ_INSTITUTE);
            if (!TextUtils.isEmpty(mapData_f56)) {
                int len = mapData_f56.length() / 2;
                return mapData_f56.substring(len, mapData_f56.length()).trim();
            } else {
                logger.error("没有获取到发卡行数据");
                return "";
            }
        }
    }

    /**
     * 获取卡号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从2域获取，域2也没有，从POS机获取。
     *
     * @return
     */
    private String getField57() {
        String fieldMap_f57 = fieldMap.get(PrinterParamEnum.PERSON_CARD_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f57)) {
            return fieldMap_f57;
        } else {
            String mapData_f57 = mapData.get(TradeInformationTag.BANK_CARD_NUM);
            if (!StringUtils.isStrNull(mapData_f57)) {
                return mapData_f57;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取批次号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从60域获取，域60也没有，从POS机获取。
     *
     * @return
     */
    private String getField58() {
        String fieldMap_f58 = fieldMap.get(PrinterParamEnum.PERSON_BATCH_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f58)) {
            return fieldMap_f58;
        } else {
            String mapData_f58 = mapData.get(TradeInformationTag.BATCH_NUMBER);
            if (!StringUtils.isStrNull(mapData_f58)) {
                return mapData_f58;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取交易流水号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从11域获取，域11也没有，从POS机获取。
     *
     * @return
     */
    private String getField59() {
        String fieldMap_f59 = fieldMap.get(PrinterParamEnum.PERSON_TRAN_FLOW_NUM.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f59)) {
            return fieldMap_f59;
        } else {
            String mapData_f59 = mapData.get(TradeInformationTag.TRACE_NUMBER);
            if (!StringUtils.isStrNull(mapData_f59)) {
                return mapData_f59;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取授权码
     * 59域解析。不压缩ANS。
     * 如果获取不到，从38域获取，域38也没有，从POS机获取。
     *
     * @return
     */
    private String getField60() {
        String fieldMap_f60 = fieldMap.get(PrinterParamEnum.PERSON_PERMISION_CODE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f60)) {
            return fieldMap_f60;
        } else {
            String mapData_f60 = mapData.get(TradeInformationTag.AUTHORIZATION_IDENTIFICATION);
            if (!StringUtils.isStrNull(mapData_f60)) {
                return mapData_f60;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取参考号
     * 59域解析。不压缩ANS。
     * 如果获取不到，从37域获取，域37也没有，从POS机获取。
     *
     * @return
     */
    private String getField61() {
        String fieldMap_f61 = fieldMap.get(PrinterParamEnum.PERSON_REFERENCE_CODE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f61)) {
            return fieldMap_f61;
        } else {
            String mapData_f61 = mapData.get(TradeInformationTag.REFERENCE_NUMBER);
            if (!StringUtils.isStrNull(mapData_f61)) {
                return mapData_f61;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取时间
     * 59域解析。格式YYYYMMDDHHMMSS。不压缩ANS。
     * 如果获取不到，从12和13域获取，域12或13也没有，从POS机获取。
     *
     * @return
     */
    private String getField62() {
        String fieldMap_f62 = fieldMap.get(PrinterParamEnum.PERSON_DATE_TIME.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f62)) {
            return fieldMap_f62;
        } else {
            Calendar c = Calendar.getInstance();
            String year = c.get(Calendar.YEAR) + "";
            String mapData_f62 = mapData.get(TradeInformationTag.TRANS_TIME);
            String mapData_ff62 = mapData.get(TradeInformationTag.TRANS_DATE);
            if (!StringUtils.isStrNull(mapData_f62) && !StringUtils.isStrNull(mapData_ff62)) {
                return year + mapData_ff62 + mapData_f62;
            } else {
                return "";
            }
        }
    }

    /**
     * 获取金额
     * 59域解析。不压缩ANS。
     * 如果获取不到，从4域获取，域4也没有，从POS机获取。
     *
     * @return
     */
    private String getField63() {
        String fieldMap_f63 = fieldMap.get(PrinterParamEnum.PERSON_AMOUNT.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f63)) {
            return fieldMap_f63;
        } else {
            String mapData_f63 = mapData.get(TradeInformationTag.TRANS_MONEY);
            if (!StringUtils.isStrNull(mapData_f63)) {
                return mapData_f63;
            } else {
                //TODO：如果获取不到，从POS本机参数获取。
                return "";
            }
        }
    }

    /**
     * 获取备注
     * 第一行固定居中打印“本人确认以上交易同意记入本卡账户”。
     * 后续行的内容为：
     * 59域解析。不压缩ANS。
     *
     * @return
     */
    private String getField64() {
        String fieldMap_f64 = fieldMap.get(PrinterParamEnum.PERSON_COMMENT.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f64)) {
            return fieldMap_f64;
        } else {
            logger.debug("没有获取到备注数据");
            return "";
        }
    }

    /**
     * 获取说明文字
     * 59域解析。不压缩ANS。
     *
     * @return
     */
    private String getField65() {
        String fieldMap_f65 = fieldMap.get(PrinterParamEnum.PERSON_DESCRIBE.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f65)) {
            return fieldMap_f65;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }


    private String getField66() {
        String fieldMap_f66 = fieldMap.get(PrinterParamEnum.PERSON_NOT_USED1.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f66)) {
            return fieldMap_f66;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField67() {
        String fieldMap_f67 = fieldMap.get(PrinterParamEnum.PERSON_NOT_USED2.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f67)) {
            return fieldMap_f67;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField68() {
        String fieldMap_f68 = fieldMap.get(PrinterParamEnum.PERSON_NOT_USED3.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f68)) {
            return fieldMap_f68;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField69() {
        String fieldMap_f69 = fieldMap.get(PrinterParamEnum.PERSON_NOT_USED4.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f69)) {
            return fieldMap_f69;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }

    private String getField70() {
        String fieldMap_f70 = fieldMap.get(PrinterParamEnum.PERSON_NOT_USED5.getParamId());
        if (!StringUtils.isStrNull(fieldMap_f70)) {
            return fieldMap_f70;
        } else {
            logger.error("没有获取到说明文字数据");
            return "";
        }
    }
    /**
     * 解析59域的键值对
     *
     * @param slipVersion
     */
    private Map<String, String> parseFieldToMap(String slipVersion) {
        Map<String, String> stringMap = DataHelper.hexAscTlv2Map(slipVersion,2,1);
        return stringMap;
    }


    public String getTranCardType() {
        if (null != mapData) {
            String serverCode = mapData.get(TradeInformationTag.SERVICE_ENTRY_MODE);
            if (null != serverCode && serverCode.length() > 0) {
                String s = serverCode.substring(0, 2);
                if (s.equals("02")) {
                    return "S";
                } else if (s.equals("05")) {
                    return "I";
                } else if (s.equals("07")) {
                    return "C";
                }else if ("01".equals(s)){
                    return "M";
                }

            }
        }
        return "未知";
    }


  /*  private String bankName(String f44) {
        if (null == f44) {
            return "未知";
        }
        String result = null;
        String msg = f44.substring(1, 4);
        switch (msg) {
            case "102":
                result = "工商银行";
                break;
            case "103":
                result = "农业银行";
                break;
            case "104":
                result = "中国银行";
                break;
            case "105":
                result = "建设银行";
                break;
            case "100":
                result = "邮储银行";
                break;
            case "301":
                result = "交通银行";
                break;
            case "302":
                result = "中信银行";
                break;
            case "303":
                result = "光大银行";
                break;
            case "304":
                result = "华夏银行";
                break;
            case "305":
                result = "民生银行";
                break;
            case "306":
                result = "广发银行";
                break;
            case "307":
                result = "深发银行";
                break;
            case "308":
                result = "招商银行";
                break;
            case "309":
                result = "兴业银行";
                break;
            case "310":
                result = "浦发银行";
                break;
            case "403":
                result = "平安银行";
                break;
            case "311":
                result = "北京银行";
                break;
            case "401":
                result = "上海银行";
                break;
            default:
                result = f44;
                break;
        }
        return result;
    }*/
}
