package com.centerm.epos.function;

import android.content.Context;

import com.centerm.cpay.midsdk.dev.DeviceFactory;
import com.centerm.cpay.midsdk.dev.define.ISerialPortDev;
import com.centerm.cpay.midsdk.dev.define.serialport.EnumRate;
import com.centerm.epos.EposApplication;
import com.centerm.epos.common.Settings;
import com.centerm.epos.utils.XLogUtil;
import com.centerm.smartpos.util.HexUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import config.BusinessConfig;

/**
 * Created by yuhc on 2017/8/18.
 * 终端参数导入
 * 通讯报文格式
 * 起始符      Length     Data       校验
 * 0x5115(2byte)  2byte    N byte     1byte
 */

public class TerminalParameter {

    private static final String TAG = TerminalParameter.class.getSimpleName();

    private final int RECEIVE_TIMEOUT_S = 10;  //10秒
    private final int UART_EVEAL_TIMEOUT_MS = 1000;   //1s
    private final int PACKAGE_LEN_MIN = 5;
    private boolean isReceivedEndTag = false, isTerminalByUser = false, isImporting = false;
    ISerialPortDev serialPortDev = null;

    /**
     * 通过PC工具导入终端参数
     *
     * @return true 导入成功
     */
    public boolean importParameter() {
        boolean isImportSuccess = false;
        try {
            serialPortDev = DeviceFactory.getInstance().getSerialPort();
            serialPortDev.open(EnumRate.R115200);
            byte[] receiveBuff;
            int timeOutCount = 0;
            boolean isOver = false;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            do {
                /**
                 * 接收串口数据
                 */
                do {
                    receiveBuff = serialPortDev.receive(UART_EVEAL_TIMEOUT_MS);
                    if (receiveBuff != null)
                        break;
                    XLogUtil.d(TAG, "^_^ 未收到串口数据 ^_^");
                    if (isImporting)
                        isOver = !(++timeOutCount < RECEIVE_TIMEOUT_S);
                    else
                        isOver = isTerminalByUser;
                } while (!isOver);
                if (receiveBuff == null)
                    break;
                outputStream.write(receiveBuff);
                XLogUtil.d(TAG, "^_^ 接收数据：" + HexUtil.bytesToHexString(receiveBuff) + " ^_^");
                timeOutCount = 0;
                /**
                 * 校验、解析、处理数据
                 */
                if (!isImporting)
                    isImporting = true;
                boolean isParamSaveOK = false;
                do {
                    if (!checkReceiveComplete(outputStream.toByteArray()))
                        break;
                    if (!checkPackage(outputStream.toByteArray()))
                        break;
                    receiveBuff = decryptPkgData(outputStream.toByteArray());
                    XLogUtil.d(TAG, "^_^ 数据明文：" + HexUtil.bytesToHexString(receiveBuff) + " ^_^");
                    outputStream.reset();
                    if (!parseAndSaveParameter(receiveBuff))
                        break;
                    isParamSaveOK = true;
                } while (false);
                //返回应答数据
                sendResponse(isParamSaveOK);
                if (isReceivedEndTag) {
                    isImportSuccess = true;
                    break;
                }
            } while (true);
            outputStream.close();
        } catch (Exception e) {
            XLogUtil.e(TAG, "^_^ 参数导入异常:" + e.getMessage() + " ^_^");
        }
        if (serialPortDev != null)
            serialPortDev.close();
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
     * 返回应答数据
     *
     * @param result 接收数据的处理结果
     */
    private void sendResponse(boolean result) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(6);
        try {
            outputStream.write(new byte[]{0x51, 0x15}); //prefix
            outputStream.write(new byte[]{0x00, 0x01}); //LEN
            outputStream.write(new byte[]{(byte) (result ? 0 : 1)});    //result
            outputStream.write(calculateCheckSum(outputStream.toByteArray()));  //check sum
            if (serialPortDev != null) {
                XLogUtil.d(TAG, "^_^ 应答数据：" + HexUtil.bytesToHexString(outputStream.toByteArray()) + " ^_^");
                serialPortDev.send(outputStream.toByteArray());
            }
        } catch (IOException e) {
            XLogUtil.e(TAG, "^_^ 发送数据失败：" + e.getMessage() + " ^_^");
        }
    }

    /**
     * 检查数据报文是否接收完成
     *
     * @param bytes 接收到的数据
     */
    private boolean checkReceiveComplete(byte[] bytes) {
        if (bytes == null || bytes.length <= PACKAGE_LEN_MIN)
            return false;
        long pagLen = HexUtil.bytes2short(Arrays.copyOfRange(bytes, 2, 4));
        return pagLen + 5 == bytes.length;
    }

    /**
     * 解析报文并保存参数内容
     *
     * @param receiveBuff 报文数据
     * @return true 成功
     */
    private boolean parseAndSaveParameter(byte[] receiveBuff) {
        int tlvDataLen = HexUtil.bytes2short(Arrays.copyOfRange(receiveBuff, 2, 4));
        byte[] tlvData = Arrays.copyOfRange(receiveBuff, 4, 4 + tlvDataLen);

        int offset = 0;
        byte tag, len;
        byte[] value;

        do {
            tag = tlvData[offset];
            len = tlvData[offset + 1];
            offset += 2;
            if (offset + len > tlvDataLen) {
                XLogUtil.e(TAG, "^_^ 超出数据长度 ^_^");
                return false;
            }

            if (len == 0)
                continue;

            value = Arrays.copyOfRange(tlvData, offset, offset + len);
            offset += len;

            if (!saveParameter(tag, value)) {
                XLogUtil.e(TAG, "^_^ 保存参数失败，TAG:" + HexUtil.bytesToHexString(new byte[]{tag}) + " ^_^");
                return false;
            }
            if (isReceivedEndTag)
                break;
        } while (offset + 2 <= tlvDataLen);
        return true;
    }

