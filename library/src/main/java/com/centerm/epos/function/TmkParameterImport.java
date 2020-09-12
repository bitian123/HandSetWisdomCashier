package com.centerm.epos.function;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.IPinPadDev;
import com.centerm.cpay.midsdk.dev.define.ISerialPortDev;
import com.centerm.cpay.midsdk.dev.define.serialport.EnumRate;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.EncryptAlgorithmEnum;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.CommonUtils;
import com.centerm.epos.utils.SecurityTool;
import com.centerm.epos.utils.ViewUtils;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import config.BusinessConfig;


/**
 * Created by liubt on 2019/12/6
 * 4.1.	联接测试及复位（99）：02000A010130393038333239390309
 */

public class TmkParameterImport {
    private static final String TAG = TmkParameterImport.class.getSimpleName();

    private final int RECEIVE_TIMEOUT_S = 90;  //10秒
    private final int UART_EVEAL_TIMEOUT_MS = 1000;   //1s
    private final int PACKAGE_LEN_MIN = 5;
    private boolean isReceivedEndTag = false, isTerminalByUser = false, isImporting = false;
    ISerialPortDev serialPortDev = null;
    private Context context;
    public static final String TestCode = "3939";
    public static final String QueryCode = "4331";
    public static final String SignInCode = "3531";
    public static final String SaleCode = "3031";
    public String currentTrade = "";

    public TmkParameterImport(Context context) {
        this.context = context;
    }

    /**
     * 通过PC工具导入终端参数
     *
     * @return true 导入成功
     */
    public Object importParameter(String tradeCode) {
        String errorCode = "";
        boolean isImportSuccess = false;
        try {
            serialPortDev = DeviceFactory.getInstance().getSerialPort();
            serialPortDev.open(EnumRate.R9600);
            byte[] receiveBuff;
            int timeOutCount = 0;
            boolean isOver = false;
            sendMsg(tradeCode);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            do {
                /**
                 * 接收串口数据
                 */
                do {
                    receiveBuff = serialPortDev.receive(UART_EVEAL_TIMEOUT_MS);
                    if (receiveBuff != null){
                        break;
                    }
                    XLogUtil.d(TAG, "^_^ 未收到串口数据 ^_^");
                    if (isImporting)
                        isOver = !(++timeOutCount < RECEIVE_TIMEOUT_S);
                    else
                        isOver = isTerminalByUser;
                } while (!isOver);
                if (receiveBuff == null)
                    break;
                outputStream.write(receiveBuff);
                String response = HexUtil.bytesToHexString(receiveBuff);
                XLogUtil.d(TAG, "^_^ 接收数据1->" + response + " ^_^");
                XLogUtil.d(TAG, "^_^ 接收数据2->" + new String(receiveBuff,"GBK") + " ^_^");

                if(!TextUtils.isEmpty(response)&&response.length()>=30){
                    String code = response.substring(26,30);
                    if("3030".equals(code)){
                        String responseLrc = response.substring(response.length()-2);
                        String localLrc = SecurityTool.lrcCal(response.substring(2,response.length()-2));
                        if(TextUtils.equals(responseLrc,localLrc)){
                            if(TestCode.equals(currentTrade)){
                                sendMsg(tradeCode);
                            }else if(SignInCode.equals(currentTrade)){
                                //签到成功
                                errorCode = "O1";
                                break;
                            }else if(SaleCode.equals(currentTrade)){
                                //签到成功
                                errorCode = "O2";
                                break;
                            }else if(QueryCode.equals(currentTrade)){
                                //查询成功
                                errorCode = "O3";
                                break;
                            }else {
                                errorCode = "X8";
                                break;
                            }
                        }else {
                            //LRC校验错
                            errorCode = "L1";
                            break;
                        }
                    }else {
                        errorCode = new String(HexUtil.hexStringToByte(code));
                        break;
                    }
                }else {
                    break;
                }

                timeOutCount = 0;
                /**
                 * 校验、解析、处理数据
                 */
                if (!isImporting)
                    isImporting = true;

            } while (true);
            outputStream.close();
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ 参数导入异常:" + e.getMessage() + " ^_^");
        }
        if (serialPortDev != null)
            serialPortDev.close();
        if(!TextUtils.isEmpty(errorCode)){
            return errorCode;
        }
        return isImportSuccess;
    }

    /**
     * 取消参数导入
     *
     * @return true 取消成功
     */
    public boolean stopImport() {
        if (isImporting)
            return false;
        isTerminalByUser = true;
        return true;
    }

