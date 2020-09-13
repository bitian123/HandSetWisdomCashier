package com.centerm.epos.bean;

import com.google.gson.Gson;

import java.util.List;

/**
 * create by liubit on 2019-09-03
 * private double readyPayAmt;
 * private boolean checked = false;
 */
public class GtBusinessListBean {


    /**
     * companyId : 96d550d42d0211e8af88005056b44833
     * moneyDetailList : [{"amountReceivable":200000,"amountReceived":0,"area":"area1","areaCode":"areaCode1","businessId":"businessId1","businessType":"认购","businessTypeCode":"Subscription","customList":[{"idNo":"idNo1","idType":0,"name":"name1"},{"idNo":"idNo2","idType":0,"name":"name2"}],"payMethod":"交付合并收款","paymentItemName":"交付合并收款","paymentName":"交付合并收款","paymentPlanId":"paymentPlanId1","receivableDate":"2020-07-18","roomFullName":"roomFullName1","roomId":"roomId1","sign":"1","subjectName":"999","superviseFlag":"1","unionList":[{"amountReceivable":210000,"amountReceived":0,"businessId":"businessId2","businessType":"认购2","businessTypeCode":"Subscription2","payMethod":"按套内面积收取","paymentItemName":"公共物业维修基金","paymentName":"公共物业维修基金","paymentPlanId":"paymentPlanId2","receivableDate":"2020-07-17","unpaidAmount":210000},{"amountReceivable":-10000,"amountReceived":0,"businessId":"businessId3","businessType":"认购3","businessTypeCode":"Subscription3","payMethod":"一次性付款","paymentItemName":"补差款","paymentName":"补差款","paymentPlanId":"paymentPlanId3","receivableDate":"2020-07-07","unpaidAmount":-10000}],"unpaidAmount":200000},{"amountReceivable":200000,"amountReceived":0,"area":"area4","areaCode":"areaCode4","businessId":"businessId4","businessType":"认购","businessTypeCode":"Subscription","customList":[{"idNo":"idNo4","idType":0,"name":"name4"},{"idNo":"idNo5","idType":0,"name":"name5"}],"payMethod":"定金","paymentItemName":"定金","paymentName":"定金","paymentPlanId":"paymentPlanId4","receivableDate":"2020-07-18","roomFullName":"roomFullName4","roomId":"roomId4","sign":"0","subjectName":"222222222222222222","superviseFlag":"0","unpaidAmount":200000},{"amountReceivable":200000,"amountReceived":0,"area":"area6","areaCode":"areaCode6","businessId":"businessId6","businessType":"认购","businessTypeCode":"Subscription","customList":[{"idNo":"idNo6","idType":0,"name":"name6"}],"payMethod":"定金","paymentItemName":"定金","paymentName":"定金","paymentPlanId":"paymentPlanId6","receivableDate":"2020-07-18","roomFullName":"roomFullName6","roomId":"roomId6","sign":"1","subjectName":"222222222222222222","superviseFlag":"0","unionList":[{"amountReceivable":210000,"amountReceived":0,"businessId":"businessId7","businessType":"认购","businessTypeCode":"Subscription","payMethod":"按套内面积收取","paymentItemName":"公共物业维修基金","paymentName":"公共物业维修基金","paymentPlanId":"paymentPlanId7","receivableDate":"2020-07-17","unpaidAmount":210000},{"amountReceivable":-10000,"amountReceived":0,"businessId":"businessId8","businessType":"认购","businessTypeCode":"Subscription","payMethod":"一次性付款","paymentItemName":"补差款","paymentName":"补差款","paymentPlanId":"paymentPlanId8","receivableDate":"2020-07-07","unpaidAmount":-10000}],"unpaidAmount":200000},{"amountReceivable":200000,"amountReceived":0,"area":"area9","areaCode":"areaCode9","businessId":"businessId9","businessType":"认购","businessTypeCode":"Subscription","customList":[{"idNo":"idNo9","idType":0,"name":"name9"}],"payMethod":"定金","paymentItemName":"定金","paymentName":"定金","paymentPlanId":"paymentPlanId9","receivableDate":"2020-07-18","roomFullName":"roomFullName9","roomId":"roomId9","sign":"0","subjectName":"222222222222222222","superviseFlag":"0","unpaidAmount":200000}]
     * projectId : f2720bca97da11e9b6017cd30ab8ab74
     * projectName : 挡板测试项目
     * respCode : 0
     * respMsg : SUCCESS
     */

