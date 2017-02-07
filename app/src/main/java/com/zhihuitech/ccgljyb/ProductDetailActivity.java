package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.zhihuitech.ccgljyb.adapter.ProductTypeUpdateListAdapter;
import com.zhihuitech.ccgljyb.entity.InOutRecord;
import com.zhihuitech.ccgljyb.entity.ProductType;
import com.zhihuitech.ccgljyb.entity.StoreInfo;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.id.list;

/**
 * Created by Administrator on 2016/11/23.
 */
public class ProductDetailActivity extends Activity {
    private ImageView ivBack;
    private LinearLayout llCreateTime;
    private TextView tvTimeTip;
    private TextView tvCreateTime;
    private TextView tvProductNameTip;
    private TextView tvProductName;
    private Button btnUpdateName;
    private TextView tvProductNumTip;
    private TextView tvProductNum;
    private TextView tvType;
    private Button btnUpdateType;
    private TextView tvLocalName;
    private TextView tvProductNumber;
    private TextView tvCreateUser;
    private LinearLayout llLastUpdateTime;
    private TextView tvLastUpdateTime;

    private String from;
    private InOutRecord ior;
    private StoreInfo si;
    private SimpleDateFormat sdf;
    private MyApplication myApp;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private List<ProductType> typeList = new ArrayList<ProductType>();
    private int screenWidth;
    private String code;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        initDatabase();
        getSystemScreenWidth();
        queryTypeListFromDB();

