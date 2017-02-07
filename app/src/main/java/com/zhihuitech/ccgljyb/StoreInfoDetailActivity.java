package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.entity.StoreInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StoreInfoDetailActivity extends Activity {
    private ImageView ivBack;
    private TextView tvProductName;
    private TextView tvProductNum;
    private TextView tvTypeName;
    private TextView tvLocalName;
    private TextView tvNumber;
    private TextView tvChangeTime;

    private StoreInfo si;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.store_info_detail);

        Intent intent = getIntent();
        si = (StoreInfo) intent.getSerializableExtra("store_info_detail");
        findViews();
        initContent();
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initContent() {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        tvProductName.setText(si.getProduct_name());
        tvProductNum.setText(si.getProduct_num());
        tvTypeName.setText((si.getCate_name() == null || si.getCate_name().equals("")) ? "全部类别" : si.getCate_name());
        tvLocalName.setText(si.getLocal_name());
        tvNumber.setText(si.getNumber());
        tvChangeTime.setText(sdf.format(new Date(Long.parseLong(si.getChange_time()) * 1000)));
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_store_info_detail);
        tvProductName = (TextView) findViewById(R.id.tv_product_name_store_info_detail);
        tvProductNum = (TextView) findViewById(R.id.tv_product_num_store_info_detail);
        tvTypeName = (TextView) findViewById(R.id.tv_type_name_store_info_detail);
        tvLocalName = (TextView) findViewById(R.id.tv_local_name_store_info_detail);
        tvNumber = (TextView) findViewById(R.id.tv_product_number_store_info_detail);
        tvChangeTime = (TextView) findViewById(R.id.tv_change_time_store_info_detail);
    }
}
