package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhihuitech.ccgljyb.adapter.CheckRecordListAdapter;
import com.zhihuitech.ccgljyb.adapter.InOutRecordListAdapter;
import com.zhihuitech.ccgljyb.entity.CheckRecord;
import com.zhihuitech.ccgljyb.entity.InOutRecord;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;

public class RecordListActivity extends Activity {
    private ImageView ivBack;
    private TextView tvInStorageTab;
    private TextView tvOutStorageTab;
    private TextView tvCheckStorageTab;
    private View vInStorage;
    private View vOutStorage;
    private View vCheckStorage;
    private ListView lvInStorage;
    private ListView lvOutStorage;
    private ListView lvCheckStorage;
    private TextView tvNoInRecord;
    private TextView tvNoOutRecord;
    private TextView tvNoCheckRecord;
    private LinearLayout llHeaderInOutRecord;
    private LinearLayout llHeaderCheckRecord;
    private List<InOutRecord> inRecordList = new ArrayList<InOutRecord>();
    private List<InOutRecord> outRecordList = new ArrayList<InOutRecord>();
    private List<CheckRecord> checkRecordList = new ArrayList<CheckRecord>();
    private InOutRecordListAdapter inAdapter;
    private InOutRecordListAdapter outAdapter;
    private CheckRecordListAdapter checkAdapter;

    private MyApplication myApp;