        Intent intent = getIntent();
        from = intent.getStringExtra("from");
        if(from.equals("store")) {
            si = (StoreInfo) intent.getSerializableExtra("store_info_detail");
            code = si.getProduct_num();
            name = si.getProduct_name();
            llCreateTime.setVisibility(View.GONE);
            llLastUpdateTime.setVisibility(View.VISIBLE);
            setContentForStoreDetail();
        } else {
            llCreateTime.setVisibility(View.VISIBLE);
            llLastUpdateTime.setVisibility(View.GONE);
            ior = (InOutRecord) intent.getSerializableExtra("in_out_record_detail");
            code = ior.getProduct_num();
            name = ior.getProduct_name();
            setContentForRecordDetail();
            if(intent.getStringExtra("from").equals("in")) {
                tvTimeTip.setText("入库时间");
            } else if(intent.getStringExtra("from").equals("out")) {
                tvTimeTip.setText("出库时间");
            }
        }
    }

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void setContentForStoreDetail() {
        tvProductNameTip.setText(si.getProduct_num().startsWith("HP") ? "货品名称" : "产品名称");
        tvProductNumTip.setText(si.getProduct_num().startsWith("HP") ? "货品编码" : "产品编码");
        tvProductName.setText(si.getProduct_name());
        tvProductNum.setText(si.getProduct_num());
        tvType.setText((si.getCate_name() == null || si.getCate_name().equals("")) ? "全部类别" : si.getCate_name());
        tvLocalName.setText(si.getLocal_name());
        tvProductNumber.setText(si.getNumber());
        tvLastUpdateTime.setText(sdf.format(new Date(Long.parseLong(si.getChange_time()) * 1000)));
        tvCreateUser.setText(myApp.getUser().getUser_name());
    }

    private void setContentForRecordDetail() {
        tvProductNameTip.setText(ior.getProduct_num().startsWith("HP") ? "货品名称" : "产品名称");
        tvProductNumTip.setText(ior.getProduct_num().startsWith("HP") ? "货品编码" : "产品编码");
        tvProductName.setText(ior.getProduct_name());
        tvProductNum.setText(ior.getProduct_num());
        tvCreateTime.setText(sdf.format(new Date(Long.parseLong(ior.getCreate_time()) * 1000)));
        tvType.setText((ior.getCategory() == null || ior.getCategory().equals("")) ? "全部类别" : ior.getCategory());
        tvLocalName.setText(ior.getLocal_name());
        tvProductNumber.setText(ior.getNumber());
        tvCreateUser.setText(ior.getCreate_user());
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_product_detail);
        llCreateTime = (LinearLayout) findViewById(R.id.ll_create_time_product_detail);
        tvTimeTip = (TextView) findViewById(R.id.tv_time_tip_product_detail);
        tvCreateTime = (TextView) findViewById(R.id.tv_create_time_product_detail);
        tvProductNameTip = (TextView) findViewById(R.id.tv_product_name_tip_product_detail);
        tvProductName = (TextView) findViewById(R.id.tv_product_name_product_detail);
        btnUpdateName = (Button) findViewById(R.id.btn_update_product_name_product_detail);
        tvProductNumTip = (TextView) findViewById(R.id.tv_product_num_tip_product_detail);
        tvProductNum = (TextView) findViewById(R.id.tv_product_num_product_detail);
        tvType = (TextView) findViewById(R.id.tv_type_name_product_detail);
        btnUpdateType = (Button) findViewById(R.id.btn_update_type_product_detail);
        tvLocalName = (TextView) findViewById(R.id.tv_local_name_product_detail);
        tvProductNumber = (TextView) findViewById(R.id.tv_product_number_product_detail);
        tvCreateUser = (TextView) findViewById(R.id.tv_create_user_product_detail);
        llLastUpdateTime = (LinearLayout) findViewById(R.id.ll_last_update_time_product_detail);
        tvLastUpdateTime = (TextView) findViewById(R.id.tv_last_update_time_product_detail);
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if(from.equals("in") || from.equals("out")) {
                    intent.putExtra("name", ior.getProduct_name());
                    intent.putExtra("category", ior.getCategory());
                } else {
                    intent.putExtra("name", si.getProduct_name());
                    intent.putExtra("category", si.getCate_name());
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        btnUpdateName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateNameDialog();
            }
        });
        btnUpdateType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(typeList.size() == 0) {
                    CustomViewUtil.createErrorToast(ProductDetailActivity.this, "暂无可选类别！");
                    return;
                }
                showUpdateTypeDialog();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent();
            if(from.equals("in") || from.equals("out")) {
                intent.putExtra("name", ior.getProduct_name());
                intent.putExtra("category", ior.getCategory());
            } else {
                intent.putExtra("name", si.getProduct_name());
                intent.putExtra("category", si.getCate_name());
            }
            setResult(333, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void queryTypeListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_product_category order by create_time desc", null);
        typeList.clear();
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
                typeList.add(pt);
            }
        }
        ProductType pt = new ProductType();
        pt.setId("0");
        pt.setName("全部类别");
        typeList.add(0, pt);
        c.close();
        db.close();
    }

    private void showUpdateNameDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.edit_product_name_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.8 * screenWidth), (int) (0.6 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_edit_product_name_dialog);
        final EditText etName = (EditText) view.findViewById(R.id.et_product_name_edit_product_name_dialog);
        Button btnSubmit = (Button) view.findViewById(R.id.btn_submit_edit_product_name_dialog);
        etName.setText(name);
        etName.setSelection(name.length());
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etName.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(ProductDetailActivity.this, "请输入名称！");
                    return;
                }
                dialog.dismiss();
                name = etName.getText().toString();
                if(from.equals("in") || from.equals("out")) {
                    ior.setProduct_name(name);
                } else {
                    si.setProduct_name(name);
                }
                tvProductName.setText(name);
                updateProductNameToDB();
            }
        });
    }

    private void showUpdateTypeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.update_type_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int)(0.8 * screenWidth), (int)(0.8 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ListView lv = (ListView) view.findViewById(R.id.lv_type_list_update_type_dialog);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_close_dialog_update_type_dialog);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ProductTypeUpdateListAdapter adapter = new ProductTypeUpdateListAdapter(ProductDetailActivity.this, typeList);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                if(from.equals("in") || from.equals("out")) {
                    ior.setCategory(typeList.get(position).getName());
                } else {
                    si.setCate_name(typeList.get(position).getName());
                }
                updateTypeToDB(typeList.get(position).getId());
                tvType.setText(typeList.get(position).getName());
            }
        });
    }

    private void updateProductNameToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cvLocalProduct = new ContentValues();
            cvLocalProduct.put("product_name", name);
            cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
            long rows = db.update("tp_local_product", cvLocalProduct, "product_num=?", new String[]{code});
            Cursor cLocalProduct = db.rawQuery("select * from tp_local_product where product_num=?", new String[]{code});
            while (cLocalProduct.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("product_name", cLocalProduct.getString(cLocalProduct.getColumnIndex("product_name")));
                itemObject.put("change_time", cLocalProduct.getString(cLocalProduct.getColumnIndex("change_time")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cLocalProduct.getString(cLocalProduct.getColumnIndex("id")));
                itemObject.put("token", cLocalProduct.getString(cLocalProduct.getColumnIndex("token")));
                itemObject.put("opposite_id", cLocalProduct.getString(cLocalProduct.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "LocalProduct");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
            ContentValues cv = new ContentValues();
            cv.put("product_name", name);
            rows = db.update("tp_in_storitem", cv, "product_num=?", new String[]{code});
            Cursor cInStoritem = db.rawQuery("select * from tp_in_storitem where product_num=?", new String[]{code});
            while (cInStoritem.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("product_name", cInStoritem.getString(cInStoritem.getColumnIndex("product_name")));
                itemObject.put("token", cInStoritem.getString(cInStoritem.getColumnIndex("token")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cInStoritem.getString(cInStoritem.getColumnIndex("id")));
                itemObject.put("opposite_id", cInStoritem.getString(cInStoritem.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "InStoritem");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
            rows = db.update("tp_out_storitem", cv, "product_num=?", new String[]{code});
            Cursor cOutStoritem = db.rawQuery("select * from tp_out_storitem where product_num=?", new String[]{code});
            while (cOutStoritem.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("product_name", cOutStoritem.getString(cOutStoritem.getColumnIndex("product_name")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cOutStoritem.getString(cOutStoritem.getColumnIndex("id")));
                itemObject.put("token", cOutStoritem.getString(cOutStoritem.getColumnIndex("token")));
                itemObject.put("opposite_id", cOutStoritem.getString(cOutStoritem.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "OutStoritem");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    private void updateTypeToDB(String typeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cvLocalProduct = new ContentValues();
            cvLocalProduct.put("category_id", typeId);
            cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
            long rows = db.update("tp_local_product", cvLocalProduct, "product_num=?", new String[]{code});
            Cursor cLocalProduct = db.rawQuery("select * from tp_local_product where product_num=?", new String[]{code});
            while (cLocalProduct.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("local_num", cLocalProduct.getString(cLocalProduct.getColumnIndex("local_num")));
                itemObject.put("category_id", cLocalProduct.getString(cLocalProduct.getColumnIndex("category_id")));
                itemObject.put("product_num", cLocalProduct.getString(cLocalProduct.getColumnIndex("product_num")));
                itemObject.put("product_name", cLocalProduct.getString(cLocalProduct.getColumnIndex("product_name")));
                itemObject.put("number", cLocalProduct.getString(cLocalProduct.getColumnIndex("number")));
                itemObject.put("u_id", cLocalProduct.getString(cLocalProduct.getColumnIndex("u_id")));
                itemObject.put("change_time", cLocalProduct.getString(cLocalProduct.getColumnIndex("change_time")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cLocalProduct.getString(cLocalProduct.getColumnIndex("id")));
                itemObject.put("token", cLocalProduct.getString(cLocalProduct.getColumnIndex("token")));
                itemObject.put("opposite_id", cLocalProduct.getString(cLocalProduct.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "LocalProduct");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
            ContentValues cv = new ContentValues();
            cv.put("category_id", typeId);
            rows = db.update("tp_in_storitem", cv, "product_num=?", new String[]{code});
            Cursor cInStoritem = db.rawQuery("select * from tp_in_storitem where product_num=?", new String[]{code});
            while (cInStoritem.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("category_id", cInStoritem.getString(cInStoritem.getColumnIndex("category_id")));
                itemObject.put("token", cInStoritem.getString(cInStoritem.getColumnIndex("token")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cInStoritem.getString(cInStoritem.getColumnIndex("id")));
                itemObject.put("opposite_id", cInStoritem.getString(cInStoritem.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "InStoritem");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
            rows = db.update("tp_out_storitem", cv, "product_num=?", new String[]{code});
            Cursor cOutStoritem = db.rawQuery("select * from tp_out_storitem where product_num=?", new String[]{code});
            while (cOutStoritem.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("category_id", cOutStoritem.getString(cOutStoritem.getColumnIndex("category_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cOutStoritem.getString(cOutStoritem.getColumnIndex("id")));
                itemObject.put("token", cOutStoritem.getString(cOutStoritem.getColumnIndex("token")));
                itemObject.put("opposite_id", cOutStoritem.getString(cOutStoritem.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "OutStoritem");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }
}
