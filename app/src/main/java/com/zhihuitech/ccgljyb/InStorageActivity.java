package com.zhihuitech.ccgljyb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.zhihuitech.ccgljyb.adapter.InStorageListAdapter;
import com.zhihuitech.ccgljyb.adapter.TypeDropDownListAdapter;
import com.zhihuitech.ccgljyb.entity.InStorageListItem;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.entity.ProductType;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.zhihuitech.ccgljyb.R.id.iv_minus_add_new_product_dialog;

public class InStorageActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code;
    private boolean isScanning = false;

    private ImageView ivBack;
    private TextView tvCurrentLocation;
    private Location location;
    private TextView tvTip;
    private ListView lv;
    private Button btnScan;
    private Button btnFinish;

    private int screenWidth;
    private PopupWindow popupWindow;
    private TextView tvProductType;
    private List<ProductType> typeList = new ArrayList<ProductType>();
    private int selectedTypeIndex = -1;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private List<InStorageListItem> itemList = new ArrayList<InStorageListItem>();
    private InStorageListAdapter listAdapter;

    private MyApplication myApp;

    private AlertDialog dialog;

    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(isScanning) {
                byte[] barcode = intent.getByteArrayExtra(ScanManager.DECODE_DATA_TAG);
                int barCodeLength = intent.getIntExtra(ScanManager.BARCODE_LENGTH_TAG, 0);
                code = new String(barcode, 0, barCodeLength);
                mVibrator.vibrate(100);
                mScanManager.stopDecode();
                isScanning = false;
                if (!TextUtils.isEmpty(code)) {
                    if (r.isPlaying()) {
                        r.stop();
                    }
                    r.play();
                    if(code.startsWith("KW")) {
                        CustomViewUtil.createErrorToast(InStorageActivity.this, "您扫到的是库位码！");
                        return;
                    }
                    if(code.startsWith("HP")) {
                        for(int i = 0; i < itemList.size(); i++) {
                            if(itemList.get(i).getProduct_num().equals(code)) {
                                CustomViewUtil.createErrorToast(InStorageActivity.this, "该货品已经在入库列表中！");
                                return;
                            }
                        }
                    }
                    queryProductByCodeFromDB();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_storage);

        myApp = (MyApplication) getApplication();
        initScan();
        initDatabase();
        findViews();
        addListeners();
        Intent intent = getIntent();
        if(intent.hasExtra("location")) {
            location = (Location) intent.getSerializableExtra("location");
            tvCurrentLocation.setText(location.getName());
        }
        getSystemScreenWidth();
        queryTypeListFromDB();

        listAdapter = new InStorageListAdapter(InStorageActivity.this, itemList);
        lv.setAdapter(listAdapter);
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    private void queryTypeListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_product_category", null);
        typeList.clear();
        if (c.getCount() > 0) {
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
        ProductType defaultPt = new ProductType();
        defaultPt.setId("0");
        defaultPt.setName("全部类别");
        typeList.add(0, defaultPt);
        c.close();
        db.close();
    }

    private void initScan() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mScanManager = new ScanManager();
        mScanManager.openScanner();
        mScanManager.switchOutputMode(0);
        Uri notification = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wet);
        r = RingtoneManager.getRingtone(this, notification);
        IntentFilter filter = new IntentFilter();
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null && value_buf[0] != null && !value_buf[0].equals("")) {
            filter.addAction(value_buf[0]);
        } else {
            filter.addAction(SCAN_ACTION);
        }
        registerReceiver(mScanReceiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == 120 && event.getRepeatCount() == 0) {
            if(dialog != null && dialog.isShowing()) {

            } else {
                if (!isScanning) {
                    isScanning = true;
                    mScanManager.startDecode();
                    mScanManager.setTriggerMode(Triggering.CONTINUOUS);
                }
            }
            return true;
        }
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == 120) {
            mScanManager.stopDecode();
            isScanning = false;
        }
        return super.onKeyUp(keyCode, event);
    }

    private void submitInStorageProductToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<JSONObject> objList = new ArrayList<JSONObject>();
        db.beginTransaction();
        try {
            // 插入数据到tp_in_storage表
            ContentValues cvInStorage = new ContentValues();
            cvInStorage.put("num", "");
            cvInStorage.put("create_time", System.currentTimeMillis() / 1000);
            cvInStorage.put("create_user", myApp.getUser().getUser_name());
            cvInStorage.put("local_num", location.getId());
            cvInStorage.put("u_id", myApp.getUser().getId());
            cvInStorage.put("token", myApp.getUser().getToken());
            long count = db.insert("tp_in_storage", null, cvInStorage);
            String in_id = "";
            if(count != -1) {
                Cursor c = db.rawQuery("select * from tp_in_storage order by id desc limit 0,1", null);
                if(c.getCount() > 0) {
                    while(c.moveToNext()) {
                        in_id = c.getString(c.getColumnIndex("id"));
                        try {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("num", c.getString(c.getColumnIndex("num")));
                            itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                            itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                            itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                            itemObject.put("operate_type", c.getString(c.getColumnIndex("operate_type")));
                            itemObject.put("local_num", c.getString(c.getColumnIndex("local_num")));
                            itemObject.put("remark", c.getString(c.getColumnIndex("remark")));
                            itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                            itemObject.put("token", c.getString(c.getColumnIndex("token")));
                            itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "InStorage");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 更新tp_in_storitem表
                for (int i = 0; i < itemList.size(); i++) {
                    ContentValues cvInStoritem = new ContentValues();
                    cvInStoritem.put("u_id", myApp.getUser().getId());
                    cvInStoritem.put("in_id", in_id);
                    cvInStoritem.put("category_id", itemList.get(i).getCategory_id());
                    cvInStoritem.put("local_num", location.getId());
                    cvInStoritem.put("product_num", itemList.get(i).getProduct_num());
                    cvInStoritem.put("product_name", itemList.get(i).getProduct_name());
                    cvInStoritem.put("number", itemList.get(i).getNumber());
                    cvInStoritem.put("create_time", System.currentTimeMillis() / 1000);
                    cvInStoritem.put("token", myApp.getUser().getToken());
                    db.insert("tp_in_storitem", null, cvInStoritem);
                    Cursor inStoritem = db.rawQuery("select * from tp_in_storitem order by id desc limit 0,1", null);
                    try {
                        while(inStoritem.moveToNext()) {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("u_id", inStoritem.getString(inStoritem.getColumnIndex("u_id")));
                            itemObject.put("in_id", inStoritem.getString(inStoritem.getColumnIndex("in_id")));
                            itemObject.put("category_id", inStoritem.getString(inStoritem.getColumnIndex("category_id")));
                            itemObject.put("product_num", inStoritem.getString(inStoritem.getColumnIndex("product_num")));
                            itemObject.put("product_name", inStoritem.getString(inStoritem.getColumnIndex("product_name")));
                            itemObject.put("local_num", inStoritem.getString(inStoritem.getColumnIndex("local_num")));
                            itemObject.put("amount", inStoritem.getString(inStoritem.getColumnIndex("amount")));
                            itemObject.put("in_price", inStoritem.getString(inStoritem.getColumnIndex("in_price")));
                            itemObject.put("number", inStoritem.getString(inStoritem.getColumnIndex("number")));
                            itemObject.put("create_time", inStoritem.getString(inStoritem.getColumnIndex("create_time")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + inStoritem.getString(inStoritem.getColumnIndex("id")));
                            itemObject.put("token", inStoritem.getString(inStoritem.getColumnIndex("token")));
                            itemObject.put("opposite_id", inStoritem.getString(inStoritem.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "InStoritem");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        inStoritem.close();
                    }
                }
                // 更新tp_local_product表
                for (int i = 0; i < itemList.size(); i++) {
                    Cursor cursor = db.rawQuery("select * from tp_local_product where local_num=? and product_num=?", new String[]{itemList.get(i).getLocal_num(), itemList.get(i).getProduct_num()});
                    if(cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            ContentValues cvLocalProduct = new ContentValues();
                            cvLocalProduct.put("number", cursor.getInt(cursor.getColumnIndex("number")) + Integer.parseInt(itemList.get(i).getNumber()));
                            cvLocalProduct.put("category_id", itemList.get(i).getCategory_id());
                            cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
                            db.update("tp_local_product", cvLocalProduct, "local_num=? and product_num=?", new String[]{location.getId(), itemList.get(i).getProduct_num()});
                            try {
                                JSONObject itemObject = new JSONObject();
                                itemObject.put("local_num", cursor.getString(cursor.getColumnIndex("local_num")));
                                itemObject.put("category_id", itemList.get(i).getCategory_id());
                                itemObject.put("product_num", cursor.getString(cursor.getColumnIndex("product_num")));
                                itemObject.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                                itemObject.put("number", cursor.getInt(cursor.getColumnIndex("number")) + Integer.parseInt(itemList.get(i).getNumber()));
                                itemObject.put("u_id", cursor.getString(cursor.getColumnIndex("u_id")));
                                itemObject.put("change_time", System.currentTimeMillis() / 1000);
                                itemObject.put("token_id", myApp.getUser().getToken() + "_" + cursor.getString(cursor.getColumnIndex("id")));
                                itemObject.put("token", cursor.getString(cursor.getColumnIndex("token")));
                                itemObject.put("opposite_id", cursor.getString(cursor.getColumnIndex("id")));
                                JSONObject obj = new JSONObject();
                                obj.put("operation", "update");
                                obj.put("table_name", "LocalProduct");
                                obj.put("data", itemObject);
                                objList.add(obj);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        ContentValues cvLocalProduct = new ContentValues();
                        cvLocalProduct.put("u_id", myApp.getUser().getId());
                        cvLocalProduct.put("category_id", itemList.get(i).getCategory_id());
                        cvLocalProduct.put("local_num", location.getId());
                        cvLocalProduct.put("product_num", itemList.get(i).getProduct_num());
                        cvLocalProduct.put("product_name", itemList.get(i).getProduct_name());
                        cvLocalProduct.put("number", itemList.get(i).getNumber());
                        cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
                        cvLocalProduct.put("token", myApp.getUser().getToken());
                        db.insert("tp_local_product", null, cvLocalProduct);
                        Cursor localProductItem = db.rawQuery("select * from tp_local_product order by id desc limit 0,1", null);
                        try {
                            while(localProductItem.moveToNext()) {
                                JSONObject itemObject = new JSONObject();
                                itemObject.put("local_num", localProductItem.getString(localProductItem.getColumnIndex("local_num")));
                                itemObject.put("category_id", localProductItem.getString(localProductItem.getColumnIndex("category_id")));
                                itemObject.put("product_num", localProductItem.getString(localProductItem.getColumnIndex("product_num")));
                                itemObject.put("product_name", localProductItem.getString(localProductItem.getColumnIndex("product_name")));
                                itemObject.put("number", localProductItem.getString(localProductItem.getColumnIndex("number")));
                                itemObject.put("u_id", localProductItem.getString(localProductItem.getColumnIndex("u_id")));
                                itemObject.put("change_time", localProductItem.getString(localProductItem.getColumnIndex("change_time")));
                                itemObject.put("token_id", myApp.getUser().getToken() + "_" + localProductItem.getString(localProductItem.getColumnIndex("id")));
                                itemObject.put("token", localProductItem.getString(localProductItem.getColumnIndex("token")));
                                itemObject.put("opposite_id", localProductItem.getString(localProductItem.getColumnIndex("id")));
                                JSONObject obj = new JSONObject();
                                obj.put("operation", "insert");
                                obj.put("table_name", "LocalProduct");
                                obj.put("data", itemObject);
                                objList.add(obj);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            localProductItem.close();
                        }
                    }
                }
                for(int i = 0; i < objList.size(); i++) {
                    myApp.getDataArray().put(objList.get(i));
                }
                myApp.saveDataToPref();
                CustomViewUtil.createToast(InStorageActivity.this, "入库成功！");
                finish();
                db.setTransactionSuccessful();
            } else {
                CustomViewUtil.createToast(InStorageActivity.this, "入库失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

    }

    private void queryProductByCodeFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select product_num,product_name,number,local_num,local_name,category_id,cate.name as category_name from (select pro.product_num,pro.product_name,pro.number,pro.local_num,loc.name as local_name,pro.category_id from tp_local_product pro,tp_location loc where pro.product_num=? and pro.local_num=loc.id) a left outer join tp_product_category cate on a.category_id=cate.id", new String[]{code});
        try {
            // 如果是已有产品，更新列表
            if(c.getCount() > 0) {
                if(code.startsWith("HP")) {
                    CustomViewUtil.createErrorToast(InStorageActivity.this, "该货品已入库，无需重复入库！");
                    return;
                }
                while (c.moveToNext()) {
                    boolean itemExist = false;
                    for(int i = 0; i < itemList.size(); i++) {
                        if(itemList.get(i).getProduct_num().equals(code)
                                && itemList.get(i).getProduct_name().equals(c.getString(c.getColumnIndex("product_name")))) {
                            itemList.get(i).setNumber("" + (Integer.parseInt(itemList.get(i).getNumber()) + 1));
                            itemExist = true;
                            break;
                        }
                    }
                    if(!itemExist) {
                        InStorageListItem item = new InStorageListItem();
                        item.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                        item.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                        item.setLocal_num(location.getId());
                        item.setLocal_name(location.getName());
                        item.setNumber("1");
                        item.setCategory_id(c.getString(c.getColumnIndex("category_id")));
                        itemList.add(item);
                    }
                    listAdapter.notifyDataSetChanged();
                    break;
                }
            }
            // 如果数据库中未查到
            else {
                // 查询还未添加到数据库中的产品列表
                boolean itemExist = false;
                for(int i = 0; i < itemList.size(); i++) {
                    if(itemList.get(i).getProduct_num().equals(code)) {
                        itemList.get(i).setNumber("" + (Integer.parseInt(itemList.get(i).getNumber()) + 1));
                        itemExist = true;
                        break;
                    }
                }
                if(itemExist) {
                    listAdapter.notifyDataSetChanged();
                } else {
                    // 如果是新产品，弹出对话框
                    showTypeEditDialog();
                }
            }
            if(itemList.size() == 0) {
                tvTip.setVisibility(View.VISIBLE);
                lv.setVisibility(View.GONE);
            } else {
                tvTip.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            c.close();
            db.close();
        }
    }

    public void showTypeEditDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(InStorageActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.add_new_product_dialog, null);
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int)(0.9 * screenWidth), (int)(1.2 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_add_new_product_dialog);
        TextView tvLocationName = (TextView) view.findViewById(R.id.tv_location_name_add_new_product_dialog);
        final TextView tvProductCode = (TextView) view.findViewById(R.id.tv_product_code_add_new_product_dialog);
        final TextView etProductName = (EditText) view.findViewById(R.id.et_product_name_add_new_product_dialog);
        final EditText etProductNumber = (EditText) view.findViewById(R.id.et_product_number_add_new_product_dialog);
        TextView tvCodeTip = (TextView) view.findViewById(R.id.tv_code_tip_add_new_product_dialog);
        TextView tvNameTip = (TextView) view.findViewById(R.id.tv_name_tip_add_new_product_dialog);
        TextView tvNumberTip = (TextView) view.findViewById(R.id.tv_number_tip_add_new_product_dialog);
        ImageView ivMinus = (ImageView) view.findViewById(iv_minus_add_new_product_dialog);
        ImageView ivAdd = (ImageView) view.findViewById(R.id.iv_add_add_new_product_dialog);
        TextView tvProductNumber = (TextView) view.findViewById(R.id.tv_product_number_add_new_product_dialog);
        tvProductType = (TextView) view.findViewById(R.id.tv_product_type_add_new_product_dialog);
        Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm_add_new_product_dialog);
        ivMinus.setVisibility(code.startsWith("HP") ? View.GONE : View.VISIBLE);
        ivAdd.setVisibility(code.startsWith("HP") ? View.GONE : View.VISIBLE);
        tvCodeTip.setText(code.startsWith("HP") ? "货品码" : "产品码");
        tvNameTip.setText(code.startsWith("HP") ? "货品名称" : "产品名称");
        tvNumberTip.setText(code.startsWith("HP") ? "货品数量" : "产品数量");
        etProductName.setHint(code.startsWith("HP") ? "请输入货品名称" : "请输入产品名称");
        etProductNumber.setEnabled(code.startsWith("HP") ? false : true);
        tvProductNumber.setVisibility(code.startsWith("HP") ? View.VISIBLE : View.GONE);
        etProductNumber.setVisibility(code.startsWith("HP") ? View.GONE : View.VISIBLE);
        tvLocationName.setText(location.getName());
        tvProductCode.setText(code);
        tvProductType.setText(typeList.get(0).getName());
        // 名称输入框获取焦点并弹出输入框
        etProductName.requestFocus();
        etProductName.requestFocusFromTouch();
        etProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                etProductName.setHint(hasFocus ? "" : (code.startsWith("HP") ? "请输入货品名称" : "请输入产品名称"));
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ivMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(etProductNumber.getText().toString().equals("") || etProductNumber.getText().toString().equals("0")) {
                    etProductNumber.setText("0");
                } else {
                    etProductNumber.setText((Integer.parseInt(etProductNumber.getText().toString()) - 1) + "");
                }
                etProductNumber.setSelection(etProductNumber.getText().toString().length());
            }
        });
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(etProductNumber.getText().toString().equals("")) {
                    etProductNumber.setText("1");
                } else {
                    etProductNumber.setText((Integer.parseInt(etProductNumber.getText().toString()) + 1) + "");
                }
                etProductNumber.setSelection(etProductNumber.getText().toString().length());
            }
        });
        tvProductType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return;
                }
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.type_popupwindow, null);
                ListView lvTypeList = (ListView) view.findViewById(R.id.lv_type_list);
                TypeDropDownListAdapter adapter = new TypeDropDownListAdapter(InStorageActivity.this, typeList);
                lvTypeList.setAdapter(adapter);
                popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                int width = tvProductType.getWidth();
                popupWindow.setWidth(width);
                popupWindow.showAsDropDown(tvProductType);
                popupWindow.setFocusable(true);
                lvTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedTypeIndex = position;
                        popupWindow.dismiss();
                        tvProductType.setText(typeList.get(selectedTypeIndex).getName());
                    }
                });
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etProductName.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(InStorageActivity.this, code.startsWith("HP") ? "请输入商品名称！": "请输入产品名称！");
                    return;
                }
                if(etProductNumber.getText().toString().equals("")) {
                    CustomViewUtil.createErrorToast(InStorageActivity.this, "产品数量不能为空！");
                    return;
                }
                InStorageListItem item = new InStorageListItem();
                item.setProduct_num(code);
                item.setProduct_name(etProductName.getText().toString());
                item.setLocal_num(location.getId());
                item.setLocal_name(location.getName());
                item.setNumber(etProductNumber.getText().toString());
                if(selectedTypeIndex != -1) {
                    item.setCategory_id(typeList.get(selectedTypeIndex).getId());
                } else {
                    item.setCategory_id("0");
                }
                itemList.add(item);
                listAdapter.notifyDataSetChanged();
                tvTip.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
                selectedTypeIndex = -1;
                dialog.dismiss();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void deleteProduct(int index) {
        showConfirmDialog(index);
//        itemList.remove(index);
//        listAdapter.notifyDataSetChanged();
//        if(itemList.size() == 0) {
//            tvTip.setVisibility(View.VISIBLE);
//            lv.setVisibility(View.GONE);
//        }
    }

    private void showConfirmDialog(final int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(InStorageActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.delete_confirm_dialog, null);
        dialog = builder.create();
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
                itemList.remove(index);
                listAdapter.notifyDataSetChanged();
                if(itemList.size() == 0) {
                    tvTip.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    public void dismissPopupWindow(int index) {
        selectedTypeIndex = index;
        popupWindow.dismiss();
        tvProductType.setText(typeList.get(selectedTypeIndex).getName());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScanManager != null) {
            mScanManager.stopDecode();
            isScanning = false;
        }
        unregisterReceiver(mScanReceiver);
    }


    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//        btnScan.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(event.getAction() == MotionEvent.ACTION_UP){
//                    System.out.println("btnScan.onTouch.ACTION_UP");
//                    isScanning = false;
//                    mScanManager.stopDecode();
//                } else if(event.getAction() == MotionEvent.ACTION_DOWN){
//                    System.out.println("btnScan.onTouch.ACTION_DOWN");
//                    v.requestFocus();
//                    v.requestFocusFromTouch();
//                    if (!isScanning) {
//                        isScanning = true;
//                        mScanManager.startDecode();
//                    }
//                }
//                return true;
//            }
//        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("btnScan.onClick");
                v.requestFocus();
                v.requestFocusFromTouch();
                if (!isScanning) {
                    isScanning = true;
                    mScanManager.startDecode();
                }
            }
        });
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestFocus();
                v.requestFocusFromTouch();
                if(itemList.size() == 0) {
                    CustomViewUtil.createErrorToast(InStorageActivity.this, "你还未添加任何产品！");
                    return;
                }
                for(int i = 0;i < itemList.size(); i++) {
                    if(itemList.get(i).getNumber().equals("") || Integer.parseInt(itemList.get(i).getNumber()) == 0) {
                        CustomViewUtil.createErrorToast(InStorageActivity.this, "入库数不能为空或者零！");
                        return;
                    }
                }
                submitInStorageProductToDB();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_in_storage);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location_in_storage);
        tvTip = (TextView) findViewById(R.id.tv_tip_in_storage);
        lv = (ListView) findViewById(R.id.lv_in_storage_list_in_storage);
        btnScan = (Button) findViewById(R.id.btn_scan_code_in_storage);
        btnFinish = (Button) findViewById(R.id.btn_finish_in_storage);
    }
}