    /**
     * 保存参数
     *
     * @param tag 标签
     * @param value 内容
     * @return true 保存成功
     */
    private boolean saveParameter(byte tag, byte[] value) {
        if (value == null || value.length == 0)
            return false;
        Context context = EposApplication.getAppContext();
        int tagIndex = tag & 0x000000ff;
        switch (tagIndex) {
            case ParameterTLVTag.MECHANT_NUM:
                BusinessConfig.getInstance().setIsoField(context, 42, new String(value));
                break;
            case ParameterTLVTag.TERMINAL_NUM:
                BusinessConfig.getInstance().setIsoField(context, 41, new String(value));
                break;
            case ParameterTLVTag.MERCHANT_NAME:
                try {
                    BusinessConfig.getInstance().setValue(context, BusinessConfig.Key.KEY_MCHNT_NAME, new String(value,
                            "GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    return false;
                }
                break;
            case ParameterTLVTag.COMM_TPDU:
                Settings.setCommonTPDU(context, new String(value));
                break;
            case ParameterTLVTag.COMM_END:
                isReceivedEndTag = true;
                break;
            case ParameterTLVTag.APN_ACCESS:
            case ParameterTLVTag.APN_USER_NAME:
            case ParameterTLVTag.APN_USER_PWD:
            case ParameterTLVTag.AREA_CODE:
            case ParameterTLVTag.COMM_TYPE:
            case ParameterTLVTag.CONNECT_TIMEOUT:
            case ParameterTLVTag.E_SLIP_SIGN:
            case ParameterTLVTag.IMS_MODE:
            case ParameterTLVTag.MANAGER_PWD:
            case ParameterTLVTag.OUTER_PHONE_NUM:
            case ParameterTLVTag.OUTER_READER_INTERFACE:
            case ParameterTLVTag.OUTER_WIRELESS_READER:
            case ParameterTLVTag.PRE_DIAL_SERVER:
            case ParameterTLVTag.SCANNER_MODULE:
            case ParameterTLVTag.SERVER_IP_1:
            case ParameterTLVTag.SERVER_IP_2:
            case ParameterTLVTag.SERVER_NUM_1:
            case ParameterTLVTag.SERVER_NUM_2:
            case ParameterTLVTag.SERVER_NUM_3:
            case ParameterTLVTag.SERVER_PORT_1:
            case ParameterTLVTag.SERVER_PORT_2:
            case ParameterTLVTag.SLIP_LOGO:
            case ParameterTLVTag.SLIP_NUM:
            case ParameterTLVTag.SM_SUPPORT:
                //暂不处理

                break;
            default:
                return false;
        }
        return true;
    }

    private boolean checkPackage(byte[] receiveBuff) {
        if (receiveBuff == null || receiveBuff.length < PACKAGE_LEN_MIN) {
            XLogUtil.e(TAG, "^_^ 数据为空或长度不足 ^_^");
            return false;
        }

        if (0x51 != receiveBuff[0] || 0x15 != receiveBuff[1]) {
            XLogUtil.e(TAG, "^_^ 报文头校验失败 ^_^");
            return false;
        }

        int tlvDataLen = HexUtil.bytes2short(Arrays.copyOfRange(receiveBuff, 2, 4));
        if (tlvDataLen + 5 != receiveBuff.length) {
            XLogUtil.e(TAG, "^_^ 报文内的长度域内容校验失败 ^_^");
            return false;
        }

        if (!checkPackageSum(receiveBuff)) {
            XLogUtil.e(TAG, "^_^ 校验和校验失败 ^_^");
            return false;
        }

        return true;
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

    /**
     * 待验证数据，最后一位是校验和
     *
     * @param packageData 报文数据
     * @return true 校验成功   false 校验失败
     */
    public boolean checkPackageSum(byte[] packageData) {
        if (packageData == null || packageData.length < 2)
            return false;
        byte sum = packageData[packageData.length - 1];
        return sum == calculateCheckSum(Arrays.copyOf(packageData, packageData.length - 1));
    }


    private static final String COMM_KEY = "Centerm";
    /**
     * 解密收到的数据域数据，解密算法：与Centerm按字节异或。
     *
     * @param pkgData    密文数据
     * @return 明文数据
     */
    private byte[] decryptPkgData(byte[] pkgData) {
        if (pkgData == null || pkgData.length < PACKAGE_LEN_MIN)
            return pkgData;

        int encryptLen = HexUtil.bytes2short(Arrays.copyOfRange(pkgData, 2, 4));
        byte[] encryptedBytes = Arrays.copyOfRange(pkgData, 4, 4 + encryptLen);
        int offset;
        byte[] keyBytes = COMM_KEY.getBytes();
        byte[] plainBytes = new byte[encryptLen];
        for (int i = 0; i < encryptLen; i += keyBytes.length) {
            for (int j = 0; j < keyBytes.length; j++) {
                offset = i + j;
                if (offset >= encryptLen)
                    break;
                plainBytes[offset] = (byte) (keyBytes[j] ^ encryptedBytes[offset]);
            }
        }
        byte[] plainPkgData = new byte[pkgData.length];
        System.arraycopy(pkgData, 0, plainPkgData, 0, pkgData.length);
        System.arraycopy(plainBytes, 0, plainPkgData, 4, plainBytes.length);
        return plainPkgData;
    }
}
