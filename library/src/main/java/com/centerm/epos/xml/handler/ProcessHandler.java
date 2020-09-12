package com.centerm.epos.xml.handler;

import android.text.TextUtils;

import com.centerm.epos.annotation.AnnotationConstant;
import com.centerm.epos.xml.bean.process.ComponentNode;
import com.centerm.epos.xml.bean.process.Condition;
import com.centerm.epos.xml.bean.process.TradeProcess;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Map;


public class ProcessHandler extends BaseHandler {

    //xml文件中标签定义
    private final String TAG_PROCESS = "process";
    private final String TAG_COMPONENT = "component";
    private final String TAG_CONDITION = "condition";
    //属性名称定义
    private final String ATTR_TRANSCODE = "transCode";
    private final String ATTR_NAME = "name";
    private final String ATTR_PRESENT = "present";
    private final String ATTR_ID = "id";
    private final String ATTR_CONTROLLER = "controller";

    private StringBuilder sb = new StringBuilder();
    private TradeProcess transaction = null;
    private ComponentNode mComponentNode = null;
    private Condition mCondition = null;

    private Map<String,String> annotationViewMap;
    private Map<String,String> annotationControlleMap;
    private Map<String,String> annotationPresentMap;
    private Map<String,String> annotationModelMap;

    public TradeProcess getTransaction() {
        return transaction;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        sb.setLength(0);
        if (TAG_PROCESS.equals(localName)) {
            transaction = new TradeProcess();
            String transCode = attributes.getValue(ATTR_TRANSCODE);
            transaction.setTransCode(transCode);
        } else if (TAG_COMPONENT.equals(localName)) {
            mComponentNode = new ComponentNode();
            String nodeId = attributes.getValue(ATTR_ID);
            String nodeName = attributes.getValue(ATTR_NAME);
            String presentName = attributes.getValue(ATTR_PRESENT);
            String controller = attributes.getValue(ATTR_CONTROLLER);
            mComponentNode.setComponentId(nodeId);
            mComponentNode.setComponentName(nodeName);
            mComponentNode.setPresentName(presentName);
            mComponentNode.setController(controller);
            replaceFromAnnotation(mComponentNode);
        } else if (TAG_CONDITION.equals(localName)) {
            mCondition = new Condition();
            String contId = attributes.getValue(ATTR_ID);
            mCondition.setId(contId);
        }
    }

    /**
     * 设置注解配置的数据
     */
    public void setAnnotationConfigMap(Map<String, String> viewMap, Map<String, String> presentMap, Map<String,
            String> controlleMap, Map<String, String> modelMap){
        annotationViewMap = viewMap;
        annotationPresentMap = presentMap;
        annotationControlleMap = controlleMap;
        annotationModelMap = modelMap;
    }

    /**
     * 从注解中获取配置的处理类名，如果配置的类名是以"@"开头的则进行处理。对交易节点的UI类名，业务类名，控制器类名进行处理
     * @param mComponentNode    交易节点
     */
    private void replaceFromAnnotation(ComponentNode mComponentNode) {
        if (mComponentNode == null)
            return;
        String annoStrBuffer;
        String strBuffer = mComponentNode.getComponentName();
        if (!TextUtils.isEmpty(strBuffer) && strBuffer.startsWith(AnnotationConstant.XML_CONFIG_PREFIX)){
            //去除第一个标识符
            if (annotationViewMap != null && annotationViewMap.size() > 0) {
                annoStrBuffer = annotationViewMap.get(strBuffer.substring(1));
                if (!TextUtils.isEmpty(annoStrBuffer))
                    mComponentNode.setComponentName(annoStrBuffer);
            }
        }
        strBuffer = mComponentNode.getPresentName();
        if (!TextUtils.isEmpty(strBuffer) && strBuffer.startsWith(AnnotationConstant.XML_CONFIG_PREFIX)){
            //去除第一个标识符
            if (annotationPresentMap != null && annotationPresentMap.size() > 0) {
                annoStrBuffer = annotationPresentMap.get(strBuffer.substring(1));
                if (!TextUtils.isEmpty(annoStrBuffer))
                    mComponentNode.setPresentName(annoStrBuffer);
            }
        }
        strBuffer = mComponentNode.getController();
        if (!TextUtils.isEmpty(strBuffer) && strBuffer.startsWith(AnnotationConstant.XML_CONFIG_PREFIX)){
            //去除第一个标识符
            if (annotationControlleMap != null && annotationControlleMap.size() > 0) {
                annoStrBuffer = annotationControlleMap.get(strBuffer.substring(1));
                if (!TextUtils.isEmpty(annoStrBuffer))
                    mComponentNode.setController(annoStrBuffer);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        super.characters(ch, start, length);
        sb.append(ch, start, length);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
        String value = sb.toString();
        if (TAG_COMPONENT.equals(localName)) {
            transaction.getComponentNodeList().add(mComponentNode);
        } else if (TAG_CONDITION.equals(localName)) {
            mCondition.setNextComponentNodeId(value);
            mComponentNode.getIdMapCondition().put(mCondition.getId(), mCondition);
        }
    }

}
