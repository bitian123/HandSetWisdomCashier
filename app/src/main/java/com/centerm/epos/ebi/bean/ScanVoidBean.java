package com.centerm.epos.ebi.bean;

/**
 * Created by liubit on 2017/12/28.
 * 扫码撤销bean
 */

public class ScanVoidBean {


    /**
     * head : {"msgType":"0200","termidm":"98909021","mercode":"845582750450001","termcde":"12000003","imei":"861097031079605","sendTime":"20171227054104"}
     * body : {"response":{"result":{"revoke_result":"","mer_order_no":""},"status":"","status_msg":""}}
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
         * response : {"result":{"revoke_result":"","mer_order_no":""},"status":"","status_msg":""}
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
             * result : {"revoke_result":"","mer_order_no":""}
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
                 * revoke_result:
                 * mer_order_no :
                 */

                private String revoke_result;
                private String mer_order_no;

                public String getMer_order_no() {
                    return mer_order_no;
                }

                public void setMer_order_no(String mer_order_no) {
                    this.mer_order_no = mer_order_no;
                }

                public String getRevoke_result() {
                    return revoke_result;
                }

                public void setRevoke_result(String revoke_result) {
                    this.revoke_result = revoke_result;
                }
            }
        }
    }


}
