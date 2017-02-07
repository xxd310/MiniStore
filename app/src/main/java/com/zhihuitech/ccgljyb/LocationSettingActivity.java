package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhihuitech.ccgljyb.adapter.LocationListAdapter;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.entity.User;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.MyListView;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LocationSettingActivity extends Activity {
    private MyListView mlv;
    private LocationListAdapter adapter;
    private List<Location> locationList = new ArrayList<Location>();
    private ImageView ivBack;
    private ImageView ivAdd;

    private MyApplication myApp;
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private final static int GET_LOCATION_LIST = 0;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LOCATION_LIST:
                    parseGetLocationListResult((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_setting);

        initDatabase();
        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void queryLocationListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_location", null);
        locationList.clear();
        if(c.getCount() > 0) {
            locationList.clear();
            while (c.moveToNext()) {
                Location location = new Location();
                location.setId(c.getString(c.getColumnIndex("id")));
                location.setName(c.getString(c.getColumnIndex("name")));
                location.setIs_delete(c.getString(c.getColumnIndex("is_delete")));
                location.setCreate_time(c.getString(c.getColumnIndex("create_time")));
                location.setNum(c.getString(c.getColumnIndex("num")));
                location.setU_id(c.getString(c.getColumnIndex("u_id")));
                locationList.add(location);
            }
        }
        adapter = new LocationListAdapter(LocationSettingActivity.this, locationList);
        mlv.setAdapter(adapter);
        c.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        queryLocationListFromDB();
    }

    private void parseGetLocationListResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(LocationSettingActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONArray dataArray = resultObject.getJSONArray("data");
                            Gson gson = new Gson();
                            locationList = gson.fromJson(dataArray.toString(), new TypeToken<List<Location>>() {}.getType());
                            if(locationList != null && locationList.size() != 0) {
                                adapter = new LocationListAdapter(LocationSettingActivity.this, locationList);
                                mlv.setAdapter(adapter);
                            }
                        }
                    } else {
                        CustomViewUtil.createErrorToast(LocationSettingActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationSettingActivity.this, AddLocationActivity.class);
                startActivity(intent);
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_location_setting);
        mlv = (MyListView) findViewById(R.id.mlv_location_list_location_setting);
        ivAdd = (ImageView) findViewById(R.id.iv_add_location_location_setting);
    }
}
