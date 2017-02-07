package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.ProductTypeSettingActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.ProductType;

import java.util.List;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ProductTypeUpdateListAdapter extends BaseAdapter {
    private Context context;
    private List<ProductType> list;

    public ProductTypeUpdateListAdapter(Context context, List<ProductType> list) {
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
            convertView = mInflater.inflate(R.layout.product_type_update_list_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_type_name_product_type_update_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ProductType pt = (ProductType) getItem(position);
        viewHolder.tvName.setText(pt.getName());
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
    }
}
