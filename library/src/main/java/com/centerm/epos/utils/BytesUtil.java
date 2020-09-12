package com.centerm.epos.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * 字节数组转换工具类 作者：刘志祥 时间：2012.09.01
 */
public class BytesUtil {

	public static final String GBK = "GBK";
	public static final String UTF8 = "utf-8";
	public static final char[] ascii = "0123456789ABCDEF".toCharArray();

	/**
	 * 将short整型数值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(short data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) ((data & 0xff00) >> 8);
		bytes[1] = (byte) (data & 0xff);
		return bytes;
	}

	/**
	 * 将字符转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(char data) {
		byte[] bytes = new byte[2];
		bytes[0] = (byte) (data >> 8);
		bytes[1] = (byte) (data);
		return bytes;
	}

	/**
	 * 将布尔值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(boolean data) {
		byte[] bytes = new byte[1];
		bytes[0] = (byte) (data ? 1 : 0);
		return bytes;
	}

	/**
	 * 将整型数值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(int data) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) ((data & 0xff000000) >> 24);
		bytes[1] = (byte) ((data & 0xff0000) >> 16);
		bytes[2] = (byte) ((data & 0xff00) >> 8);
		bytes[3] = (byte) (data & 0xff);
		return bytes;
	}

	/**
	 * 将long整型数值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(long data) {
		byte[] bytes = new byte[8];
		bytes[0] = (byte) ((data >> 56) & 0xff);
		bytes[1] = (byte) ((data >> 48) & 0xff);
		bytes[2] = (byte) ((data >> 40) & 0xff);
		bytes[3] = (byte) ((data >> 32) & 0xff);
		bytes[4] = (byte) ((data >> 24) & 0xff);
		bytes[5] = (byte) ((data >> 16) & 0xff);
		bytes[6] = (byte) ((data >> 8) & 0xff);
		bytes[7] = (byte) (data & 0xff);
		return bytes;
	}

	/**
	 * 将float型数值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(float data) {
		int intBits = Float.floatToIntBits(data);
		return getBytes(intBits);
	}

	/**
	 * 将double型数值转换为字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(double data) {
		long intBits = Double.doubleToLongBits(data);
		return getBytes(intBits);
	}

	/**
	 * 将字符串按照charsetName编码格式的字节数组
	 * 
	 * @param data
	 *            字符串
	 * @param charsetName
	 *            编码格式
	 * @return
	 */
	public static byte[] getBytes(String data, String charsetName) {
		Charset charset = Charset.forName(charsetName);
		return data.getBytes(charset);
	}

	/**
	 * 将字符串按照GBK编码格式的字节数组
	 * 
	 * @param data
	 * @return
	 */
	public static byte[] getBytes(String data) {
		return getBytes(data, GBK);
	}

	/**
	 * 将字节数组第0字节转换为布尔值
	 * 
	 * @param bytes
	 * @return
	 */
	public static boolean getBoolean(byte[] bytes) {
		return bytes[0] == 1;
	}

	/**
	 * 将字节数组的第index字节转换为布尔值
	 * 
	 * @param bytes
	 * @param index
	 * @return
	 */
	public static boolean getBoolean(byte[] bytes, int index) {
		return bytes[index] == 1;
	}

	/**
	 * 将字节数组前2字节转换为short整型数值
	 * 
	 * @param bytes
	 * @return
	 */
	public static short getShort(byte[] bytes) {
		return (short) ((0xff00 & (bytes[0] << 8)) | (0xff & bytes[1]));
	}

	/**
	 * 将字节数组从startIndex开始的2个字节转换为short整型数值
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static short getShort(byte[] bytes, int startIndex) {
		return (short) ((0xff00 & (bytes[startIndex] << 8)) | (0xff & bytes[startIndex + 1]));
	}

	/**
	 * 将字节数组前2字节转换为字符
	 * 
	 * @param bytes
	 * @return
	 */
	public static char getChar(byte[] bytes) {
		return (char) ((0xff00 & (bytes[0] << 8)) | (0xff & bytes[1]));
	}

