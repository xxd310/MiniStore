package com.zhihuitech.ccgljyb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zhihuitech.ccgljyb.adapter.ProductTypeListAdapter;
import com.zhihuitech.ccgljyb.entity.ProductType;
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

import static com.zhihuitech.ccgljyb.util.CustomViewUtil.dialog;

public class ProductTypeSettingActivity extends Activity {
    private ImageView ivBack;
    private EditText etTypeName;
    private Button btnAdd;
    private ListView mlv;
    private ProductTypeListAdapter adapter;
    private List<ProductType> list = new ArrayList<ProductType>();

    private MyApplication myApp;
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private final static int GET_TYPE_LIST = 0;
    private final static int TYPE_ADD = 1;
    private final static int TYPE_EDIT = 2;
    private final static int TYPE_DEL = 3;

    private int screenWidth;

    private ProductType currentPT;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_TYPE_LIST:
                    parseGetTypeListResult((String) msg.obj);
                    break;
                case TYPE_ADD:
                    CustomViewUtil.dismissDialog();
                    parseTypeAddResult((String) msg.obj);
                    break;
                case TYPE_EDIT:
                    CustomViewUtil.dismissDialog();
                    parseTypeEditResult((String) msg.obj);
                    break;
                case TYPE_DEL:
                    CustomViewUtil.dismissDialog();
                    parseTypeDelResult((String) msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_type_setting);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();
        getSystemScreenWidth();
        queryTypeListFromDB();
    }

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    /*@Override
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
    }*/

    private void queryTypeListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_product_category order by create_time desc", null);
        list.clear();
        if(c.getCount() > 0) {
            while (c.moveToNext()) {
                ProductType pt = new ProductType();
                pt.setId(c.getString(c.getColumnIndex("id")));
                pt.setName(c.getString(c.getColumnIndex("name")));
                pt.setCreate_time(c.getString(c.getColumnIndex("create_time")));
                pt.setIs_delete(c.getString(c.getColumnIndex("is_delete")));
                pt.setCreate_user(c.getString(c.getColumnIndex("create_user")));
                pt.setU_id(c.getString(c.getColumnIndex("u_id")));
                pt.setRemark(c.getString(c.getColumnIndex("remark")));
                list.add(pt);
            }
        }
        c.close();
        db.close();
        adapter = new ProductTypeListAdapter(ProductTypeSettingActivity.this, list);
        mlv.setAdapter(adapter);
    }

    private void parseTypeEditResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        for(int i = 0; i < list.size(); i++) {
                            if(list.get(i).getId().equals(currentPT.getId())) {
                                list.get(i).setName(currentPT.getName());
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseTypeDelResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        for(int i = 0; i < list.size(); i++) {
                            if(list.get(i).getId().equals(currentPT.getId())) {
                                list.remove(i);
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, resultObject.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseTypeAddResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        etTypeName.setText("");
                        new Thread() {
                            @Override
                            public void run() {
                                String result = DataProvider.getTypeList(myApp.getUser().getId());
                                Message msg = handler.obtainMessage();
                                msg.what = GET_TYPE_LIST;
                                msg.obj = result;
                                handler.sendMessage(msg);
                            }
                        }.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void parseGetTypeListResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONArray dataArray = resultObject.getJSONArray("data");
                            Gson gson = new Gson();
                            list = gson.fromJson(dataArray.toString(), new TypeToken<List<ProductType>>() {}.getType());
                        }
                        if(list != null && list.size() != 0) {
                            adapter = new ProductTypeListAdapter(ProductTypeSettingActivity.this, list);
                            mlv.setAdapter(adapter);
                        }
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
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etTypeName.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "请先输入类别！");
                    return;
                }
                insertTypeToDB();
            }
        });
    }

    private void insertTypeToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from tp_product_category where name=?", new String[]{etTypeName.getText().toString()});
            if(c.getCount() > 0) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "该类型名已经存在！");
                c.close();
                return;
            }
            c.close();
            ContentValues cv = new ContentValues();
            cv.put("name", etTypeName.getText().toString());
            cv.put("create_time", System.currentTimeMillis() / 1000);
            cv.put("create_user", myApp.getUser().getUser_name());
            cv.put("u_id", myApp.getUser().getId());
            cv.put("token", myApp.getUser().getToken());
            long count = db.insert("tp_product_category", null, cv);
            if(count != -1) {
                saveInsertDataToData(db);
                CustomViewUtil.createToast(ProductTypeSettingActivity.this, "新增成功！");
                etTypeName.setText("");
                queryTypeListFromDB();
            } else {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "新增失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void saveInsertDataToData(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from tp_product_category order by id desc limit 0,1", null);
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                itemObject.put("remark", c.getString(c.getColumnIndex("remark")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "insert");
                obj.put("table_name", "ProductCategory");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_product_type_setting);
        etTypeName = (EditText) findViewById(R.id.et_product_type_name_product_type_setting);
        btnAdd = (Button) findViewById(R.id.btn_add_product_type_product_type_setting);
        mlv = (ListView) findViewById(R.id.mlv_product_type_list_product_type_setting);
    }

    public void showTypeEditDialog(ProductType pt) {
        currentPT = pt;
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProductTypeSettingActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.edit_type_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int)(0.8 * screenWidth), (int)(0.6 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        final ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_edit_type_dialog);
        final EditText etNewTypeName = (EditText) view.findViewById(R.id.et_type_name_edit_type_dialog);
        etNewTypeName.setText(pt.getName());
        etNewTypeName.setSelection(pt.getName().length());
        final Button btnSubmit = (Button) view.findViewById(R.id.btn_submit_edit_type_dialog);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etNewTypeName.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "类别名不能为空！");
                    return;
                }
                boolean result = updateTypeToDB(etNewTypeName.getText().toString());
                if(result) {
                    dialog.dismiss();
                }
            }
        });
    }

    private boolean updateTypeToDB(String newName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from tp_product_category where name=?", new String[]{newName});
            if(c.getCount() > 0) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "该类型名已经存在！");
                c.close();
                return false;
            }
            c.close();
            ContentValues updatedValues = new ContentValues();
            updatedValues.put("name", newName);
            int count = db.update("tp_product_category", updatedValues, "id=?", new String[]{currentPT.getId()});
            if(count == 0) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "更新失败！");
                return false;
            } else {
                saveUpdateDataToData(db, currentPT.getId());
                CustomViewUtil.createToast(ProductTypeSettingActivity.this, "更新成功！");
                queryTypeListFromDB();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveUpdateDataToData(SQLiteDatabase db, String id) {
        Cursor c = db.rawQuery("select * from tp_product_category where id=?", new String[]{id});
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                itemObject.put("remark", c.getString(c.getColumnIndex("remark")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "ProductCategory");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void typeDelete(ProductType pt) {
        showConfirmDialog(pt);
//        currentPT = pt;
//        deleteTypeFromDB();
    }

    private void showConfirmDialog(final ProductType pt) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProductTypeSettingActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_confirm_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int)(0.8 * screenWidth), (int)(0.6 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_delete_confirm_dialog);
        Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm_delete_confirm_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_delete_confirm_dialog);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                currentPT = pt;
                deleteTypeFromDB();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }



    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void deleteTypeFromDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("select * from tp_local_product where category_id=?", new String[]{currentPT.getId()});
            if(c.getCount() > 0) {
                CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "该类别对应某些产品，无法删除！");
            } else {
                saveDeleteDataToData(db, currentPT.getId());
                int count = db.delete("tp_product_category", "id=?", new String[]{currentPT.getId()});
                if(count == 0) {
                    myApp.getDataArray().remove(myApp.getDataArray().length() - 1);
                    myApp.saveDataToPref();
                    CustomViewUtil.createErrorToast(ProductTypeSettingActivity.this, "删除失败！");
                } else {
                    CustomViewUtil.createToast(ProductTypeSettingActivity.this, "删除成功！");
                    queryTypeListFromDB();
                }
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void saveDeleteDataToData(SQLiteDatabase db, String id) {
        Cursor c = db.rawQuery("select * from tp_product_category where id=?", new String[]{id});
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                itemObject.put("remark", c.getString(c.getColumnIndex("remark")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "delete");
                obj.put("table_name", "ProductCategory");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
