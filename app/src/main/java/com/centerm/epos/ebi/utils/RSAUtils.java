package com.centerm.epos.ebi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import android.util.Base64;

import com.centerm.cpay.midsdk.dev.common.utils.HexUtils;

/**
 * Created by liubit on 2017/12/26.
 * rsa公钥加解密算法
 */

public class RSAUtils {
    private static RSAPublicKey publicKey = null;
    public static final String RSA = "RSA";// 非对称加密密钥算法
    public static final String ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";//加密填充方式
    /**************************** RSA 公钥加密解密**************************************/
    /**
     * 从字符串中加载公钥,从服务端获取
     *
     * @param pubKey
     *            公钥数据字符串
     * @throws Exception
     *             加载公钥时产生的异常
     */
    public static void loadPublicKey(String pubKey) {
        try {
            byte[] buffer = Base64.decode(pubKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadPublicKey(InputStream in) {
        try {
            String pubKey = readKey(in);
            byte[] buffer = Base64.decode(pubKey, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取密钥信息
     *
     * @param in
     * @return
     * @throws IOException
     */
    private static String readKey(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String readLine = null;
        StringBuilder sb = new StringBuilder();
        while ((readLine = br.readLine()) != null)
        {
            if (readLine.charAt(0) == '-')
            {
                continue;
            } else
            {
                sb.append(readLine);
                sb.append('\r');
            }
        }

        return sb.toString();
    }

    /**
     * 公钥加密过程
     *
     * @param pubKey
     *            公钥
     * @param plainData
     *            明文数据
     * @return
     * @throws Exception
     *             加密过程中的异常信息
     */
    public static String encryptWithRSA(String pubKey, String plainData) throws Exception {
        loadPublicKey(pubKey);
        if (publicKey == null) {
            throw new NullPointerException("encrypt PublicKey is null !");
        }

        Cipher cipher = null;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// 此处如果写成"RSA"加密出来的信息JAVA服务器无法解析

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(plainData.getBytes("utf-8"));
        // 必须先encode成 byte[]，再转成encodeToString，否则服务器解密会失败
        byte[] encode = Base64.encode(output, Base64.DEFAULT);
        return Base64.encodeToString(encode, Base64.DEFAULT);
    }

    /**
     * 公钥加密过程
     *
     * @param plainData
     *            明文数据
     * @return
     * @throws Exception
     *             加密过程中的异常信息
     */
    public static String encryptWithRSA(String plainData) throws Exception {
        if (publicKey == null) {
            throw new NullPointerException("encrypt PublicKey is null !");
        }

        Cipher cipher = null;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// 此处如果写成"RSA"加密出来的信息JAVA服务器无法解析

        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(plainData.getBytes("utf-8"));
        // 必须先encode成 byte[]，再转成encodeToString，否则服务器解密会失败
        //byte[] encode = Base64.encode(output, Base64.DEFAULT);
        //return Base64.encodeToString(encode, Base64.DEFAULT);
        return HexUtils.bytesToHexString(output);
    }

    /**
     * 公钥解密过程
     *
     * @param pubKey
     *            公钥
     * @param encryedData
     *            明文数据
     * @return
     * @throws Exception
     *             加密过程中的异常信息
     */
    public static String decryptWithRSA(String pubKey, String encryedData) throws Exception {
        loadPublicKey(pubKey);
        if (publicKey == null) {
            throw new NullPointerException("decrypt PublicKey is null !");
        }

        Cipher cipher = null;
        cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// 此处如果写成"RSA"解析的数据前多出来些乱码
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] output = cipher.doFinal(Base64.decode(encryedData, Base64.DEFAULT));
        return new String(output);
    }
    /**************************** RSA 公钥加密解密**************************************/


}