    private String companyId;
    private String projectId;
    private String projectName;
    private String respCode;
    private String respMsg;
    private String templateId ="1";
    private String templateName="默认小票模板";
    private List<MoneyDetailListBean> moneyDetailList;

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public List<MoneyDetailListBean> getMoneyDetailList() {
        return moneyDetailList;
    }

    public void setMoneyDetailList(List<MoneyDetailListBean> moneyDetailList) {
        this.moneyDetailList = moneyDetailList;
    }

    public static class MoneyDetailListBean {
        /**
         * amountReceivable : 200000.0
         * amountReceived : 0.0
         * area : area1
         * areaCode : areaCode1
         * businessId : businessId1
         * businessType : 认购
         * businessTypeCode : Subscription
         * customList : [{"idNo":"idNo1","idType":0,"name":"name1"},{"idNo":"idNo2","idType":0,"name":"name2"}]
         * payMethod : 交付合并收款
         * paymentItemName : 交付合并收款
         * paymentName : 交付合并收款
         * paymentPlanId : paymentPlanId1
         * receivableDate : 2020-07-18
         * roomFullName : roomFullName1
         * roomId : roomId1
         * sign : 1
         * subjectName : 999
         * superviseFlag : 1
         * unionList : [{"amountReceivable":210000,"amountReceived":0,"businessId":"businessId2","businessType":"认购2","businessTypeCode":"Subscription2","payMethod":"按套内面积收取","paymentItemName":"公共物业维修基金","paymentName":"公共物业维修基金","paymentPlanId":"paymentPlanId2","receivableDate":"2020-07-17","unpaidAmount":210000},{"amountReceivable":-10000,"amountReceived":0,"businessId":"businessId3","businessType":"认购3","businessTypeCode":"Subscription3","payMethod":"一次性付款","paymentItemName":"补差款","paymentName":"补差款","paymentPlanId":"paymentPlanId3","receivableDate":"2020-07-07","unpaidAmount":-10000}]
         * unpaidAmount : 200000.0
         */

        private double amountReceivable;
        private double amountReceived;
        private String area;//地区
        private String areaCode;//地区编码
        private String businessId;
        private String businessType;
        private String businessTypeCode;
        private String payMethod;
        private String paymentItemName;
        private String paymentName;
        private String paymentPlanId;
        private String receivableDate;
        private String roomFullName;
        private String roomId;
        private String sign = "0";//是否是合并支付
        private String subjectName;
        private String superviseFlag = "0";//是否监管
        private double unpaidAmount;
        private List<CustomListBean> customList;
        private List<UnionListBean> unionList;
        private String projectName;
        private String billId;
        private int moneyType;
        private boolean checked = false;
        private String contractNo = "";

        private double readyPayAmt;

        public static MoneyDetailListBean objectFromData(String str) {

            return new Gson().fromJson(str, MoneyDetailListBean.class);
        }


        public int getMoneyType() {
            return moneyType;
        }

        public void setMoneyType(int moneyType) {
            this.moneyType = moneyType;
        }

        public String getBillId() {
            return billId;
        }

