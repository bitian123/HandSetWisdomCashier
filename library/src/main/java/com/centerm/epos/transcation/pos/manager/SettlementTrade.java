package com.centerm.epos.transcation.pos.manager;

import android.os.AsyncTask;
import android.widget.Toast;

import com.centerm.epos.base.BaseTradePresent;
import com.centerm.epos.base.ITradeView;
import com.centerm.epos.bean.TradeInfoRecord;
import com.centerm.epos.common.Settings;
import com.centerm.epos.fragment.trade.TradingFragment;
import com.centerm.epos.task.AsyncCheckBillTask;
import com.centerm.epos.task.AsyncUploadArpcErrorTask;
import com.centerm.epos.task.AsyncUploadIcDataTask;
import com.centerm.epos.task.AsyncUploadIccSpecialTask;
import com.centerm.epos.task.AsyncUploadMagsDataTask;
import com.centerm.epos.task.AsyncUploadOfflineTask;
import com.centerm.epos.task.AsyncUploadRefundDataTask;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import config.BusinessConfig;

import static com.centerm.epos.common.TransDataKey.key_is_balance_settle;
import static com.centerm.epos.common.TransDataKey.key_is_balance_settle_foreign;

/*
    *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
    *@author zhouzhihua 2017.11.07
    * */

/**
 * Created by yuhc on 2017/4/3.
 */

public class SettlementTrade implements ManageTransaction {
    protected Logger logger = Logger.getLogger(this.getClass());
    private List<TradeInfoRecord> magsCards = new ArrayList<>();
    private List<TradeInfoRecord> icCards = new ArrayList<>();
    private List<TradeInfoRecord> refundInfos;

    private List<TradeInfoRecord> ecOfflineApproved;
    private List<TradeInfoRecord> ecOfflineDenial;
    private List<TradeInfoRecord> icCardsArpcErr;

    TradingFragment mTradingView;
    BaseTradePresent mTradePresent;

    @Override
    public void execute(final ITradeView tradeView, final BaseTradePresent tradePresent) {
        if (!(tradeView instanceof TradingFragment))
            return;
        mTradingView = (TradingFragment) tradeView;
        mTradePresent = tradePresent;
        ((TradingFragment) tradeView).updateHint("继续批结算...");
        String amountStr = Settings.getValue(tradeView.getHostActivity(), Settings.KEY.BATCH_SEND_RETURN_DATA, "");
        if (!"".equals(amountStr)) {
            onAccountCheckSuccess(amountStr);
        } else {
            logger.error("继续批结算时，获取到对账返回的信息为空！");
        }
    }

