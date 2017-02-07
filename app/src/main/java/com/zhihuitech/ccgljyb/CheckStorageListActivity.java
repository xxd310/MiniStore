package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.adapter.CheckStorageListAdapter;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;

import java.util.ArrayList;
import java.util.List;

public class CheckStorageListActivity extends Activity {
    private ImageView ivBack;
    private TextView tvAbnormal;
    private TextView tvNormal;
    private View vAbnormal;
    private View vNormal;
    private ListView lvAbnormal;
    private ListView lvNormal;
    private TextView tvNoResultAbnormal;
    private TextView tvNoResultNormal;
    private Button btnContinue;

    private List<CheckStorageListItem> list = new ArrayList<CheckStorageListItem>();
    private List<CheckStorageListItem> abnormalList = new ArrayList<CheckStorageListItem>();
    private List<CheckStorageListItem> normalList = new ArrayList<CheckStorageListItem>();
    private CheckStorageListAdapter abNormalAdapter;
    private CheckStorageListAdapter normalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_storage_list);
        
        findViews();
        addListeners();

        Intent intent = getIntent();
        list = (List<CheckStorageListItem>) intent.getSerializableExtra("list");

        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).getStatus().equals("1")) {
                normalList.add(list.get(i));
            } else {
                abnormalList.add(list.get(i));
            }
        }
        abNormalAdapter = new CheckStorageListAdapter(CheckStorageListActivity.this, abnormalList);
        lvAbnormal.setAdapter(abNormalAdapter);
        normalAdapter = new CheckStorageListAdapter(CheckStorageListActivity.this, normalList);
        lvNormal.setAdapter(normalAdapter);
        if(abnormalList.size() == 0) {
            tvNoResultAbnormal.setVisibility(View.VISIBLE);
            tvNoResultNormal.setVisibility(View.GONE);
            lvAbnormal.setVisibility(View.GONE);
        } else {
            tvNoResultAbnormal.setVisibility(View.GONE);
            tvNoResultNormal.setVisibility(View.GONE);
            lvAbnormal.setVisibility(View.VISIBLE);
        }
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvAbnormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAbnormal.setTextColor(0XFF2185C5);
                tvNormal.setTextColor(0XFF3E454C);
                vAbnormal.setVisibility(View.VISIBLE);
                vNormal.setVisibility(View.INVISIBLE);
                lvNormal.setVisibility(View.GONE);
                tvNoResultNormal.setVisibility(View.GONE);
                if(abnormalList.size() == 0) {
                    lvAbnormal.setVisibility(View.GONE);
                    tvNoResultAbnormal.setVisibility(View.VISIBLE);
                } else {
                    lvAbnormal.setVisibility(View.VISIBLE);
                    tvNoResultAbnormal.setVisibility(View.GONE);
                }
            }
        });
        tvNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvNormal.setTextColor(0XFF2185C5);
                tvAbnormal.setTextColor(0XFF3E454C);
                vNormal.setVisibility(View.VISIBLE);
                vAbnormal.setVisibility(View.INVISIBLE);
                lvAbnormal.setVisibility(View.GONE);
                tvNoResultAbnormal.setVisibility(View.GONE);
                if(normalList.size() == 0) {
                    lvNormal.setVisibility(View.GONE);
                    tvNoResultNormal.setVisibility(View.VISIBLE);
                } else {
                    lvNormal.setVisibility(View.VISIBLE);
                    tvNoResultNormal.setVisibility(View.GONE);
                }
            }
        });
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckStorageListActivity.this, LocationSelectActivity.class);
                intent.putExtra("from", "check");
                startActivity(intent);
                finish();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_check_storage_list);
        tvAbnormal = (TextView) findViewById(R.id.tv_abnormal_product_check_storage_list);
        tvNormal = (TextView) findViewById(R.id.tv_normal_product_check_storage_list);
        vAbnormal = findViewById(R.id.v_abnormal_product_check_storage_list);
        vNormal = findViewById(R.id.v_normal_product_check_storage_list);
        lvAbnormal = (ListView) findViewById(R.id.lv_abnormal_product_check_storage_list);
        lvNormal = (ListView) findViewById(R.id.lv_normal_product_check_storage_list);
        tvNoResultAbnormal = (TextView) findViewById(R.id.tv_no_abnormal_product_check_storage_list);
        tvNoResultNormal = (TextView) findViewById(R.id.tv_no_normal_product_check_storage_list);
        btnContinue = (Button) findViewById(R.id.btn_continue_check_storage_list);
    }
}
