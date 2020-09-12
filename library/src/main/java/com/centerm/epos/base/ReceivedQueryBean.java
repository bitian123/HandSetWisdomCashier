package com.centerm.epos.base;

import java.util.List;

/**
 * create by liubit on 2019-09-10
 */
public class ReceivedQueryBean {

    /**
     * companyId : 111
     * idType : 0
     * projectId : 1000
     * projectName : 杭州绿城项目
     * queryLists : [{"amountReceivable":0.01,"amountReceived":0.01,"businessId":"0001","businessType":"1","customList":[{"idNo":"1","idType":4,"name":"张三","subOrderId":"1029111539658752"}],"mainOrderId":"C201909271004450968","moneyType":1,"payMethod":"01","paymentItemName":"款项名称1","paymentPlanId":"0001","receivableDate":"2019-09-27","roomFullName":"房间1","roomId":"123","subjectName":"银行账号名称1","unpaidAmount":0}]
     * respCode : 0
     * respMsg : SUCCESS
     */

    private String companyId;
    private int idType;
    private String projectId;
    private String projectName;
    private String respCode;
    private String respMsg;
    private List<QueryListsBean> queryLists;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public int getIdType() {
        return idType;
    }

    public void setIdType(int idType) {
        this.idType = idType;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public List<QueryListsBean> getQueryLists() {
        return queryLists;
    }

    public void setQueryLists(List<QueryListsBean> queryLists) {
        this.queryLists = queryLists;
    }

    public static class QueryListsBean {
        /**
         * amountReceivable : 0.01
         * amountReceived : 0.01
         * businessId : 0001
         * businessType : 1
         * customList : [{"idNo":"1","idType":4,"name":"张三","subOrderId":"1029111539658752"}]
         * mainOrderId : C201909271004450968
         * moneyType : 1
         * payMethod : 01
         * paymentItemName : 款项名称1
         * paymentPlanId : 0001
         * receivableDate : 2019-09-27
         * roomFullName : 房间1
         * roomId : 123
         * subjectName : 银行账号名称1
         * unpaidAmount : 0.0
         */

        private double amountReceivable;
        private double amountReceived;
        private String businessId;
        private String businessType;
        private String mainOrderId;
        private int moneyType;
        private String payMethod;
        private String paymentItemName;
        private String paymentPlanId;
        private String receivableDate;
        private String roomFullName;
        private String roomId;
        private String subjectName;
        private String printTime;
        private String billCode;
        private String payDate;
        private double unpaidAmount;
        private List<CustomListBean> customList;
        private boolean checked = false;

        public String getPayDate() {
            return payDate;
        }

        public void setPayDate(String payDate) {
            this.payDate = payDate;
        }

        public String getPrintTime() {
            return printTime;
        }

        public void setPrintTime(String printTime) {
            this.printTime = printTime;
        }

        public String getBillCode() {
            return billCode;
        }

        public void setBillCode(String billCode) {
            this.billCode = billCode;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public double getAmountReceivable() {
            return amountReceivable;
        }

        public void setAmountReceivable(double amountReceivable) {
            this.amountReceivable = amountReceivable;
        }

        public double getAmountReceived() {
            return amountReceived;
        }

        public void setAmountReceived(double amountReceived) {
            this.amountReceived = amountReceived;
        }

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public String getBusinessType() {
            return businessType;
        }

        public void setBusinessType(String businessType) {
            this.businessType = businessType;
        }

        public String getMainOrderId() {
            return mainOrderId;
        }

        public void setMainOrderId(String mainOrderId) {
            this.mainOrderId = mainOrderId;
        }

        public int getMoneyType() {
            return moneyType;
        }

        public void setMoneyType(int moneyType) {
            this.moneyType = moneyType;
        }

        public String getPayMethod() {
            return payMethod;
        }

        public void setPayMethod(String payMethod) {
            this.payMethod = payMethod;
        }

        public String getPaymentItemName() {
            return paymentItemName;
        }

        public void setPaymentItemName(String paymentItemName) {
            this.paymentItemName = paymentItemName;
        }

        public String getPaymentPlanId() {
            return paymentPlanId;
        }

        public void setPaymentPlanId(String paymentPlanId) {
            this.paymentPlanId = paymentPlanId;
        }

        public String getReceivableDate() {
            return receivableDate;
        }

        public void setReceivableDate(String receivableDate) {
            this.receivableDate = receivableDate;
        }

        public String getRoomFullName() {
            return roomFullName;
        }

        public void setRoomFullName(String roomFullName) {
            this.roomFullName = roomFullName;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public double getUnpaidAmount() {
            return unpaidAmount;
        }

        public void setUnpaidAmount(double unpaidAmount) {
            this.unpaidAmount = unpaidAmount;
        }

        public List<CustomListBean> getCustomList() {
            return customList;
        }

        public void setCustomList(List<CustomListBean> customList) {
            this.customList = customList;
        }

        public static class CustomListBean {
            /**
             * idNo : 1
             * idType : 4
             * name : 张三
             * subOrderId : 1029111539658752
             */

            private String idNo;
            private int idType;
            private String name;
            private String subOrderId;

            public String getIdNo() {
                return idNo;
            }

            public void setIdNo(String idNo) {
                this.idNo = idNo;
            }

            public int getIdType() {
                return idType;
            }

            public void setIdType(int idType) {
                this.idType = idType;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getSubOrderId() {
                return subOrderId;
            }

            public void setSubOrderId(String subOrderId) {
                this.subOrderId = subOrderId;
            }
        }
    }

    @Override
    public String toString() {
        return "ReceivedQueryBean{" +
                "companyId='" + companyId + '\'' +
                ", idType=" + idType +
                ", projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", respCode='" + respCode + '\'' +
                ", respMsg='" + respMsg + '\'' +
                ", queryLists=" + queryLists +
                '}';
    }
}
