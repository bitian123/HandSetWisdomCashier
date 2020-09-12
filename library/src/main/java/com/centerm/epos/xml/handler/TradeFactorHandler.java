package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.bean.TranscationFactor;
import com.centerm.epos.transcation.pos.constant.TranscationFactorTable;
import com.centerm.epos.xml.bean.project.ConfigItem;
import com.centerm.epos.xml.bean.project.ProjectConfig;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class TradeFactorHandler extends BaseHandler {

//    private TranscationFactorTable mTranscationFactorTable;
    private TranscationFactor mTranscationFactor;
    Map<String, TranscationFactor> mFactorMap;

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        String key = attributes.getValue(xmlAttrs.key);
        String tradeName = attributes.getValue(xmlAttrs.tradeName);
        String msgReqType = attributes.getValue(xmlAttrs.msgReqType);
        String msgRespType = attributes.getValue(xmlAttrs.msgRespType);
        String processCode = attributes.getValue(xmlAttrs.processCode);
        String servicePoint = attributes.getValue(xmlAttrs.servicePoint);
        String tradeCode = attributes.getValue(xmlAttrs.tradeCode);
        String tradeReverse = attributes.getValue(xmlAttrs.tradeReverse);

        if (xmlTag.factor.equals(localName)){
//            mTranscationFactorTable = new TranscationFactorTable();
            mFactorMap = new HashMap<>();
        }else if (xmlTag.Item.equals(localName)) {
            mTranscationFactor = new TranscationFactor();
        } else {
            throw new SAXException("The tag[" + localName + "] is illegal, must be " + xmlTag.factor + " or " + xmlTag
                    .Item);
        }
        if (tagStack.size() == 0) {
            //第一个节点，判断是否是menu节点
            if (!xmlTag.factor.equals(localName)) {
                throw new SAXException("The first tag is illegal, must be " + xmlTag.factor + ".");
            } else {
                //// TODO: 2017/6/22 do some init
            }
        }
        if (mTranscationFactor != null && mFactorMap != null) {
            mTranscationFactor.setTransName(tradeName);
            mTranscationFactor.setMessageTypeRequest(msgReqType);
            mTranscationFactor.setMessageTypeResponse(msgRespType);
            mTranscationFactor.setProcessCode(processCode);
            mTranscationFactor.setServicePoint(servicePoint);
            mTranscationFactor.setTradeCode(tradeCode);
            if (TextUtils.isEmpty(tradeReverse))
                mTranscationFactor.setReverse(false);
            else
                mTranscationFactor.setReverse(Boolean.parseBoolean(tradeReverse));

//            mTranscationFactorTable.putTradeFactor(key, mTranscationFactor);
            mFactorMap.put(key, mTranscationFactor);
            mTranscationFactor = null;
        }
        tagStack.push(localName);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        tagStack = null;
    }

    public Map<String, TranscationFactor> getTranscationFactorTable() {
        return mFactorMap;
    }
}
