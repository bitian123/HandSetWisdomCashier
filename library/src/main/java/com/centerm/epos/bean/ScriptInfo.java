package com.centerm.epos.bean;

import com.centerm.epos.common.TransDataKey;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ic卡脚本
 */

@DatabaseTable(tableName = "tb_scriptinfo_serial")
public class ScriptInfo implements Serializable {

    @DatabaseField
    private String transCode;//交易类型
    @DatabaseField
    private String iso_f2;//主账号
    @DatabaseField
    private String iso_f3;//交易处理码
    @DatabaseField
    private String iso_f4;//交易金额
    @DatabaseField(id = true)//作为主键
    private String iso_f11;//受卡方系统跟踪号（终端流水号）

    @DatabaseField
    private String iso_f22;//服务点输入方式码
    @DatabaseField
    private String iso_f23;//卡序列号

    @DatabaseField
    private String iso_f25;//服务点条件码
    @DatabaseField
    private String iso_f26;//服务点PIN获取码

    @DatabaseField
    private String iso_f32;//受理方标志码

    @DatabaseField
    private String iso_f37;//检索参考号
    @DatabaseField
    private String iso_f38;//授权码

    @DatabaseField
    private String iso_f41;//受卡机终端标识码
    @DatabaseField
    private String iso_f42;//受卡方标识码

    @DatabaseField
    private String iso_f49;//交易货币代码

    @DatabaseField
    private String iso_f55;//IC卡数据域

    @DatabaseField
    private String iso_f60;//自定义域（交易类型码+网络管理码+终端读取能力+IC卡条件代码）
    @DatabaseField
    private String iso_f61;//
    @DatabaseField
    private String iso_f64;//
    @DatabaseField(defaultValue = "0")
    private int sendCount;//上送次数   最多3次   99说明平台返回该笔订单失败
    @DatabaseField
    private int scriptResult;//脚本执行结果，0-无脚本（非成功交易、非IC卡交易、非消费、预授权交易均无脚本）；1-脚本执行成功；2-脚本执行失败；3-脚本上送完成


    public ScriptInfo() {
    }

    /**
     * 构造函数。
     * 对于该应用框架而言，该构造函数用于交易请求时，通过将交易码和交易参数传入，构建原始的数据模型存入数据库；
     * 待返回报文到达时，通过{@link #update(Map)}方法，来更新此数据模型，同时更新到数据库；以此来保证一条记录能完整的包含请求参数和返回参数。
     *
     * @param transCode 自定义的交易码
     * @param dataMap   请求参数集合
     */
    public ScriptInfo(String transCode, Map<String, String> dataMap) {
        this.transCode = transCode;
        initValue(dataMap);
    }

    private void initValue(Map<String, String> mapData) {
        iso_f2 = mapData.get(TransDataKey.iso_f2);
        iso_f3 = mapData.get(TransDataKey.iso_f3);
        iso_f4 = mapData.get(TransDataKey.iso_f4);
        iso_f11 = mapData.get(TransDataKey.iso_f11);
        iso_f22 = mapData.get(TransDataKey.iso_f22);
        iso_f23 = mapData.get(TransDataKey.iso_f23);

        iso_f25 = mapData.get(TransDataKey.iso_f25);
        iso_f26 = mapData.get(TransDataKey.iso_f26);

        iso_f32 = mapData.get(TransDataKey.iso_f32);

        iso_f37 = mapData.get(TransDataKey.iso_f37);
        iso_f38 = mapData.get(TransDataKey.iso_f38);

        iso_f41 = mapData.get(TransDataKey.iso_f41);
        iso_f42 = mapData.get(TransDataKey.iso_f42);

        iso_f49 = mapData.get(TransDataKey.iso_f49);

        iso_f55 = mapData.get(TransDataKey.iso_f55);

        iso_f60 = mapData.get(TransDataKey.iso_f60);
        iso_f61 = mapData.get(TransDataKey.iso_f61);
        iso_f64 = mapData.get(TransDataKey.iso_f64);

        String r = mapData.get(TransDataKey.KEY_IC_SCRIPT_RESULT);
        if ("true".equals(r)) {
            scriptResult = 1;
        } else if ("false".equals(r)) {
            scriptResult = 2;
        }

    }


