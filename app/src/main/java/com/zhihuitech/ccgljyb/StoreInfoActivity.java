package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhihuitech.ccgljyb.adapter.StoreInfoListAdapter;
import com.zhihuitech.ccgljyb.adapter.LocationDropDownListAdapter;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.entity.StoreInfo;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreInfoActivity extends Activity {
    private ImageView ivBack;
    private ListView lvStoreInfoList;
    private List<StoreInfo> allStoreInfoList = new ArrayList<StoreInfo>();

    private List<StoreInfo> storeInfoList = new ArrayList<StoreInfo>();
    private StoreInfoListAdapter storeInfoListAdapter;

    private EditText etQueryName;
    private Button btnQuery;
    private LinearLayout llButton;
    private Button btn;
    private TextView tvTip;

    private PopupWindow popupWindow;
    private List<Location> locationList = new ArrayList<Location>();
    private ListView lvLocationList;
    private LocationDropDownListAdapter locationAdapter;

    private MyApplication myApp;
    private final static int GET_LOCATION_LIST = 0;
    private final static int CHECK_STORAGE = 1;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private String currentLocation = "全部库位";

    private int currentIndex = -1;
    private String currentProductCode = "";

    private TextView tvTotalNumber;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_LOCATION_LIST:
                    parseGetLocationListResult((String)msg.obj);
                    break;
                case CHECK_STORAGE:
                    parseCheckStorageResult((String)msg.obj);
                    break;
            }
        }
    };

    private void parseCheckStorageResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(StoreInfoActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONArray dataArray = resultObject.getJSONArray("data");
                            Gson gson = new Gson();
                            allStoreInfoList = gson.fromJson(dataArray.toString(), new TypeToken<List<CheckStorageListItem>>() {}.getType());
                            storeInfoList.addAll(allStoreInfoList);
                            storeInfoListAdapter = new StoreInfoListAdapter(StoreInfoActivity.this, storeInfoList);
                            lvStoreInfoList.setAdapter(storeInfoListAdapter);
                            lvStoreInfoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // 跳转到详情页
//                                    Intent intent = new Intent(StoreInfoActivity.this, StoreInfoDetailActivity.class);
                                    Intent intent = new Intent(StoreInfoActivity.this, ProductDetailActivity.class);
                                    intent.putExtra("from", "store");
                                    intent.putExtra("store_info_detail", storeInfoList.get(position));
                                    startActivity(intent);
                                }
                            });
                        }
                    } else {
                        CustomViewUtil.createErrorToast(StoreInfoActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void parseGetLocationListResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(StoreInfoActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONArray dataArray = resultObject.getJSONArray("data");
                            Gson gson = new Gson();
                            locationList = gson.fromJson(dataArray.toString(), new TypeToken<List<Location>>() {}.getType());
                            if(locationList != null) {
                                Location location = new Location();
                                location.setName("全部库位");
                                locationList.add(location);
                            }
                            btn.setEnabled(true);
                        }
                    } else {
                        CustomViewUtil.createErrorToast(StoreInfoActivity.this, resultObject.getString("message"));
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
        setContentView(R.layout.store_info);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();

        queryLocationListFromDB();
        queryStoreInfoListFromDB();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    // 判定是否需要隐藏
    private boolean isHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top
                    && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    // 隐藏软键盘
    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void queryStoreInfoListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select a.id as id,a.product_name as product_name,a.product_num as product_num,a.local_name as local_name,a.number as number,cate.name as cate_name,a.category_id as category_id,a.change_time as change_time from (select pro.id,pro.product_name,pro.product_num,loc.name as local_name,pro.number,pro.change_time,pro.category_id from tp_local_product pro,tp_location loc where pro.local_num=loc.id) a left outer join tp_product_category cate on cate.id=a.category_id order by change_time desc", null);
        if(c.getCount() > 0) {
            allStoreInfoList.clear();
            while (c.moveToNext()) {
                StoreInfo si = new StoreInfo();
                si.setId(c.getString(c.getColumnIndex("id")));
                si.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                si.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                si.setLocal_name(c.getString(c.getColumnIndex("local_name")));
                si.setCate_name(c.getString(c.getColumnIndex("cate_name")));
                si.setNumber(c.getString(c.getColumnIndex("number")));
                si.setChange_time(c.getString(c.getColumnIndex("change_time")));
                allStoreInfoList.add(si);
            }
        }
        c.close();
        db.close();
        storeInfoList.addAll(allStoreInfoList);
        storeInfoListAdapter = new StoreInfoListAdapter(StoreInfoActivity.this, storeInfoList);
        lvStoreInfoList.setAdapter(storeInfoListAdapter);
        calculateTotalNumber();
        if(storeInfoList.size() == 0) {
            tvTip.setVisibility(View.VISIBLE);
            lvStoreInfoList.setVisibility(View.GONE);
        } else {
            tvTip.setVisibility(View.GONE);
            lvStoreInfoList.setVisibility(View.VISIBLE);
        }
        lvStoreInfoList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 跳转到详情页
//                Intent intent = new Intent(StoreInfoActivity.this, StoreInfoDetailActivity.class);
                Intent intent = new Intent(StoreInfoActivity.this, ProductDetailActivity.class);
                intent.putExtra("from", "store");
                intent.putExtra("store_info_detail", storeInfoList.get(position));
                currentProductCode = storeInfoList.get(position).getProduct_num();
                currentIndex = position;
                startActivityForResult(intent, 333);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 333:
                for(int i = 0; i < allStoreInfoList.size(); i++) {
                    if(currentProductCode.equals(allStoreInfoList.get(i).getProduct_num())) {
                        allStoreInfoList.get(i).setProduct_name(data.getStringExtra("name"));
                        allStoreInfoList.get(i).setCate_name(data.getStringExtra("category"));
                    }
                }
//                for(int i = 0; i < storeInfoList.size(); i++) {
//                    if(currentProductCode.equals(storeInfoList.get(i).getProduct_num())) {
//                        storeInfoList.get(i).setProduct_name(data.getStringExtra("name"));
//                        storeInfoList.get(i).setCate_name(data.getStringExtra("category"));
//                    }
//                }
                storeInfoListAdapter.notifyDataSetChanged();
                calculateTotalNumber();
                break;
        }
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
            if(locationList != null) {
                Location location = new Location();
                location.setName("全部库位");
                locationList.add(location);
            }
            btn.setEnabled(true);
        }
        c.close();
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortDisplayContentByProductName(etQueryName.getText().toString());
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(locationList == null || locationList.size() == 0) {
                    return;
                }
                if(popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return;
                }
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.location_popupwindow, null);
                lvLocationList = (ListView) view.findViewById(R.id.lv_location_list);
                locationAdapter = new LocationDropDownListAdapter(StoreInfoActivity.this, locationList);
                lvLocationList.setAdapter(locationAdapter);
                popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                int width = llButton.getWidth();
                popupWindow.setWidth(width);
                popupWindow.showAtLocation(llButton, Gravity.BOTTOM, 0, llButton.getHeight() + 3);
                popupWindow.setFocusable(true);
            }
        });
        etQueryName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                sortDisplayContentByProductName(s.toString());
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_store_info);
        lvStoreInfoList = (ListView) findViewById(R.id.lv_store_info_list_store_info);
        llButton = (LinearLayout) findViewById(R.id.ll_check_storage_store_info);
        btn = (Button) findViewById(R.id.btn_check_storage_store_info);
        tvTip = (TextView) findViewById(R.id.tv_tip_store_info);
        etQueryName = (EditText) findViewById(R.id.et_query_name_store_info);
        btnQuery = (Button) findViewById(R.id.btn_query_store_info);
        tvTotalNumber = (TextView) findViewById(R.id.tv_product_total_number_store_info);
    }

    public void dismissPopupWindow(String text) {
        btn.setText(text);
        currentLocation = text;
        popupWindow.dismiss();
        sortDisplayContentByLocationName(currentLocation);
    }

    private void sortDisplayContentByLocationName(String localName) {
        storeInfoList.clear();
        if(localName.equals("全部库位")) {
            storeInfoList.addAll(allStoreInfoList);
        } else {
            for(int i = 0; i < allStoreInfoList.size(); i++) {
                if(localName.equals(allStoreInfoList.get(i).getLocal_name())) {
                    storeInfoList.add(allStoreInfoList.get(i));
                }
            }
        }
        storeInfoListAdapter.notifyDataSetChanged();
        calculateTotalNumber();
        if(storeInfoList.size() == 0) {
            tvTip.setVisibility(View.VISIBLE);
            lvStoreInfoList.setVisibility(View.GONE);
        } else {
            tvTip.setVisibility(View.GONE);
            lvStoreInfoList.setVisibility(View.VISIBLE);
        }
    }

    private void sortDisplayContentByProductName(String productName) {
        storeInfoList.clear();
        List<StoreInfo> tempList = new ArrayList<StoreInfo>();
        if(currentLocation.equals("全部库位")) {
            tempList.addAll(allStoreInfoList);
        } else {
            for(int i = 0; i < allStoreInfoList.size(); i++) {
                if(currentLocation.equals(allStoreInfoList.get(i).getLocal_name())) {
                    tempList.add(allStoreInfoList.get(i));
                }
            }
        }
        for(int i = 0; i < tempList.size(); i++) {
            if(tempList.get(i).getProduct_name().contains(productName)) {
                storeInfoList.add(tempList.get(i));
            }
        }
        storeInfoListAdapter.notifyDataSetChanged();
        calculateTotalNumber();
        if(storeInfoList.size() == 0) {
            tvTip.setVisibility(View.VISIBLE);
            lvStoreInfoList.setVisibility(View.GONE);
        } else {
            tvTip.setVisibility(View.GONE);
            lvStoreInfoList.setVisibility(View.VISIBLE);
        }
    }

    private void calculateTotalNumber() {
        long total = 0;
        for(int i = 0; i < storeInfoList.size(); i++) {
            total += Long.parseLong(storeInfoList.get(i).getNumber());
        }
        tvTotalNumber.setText(total + "");
    }

}
