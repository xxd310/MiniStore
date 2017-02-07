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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.zhihuitech.ccgljyb.adapter.OutStorageListAdapter;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.entity.OutStorageListItem;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OutStorageActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code = "";
    private boolean isScanning = false;

    private ImageView ivBack;
    private TextView tvCurrentLocation;
    private Location location;
    private TextView tvTip;
    private ListView lv;
    private Button btnScan;
    private Button btnFinish;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private List<OutStorageListItem> itemList = new ArrayList<OutStorageListItem>();
    private OutStorageListAdapter listAdapter;

    private MyApplication myApp;

    private int screenWidth;

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
                        CustomViewUtil.createErrorToast(OutStorageActivity.this, "您扫到的是库位码！");
                        return;
                    }
                    queryProductByCodeFromDB();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.out_storage);

        myApp = (MyApplication) getApplication();
        initScan();
        initDatabase();
        findViews();
        addListeners();
        getSystemScreenWidth();
        Intent intent = getIntent();
        if(intent.hasExtra("location")) {
            location = (Location) intent.getSerializableExtra("location");
            tvCurrentLocation.setText(location.getName());
        }

        listAdapter = new OutStorageListAdapter(OutStorageActivity.this, itemList);
        lv.setAdapter(listAdapter);
    }

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
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
                    CustomViewUtil.createErrorToast(OutStorageActivity.this, "你还未添加任何产品！");
                    return;
                }
                for (int i = 0; i < itemList.size(); i++) {
                    if(itemList.get(i).getOut_number().equals("") || itemList.get(i).getOut_number().equals("0")) {
                        CustomViewUtil.createErrorToast(OutStorageActivity.this, "出库数不能为空或者0！");
                        return;
                    }
                    if(Integer.parseInt(itemList.get(i).getOut_number()) > Integer.parseInt(itemList.get(i).getNumber())) {
                        CustomViewUtil.createErrorToast(OutStorageActivity.this, itemList.get(i).getProduct_name() + "的出库数大于库存数");
                        return;
                    }
                }
                submitOutStorageProductToDB();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_out_storage);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location_out_storage);
        tvTip = (TextView) findViewById(R.id.tv_tip_out_storage);
        lv = (ListView) findViewById(R.id.lv_out_storage_list_out_storage);
        btnScan = (Button) findViewById(R.id.btn_scan_code_out_storage);
        btnFinish = (Button) findViewById(R.id.btn_finish_out_storage);
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

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(OutStorageActivity.this);
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

    private void submitOutStorageProductToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<JSONObject> objList = new ArrayList<JSONObject>();
        db.beginTransaction();
        try {
            // 插入数据到tp_out_storage表
            ContentValues cvOutStorage = new ContentValues();
            cvOutStorage.put("num", "");
            cvOutStorage.put("operate_type", "");
            cvOutStorage.put("create_time", System.currentTimeMillis() / 1000);
            cvOutStorage.put("create_user", myApp.getUser().getUser_name());
            cvOutStorage.put("u_id", myApp.getUser().getId());
            cvOutStorage.put("token", myApp.getUser().getToken());
            long count = db.insert("tp_out_storage", null, cvOutStorage);
            String out_id = "";
            if(count != -1) {
                Cursor c = db.rawQuery("select * from tp_out_storage order by id desc limit 0,1", null);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        out_id = c.getString(c.getColumnIndex("id"));
                        try {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("num", c.getString(c.getColumnIndex("num")));
                            itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                            itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                            itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                            itemObject.put("remark", c.getString(c.getColumnIndex("remark")));
                            itemObject.put("operate_type", c.getString(c.getColumnIndex("operate_type")));
                            itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                            itemObject.put("token", c.getString(c.getColumnIndex("token")));
                            itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "OutStorage");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 更新tp_out_storitem表
                for (int i = 0; i < itemList.size(); i++) {
                    ContentValues cvOutStoritem = new ContentValues();
                    cvOutStoritem.put("u_id", myApp.getUser().getId());
                    cvOutStoritem.put("out_id", out_id);
                    cvOutStoritem.put("local_num", itemList.get(i).getLocal_num());
                    cvOutStoritem.put("product_num", itemList.get(i).getProduct_num());
                    cvOutStoritem.put("product_name", itemList.get(i).getProduct_name());
                    cvOutStoritem.put("category_id", itemList.get(i).getCategory_id());
                    cvOutStoritem.put("number", itemList.get(i).getOut_number());
                    cvOutStoritem.put("create_time", System.currentTimeMillis() / 1000);
                    cvOutStoritem.put("token", myApp.getUser().getToken());
                    db.insert("tp_out_storitem", null, cvOutStoritem);
                    Cursor outStoritem = db.rawQuery("select * from tp_out_storitem order by id desc limit 0,1", null);
                    try {
                        while(outStoritem.moveToNext()) {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("u_id", outStoritem.getString(outStoritem.getColumnIndex("u_id")));
                            itemObject.put("out_id", outStoritem.getString(outStoritem.getColumnIndex("out_id")));
                            itemObject.put("category_id", outStoritem.getString(outStoritem.getColumnIndex("category_id")));
                            itemObject.put("product_num", outStoritem.getString(outStoritem.getColumnIndex("product_num")));
                            itemObject.put("product_name", outStoritem.getString(outStoritem.getColumnIndex("product_name")));
                            itemObject.put("local_num", outStoritem.getString(outStoritem.getColumnIndex("local_num")));
                            itemObject.put("number", outStoritem.getString(outStoritem.getColumnIndex("number")));
//                            itemObject.put("out_price", outStoritem.getString(outStoritem.getColumnIndex("out_price")));
//                            itemObject.put("amount", outStoritem.getString(outStoritem.getColumnIndex("amount")));
                            itemObject.put("create_time", outStoritem.getString(outStoritem.getColumnIndex("create_time")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + outStoritem.getString(outStoritem.getColumnIndex("id")));
                            itemObject.put("token", outStoritem.getString(outStoritem.getColumnIndex("token")));
                            itemObject.put("opposite_id", outStoritem.getString(outStoritem.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "OutStoritem");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        outStoritem.close();
                    }
                }
                // 更新tp_local_product表
                for (int i = 0; i < itemList.size(); i++) {
                    Cursor cursor = db.rawQuery("select * from tp_local_product where local_num=? and product_num=?", new String[]{itemList.get(i).getLocal_num(), itemList.get(i).getProduct_num()});
                    if(cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            if(itemList.get(i).getProduct_num().startsWith("HP")) {
                                try {
                                    JSONObject itemObject = new JSONObject();
                                    itemObject.put("local_num", cursor.getString(cursor.getColumnIndex("local_num")));
                                    itemObject.put("category_id", cursor.getString(cursor.getColumnIndex("category_id")));
                                    itemObject.put("product_num", cursor.getString(cursor.getColumnIndex("product_num")));
                                    itemObject.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                                    itemObject.put("number", cursor.getString(cursor.getColumnIndex("number")));
                                    itemObject.put("u_id", cursor.getString(cursor.getColumnIndex("u_id")));
                                    itemObject.put("change_time", cursor.getString(cursor.getColumnIndex("change_time")));
                                    itemObject.put("token_id", myApp.getUser().getToken() + "_" + cursor.getString(cursor.getColumnIndex("id")));
                                    itemObject.put("token", cursor.getString(cursor.getColumnIndex("token")));
                                    itemObject.put("opposite_id", cursor.getString(cursor.getColumnIndex("id")));
                                    JSONObject obj = new JSONObject();
                                    obj.put("operation", "delete");
                                    obj.put("table_name", "LocalProduct");
                                    obj.put("data", itemObject);
                                    objList.add(obj);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                db.delete("tp_local_product", "product_num=?", new String[]{itemList.get(i).getProduct_num()});
                            } else {
                                ContentValues cvLocalProduct = new ContentValues();
                                cvLocalProduct.put("number", cursor.getInt(cursor.getColumnIndex("number")) - Integer.parseInt(itemList.get(i).getOut_number()));
                                cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
                                db.update("tp_local_product", cvLocalProduct, "local_num=? and product_num=?", new String[]{itemList.get(i).getLocal_num(), itemList.get(i).getProduct_num()});
                                try {
                                    JSONObject itemObject = new JSONObject();
                                    itemObject.put("local_num", cursor.getString(cursor.getColumnIndex("local_num")));
                                    itemObject.put("category_id", cursor.getString(cursor.getColumnIndex("category_id")));
                                    itemObject.put("product_num", cursor.getString(cursor.getColumnIndex("product_num")));
                                    itemObject.put("product_name", cursor.getString(cursor.getColumnIndex("product_name")));
                                    itemObject.put("number", cursor.getInt(cursor.getColumnIndex("number")) - Integer.parseInt(itemList.get(i).getOut_number()));
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
                        }
                    }
                }
                for(int i = 0; i < objList.size(); i++) {
                    myApp.getDataArray().put(objList.get(i));
                }
                myApp.saveDataToPref();
                db.setTransactionSuccessful();
                CustomViewUtil.createToast(OutStorageActivity.this, "出库成功！");
                finish();
            } else {
                CustomViewUtil.createToast(OutStorageActivity.this, "出库失败！");
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
        Cursor c = db.rawQuery("select product_num,product_name,number,local_num,local_name,category_id,cate.name as category_name from (select pro.product_num,pro.product_name,pro.number,pro.local_num,loc.name as local_name,pro.category_id from tp_local_product pro,tp_location loc where pro.product_num=? and pro.local_num=loc.id and loc.id=?) a left outer join tp_product_category cate on a.category_id=cate.id", new String[]{code, location.getId()});
        try {
            if(c.getCount() > 0) {
                if(code.startsWith("HP")) {
                    for(int i = 0; i < itemList.size(); i++) {
                        if(itemList.get(i).getProduct_num().equals(code)) {
                            CustomViewUtil.createErrorToast(OutStorageActivity.this, "该商品已经在出库列表中！");
                            return;
                        }
                    }
                }
                while (c.moveToNext()) {
                    boolean itemExist = false;
                    for(int i = 0; i < itemList.size(); i++) {
                        if(itemList.get(i).getProduct_num().equals(code)
                                && itemList.get(i).getProduct_name().equals(c.getString(c.getColumnIndex("product_name")))) {
                            if(itemList.get(i).getOut_number().equals("")) {
                                itemList.get(i).setOut_number("1");
                            } else {
                                if(Integer.parseInt(itemList.get(i).getNumber()) == Integer.parseInt(itemList.get(i).getOut_number())) {
                                    CustomViewUtil.createErrorToast(OutStorageActivity.this, "已达最大库存数！");
                                    return;
                                } else {
                                    itemList.get(i).setOut_number("" + (Integer.parseInt(itemList.get(i).getOut_number()) + 1));
                                }
                            }
                            itemExist = true;
                            break;
                        }
                    }
                    if(!itemExist) {
                        OutStorageListItem item = new OutStorageListItem();
                        item.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                        item.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                        item.setLocal_num(location.getId());
                        item.setLocal_name(location.getName());
                        item.setNumber(c.getString(c.getColumnIndex("number")));
                        item.setOut_number("1");
                        item.setCategory_id(c.getString(c.getColumnIndex("category_id")));
                        itemList.add(item);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }
            // 如果数据库中未查到
            else {
                CustomViewUtil.createErrorToast(OutStorageActivity.this, "仓库中不存在该产品！");
                return;
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
