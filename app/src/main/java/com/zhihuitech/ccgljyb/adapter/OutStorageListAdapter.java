package com.zhihuitech.ccgljyb.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.InStorageActivity;
import com.zhihuitech.ccgljyb.OutStorageActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.entity.InStorageListItem;
import com.zhihuitech.ccgljyb.entity.OutStorageListItem;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;

import java.util.List;

/**
 * Created by Administrator on 2016/10/27.
 */
public class OutStorageListAdapter extends BaseAdapter{
    private Context context;
    private List<OutStorageListItem> list;

    public OutStorageListAdapter(Context context, List<OutStorageListItem> list) {
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
            convertView = mInflater.inflate(R.layout.out_storage_list_item, null);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_product_name_out_storage_list_item);
            viewHolder.tvStock = (TextView) convertView.findViewById(R.id.tv_stock_out_storage_list_item);
            viewHolder.etNumber = (EditText) convertView.findViewById(R.id.et_product_number_out_storage_list_item);
            viewHolder.tvDelete = (TextView) convertView.findViewById(R.id.tv_delete_out_storage_list_item);
            viewHolder.ivMinus = (ImageView) convertView.findViewById(R.id.iv_minus_out_storage_list_item);
            viewHolder.ivAdd = (ImageView) convertView.findViewById(R.id.iv_add_out_storage_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final OutStorageListItem item = (OutStorageListItem) getItem(position);
        viewHolder.tvName.setText(item.getProduct_name());
        viewHolder.tvStock.setText(item.getNumber());
        viewHolder.etNumber.setText(item.getOut_number());
        viewHolder.etNumber.setEnabled(item.getProduct_num().startsWith("HP") ? false : true);
        viewHolder.ivMinus.setVisibility(item.getProduct_num().startsWith("HP") ? View.GONE : View.VISIBLE);
        viewHolder.ivAdd.setVisibility(item.getProduct_num().startsWith("HP") ? View.GONE : View.VISIBLE);
        viewHolder.etNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    String in = ((EditText)v).getText().toString();
                    if(!in.equals("")) {
                        item.setOut_number(in);
                    } else {
                        item.setOut_number("0");
                    }
                }
            }
        });
        viewHolder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((OutStorageActivity)context).deleteProduct(position);
            }
        });
        viewHolder.ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(!item.getOut_number().equals("")) {
                    if(Integer.parseInt(item.getOut_number()) > 0) {
                        item.setOut_number((Integer.parseInt(item.getOut_number()) - 1) + "");
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
                if(item.getOut_number().equals("")) {
                    item.setOut_number("1");
                } else {
                    if(Integer.parseInt(item.getNumber()) <= Integer.parseInt(item.getOut_number())) {
                        CustomViewUtil.createErrorToast(context, "已达最大库存数，无法再增加！");
                        return;
                    }
                    item.setOut_number((Integer.parseInt(item.getOut_number()) + 1) + "");
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }
    static class ViewHolder {
        TextView tvName;
        TextView tvStock;
        TextView etNumber;
        TextView tvDelete;
        ImageView ivMinus;
        ImageView ivAdd;
    }
}
