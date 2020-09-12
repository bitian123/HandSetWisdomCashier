package com.centerm.epos.event;

/**
 * Created by yuhc on 2017/4/3.
 */

public class TradeMessage {
    public static final int BATCH_CHECK_SUCCESS = 1;    //批结算成功
    public static final int PRE_TASK_CONTINUE = 2;      //前置任务继续处理


    public static final int PRINT_TRADE_SLIP = 3;       //打印交易凭条
    public static final int PRINT_NEXT_CONFIRM = 4;     //打印下一联确认
    public static final int PRINT_SLIP_COMPLETE = 5;    //打印完成
    public static final int PRINTER_STATE_CHECK = 6;    //打印机状态检测


    public static final int PRINT_SLIP_LAST = 7;    //打印最后一笔业务
    public static final int PRINT_TRADE_DETAIL = 8;    //打印交易明细
    public static final int PRINT_TRADE_SUMMARY = 9;    //打印结算单
    public static final int PRINT_ERROR = 10;    //打印失败
    public static final int QUERY_SCAN_PAY = 11;
    public static final int PRINT_SLIP_ANY = 12;    //打印任意一笔 zhouzhihua add 2017.11.06


    public static final int PRE_TASK_COMM_TERMINATE = 13;/*前置任务无法连接后台终止*/

    public static final int MAG_LOAD_TASK_CONTINUE = 14;/*磁条卡交易是否继续*/
    public static final int MAG_LOAD_CONFIRM = 15;/*磁条卡充值确认*/

    public static final int GO_LOGIN = 16;/*回到登录界面*/
    public static final int EXIT = 17;/*退出界面*/
}
