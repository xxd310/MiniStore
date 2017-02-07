package com.zhihuitech.ccgljyb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.zhihuitech.ccgljyb.entity.SyncEvent;
import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2016/11/15.
 */
public class SyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "y8392093812983", Toast.LENGTH_LONG).show();
        EventBus.getDefault().post(new SyncEvent("Start_Sync"));
    }
}
