package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.ProductTypeSettingActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.entity.ProductType;

import java.util.List;

/**
 * Created by Administrator on 2016/10/26.
 */
public class ProductTypeListAdapter extends BaseAdapter {
    private Context context;
    private List<ProductType> list;

    public ProductTypeListAdapter(Context context, List<ProductType> list) {
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
            convertView = mInflater.inflate(R.layout.product_type_list_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_type_name_product_type_list_item);
            viewHolder.tvModify = (TextView) convertView.findViewById(R.id.tv_modify_product_type_list_item);
            viewHolder.tvDelete = (TextView) convertView.findViewById(R.id.tv_delete_product_type_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ProductType pt = (ProductType) getItem(position);
        viewHolder.tvName.setText(pt.getName());
        viewHolder.tvModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProductTypeSettingActivity)context).showTypeEditDialog(pt);
            }
        });
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ProductTypeSettingActivity)context).typeDelete(pt);
            }
        });
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvModify;
        TextView tvDelete;
    }
}
