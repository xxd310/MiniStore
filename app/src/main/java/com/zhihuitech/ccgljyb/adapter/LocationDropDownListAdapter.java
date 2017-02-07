package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.StoreInfoActivity;
import com.zhihuitech.ccgljyb.LocationSelectActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.Location;

import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class LocationDropDownListAdapter extends BaseAdapter {
    private Context context;
    private List<Location> list;

    public LocationDropDownListAdapter(Context context, List<Location> list) {
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
            convertView = mInflater.inflate(R.layout.location_popupwindow_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_location_name_location_popupwindow_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Location location = (Location) getItem(position);
        viewHolder.tvName.setText(location.getName());
        viewHolder.tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(context instanceof LocationSelectActivity) {
                    ((LocationSelectActivity)context).dismissPopupWindow(((TextView)v).getText().toString(), position);
                } else if(context instanceof StoreInfoActivity) {
                    ((StoreInfoActivity)context).dismissPopupWindow(((TextView)v).getText().toString());
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        TextView tvName;
    }
}
