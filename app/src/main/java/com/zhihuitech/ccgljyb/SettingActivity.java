package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import com.zhihuitech.ccgljyb.util.CustomViewUtil;
import com.zhihuitech.ccgljyb.util.database.DatabaseContext;
import com.zhihuitech.ccgljyb.util.database.DatabaseHelper;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.net.URLConnection;

import static android.R.attr.switchMinWidth;
import static android.R.attr.x;
import static com.zhihuitech.ccgljyb.util.CustomViewUtil.dialog;

public class SettingActivity extends Activity {
    private ImageView ivBack;
    private RelativeLayout rlSetLocation;
    private RelativeLayout rlSetProductType;
    private RelativeLayout rlUpdateSetting;
    private RelativeLayout rlAuthorizationSetting;

    private MyApplication myApp;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // 授权过期
            if(Long.parseLong(myApp.getUser().getEnd_time()) * 1000 < Long.parseLong(msg.obj.toString())) {
                rlSetLocation.setEnabled(false);
                rlSetProductType.setEnabled(false);
                rlUpdateSetting.setEnabled(false);
            } else {
                rlSetLocation.setEnabled(true);
                rlSetProductType.setEnabled(true);
                rlUpdateSetting.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        myApp = (MyApplication) getApplication();
        findViews();
        addListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void addListeners() {
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        rlSetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, LocationSettingActivity.class);
                startActivity(intent);
            }
        });
        rlSetProductType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, ProductTypeSettingActivity.class);
                startActivity(intent);
            }
        });
        rlUpdateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, UpdateSettingActivity.class);
                startActivity(intent);
            }
        });
        rlAuthorizationSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, AuthorizationCenterActivity.class);
                startActivity(intent);
            }
        });
    }


    private void findViews() {
        ivBack = (ImageView) findViewById(R.id.iv_back_setting);
        rlSetLocation = (RelativeLayout) findViewById(R.id.rl_set_location_setting);
        rlSetProductType = (RelativeLayout) findViewById(R.id.rl_set_product_type_setting);
        rlUpdateSetting = (RelativeLayout) findViewById(R.id.rl_update_setting);
        rlAuthorizationSetting = (RelativeLayout) findViewById(R.id.rl_authorization_setting);
    }
}
