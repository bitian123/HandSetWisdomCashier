package com.centerm.epos.ebi.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES/3DES加解密的辅助类
 * 完善补充工作者 Zhixiang Liu
 * 完善时间 2012.05.07
 *
 * @原作者 Jianping Wang
 */
public class SecurityUtil {

    private static final String ALGORITHM = "DES";
    private static SecurityUtil util = new SecurityUtil();

    public static SecurityUtil getInstance() {
        return util;
    }

    /**
     * DES加密
     *
     * @param key 加密密钥
     * @param source 明文
     * @return string 密文(16进制数字符串)
     */
    public String encryptDES(String key, String source) {
        return bcd2str(encryptDes(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * DES加密
     *
     * @param keybyte 加密密钥
     * @param src 明文
     * @return byte[] 密文
     */
    public static byte[] encryptDes(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
            // 加密
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            return cipher.doFinal(src);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    /**
     * DES解密
     *
     * @param key 解密密钥
     * @param source 密文
     * @return string 明文(16进制数字符串)
     */
    public static String decryptDES(String key, String source) {
        return bcd2str(decryptDes(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * DES解密
     *
     * @param keybyte 解密密钥
     * @param src 密文
     * @return byte[] 明文
     */
    public static byte[] decryptDes(byte[] keybyte, byte[] src) {
        try {
            // 生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, ALGORITHM);
            // 解密
            Cipher cipher = Cipher.getInstance(ALGORITHM + "/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, deskey);
            return cipher.doFinal(src);
        } catch (Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }

    /**
     * 3DES加密
     *
     * @param key 加密密钥(16字节长度)
     * @param source 明文
     * @return string 密文(16进制数字符串)
     */
    public static String encrype3DES(String key, String source) {
        return bcd2str(encrype3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    public static String encrype3DES(String key, byte[] source) {
        return bcd2str(encrype3Des(hexStringToByte(key.toUpperCase()), source));
    }

    /**
     * 3DES加密
     *
     * @param key 加密密钥(16字节长度)
     * @param source 明文
     * @return byte[] 密文(16进制数字符串)
     */
    public static byte[] encrype3DESbyte(String key, String source) {
        return encrype3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase()));
    }

    /**
     * 3DES加密
     *
     * @param key 加密密钥(16字节长度)
     * @param source 明文
     * @return byte[] 密文
     */
    public static byte[] encrype3Des(byte[] key, byte[] source) {
        //初始化加密数据块
        byte[] cursorSourceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourceBytes, 0, 8);
        //初始化左半部分密钥
        byte[] keyLeft = new byte[8];
        System.arraycopy(key, 0, keyLeft, 0, 8);
        //初始化右半部分密钥
        byte[] keyRight = new byte[8];
        System.arraycopy(key, 8, keyRight, 0, 8);
        //第一步 : 用左半部分密钥对数据进行DES加密
        byte[] encryptResultBytes = encryptDes(keyLeft, cursorSourceBytes);
        //第二步 : 用右半部分密钥对第一步加密结果进行DES解密
        byte[] decryptResultbytes = decryptDes(keyRight, encryptResultBytes);
        //第三步 : 用左半部分密钥对第三步解密结果进行DES加密
        byte[] cursorResultBytes = encryptDes(keyLeft, decryptResultbytes);
        if (source.length > 8) {//判断是否有多个8字节数据块
            //初始化下一个数据块
            byte[] tempSourceBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, tempSourceBytes, 0, source.length - 8);
            //下一个数据库加密结果
            byte[] subRelultBytes = encrype3Des(key, tempSourceBytes);
            byte[] resultBytes = new byte[cursorResultBytes.length + subRelultBytes.length];
            //合并加密结果
            System.arraycopy(cursorResultBytes, 0, resultBytes, 0, cursorResultBytes.length);
            System.arraycopy(subRelultBytes, 0, resultBytes, cursorResultBytes.length, subRelultBytes.length);
            return resultBytes;
        }
        return cursorResultBytes;
    }

    /**
     * 3DES加密
     *
     * @param key 加密密钥(24字节长度)
     * @param source 明文
     * @return byte[] 密文(24进制数字符串)
     */
    public static byte[] encrypeD3DESbyte(String key, String source) {
        return encrypeD3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase()));
    }

    /**
     * 3DES加密
     *
     * @param key 加密密钥(24字节长度)
     * @param source 明文
     * @return byte[] 密文
     */
    public static byte[] encrypeD3Des(byte[] key, byte[] source) {
        //初始化加密数据块
        byte[] cursorSourceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourceBytes, 0, 8);
        //初始化左半部分密钥
        byte[] keyLeft = new byte[8];
        System.arraycopy(key, 0, keyLeft, 0, 8);
        //初始化右半部分密钥
        byte[] keyRight = new byte[8];
        System.arraycopy(key, 8, keyRight, 0, 8);

        byte[] key3th = new byte[8];
        System.arraycopy(key, 16, key3th, 0, 8);

        //第一步 : 用左半部分密钥对数据进行DES加密
        byte[] encryptResultBytes = encryptDes(keyLeft, cursorSourceBytes);
        //第二步 : 用右半部分密钥对第一步加密结果进行DES解密
        byte[] decryptResultbytes = decryptDes(keyRight, encryptResultBytes);
        //第三步 : 用左半部分密钥对第三步解密结果进行DES加密
        byte[] cursorResultBytes = encryptDes(key3th, decryptResultbytes);
        if (source.length > 8) {//判断是否有多个8字节数据块
            //初始化下一个数据块
            byte[] tempSourceBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, tempSourceBytes, 0, source.length - 8);
            //下一个数据库加密结果
            byte[] subRelultBytes = encrypeD3Des(key, tempSourceBytes);
            byte[] resultBytes = new byte[cursorResultBytes.length + subRelultBytes.length];
            //合并加密结果
            System.arraycopy(cursorResultBytes, 0, resultBytes, 0, cursorResultBytes.length);
            System.arraycopy(subRelultBytes, 0, resultBytes, cursorResultBytes.length, subRelultBytes.length);
            return resultBytes;
        }
        return cursorResultBytes;
    }

    /**
     * 3DES解密
     *
     * @param key 解密密钥(16字节长度)
     * @param source 密文
     * @return string 明文(16进制数字符串)
     */
    public static String decrypt3DES(String key, String source) {
        return bcd2str(decrypt3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    public static String decrypt3DESBase(String key, String source) {
        try {
            return new String(decrypt3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())),
                    "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String str2HexStr(String str)
    {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (int i = 0; i < bs.length; i++)
        {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = bs[i] & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public static byte[] StrToBCD(String str) {
        return StrToBCD(str, str.length());
    }

    public static byte[] StrToBCD(String str, int numlen) {
        if (numlen % 2 != 0)
            numlen++;

        while (str.length() < numlen) {
            str = "0" + str;
        }

        byte[] bStr = new byte[str.length() / 2];
        char[] cs = str.toCharArray();
        int i = 0;
        int iNum = 0;
        for (i = 0; i < cs.length; i += 2) {

            int iTemp = 0;
            if (cs[i] >= '0' && cs[i] <= '9') {
                iTemp = (cs[i] - '0') << 4;
            } else {
                // 判断是否为a~f
                if (cs[i] >= 'a' && cs[i] <= 'f') {
                    cs[i] -= 32;
                }
                iTemp = (cs[i] - '0' - 7) << 4;
            }
            // 处理低位
            if (cs[i + 1] >= '0' && cs[i + 1] <= '9') {
                iTemp += cs[i + 1] - '0';
            } else {
                // 判断是否为a~f
                if (cs[i + 1] >= 'a' && cs[i + 1] <= 'f') {
                    cs[i + 1] -= 32;
                }
                iTemp += cs[i + 1] - '0' - 7;
            }
            bStr[iNum] = (byte) iTemp;
            iNum++;
        }
        return bStr;

    }

    /**
     * 3DES解密
     *
     * @param key 解密密钥(16字节长度)
     * @param source 密文
     * @return byte[] 明文
     */
    public static byte[] decrypt3Des(byte[] key, byte[] source) {
        //将16字节密钥分解为各8字节的两个子密钥
        byte[] keyleft = new byte[8];
        System.arraycopy(key, 0, keyleft, 0, 8);
        byte[] keyright = new byte[8];
        System.arraycopy(key, 8, keyright, 0, 8);
        //初始化当前密文
        byte[] cursorSrouceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSrouceBytes, 0, 8);
        //加密步骤一：第一个子密钥对当前密文解密
        byte[] leftencrypt1 = decryptDes(keyleft, cursorSrouceBytes);
        //加密步骤二：第二个子密钥对步骤一加密结果进行加密
        byte[] rightdecrypt2 = encryptDes(keyright, leftencrypt1);
        //加密步骤三A：第一个子密钥对步骤三结果进行解密
        byte[] leftencrypt3 = decryptDes(keyleft, rightdecrypt2);
        if (source.length > 8) {//判断是否含下一个8字节密文数据块
            //初始化下一个密文数据块
            byte[] subSourceBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, subSourceBytes, 0, source.length - 8);
            //下一个密文数据库解密结果
            byte[] subResultBytes = decrypt3Des(key, subSourceBytes);
            //生成解密结果
            byte[] resultBytes = new byte[subResultBytes.length + leftencrypt3.length];
            System.arraycopy(leftencrypt3, 0, resultBytes, 0, leftencrypt3.length);
            System.arraycopy(subResultBytes, 0, resultBytes, leftencrypt3.length, subResultBytes.length);
            return resultBytes;
        }
        return leftencrypt3;
    }

    /**
     * 3DES解密
     *
     * @param key 解密密钥(24字节长度)
     * @param source 密文
     * @return string 明文(24进制数字符串)
     */
    public static String decryptD3DES(String key, String source) {
        return bcd2str(decryptD3Des(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * 3DES解密
     *
     * @param key 解密密钥(24字节长度)
     * @param source 密文
     * @return byte[] 明文
     */
    public static byte[] decryptD3Des(byte[] key, byte[] source) {
        //将16字节密钥分解为各8字节的两个子密钥
        byte[] keyleft = new byte[8];
        System.arraycopy(key, 0, keyleft, 0, 8);
        byte[] keyright = new byte[8];
        System.arraycopy(key, 8, keyright, 0, 8);

        byte[] key3th = new byte[8];
        System.arraycopy(key, 16, key3th, 0, 8);

        //初始化当前密文
        byte[] cursorSrouceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSrouceBytes, 0, 8);
        //加密步骤一：第一个子密钥对当前密文解密
        byte[] leftencrypt1 = decryptDes(key3th, cursorSrouceBytes);
        //加密步骤二：第二个子密钥对步骤一加密结果进行加密
        byte[] rightdecrypt2 = encryptDes(keyright, leftencrypt1);
        //加密步骤三A：第一个子密钥对步骤三结果进行解密
        byte[] leftencrypt3 = decryptDes(keyleft, rightdecrypt2);
        if (source.length > 8) {//判断是否含下一个8字节密文数据块
            //初始化下一个密文数据块
            byte[] subSourceBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, subSourceBytes, 0, source.length - 8);
            //下一个密文数据库解密结果
            byte[] subResultBytes = decryptD3Des(key, subSourceBytes);
            //生成解密结果
            byte[] resultBytes = new byte[subResultBytes.length + leftencrypt3.length];
            System.arraycopy(leftencrypt3, 0, resultBytes, 0, leftencrypt3.length);
            System.arraycopy(subResultBytes, 0, resultBytes, leftencrypt3.length, subResultBytes.length);
            return resultBytes;
        }
        return leftencrypt3;
    }

    /**
     * ANSI-X9.9标准下DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
     * @return string mac值(16进制数字符串)
     */
    public String ansiMacDES(String key, String vector, String source, boolean isHex) throws Exception {
        return mac(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes(), hexStringToByte(key.toUpperCase
                ()), hexStringToByte(vector.toUpperCase()));
    }

    public static byte[] pboc3desmac(byte[] source, byte[] key, byte[] vector) throws Exception {
        byte[] orginal = vector;//原始值
        byte[] leftKey = new byte[8];//左半部分密钥
        byte[] rightKey = new byte[8];//右部分密钥
        System.arraycopy(key, 8, rightKey, 0, 8);
        System.arraycopy(key, 0, leftKey, 0, 8);
        for (int i = 0; i < source.length; i += 8) {
            byte[] temp = new byte[8];
            System.arraycopy(source, i, temp, 0, temp.length);
            orginal = xor(orginal, temp);//异或
            orginal = encryptDes(leftKey, orginal);//用左半部分DES加密
        }
        byte[] debyrightKeySrc = decryptDes(rightKey, orginal);//用右半部分密钥解密
        byte[] encryptLeftKeySrc = encryptDes(leftKey, debyrightKeySrc);//再用左半部分密钥加密
        byte[] result = new byte[8];
        System.arraycopy(encryptLeftKeySrc, 0, result, 0, 8);//得到8字节的结果值
        return result;
    }

    public static String HexByteString(byte[] hexData) {
        StringBuffer tmpBuf = new StringBuffer();
        for (int i = 0; i < hexData.length; i++)
            tmpBuf.append(String.format("%02X", hexData[i]));
        return tmpBuf.toString();
    }

    /**
     * @Title: hexByte2StringByte
     * @Description: 十六进制数扩展为Asc码, 例如:0x1A 变换为 0x31 0x41; 注意: 0x1a 结果也是 0x31 0x41
     * @param: @param hexData
     * @param: @return
     * @return: byte[]
     */
    public static byte[] hexByte2StringByte(byte[] hexData) {
        if (hexData == null)
            return null;

        return HexByteString(hexData).toString().getBytes();
    }


    public String alipayMacAlgorithm(String key, String source) throws Exception {
        return HexByteString(alipay3desmac(hexStringToByte(source.toUpperCase()), hexStringToByte(key.toUpperCase())));
    }

    /**
     * @Title: alipay3desmac
     * @Description: 使用3倍长的3DES加密算法, 填充00到8的倍数, 异或出8byte结果, 结果转换为16byte ASC码,
     * 对前8字节 做3DES加密,结果与后8字节异或,异或结果做3DES加密,加密结果再转换为16byte ASC,取前8 byte 计算结果
     * @param: @param source
     * @param: @param key
     * @param: @return
     * @param: @throws Exception
     * @return: byte[]
     */
    public static byte[] alipay3desmac(byte[] source, byte[] key) throws Exception {
        byte[] temp = new byte[8];
        byte[] xorResult = new byte[8];
        int fillLen = 8 - source.length % 8;
        byte[] filledData = new byte[fillLen + source.length];
        System.arraycopy(source, 0, filledData, 0, source.length);
        for (int i = 0; i < filledData.length; i += 8) {
            System.arraycopy(filledData, i, temp, 0, temp.length);
            xorResult = xor(xorResult, temp);//异或
        }
//		   Log.d("Xor Result:", DataConverter.bytesToHexStringForPrint(xorResult));
        //扩展为16字节,并加密前8字节
        byte[] ascBytes = hexByte2StringByte(xorResult);
        System.arraycopy(ascBytes, 0, temp, 0, 8);
        byte[] encryptedData = encrypeD3Des(key, temp);//3倍长密钥加密
        //与后8字节异常并加密
        System.arraycopy(ascBytes, 8, temp, 0, 8);
        xorResult = xor(encryptedData, temp);//异或
        encryptedData = encrypeD3Des(key, xorResult);//3倍长密钥加密
        //扩展为16字节,取前8字节为MAC值
        ascBytes = hexByte2StringByte(encryptedData);
        byte[] result = new byte[8];
        System.arraycopy(ascBytes, 0, result, 0, 8);//得到8字节的结果值
        return result;
    }

    /**
     * DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @return string mac值(16进制数字符串)
     */
    public String mac(byte[] source, byte[] key, byte[] vector) throws Exception {
        byte[] cursorSourceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length > 8 ? 8 : source.length));
        byte[] sourceLeftXor = xor(cursorSourceBytes, vector);
        byte[] sourceLeftEncrypt = encryptDes(key, sourceLeftXor);
        if (source.length > 8) {
            byte[] tempBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, tempBytes, 0, source.length - 8);
            return mac(tempBytes, key, sourceLeftEncrypt);
        }
        return bcd2str(sourceLeftEncrypt);
    }

    /**
     * PBOC标准下DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
     * @return string mac值(16进制数字符串)
     */
    public String pbocMacDES(String key, String vector, String source, boolean isHex) throws Exception {
        byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes());
        return mac(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
    }

    /**
     * 根据PBOC标准对字节补位
     *
     * @param sourceBytes 需要补充的byte数组
     * @return byte[] 补充完毕的byte数组
     */
    public static byte[] fillBytes(byte[] sourceBytes) {
        int mod = sourceBytes.length % 8;
        byte[] sourceFilledBytes = new byte[sourceBytes.length + (8 - mod)];
        System.arraycopy(sourceBytes, 0, sourceFilledBytes, 0, sourceBytes.length);
        if (mod == 0) {
            byte[] fillBytes = hexStringToByte("8000000000000000");
            System.arraycopy(fillBytes, 0, sourceFilledBytes, sourceBytes.length, fillBytes.length);
        } else {
            for (int i = 0; i < (8 - mod); i++) {
                sourceFilledBytes[sourceBytes.length + i] = hexStringToByte(i == 0 ? "80" : "00")[0];
            }
        }
        return sourceFilledBytes;
    }

    /**
     * ANSI-X9.9标准下3DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
     * @return string mac值(16进制数字符串)
     */
    public String ansiMac3DES(String source, String key, String vector, boolean isHex) throws Exception {
        return mac3Des(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes(), hexStringToByte(key
                .toUpperCase()), hexStringToByte(vector.toUpperCase()));
    }

    /**
     * PBOC标准下3DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @param isHex 标志源数据是否以字符串表示的16进制形式进行操作
     * @return string mac值(16进制数字符串)
     */
    public String pbocMac3DES(String key, String vector, String source, boolean isHex) throws Exception {
        byte[] sourceFilledBytes = fillBytes(isHex ? hexStringToByte(source.toUpperCase()) : source.getBytes());
        return mac3Des(sourceFilledBytes, hexStringToByte(key.toUpperCase()), hexStringToByte(vector.toUpperCase()));
    }

    /**
     * 3DES算法的MAC计算
     *
     * @param source 源数据
     * @param key 加密密钥
     * @param vector 初始向量
     * @return string mac值(16进制数字符串)
     */
    public String mac3Des(byte[] source, byte[] key, byte[] vector) throws Exception {
        byte[] cursorSourceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourceBytes, 0, (source.length >= 8 ? 8 : source.length));
        byte[] cursorSourceXor = xor(cursorSourceBytes, vector);
        if (source.length > 8) {
            byte[] cursorKey = new byte[8];
            System.arraycopy(key, 0, cursorKey, 0, 8);
            byte[] sourceLeftEncrypt = encryptDes(cursorKey, cursorSourceXor);
            byte[] tempBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, tempBytes, 0, source.length - 8);
            return mac3Des(tempBytes, key, sourceLeftEncrypt);
        }
        return bcd2str(encrype3Des(key, cursorSourceXor));
    }

    /**
     * Diversify密钥分散算法
     *
     * @param source 源数据
     * @param key 主控密钥MK
     * @return string 分散密钥DK(16进制数字符串)
     */
    public String diversify(String key, String source) {
        return bcd2str(diversify(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * Diversify密钥分散算法
     *
     * @param source 源数据
     * @param key 主控密钥MK
     * @return byte[] 分散密钥DK(16进制数字符串)
     */
    public byte[] diversify(byte[] key, byte[] source) {
        //当前分散数据
        byte[] cursorSourceBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourceBytes, 0, source.length > 8 ? 8 : source.length);
        //推导左半部分key
        byte[] leftDivBytes = encrype3Des(key, cursorSourceBytes);
        //当前分散数据取反
        for (int i = 0; i < cursorSourceBytes.length; i++) {
            cursorSourceBytes[i] = (byte) ~cursorSourceBytes[i];
        }
        //推导右半部分key
        byte[] rightDivBytes = encrype3Des(key, cursorSourceBytes);
        //合并
        byte[] resultBytes = new byte[leftDivBytes.length + rightDivBytes.length];
        System.arraycopy(leftDivBytes, 0, resultBytes, 0, leftDivBytes.length);
        System.arraycopy(rightDivBytes, 0, resultBytes, leftDivBytes.length, rightDivBytes.length);
        if (source.length > 8) {//判断是否二次分散运算
            byte[] tempBytes = new byte[source.length - 8];
            System.arraycopy(source, 8, tempBytes, 0, source.length - 8);
            return diversify(resultBytes, tempBytes);
        }
        return resultBytes;
    }

    /**
     * Double-One-Way分散算法
     *
     * @param source 源数据
     * @param key 主控密钥MK
     * @return string 分散密钥DK(16进制数字符串)
     */
    public String diversifyByDoubleOneWay(String source, String key) throws Exception {
        return diversifyDouble(source, key);
    }

    /**
     * Double-One-Way分散运算
     *
     * @param source 源数据
     * @param key 主控密钥MK
     * @return string 分散密钥DK(16进制数字符串)
     */
    private String diversifyDouble(String source, String key) throws Exception {
        byte[] keyleft = hexStringToByte(key.substring(0, key.length() / 2).toUpperCase());
        byte[] keyright = hexStringToByte(key.substring(key.length() / 2).toUpperCase());
        byte[] sourceBytes = hexStringToByte(source.toUpperCase());
        byte[] sourceUnDes = decryptDes(keyleft, sourceBytes);
        byte[] sourceunDesDes = encryptDes(keyright, sourceUnDes);
        byte[] sourceunDesDesUnDes = decryptDes(keyleft, sourceunDesDes);
        byte[] keyleftXor = xor(sourceBytes, sourceunDesDesUnDes);
        return bcd2str(keyleftXor);
    }

    /**
     * 异或运算
     *
     * @param xor1 操作数1
     * @param xor2 操作数2
     * @return string 异或结果(16进制数字符串)
     */
    public String xor(String xor1, String xor2) throws Exception {
        return bcd2str(xor(hexStringToByte(xor1), hexStringToByte(xor2)));
    }

    /**
     * 异或运算
     *
     * @param hexSource1 操作数1
     * @param hexSource2 操作数2
     * @return byte[] 异或结果(16进制数字符串)
     */
    public static byte[] xor(byte[] hexSource1, byte[] hexSource2) throws Exception {
        int length = hexSource1.length;
        byte[] xor = new byte[length];
        for (int i = 0; i < length; i++) {
            xor[i] = (byte) (hexSource1[i] ^ hexSource2[i]);
        }
        return xor;
    }

    /**
     * 将字节转换成16进制数字符
     *
     * @param bcds 待转换的byte数组
     * @return string 转换结果(字母都转为大写)
     */
    public static String bcd2str(byte[] bcds) {
        char[] ascii = "0123456789abcdef".toCharArray();
        byte[] temp = new byte[bcds.length * 2];
        for (int i = 0; i < bcds.length; i++) {
            temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
            temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
        }
        StringBuffer res = new StringBuffer();

        for (int i = 0; i < temp.length; i++) {
            res.append(ascii[temp[i]]);
        }
        return res.toString().toUpperCase();
    }

    /**
     * 16进制字符串转换成字节数组
     *
     * @param hex 待转换的字符串
     * @return byte[] 结果数组
     */
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    /**
     * ECB加密
     *
     * @param key 加密密钥
     * @param source 明文
     * @return string 密文(16进制数字符串)
     */
    public String encryptECB(String key, String source) {
        return bcd2str(encryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * ECB-3DES加密
     *
     * @param key 加密密钥
     * @param source 明文
     * @return byte[] 密文
     */
    public byte[] encryptECB(byte[] key, byte[] source) {
        byte[] cursorSourntBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourntBytes, 0, source.length > 8 ? 8 : source.length);
        byte[] currorEncryptResult = key.length > 8 ? encrype3Des(key, cursorSourntBytes) : encryptDes(key,
                cursorSourntBytes);
        if (source.length > 8) {
            byte[] nextSource = new byte[source.length - 8];
            System.arraycopy(source, 8, nextSource, 0, source.length - 8);
            byte[] subEncryptResult = encryptECB(key, nextSource);
            byte[] encryptResult = new byte[currorEncryptResult.length + subEncryptResult.length];
            System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
            System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
            return encryptResult;
        }
        return currorEncryptResult;
    }

    /**
     * ECB-3DES解密
     *
     * @param key 解密密钥
     * @param source 密文
     * @return String 明文
     */
    public static String decryptECB(String key, String source) {
        return bcd2str(decryptECB(hexStringToByte(key.toUpperCase()), hexStringToByte(source.toUpperCase())));
    }

    /**
     * ECB-3DES解密
     *
     * @param key 解密密钥
     * @param source 密文
     * @return byte[] 明文
     */
    public static byte[] decryptECB(byte[] key, byte[] source) {
        byte[] cursorSourntBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourntBytes, 0, source.length > 8 ? 8 : source.length);
        byte[] currorDecryptResult = key.length > 8 ? decrypt3Des(key, cursorSourntBytes) : decryptDes(key,
                cursorSourntBytes);
        if (source.length > 8) {
            byte[] nextSource = new byte[source.length - 8];
            System.arraycopy(source, 8, nextSource, 0, source.length - 8);
            byte[] subEncryptResult = decryptECB(key, nextSource);
            byte[] encryptResult = new byte[currorDecryptResult.length + subEncryptResult.length];
            System.arraycopy(currorDecryptResult, 0, encryptResult, 0, currorDecryptResult.length);
            System.arraycopy(subEncryptResult, 0, encryptResult, currorDecryptResult.length, subEncryptResult.length);
            return encryptResult;
        }
        return currorDecryptResult;
    }

    /**
     * CBC-3DES加密
     *
     * @param key 加密密钥
     * @param vector 初始向量
     * @param source 明文
     * @return string 密文
     */
    public String encryptCBC(String key, String vector, String source) throws Exception {
        return bcd2str(encryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector != null ? vector :
                "0000000000000000"), hexStringToByte(source.toUpperCase())));
    }

    /**
     * CBC-3DES加密
     *
     * @param key 加密密钥
     * @param vector 初始向量
     * @param source 明文
     * @return byte[] 密文
     */
    public byte[] encryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
        byte[] cursorSourntBytes = new byte[8];
        System.arraycopy(source, 0, cursorSourntBytes, 0, source.length > 8 ? 8 : source.length);
        byte[] xorResultBytes = xor(cursorSourntBytes, vector);
        byte[] currorEncryptResult = key.length > 8 ? encrype3Des(key, xorResultBytes) : encryptDes(key,
                xorResultBytes);
        if (source.length > 8) {
            byte[] nextSource = new byte[source.length - 8];
            System.arraycopy(source, 8, nextSource, 0, source.length - 8);
            byte[] subEncryptResult = encryptCBC(key, currorEncryptResult, nextSource);
            byte[] encryptResult = new byte[currorEncryptResult.length + subEncryptResult.length];
            System.arraycopy(currorEncryptResult, 0, encryptResult, 0, currorEncryptResult.length);
            System.arraycopy(subEncryptResult, 0, encryptResult, currorEncryptResult.length, subEncryptResult.length);
            return encryptResult;
        }
        return currorEncryptResult;
    }

    /**
     * CBC-3DES解密
     *
     * @param key 解密密钥
     * @param vector 初始向量
     * @param source 密文
     * @return string 明文
     */
    public String decryptCBC(String key, String vector, String source) throws Exception {
        return bcd2str(decryptCBC(hexStringToByte(key.toUpperCase()), hexStringToByte(vector != null ? vector :
                "0000000000000000"), hexStringToByte(source.toUpperCase())));
    }

    /**
     * CBC-3DES解密
     *
     * @param key 解密密钥
     * @param vector 初始向量
     * @param source 密文
     * @return byte[] 明文
     */
    public byte[] decryptCBC(byte[] key, byte[] vector, byte[] source) throws Exception {
        byte[] decryptBytes = new byte[8];
        System.arraycopy(source, 0, decryptBytes, 0, source.length > 8 ? 8 : source.length);

        byte[] decryptResult = key.length > 8 ? decrypt3Des(key, decryptBytes) : decryptDes(key, decryptBytes);
        byte[] result = xor(decryptResult, vector);
        if (source.length > 8) {
            byte[] nextSource = new byte[source.length - 8];
            System.arraycopy(source, 8, nextSource, 0, source.length - 8);
            byte[] subDecryptResult = decryptCBC(key, decryptBytes, nextSource);
            byte[] encryptResult = new byte[result.length + subDecryptResult.length];
            System.arraycopy(result, 0, encryptResult, 0, result.length);
            System.arraycopy(subDecryptResult, 0, encryptResult, result.length, subDecryptResult.length);
            return encryptResult;
        }
        return result;
    }

    /**
     * 将字符转换为对应的16进制字节
     *
     * @param c 字符
     * @return byte 对应字节
     */
    private static byte toByte(char c) {
        byte b = (byte) "0123456789ABCDEF".indexOf(c);
        return b;
    }

    /**
     * 检查名称为name的字符串value是否满足长度为length且每个字符是否都为16进制字符
     *
     * @param name 字符串名称
     * @param value 字符串值
     * @param length 字符串长度(小等于0时不进行长度校验)
     * @return boolean
     */
    public static boolean isHexademical(String name, String value, int length) throws Exception {
        if (null == value || (value.length() != length && length > 0)) {
            throw new Exception(name + "长度应为" + length);
        }
        String texts = "0123456789abcdefABCDEF";
        int len = value.length();
        for (int i = 0; i < len; i++) {
            if (texts.indexOf(value.charAt(i)) == -1) {
                throw new Exception(name + "包含的字符应为16进制字符");
            }
        }
        return true;
    }

    /**
     * 检查名称为name的字符串value是否满足长度为length且每个字符是否都为字母或数字
     *
     * @param name 字符串名称
     * @param value 字符串值
     * @param length 字符串长度(小于0时不进行长度校验)
     * @return boolean
     */
    public static boolean isAlphanumeric(String name, String value, int length) throws Exception {
        if (null == value || (value.length() != length && length > 0)) {
            throw new Exception(name + "长度应为" + length);
        }
        String texts = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int len = value.length();
        for (int i = 0; i < len; i++) {
            if (texts.indexOf(value.charAt(i)) == -1) {
                throw new Exception(name + "包含的字符应为数字或字母");
            }
        }
        return true;
    }

    public static String getMD5Str(String str) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer md5StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }
        return md5StrBuff.toString().toUpperCase();
    }

    public static String getSHA1Str(String str){
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
            messageDigest.reset();
            messageDigest.update(str.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        byte[] byteArray = messageDigest.digest();
        StringBuffer sha1StrBuff = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                sha1StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                sha1StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }


        return sha1StrBuff.toString().toUpperCase();
    }

    public static String encrptyRSA(X509Certificate cert, String data) {
        if (cert == null || TextUtils.isEmpty(data)) {
            return null;
        }

        try {
            byte[] dataStr = data.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.ENCRYPT_MODE, cert);
            // 传入编码数据并返回编码结果
            byte[] enResult = cipher.doFinal(dataStr);
            return bcd2str(enResult);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static final byte[] hex2byte(String var0) throws IllegalArgumentException {
        if (var0.length() % 2 != 0) {
            throw new IllegalArgumentException();
        } else {
            char[] var1 = var0.toCharArray();
            byte[] var2 = new byte[var0.length() / 2];
            int var3 = 0;
            int var4 = 0;

            for (int var5 = var0.length(); var3 < var5; ++var4) {
                String var6 = "" + var1[var3++] + var1[var3];
                int var7 = Integer.parseInt(var6, 16) & 255;
                var2[var4] = (new Integer(var7)).byteValue();
                ++var3;
            }

            return var2;
        }
    }


    public static String decryptRSA(X509Certificate cert, String ciphertext) {
        if (cert == null || TextUtils.isEmpty(ciphertext)) {
            return null;
        }
        try {
//            byte[] dataStr = hexStringToByte(ciphertext);
            byte[] dataStr = hex2byte(ciphertext);
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.DECRYPT_MODE, cert);
            // 传入编码数据并返回编码结果
            byte[] result = cipher.doFinal(dataStr);
            return bcd2str(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean veritySign(X509Certificate cert, String oriData, String signData){
        if (cert == null || TextUtils.isEmpty(oriData) || TextUtils.isEmpty(signData)) {
            return false;
        }
        try {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(cert);
            signature.update(oriData.getBytes("UTF-8"));
            return signature.verify(hexStringToByte(signData));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获得随机密钥 未加密
     * @return
     */
    public static String calRandomKey(){
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<32;i++){
            sb.append(getRandom(9));
        }
        return sb.toString();
    }

    /**
     * 获取0-max的随机数
     * @param max 最大值
     * @return
     */
    public static String getRandom(int max){
        Random random = new Random();
        int a=random.nextInt(max+1);
        return a+"";
    }

    public static X509Certificate getPayCertificate(Context context) {
        try {
            CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509", "BC");
            InputStream bais = context.getAssets().open("cmp-pay.crt");
            X509Certificate cert = (X509Certificate) certificatefactory.generateCertificate(bais);
            return cert;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过终端上送的随机数生成临时通讯密钥。
     * 1. 取客户端32位随机数中的：第4位到第12位， 第20位到第28位，拼接成一个十六位的字符串；
     * 2. 对拼接好的字符串进行MD5加密，且全部转换成大写，即可得到加密密钥；
     *
     * @param random 随机数
     * @return 密钥
     */
    public static String generateTempCommKey(String random) {
        if (TextUtils.isEmpty(random)) {
            return null;
        }
        if (random.length() < 32) {
            return null;
        }
        StringBuilder stringBuffer = new StringBuilder(24);
        stringBuffer.append(random.substring(3, 11))
                .append(random.substring(19, 27));
        return SecurityUtil.getMD5Str(stringBuffer.toString());
    }

    /**
     * 通过终端和平台产生的随机数，根据规范描述生成通讯密钥。
     * 1. 取32位随机数中的：第4位到第12位， 第20位到第28位，拼接成一个十六位的字符串 StringA；
     * 2. 取客户端上送的随机数后9位 StringB；
     * 3. 拼接StringC = StringA + StringB；
     * 4. 对拼接好的字符串进行MD5加密，且全部转换成大写，即可得到加密密钥；
     *
     * @param serverRandom 平台返回的32字节随机数
     * @param clientRandom 终端生成的32字节随机数
     * @return 密钥
     */
    public static String generateCommunicationKey(String serverRandom, String clientRandom) {
        if (TextUtils.isEmpty(serverRandom) || TextUtils.isEmpty(clientRandom)) {
            Log.e("===", "^_^ generateCommunicationKey 的输入参数为空！^_^");
            return null;
        }
        if (serverRandom.length() < 32 || clientRandom.length() < 32) {
            Log.e("===", "^_^ 终端或平台的随机数长度错误！^_^");
            return null;
        }
        StringBuilder stringBuffer = new StringBuilder(24);
        stringBuffer.append(serverRandom.substring(3, 11))
                .append(serverRandom.substring(19, 27))
                .append(clientRandom.substring(23, 32));
        Log.d("===", "^_^ 通讯密钥组合数据：" + stringBuffer.toString() + " ^_^");
        return SecurityUtil.getMD5Str(stringBuffer.toString());
    }

    public static void main(String[] args) {
        /*byte[] gaucDefaultSecurityKey = {
                0x43, 0x65, 0x6E, 0x74, 0x65, 0x72, 0x6D, 0x43,
				0x39, 0x32, 0x30, 0x61, 0x6C, 0x70, 0x61, 0x79};*/

//		byte[] gaucDefaultSecurityKey = {0x43, 0x65, 0x6E, 0x74, 0x65, 0x72, 0x6D, 0x5F, 0x43, 0x39, 0x32, 0x30, 0x45,
// 0x5F, 0x41, 0x6C, 0x69, 0x70, 0x61, 0x79, 0x32, 0x30, 0x31, 0x35};
//		byte[] dataFill = SecurityUtil.fillBytes("111111111111111111111111111111111111111111111111".getBytes());
//		//System.out.println(HexBinary.encode(dataFill));
//		byte[] dataEn = SecurityUtil.getInstance().encrypeD3Des(HexBinary.decode
// ("43656E7465726D5F43393230455F416C6970617932303135"), dataFill);
//		System.out.println("密文：" + HexBinary.encode(dataEn));
//		//String str1 =
// "CBAABB7FAE3FABD964B0E9686F9BDD4E4EAC73AD882CD4F1233EA7F46A0B45ED1D0007A955BEE337C866DE9AFCE8332FC431A969BF768D64EBCD834DCC14D9456DFC013F828946CB" ;
//		//String str2 = "8B700A44AD63606A1F13E0DF5ACC322155077DBFAEF6612CBF603DD36B19B6271E9E9C7A7551A231";
//		dataEn = HexBinary.decode
// ("7BE7DE713A3588B4E0933B7DC36C7E846CF1563DFE3FA30AD5DFF7D43F263791F655765299BB04DE152A4843B7580BD7E0153F888DB55A376CD302B72B6F5F48C16D83867C7789B0ED4819ACB8F7F6D492B5F8245B00923FCA68F47354B00EA16129379DCCF7AF597FD16F003573D68F6645BE8A2EAE9377ED2516ED662DD8BA6B338BB3B29E14110EA193EB986BCBB1C47AA27AFEB796F35188770C63816E076754F7672D5A4EF742E34572F1808536BDC1D3FF872FAE4B976C129C52E707BF87D2377ED2E5284D8023B17BA823C06C0268A4E2619E15E9CBD7AAC4C48B6AF44D625EB8489280FBA5A8535A912811462FF32D25BDE5B370CF880783E8E2EE2445D4277BC6045D27BD3CA26DB9907814478395A10BAADE3D0699D829528976835005C29A4CF9E3F338F0784AE2836A4BAA46E8AB6F53A0E31D75A5D6F22A4A80C373628FA014E43CFC15AB3E416BDDA9C6C441BE5B0E7B8CDDB031F115AB19243353131D16EF144DFBC0ECB5B3F309FE4241DB7D530CFDF1E31EB7A34B45E8C4CF25D83C8FD0CEADD25747B666342DD2B3FEFA0C0FE71D6D6BF49431475F4004FE3580C865B8055A39460DBE94404B0358C0779DE919877216AC50C333609B86D442A9E72F7EDAE17FFBDFD916ED35CA38512507FCFF9FEC25D7F756D9812B36704F3EF3695DBA148F6A2CCC9DBD1B42B8C3D3EBE3CE607D181417625E4477451FE1BA7B4CFB4AAFB8C1F82DC4DDA6E842BB72B12BDA10D0907E130A61CAEA408C5F5B9985E9305A");
//		byte[] data = SecurityUtil.getInstance().decryptD3Des("123456789012345678901234".getBytes(), dataEn);
//		System.out.println("明文：" + HexBinary.encode(data));
    }

    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public static String SHA256(final String strText) {
        return SHA(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public static String SHA512(final String strText) {
        return SHA(strText, "SHA-512");
    }

    /**
     * 字符串 SHA 加密
     *
     * @param strSourceText
     * @return
     */
    private static String SHA(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // SHA 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    // 转化十六进制编码为字符串 压缩为16进制数
    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length()/2];
        for(int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

}
