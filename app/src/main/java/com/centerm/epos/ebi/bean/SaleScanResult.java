package com.centerm.epos.ebi.bean;

/**
 * Created by liubit on 2017/12/27.
 * 扫码支付bean
 */

public class SaleScanResult {

    /**
     * head : {"msgType":"0200","termidm":"98909021","mercode":"845582750450001","termcde":"12000003","imei":"861097031079605","sendTime":"20171227054104"}
     * body : {"response":{"result":{"mer_order_no":"C201712270541047708","pay_amount":"1","pay_result":"S","pay_no":"2017122700003998","pay_time":"20171227174110","actual_pay_amount":"1"},"status":"00","status_msg":"success"}}
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
         * response : {"result":{"mer_order_no":"C201712270541047708","pay_amount":"1","pay_result":"S","pay_no":"2017122700003998","pay_time":"20171227174110","actual_pay_amount":"1"},"status":"00","status_msg":"success"}
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
             * result : {"mer_order_no":"C201712270541047708","pay_amount":"1","pay_result":"S","pay_no":"2017122700003998","pay_time":"20171227174110","actual_pay_amount":"1"}
             * status : 00
             * status_msg : success
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
                 * mer_order_no : C201712270541047708
                 * pay_amount : 1
                 * pay_result : S
                 * pay_no : 2017122700003998
                 * pay_time : 20171227174110
                 * actual_pay_amount : 1
                 */

                private String mer_order_no;
                private String out_order_no;
                private String pay_amount;
                private String pay_result;
                private String pay_no;
                private String pay_time;
                private String pay_request_no;
                private String receipt_amount;
                private String actual_pay_amount;

                public String getMer_order_no() {
                    return mer_order_no;
                }

                public void setMer_order_no(String mer_order_no) {
                    this.mer_order_no = mer_order_no;
                }

                public String getPay_amount() {
                    return pay_amount;
                }

                public void setPay_amount(String pay_amount) {
                    this.pay_amount = pay_amount;
                }

                public String getPay_result() {
                    return pay_result;
                }

                public void setPay_result(String pay_result) {
                    this.pay_result = pay_result;
                }

                public String getPay_no() {
                    return pay_no;
                }

                public void setPay_no(String pay_no) {
                    this.pay_no = pay_no;
                }

                public String getReceipt_amount() {
                    return receipt_amount;
                }

                public void setReceipt_amount(String receipt_amount) {
                    this.receipt_amount = receipt_amount;
                }

                public String getPay_request_no() {
                    return pay_request_no;
                }

                public void setPay_request_no(String pay_request_no) {
                    this.pay_request_no = pay_request_no;
                }

                public String getPay_time() {
                    return pay_time;
                }

                public void setPay_time(String pay_time) {
                    this.pay_time = pay_time;
                }

                public String getActual_pay_amount() {
                    return actual_pay_amount;
                }

                public void setActual_pay_amount(String actual_pay_amount) {
                    this.actual_pay_amount = actual_pay_amount;
                }

                public String getOut_order_no() {
                    return out_order_no;
                }

                public void setOut_order_no(String out_order_no) {
                    this.out_order_no = out_order_no;
                }

                @Override
                public String toString() {
                    return "ResultBean{" +
                            "mer_order_no='" + mer_order_no + '\'' +
                            "out_order_no='" + out_order_no + '\'' +
                            ", pay_amount='" + pay_amount + '\'' +
                            ", pay_result='" + pay_result + '\'' +
                            ", pay_no='" + pay_no + '\'' +
                            ", pay_time='" + pay_time + '\'' +
                            ", pay_request_no='" + pay_request_no + '\'' +
                            ", receipt_amount='" + receipt_amount + '\'' +
                            ", actual_pay_amount='" + actual_pay_amount + '\'' +
                            '}';
                }
            }

            @Override
            public String toString() {
                return "ResponseBean{" +
                        "result=" + result +
                        ", status='" + status + '\'' +
                        ", status_msg='" + status_msg + '\'' +
                        '}';
            }
        }
    }

    @Override
    public String toString() {
        return "SaleScanResult{" +
                "head=" + head +
                ", body=" + body +
                ", sign='" + sign + '\'' +
                '}';
    }
}
