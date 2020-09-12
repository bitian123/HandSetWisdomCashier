package com.centerm.epos.transcation.pos.data;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;
import com.centerm.epos.EposApplication;
import com.centerm.epos.bean.transcation.InstallmentInformation;
import com.centerm.epos.bean.transcation.OriginalMessage;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.transcation.pos.constant.TradeInformationTag;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import config.BusinessConfig;

import static com.centerm.epos.common.TransCode.DOWNLOAD_AID;
import static com.centerm.epos.common.TransCode.DOWNLOAD_BLACK_CARD_BIN_QPS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CAPK;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CARD_BIN;
import static com.centerm.epos.common.TransCode.DOWNLOAD_CARD_BIN_QPS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_PARAMS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_QPS_PARAMS;
import static com.centerm.epos.common.TransCode.DOWNLOAD_TERMINAL_PARAMETER;
import static com.centerm.epos.common.TransCode.EC_LOAD_OUTER;
import static com.centerm.epos.common.TransCode.ESIGN_UPLOAD;
import static com.centerm.epos.common.TransCode.ESIGN_UPLOAD_PART;
import static com.centerm.epos.common.TransCode.E_REFUND;
import static com.centerm.epos.common.TransCode.ISS_INTEGRAL_SALE;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_LOAD;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_LOAD_VERIFY;
import static com.centerm.epos.common.TransCode.MAG_ACCOUNT_VERIFY;
import static com.centerm.epos.common.TransCode.MOTO_AUTH;
import static com.centerm.epos.common.TransCode.MOTO_AUTH_COMPLETE;
import static com.centerm.epos.common.TransCode.MOTO_CARDHOLDER_VERIFY;
import static com.centerm.epos.common.TransCode.MOTO_SALE;
import static com.centerm.epos.common.TransCode.OBTAIN_TMK;
import static com.centerm.epos.common.TransCode.POS_STATUS_UPLOAD;
import static com.centerm.epos.common.TransCode.RESERVATION_SALE;
import static com.centerm.epos.common.TransCode.SALE_INSTALLMENT;
import static com.centerm.epos.common.TransCode.SETTLEMENT_INFO_QUERY;
import static com.centerm.epos.common.TransCode.SIGN_IN;
import static com.centerm.epos.common.TransCode.TRANS_IC_DETAIL;
import static com.centerm.epos.common.TransCode.UNION_INTEGRAL_SALE;
import static com.centerm.epos.common.TransDataKey.KEY_PARAMS_COUNTS;
import static com.centerm.epos.common.TransDataKey.KEY_PARAMS_TYPE;

/**
 * Created by yuhc on 2017/2/8.<br>
 * 域62 自定义域(Reserved Private)，最大512个字节的数据域。<br>
 * 用法：终端密钥(POS Security Key);终端状态信息(Terminal Status Information);终端参数信息(Configation Table Message); <br>
 * PBOC借/贷记IC卡终端专用参数信息(PBOC IC Configation Table Message);基于PBOC电子钱包标准的圈存确认明细;<br>
 * 终端设备信息。
 */

public class BaseField62 implements I8583Field {
    public static String SecurityKey = "";

    /**
     * 日志头信息：类名
     */
    private static final String TAG = BaseField62.class.getSimpleName();

    /**
     * 业务数据
     */
    protected Map<String, Object> tradeInfo;

    public BaseField62(Map<String, Object> tradeInfo) {
        this.tradeInfo = tradeInfo;
    }

    public BaseField62() {
    }

