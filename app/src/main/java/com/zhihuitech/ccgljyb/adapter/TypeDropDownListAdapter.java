package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.InStorageActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.ProductType;

import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class TypeDropDownListAdapter extends BaseAdapter {
    private Context context;
    private List<ProductType> list;

    public TypeDropDownListAdapter(Context context, List<ProductType> list) {
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
            convertView = mInflater.inflate(R.layout.type_popupwindow_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_type_name_type_popupwindow_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ProductType pt = (ProductType) getItem(position);
        viewHolder.tvName.setText(pt.getName());
        viewHolder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof InStorageActivity) {
                    ((InStorageActivity)context).dismissPopupWindow(position);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
    }
}
