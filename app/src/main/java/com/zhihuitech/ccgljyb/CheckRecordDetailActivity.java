package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.adapter.CheckStorageListAdapter;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class CheckRecordDetailActivity extends Activity {
    private ImageView ivBack;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private List<CheckStorageListItem> list = new ArrayList<CheckStorageListItem>();
    private ListView lv;
    private CheckStorageListAdapter adapter;

    private String id;
    private String local_name;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_record_detail);
        
        findViews();
        addListeners();
        initDatabase();

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        local_name = intent.getStringExtra("local_name");
        
        queryCheckRecordDetailFromDB();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_check_record_detail);
        lv = (ListView) findViewById(R.id.lv_check_list_check_record_detail);
    }

    private void queryCheckRecordDetailFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_checkitem where check_id=?", new String[]{id});
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                CheckStorageListItem item = new CheckStorageListItem();
                item.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                item.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                item.setNumber(c.getString(c.getColumnIndex("local_number")));
                item.setLocal_name(local_name);
                item.setActual_number(c.getString(c.getColumnIndex("actual_number")));
                item.setStatus(c.getString(c.getColumnIndex("status")));
                list.add(item);
            }
        }
        if(list != null && list.size() > 0) {
            adapter = new CheckStorageListAdapter(CheckRecordDetailActivity.this, list);
            lv.setAdapter(adapter);
        }
    }
}