	/**
	 * 将字节数组从startIndex开始的2个字节转换为字符
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static char getChar(byte[] bytes, int startIndex) {
		return (char) ((0xff00 & (bytes[startIndex] << 8)) | (0xff & bytes[startIndex + 1]));
	}

	/**
	 * 将两个字节转换成整型数值
	 */
	public static int getIntWithTwoByte(byte[] bytes) {
		return (bytes[0] & 0xff) * 256 + (bytes[1] & 0xff);
	}
	/**
	 * 将字节数组前4字节转换为整型数值
	 * 
	 * @param bytes
	 * @return
	 */
	public static int getInt(byte[] bytes) {
		return (0xff000000 & (bytes[0] << 24) | (0xff0000 & (bytes[1] << 16))
				| (0xff00 & (bytes[2] << 8)) | (0xff & bytes[3]));
	}

	/**
	 * 将字节数组从startIndex开始的4个字节转换为整型数值
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static int getInt(byte[] bytes, int startIndex) {
		return (0xff000000 & (bytes[startIndex] << 24)
				| (0xff0000 & (bytes[startIndex + 1] << 16))
				| (0xff00 & (bytes[startIndex + 2] << 8)) | (0xff & bytes[startIndex + 3]));
	}

	/**
	 * 4个字节数组转换为整形
	 * 
	 * @param b
	 * @return
	 */
	public static int byteToInt(byte[] b) {
		return ((((b[0] & 0xff) << 8 | (b[1] & 0xff)) << 8) | (b[2] & 0xff)) << 8
				| (b[3] & 0xff);
	}

	/**
	 * 将字节数组前8字节转换为long整型数值
	 * 
	 * @param bytes
	 * @return
	 */
	public static long getLong(byte[] bytes) {
		return (0xff00000000000000L & ((long) bytes[0] << 56)
				| (0xff000000000000L & ((long) bytes[1] << 48))
				| (0xff0000000000L & ((long) bytes[2] << 40))
				| (0xff00000000L & ((long) bytes[3] << 32))
				| (0xff000000L & ((long) bytes[4] << 24))
				| (0xff0000L & ((long) bytes[5] << 16))
				| (0xff00L & ((long) bytes[6] << 8)) | (0xffL & (long) bytes[7]));
	}

	/**
	 * 将字节数组从startIndex开始的8个字节转换为long整型数值
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static long getLong(byte[] bytes, int startIndex) {
		return (0xff00000000000000L & ((long) bytes[startIndex] << 56)
				| (0xff000000000000L & ((long) bytes[startIndex + 1] << 48))
				| (0xff0000000000L & ((long) bytes[startIndex + 2] << 40))
				| (0xff00000000L & ((long) bytes[startIndex + 3] << 32))
				| (0xff000000L & ((long) bytes[startIndex + 4] << 24))
				| (0xff0000L & ((long) bytes[startIndex + 5] << 16))
				| (0xff00L & ((long) bytes[startIndex + 6] << 8)) | (0xffL & (long) bytes[startIndex + 7]));
	}

	/**
	 * 将字节数组前4字节转换为float型数值
	 * 
	 * @param bytes
	 * @return
	 */
	public static float getFloat(byte[] bytes) {
		return Float.intBitsToFloat(getInt(bytes));
	}

	/**
	 * 将字节数组从startIndex开始的4个字节转换为float型数值
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static float getFloat(byte[] bytes, int startIndex) {
		byte[] result = new byte[4];
		System.arraycopy(bytes, startIndex, result, 0, 4);
		return Float.intBitsToFloat(getInt(result));
	}

	/**
	 * 将字节数组前8字节转换为double型数值
	 * 
	 * @param bytes
	 * @return
	 */
	public static double getDouble(byte[] bytes) {
		long l = getLong(bytes);
		return Double.longBitsToDouble(l);
	}

	/**
	 * 将字节数组从startIndex开始的8个字节转换为double型数值
	 * 
	 * @param bytes
	 * @param startIndex
	 * @return
	 */
	public static double getDouble(byte[] bytes, int startIndex) {
		byte[] result = new byte[8];
		System.arraycopy(bytes, startIndex, result, 0, 8);
		long l = getLong(result);
		return Double.longBitsToDouble(l);
	}

	/**
	 * 将charsetName编码格式的字节数组转换为字符串
	 * 
	 * @param bytes
	 * @param charsetName
	 * @return
	 */
	public static String getString(byte[] bytes, String charsetName) {
		return new String(bytes, Charset.forName(charsetName));
	}