    /**
     * 从业务数据中取出数据，并根据规范要求输出。
     *
     * @return 相关数据
     */
    private String packageField62BySM4(String transCode) {
        String s = null, string = null;

        switch (transCode) {
            case SALE_INSTALLMENT:
                string = getInstallmentInfo();
                if (null != string) {
                    s = "09" + String.format("%02X", string.length()) + getBinString(string);
                }
                break;

            case MAG_ACCOUNT_LOAD:
            case EC_LOAD_OUTER:
                string = (String) tradeInfo.get(TradeInformationTag.TRANSFER_INTO_CARD);
                if (null != string) {
                    s = "10" + String.format("%02X", string.length()) + getBinString(string);
                }

            case ISS_INTEGRAL_SALE:
            case UNION_INTEGRAL_SALE:
                string = (String) tradeInfo.get(TradeInformationTag.ISO62_REQ);
                if (null != string) {
                    s = "13" + String.format("%02X", string.length()) + getBinString(string);
                }
                break;

            case RESERVATION_SALE:
            case MOTO_AUTH_COMPLETE:
                string = (String) tradeInfo.get(TradeInformationTag.ISO62_REQ);
                if (null != string) {
                    s = "12" + String.format("%02X", string.length()) + getBinString(string);
                }
                break;
            default:
        }
        return s;
    }

    /**
     * @param string 字符串数据
     * @return 转换为BIN格式的数据，在组8583包时进行数据转换
     */
    private String getBinString(String string) {
        String fieldBuff = "";
        if (string != null) {
            try {
                fieldBuff = HexUtil.bytesToHexString(string.getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                XLogUtil.e(TAG, "^_^ getBinString 数据转换失败" + " ^_^");
            }
        }
        return fieldBuff;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String encode() {
        if (tradeInfo == null) {
            XLogUtil.e(TAG, "^_^ encode 输入参数 tradeInfo 为空 ^_^");
            return null;
        }
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType)) {
            XLogUtil.e(TAG, "^_^ 获取交易类型数据失败 ^_^");
            return null;
        }

        String fieldBuff = null;
        if (EncryptAlgorithmEnum.SM4 == Settings.getEncryptAlgorithmEnum(EposApplication.getAppContext())) {
            String pwdObj = (String) tradeInfo.get(TradeInformationTag.SM4_PASSWORD);
            if (!TextUtils.isEmpty(pwdObj)) {
                fieldBuff = format22Usage();
                String otherS = packageField62BySM4(transType);/*国密改造62域打包数据*/
                if (null != otherS) {
                    fieldBuff += otherS;
                }
                XLogUtil.e(TAG, "^_^ encode result:" + fieldBuff + " ^_^");
                return fieldBuff;
            }
        }
        switch (transType) {
            case SIGN_IN:
            case OBTAIN_TMK:
                fieldBuff = formatSignInData();
                break;
            case POS_STATUS_UPLOAD:
                fieldBuff = formatTerminalInfo();
                break;
            case DOWNLOAD_CARD_BIN:
            case DOWNLOAD_CARD_BIN_QPS:
            case DOWNLOAD_BLACK_CARD_BIN_QPS:
                fieldBuff = (String) tradeInfo.get(TradeInformationTag.IC_PARAMETER_CAPD_BIN);
                if (TextUtils.isEmpty(fieldBuff)) {
                    fieldBuff = BusinessConfig.getInstance().getValue(EposApplication.getAppContext(),
                            BusinessConfig.Key.KEY_LAST_BIN_NO);
                } else if (fieldBuff.length() < 3) {
                    fieldBuff = String.format(Locale.CHINA, "%03d", Integer.parseInt(fieldBuff, 10));
                }
                break;
            case DOWNLOAD_PARAMS:
                String type = (String) tradeInfo.get(TradeInformationTag.PARAMS_TYPE);
                switch (type) {
                    case DOWNLOAD_AID:
                        fieldBuff = (String) tradeInfo.get(TradeInformationTag.IC_PARAMETER_AID);
                        break;
                    case DOWNLOAD_CAPK:
                        fieldBuff = (String) tradeInfo.get(TradeInformationTag.IC_PARAMETER_CAPK);
                        break;
                    case DOWNLOAD_QPS_PARAMS:
                        fieldBuff = (String) tradeInfo.get(TradeInformationTag.IC_PARAMETER_QPS);
                        break;
                    default:
                }
                break;
            case TRANS_IC_DETAIL:
                fieldBuff = (String) tradeInfo.get(TradeInformationTag.ORIGINAL_MESSAGE);
                break;
            case ESIGN_UPLOAD:
            case ESIGN_UPLOAD_PART:
                fieldBuff = (String) tradeInfo.get(TradeInformationTag.E_SIGNATURE_DATA);
                //数据量大，及时释放内存 33363332323033313332323033333334323033353336323033373338323033393330323033313332323033333334
                tradeInfo.remove(TradeInformationTag.E_SIGNATURE_DATA);
                break;

            case SALE_INSTALLMENT:
                //用法九：分期付款请求信息，最大62
                fieldBuff = getBinString(getInstallmentInfo());
                break;

            case EC_LOAD_OUTER:/*非指定账户圈存 用法十，转入卡 卡号*/
            case MAG_ACCOUNT_LOAD:
                fieldBuff = getBinString((String) tradeInfo.get(TradeInformationTag.TRANSFER_INTO_CARD));
                break;

            case E_REFUND:/*用法十八 原终端号*/
                OriginalMessage originalMessage = (OriginalMessage) tradeInfo.get(TradeInformationTag.ORIGINAL_MESSAGE);
                fieldBuff = originalMessage.getTermNo();
                break;

            case ISS_INTEGRAL_SALE://用法十三
            case UNION_INTEGRAL_SALE://用法十三
                fieldBuff = getBinString((String) tradeInfo.get(TradeInformationTag.ISO62_REQ));
                break;

            case MAG_ACCOUNT_VERIFY:
            case MAG_ACCOUNT_LOAD_VERIFY:
            case MOTO_CARDHOLDER_VERIFY: //用法十二
                fieldBuff = (String) tradeInfo.get(TradeInformationTag.ISO62_REQ);
                break;

            case MOTO_SALE:
            case MOTO_AUTH:
                fieldBuff = (String) tradeInfo.get(TradeInformationTag.ISO62_REQ);
                break;

            case RESERVATION_SALE:
            case MOTO_AUTH_COMPLETE:
                fieldBuff = getBinString((String) tradeInfo.get(TradeInformationTag.ISO62_REQ));
                break;
            default:
        }
        XLogUtil.d(TAG, "^_^ encode result:" + fieldBuff + " ^_^");
        return fieldBuff;
    }

