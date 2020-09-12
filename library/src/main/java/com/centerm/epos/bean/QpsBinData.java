package com.centerm.epos.bean;

import android.text.TextUtils;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

/**
 * author:wanliang527</br>
 * date:2017/1/17</br>
 */

@DatabaseTable(tableName = "tb_qps_bin")
public class QpsBinData {

    @DatabaseField(generatedId = true)
    private int id;
    @DatabaseField
    private String type;//A或B
    @DatabaseField
    private String cardBin;//卡BIN
    @DatabaseField
    private int cardLen;//卡号长度

    public QpsBinData() {
    }

    public QpsBinData(String type, String cardBin, int cardLen) {
        this.type = type;
        this.cardBin = cardBin;
        this.cardLen = cardLen;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCardBin() {
        return cardBin;
    }

    public void setCardBin(String cardBin) {
        this.cardBin = cardBin;
    }

    public int getCardLen() {
        return cardLen;
    }

    public void setCardLen(int cardLen) {
        this.cardLen = cardLen;
    }

    public static List<QpsBinData> parse(String iso62) {
        List<QpsBinData> binList = new ArrayList<>();
        if (TextUtils.isEmpty(iso62)) {
            return binList;
        }
        int index = 4;
        while (index < iso62.length()) {
            QpsBinData data = new QpsBinData();
            data.type = "B";
            try {
                data.cardLen = Integer.parseInt(iso62.substring(index, index + 2).trim());
                index += 2;
                data.cardBin = iso62.substring(index, index + 6).trim();
                index += 6;
                binList.add(data);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
        return binList;
    }

    @Override
    public String toString() {
        return "QpsBinData{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", cardBin='" + cardBin + '\'' +
                ", cardLen=" + cardLen +
                '}';
    }
}
