package com.centerm.epos.xml.bean.menu;

import android.os.Parcel;

import com.centerm.epos.base.BaseFragmentActivity;

import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;

/**
 * 子菜单实体类
 * author:wanliang527</br>
 * date:2016/10/25</br>
 */

public class Menu extends MenuItem {

    private ViewStructure structure = ViewStructure.LIST;
    private TopViewType topType = TopViewType.TITLE;
    private List<MenuItem> itemList = new ArrayList<>();

    public Menu() {
    }


    public Menu(String iconName, String textName) {
        super(iconName, textName);
    }

    public void add(MenuItem item) {
        itemList.add(item);
    }

    public ViewStructure getStructure() {
        return structure;
    }

    public void setStructure(ViewStructure structure) {
        this.structure = structure;
    }

    public TopViewType getTopType() {
        return topType;
    }

    public void setTopType(TopViewType topType) {
        this.topType = topType;
    }

    public int getCounts() {
        return itemList.size();
    }

    public MenuItem getItem(int positon) {
        if (positon >= getCounts()) {
            return null;
        }
        return itemList.get(positon);
    }

    public List<MenuItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<MenuItem> itemList) {
        this.itemList = itemList;
    }

    public void removeItem(String tag) {
        for (int i = 0; i < itemList.size(); i++) {
            MenuItem item = itemList.get(i);
            if (item.getEnTag().equals(tag)) {
                itemList.remove(i);
                break;
            }
        }
    }

    public Menu filteHideItems(){
        List<MenuItem> filteList = new ArrayList<>();
        BusinessConfig config = BusinessConfig.getInstance();
        for (MenuItem item: itemList){
            if (item.isShow() && config.isTradeOpened(item.getEnTag()))
                filteList.add(item);
        }
        itemList = filteList;
        filteList = new ArrayList<>();

        return this;
    }

    public MenuItem findItem(String tag) {
        for (int i = 0; i < itemList.size(); i++) {
            MenuItem item = itemList.get(i);
            if (item.getEnTag().equals(tag)) {
                return itemList.get(i);
            }
        }
        return null;
    }

    public static final Creator<Menu> CREATOR = new Creator<Menu>() {
        @Override
        public Menu createFromParcel(Parcel source) {
            Menu menu = new Menu();
            menu.enTag = source.readString();
            menu.chnTag = source.readString();
            menu.iconResName = source.readString();
            menu.textResName = source.readString();
            menu.processFile = source.readString();
            menu.transCode = source.readString();
            menu.isShow = source.readByte() == 1;
            menu.viewStyle = Style.valueOf(source.readString());
            menu.hasParent = source.readByte() == 1;
            menu.structure = ViewStructure.valueOf(source.readString());
            menu.topType = TopViewType.valueOf(source.readString());
            source.readList(menu.itemList, getClass().getClassLoader());
            return menu;
        }

        @Override
        public Menu[] newArray(int size) {
            return new Menu[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(structure.name());
        dest.writeString(topType.name());
        dest.writeList(itemList);
    }

    /**
     * 视图结构
     */
    public enum ViewStructure {
        //九宫格
        GRID,
        //列表
        LIST
    }

    /**
     * 菜单顶部视图类型
     */
    public enum TopViewType {
        TITLE,//标题栏
        BANNER,//横幅
        MIX,//混合
        NONE//无
    }

    @Override
    public String toString() {
        return "Menu{" +
                "\nstructure='" + structure + '\'' +
                "\nenTag='" + enTag + '\'' +
                "\nchnTag='" + chnTag + '\'' +
                "\niconResName='" + iconResName + '\'' +
                "\ntextResName='" + textResName + '\'' +
                "\nprocessFile='" + processFile + '\'' +
                "\ntransCode='" + transCode + '\'' +
                "\nisShow='" + isShow + '\'' +
                "\nitemList='" + itemList +
                '}';
    }
}
