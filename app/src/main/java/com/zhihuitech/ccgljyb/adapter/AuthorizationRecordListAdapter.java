package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.AuthorizationRecord;
import com.zhihuitech.ccgljyb.entity.CheckRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class AuthorizationRecordListAdapter extends BaseAdapter{
    private Context context;
    private List<AuthorizationRecord> list;
    private SimpleDateFormat sdf1;
    private SimpleDateFormat sdf2;

    public AuthorizationRecordListAdapter(Context context, List<AuthorizationRecord> list) {
        this.context = context;
        this.list = list;
        sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf2 = new SimpleDateFormat("yyyy-MM-dd");
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
            convertView = mInflater.inflate(R.layout.authorization_record_list_item, null);
            viewHolder.tvAuthorizeTime = (TextView) convertView.findViewById(R.id.tv_auth_time_authorization_record_list_item);
            viewHolder.tvEndTime = (TextView) convertView.findViewById(R.id.tv_end_time_authorization_record_list_item);
            viewHolder.tvValidityTime = (TextView) convertView.findViewById(R.id.tv_validity_time_authorization_record_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final AuthorizationRecord item = (AuthorizationRecord) getItem(position);
        viewHolder.tvAuthorizeTime.setText(sdf1.format(new Date(Long.parseLong(item.getAuthorize_time()) * 1000)));
        viewHolder.tvValidityTime.setText(Integer.parseInt(item.getValidity_time()) * 12 + "个月");
        viewHolder.tvEndTime.setText(sdf2.format(new Date(Long.parseLong(item.getEnd_time()) * 1000)));
        return convertView;
    }

    static class ViewHolder {
        TextView tvAuthorizeTime;
        TextView tvEndTime;
        TextView tvValidityTime;
    }
}
