package com.centerm.epos.redevelop;

/**
 * Created by yuhc on 2017/11/1.
 * 二次开发接口定义
 */

public interface IRedevelopAction {

    //报文头处理
    String REQUEST_TRADE = "RequestTrade";
    //报文头请求标识对应的业务
    String REQUEST_CODE = "RequestCode";
    //报文头请求处理校验
    String REQUEST_CHECK = "RequestCheck";
    //报文头处理
    String PROCESS_REQUEST = "ProcessRequest";
    //获取报文头数据
    String MSG_HEAD_DATA = "MessaeHeadData";
    //获取应用项目的数据库版本号
    String PROJECT_DB_VERSION = "ProjectDBVersion";

    /**
     * 无参数的执行函数
     *
     * @return 返回值
     */
    Object doAction();

    /**
     * 带一个参数的执行函数
     *
     * @param parameterObj 参数1
     * @return 返回结果
     */
    Object doAction(Object parameterObj);

    /**
     * 带二个参数的执行函数
     *
     * @param parameter1Obj 参数1
     * @param parameter2Obj 参数2
     * @return 返回结果
     */
    Object doAction(Object parameter1Obj, Object parameter2Obj);

    /**
     * 带三个参数的执行函数
     *
     * @param parameter1Obj 参数1
     * @param parameter2Obj 参数2
     * @param parameter2Obj 参数3
     * @return 返回结果
     */
    Object doAction(Object parameter1Obj, Object parameter2Obj, Object parameter3Obj);

}