    /**
     * 数据更新
     *
     * @param returnData 平台返回参数集合
     * @return 更新后的交易流水，可以直接用于更新数据库
     */
    public ScriptInfo update(Map<String, String> returnData) {
        Map<String, String> map = convert2Map();
        map.putAll(returnData);
        initValue(map);
        return this;
    }

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }


    public int getScriptResult() {
        return scriptResult;
    }

    public void setScriptResult(int scriptResult) {
        this.scriptResult = scriptResult;
    }

    /**
     * 获取批次号，批次号存在于60.2域中
     */
    public String getBatchNo() {
        if (iso_f60 != null && iso_f60.length() >= 8) {
            return iso_f60.substring(2, 8);
        }
        return null;
    }

    //**********************************************//
    public String getIso_f2() {
        return iso_f2;
    }

    public void setIso_f2(String iso_f2) {
        this.iso_f2 = iso_f2;
    }

    public String getIso_f3() {
        return iso_f3;
    }

    public void setIso_f3(String iso_f3) {
        this.iso_f3 = iso_f3;
    }

    public String getIso_f4() {
        return iso_f4;
    }

    public void setIso_f4(String iso_f4) {
        this.iso_f4 = iso_f4;
    }

    public String getIso_f11() {
        return iso_f11;
    }

    public void setIso_f11(String iso_f11) {
        this.iso_f11 = iso_f11;
    }

    public String getIso_f22() {
        return iso_f22;
    }

    public void setIso_f22(String iso_f22) {
        this.iso_f22 = iso_f22;
    }

    public String getIso_f23() {
        return iso_f23;
    }

    public void setIso_f23(String iso_f23) {
        this.iso_f23 = iso_f23;
    }

    public String getIso_f25() {
        return iso_f25;
    }

    public void setIso_f25(String iso_f25) {
        this.iso_f25 = iso_f25;
    }

    public String getIso_f26() {
        return iso_f26;
    }

    public void setIso_f26(String iso_f26) {
        this.iso_f26 = iso_f26;
    }

    public String getIso_f41() {
        return iso_f41;
    }

    public void setIso_f41(String iso_f41) {
        this.iso_f41 = iso_f41;
    }

    public String getIso_f42() {
        return iso_f42;
    }

    public void setIso_f42(String iso_f42) {
        this.iso_f42 = iso_f42;
    }

    public String getIso_f49() {
        return iso_f49;
    }

    public void setIso_f49(String iso_f49) {
        this.iso_f49 = iso_f49;
    }


    public String getIso_f55() {
        return iso_f55;
    }

    public void setIso_f55(String iso_f55) {
        this.iso_f55 = iso_f55;
    }

    public String getIso_f60() {
        return iso_f60;
    }

    public void setIso_f60(String iso_f60) {
        this.iso_f60 = iso_f60;
    }

    public String getIso_f32() {
        return iso_f32;
    }

    public void setIso_f32(String iso_f32) {
        this.iso_f32 = iso_f32;
    }

    public String getIso_f37() {
        return iso_f37;
    }

    public void setIso_f37(String iso_f37) {
        this.iso_f37 = iso_f37;
    }

    public String getIso_f38() {
        return iso_f38;
    }

    public void setIso_f38(String iso_f38) {
        this.iso_f38 = iso_f38;
    }

    public String getIso_f61() {
        return iso_f61;
    }

    public void setIso_f61(String iso_f61) {
        this.iso_f61 = iso_f61;
    }

    public int getSendCount() {
        return sendCount;
    }

    public void setSendCount(int sendCount) {
        this.sendCount = sendCount;
    }

    public Map<String, String> convert2Map() {
        Map<String, String> map = new HashMap<>();
        map.put(TransDataKey.iso_f2, iso_f2);
        map.put(TransDataKey.iso_f3, iso_f3);
        map.put(TransDataKey.iso_f4, iso_f4);

        map.put(TransDataKey.iso_f11, iso_f11);

        map.put(TransDataKey.iso_f22, iso_f22);
        map.put(TransDataKey.iso_f23, iso_f23);

        map.put(TransDataKey.iso_f25, iso_f25);
        map.put(TransDataKey.iso_f26, iso_f26);

        map.put(TransDataKey.iso_f32, iso_f32);

        map.put(TransDataKey.iso_f37, iso_f37);
        map.put(TransDataKey.iso_f38, iso_f38);

        map.put(TransDataKey.iso_f41, iso_f41);
        map.put(TransDataKey.iso_f42, iso_f42);

        map.put(TransDataKey.iso_f49, iso_f49);

        map.put(TransDataKey.iso_f55, iso_f55);

        map.put(TransDataKey.iso_f60, iso_f60);
        map.put(TransDataKey.iso_f61, iso_f61);
        map.put(TransDataKey.iso_f64, iso_f64);
        return map;
    }

    @Override
    public String toString() {
        return "TradeInfo{" +
                "transCode='" + transCode + '\'' +
                ", iso_f2='" + iso_f2 + '\'' +
                ", iso_f3='" + iso_f3 + '\'' +
                ", iso_f4='" + iso_f4 + '\'' +
                ", iso_f11='" + iso_f11 + '\'' +
                ", iso_f22='" + iso_f22 + '\'' +
                ", iso_f23='" + iso_f23 + '\'' +
                ", iso_f25='" + iso_f25 + '\'' +
                ", iso_f26='" + iso_f26 + '\'' +
                ", iso_f32='" + iso_f32 + '\'' +
                ", iso_f37='" + iso_f37 + '\'' +
                ", iso_f38='" + iso_f38 + '\'' +
                ", iso_f41='" + iso_f41 + '\'' +
                ", iso_f42='" + iso_f42 + '\'' +
                ", iso_f49='" + iso_f49 + '\'' +
                ", iso_f55='" + iso_f55 + '\'' +
                ", iso_f60='" + iso_f60 + '\'' +
                ", iso_f61='" + iso_f61 + '\'' +
                '}';
    }
}
