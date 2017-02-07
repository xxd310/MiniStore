package com.zhihuitech.ccgljyb;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import com.zhihuitech.ccgljyb.util.HttpUtil;
import com.zhihuitech.ccgljyb.util.UpdateManager;

import static android.R.id.edit;

public class UpdateSettingActivity extends Activity {
    private ImageView ivBack;
    private CheckBox cbAutoUpdate;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "ccgl";
    private MyApplication myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_setting);

        myApp = (MyApplication) getApplication();
        ivBack = (ImageView) findViewById(R.id.iv_back_update_setting);
        cbAutoUpdate = (CheckBox) findViewById(R.id.cb_auto_update_setting);
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        cbAutoUpdate.setChecked(pref.getBoolean("auto_download", false));
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cbAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                if(isChecked) {
                    edit.putBoolean("auto_download", true);
                    if(myApp.getMyThread() != null && !myApp.getMyThread().isAlive()) {
                        myApp.getMyThread().start();
                    }
                } else {
                    edit.putBoolean("auto_download", false);
                    if(myApp.getMyThread() != null && myApp.getMyThread().isAlive()) {
                        myApp.getMyThread().interrupt();
                    }
                }
                edit.commit();
            }
        });
    }
}
