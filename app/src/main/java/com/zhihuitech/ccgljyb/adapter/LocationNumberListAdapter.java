package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.LocationNumber;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */
public class LocationNumberListAdapter extends BaseAdapter{
    private Context context;
    private List<LocationNumber> list;

    public LocationNumberListAdapter(Context context, List<LocationNumber> list) {
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
            convertView = mInflater.inflate(R.layout.location_out_number_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_local_name_location_out_number_item);
            viewHolder.tvNumber = (TextView) convertView.findViewById(R.id.tv_local_num_location_out_number_item);
            viewHolder.etOutNumber = (EditText) convertView.findViewById(R.id.et_product_num_location_out_number_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final LocationNumber ln = (LocationNumber) getItem(position);
        viewHolder.tvName.setText(ln.getLocal_name());
        viewHolder.tvNumber.setText("库存" + ln.getNumber());
        viewHolder.etOutNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    String in = ((EditText)v).getText().toString();
                    ln.setOut_number(in);
                }
            }
        });
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvNumber;
        EditText etOutNumber;
    }
}
