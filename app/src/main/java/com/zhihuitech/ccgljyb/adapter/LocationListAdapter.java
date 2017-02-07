package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.EditLocationActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.Location;

import java.util.List;

/**
 * Created by Administrator on 2016/10/26.
 */
public class LocationListAdapter extends BaseAdapter {
    private Context context;
    private List<Location> list;

    public LocationListAdapter(Context context, List<Location> list) {
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
            convertView = mInflater.inflate(R.layout.location_list_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_location_name_location_list_item);
            viewHolder.tvBarcode = (TextView) convertView.findViewById(R.id.tv_location_barcode_location_list_item);
            viewHolder.btnEdit = (Button) convertView.findViewById(R.id.btn_edit_location_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final Location location = (Location) getItem(position);
        viewHolder.tvName.setText(location.getName());
        viewHolder.tvBarcode.setText(location.getNum());
        viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditLocationActivity.class);
                intent.putExtra("location", location);
                context.startActivity(intent);
            }
        });
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvBarcode;
        Button btnEdit;
    }
}
