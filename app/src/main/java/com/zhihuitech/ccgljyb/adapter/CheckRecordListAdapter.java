package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.zhihuitech.ccgljyb.CheckStorageActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.CheckRecord;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class CheckRecordListAdapter extends BaseAdapter{
    private Context context;
    private List<CheckRecord> list;
    private SimpleDateFormat sdf;

    public CheckRecordListAdapter(Context context, List<CheckRecord> list) {
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
            convertView = mInflater.inflate(R.layout.check_record_list_item, null);
            viewHolder.tvCreateTime = (TextView) convertView.findViewById(R.id.tv_create_time_check_record_list_item);
            viewHolder.tvLocalName = (TextView) convertView.findViewById(R.id.tv_local_name_check_record_list_item);
            viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.tv_check_status_check_record_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CheckRecord item = (CheckRecord) getItem(position);
        viewHolder.tvCreateTime.setText(sdf.format(new Date(Long.parseLong(item.getCreate_time()) * 1000)));
        viewHolder.tvLocalName.setText(item.getLocal_name());
        viewHolder.tvStatus.setText(item.getStatus().equals("1") ? "正常" : "不匹配");
        viewHolder.tvStatus.setTextColor(item.getStatus().equals("1") ? 0xFF666666 : Color.RED);
        return convertView;
    }
    static class ViewHolder {
        TextView tvCreateTime;
        TextView tvLocalName;
        TextView tvStatus;
    }
}
