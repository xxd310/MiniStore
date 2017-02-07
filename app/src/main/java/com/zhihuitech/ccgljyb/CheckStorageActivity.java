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
import com.zhihuitech.ccgljyb.adapter.CheckStorageAdapter;
import com.zhihuitech.ccgljyb.adapter.CheckStorageListAdapter;
import com.zhihuitech.ccgljyb.entity.CheckStorageListItem;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CheckStorageActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code = "";
    private boolean isScanning = false;

    private int screenWidth;
    private ImageView ivBack;
    private TextView tvCurrentLocation;
    private TextView tvTip;
    private ListView lv;
    private Button btnScan;
    private Button btnFinish;
    private Button btnCheckAgain;
    private Location location;
    private String status = "1";

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private List<CheckStorageListItem> itemList = new ArrayList<CheckStorageListItem>();
    private CheckStorageAdapter listAdapter;

    private MyApplication myApp;

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
                        CustomViewUtil.createErrorToast(CheckStorageActivity.this, "您扫到的是库位码！");
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
        setContentView(R.layout.check_storage);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initScan();
        initDatabase();
        getSystemScreenWidth();
        Intent intent = getIntent();
        if(intent.hasExtra("location")) {
            location = (Location) intent.getSerializableExtra("location");
            tvCurrentLocation.setText(location.getName());
        }

        listAdapter = new CheckStorageAdapter(CheckStorageActivity.this, itemList);
        lv.setAdapter(listAdapter);
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
                    CustomViewUtil.createErrorToast(CheckStorageActivity.this, "你还未添加盘仓产品！");
                    return;
                }
                for(int i = 0; i < itemList.size(); i++) {
                    if(Integer.parseInt(itemList.get(i).getNumber()) != Integer.parseInt(itemList.get(i).getActual_number())) {
                        itemList.get(i).setStatus("0");
                        status = "0";
                    } else {
                        itemList.get(i).setStatus("1");
                    }
                }
                showCheckResultDialog();
            }
        });
        btnCheckAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemList.clear();
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_check_storage);
        tvCurrentLocation = (TextView) findViewById(R.id.tv_current_location_check_storage);
        tvTip = (TextView) findViewById(R.id.tv_tip_check_storage);
        lv = (ListView) findViewById(R.id.lv_check_storage_list_check_storage);
        btnCheckAgain = (Button) findViewById(R.id.btn_check_again_check_storage);
        btnScan = (Button) findViewById(R.id.btn_scan_code_check_storage);
        btnFinish = (Button) findViewById(R.id.btn_finish_check_storage);
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

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    public void showCheckResultDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(CheckStorageActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.check_storage_result_dialog, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setContentView(view);
        dialog.getWindow().setLayout((int) (0.9 * screenWidth), (int) (1.1 * screenWidth));
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_check_storage_result_dialog);
        ListView lvCheckResult = (ListView) view.findViewById(R.id.lv_check_result_check_storage_result_dialog);
        CheckStorageListAdapter adapter = new CheckStorageListAdapter(CheckStorageActivity.this, itemList);
        lvCheckResult.setAdapter(adapter);
        Button btnConfirm = (Button) view.findViewById(R.id.btn_confirm_check_storage_result_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_check_storage_result_dialog);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCheckStorageProductToDB();
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

    private void submitCheckStorageProductToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<JSONObject> objList = new ArrayList<JSONObject>();
        db.beginTransaction();
        try {
            // 插入数据到tp_check_stock表
            ContentValues cvCheckStock = new ContentValues();
            cvCheckStock.put("local_num", location.getId());
            cvCheckStock.put("status", status);
            cvCheckStock.put("create_time", System.currentTimeMillis() / 1000);
            cvCheckStock.put("create_user", myApp.getUser().getUser_name());
            cvCheckStock.put("u_id", myApp.getUser().getId());
            cvCheckStock.put("token", myApp.getUser().getToken());
            long count = db.insert("tp_check_stock", null, cvCheckStock);
            String check_id = "";
            if(count != -1) {
                Cursor c = db.rawQuery("select * from tp_check_stock order by id desc limit 0,1", null);
                if (c.getCount() > 0) {
                    while (c.moveToNext()) {
                        check_id = c.getString(c.getColumnIndex("id"));
                        try {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("local_num", c.getString(c.getColumnIndex("local_num")));
                            itemObject.put("status", c.getString(c.getColumnIndex("status")));
                            itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                            itemObject.put("create_user", c.getString(c.getColumnIndex("create_user")));
                            itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                            itemObject.put("token", c.getString(c.getColumnIndex("token")));
                            itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "CheckStock");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 更新tp_checkitem表
                for (int i = 0; i < itemList.size(); i++) {
                    ContentValues cvCheckitem = new ContentValues();
                    cvCheckitem.put("u_id", myApp.getUser().getId());
                    cvCheckitem.put("check_id", check_id);
                    cvCheckitem.put("local_number", itemList.get(i).getNumber());
                    cvCheckitem.put("product_num", itemList.get(i).getProduct_num());
                    cvCheckitem.put("product_name", itemList.get(i).getProduct_name());
                    cvCheckitem.put("actual_number", itemList.get(i).getActual_number());
                    cvCheckitem.put("create_time", System.currentTimeMillis() / 1000);
                    cvCheckitem.put("status", itemList.get(i).getStatus());
                    cvCheckitem.put("token", myApp.getUser().getToken());
                    db.insert("tp_checkitem", null, cvCheckitem);
                    if(itemList.get(i).getStatus().equals("0")) {
                        ContentValues cvLocalProduct = new ContentValues();
                        cvLocalProduct.put("number", itemList.get(i).getActual_number());
                        cvLocalProduct.put("change_time", System.currentTimeMillis() / 1000);
                        db.update("tp_local_product", cvLocalProduct, "product_num=? and local_num=?", new String[]{itemList.get(i).getProduct_num(), location.getId()});
                        Cursor cursor = db.rawQuery("select * from tp_local_product where local_num=? and product_num=?", new String[]{location.getId(), itemList.get(i).getProduct_num()});
                        while (cursor.moveToNext()) {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("number", itemList.get(i).getActual_number());
                            itemObject.put("change_time", System.currentTimeMillis() / 1000);
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + cursor.getString(cursor.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "update");
                            obj.put("table_name", "LocalProduct");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        }
                    }
                    Cursor checkItem = db.rawQuery("select * from tp_checkitem order by id desc limit 0,1", null);
                    try {
                        while (checkItem.moveToNext()) {
                            JSONObject itemObject = new JSONObject();
                            itemObject.put("check_id", checkItem.getString(checkItem.getColumnIndex("check_id")));
                            itemObject.put("u_id", checkItem.getString(checkItem.getColumnIndex("u_id")));
                            itemObject.put("product_num", checkItem.getString(checkItem.getColumnIndex("product_num")));
                            itemObject.put("product_name", checkItem.getString(checkItem.getColumnIndex("product_name")));
                            itemObject.put("local_number", checkItem.getString(checkItem.getColumnIndex("local_number")));
                            itemObject.put("actual_number", checkItem.getString(checkItem.getColumnIndex("actual_number")));
                            itemObject.put("create_time", checkItem.getString(checkItem.getColumnIndex("create_time")));
                            itemObject.put("status", checkItem.getString(checkItem.getColumnIndex("status")));
                            itemObject.put("token_id", myApp.getUser().getToken() + "_" + checkItem.getString(checkItem.getColumnIndex("id")));
                            itemObject.put("token", checkItem.getString(checkItem.getColumnIndex("token")));
                            itemObject.put("opposite_id", checkItem.getString(checkItem.getColumnIndex("id")));
                            JSONObject obj = new JSONObject();
                            obj.put("operation", "insert");
                            obj.put("table_name", "Checkitem");
                            obj.put("data", itemObject);
                            objList.add(obj);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        checkItem.close();
                    }
                }
                for(int i = 0; i < objList.size(); i++) {
                    myApp.getDataArray().put(objList.get(i));
                }
                myApp.saveDataToPref();
                db.setTransactionSuccessful();
                CustomViewUtil.createToast(CheckStorageActivity.this, "盘仓成功！");
                Intent intent = new Intent(CheckStorageActivity.this, CheckStorageListActivity.class);
                intent.putExtra("list", (ArrayList)itemList);
                startActivity(intent);
                finish();
            } else {
                CustomViewUtil.createToast(CheckStorageActivity.this, "盘仓失败！");
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
                            CustomViewUtil.createToast(CheckStorageActivity.this, "该商品已经在盘仓列表中！");
                            return;
                        }
                    }
                }
                while (c.moveToNext()) {
                    boolean itemExist = false;
                    for(int i = 0; i < itemList.size(); i++) {
                        if(itemList.get(i).getProduct_num().equals(code)
                                && itemList.get(i).getProduct_name().equals(c.getString(c.getColumnIndex("product_name")))) {
                            if(itemList.get(i).getActual_number().equals("")) {
                                itemList.get(i).setActual_number("1");
                            } else {
                                itemList.get(i).setActual_number("" + (Integer.parseInt(itemList.get(i).getActual_number()) + 1));
//                                if(Integer.parseInt(itemList.get(i).getNumber()) == Integer.parseInt(itemList.get(i).getActual_number())) {
//                                    CustomViewUtil.createToast(CheckStorageActivity.this, "已达最大库存数！");
//                                    return;
//                                } else {
//                                    itemList.get(i).setActual_number("" + (Integer.parseInt(itemList.get(i).getActual_number()) + 1));
//                                }
                            }
                            itemExist = true;
                            break;
                        }
                    }
                    if(!itemExist) {
                        CheckStorageListItem item = new CheckStorageListItem();
                        item.setProduct_num(c.getString(c.getColumnIndex("product_num")));
                        item.setProduct_name(c.getString(c.getColumnIndex("product_name")));
                        item.setLocal_name(location.getName());
                        item.setNumber(c.getString(c.getColumnIndex("number")));
                        item.setActual_number("1");
                        item.setCategory_id(c.getString(c.getColumnIndex("category_id")));
                        itemList.add(item);
                    }
                }
                listAdapter.notifyDataSetChanged();
            } else {
                CustomViewUtil.createToast(CheckStorageActivity.this, "仓库中不存在该产品！");
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(CheckStorageActivity.this);
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
    protected void onDestroy() {
        super.onDestroy();
        if (mScanManager != null) {
            mScanManager.stopDecode();
            isScanning = false;
        }
        unregisterReceiver(mScanReceiver);
    }
}
