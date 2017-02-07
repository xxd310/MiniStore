package com.zhihuitech.ccgljyb;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.*;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.zhihuitech.ccgljyb.entity.SyncEvent;
import com.zhihuitech.ccgljyb.entity.UpdateAuthEvent;
import com.zhihuitech.ccgljyb.entity.User;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.HttpUtil;
import com.zhihuitech.ccgljyb.util.MD5;
import com.zhihuitech.ccgljyb.util.UpdateManager;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends Activity {
    private TextView tvShopName;
    private TextView tvUserName;
    private TextView tvAuthExpire;
    private RelativeLayout rlInStorage;
    private RelativeLayout rlOutStorage;
    private RelativeLayout rlCheck;
    private RelativeLayout rlStoreInfo;
    private RelativeLayout rlSetting;
    private RelativeLayout rlRecord;
    private RelativeLayout rlScanCodeQueryStorage;
    private RelativeLayout rlHelp;

    private TextView tvInStorage;
    private TextView tvOutStorage;
    private TextView tvCheckStorage;
    private TextView tvRecord;
    private TextView tvScanCode;
    private TextView tvStoreInfo;
    private TextView tvSetting;
    private TextView tvHelp;

    private MyApplication myApp;
    private int screenWidth;
    private AlertDialog authExpireDialog;
    private AlertDialog connectWifiDialog;

    private final static int UPDATE_YUN = 0;
    private final static int SHOW_EXPIRE_DIALOG = 1;
    private final static int EXIT = 2;
    private final static int CHECK_AUTH_TIME = 3;
    private final static int UPDATE_AUTH = 4;
    private final static int SAVE_SYNC_TIME = 5;
    private final static int CHECK_EXPIRE_TIME = 6;

    private String uploadData;
    private String appKey = "4654501791a54475b93e4e0834b30570";
    private String secretKey = "32faebaa8130cc06914b974b6427725b7440f727";

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "ccgl";
    private boolean autoDownload = false;

    // 连续按两次退出的标记
    private boolean isExit = false;

    // APP更新管理类
    private UpdateManager updateManager;
    private String imei;
    private DatabaseContext dbContext;
    private DatabaseHelper dbHelper;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_EXPIRE_DIALOG:
                    showAuthExpireDialog((Long.parseLong(myApp.getUser().getEnd_time()) - System.currentTimeMillis() / 1000) / (24 * 60 * 60) + "");
                    break;
                case UPDATE_YUN:
                    parseUpdateYunResult((String)msg.obj);
                    break;
                case EXIT:
                    isExit = false;
                    break;
                case CHECK_AUTH_TIME:
                    pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    long lastSyncTime = pref.getLong("last_sync_time", 0);
                    // 如果超过两个星期未同步数据
                    if(Long.valueOf(msg.obj.toString()) - lastSyncTime > 2 * 7 * 24 * 60 * 60 * 1000) {
//                        if(!HttpUtil.checkConnection(MainActivity.this)) {
//                            showConnectWifiDialog();
//                            disableLayout(true);
//                            return;
//                        } else {
//                            syncData();
//                        }
                        syncData();
                    }
                    // 如果授权截止时间早于当前时间，禁掉除基本设置之外的所有按钮
                    if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 <= Long.valueOf(msg.obj.toString())) {
                        disableLayout(false);
                        tvAuthExpire.setVisibility(View.VISIBLE);
                    } else {
                        enableLayout();
                        tvAuthExpire.setVisibility(View.GONE);
                    }
                    break;
                case UPDATE_AUTH:
                    parseAuthorizeRecordResult((String) msg.obj);
                    break;
                case SAVE_SYNC_TIME:
                    pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putLong("last_sync_time", Long.parseLong((String) msg.obj.toString()));
                    edit.commit();
                    break;
                case CHECK_EXPIRE_TIME:
                    if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 > Long.valueOf(msg.obj.toString())
                            &&(Long.parseLong(myApp.getUser().getEnd_time()) - Long.valueOf(msg.obj.toString()) / 1000) < 7 * 24 * 60 * 60) {
                        // 显示剩余天数的dialog
                        showAuthExpireDialog((Long.parseLong(myApp.getUser().getEnd_time()) - Long.valueOf(msg.obj.toString()) / 1000) / (24 * 60 * 60) + "");
                        // 每三个小时提示用户进行授权
                        new Thread() {
                            @Override
                            public void run() {
                                while(true) {
                                    Message msg = handler.obtainMessage();
                                    msg.what = SHOW_EXPIRE_DIALOG;
                                    handler.sendMessage(msg);
                                    try {
                                        Thread.sleep(3 * 60 * 60 * 1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }.start();
                    }
                    break;
            }
        }
    };

    private void parseUpdateYunResult(String result) {
        System.out.println("数据同步好了");
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(MainActivity.this, result);
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.getInt("code") == 1) {
                        saveInfoToPref();
                        if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 > System.currentTimeMillis()) {
                            enableLayout();
                        }
                        if(resultObject.getString("message").equals("数据为空")) {
                            return;
                        }
                        sendNotification("数据同步成功！");
                    } else {
                        restoreInfoToPref();
                        sendNotification("数据同步失败！");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendNotification(String result) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.icon = R.drawable.logo_48;
        notification.tickerText = "简易仓库提示";
        notification.defaults = Notification.DEFAULT_SOUND;
        notification.audioStreamType = AudioManager.ADJUST_LOWER;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent();
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        notification.setLatestEventInfo(this, "提示信息", result, pendIntent);
        manager.notify(1, notification);
    }

    private void restoreInfoToPref() {
        try {
            JSONArray uploadDataArray = new JSONArray(uploadData);
            JSONArray tempArray = new JSONArray("[]");
            for(int i = 0; i < myApp.getDataArray().length(); i++) {
                tempArray.put(myApp.getDataArray().getJSONObject(i));
            }
            myApp.setDataArray(new JSONArray("[]"));
            for(int i = 0; i < uploadDataArray.length(); i++) {
                myApp.getDataArray().put(uploadDataArray.getJSONObject(i));
            }
            for(int i = 0; i < tempArray.length(); i++) {
                myApp.getDataArray().put(tempArray.getJSONObject(i));
            }
            myApp.saveDataToPref();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
        getSystemScreenWidth();
        initDatabase();
        // 获取设备的imei码
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        imei = telephonyManager.getDeviceId();
//        imei = "865959021323341";
        // 注册EventBus
        EventBus.getDefault().register(this);
        // 设置定时任务进行数据同步
        setSyncAlarmManager();
        // 设置定时任务进行授权更新
        setUpdateAlarmManager();
        //如果离授权截止日期小于7天，提示用户进行授权
        if(HttpUtil.checkConnection(MainActivity.this)) {
            new Thread() {
                @Override
                public void run() {
                    URL url = null;//取得资源对象
                    try {
                        url = new URL("http://www.baidu.com");
                        URLConnection uc = url.openConnection();//生成连接对象
                        uc.connect(); //发出连接
                        long timeStamp = uc.getDate(); //取得网站日期时间
                        System.out.println("网络时间戳:" + timeStamp);
                        Message msg = handler.obtainMessage();
                        msg.obj = timeStamp;
                        msg.what = CHECK_EXPIRE_TIME;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } else {
            if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 > System.currentTimeMillis()
                    &&(Long.parseLong(myApp.getUser().getEnd_time()) - System.currentTimeMillis() / 1000) < 7 * 24 * 60 * 60) {
                // 显示剩余天数的dialog
                showAuthExpireDialog((Long.parseLong(myApp.getUser().getEnd_time()) - System.currentTimeMillis() / 1000) / (24 * 60 * 60) + "");
                // 每三个小时提示用户进行授权
                new Thread() {
                    @Override
                    public void run() {
                        while(true) {
                            Message msg = handler.obtainMessage();
                            msg.what = SHOW_EXPIRE_DIALOG;
                            handler.sendMessage(msg);
                            try {
                                Thread.sleep(3 * 60 * 60 * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }.start();
            }
        }

        // 启动自动下载更新的线程
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        autoDownload = pref.getBoolean("auto_download", false);
        if(autoDownload) {
            myApp.getMyThread().start();
        }
        // 如果WIFI连接正常，检查更新
        if(HttpUtil.checkConnection(MainActivity.this)) {
            updateManager = UpdateManager.getInstance();
            updateManager.setmContext(this);
            updateManager.setScreenWidth(screenWidth);
            updateManager.checkUpdate();
        }
    }

    private void initDatabase() {
        dbContext = new DatabaseContext(this);
        dbHelper = DatabaseHelper.getInstance(dbContext);
    }

    public void saveVersionInfoToPref(String version, String url, String name, String force_update) {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("version", version);
        edit.putString("url", url);
        edit.putString("apk_name", name);
        edit.putString("force_update", force_update);
        edit.commit();
    }

    private void setSyncAlarmManager() {
        Intent intent = new Intent(MainActivity.this, SyncService.class);
        PendingIntent sender = PendingIntent.getService(MainActivity.this, 0, intent, 0);
        long systemTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = 1000L * 60 * 60;
        System.out.println("time=" + time);
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC,
                time, 1000L * 60 * 60, sender);
    }

    /*private void setUpdateAlarmManager() {
        Intent intent = new Intent(MainActivity.this, UpdateAuthService.class);
        PendingIntent sender = PendingIntent.getService(MainActivity.this, 0, intent, 0);
        long systemTime = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 18);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if (systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        System.out.println("time=" + time);
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        manager.set(AlarmManager.RTC, System.currentTimeMillis() + 15 * 1000, sender);
            manager.setRepeating(AlarmManager.RTC,
                time, 1000L * 10, sender);
    }*/

    private void setUpdateAlarmManager() {
        Intent intent = new Intent(MainActivity.this, UpdateAuthService.class);
        PendingIntent sender = PendingIntent.getService(MainActivity.this, 0, intent, 0);

        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;
        System.out.println("firstTime=" + firstTime);
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                firstTime, 1000L * 60 * 60 * 24, sender);
    }

    @Subscribe
    public void onEventMainThread(SyncEvent event) {
        System.out.println("onEventMainThread.开始同步");
        // 如果网络已连接
        if(HttpUtil.checkConnection(MainActivity.this)) {
            syncData();
        }
    }

    @Subscribe
    public void onEventMainThread(UpdateAuthEvent event) {
        System.out.println("onEventMainThread.开始更新授权");
        // 如果网络已连接
        if(HttpUtil.checkConnection(MainActivity.this)) {
            updateAuth();
        }
    }

    private void updateAuth() {
        new Thread() {
            @Override
            public void run() {
                String timeStamp = System.currentTimeMillis() / 1000 + "";
                String originSign = secretKey + "appKey" + appKey + "imei"+ imei + "timestamp" + timeStamp + secretKey;
                String sign = MD5.GetMD5Code(originSign);
                String result = DataProvider.authorizeRecord(imei, sign.toUpperCase(), timeStamp);
                Message msg = handler.obtainMessage();
                msg.obj = result;
                msg.what = UPDATE_AUTH;
                handler.sendMessage(msg);
            }
        }.start();
    }

    private void parseAuthorizeRecordResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                return;
            } else {
                try {
                    JSONObject resultObject = new JSONObject(result);
                    if (resultObject.getInt("code") == 1) {
                        JSONArray dataArray = resultObject.getJSONArray("data");
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete("tp_authorize_record", null, null);
                        for (int i = 0; i < dataArray.length(); i++) {
                            JSONObject obj = dataArray.getJSONObject(i);
                            if(i == 0) {
                                myApp.getUser().setEnd_time(obj.getString("end_time"));
                                // 把最新的记录存入SharedPreference
                                pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putLong("end_time", Long.parseLong(obj.getString("end_time")));
                                edit.commit();
                            }
                            ContentValues recordCV = new ContentValues();
                            recordCV.put("authorize_time", obj.getString("authorize_time"));
                            recordCV.put("validity_time", obj.getString("validity_time"));
                            recordCV.put("end_time", obj.getString("end_time"));
                            db.insert("tp_authorize_record", null, recordCV);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 判断用户是否单击的是返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 如果isExit标记为false，提示用户再次按键
            if (!isExit) {
                isExit = true;
                CustomViewUtil.createToast(MainActivity.this, "再按一次退出应用！");
                // 如果用户没有在2秒内再次按返回键的话，就发送消息标记用户为不退出状态
                Message msg = handler.obtainMessage();
                msg.what = EXIT;
                handler.sendMessageDelayed(msg, 2000);
            } else {
                // 退出程序
                finish();
                System.exit(0);
            }
        }
        return false;
    }

    public void showConnectWifiDialog() {
        if(connectWifiDialog != null && connectWifiDialog.isShowing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.connect_wifi_dialog, null);
        builder.setCancelable(false);
        connectWifiDialog = builder.create();
        connectWifiDialog.show();
        connectWifiDialog.getWindow().setContentView(view);
        connectWifiDialog.getWindow().setLayout((int) (0.9 * screenWidth), (int) (0.7 * screenWidth));
        connectWifiDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_connect_wifi_dialog);
        Button btnSetWifi = (Button) view.findViewById(R.id.btn_set_wifi_connect_wifi_dialog);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWifiDialog.dismiss();
            }
        });
        btnSetWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWifiDialog.dismiss();
                Intent wifiSettingsIntent = new Intent("android.settings.WIFI_SETTINGS");
                startActivityForResult(wifiSettingsIntent, 123);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                if(HttpUtil.checkConnection(MainActivity.this)) {
                    syncData();
                }
                break;
        }
    }

    private void getInfoFromPref() {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        // 获取需要同步的数据
        uploadData = pref.getString("data", "[]");
        // 重置data
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("data", "[]");
        edit.commit();
        try {
            JSONArray dataArray = new JSONArray("[]");
            myApp.setDataArray(dataArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveInfoToPref() {
        new Thread() {
            @Override
            public void run() {
                URL url = null;//取得资源对象
                try {
                    url = new URL("http://www.baidu.com");
                    URLConnection uc = url.openConnection();//生成连接对象
                    uc.connect(); //发出连接
                    long timeStamp = uc.getDate(); //取得网站日期时间
                    System.out.println("网络时间戳:" + timeStamp);
                    Message msg = handler.obtainMessage();
                    msg.obj = timeStamp;
                    msg.what = SAVE_SYNC_TIME;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void getSystemScreenWidth() {
        WindowManager wm = getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
    }

    private void initData() {
        tvShopName.setText((myApp.getUser().getStore_name() == null || myApp.getUser().getStore_name().equals("")) ? "商铺名" : myApp.getUser().getStore_name());
        tvUserName.setText((myApp.getUser().getUser_name() == null || myApp.getUser().getUser_name().equals("")) ? "用户名" : myApp.getUser().getUser_name());
    }

    @Override
    protected void onResume() {
        System.out.println("MainActivity.onResume");
        super.onResume();
        initData();
        // 如果网络可用，根据网络时间判断授权情况
        if(HttpUtil.checkConnection(MainActivity.this)) {
            new Thread() {
                @Override
                public void run() {
                    URL url = null;//取得资源对象
                    try {
                        url = new URL("http://www.baidu.com");
                        URLConnection uc = url.openConnection();//生成连接对象
                        uc.connect(); //发出连接
                        long timeStamp = uc.getDate(); //取得网站日期时间
                        System.out.println("网络时间戳:" + timeStamp);
                        Message msg = handler.obtainMessage();
                        msg.obj = timeStamp;
                        msg.what = CHECK_AUTH_TIME;
                        handler.sendMessage(msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        //如果网络不可用，根据系统时间判断授权情况
        else {
            pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            long lastSyncTime = pref.getLong("last_sync_time", 0);
            // 如果超过两个星期未同步数据
            System.out.println(System.currentTimeMillis() + "===========================");
            System.out.println("lastSyncTime=" + lastSyncTime);
            if(System.currentTimeMillis() - lastSyncTime > 2 * 7 * 24 * 60 * 60 * 1000) {
//                if(!HttpUtil.checkConnection(MainActivity.this)) {
//                    showConnectWifiDialog();
//                    disableLayout(true);
//                    return;
//                } else {
//                    syncData();
//                }
                showConnectWifiDialog();
                disableLayout(true);
                return;
            }
            // 如果授权截止时间早于当前时间，禁掉除基本设置之外的所有按钮
            System.out.println("myApp.getUser().getEnd_time()=" + Long.parseLong(myApp.getUser().getEnd_time()) * 1000 + "System.currentTimeMillis()=" + System.currentTimeMillis());
            if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 <= System.currentTimeMillis()) {
                disableLayout(false);
                tvAuthExpire.setVisibility(View.VISIBLE);
            } else {
                enableLayout();
                tvAuthExpire.setVisibility(View.GONE);
            }
        }
    }

    private void syncData() {
        getInfoFromPref();
        try {
            JSONArray array = new JSONArray(uploadData);
//            if(array.length() > 0) {
                new Thread() {
                    @Override
                    public void run() {
                        String timeStamp = System.currentTimeMillis() / 1000 + "";
                        String originSign = secretKey + "appKey" + appKey + "data"+ uploadData + "timestamp" + timeStamp + secretKey;
                        String sign = MD5.GetMD5Code(originSign);
                        String result = DataProvider.updateYun(uploadData, sign.toUpperCase(), timeStamp);
                        Message msg = handler.obtainMessage();
                        msg.obj = result;
                        msg.what = UPDATE_YUN;
                        handler.sendMessage(msg);
                    }
                }.start();
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if(myApp.getMyThread() != null && myApp.getMyThread().isAlive()) {
            myApp.getMyThread().interrupt();
        }
    }

    public void showAuthExpireDialog(String leftTime) {
        if(authExpireDialog != null && authExpireDialog.isShowing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.authorization_expire_dialog, null);
        authExpireDialog = builder.create();
        authExpireDialog.show();
        authExpireDialog.getWindow().setContentView(view);
        authExpireDialog.getWindow().setLayout((int) (0.9 * screenWidth), (int) (0.7 * screenWidth));
        authExpireDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_dialog_authorization_expire_dialog);
        TextView tvLeftTime = (TextView) view.findViewById(R.id.tv_left_time_authorization_expire_dialog);
        if(Integer.parseInt(leftTime) == 0) {
            tvLeftTime.setText("您的授权即将到期");
        } else {
            tvLeftTime.setText("您的授权将在" + leftTime + "天后到期");
        }
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authExpireDialog.dismiss();
            }
        });
    }

    private void enableLayout() {
        rlInStorage.setEnabled(true);
        tvInStorage.setTextColor(0xFFFFFFFF);
        rlOutStorage.setEnabled(true);
        tvOutStorage.setTextColor(0xFFFFFFFF);
        rlCheck.setEnabled(true);
        tvCheckStorage.setTextColor(0xFFFFFFFF);
        rlRecord.setEnabled(true);
        tvRecord.setTextColor(0xFFFFFFFF);
        rlScanCodeQueryStorage.setEnabled(true);
        tvScanCode.setTextColor(0xFFFFFFFF);
        rlStoreInfo.setEnabled(true);
        tvStoreInfo.setTextColor(0xFFFFFFFF);
//        rlHelp.setEnabled(true);
//        tvHelp.setTextColor(0xFFFFFFFF);
//        rlSetting.setEnabled(true);
//        tvSetting.setTextColor(0xFFFFFFFF);
    }

    private void disableLayout(boolean disableSetting) {
        rlInStorage.setEnabled(false);
        tvInStorage.setTextColor(0xFF767676);
        rlOutStorage.setEnabled(false);
        tvOutStorage.setTextColor(0xFF767676);
        rlCheck.setEnabled(false);
        tvCheckStorage.setTextColor(0xFF767676);
        rlRecord.setEnabled(false);
        tvRecord.setTextColor(0xFF767676);
        rlScanCodeQueryStorage.setEnabled(false);
        tvScanCode.setTextColor(0xFF767676);
        rlStoreInfo.setEnabled(false);
        tvStoreInfo.setTextColor(0xFF767676);
//        rlHelp.setEnabled(false);
//        tvHelp.setTextColor(0xFF767676);
//        if(disableSetting) {
//            rlSetting.setEnabled(false);
//            tvSetting.setTextColor(0xFF767676);
//        }
    }

    private void addListeners() {
        rlInStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationSelectActivity.class);
                intent.putExtra("from", "in");
                startActivity(intent);
            }
        });
        rlOutStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationSelectActivity.class);
                intent.putExtra("from", "out");
                startActivity(intent);
            }
        });
        rlCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LocationSelectActivity.class);
                intent.putExtra("from", "check");
                startActivity(intent);
            }
        });
        rlStoreInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, StoreInfoActivity.class);
                startActivity(intent);
            }
        });
        rlSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });
        rlRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RecordListActivity.class);
                startActivity(intent);
            }
        });
        rlScanCodeQueryStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanCodeQueryStorageActivity.class);
                startActivity(intent);
            }
        });
        rlHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void findViews() {
        tvShopName = (TextView) findViewById(R.id.tv_shop_name_main);
        tvUserName = (TextView) findViewById(R.id.tv_user_name_main);
        tvAuthExpire = (TextView) findViewById(R.id.tv_auth_expire_main);
        rlInStorage = (RelativeLayout) findViewById(R.id.rl_in_storage_main);
        rlOutStorage = (RelativeLayout) findViewById(R.id.rl_out_storage_main);
        rlCheck = (RelativeLayout) findViewById(R.id.rl_check_main);
        rlStoreInfo = (RelativeLayout) findViewById(R.id.rl_store_info_main);
        rlSetting = (RelativeLayout) findViewById(R.id.rl_setting_main);
        rlRecord = (RelativeLayout) findViewById(R.id.rl_query_record_main);
        rlScanCodeQueryStorage = (RelativeLayout) findViewById(R.id.rl_scan_code_main);
        rlHelp = (RelativeLayout) findViewById(R.id.rl_help_main);
        tvInStorage = (TextView) findViewById(R.id.tv_in_storage_main);
        tvOutStorage = (TextView) findViewById(R.id.tv_out_storage_main);
        tvCheckStorage = (TextView) findViewById(R.id.tv_check_storage_main);
        tvRecord = (TextView) findViewById(R.id.tv_query_record_main);
        tvScanCode = (TextView) findViewById(R.id.tv_scan_code_main);
        tvStoreInfo = (TextView) findViewById(R.id.tv_store_info_main);
        tvHelp = (TextView) findViewById(R.id.tv_help_main);
        tvSetting = (TextView) findViewById(R.id.tv_setting_main);
    }
}
