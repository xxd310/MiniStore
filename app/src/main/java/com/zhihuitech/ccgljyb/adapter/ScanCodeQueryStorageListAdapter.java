package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.LocationNumber;

import java.util.List;

/**
 * Created by Administrator on 2016/10/28.
 */
public class ScanCodeQueryStorageListAdapter extends BaseAdapter{
    private Context context;
    private List<LocationNumber> list;

    public ScanCodeQueryStorageListAdapter(Context context, List<LocationNumber> list) {
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
            convertView = mInflater.inflate(R.layout.scan_code_query_storage_list_item, null);
            viewHolder.tvLocalName = (TextView) convertView.findViewById(R.id.tv_local_name_scan_code_query_storage_list_item);
            viewHolder.tvProductNumber = (TextView) convertView.findViewById(R.id.tv_product_num_scan_code_query_storage_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final LocationNumber ln = (LocationNumber) getItem(position);
        viewHolder.tvLocalName.setText(ln.getLocal_name());
        viewHolder.tvProductNumber.setText(ln.getNumber());
        return convertView;
    }
    static class ViewHolder {
        TextView tvLocalName;
        TextView tvProductNumber;
    }
}
