package com.centerm.epos.xml.bean.process;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class TradeProcess implements Parcelable {
    private Logger logger = Logger.getLogger(TradeProcess.class);
    private String transCode;                                                     //当前交易码
    private ComponentNode curNode;                                                //当前所处的交易节点
    private List<String> lastNodeIds = new ArrayList<>();    //当前节点所对应的上一个节点，用于流程回退
    private LinkedList<ComponentNode> componentNodeList = new LinkedList<>();     //当前交易涉及的所有节点集
    private Map<String, String> dataMap = new HashMap<>();                        //交易流程中存储必要数据，用于组包或者持久化保存
    private Map<String, String> tempMap = new HashMap<>();                        //交易流程中存储临时数据，用于逻辑判断
    private Map<String, Object> transDatas = new HashMap<>();                     //业务数据

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public List<ComponentNode> getComponentNodeList() {
        return componentNodeList;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public Map<String, String> getTempMap() {
        return tempMap;
    }

    public Map<String, Object> getTransDatas() {
        return transDatas;
    }

    public void setTransDatas(Map<String, Object> transDatas) {
        this.transDatas = transDatas;
    }

    public void setTempMap(Map<String, String> tempMap) {
        this.tempMap = tempMap;
    }

    public ComponentNode getFirstComponentNode() {
        return componentNodeList.get(0);
    }

    public ComponentNode getCurNode() {
        return curNode;
    }

    public void setCurNode(ComponentNode curNode, boolean isForward) {
        if (isForward && this.curNode != null)
            this.lastNodeIds.add(this.curNode.getComponentId());
        this.curNode = curNode;
    }

    public ComponentNode getLastNode() {
        if (lastNodeIds.size() == 0)
            return null;
        String lastNodeId = lastNodeIds.get(lastNodeIds.size()-1);
        lastNodeIds.remove(lastNodeIds.size()-1);
        for (int i = 0; i < componentNodeList.size(); i++) {
            ComponentNode node = componentNodeList.get(i);
            if (node.getComponentId().equals(lastNodeId)) {
                return node;
            }
        }
        return null;
    }

    /**
     * 获取下一个节点对象。如果当前节点对象为空，则默认返回第一个节点；如果当前节点已经是最后一个，则返回null
     *
     * @param conditionId 条件ID
     * @return 节点对象
     */
    public ComponentNode getNextComponentNode(String conditionId) {
        if (curNode == null) {
            if (componentNodeList.isEmpty()) {
                return null;
            }
            ComponentNode node = componentNodeList.getFirst();
            logger.warn("当前节点为空，返回第一个节点, [" + node.getComponentName() + "]");
            return node;
        }
        Condition condition = curNode.getIdMapCondition().get(conditionId);
        if (condition == null)
            return null;
        String nextId = condition.getNextComponentNodeId();
        for (int i = 0; i < componentNodeList.size(); i++) {
            ComponentNode node = componentNodeList.get(i);
            if (node.getComponentId().equals(nextId)) {
                return node;
            }
        }
        return null;
    }

    public ComponentNode getNextComponentNode(ComponentNode currentNode, String conditionId) {
        curNode = currentNode;
        return getNextComponentNode(conditionId);
    }

    public static final Creator<TradeProcess> CREATOR = new Creator<TradeProcess>() {

        @SuppressWarnings("unchecked")
        @Override
        public TradeProcess createFromParcel(Parcel source) {
            TradeProcess mTransaction = new TradeProcess();
            mTransaction.transCode = source.readString();
            source.readStringList(mTransaction.lastNodeIds);
            mTransaction.dataMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
            mTransaction.tempMap = (Map<String, String>) source.readHashMap(getClass().getClassLoader());
            mTransaction.transDatas = (Map<String, Object>) source.readHashMap(getClass().getClassLoader());
            source.readList(mTransaction.componentNodeList, getClass().getClassLoader());
            return mTransaction;
        }

        @Override
        public TradeProcess[] newArray(int size) {
            return new TradeProcess[size];
        }

    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(transCode);
        dest.writeStringList(lastNodeIds);
        dest.writeMap(dataMap);
        dest.writeMap(tempMap);
        dest.writeMap(transDatas);
        dest.writeList(componentNodeList);
    }

    @Override
    public String toString() {
        return "TradeProcess{" +
                "componentNodeList=" + componentNodeList +
                '}';
    }
}