    /**
     * 组织分期付款数据
     *
     * @return 分期信息
     */
    @SuppressLint("DefaultLocale")
    @Nullable
    private String getInstallmentInfo() {
        StringBuilder stringBuffer = new StringBuilder(70);
        String period = (String) tradeInfo.get(TradeInformationTag.INSTALLMENT_PERIOD);
        if (TextUtils.isEmpty(period)) {
            XLogUtil.i(TAG, "^_^ 获取分期期数失败 ^_^");
            return null;
        }
        //添加分期期数
        stringBuffer.append(String.format("%02d", Integer.parseInt(period)));

        String code = (String) tradeInfo.get(TradeInformationTag.INSTALLMENT_CODE);
        //modify by yuhc 编码允许为空（徽商银行生厂环境测试分期业务时，不用输入编号）
//        if (TextUtils.isEmpty(code)) {
//            XLogUtil.i(TAG, "^_^ 获取分期商品编码失败 ^_^");
//            return null;
//        }
        //添加分期商品的编码
        byte[] filledBuffer = new byte[30];
        Arrays.fill(filledBuffer, (byte) ' ');
        if (!TextUtils.isEmpty(code)) {
            System.arraycopy(code.getBytes(), 0, filledBuffer, 0, code.getBytes().length);
        }
        stringBuffer.append(new String(filledBuffer));
        //添加分期手续费支付方式
        int mode = (int) tradeInfo.get(TradeInformationTag.INSTALLMENT_PAY_MODE);
        Arrays.fill(filledBuffer, (byte) ' ');
        filledBuffer[0] = '1';
        filledBuffer[1] = (byte) ('0' + mode);
        stringBuffer.append(new String(filledBuffer));
        return stringBuffer.toString();
    }

