package com.centerm.epos.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.centerm.epos.R;
import com.centerm.epos.bean.GtBusinessListBean;
import com.centerm.epos.utils.DataHelper;

import java.util.List;

public class BusinessUnionAdapter extends BaseAdapter {

    private  Context context;
    private Activity mActivity;
    private List<GtBusinessListBean.MoneyDetailListBean.UnionListBean> listBeans;
    private GtBusinessListBean.MoneyDetailListBean moneyListBean;
    private LayoutInflater mInflater;


    public BusinessUnionAdapter(Context mCtx, Activity activity,   GtBusinessListBean.MoneyDetailListBean moneyDetailListBean) {
        this.context = mCtx;
        mActivity = activity;
        mInflater = LayoutInflater.from(mCtx);
        listBeans = moneyDetailListBean.getUnionList();
        moneyListBean = moneyDetailListBean;
    }

    @Override
    public int getCount() {
        return listBeans == null ? 0: listBeans.size();
    }

    @Override
    public Object getItem(int position) {
        return listBeans.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            int layoutId = R.layout.v_union_business_item;
            convertView = mInflater.inflate(layoutId, parent, false);

            holder = new ViewHolder();

            holder.mTvRoomId = ((TextView)convertView.findViewById(R.id.mTvRoomId));
            holder.mTvBillId = ((TextView)convertView.findViewById(R.id.mTvBillId));
            holder.mTvMoneyType = ((TextView)convertView.findViewById(R.id.mTvMoneyType));
            holder.mTvName = ((TextView)convertView.findViewById(R.id.mTvName));
            holder.mTvUnpaidAmount = ((TextView)convertView.findViewById(R.id.mTvUnpaidAmount));
            holder.mTvPaidAmount = ((TextView)convertView.findViewById(R.id.mTvPaidAmount));
            holder.mTvAmtReceivable = ((TextView)convertView.findViewById(R.id.mTvAmtReceivable));
            holder.mTvSettleNo = ((TextView)convertView.findViewById(R.id.mTvSettleNo));
            holder.mEtPayAmount = (TextView) convertView.findViewById(R.id.mEtPayAmount);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        GtBusinessListBean.MoneyDetailListBean.UnionListBean bean = listBeans.get(position);
        holder.mTvRoomId.setText(moneyListBean.getProjectName());
        holder.mTvBillId.setText(moneyListBean.getRoomFullName());
        holder.mTvMoneyType.setText("款项名称: "+bean.getPaymentItemName());
        StringBuilder builder = new StringBuilder("姓名:");
        for(GtBusinessListBean.MoneyDetailListBean.CustomListBean custom : moneyListBean.getCustomList()){
            builder.append(" "+custom.getName());
        }
        holder.mTvName.setText(builder.toString());
        holder.mTvUnpaidAmount.setText("应收金额: "+DataHelper.saved2Decimal(bean.getAmountReceivable())+"元");
        holder.mTvPaidAmount.setText("已收金额: "+DataHelper.saved2Decimal(bean.getAmountReceived())+"元");
        holder.mTvAmtReceivable.setText("本次应收: "+DataHelper.saved2Decimal(bean.getUnpaidAmount())+"元");
        holder.mTvSettleNo.setText("结算帐户: "+moneyListBean.getSubjectName());
        holder.mEtPayAmount.setText(DataHelper.saved2Decimal(bean.getUnpaidAmount())+"元");
        return convertView;
    }


    private static class ViewHolder {
        TextView mTvRoomId;
        TextView mTvBillId;
        TextView mTvMoneyType;
        TextView mTvName;
        TextView mTvUnpaidAmount;
        TextView mTvPaidAmount;
        TextView mTvAmtReceivable;
        TextView mTvSettleNo;
        TextView mEtPayAmount;
    }

}
