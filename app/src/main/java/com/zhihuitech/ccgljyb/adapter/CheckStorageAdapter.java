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
public class CheckStorageAdapter extends BaseAdapter{
    private Context context;
    private List<CheckStorageListItem> list;

    public CheckStorageAdapter(Context context, List<CheckStorageListItem> list) {
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
            convertView = mInflater.inflate(R.layout.check_storage_item, null);
            viewHolder.ll = (LinearLayout) convertView.findViewById(R.id.ll_check_storage_item);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_name_check_storage_item);
            viewHolder.tvStock = (TextView) convertView.findViewById(R.id.tv_stock_check_storage_item);
            viewHolder.etNumber = (EditText) convertView.findViewById(R.id.et_product_number_check_storage_item);
            viewHolder.tvDelete = (TextView) convertView.findViewById(R.id.tv_delete_check_storage_item);
            viewHolder.ivMinus = (ImageView) convertView.findViewById(R.id.iv_minus_check_storage_item);
            viewHolder.ivAdd = (ImageView) convertView.findViewById(R.id.iv_add_check_storage_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final CheckStorageListItem item = (CheckStorageListItem) getItem(position);
        viewHolder.tvName.setText(item.getProduct_name());
        viewHolder.tvStock.setText(item.getNumber());
        viewHolder.etNumber.setText(item.getActual_number());
        viewHolder.etNumber.setEnabled(item.getProduct_num().startsWith("HP") ? false : true);
        viewHolder.ivMinus.setVisibility(item.getProduct_num().startsWith("HP") ? View.GONE : View.VISIBLE);
        viewHolder.ivAdd.setVisibility(item.getProduct_num().startsWith("HP") ? View.GONE : View.VISIBLE);
        viewHolder.ll.setBackgroundColor(Integer.parseInt(item.getNumber()) != Integer.parseInt(item.getActual_number()) ? Color.RED : Color.WHITE);
        viewHolder.tvDelete.setTextColor(Integer.parseInt(item.getNumber()) != Integer.parseInt(item.getActual_number()) ? Color.WHITE : Color.RED);
        viewHolder.etNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    String in = ((EditText)v).getText().toString();
                    if(!in.equals("")) {
                        item.setActual_number(in);
                    } else {
                        item.setActual_number("0");
                    }
                }
            }
        });
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckStorageActivity)context).deleteProduct(position);
            }
        });
        viewHolder.ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(!item.getActual_number().equals("")) {
                    if(Integer.parseInt(item.getActual_number()) > 0) {
                        item.setActual_number((Integer.parseInt(item.getActual_number()) - 1) + "");
                    }
                    notifyDataSetChanged();
                }
            }
        });
        viewHolder.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(item.getActual_number().equals("")) {
                    item.setActual_number("1");
                } else {
//                    if(Integer.parseInt(item.getNumber()) <= Integer.parseInt(item.getActual_number())) {
//                        CustomViewUtil.createToast(context, "已达最大库存数，无法再增加！");
//                        return;
//                    }
                    item.setActual_number((Integer.parseInt(item.getActual_number()) + 1) + "");
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
    static class ViewHolder {
        LinearLayout ll;
        TextView tvName;
        TextView tvStock;
        TextView etNumber;
        TextView tvDelete;
        ImageView ivMinus;
        ImageView ivAdd;
    }
}