	/**
	 * 将GBK编码格式的字节数组转换为字符串
	 * 
	 * @param bytes
	 * @return
	 */
	public static String getString(byte[] bytes) {
		return getString(bytes, GBK);
	}
	
	/**
	 * 将16进制字符串转换为字节数组00000D00->0 0 13 0
	 * 
	 * @param hex
	 * @return
	 */
	public static byte[] hexStringToBytes(String hex) {
		if (hex == null || "".equals(hex)) {
			return null;
		}
		int len = hex.length() / 2;
		byte[] result = new byte[len];
		char[] chArr = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = i * 2;
			result[i] = (byte) (toByte(chArr[pos]) << 4 | toByte(chArr[pos + 1]));
		}
		return result;
	}

	/**
	 * 将BCD编码的字节数组转换为字符串
	 * 
	 * @param bcds
	 * @return
	 */
	public static String bcdToString(byte[] bcds) {
		if (bcds == null || bcds.length == 0) {
			return null;
		}
		byte[] temp = new byte[2 * bcds.length];
		for (int i = 0; i < bcds.length; i++) {
			temp[i * 2] = (byte) ((bcds[i] >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (bcds[i] & 0x0f);
		}
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString();
	}

	/**
	 * 将字节数组取反
	 * 
	 * @param src
	 * @return
	 */
	public static String negate(byte[] src) {
		if (src == null || src.length == 0) {
			return null;
		}
		byte[] temp = new byte[2 * src.length];
		for (int i = 0; i < src.length; i++) {
			byte tmp = (byte) (0xFF ^ src[i]);
			temp[i * 2] = (byte) ((tmp >> 4) & 0x0f);
			temp[i * 2 + 1] = (byte) (tmp & 0x0f);
		}
		StringBuffer res = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			res.append(ascii[temp[i]]);
		}
		return res.toString();
	}

	/**
	 * 比较字节数组是否相同
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean compareBytes(byte[] a, byte[] b) {
		if (a == null || a.length == 0 || b == null || b.length == 0
				|| a.length != b.length) {
			return false;
		}
		if (a.length == b.length) {
			for (int i = 0; i < a.length; i++) {
				if (a[i] != b[i]) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * 将字节数组转换为二进制字符串
	 * 
	 * @param items
	 * @return
	 */
	public static String bytesToBinaryString(byte[] items) {
		if (items == null || items.length == 0) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		for (byte item : items) {
			buf.append(byteToBinaryString(item));
		}
		return buf.toString();
	}

	/**
	 * 将字节转换为二进制字符串
	 * 
	 * @param item
	 * @return
	 */
	public static String byteToBinaryString(byte item) {
		byte a = item;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < 8; i++) {
			buf.insert(0, a % 2);
			a = (byte) (a >> 1);
		}
		return buf.toString();
	}

	/**
	 * 对数组a，b进行异或运算
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] xor(byte[] a, byte[] b) {
		if (a == null || a.length == 0 || b == null || b.length == 0
				|| a.length != b.length) {
			return null;
		}
		byte[] result = new byte[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = (byte) (a[i] ^ b[i]);
		}
		return result;
	}

	/**
	 * 将short整型数值转换为字节数组(返回两个字节的数组) 可以用 【1】获得一位的
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] shortToBytes(int num) {
		byte[] temp = new byte[2];
		for (int i = 0; i < 2; i++) {
			temp[i] = (byte) ((num >>> (8 - i * 8)) & 0xFF);
		}
		return temp;
	}

	public static int bytesToShort(byte[] arr) {
		int mask = 0xFF;
		int temp = 0;
		int result = 0;
		for (int i = 0; i < 2; i++) {
			result <<= 8;
			temp = arr[i] & mask;
			result |= temp;
		}
		return result;
	}

	/**
	 * 将一个单位的字节数组转换成int型
	 * 
	 * @param arr
	 * @return
	 */
	public static int parse(byte[] arr) {
		char c = (char) arr[0];
		if (c >= 'a')
			return (c - 'a' + 10) & 0x0f;
		if (c >= 'A')
			return (c - 'A' + 10) & 0x0f;
		return (c - '0') & 0x0f;
	}

	/**
	 * 将整型数值转换为指定长度的字节数组
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] intToBytes(int num) {
		byte[] temp = new byte[4];
		for (int i = 0; i < 4; i++) {
			temp[i] = (byte) ((num >>> (24 - i * 8)) & 0xFF);
		}
		return temp;
	}

	/**
	 * 将整型数值转换为指定长度的字节数组
	 * 
	 * @param src
	 * @param len
	 * @return
	 */
	public static byte[] intToBytes(int src, int len) {
		if (len < 1 || len > 4) {
			return null;
		}
		byte[] temp = new byte[len];
		for (int i = 0; i < len; i++) {
			temp[len - 1 - i] = (byte) ((src >>> (8 * i)) & 0xFF);
		}
		return temp;
	}

	/**
	 * 将字节数组转换为整型数值
	 * 
	 * @param arr
	 * @return
	 */
	public static int bytesToInt(byte[] arr) {
		int mask = 0xFF;
		int temp = 0;
		int result = 0;
		for (int i = 0; i < 4; i++) {
			result <<= 8;
			temp = arr[i] & mask;
			result |= temp;
		}
		return result;
	}
	
	/**
	 * 将整型数值转换为BYTE
	 * 
	 * @param src
	 * @return
	 */
	public static int intToByte(int src) {
		
		return BytesUtil.shortToBytes(src)[1];
	}

	/**
	 * 将long整型数值转换为字节数组
	 * 
	 * @param num
	 * @return
	 */
	public static byte[] longToBytes(long num) {
		byte[] temp = new byte[8];
		for (int i = 0; i < 8; i++) {
			temp[i] = (byte) ((num >>> (56 - i * 8)) & 0xFF);
		}
		return temp;
	}

	/**
	 * 将字节数组转换为long整型数值
	 * 
	 * @param arr
	 * @return
	 */
	public static long bytesToLong(byte[] arr) {
		int mask = 0xFF;
		int temp = 0;
		long result = 0;
		for (int i = 0; i < 8; i++) {
			result <<= 8;
			temp = arr[i] & mask;
			result |= temp;
		}
		return result;
	}

	/**
	 * 将16进制字符转换为字节
	 * 
	 * @param c
	 * @return
	 */
	public static byte toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		return b;
	}

	public static String bcd2Str(byte[] bytes)
	{
		StringBuilder temp = new StringBuilder(bytes.length * 2);

		for (int i = 0; i < bytes.length; i++)
		{
			temp.append((byte)((bytes[i] & 0xf0) >> 4));
			temp.append((byte)(bytes[i] & 0x0f));
		}
		return temp.toString().substring(0, 1).equals("0") ? temp.toString().substring(1) : temp.toString();
	}

	public static byte[] str2Bcd(String asc) {
		int len = asc.length();
		int mod = len % 2;
		if (mod != 0) {
			asc = "0" + asc;
			len = asc.length();
		}
		byte abt[] = new byte[len];
		if (len >= 2) {
			len = len / 2;
		}
		byte bbt[] = new byte[len];
		abt = asc.getBytes();
		int j, k;
		for (int p = 0; p < asc.length() / 2; p++) {
			if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
				j = abt[2 * p] - '0';
			} else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
				j = abt[2 * p] - 'a' + 0x0a;
			} else {
				j = abt[2 * p] - 'A' + 0x0a;
			}
			if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
				k = abt[2 * p + 1] - '0';
			} else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
				k = abt[2 * p + 1] - 'a' + 0x0a;
			} else {
				k = abt[2 * p + 1] - 'A' + 0x0a;
			}
			int a = (j << 4) + k;
			byte b = (byte) a;
			bbt[p] = b;
		}
		return bbt;
	}

	public static String hex2Str(String str) throws UnsupportedEncodingException {
		String strArr[] = str.split("\\\\"); // 分割拿到形如 xE9 的16进制数据
		byte[] byteArr = new byte[strArr.length - 1];
		for (int i = 1; i < strArr.length; i++) {
			Integer hexInt = Integer.decode("0" + strArr[i]);
			byteArr[i - 1] = hexInt.byteValue();
		}
		return new String(byteArr, "UTF-8");
	}

	public static String bytesToHexString(byte[] src){
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

}
