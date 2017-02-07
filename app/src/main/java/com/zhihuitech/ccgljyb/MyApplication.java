package com.zhihuitech.ccgljyb;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import com.zhihuitech.ccgljyb.entity.User;
import com.zhihuitech.ccgljyb.util.HttpUtil;
import com.zhihuitech.ccgljyb.util.StompUtil;
import com.zhihuitech.ccgljyb.util.UpdateManager;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/10/25.
 */
public class MyApplication extends Application {
    private User user;
    private JSONArray dataArray;
    private MyThread myThread;
    private HashMap<String, String> versionMap = new HashMap<String, String>();
    private UpdateManager updateManager;
    private boolean finishDownload = false;

    private SharedPreferences pref = null;
    private final static String PREF_NAME = "ccgl";

    @Override
    public void onCreate() {
        super.onCreate();
        myThread = new MyThread();
        updateManager = UpdateManager.getInstance();
    }

    public void saveDataToPref() {
        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("data", dataArray.toString());
        edit.commit();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            while (!finishDownload) {
                // 如果网络正常，下载最新的安装包
                if(HttpUtil.checkConnection(MyApplication.this)) {
                    try {
                        pref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                        // 判断SD卡是否存在，并且是否具有读写权限
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            // 获得存储卡的路径
                            String root = Environment.getExternalStorageDirectory() + "/";
                            String mSavePath = root + "ccgl";
                            URL url = new URL(pref.getString("url", ""));
                            System.out.println("MyThread.url=" + pref.getString("url", ""));
                            // 创建连接
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.connect();
                            // 获取文件大小
                            int length = conn.getContentLength();
                            // 创建输入流
                            InputStream is = conn.getInputStream();
                            File file = new File(mSavePath);
                            // 判断文件目录是否存在
                            if (!file.exists()) {
                                file.mkdir();
                            }
                            File apkFile = new File(mSavePath + "/" + pref.getString("apk_name", ""));
                            if(apkFile.exists()) {
                                System.out.println("apkFile.length=" + apkFile.length());
                                if(length == apkFile.length()) {
                                    finishDownload = true;
                                    return;
                                } else {
                                    apkFile.delete();
                                }
                            }
                            apkFile = new File(mSavePath, pref.getString("apk_name", ""));
                            FileOutputStream fos = new FileOutputStream(apkFile);
                            int count = 0;
                            // 缓存
                            byte buf[] = new byte[1024];
                            // 写入到文件中
                            do {
                                int numread = is.read(buf);
                                count += numread;
                                if (numread <= 0) {
                                    finishDownload = true;
                                    break;
                                }
                                // 写入文件
                                fos.write(buf, 0, numread);
                            } while (true);
                            fos.close();
                            is.close();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000 * 60);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public JSONArray getDataArray() {
        return dataArray;
    }

    public void setDataArray(JSONArray dataArray) {
        this.dataArray = dataArray;
    }

    public MyThread getMyThread() {
        return myThread;
    }

    public void setMyThread(MyThread myThread) {
        this.myThread = myThread;
    }

    public HashMap<String, String> getVersionMap() {
        return versionMap;
    }

    public void setVersionMap(HashMap<String, String> versionMap) {
        this.versionMap = versionMap;
    }


}
