package com.zhihuitech.ccgljyb;

import android.app.Activity;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class AddLocationActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code = "";
    private boolean isScanning = false;

    private ImageView ivBack;
    private EditText etLocationCode;
    private EditText etLocationName;
    private Button btnSubmit;

    private String locationCode;
    private String locationName;

    private MyApplication myApp;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_location);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        initDatabase();
        initScan();
        Intent intent = getIntent();
        if(intent.hasExtra("code")) {
            etLocationCode.setText(intent.getStringExtra("code"));
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
                        if(code.length() != 14) {
                            CustomViewUtil.createErrorToast(AddLocationActivity.this, "您扫的库位码不符合规则！");
                        } else {
                            etLocationCode.setText(code);
                        }
                    } else {
                        CustomViewUtil.createErrorToast(AddLocationActivity.this, "您扫的不是库位码！");
                        return;
                    }
                    // 库位码输入框获得焦点，并弹出输入法
                    etLocationName.requestFocus();
                    etLocationName.requestFocusFromTouch();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etLocationName, 0);
//                    if(imm.hideSoftInputFromWindow(etLocationName.getWindowToken(), 0)) {
//                        //软键盘已弹出
//                        imm.showSoftInput(etLocationName, 0);
//                    } else {
//                        //软键盘未弹出
//                        imm.showSoftInput(etLocationName, 0);
//                    }
                }
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
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

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
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
                locationCode = etLocationCode.getText().toString();
                locationName = etLocationName.getText().toString();
                if(locationCode.equals("") || locationName.equals("")) {
                    CustomViewUtil.createErrorToast(AddLocationActivity.this, "库位码和库位名都不能为空！");
                    return;
                }
                saveLocationToDB();
            }
        });
    }

    private void saveLocationToDB() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            Cursor c1 = db.rawQuery("select * from tp_location where num=?", new String[]{locationCode});
            if(c1.getCount() > 0) {
                c1.close();
                CustomViewUtil.createToast(AddLocationActivity.this, "该库位已添加，不可重复添加！");
                return;
            } else {
                c1.close();
            }
            Cursor c2 = db.rawQuery("select * from tp_location where name=?", new String[]{locationName});
            if(c2.getCount() > 0) {
                c2.close();
                CustomViewUtil.createToast(AddLocationActivity.this, "该库位名已存在！");
                return;
            } else {
                c2.close();
            }
            ContentValues cv = new ContentValues();
            cv.put("num", locationCode);
            cv.put("name", locationName);
            cv.put("create_time", System.currentTimeMillis() / 1000);
            cv.put("u_id", myApp.getUser().getId());
            cv.put("token", myApp.getUser().getToken());
            long count = db.insert("tp_location", null, cv);
            if(count != -1) {
                saveInsertDataToData(db);
                CustomViewUtil.createToast(AddLocationActivity.this, "新增成功！");
                Intent intent = new Intent();
                intent.putExtra("location_code", locationCode);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                CustomViewUtil.createToast(AddLocationActivity.this, "新增失败！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveInsertDataToData(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from tp_location order by id desc limit 0,1", null);
        while (c.moveToNext()) {
            try {
                JSONObject itemObject = new JSONObject();
                itemObject.put("num", c.getString(c.getColumnIndex("num")));
                itemObject.put("name", c.getString(c.getColumnIndex("name")));
                itemObject.put("is_delete", c.getString(c.getColumnIndex("is_delete")));
                itemObject.put("create_time", c.getString(c.getColumnIndex("create_time")));
                itemObject.put("u_id", c.getString(c.getColumnIndex("u_id")));
                itemObject.put("token_id", myApp.getUser().getToken() + "_" + c.getString(c.getColumnIndex("id")));
                itemObject.put("token", myApp.getUser().getToken());
                itemObject.put("opposite_id", c.getString(c.getColumnIndex("id")));
                JSONObject obj = new JSONObject();
                obj.put("operation", "insert");
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
        ivBack = (ImageView) findViewById(R.id.iv_back_add_location);
        etLocationCode = (EditText) findViewById(R.id.et_location_code_add_location);
        etLocationName = (EditText) findViewById(R.id.et_location_name_add_location);
        btnSubmit = (Button) findViewById(R.id.btn_submit_add_location);
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
