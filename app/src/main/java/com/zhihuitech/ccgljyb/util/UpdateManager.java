package com.zhihuitech.ccgljyb.util;

import java.net.MalformedURLException;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.logging.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zhihuitech.ccgljyb.MainActivity;
import com.zhihuitech.ccgljyb.R;
import com.zhihuitech.ccgljyb.provider.DataProvider;
import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.versionCode;
import static android.R.attr.versionName;
import static com.zhihuitech.ccgljyb.util.CustomViewUtil.dialog;

public class UpdateManager {
    private static final String TAG = "UpdateManager";

    /* 下载中 */
    private static final int DOWNLOADING = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    /* 获取版本信息 */
    private static final int GET_VERSION = 4;
    /* 保存解析的XML信息 */
    HashMap<String, String> mHashMap = new HashMap<String, String>();
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;
    /* 更新进度条 */
    private NumberProgressBar mProgress;
    private Dialog mDownloadDialog;

    private int screenWidth;

    private boolean forceUpdate = true;

    private AlertDialog updateTipDialog;
    private AlertDialog updateForceDialog;
    private AlertDialog downloadApkDialog;

    private String appKey = "4654501791a54475b93e4e0834b30570";
    private String secretKey = "32faebaa8130cc06914b974b6427725b7440f727";

    private static UpdateManager instance;

    private UpdateManager () {

    }

