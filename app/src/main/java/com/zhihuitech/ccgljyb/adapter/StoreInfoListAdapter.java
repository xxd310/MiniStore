package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.StoreInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class StoreInfoListAdapter extends BaseAdapter {
    private Context context;
    private List<StoreInfo> list;
    private SimpleDateFormat sdf;

    public StoreInfoListAdapter(Context context, List<StoreInfo> list) {
        this.context = context;
        this.list = list;
        sdf = new SimpleDateFormat("yyyy-MM-dd");
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (null == convertView) {
            viewHolder = new ViewHolder();
            LayoutInflater mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.store_info_list_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_name_store_info_list_item);
            viewHolder.tvLocation = (TextView) convertView.findViewById(R.id.tv_location_name_store_info_list_item);
            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_product_number_store_info_list_item);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time_store_info_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        StoreInfo si = (StoreInfo) getItem(position);
        viewHolder.tvName.setText(si.getProduct_name());
        viewHolder.tvLocation.setText(si.getLocal_name());
        viewHolder.tvNumber.setText(si.getNumber());
        viewHolder.tvTime.setText(sdf.format(new Date(Long.parseLong(si.getChange_time()) * 1000)));
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
        TextView tvLocation;
        TextView tvNumber;
        TextView tvTime;
    }
}
