package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.zhihuitech.ccgljyb.adapter.LocationDropDownListAdapter;
import com.zhihuitech.ccgljyb.entity.Location;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class LocationSelectActivity extends Activity {
    // 扫码所需参数对象
    private final static String SCAN_ACTION = ScanManager.ACTION_DECODE;
    private Ringtone r;
    private Vibrator mVibrator;
    private ScanManager mScanManager;
    private String code = "";
    private boolean isScanning = false;

    private ImageView ivBack;
    private TextView tvTitle;
    private TextView tvLocation;
    private Button btnScanCodeInStorage;

    private PopupWindow popupWindow;
    private ListView lvLocationList;
    private List<Location> locationList = new ArrayList<Location>();
    private LocationDropDownListAdapter adapter;
    private int currentSelectedIndex = -1;

    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private String from;

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
                    if(!code.startsWith("KW")) {
                        CustomViewUtil.createErrorToast(LocationSelectActivity.this, "您扫的不是库位码，请重新扫描！");
                        return;
                    }
                    if(code.length() != 14) {
                        CustomViewUtil.createErrorToast(LocationSelectActivity.this, "您扫的库位码不符合规则！");
                        return;
                    }
                    // 检查该库位码是否已存入数据库
                    boolean locationCodeExit = false;
                    for(int i = 0; i < locationList.size(); i++) {
                        if(locationList.get(i).getNum().equals(code)) {
                            locationCodeExit = true;
                            currentSelectedIndex = i;
                            break;
                        }
                    }
                    // 如果扫描到的是已存在的库位码，设置当前选中的库位，并且跳转到入库界面
                    if(locationCodeExit) {
                        tvLocation.setText(locationList.get(currentSelectedIndex).getName());
                        tvLocation.setTextColor(Color.BLACK);
                        Intent i;
                        if(from.equals("in")) {
                            i = new Intent(LocationSelectActivity.this, InStorageActivity.class);
                            i.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(i);
                        } else if(from.equals("out")) {
                            i = new Intent(LocationSelectActivity.this, OutStorageActivity.class);
                            i.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(i);
                        } else if(from.equals("check")) {
                            i = new Intent(LocationSelectActivity.this, CheckStorageActivity.class);
                            i.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(i);
                        }
                    } else {
                        CustomViewUtil.createErrorToast(LocationSelectActivity.this, "暂无此库位信息，请先添加库位！");
                        Intent i = new Intent(LocationSelectActivity.this, AddLocationActivity.class);
                        i.putExtra("code", code);
                        startActivityForResult(i, 11);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_select);

        Intent intent = getIntent();
        if(intent.hasExtra("from")) {
            from = intent.getStringExtra("from");
        }
        findViews();
        addListeners();
        initScan();
        initDatabase();
        queryLocationListFromDB();

        if(from.equals("in")) {
            tvTitle.setText("入库操作");
        } else if(from.equals("out")) {
            tvTitle.setText("出库操作");
        } else if(from.equals("check")) {
            tvTitle.setText("盘仓操作");
        }
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    private void queryLocationListFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from tp_location", null);
        locationList.clear();
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                Location location = new Location();
                location.setId(c.getString(c.getColumnIndex("id")));
                location.setName(c.getString(c.getColumnIndex("name")));
                location.setIs_delete(c.getString(c.getColumnIndex("is_delete")));
                location.setCreate_time(c.getString(c.getColumnIndex("create_time")));
                location.setNum(c.getString(c.getColumnIndex("num")));
                location.setU_id(c.getString(c.getColumnIndex("u_id")));
                locationList.add(location);
            }
            tvLocation.setEnabled(true);
        }
        c.close();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 11 && resultCode == RESULT_OK) {
            queryLocationListFromDB();
            String addedCode = data.getStringExtra("location_code");
            for(int i = 0; i < locationList.size(); i++) {
                if(locationList.get(i).getNum().equals(addedCode)) {
                    currentSelectedIndex = i;
                    tvLocation.setText(locationList.get(i).getName());
                    tvLocation.setTextColor(Color.BLACK);
                    break;
                }
            }
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
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    return;
                }
                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.location_popupwindow, null);
                lvLocationList = (ListView) view.findViewById(R.id.lv_location_list);
                adapter = new LocationDropDownListAdapter(LocationSelectActivity.this, locationList);
                lvLocationList.setAdapter(adapter);
                popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
                int width = tvLocation.getWidth();
                popupWindow.setWidth(width);
                popupWindow.showAsDropDown(tvLocation);
                popupWindow.setFocusable(true);
            }
        });
        /*btnScanCodeInStorage.setOnTouchListener(new View.OnTouchListener() {
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
                    if(currentSelectedIndex == -1) {
                        if (!isScanning) {
                            isScanning = true;
                            mScanManager.startDecode();
                        }
                    } else {
                        if(from.equals("in")) {
                            Intent intent = new Intent(LocationSelectActivity.this, InStorageActivity.class);
                            intent.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(intent);
                        } else if(from.equals("out")) {
                            Intent intent = new Intent(LocationSelectActivity.this, OutStorageActivity.class);
                            intent.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(intent);
                        } else if(from.equals("check")) {
                            Intent intent = new Intent(LocationSelectActivity.this, CheckStorageActivity.class);
                            intent.putExtra("location", locationList.get(currentSelectedIndex));
                            startActivity(intent);
                        }
                    }
                }
                return true;
            }
        });*/
        btnScanCodeInStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentSelectedIndex == -1) {
                    if (!isScanning) {
                        isScanning = true;
                        mScanManager.startDecode();
                    }
                } else {
                    if(from.equals("in")) {
                        Intent intent = new Intent(LocationSelectActivity.this, InStorageActivity.class);
                        intent.putExtra("location", locationList.get(currentSelectedIndex));
                        startActivity(intent);
                    } else if(from.equals("out")) {
                        Intent intent = new Intent(LocationSelectActivity.this, OutStorageActivity.class);
                        intent.putExtra("location", locationList.get(currentSelectedIndex));
                        startActivity(intent);
                    } else if(from.equals("check")) {
                        Intent intent = new Intent(LocationSelectActivity.this, CheckStorageActivity.class);
                        intent.putExtra("location", locationList.get(currentSelectedIndex));
                        startActivity(intent);
                    }
                }
            }
        });
    }

    public void dismissPopupWindow(String text, int index) {
        currentSelectedIndex = index;
        tvLocation.setText(text);
        tvLocation.setTextColor(Color.BLACK);
        popupWindow.dismiss();
        if(from.equals("in")) {
            Intent intent = new Intent(LocationSelectActivity.this, InStorageActivity.class);
            intent.putExtra("location", locationList.get(currentSelectedIndex));
            startActivity(intent);
        } else if(from.equals("out")) {
            Intent intent = new Intent(LocationSelectActivity.this, OutStorageActivity.class);
            intent.putExtra("location", locationList.get(currentSelectedIndex));
            startActivity(intent);
        } else if(from.equals("check")) {
            Intent intent = new Intent(LocationSelectActivity.this, CheckStorageActivity.class);
            intent.putExtra("location", locationList.get(currentSelectedIndex));
            startActivity(intent);
        }
    }

    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_location_select);
        tvTitle = (TextView) findViewById(R.id.tv_title_location_select);
        tvLocation = (TextView) findViewById(R.id.tv_location_name_location_select);
        btnScanCodeInStorage = (Button) findViewById(R.id.btn_scan_code_in_storage_location_select);
    }
}
