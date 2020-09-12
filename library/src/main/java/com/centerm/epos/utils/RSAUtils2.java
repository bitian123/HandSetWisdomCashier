package com.centerm.epos.utils;

import android.util.Base64;
import android.util.Log;

import com.centerm.epos.EposApplication;

import org.json.JSONObject;

import config.BusinessConfig;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by xux on 2018/1/19.
 */
public class RSAUtils2 {


    public static boolean checkRSA(JSONObject jsonObject, String signData, String pubKey){
       /* YGBizMessage msg = bizCtx.getCurrentMsg();
        Logger logger = YGLogger.getLogger(msg);
        logger.info("��ǩ��ʼ");
        logger.info("jsonObject:" + jsonObject);*/
        boolean chkRslt = false;//Ĭ��ʧ��
        try {
            java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
                    new BASE64Decoder().decodeBuffer(pubKey));
            // RSA�ԳƼ����㷨
            KeyFactory keyFactory;
            keyFactory = KeyFactory.getInstance("RSA");
            // ȡ��Կ�׶���
            PublicKey publicKey = keyFactory.generatePublic(bobPubKeySpec);
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initVerify(publicKey);//���빫Կ
            signature.update(jsonObject.toString() .getBytes("UTF-8"));//��������
            //logger.info("signData:" + signData);
            //��ǩ���
            chkRslt = signature.verify(Base64.decode(signData,Base64.DEFAULT));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //logger.info("data visa after:" + true);
        return chkRslt;
    }

    public static String signRSA(JSONObject jsonObject){
        /*YGBizMessage msg = bizCtx.getCurrentMsg();
        Logger logger = YGLogger.getLogger(msg);
        YGEDB edb = msg.getEDBBody();
        logger.info("sign data  ��ǩ��ʼ");*/

        /* 2. ������ǩ�ֶ�, sign�ֶβ�����ǩ�� mapתString���������Զ���*/
        Log.i("===", "signRSA: "+jsonObject.toString());
        String prvKey = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJh1xQMZ2nsN+YHbabrgiqARNE3oy95NuSXW2CMpgd1Z+DR3wZTbpKs9dpdb6/6e4gf8OrzdnFKCYPb8cHDsh0OA0FfXd2Sydb5FMH5yvnc7UCCfbGUReqKMpVSUFUqgjC05b/TgiWjIM9Ji4byTgWR7gQdQ9FvCggMR/ScpT4O9AgMBAAECgYAR9r5x8RfnK+xcOqgoltB+r8hD7cwns2y/YqMw8XEVzcYLGJk8muy6KJHSn8gcxkfHvRaGrm3+4tHeCDyPca82O/IFP7Fp/jfkHJml60PC5CtSRgwMqfv5faRiLwYBjtKgkxQxXWCnx/WSaCFmyvQ2qjdGbVfY6PkeN5fkBJPNwQJBAOM8INFDgUTVNKu4kxz41OztitX3T6Fyh30CYiYBDLWkE/FTYySCocPqgG+dbZrZJM8QJGK+pc4DMZbHweBMzoUCQQCrwnShVQ0DWHQ7uPJER0DXbbWiQs07O4I+L6ZmHcU3e1BS5Mr4GOgECup1oIG/mjR0N5QTENZZvaKUqm9qCTHZAkAKmlmlqhK0FYa5pIkoxuuYipT52upaTC/KQ3w07cOcDiXoEs4DfBz0OVL44k2k9hDjoIsyGFdAP6HmuTdwseTJAkA2XQLbOXc38tw/Nud/UuokBXZy2B/rOoebtSs+sPeF+wLOadoQMpnlBat19Yp7oYwuqJS2gCLIHqyBz3waHLTpAkARx1W2j3Omleh0WR2gF67SdSYWgrw4hTp9Nx09Tw9aLTFjIJ7iogmO6w0lE6bAORVDLbjqihf+kUbx2YCFz2U8";
        if(!BusinessConfig.getInstance().getFlag(EposApplication.getAppContext(), BusinessConfig.Key.PRO_ENR)){
            prvKey = "MIICeQIBADANBgkqhkiG9w0BAQEFAASCAmMwggJfAgEAAoGBAM4/rC99irvS//RV2SMd4KHzhuqknbcpeJWMXC4FvWt+n06f7FP5ZWemx8VAmEL1/n41jOPbLTgSacuFQQmemsCp5H8d2g/XBqYy1q3f/0qPCi8Tt1yop5efcumjma2jcRdAkSyQClc8GFmEeRkEl19chrEpI/6bxtXoBD2dDLpZAgMBAAECgYEAs4btBDGND0ztCuunJFAfdhkaeShtOD/a/KG+ozjP1r/TP4cpGTdfM0gTX/mID9E8gvNt/fCMfeBZQpRtNkhefoGvlij9SSVJMyvCtqaGEbUzvaBhzLkko+d7YdZeeUg6SWAPTnPSSG3nXRwR3pMa7TqFiVpBJN8HyV8mE2fMznkCQQDsredSzLmkKv/sEZaZNngOfQxjCfBME8+Gnoz+LsQwFoXRNxKToGw1x/yt+cjWAkn+FV9Nb98oPkqPjLdKdDsnAkEA3xXVh/S1R42k1gqu0UKB1hHSQ0EUGqg9i8vWqATf0SomC4SB0K7mgmEUNDrka9KNnw1Bc9TL6w/zmPKCcibOfwJBANMjvMan7kCfP5n4gtIBvo6mTcOYnS8xSSQ+E2e6jribjxt6Nu9N4NsFoswNlnYcqqepp1Bsqba8A0YWcXlRQWcCQQDaYYpdg+yttfgF3AFUMlHdWCbH1X4ztjxBjHJ+mf7rx+HkZnuZ6I0YVqYrlvciocQnThejp01Tt5LUR5nw2xJLAkEAzoOMTiGDRQGh1BHGtbfMO0U7abma+T2XPYUUCQA8QH3B4Lr0Zm1JJ1mcPVsM3yVgBDQqiLXYIy4nKJyvn5kLWg==";
        }
        String line = jsonObject.toString();
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec priPKCS8;
        String sign = "";
        try {
            priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(prvKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            privateKey = keyf.generatePrivate(priPKCS8);

            //����ǩ�����ܷ�ʽ
            Signature signature = Signature.getInstance("MD5withRSA");
            signature.initSign(privateKey);//����˽Կ
            //ǩ���ͼ���һ�� Ҫ���ֽ���ʽ utf-8�ַ����õ��ֽ�
            signature.update(line.getBytes("UTF-8"));
            //�õ�base64�����ǩ������ֶ�
            sign = Base64.encodeToString(signature.sign(),Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sign;
    }
}
