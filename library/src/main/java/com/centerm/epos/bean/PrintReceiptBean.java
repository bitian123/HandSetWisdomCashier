package com.centerm.epos.bean;

import java.util.List;

/**
 * create by liubit on 2019-09-26
 */
public class PrintReceiptBean {

    /**
     * respMsg : SUCCESS
     * body : [{"image":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_sign.png","print":[{"image":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_print_1.png"},{"image":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_print_2.png"}],"subOrderId":"418913948720779264","url":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_sign.pdf"}]
     * respCode : 0
     */

    private String respMsg;
    private String respCode;
    private List<BodyBean> body;

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public List<BodyBean> getBody() {
        return body;
    }

    public void setBody(List<BodyBean> body) {
        this.body = body;
    }

    public static class BodyBean {
        /**
         * image : http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_sign.png
         * print : [{"image":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_print_1.png"},{"image":"http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_print_2.png"}]
         * subOrderId : 418913948720779264
         * url : http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_sign.pdf
         */

        private String image;
        private String subOrderId;
        private String url;
        private List<PrintBean> print;

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public String getSubOrderId() {
            return subOrderId;
        }

        public void setSubOrderId(String subOrderId) {
            this.subOrderId = subOrderId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<PrintBean> getPrint() {
            return print;
        }

        public void setPrint(List<PrintBean> print) {
            this.print = print;
        }

        public static class PrintBean {
            /**
             * image : http://116.228.47.76:8088/sources/up/20200509/sign/pdf/418913948720779264_print_1.png
             */

            private String image;

            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
            }
        }
    }
}
