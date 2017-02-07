package com.zhihuitech.ccgljyb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import static com.zhihuitech.ccgljyb.R.drawable.tel;
import static com.zhihuitech.ccgljyb.R.id.et_location_code_edit_location;

public class EditLocationActivity extends Activity {
    private ImageView ivBack;
    private EditText etCode;
    private EditText etName;
    private Button btnSubmit;
    private Button btnDelete;
    private Location location;

    private String locName;

    private final static int LOCATION_EDIT = 0;
    private final static int LOCATION_DELETE = 1;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private MyApplication myApp;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOCATION_EDIT:
                    CustomViewUtil.dismissDialog();
                    parseLocationEditResult((String) msg.obj);
                    break;
                case LOCATION_DELETE:
                    CustomViewUtil.dismissDialog();
                    parseLocationDelResult((String) msg.obj);
                    break;
            }
        }
    };

    private void parseLocationEditResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(EditLocationActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        CustomViewUtil.createToast(EditLocationActivity.this, resultObject.getString("message"));
                        finish();
                    } else {
                        CustomViewUtil.createErrorToast(EditLocationActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseLocationDelResult(String result) {
        if(result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(EditLocationActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        CustomViewUtil.createToast(EditLocationActivity.this, resultObject.getString("message"));
                        finish();
                    } else {
                        CustomViewUtil.createErrorToast(EditLocationActivity.this, resultObject.getString("message"));
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
        setContentView(R.layout.edit_location);

        myApp = (MyApplication) getApplication();
        Intent intent = getIntent();
        location = (Location) intent.getSerializableExtra("location");
        locName = location.getName();

        findViews();
        addListeners();
        initData();
        initDatabase();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void initData() {
        etCode.setText(location.getNum());
        etName.setText(location.getName());
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etName.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(EditLocationActivity.this, "请输入库位名！");
                    return;
                }
                if(locName.equals(etName.getText().toString())) {
                    CustomViewUtil.createErrorToast(EditLocationActivity.this, "库位名未修改，无需提交！");
                    return;
                }
                updateLocationToDB();
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteLocationFromDB();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void deleteLocationFromDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from tp_local_product where local_num = ? and number != '0'", new String[]{location.getId()});
            if(c.getCount() > 0) {
                CustomViewUtil.createErrorToast(EditLocationActivity.this, "该库位下还有产品，无法删除！");
            } else {
                saveDeleteDataToData(db, location.getId());
                int count = db.delete("tp_location", "id=?", new String[]{location.getId()});
                if(count == 0) {
                    myApp.getDataArray().remove(myApp.getDataArray().length() - 1);
                    myApp.saveDataToPref();
                    CustomViewUtil.createErrorToast(EditLocationActivity.this, "删除失败！");
                    c.close();
                } else {
                    CustomViewUtil.createToast(EditLocationActivity.this, "删除成功！");
                    c.close();
                    finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void saveDeleteDataToData(SQLiteDatabase db, String id) {
        Cursor c = db.rawQuery("select * from tp_location where id=?", new String[]{id});
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("num", c.getString(c.getColumnIndex("num")));
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "delete");
                obj.put("table_name", "Location");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLocationToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from tp_location where name=?", new String[]{etName.getText().toString()});
            if(c.getCount() > 0) {
                CustomViewUtil.createErrorToast(EditLocationActivity.this, "该库位名已存在！");
                return;
            }
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("name", etName.getText().toString());
            int count = db.update("tp_location", updatedValues, "num=?", new String[]{location.getNum()});
            if(count == 0) {
                CustomViewUtil.createErrorToast(EditLocationActivity.this, "更新失败！");
            } else {
                saveUpdateDataToData(db, location.getNum());
                CustomViewUtil.createToast(EditLocationActivity.this, "更新成功！");
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void saveUpdateDataToData(SQLiteDatabase db, String num) {
        Cursor c = db.rawQuery("select * from tp_location where num=?", new String[]{num});
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("num", c.getString(c.getColumnIndex("num")));
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "Location");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_edit_location);
        etCode = (EditText) findViewById(R.id.et_location_code_edit_location);
        etName = (EditText) findViewById(R.id.et_location_name_edit_location);
        btnSubmit = (Button) findViewById(R.id.btn_submit_edit_location);
        btnDelete = (Button) findViewById(R.id.btn_delete_edit_location);
    }
}
