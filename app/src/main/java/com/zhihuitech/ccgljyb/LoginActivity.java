package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.victor.loading.rotate.RotateLoading;
import com.zhihuitech.ccgljyb.entity.User;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.HttpUtil;
import com.zhihuitech.ccgljyb.util.MD5;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;

public class LoginActivity extends Activity {
    private String imei;
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;
    private MyApplication myApp;
    private LinearLayout llAuthorization;
    private RotateLoading rl;
    private TextView tvNoResult;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "ccgl";

    private String userId;
    private String username;
    private String storeName;
    private boolean first_in;
    private long endTime;
    private long lastSyncTime = 0;
    private String data;
    private String token;

    private final static int LOGIN = 0;
    private final static int GET_SYNC_TIME = 1;

    private String appKey = "4654501791a54475b93e4e0834b30570";
    private String secretKey = "32faebaa8130cc06914b974b6427725b7440f727";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        myApp = (MyApplication) getApplication();
        llAuthorization = (LinearLayout) findViewById(R.id.ll_authorization_login);
        tvNoResult = (TextView) findViewById(R.id.tv_no_result_tip_login);
        rl = (RotateLoading) findViewById(R.id.rotateloading_login);
        rl.start();

        getInfoFromPref();
        myApp = (MyApplication) getApplication();
        initDatabase();

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
//        imei = "865959021323341";
        System.out.println("imei=" + imei);

        // 如果是第一次进入这个App，查询授权信息
        if(first_in) {
            if(!HttpUtil.checkConnection(LoginActivity.this)) {
                tvNoResult.setText("WIFI连接不正常\n请确保WIFI连接正常\n再重试");
                llAuthorization.setVisibility(View.GONE);
                return;
            } else {
                tvNoResult.setVisibility(View.GONE);
                llAuthorization.setVisibility(View.VISIBLE);
            }
            new Thread() {
                @Override
                public void run() {
                    String timeStamp = System.currentTimeMillis() / 1000 + "";
                    String originSign = secretKey + "appKey" + appKey + "imei"+ imei + "model" + Build.MODEL + "timestamp" + timeStamp + "version" + Build.VERSION.RELEASE + secretKey;
                    String sign = MD5.GetMD5Code(originSign);
                    String result = DataProvider.login(imei, Build.MODEL, Build.VERSION.RELEASE, timeStamp, sign.toUpperCase());
                    Message msg = handler.obtainMessage();
                    msg.what = LOGIN;
                    msg.obj = result;
                    handler.sendMessage(msg);
                }
            }.start();
        } else {
            try {
                JSONArray dataArray = new JSONArray(data);
                myApp.setDataArray(dataArray);
                if(lastSyncTime == 0) {
                    if (HttpUtil.checkConnection(this)) {
                        new Thread() {
                            @Override
                            public void run() {
                                URL url = null;//取得资源对象
                                try {
                                    url = new URL("http://www.baidu.com");
                                    URLConnection uc = url.openConnection();//生成连接对象
                                    uc.connect(); //发出连接
                                    long timeStamp = uc.getDate(); //取得网站日期时间
                                    Message msg = handler.obtainMessage();
                                    msg.obj = timeStamp;
                                    msg.what = GET_SYNC_TIME;
                                    handler.sendMessage(msg);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } else {
                        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putLong("last_sync_time", System.currentTimeMillis());
                        edit.commit();
                    }
                }
                User u = new User();
                u.setId(userId);
                u.setStore_name(storeName);
                u.setUser_name(username);
                u.setEnd_time(endTime + "");
                u.setToken(token);
                myApp.setUser(u);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void getInfoFromPref() {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        first_in = pref.getBoolean("first_in", true);
        userId = pref.getString("userId", "0");
        endTime = pref.getLong("end_time", System.currentTimeMillis() / 1000);
        username = pref.getString("username", "");
        storeName = pref.getString("storeName", "");
        token = pref.getString("token", "");
        data = pref.getString("data", "[]");
        lastSyncTime = pref.getLong("last_sync_time", 0);
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN:
                    parseLoginResult((String) msg.obj);
                    break;
                case GET_SYNC_TIME:
                    pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putLong("last_sync_time", Long.parseLong(msg.obj.toString()));
                    edit.commit();
                    break;
            }
        }
    };

    private void parseLoginResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                tvNoResult.setVisibility(View.VISIBLE);
                tvNoResult.setText(result);
                llAuthorization.setVisibility(View.GONE);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        JSONObject dataObject = resultObject.getJSONObject("data");
                        if(dataObject.has("current_time") && !dataObject.isNull("current_time")) {
                            lastSyncTime = dataObject.getLong("current_time");
                        }
                        // 试用
                        if(resultObject.isNull("record")) {
                            if(dataObject.has("end_time") && !dataObject.isNull("end_time")) {
                                endTime = dataObject.getLong("end_time");
                            } else {
                                endTime = System.currentTimeMillis() / 1000;
                            }
                        } else {
                            // 授权记录
                            JSONArray recordArray = resultObject.getJSONArray("record");
                            db.delete("tp_authorize_record", null, null);
                            for(int i = 0; i < recordArray.length(); i++) {
                                JSONObject obj = recordArray.getJSONObject(i);
                                if(i == 0) {
                                    endTime = obj.getLong("end_time");
                                }
                                ContentValues recordCV = new ContentValues();
                                recordCV.put("authorize_time", obj.getString("authorize_time"));
                                recordCV.put("validity_time", obj.getString("validity_time"));
                                recordCV.put("end_time", obj.getString("end_time"));
                                db.insert("tp_authorize_record", null, recordCV);
                            }
                        }
                        Cursor c = db.rawQuery("select * from tp_storage_user", null);
                        // 如果user表中没有记录
                        if(c.getCount() == 0) {
                            // 用户信息
                            ContentValues cv = new ContentValues();
                            cv.put("user_name", dataObject.getString("user_name"));
                            cv.put("name", dataObject.getString("store_name"));
                            cv.put("token", dataObject.getString("token"));
                            long rowId = db.insert("tp_storage_user", null, cv);
                            if(rowId != -1) {
                                c = db.rawQuery("select * from tp_storage_user", null);
                            }
                        }
                        while (c.moveToNext()) {
                            first_in = false;
                            userId = c.getString(c.getColumnIndex("id"));
                            username = c.getString(c.getColumnIndex("user_name"));
                            storeName = c.getString(c.getColumnIndex("name"));
                            token = c.getString(c.getColumnIndex("token"));
                            try {
                                JSONArray dataArray = new JSONArray("[]");
                                myApp.setDataArray(dataArray);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            saveInfoToPref();
                            User user = new User();
                            user.setId(c.getString(c.getColumnIndex("id")));
                            user.setStore_name(c.getString(c.getColumnIndex("name")));
                            user.setUser_name(c.getString(c.getColumnIndex("user_name")));
                            user.setEnd_time(endTime + "");
                            user.setToken(c.getString(c.getColumnIndex("token")));
                            myApp.setUser(user);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        tvNoResult.setVisibility(View.VISIBLE);
                        tvNoResult.setText(resultObject.getString("message"));
                        llAuthorization.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveInfoToPref() {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putBoolean("first_in", false);
        edit.putLong("end_time", endTime);
        edit.putString("storeName", storeName);
        edit.putString("userId", userId);
        edit.putString("username", username);
        edit.putString("token", token);
        edit.putLong("last_sync_time", lastSyncTime);
        edit.commit();
    }
}
