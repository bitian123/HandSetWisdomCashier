package com.centerm.epos.bean;


import java.util.Objects;

public class ContrantInfoBean {
    String name ;
    String idNo;

    public ContrantInfoBean(String name, String idNo) {
        this.name = name;
        this.idNo = idNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ContrantInfoBean)) {
            return false;
        }
        ContrantInfoBean that = (ContrantInfoBean) o;
        return getName().equals(that.getName()) &&
                getIdNo().equals(that.getIdNo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getIdNo());
    }


    public static void main(String[] args) {
        ContrantInfoBean b1=new ContrantInfoBean("1","张三");

        ContrantInfoBean b2=new ContrantInfoBean("1","张三");
       boolean isEqual = b1.equals(b2);
        System.out.println("是否相等:"+isEqual);

    }
}
