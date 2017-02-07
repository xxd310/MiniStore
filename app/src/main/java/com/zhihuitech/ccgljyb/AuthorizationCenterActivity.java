package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.adapter.AuthorizationRecordListAdapter;
import com.zhihuitech.ccgljyb.entity.AuthorizationRecord;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.HttpUtil;
import com.zhihuitech.ccgljyb.util.MD5;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuthorizationCenterActivity extends Activity {
    private ImageView ivBack;
    private TextView tvEndTime;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private MyApplication myApp;
    private ListView lvRecord;
    private List<AuthorizationRecord> list = new ArrayList<AuthorizationRecord>();
    private AuthorizationRecordListAdapter adapter;
    private Button btnUpdateAuth;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "ccgl";

    private String imei;

    private String appKey = "4654501791a54475b93e4e0834b30570";
    private String secretKey = "32faebaa8130cc06914b974b6427725b7440f727";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            CustomViewUtil.dismissDialog();
            parseAuthorizeRecordResult((String) msg.obj);
        }
    };

    private void parseAuthorizeRecordResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(AuthorizationCenterActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        CustomViewUtil.createToast(AuthorizationCenterActivity.this, "更新授权成功！");
                        JSONArray dataArray = resultObject.getJSONArray("data");
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("tp_authorize_record", null, null);
                        list.clear();
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            AuthorizationRecord ar = new AuthorizationRecord();
                            ar.setAuthorize_time(obj.getString("authorize_time"));
                            ar.setEnd_time(obj.getString("end_time"));
                            ar.setValidity_time(obj.getString("validity_time"));
                            list.add(ar);
                            ContentValues recordCV = new ContentValues();
                            recordCV.put("authorize_time", obj.getString("authorize_time"));
                            recordCV.put("validity_time", obj.getString("validity_time"));
                            recordCV.put("end_time", obj.getString("end_time"));
                            db.insert("tp_authorize_record", null, recordCV);
                        }
                        adapter.notifyDataSetChanged();
                        myApp.getUser().setEnd_time(list.get(0).getEnd_time());
                        tvEndTime.setText(sdf.format(new Date(Long.parseLong(myApp.getUser().getEnd_time()) * 1000)));
                        // 把最新的记录存入SharedPreference
                        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        SharedPreferences.Editor edit = pref.edit();
                        edit.putLong("end_time", Long.parseLong(list.get(0).getEnd_time()));
                        edit.commit();
                    } else {
//                        CustomViewUtil.createErrorToast(AuthorizationCenterActivity.this, resultObject.getString("message"));
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
        setContentView(R.layout.authorization_center);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
//        imei = "865959021323341";
        System.out.println("imei=" + imei);
        tvEndTime.setText(sdf.format(new Date(Long.parseLong(myApp.getUser().getEnd_time()) * 1000)));

        adapter = new AuthorizationRecordListAdapter(AuthorizationCenterActivity.this, list);
        lvRecord.setAdapter(adapter);

        queryAuthRecordFromDB();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void queryAuthRecordFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_authorize_record order by end_time desc", null);
        try {
            list.clear();
            while (c.moveToNext()) {
                AuthorizationRecord ar = new AuthorizationRecord();
                ar.setAuthorize_time(c.getString(c.getColumnIndex("authorize_time")));
                ar.setValidity_time(c.getString(c.getColumnIndex("validity_time")));
                ar.setEnd_time(c.getString(c.getColumnIndex("end_time")));
                list.add(ar);
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnUpdateAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!HttpUtil.checkConnection(AuthorizationCenterActivity.this)) {
                    CustomViewUtil.createErrorToast(AuthorizationCenterActivity.this, "请确保WIFI已经连接！");
                    return;
                } else {
                    CustomViewUtil.createProgressDialog(AuthorizationCenterActivity.this, "更新授权中，请稍后...");
                    new Thread() {
                        @Override
                        public void run() {
                            String timeStamp = System.currentTimeMillis() / 1000 + "";
                            String originSign = secretKey + "appKey" + appKey + "imei"+ imei + "timestamp" + timeStamp + secretKey;
                            String sign = MD5.GetMD5Code(originSign);

                            String result = DataProvider.authorizeRecord(imei, sign.toUpperCase(), timeStamp);
                            Message msg = handler.obtainMessage();
                            msg.obj = result;
                            handler.sendMessage(msg);
                        }
                    }.start();
                }
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_authorization_center);
        tvEndTime = (TextView) findViewById(R.id.tv_auth_end_time_authorization_center);
        lvRecord = (ListView) findViewById(R.id.lv_auth_record_authorization_center);
        btnUpdateAuth = (Button) findViewById(R.id.btn_update_authorization_center);
    }

}
