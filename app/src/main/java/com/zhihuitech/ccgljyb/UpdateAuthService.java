package com.zhihuitech.ccgljyb;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zhihuitech.ccgljyb.entity.SyncEvent;
import com.zhihuitech.ccgljyb.entity.UpdateAuthEvent;
import org.greenrobot.eventbus.EventBus;

public class UpdateAuthService extends Service {
    public UpdateAuthService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("UpdateAuthService.onStartCommand");
        EventBus.getDefault().post(new UpdateAuthEvent("开始更新授权"));
        return super.onStartCommand(intent, flags, startId);
    }
}