    private final static int RECORD = 0;
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;
    private int currentIndex = -1;
    private String currentProductCode = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD:
                    parseRecordResult((String) msg.obj);
                    break;
            }
        }
    };

    private void parseRecordResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(RecordListActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONObject dataObject = resultObject.getJSONObject("data");
                            if(!dataObject.isNull("in")) {
                                Gson gson = new Gson();
                                inRecordList = gson.fromJson(dataObject.getJSONArray("in").toString(), new TypeToken<List<InOutRecord>>() {}.getType());
                                if(inRecordList != null && inRecordList.size() > 0) {
                                    inAdapter = new InOutRecordListAdapter(RecordListActivity.this, inRecordList);
                                    lvInStorage.setAdapter(inAdapter);
                                    lvInStorage.setVisibility(View.VISIBLE);
                                    tvNoInRecord.setVisibility(View.GONE);
                                    lvInStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(RecordListActivity.this, InOutRecordDetailActivity.class);
                                            intent.putExtra("in_out_record_detail", inRecordList.get(position));
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    lvInStorage.setVisibility(View.GONE);
                                    tvNoInRecord.setVisibility(View.VISIBLE);
                                }
                            }
                            if(!dataObject.isNull("out")) {
                                Gson gson = new Gson();
                                outRecordList = gson.fromJson(dataObject.getJSONArray("out").toString(), new TypeToken<List<InOutRecord>>() {}.getType());
                                if(outRecordList != null && outRecordList.size() > 0) {
                                    outAdapter = new InOutRecordListAdapter(RecordListActivity.this, outRecordList);
                                    lvOutStorage.setAdapter(outAdapter);
                                    lvOutStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(RecordListActivity.this, InOutRecordDetailActivity.class);
                                            intent.putExtra("in_out_record_detail", outRecordList.get(position));
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        CustomViewUtil.createErrorToast(RecordListActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_list);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();
        
        queryInRecordFromDB();
        queryOutRecordFromDB();
        queryCheckRecordFromDB();
    }

    private void queryOutRecordFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select sid,screate_time,product_name,product_num,category_id,cate.name as category_name,loc_name,user as create_user,number from (select sto.id as sid,sto.create_time as screate_time,item.product_name,item.product_num,item.category_id,loc.name as loc_name,item.number,sto.create_user as user from tp_out_storage sto,tp_out_storitem item,tp_location loc where sto.id=item.out_id and loc.id=item.local_num)  a left outer join tp_product_category cate on a.category_id=cate.id order by screate_time desc", null);
        outRecordList.clear();
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                InOutRecord ior = new InOutRecord();
                ior.setId(c.getString(c.getColumnIndex("sid")));
                ior.setCreate_time(c.getString(c.getColumnIndex("screate_time")));
                ior.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                ior.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                ior.setCategory(c.getString(c.getColumnIndex("category_name")));
                ior.setLocal_name(c.getString(c.getColumnIndex("loc_name")));
                ior.setNumber(c.getString(c.getColumnIndex("number")));
                ior.setCreate_user(c.getString(c.getColumnIndex("create_user")));
                outRecordList.add(ior);
            }
        }
        c.close();
        db.close();
        if(outRecordList != null && outRecordList.size() > 0) {
            outAdapter = new InOutRecordListAdapter(RecordListActivity.this, outRecordList);
            lvOutStorage.setAdapter(outAdapter);
            lvOutStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent intent = new Intent(RecordListActivity.this, InOutRecordDetailActivity.class);
                    Intent intent = new Intent(RecordListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("from", "out");
                    intent.putExtra("in_out_record_detail", outRecordList.get(position));
                    currentIndex = position;
                    currentProductCode = outRecordList.get(position).getProduct_num();
                    startActivityForResult(intent, 111);
                }
            });
        }
    }

    private void queryInRecordFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select a.id as id,a.create_time as create_time,a.product_name as product_name,a.product_num as product_num,a.category_id as category_id,a.loc_name as loc_name,cate.name as cate_name,a.number as number,a.create_user as create_user from (select sto.id,sto.create_time,item.product_name,item.product_num,item.category_id,loc.name as loc_name,item.number,sto.create_user from tp_in_storage sto,tp_in_storitem item,tp_location loc where sto.id=item.in_id and loc.id=sto.local_num) a left outer join tp_product_category cate on cate.id=a.category_id order by a.create_time desc", null);
        inRecordList.clear();
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                InOutRecord ior = new InOutRecord();
                ior.setId(c.getString(c.getColumnIndex("id")));
                ior.setCreate_time(c.getString(c.getColumnIndex("create_time")));
                ior.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                ior.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                ior.setCategory(c.getString(c.getColumnIndex("cate_name")));
                ior.setLocal_name(c.getString(c.getColumnIndex("loc_name")));
                ior.setNumber(c.getString(c.getColumnIndex("number")));
                ior.setCreate_user(c.getString(c.getColumnIndex("create_user")));
                inRecordList.add(ior);
            }
        }
        c.close();
        db.close();
        if(inRecordList != null && inRecordList.size() > 0) {
            inAdapter = new InOutRecordListAdapter(RecordListActivity.this, inRecordList);
            lvInStorage.setAdapter(inAdapter);
            lvInStorage.setVisibility(View.VISIBLE);
            tvNoInRecord.setVisibility(View.GONE);
            lvInStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent intent = new Intent(RecordListActivity.this, InOutRecordDetailActivity.class);
                    Intent intent = new Intent(RecordListActivity.this, ProductDetailActivity.class);
                    intent.putExtra("from", "in");
                    intent.putExtra("in_out_record_detail", inRecordList.get(position));
                    currentIndex = position;
                    currentProductCode = inRecordList.get(position).getProduct_num();
                    startActivityForResult(intent, 222);
                }
            });
        } else {
            lvInStorage.setVisibility(View.GONE);
            tvNoInRecord.setVisibility(View.VISIBLE);
        }
    }

    private void queryCheckRecordFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select sto.id,sto.create_time,loc.name as local_name,sto.status from tp_check_stock sto,tp_location loc where sto.local_num=loc.id order by sto.create_time desc", null);
        try {
            checkRecordList.clear();
            if(c.getCount() > 0) {
                while (c.moveToNext()) {
                    CheckRecord cr = new CheckRecord();
                    cr.setId(c.getString(c.getColumnIndex("id")));
                    cr.setCreate_time(c.getString(c.getColumnIndex("create_time")));
                    cr.setLocal_name(c.getString(c.getColumnIndex("local_name")));
                    cr.setStatus(c.getString(c.getColumnIndex("status")));
                    checkRecordList.add(cr);
                }
            }
            if(checkRecordList != null && checkRecordList.size() > 0) {
                checkAdapter = new CheckRecordListAdapter(RecordListActivity.this, checkRecordList);
                lvCheckStorage.setAdapter(checkAdapter);
                lvCheckStorage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(RecordListActivity.this, CheckRecordDetailActivity.class);
                        intent.putExtra("id", checkRecordList.get(position).getId());
                        intent.putExtra("local_name", checkRecordList.get(position).getLocal_name());
                        startActivity(intent);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 111:
                for(int i = 0; i < outRecordList.size(); i++) {
                    if(currentProductCode.equals(outRecordList.get(i).getProduct_num())) {
                        outRecordList.get(i).setProduct_name(data.getStringExtra("name"));
                        outRecordList.get(i).setCategory(data.getStringExtra("category"));
                    }
                }
                outAdapter.notifyDataSetChanged();
                break;
            case 222:
                for(int i = 0; i < inRecordList.size(); i++) {
                    if(currentProductCode.equals(inRecordList.get(i).getProduct_num())) {
                        inRecordList.get(i).setProduct_name(data.getStringExtra("name"));
                        inRecordList.get(i).setCategory(data.getStringExtra("category"));
                    }
                }
                inAdapter.notifyDataSetChanged();
                break;
        }
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
        tvInStorageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inRecordList == null || inRecordList.size() == 0) {
                    tvNoInRecord.setVisibility(View.VISIBLE);
                    lvInStorage.setVisibility(View.GONE);
                } else {
                    tvNoInRecord.setVisibility(View.GONE);
                    lvInStorage.setVisibility(View.VISIBLE);
                }
                lvOutStorage.setVisibility(View.GONE);
                lvCheckStorage.setVisibility(View.GONE);
                vInStorage.setVisibility(View.VISIBLE);
                vOutStorage.setVisibility(View.INVISIBLE);
                vCheckStorage.setVisibility(View.INVISIBLE);
                tvInStorageTab.setTextColor(0XFF2185C5);
                tvOutStorageTab.setTextColor(0XFF3E454C);
                tvCheckStorageTab.setTextColor(0XFF3E454C);
                tvNoOutRecord.setVisibility(View.GONE);
                tvNoCheckRecord.setVisibility(View.GONE);
                llHeaderInOutRecord.setVisibility(View.VISIBLE);
                llHeaderCheckRecord.setVisibility(View.GONE);
            }
        });
        tvOutStorageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outRecordList == null || outRecordList.size() == 0) {
                    tvNoOutRecord.setVisibility(View.VISIBLE);
                    lvOutStorage.setVisibility(GONE);
                } else {
                    tvNoOutRecord.setVisibility(GONE);
                    lvOutStorage.setVisibility(View.VISIBLE);
                }
                lvInStorage.setVisibility(GONE);
                lvCheckStorage.setVisibility(View.GONE);
                vInStorage.setVisibility(View.INVISIBLE);
                vOutStorage.setVisibility(View.VISIBLE);
                vCheckStorage.setVisibility(View.INVISIBLE);
                tvInStorageTab.setTextColor(0XFF3E454C);
                tvOutStorageTab.setTextColor(0XFF2185C5);
                tvCheckStorageTab.setTextColor(0XFF3E454C);
                tvNoInRecord.setVisibility(View.GONE);
                tvNoCheckRecord.setVisibility(View.GONE);
                llHeaderInOutRecord.setVisibility(View.VISIBLE);
                llHeaderCheckRecord.setVisibility(View.GONE);
            }
        });
        tvCheckStorageTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkRecordList == null || checkRecordList.size() == 0) {
                    tvNoCheckRecord.setVisibility(View.VISIBLE);
                    lvCheckStorage.setVisibility(GONE);
                } else {
                    tvNoCheckRecord.setVisibility(GONE);
                    lvCheckStorage.setVisibility(View.VISIBLE);
                }
                lvInStorage.setVisibility(GONE);
                lvOutStorage.setVisibility(View.GONE);
                vInStorage.setVisibility(View.INVISIBLE);
                vOutStorage.setVisibility(View.INVISIBLE);
                vCheckStorage.setVisibility(View.VISIBLE);
                tvInStorageTab.setTextColor(0XFF3E454C);
                tvOutStorageTab.setTextColor(0XFF3E454C);
                tvCheckStorageTab.setTextColor(0XFF2185C5);
                tvNoInRecord.setVisibility(View.GONE);
                tvNoOutRecord.setVisibility(View.GONE);
                llHeaderInOutRecord.setVisibility(View.GONE);
                llHeaderCheckRecord.setVisibility(View.VISIBLE);
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_record_list);
        tvInStorageTab = (TextView) findViewById(R.id.tv_in_storage_record_list);
        tvOutStorageTab = (TextView) findViewById(R.id.tv_out_storage_record_list);
        tvCheckStorageTab = (TextView) findViewById(R.id.tv_check_storage_record_list);
        vInStorage = findViewById(R.id.v_in_storage_record_list);
        vOutStorage = findViewById(R.id.v_out_storage_record_list);
        vCheckStorage = findViewById(R.id.v_check_storage_record_list);
        lvInStorage = (ListView) findViewById(R.id.lv_in_storage_record_list);
        lvOutStorage = (ListView) findViewById(R.id.lv_out_storage_record_list);
        lvCheckStorage = (ListView) findViewById(R.id.lv_check_storage_record_list);
        tvNoInRecord = (TextView) findViewById(R.id.tv_no_in_storage_record_record_list);
        tvNoOutRecord = (TextView) findViewById(R.id.tv_no_out_storage_record_record_list);
        tvNoCheckRecord = (TextView) findViewById(R.id.tv_no_check_storage_record_record_list);
        llHeaderInOutRecord = (LinearLayout) findViewById(R.id.ll_header_in_out_storage_record_list);
        llHeaderCheckRecord = (LinearLayout) findViewById(R.id.ll_header_check_storage_record_list);
    }
}
