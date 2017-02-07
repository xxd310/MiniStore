package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zhihuitech.ccgljyb.CheckStorageActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class CheckStorageListAdapter extends BaseAdapter{
    private Context context;
    private List<CheckStorageListItem> list;

    public CheckStorageListAdapter(Context context, List<CheckStorageListItem> list) {
        this.context = context;
        this.list = list;
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
            convertView = mInflater.inflate(R.layout.check_storage_list_item, null);
            viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.ll_check_storage_list_item);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_name_check_storage_list_item);
            viewHolder.tvLocalName = (TextView) convertView.findViewById(R.id.tv_local_name_check_storage_list_item);
            viewHolder.tvLocalNumber = (TextView) convertView.findViewById(R.id.tv_local_number_check_storage_list_item);
            viewHolder.tvActualNumber = (TextView) convertView.findViewById(R.id.tv_actual_number_check_storage_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CheckStorageListItem item = (CheckStorageListItem) getItem(position);
        viewHolder.tvName.setText(item.getProduct_name());
        viewHolder.tvLocalName.setText(item.getLocal_name());
        viewHolder.tvLocalNumber.setText(item.getNumber());
        viewHolder.tvActualNumber.setText(item.getActual_number());
        viewHolder.ll.setBackgroundColor(Integer.parseInt(item.getNumber()) != Integer.parseInt(item.getActual_number()) ? Color.RED : Color.WHITE);
        return convertView;
    }

    static class ViewHolder {
        LinearLayout ll;
        TextView tvName;
        TextView tvLocalName;
        TextView tvLocalNumber;
        TextView tvActualNumber;
    }
}