    /**
     * 格式化为第二十二用法，目前只添加标签为00的国密密码
     *
     * @return 域数据
     */
    private String format22Usage() {
        String pwdObj = (String) tradeInfo.get(TradeInformationTag.SM4_PASSWORD);
        if (TextUtils.isEmpty(pwdObj))
            return null;
        return "0010" + pwdObj;
    }

    /**
     * 组织终端状态信息
     *
     * @return 终端状态
     */
    private String formatTerminalInfo() {
        String paramsType = (String) tradeInfo.get(KEY_PARAMS_TYPE);
        if ("8".equals(paramsType)) {
            return getTerminalState();
        }
        String counts = (String) tradeInfo.get(KEY_PARAMS_COUNTS);
        if (TextUtils.isEmpty(counts)) {
            counts = "00";
        } else if (counts.length() == 1) {
            counts = "0" + counts;
        }
        return "1" + counts;
    }


    /**
     * 获取终端状态信息
     */
    private String getTerminalState() {
        StringBuilder stringBuilder = new StringBuilder();
        //硬件状态
        stringBuilder.append("01").append("1");
        stringBuilder.append("02").append("1");
        stringBuilder.append("03").append("1");
        stringBuilder.append("04").append("1");
        stringBuilder.append("05").append("1");

        // TODO: 2017/5/17 实际参数
        //下载的参数
        stringBuilder.append("11").append("60");
        stringBuilder.append("12").append("60");
        stringBuilder.append("13").append("2");
        stringBuilder.append("14").append("12345678901   ");
        stringBuilder.append("15").append("12345678901   ");
        stringBuilder.append("16").append("12345678901   ");
        stringBuilder.append("17").append("12345678901   ");
        stringBuilder.append("18").append("0");
        stringBuilder.append("19").append("00");
        stringBuilder.append("20").append("1");
        stringBuilder.append("21").append("1");
        stringBuilder.append("23").append("2");
        stringBuilder.append("25").append("6");
        stringBuilder.append("27").append("10");
        //通讯统计
        stringBuilder.append("51").append("000000000000");
        return stringBuilder.toString();
    }

    /**
     * 组织和处理签到交易数据
     *
     * @return 签到信息
     */
    private String formatSignInData() {
        return getIso62();
    }