    public void onAccountCheckSuccess(String checkStr) {
//        mTradingView.updateHint("正在批结算\n    请稍后");
        magsCards.clear();
        icCards.clear();
        final String amountCode = ""+checkStr.charAt(30);
        /*
        * @author zhouzhihua
        * 增加外卡对账标志foreignCard 2017.11.07
        * */
        if(checkStr.length() == (12+3+12+3+1)*2) {
            String foreignCard = "" + checkStr.charAt(checkStr.length() - 1);
            mTradePresent.getTransData().put(key_is_balance_settle_foreign,foreignCard.equals("1") ? "1" : "0");
        }

        Settings.setValue(mTradingView.getHostActivity(), Settings.KEY.IS_BATCH_EQUELS, amountCode);
        mTradingView.getHostActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AsyncCheckBillTask(mTradingView.getHostActivity()) {
                    @Override
                    public void onStart() {
                        super.onStart();
                        mTradingView.updateHint("正在查询终端数据……");
                    }

                    @Override
                    public void onFinish(List<List<TradeInfoRecord>> lists) {
                        super.onFinish(lists);

                        boolean isSupportBatchUpload = BusinessConfig.getInstance().getFlag(mTradingView.getContext()
                                , BusinessConfig.Key.TOGGLE_BATCH_UPLOAD);
                        if (!isSupportBatchUpload){
                            logger.debug("不支持批上送，直接发送批上送完成");
                            new BatchSendComplete().execute(mTradingView,mTradePresent);
                            return;
                        }
                        /*
                        *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
                        *@author zhouzhihua 2017.11.07
                        * */
                        mTradePresent.getTransData().put(key_is_balance_settle, "0");
                        magsCards = lists.get(0);
                        icCards = lists.get(1);
                        refundInfos = lists.get(2);
                        ecOfflineApproved = lists.get(3);
                        ecOfflineDenial = lists.get(4);
                        icCardsArpcErr = lists.get(5);
                        switch (amountCode) {
                            case "0":
                                mTradePresent.gotoNextStep();
                                mTradingView.popToast("对账返回码错误：00", Toast.LENGTH_SHORT);
                                logger.debug("对账返回为0未定义");
                                break;
                            case "1"://对账平
                                logger.debug("对账返回为1对账平");
                                /*
                                *BUGID:0002279: 进行结算，平台返回对账不平，批上送结束为207，应该为202
                                *@author zhouzhihua 2017.11.07
                                * */
                                mTradePresent.getTransData().put(key_is_balance_settle, "1");
                                roopToUploadIc();
                                break;
                            case "2"://对账不平
                                mTradingView.updateHint("  对账不平 \n正在批上送\n    请稍侯  ");
                                logger.debug("对账返回为2对账不平");
                                //roopToUploadCard();
                                roopToUpMagOfflineTrans();
                                break;
                            case "3":
                                mTradingView.updateHint("  对账出错 \n正在批上送\n    请稍侯  ");
                                logger.debug("对账返回为3对账出错");
                                roopToUpMagOfflineTrans();
                                break;
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });

    }
    private void roopToUpMagOfflineTrans(){
        roopToUpIccOfflineTrans();
    }

    private void roopToUpIccOfflineTrans(){
        if ( null != ecOfflineApproved && ecOfflineApproved.size() > 0 ){
            new AsyncUploadOfflineTask(mTradingView.getHostActivity(), mTradePresent.getTransData()){
                @Override
                public void onProgress(Integer counts, Integer index) {
                    super.onProgress(counts, index);
                    mTradingView.updateHint("IC卡脱机交易" + counts + "批\n正在上送第" + (index) + "批");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    mTradingView.updateHint("正在查IC卡脱胶交易数据……");
                }

                @Override
                public void onFinish(String[] strings) {
                    super.onFinish(strings);
                    roopToUploadRefund();
                }
            }.setInfoList(ecOfflineApproved).setSettleFlag(true)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else{
            logger.debug("roopToUpIccOfflineTrans ecOfflineApproved没有交易记录");
            roopToUploadCard();
        }

    }

    private void roopToUploadCard() {
        if (null != magsCards && magsCards.size() > 0) {
            //uploadMagsCardData(strings.get(0));
            new AsyncUploadMagsDataTask(mTradingView.getHostActivity(), mTradePresent.getTransData(), magsCards) {
                @Override
                public void onProgress(Integer counts, Integer index) {
                    super.onProgress(counts, index);
                    mTradingView.updateHint("磁条卡数据共" + counts + "批\n正在上送第" + (index) + "批");
                }

                @Override
                public void onStart() {
                    super.onStart();
                    mTradingView.updateHint("正在查询磁条卡数据……");
                }

                @Override
                public void onFinish(String[] strings) {
                    super.onFinish(strings);
                    roopToUploadRefund();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            logger.debug("磁条卡没有交易记录");
            roopToUploadRefund();
        }
    }

    private void roopToUploadRefund() {
        logger.debug("进入退货循环上送方法");
        if (null != refundInfos && refundInfos.size() > 0) {
            uploadRefundDataTask();
        } else {
            logger.debug("退货没有交易记录");
            roopToUploadIc();
        }
    }

    private void uploadRefundDataTask() {
        new AsyncUploadRefundDataTask(mTradingView.getHostActivity(), mTradePresent.getTransData(), refundInfos) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                mTradingView.updateHint("退货数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                mTradingView.updateHint("正在查询退货数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                roopToUploadIc();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void roopToUploadIc() {
        if (icCards != null && icCards.size() > 0) {
            uploadIcDataTask();
        } else {
            logger.debug("roopToUploadIc 没有交易记录");
            roopToUploadIccSpecial();
        }

    }

    private void uploadIcDataTask() {
        new AsyncUploadIcDataTask(mTradingView.getHostActivity(), mTradePresent.getTransData(), icCards) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                mTradingView.updateHint("IC卡数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                mTradingView.updateHint("正在查询IC卡数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
               // new BatchSendComplete().execute(mTradingView,mTradePresent);
                roopToUploadIccSpecial();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void roopToUploadIccSpecial() {
        if (ecOfflineDenial != null && ecOfflineDenial.size() > 0) {
            uploadIccIccSpecialTask();
        } else {
            logger.debug("roopToUploadIccSpecial 没有交易记录");
            roopToUploadIccArpcError();
        }

    }
    private void uploadIccIccSpecialTask() {
        new AsyncUploadIccSpecialTask(mTradingView.getHostActivity(), mTradePresent.getTransData(), ecOfflineDenial) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                mTradingView.updateHint("IC卡脱机拒绝交易数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                mTradingView.updateHint("正在查询IC卡数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                roopToUploadIccArpcError();
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
    private void roopToUploadIccArpcError() {
        if(icCardsArpcErr != null && icCardsArpcErr.size() > 0) {
            uploadIccArpcErrorTask();
        } else {
            logger.debug("roopToUploadIccArpcError 没有交易记录");
            new BatchSendComplete().execute(mTradingView,mTradePresent);
        }

    }
    private void uploadIccArpcErrorTask() {
        new AsyncUploadArpcErrorTask(mTradingView.getHostActivity(), mTradePresent.getTransData(), icCardsArpcErr) {
            @Override
            public void onProgress(Integer counts, Integer index) {
                super.onProgress(counts, index);
                mTradingView.updateHint("IC卡ARPC数据共" + counts + "条\n正在上送第" + (index) + "条");
            }

            @Override
            public void onStart() {
                super.onStart();
                mTradingView.updateHint("正在查询IC卡数据……");
            }

            @Override
            public void onFinish(String[] strings) {
                super.onFinish(strings);
                new BatchSendComplete().execute(mTradingView,mTradePresent);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


}
