package com.centerm.epos.ebi.bean;

/**
 * Created by liubit on 2017/12/25.
 * 下载主密钥
 */

public class LoadMakBean {

    /**
     * head : {"msgType":"0210","termidm":"lipingjhpaytest","mercode":"872880015200001","termcde":"12345678","imei":"869612028790724","responseTime":"20171225164748"}
     * body : {"response":{"status":"PF","status_msg":"请求参数错误"}}
     */

    private RespHeader head;
    private BodyBean body;

    public RespHeader getHead() {
        return head;
    }

    public void setHead(RespHeader head) {
        this.head = head;
    }

    public BodyBean getBody() {
        return body;
    }

    public void setBody(BodyBean body) {
        this.body = body;
    }

    public static class BodyBean {
        /**
         * response : {"status":"PF","status_msg":"请求参数错误"}
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
             * status : PF
             * status_msg : 请求参数错误
             */

            private String status;
            private String status_msg;
            private String encSAKey;

            public String getEncSAKey() {
                return encSAKey;
            }

            public void setEncSAKey(String encSAKey) {
                this.encSAKey = encSAKey;
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

            @Override
            public String toString() {
                return "ResponseBean{" +
                        "status='" + status + '\'' +
                        ", status_msg='" + status_msg + '\'' +
                        ", encSAKey='" + encSAKey + '\'' +
                        '}';
            }
        }
    }

    @Override
    public String toString() {
        return "LoadMakBean{" +
                "head=" + head +
                ", body=" + body +
                '}';
    }
}