    /**
     * 是否中断了参数导入任务
     *
     * @return true 已取消
     */
    public boolean isTerminalByUser() {
        return isTerminalByUser;
    }

    /**
     * 4.1.	联接测试及复位（99）：02000A010130393038333239390309
     * 返回：0200160201303930383332061C30301CBDBBD2D7B3C9B9A61C036A
     * 5.2.5 签到指令（51）：02005E030130393330333535311c3839383433303137303131303336391c30313339373930371ccda8b3ccb5e7c6f71c1c1c0318
     * 返回：0200160401303030313630061C30301CBDBBD2D7B3C9B9A61C036B
     * 5.2.30.	房产查询指令
     * 02 0019 03 01 303030313932 4231 1C 1C 1C 1C 1C 31323334353637383930 03 7C
     *
     * */
    private void sendMsg(String code) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            String signInMsg = getSendMsg(code);
            outputStream.write(HexUtil.hexStringToByte(signInMsg));
            if (serialPortDev != null) {
                XLogUtil.d(TAG, "^_^ 发送签到数据 ^_^");
                serialPortDev.send(outputStream.toByteArray());
            }
        } catch (IOException e) {
            XLogUtil.e(TAG, "^_^ 发送数据失败：" + e.getMessage() + " ^_^");
        }
    }

    public String getSendMsg(String code){
        String sendMsg = "";
        if(TextUtils.isEmpty(code)){
            return sendMsg;
        }
        currentTrade = code;
        StringBuilder content = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        String len = "0000";
        switch (code){
        case TestCode:
            content.append("01")
                    .append("01")
                    .append(HexUtil.bytesToHexString("000000".getBytes()))//流水号
                    .append(code);
            len = SecurityTool.getStr16Len(content.toString()).toUpperCase();

            builder.append("02").append(len).append(content).append("03").append(SecurityTool.lrcCal(len+content+"03"));
            sendMsg = builder.toString();
            break;
        case SignInCode:
//            签到：02 000E 03 01 303030303231 35311C1C1C1C0308
            content.append("03")
                    .append("01")
                    .append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext()).getBytes()))//流水号
                    .append(code)
                    .append("1C")//.append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42).getBytes()))
                    .append("1C")//.append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41).getBytes()))
                    .append("1C")
                    .append("1C")
            ;
            len = SecurityTool.getStr16Len(content.toString()).toUpperCase();

            builder.append("02").append(len).append(content).append("03").append(SecurityTool.lrcCal(len+content+"03"));
            sendMsg = builder.toString();
            break;
        case SaleCode:
            content.append("03")
                    .append("01")
                    .append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext()).getBytes()))//流水号
                    .append(code)
                    .append("1C")//.append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 42).getBytes()))
                    .append("1C")//.append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getIsoField(EposApplication.getAppContext(), 41).getBytes()))
                    .append("1C")//.append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getValue(EposApplication.getAppContext(), BusinessConfig.Key.KEY_MCHNT_NAME).getBytes()))
                    .append("1C")
                    .append("1C").append("000000000002")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
            ;
            len = SecurityTool.getStr16Len(content.toString()).toUpperCase();

            builder.append("02").append(len).append(content).append("03").append(SecurityTool.lrcCal(len+content+"03"));
            sendMsg = builder.toString();
            break;
        case QueryCode:
//            STX     len   PATH     TYPE      ID                                                   ETX   LRC
//            02      0019   03      01     303030313932  4231 1C 1C 1C 1C 1C 31323334353637383930   03    7C
            content.append("03")
                    .append("01")
                    .append(HexUtil.bytesToHexString(BusinessConfig.getInstance().getPosSerial(EposApplication.getAppContext()).getBytes()))//流水号
                    .append(code)
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C")
                    .append("1C").append(HexUtil.bytesToHexString("1234567890".getBytes()))
                    .append("1C")
            ;
            len = SecurityTool.getStr16Len(content.toString()).toUpperCase();

            builder.append("02").append(len).append(content).append("03").append(SecurityTool.lrcCal(len+content+"03"));
            sendMsg = builder.toString();
            break;
        default:
            break;
        }
        return sendMsg;
    }

    /**
     * 计算校验全，所有数据异或
     *
     * @param data 待计算数据
     * @return 校验值
     */
    public byte calculateCheckSum(byte[] data) {
        if (data == null || data.length == 0)
            return 0;
        byte xorResult = 0;
        for (int i = 0; i < data.length; i++)
            xorResult ^= data[i];
        return xorResult;
    }


}
