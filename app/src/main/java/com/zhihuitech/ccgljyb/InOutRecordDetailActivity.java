package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.entity.InOutRecord;

import java.text.SimpleDateFormat;
import java.util.Date;

public class InOutRecordDetailActivity extends Activity {
    private ImageView ivBack;
    private TextView tvCreateTime;
    private TextView tvProductName;
    private TextView tvProductNum;
    private TextView tvTypeName;
    private TextView tvLocalName;
    private TextView tvNumber;
    private TextView tvCreateUser;

    private InOutRecord ior;
    private SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_out_record_detail);

        Intent intent = getIntent();
        ior = (InOutRecord) intent.getSerializableExtra("in_out_record_detail");

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
        tvCreateTime.setText(sdf.format(new Date(Long.parseLong(ior.getCreate_time()) * 1000)));
        tvProductName.setText(ior.getProduct_name());
        tvProductNum.setText(ior.getProduct_num());
        tvTypeName.setText((ior.getCategory() == null || ior.getCategory().equals("")) ? "全部类别" : ior.getCategory());
        tvLocalName.setText(ior.getLocal_name());
        tvNumber.setText(ior.getNumber());
        tvCreateUser.setText(ior.getCreate_user());
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_in_out_record_detail);
        tvCreateTime = (TextView) findViewById(R.id.tv_create_time_in_out_record_detail);
        tvProductName = (TextView) findViewById(R.id.tv_product_name_in_out_record_detail);
        tvProductNum = (TextView) findViewById(R.id.tv_product_num_in_out_record_detail);
        tvTypeName = (TextView) findViewById(R.id.tv_type_name_in_out_record_detail);
        tvLocalName = (TextView) findViewById(R.id.tv_local_name_in_out_record_detail);
        tvNumber = (TextView) findViewById(R.id.tv_product_number_in_out_record_detail);
        tvCreateUser = (TextView) findViewById(R.id.tv_create_user_in_out_record_detail);
    }
}
