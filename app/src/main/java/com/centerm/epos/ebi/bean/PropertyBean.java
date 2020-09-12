package com.centerm.epos.ebi.bean;

public class PropertyBean {
    /**
     * head : {"msgType":"0200","termidm":"D1V0590002138","mercode":"872290045110204","termcde":"08000251","imei":"867188036882822","sendTime":"20190313092746"}
     * body : {"response":{"result":{"mer_order_no":"DD1552307353030","merc_id":"872290045110204","pay_amount":"1","result":"S","result_desc":"成功","order_time":"1552307353030"},"status":"00","status_msg":"成功"}}
     * sign : CCE1B318E4DFE40EEB8102F7CFF2C37C21CAEE888A46D09EAF7A20503504589C
     */

    private HeadBean head;
    private BodyBean body;
    private String sign;

    public HeadBean getHead() {
        return head;
    }

    public void setHead(HeadBean head) {
        this.head = head;
    }

    public BodyBean getBody() {
        return body;
    }

    public void setBody(BodyBean body) {
        this.body = body;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public static class HeadBean {
        /**
         * msgType : 0200
         * termidm : D1V0590002138
         * mercode : 872290045110204
         * termcde : 08000251
         * imei : 867188036882822
         * sendTime : 20190313092746
         */

        private String msgType;
        private String termidm;
        private String mercode;
        private String termcde;
        private String imei;
        private String sendTime;

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public String getTermidm() {
            return termidm;
        }

        public void setTermidm(String termidm) {
            this.termidm = termidm;
        }

        public String getMercode() {
            return mercode;
        }

        public void setMercode(String mercode) {
            this.mercode = mercode;
        }

        public String getTermcde() {
            return termcde;
        }

        public void setTermcde(String termcde) {
            this.termcde = termcde;
        }

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }

        public String getSendTime() {
            return sendTime;
        }

        public void setSendTime(String sendTime) {
            this.sendTime = sendTime;
        }
    }

    public static class BodyBean {
        /**
         * response : {"result":{"mer_order_no":"DD1552307353030","merc_id":"872290045110204","pay_amount":"1","result":"S","result_desc":"成功","order_time":"1552307353030"},"status":"00","status_msg":"成功"}
         */

        private ResponseBean response;

        public ResponseBean getResponse() {
            return response;
        }

        public void setResponse(ResponseBean response) {
            this.response = response;
        }

        public static class ResponseBean {
            /**
             * result : {"mer_order_no":"DD1552307353030","merc_id":"872290045110204","pay_amount":"1","result":"S","result_desc":"成功","order_time":"1552307353030"}
             * status : 00
             * status_msg : 成功
             */

            private ResultBean result;
            private String status;
            private String status_msg;

            public ResultBean getResult() {
                return result;
            }

            public void setResult(ResultBean result) {
                this.result = result;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getStatus_msg() {
                return status_msg;
            }

            public void setStatus_msg(String status_msg) {
                this.status_msg = status_msg;
            }

            public static class ResultBean {
                /**
                 * mer_order_no : DD1552307353030
                 * merc_id : 872290045110204
                 * pay_amount : 1
                 * result : S
                 * result_desc : 成功
                 * order_time : 1552307353030
                 */

                private String mer_order_no;
                private String merc_id;
                private String pay_amount;
                private String result;
                private String result_desc;
                private String order_time;

                public String getMer_order_no() {
                    return mer_order_no;
                }

                public void setMer_order_no(String mer_order_no) {
                    this.mer_order_no = mer_order_no;
                }

                public String getMerc_id() {
                    return merc_id;
                }

                public void setMerc_id(String merc_id) {
                    this.merc_id = merc_id;
                }

                public String getPay_amount() {
                    return pay_amount;
                }

                public void setPay_amount(String pay_amount) {
                    this.pay_amount = pay_amount;
                }

                public String getResult() {
                    return result;
                }

                public void setResult(String result) {
                    this.result = result;
                }

                public String getResult_desc() {
                    return result_desc;
                }

                public void setResult_desc(String result_desc) {
                    this.result_desc = result_desc;
                }

                public String getOrder_time() {
                    return order_time;
                }

                public void setOrder_time(String order_time) {
                    this.order_time = order_time;
                }
            }
        }
    }
}
