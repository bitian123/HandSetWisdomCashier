package com.centerm.epos.ebi.bean;

/**
 * Created by liubit on 2017/12/28.
 * 扫码退货bean
 */

public class ScanRefundBean {


    /**
     * head : {"msgType":"0200","termidm":"98909021","mercode":"845582750450001","termcde":"12000003","imei":"861097031079605","sendTime":"20171227054104"}
     * body : {"response":{"result":{"refund_result":"","mer_refund_order_no":"","mer_order_no":"","refund_amount":"","actual_refund_amount":"","refund_time":""},"status":"","status_msg":""}}
     * sign : B79CBF7D711FFC9799EDA27AD8E007ED3E13A834524A5F7EDB47F7ED3686FB50
     */

    private RequestHeader head;
    private BodyBean body;
    private String sign;

    public RequestHeader getHead() {
        return head;
    }

    public void setHead(RequestHeader head) {
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

    public static class BodyBean {
        /**
         * response : {"result":{"refund_result":"","mer_refund_order_no":"","mer_order_no":"","refund_amount":"","actual_refund_amount":"","refund_time":""},"status":"","status_msg":""}
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
             * result : {"refund_result":"","mer_refund_order_no":"","mer_order_no":"","refund_amount":"","actual_refund_amount":"","refund_time":""}
             * status :
             * status_msg :
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
                 * refund_result :
                 * mer_refund_order_no :
                 * mer_order_no :
                 * refund_amount :
                 * actual_refund_amount :
                 * refund_time :
                 */

                private String refund_result;
                private String mer_refund_order_no;
                private String mer_order_no;
                private String refund_amount;
                private String actual_refund_amount;
                private String refund_time;
                private String pay_type;

                public String getPay_type() {
                    return pay_type;
                }

                public void setPay_type(String pay_type) {
                    this.pay_type = pay_type;
                }

                public String getRefund_result() {
                    return refund_result;
                }

                public void setRefund_result(String refund_result) {
                    this.refund_result = refund_result;
                }

                public String getMer_refund_order_no() {
                    return mer_refund_order_no;
                }

                public void setMer_refund_order_no(String mer_refund_order_no) {
                    this.mer_refund_order_no = mer_refund_order_no;
                }

                public String getMer_order_no() {
                    return mer_order_no;
                }

                public void setMer_order_no(String mer_order_no) {
                    this.mer_order_no = mer_order_no;
                }

                public String getRefund_amount() {
                    return refund_amount;
                }

                public void setRefund_amount(String refund_amount) {
                    this.refund_amount = refund_amount;
                }

                public String getActual_refund_amount() {
                    return actual_refund_amount;
                }

                public void setActual_refund_amount(String actual_refund_amount) {
                    this.actual_refund_amount = actual_refund_amount;
                }

                public String getRefund_time() {
                    return refund_time;
                }

                public void setRefund_time(String refund_time) {
                    this.refund_time = refund_time;
                }
            }
        }
    }
}
