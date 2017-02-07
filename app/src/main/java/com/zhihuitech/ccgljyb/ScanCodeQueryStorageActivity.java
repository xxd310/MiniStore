package com.zhihuitech.ccgljyb;

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
import com.zhihuitech.ccgljyb.adapter.ProductTypeUpdateListAdapter;
import com.zhihuitech.ccgljyb.adapter.ScanCodeQueryStorageListAdapter;
import com.zhihuitech.ccgljyb.entity.LocationNumber;
import com.zhihuitech.ccgljyb.entity.ProductType;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScanCodeQueryStorageActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code;
    private boolean isScanning = false;

    private int screenWidth;

    private ImageView ivBack;
    private TextView tvProductName;
    private TextView tvProductNum;
    private TextView tvTypeName;
    private TextView tvNameTip;
    private TextView tvCodeTip;
    private TextView tvQueryTip;
    private LinearLayout llResult;
    private Button btnScan;
    private Button btnUpdateType;

    private ListView lv;
    private List<LocationNumber> lnList = new ArrayList<LocationNumber>();
    private ScanCodeQueryStorageListAdapter adapter;

    private List<ProductType> list = new ArrayList<ProductType>();

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_code_query_storage);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();
        initScan();
        getSystemScreenWidth();
        queryTypeListFromDB();
        adapter = new ScanCodeQueryStorageListAdapter(ScanCodeQueryStorageActivity.this, lnList);
        lv.setAdapter(adapter);
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
                        CustomViewUtil.createErrorToast(ScanCodeQueryStorageActivity.this, "您扫的是库位码！");
                        return;
                    }
                    tvProductNum.setText(code);
                    queryProductByCodeFromDB();
                }
            }
        }
    };

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

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
        ProductType pt = new ProductType();
        pt.setId("0");
        pt.setName("全部类别");
        list.add(0, pt);
        c.close();
        db.close();
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /*btnScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP){
                    System.out.println("btnScan.onTouch.ACTION_UP");
                    isScanning = false;
                    mScanManager.stopDecode();
                } else if(event.getAction() == MotionEvent.ACTION_DOWN){
                    System.out.println("btnScan.onTouch.ACTION_DOWN");
                    v.requestFocus();
                    v.requestFocusFromTouch();
                    if (!isScanning) {
                        isScanning = true;
                        mScanManager.startDecode();
                    }
                }
                return true;
            }
        });*/
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning) {
                    isScanning = true;
                    mScanManager.startDecode();
                }
            }
        });
        btnUpdateType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateTypeDialog();
            }
        });
    }

    private void showUpdateTypeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(ScanCodeQueryStorageActivity.this);
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
        ProductTypeUpdateListAdapter adapter = new ProductTypeUpdateListAdapter(ScanCodeQueryStorageActivity.this, list);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                updateTypeToDB(list.get(position).getId());
                tvTypeName.setText(list.get(position).getName());
            }
        });
    }

    private void updateTypeToDB(String typeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put("category_id", typeId);
            cv.put("change_time", System.currentTimeMillis() / 1000);
            db.update("tp_local_product",cv,"product_num=?",new String[]{code});
            Cursor c = db.rawQuery("select * from tp_local_product where product_num=?", new String[]{code});
            while (c.moveToNext()) {
                JSONObject itemObject = new JSONObject();
                itemObject.put("local_num", c.getString(c.getColumnIndex("local_num")));
                itemObject.put("category_id", c.getString(c.getColumnIndex("category_id")));
                itemObject.put("product_num", c.getString(c.getColumnIndex("product_num")));
                itemObject.put("product_name", c.getString(c.getColumnIndex("product_name")));
                itemObject.put("number", c.getString(c.getColumnIndex("number")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("change_time", c.getString(c.getColumnIndex("change_time")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", c.getString(c.getColumnIndex("token")));
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "update");
                obj.put("table_name", "LocalProduct");
                obj.put("data", itemObject);
                myApp.getDataArray().put(obj);
                myApp.saveDataToPref();
            }
            ContentValues cvItem = new ContentValues();
            cvItem.put("category_id", typeId);
            db.update("tp_in_storitem", cv, "product_num=?", new String[]{code});
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
            db.update("tp_out_storitem", cv, "product_num=?", new String[]{code});
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

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_scan_code_query_storage);
        tvProductName = (TextView) findViewById(R.id.tv_product_name_scan_code_query_storage);
        tvProductNum = (TextView) findViewById(R.id.tv_product_num_scan_code_query_storage);
        tvTypeName = (TextView) findViewById(R.id.tv_type_name_scan_code_query_storage);
        tvNameTip = (TextView) findViewById(R.id.tv_name_tip_scan_code_query_storage);
        tvCodeTip = (TextView) findViewById(R.id.tv_code_tip_scan_code_query_storage);
        lv = (ListView) findViewById(R.id.lv_storage_info_scan_code_query_storage);
        tvQueryTip = (TextView) findViewById(R.id.tv_query_tip_scan_code_query_storage);
        llResult = (LinearLayout) findViewById(R.id.ll_query_result_scan_code_query_storage);
        btnScan = (Button) findViewById(R.id.btn_scan_code_scan_code_query_storage);
        btnUpdateType = (Button) findViewById(R.id.btn_update_type_scan_code_query_storage);
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
        if (!isScanning) {
            isScanning = true;
            mScanManager.startDecode();
        }
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == 120 && event.getRepeatCount() == 0) {
            if (!isScanning) {
                isScanning = true;
                mScanManager.startDecode();
                mScanManager.setTriggerMode(Triggering.CONTINUOUS);
            }
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

    private void queryProductByCodeFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select product_name,local_num,local_name,number,category_id,cate.name as category_name from (select pro.product_name,pro.local_num,pro.number,loc.name as local_name,category_id from tp_local_product pro, tp_location loc where product_num=? and pro.local_num=loc.id) a left outer join tp_product_category cate on a.category_id=cate.id", new String[]{code});
        lnList.clear();
        if(c.getCount() > 0) {
            tvNameTip.setText(code.startsWith("HP") ? "货品名称" : "产品名称");
            tvCodeTip.setText(code.startsWith("HP") ? "货品编码" : "产品编码");
            lv.setVisibility(View.VISIBLE);
            while (c.moveToNext()) {
                tvProductName.setText(c.getString(c.getColumnIndex("product_name")));
                tvTypeName.setText(c.getString(c.getColumnIndex("category_name")) == null ? "全部类别" : c.getString(c.getColumnIndex("category_name")));
                LocationNumber ln = new LocationNumber();
                ln.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                ln.setLocal_num(c.getString(c.getColumnIndex("local_num")));
                ln.setLocal_name(c.getString(c.getColumnIndex("local_name")));
                ln.setNumber(c.getString(c.getColumnIndex("number")));
                lnList.add(ln);
            }
            adapter.notifyDataSetChanged();
            llResult.setVisibility(View.VISIBLE);
            tvQueryTip.setVisibility(View.GONE);
        } else {
            tvProductNum.setText("");
            tvProductName.setText("");
            tvTypeName.setText("");
            lv.setVisibility(View.GONE);
            CustomViewUtil.createToast(ScanCodeQueryStorageActivity.this, "暂无此产品信息！");
            llResult.setVisibility(View.GONE);
            tvQueryTip.setVisibility(View.VISIBLE);
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
}