    /**
     * @param fieldMsg 域数据
     * @return null
     */
    @SuppressLint("DefaultLocale")
    @Override
    public Map<String, Object> decode(String fieldMsg) {
        String transType = (String) tradeInfo.get(TradeInformationTag.TRANSACTION_TYPE);
        if (TextUtils.isEmpty(transType))
            return null;
        Map<String, Object> tradeData = new HashMap<>();
        switch (transType) {
            case SIGN_IN:
                tradeData.put(TradeInformationTag.WORK_KEY, fieldMsg);
                break;
            case OBTAIN_TMK:
                tradeData.put(TradeInformationTag.SECURITY_KEY, fieldMsg);
                SecurityKey = fieldMsg;
                break;
            case POS_STATUS_UPLOAD:
                String paramsType = (String) tradeInfo.get(KEY_PARAMS_TYPE);
                if ("8".equals(paramsType)) {
                    tradeData.put(TradeInformationTag.TERMINAL_PARAMETER, fieldMsg);
                } else
                    tradeData.put(TradeInformationTag.IC_PARAMETER_INDEX, fieldMsg);
                break;
            case DOWNLOAD_PARAMS:
                String type = (String) tradeInfo.get(TradeInformationTag.PARAMS_TYPE);
                switch (type) {
                    case DOWNLOAD_QPS_PARAMS:
                        tradeData.put(TradeInformationTag.IC_PARAMETER_QPS, new String(HexUtils.hexStringToByte
                                (fieldMsg)));
                        break;
                    case DOWNLOAD_AID:
                        tradeData.put(TradeInformationTag.IC_PARAMETER_AID, fieldMsg);
                        break;
                    case DOWNLOAD_CAPK:
                        tradeData.put(TradeInformationTag.IC_PARAMETER_CAPK, fieldMsg);
                        break;
                    case DOWNLOAD_TERMINAL_PARAMETER:
                        tradeData.put(TradeInformationTag.TERMINAL_PARAMETER, fieldMsg);
                        break;
                    default:
                }
                break;
            case SETTLEMENT_INFO_QUERY: //结算账户查询
                tradeData.put(TradeInformationTag.SETTLEMENT_INFO, fieldMsg);
                break;
            case DOWNLOAD_CARD_BIN:
            case DOWNLOAD_CARD_BIN_QPS:
            case DOWNLOAD_BLACK_CARD_BIN_QPS:
                tradeData.put(TradeInformationTag.IC_PARAMETER_CAPD_BIN, fieldMsg);
                break;
            case SALE_INSTALLMENT:
                InstallmentInformation installmentInformation = new InstallmentInformation();
                String period = (String) tradeInfo.get(TradeInformationTag.INSTALLMENT_PERIOD);
                if (!TextUtils.isEmpty(period)) {
                    installmentInformation.setPeriod(String.format("%d", Integer.parseInt(period)));
                }
                installmentInformation.setVoucherNo((String) tradeInfo.get(TradeInformationTag.TRACE_NUMBER));
                try {
                    int offset = 0;
                    //首期还款金额
                    installmentInformation.setPayAmountFirst(fieldMsg.substring(offset, offset + 12));
                    offset += 12;
                    //还款币种
                    installmentInformation.setCurrencyCode(fieldMsg.substring(offset, offset + 3));
                    offset += 3;
                    //持卡人分期付款手续费
                    installmentInformation.setFeeTotal(fieldMsg.substring(offset, offset + 12));
                    offset += 12;
                    //分期付款奖励积分
                    installmentInformation.setPoint(fieldMsg.substring(offset, offset + 12));
                    offset += 12;
                    //手续费支付方式
                    installmentInformation.setPayMode(fieldMsg.substring(offset, offset + 1));
                    offset += 1;
                    //首期手续费
                    installmentInformation.setFeeFirst(fieldMsg.substring(offset, offset + 12));
                    offset += 12;
                    //每期手续费
                    installmentInformation.setFeeEach(fieldMsg.substring(offset, offset + 12));
                    offset += 12;
                    //保留使用
                    installmentInformation.setReserveMessage(fieldMsg.substring(offset, offset + 13));
                } catch (StringIndexOutOfBoundsException exception) {
                    //未返回规范定义的所有数据，有什么数据就存什么，不影响交易结果
                }
                tradeData.put(TradeInformationTag.INSTALLMENT_INFORMATION, installmentInformation);
                break;

            case EC_LOAD_OUTER:/*非指定账户圈存*/
                tradeData.put(TradeInformationTag.TRANSFER_INTO_CARD, fieldMsg);
                break;

            case ISS_INTEGRAL_SALE: //用法十四
            case MAG_ACCOUNT_VERIFY: //磁条卡充值 可充值余额
            case MAG_ACCOUNT_LOAD_VERIFY://磁条卡充值 可充值余额
                tradeData.put(TradeInformationTag.ISO62_RES, fieldMsg);
                break;
            default:

        }
        XLogUtil.d(TAG, "^_^ decode result:" + tradeData + " ^_^");
        return tradeData;
    }

    private String getIso62() {
        String tag = "Sequence No";
        String sn = CommonUtils.getSn();
        String value = getTerminalNetworkCode() + sn;
        int len = value.length();
        return tag + len + value;
    }

    /**
     * 获取终端入网认证编号
     *
     * @return 4个字符的编号
     */
    private String getTerminalNetworkCode() {
        switch (android.os.Build.MODEL) {
            case "K9":
                return "9658";
            case "V8":
                return "9519";
            case "C960F":
                return "9483";
        }
        return "0000";
    }

}