    public static synchronized UpdateManager getInstance() {
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOADING:
                    // 设置进度条位置
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
                    installApk();
                    break;
                case GET_VERSION:
                    parseGetVersionResult((String)msg.obj);
                    if(mHashMap == null || mHashMap.size() == 0) {
                        CustomViewUtil.createErrorToast(mContext, "无法获取版本信息！");
                        return;
                    }
                    if(checkIfNeedUpdate()) {
                        if(mHashMap.get("force_update").toString().equals("1")) {
                            forceUpdate = true;
                            showForceUpdateNoticeDialog();
                        } else {
                            forceUpdate = false;
                            // 显示提示对话框
                            showNoticeDialog();
                        }
                    } else {
                        forceUpdate = false;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void parseGetVersionResult(String result) {
        if (result != null && !result.equals("")) {
            if (result.contains("超时")) {
                CustomViewUtil.createErrorToast(mContext, "获取版本信息" + result);
                return;
            } else {
                try {
                    mHashMap.clear();
                    JSONObject resultObject = new JSONObject(result);
                    if(resultObject.has("code") && resultObject.getInt("code") == 1) {
                        if(resultObject.has("data") && !resultObject.isNull("data")) {
                            JSONObject dataObject = resultObject.getJSONObject("data");
                            mHashMap.put("version", dataObject.getString("version"));
                            mHashMap.put("name", dataObject.getString("name"));
                            mHashMap.put("url", dataObject.getString("url"));
                            mHashMap.put("force_update", dataObject.getString("force_update"));
                            ((MainActivity)mContext).saveVersionInfoToPref(dataObject.getString("version"), dataObject.getString("url"), dataObject.getString("name"), dataObject.getString("force_update"));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {
        if(forceUpdate) {
            new Thread() {
                @Override
                public void run() {
                    String timeStamp = System.currentTimeMillis() / 1000 + "";
                    String originSign = secretKey + "appKey" + appKey + "timestamp" + timeStamp + secretKey;
                    String sign = MD5.GetMD5Code(originSign);
                    String result = DataProvider.getVersion(sign.toUpperCase(), timeStamp);
                    Message msg = mHandler.obtainMessage();
                    msg.what = GET_VERSION;
                    msg.obj = result;
                    mHandler.sendMessage(msg);
                }
            }.start();
        }
    }

    private boolean checkIfNeedUpdate() {
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);
        double versionName = getVersionName(mContext);
        if (null != mHashMap) {
            Log.i(TAG, "mHashMap=" + mHashMap);
            double serviceCode = Double.parseDouble(mHashMap.get("version"));
            Log.i(TAG, "serviceCode=" + serviceCode);
            // 版本判断
            if (serviceCode > versionName) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查软件是否有更新版本
     * @return
     */
    private boolean isUpdate(InputStream inStream) {
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);
        // 解析XML文件。 由于XML文件比较小，因此使用DOM方式进行解析
        ParseXmlService service = new ParseXmlService();
        try {
            mHashMap = service.parseXml(inStream);
            if (null != mHashMap) {
                Log.i(TAG, "mHashMap=" + mHashMap);
                int serviceCode = Integer.valueOf(mHashMap.get("version"));
                Log.i(TAG, "serviceCode=" + serviceCode);
                // 版本判断
                if (serviceCode > versionCode) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 根据URL得到输入流
     *
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public InputStream getInputStreamFromUrl(String urlStr)  {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            return urlConn.getInputStream();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("com.zhihuitech.ccgljyb", 0).versionCode;
            Log.i(TAG, "versionCode=" + versionCode);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private double getVersionName(Context context) {
        double versionName = 0.0;
        try {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionName
            versionName = Double.parseDouble(context.getPackageManager().getPackageInfo("com.zhihuitech.ccgljyb", 0).versionName);
            Log.i(TAG, "versionName=" + versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {
        if (updateTipDialog != null && updateTipDialog.isShowing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.update_tip_dialog, null);
        updateTipDialog = builder.create();
        updateTipDialog.show();
        updateTipDialog.getWindow().setContentView(view);
        updateTipDialog.getWindow().setLayout((int) (0.85 * screenWidth), (int) (0.6 * screenWidth));
        updateTipDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Button btnUpdateNow = (Button) view.findViewById(R.id.btn_update_now_update_tip_dialog);
        Button btnUpdateLater = (Button) view.findViewById(R.id.btn_update_later_update_tip_dialog);
        ImageView ivCloseDialog = (ImageView) view.findViewById(R.id.iv_close_dialog_update_tip_dialog);
        ivCloseDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTipDialog.dismiss();
            }
        });
        btnUpdateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTipDialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        btnUpdateLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTipDialog.dismiss();
            }
        });
    }

    /**
     * 显示软件更新对话框
     */
    private void showForceUpdateNoticeDialog() {
        if (updateForceDialog != null && updateForceDialog.isShowing()) {
            return;
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.update_force_dialog, null);
        builder.setCancelable(false);
        updateForceDialog = builder.create();
        updateForceDialog.show();
        updateForceDialog.getWindow().setContentView(view);
        updateForceDialog.getWindow().setLayout((int) (0.85 * screenWidth), (int) (0.6 * screenWidth));
        updateForceDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Button btnUpdateNow = (Button) view.findViewById(R.id.btn_update_now_update_force_dialog);
        btnUpdateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateForceDialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.soft_update_dialog, null);
        builder.setCancelable(false);
        downloadApkDialog = builder.create();
        downloadApkDialog.show();
        downloadApkDialog.getWindow().setContentView(view);
        downloadApkDialog.getWindow().setLayout((int) (0.8 * screenWidth), (int) (0.6 * screenWidth));
        downloadApkDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mProgress = (NumberProgressBar) view.findViewById(R.id.pb_update_progress_soft_update_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_soft_update_dialog);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadApkDialog.dismiss();
                // 设置取消状态
                cancelUpdate = true;
            }
        });
        // 下载文件
        new DownloadApkThread().start();
    }

    /**
     * 下载文件线程
     *
     */
    private class DownloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String root = Environment.getExternalStorageDirectory() + "/";
                    mSavePath = root + "ccgl";
                    URL url = new URL(mHashMap.get("url"));
                    System.out.println("download_url=" + url);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    System.out.println("apk.length=" + length);
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePath);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePath + "/" + mHashMap.get("name"));
                    if(apkFile.exists()) {
                        System.out.println("apkFile.length=" + apkFile.length());
                        if(length == apkFile.length()) {
                            downloadApkDialog.dismiss();
                            installApk();
                            return;
                        } else {
                            apkFile.delete();
                        }
                    }
                    apkFile = new File(mSavePath, mHashMap.get("name"));
                    FileOutputStream fos = new FileOutputStream(apkFile);
                    int count = 0;
                    // 缓存
                    byte buf[] = new byte[1024];
                    // 写入到文件中
                    do {
                        int numread = is.read(buf);
                        count += numread;
                        // 计算进度条位置
                        progress = (int) (((float) count / length) * 100);
                        // 更新进度
                        mHandler.sendEmptyMessage(DOWNLOADING);
                        if (numread <= 0) {
                            // 下载完成
                            mHandler.sendEmptyMessage(DOWNLOAD_FINISH);
                            break;
                        }
                        // 写入文件
                        fos.write(buf, 0, numread);
                    } while (!cancelUpdate);// 点击取消就停止下载.
                    fos.close();
                    is.close();
                }
            } catch (MalformedURLException e) {
                System.out.println("MalformedURLException=" + e.toString());
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("IOException=" + e.toString());
                e.printStackTrace();
            }
            // 取消下载对话框显示
            downloadApkDialog.dismiss();
        }
    }

    /**
     * 安装APK文件
     */
    private void installApk() {
        File apk = new File(mSavePath, mHashMap.get("name"));
        if (!apk.exists()) {
            return;
        }
        // 通过Intent安装APK文件
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apk.toString()), "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public HashMap<String, String> getmHashMap() {
        return mHashMap;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }
}
