// IVirtualPinPad.aidl
package com.centerm.cpay.securitysuite.aidl;

/**
 * author: linwanliang</br>
 * date:2016/6/30</br>
 */
interface IVirtualPinPad {

    /**
     * 发散主密钥。
     *
     * @param keyValue   密钥值，长度为16的字节数组转成16进制字符串
     * @param checkValue 校验值，长度为4的字节数组转成16进制字符串，传入null表示不进行校验
     * @return 发散成功返回true，发散失败返回false
     */
    boolean loadMainKey(String keyValue, String checkValue);

    /**
     * 发散工作密钥
     *
     * @param index      密钥索引值
     * @param keyValue   密钥值
     * @param checkValue 校验值
     * @return 发散成功返回true，发散失败返回false
     */
    boolean loadWorkKey(int index, String keyValue, String checkValue);

    /**
     * 计算MAC
     *
     * @param keyIndex 工作密钥索引值
     * @param message  报文
     * @return mac值
     */
    String calculateMac(int keyIndex, String message);

    /**
     * 计算MAC
     *
     * @param keyIndex 工作密钥索引值
     * @param message  报文
     * @return mac值
     */
    byte[] calculateMac2(int keyIndex,in byte[] message);


    /**
     * 判断密钥是否存在
     *
     * @param keyIndex 工作密钥索引值
     * @return 存在返回true，失败返回false
     */
    boolean isKeyExist(int keyIndex);


}