        public void setBillId(String billId) {
            this.billId = billId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public double getReadyPayAmt() {
            return readyPayAmt;
        }

        public void setReadyPayAmt(double readyPayAmt) {
            this.readyPayAmt = readyPayAmt;
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

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
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

        public String getBusinessTypeCode() {
            return businessTypeCode;
        }

        public void setBusinessTypeCode(String businessTypeCode) {
            this.businessTypeCode = businessTypeCode;
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

        public String getPaymentName() {
            return paymentName;
        }

        public void setPaymentName(String paymentName) {
            this.paymentName = paymentName;
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

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public void setSubjectName(String subjectName) {
            this.subjectName = subjectName;
        }

        public String getSuperviseFlag() {
            return superviseFlag;
        }

        public void setSuperviseFlag(String superviseFlag) {
            this.superviseFlag = superviseFlag;
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

        public List<UnionListBean> getUnionList() {
            return unionList;
        }

        public void setUnionList(List<UnionListBean> unionList) {
            this.unionList = unionList;
        }

        public String getContractNo() {
            return contractNo;
        }

        public void setContractNo(String contractNo) {
            this.contractNo = contractNo;
        }

        public static class CustomListBean {
            /**
             * idNo : idNo1
             * idType : 0
             * name : name1
             */

            private String idNo;
            private int idType;
            private String name;

            public static CustomListBean objectFromData(String str) {

                return new Gson().fromJson(str, CustomListBean.class);
            }

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

            @Override
            public String toString() {
                return "CustomListBean{" +
                        "idNo='" + idNo + '\'' +
                        ", idType=" + idType +
                        ", name='" + name + '\'' +
                        '}';
            }
        }

        public static class UnionListBean {
            /**
             * amountReceivable : 210000
             * amountReceived : 0
             * businessId : businessId2
             * businessType : 认购2
             * businessTypeCode : Subscription2
             * payMethod : 按套内面积收取
             * paymentItemName : 公共物业维修基金
             * paymentName : 公共物业维修基金
             * paymentPlanId : paymentPlanId2
             * receivableDate : 2020-07-17
             * unpaidAmount : 210000
             */

            private double amountReceivable;
            private double amountReceived;
            private String businessId;
            private String businessType;
            private String businessTypeCode;
            private String payMethod;
            private String paymentItemName;
            private String paymentName;
            private String paymentPlanId;
            private String receivableDate;
            private double unpaidAmount;

            public static UnionListBean objectFromData(String str) {

                return new Gson().fromJson(str, UnionListBean.class);
            }

            public double getAmountReceivable() {
                return amountReceivable;
            }

            public void setAmountReceivable(int amountReceivable) {
                this.amountReceivable = amountReceivable;
            }

            public double getAmountReceived() {
                return amountReceived;
            }

            public void setAmountReceived(int amountReceived) {
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

            public String getBusinessTypeCode() {
                return businessTypeCode;
            }

            public void setBusinessTypeCode(String businessTypeCode) {
                this.businessTypeCode = businessTypeCode;
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

            public String getPaymentName() {
                return paymentName;
            }

            public void setPaymentName(String paymentName) {
                this.paymentName = paymentName;
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

            public double getUnpaidAmount() {
                return unpaidAmount;
            }

            public void setUnpaidAmount(double unpaidAmount) {
                this.unpaidAmount = unpaidAmount;
            }

            @Override
            public String toString() {
                return "UnionListBean{" +
                        "amountReceivable=" + amountReceivable +
                        ", amountReceived=" + amountReceived +
                        ", businessId='" + businessId + '\'' +
                        ", businessType='" + businessType + '\'' +
                        ", businessTypeCode='" + businessTypeCode + '\'' +
                        ", payMethod='" + payMethod + '\'' +
                        ", paymentItemName='" + paymentItemName + '\'' +
                        ", paymentName='" + paymentName + '\'' +
                        ", paymentPlanId='" + paymentPlanId + '\'' +
                        ", receivableDate='" + receivableDate + '\'' +
                        ", unpaidAmount=" + unpaidAmount +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "MoneyDetailListBean{" +
                    "amountReceivable=" + amountReceivable +
                    ", amountReceived=" + amountReceived +
                    ", area='" + area + '\'' +
                    ", areaCode='" + areaCode + '\'' +
                    ", businessId='" + businessId + '\'' +
                    ", businessType='" + businessType + '\'' +
                    ", businessTypeCode='" + businessTypeCode + '\'' +
                    ", payMethod='" + payMethod + '\'' +
                    ", paymentItemName='" + paymentItemName + '\'' +
                    ", paymentName='" + paymentName + '\'' +
                    ", paymentPlanId='" + paymentPlanId + '\'' +
                    ", receivableDate='" + receivableDate + '\'' +
                    ", roomFullName='" + roomFullName + '\'' +
                    ", roomId='" + roomId + '\'' +
                    ", sign='" + sign + '\'' +
                    ", subjectName='" + subjectName + '\'' +
                    ", superviseFlag='" + superviseFlag + '\'' +
                    ", unpaidAmount=" + unpaidAmount +
                    ", customList=" + customList +
                    ", unionList=" + unionList +
                    ", projectName='" + projectName + '\'' +
                    ", billId='" + billId + '\'' +
                    ", moneyType=" + moneyType +
                    ", checked=" + checked +
                    ", readyPayAmt=" + readyPayAmt +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "GtBusinessListBean{" +
                "companyId='" + companyId + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectName='" + projectName + '\'' +
                ", respCode='" + respCode + '\'' +
                ", respMsg='" + respMsg + '\'' +
                ", moneyDetailList=" + moneyDetailList +
                '}';
    }
}
