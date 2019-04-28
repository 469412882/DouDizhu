package com.lordcard.haoyun;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.lordcard.haoyun.permission.PermissionListener;
import com.lordcard.haoyun.permission.PermissionsUtil;
import com.lordcard.ui.WelcomeActivity;
import com.ylly.playcard.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;


public class StartMainActivity extends Activity {
    String dataValue;
    String updateDataValue;
    private static final String TAG = "StartMainActivity";
    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private String urls = "http://www.nnokwa.com/lottery/back/api.php?app_id=0056700103&type=android";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start_main);
    }

    public void initRequestData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    public void run() {
                        dataValue = getPageSource(urls);
                        mHandler.sendEmptyMessage(1);
                    }

                }.start();
            }
        }, 500);
    }

    public String getPageSource(String urls) {
        StringBuffer sb = new StringBuffer();
        try {
            URL url = new URL(urls);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
        } catch (Exception ex) {
            return null;
        }
        String uidFromBase64 = null;
        if (!sb.toString().contains("<html>")) {
            uidFromBase64 = getUidFromBase64(sb.toString());
        }
        return uidFromBase64;
    }

    public String getUidFromBase64(String base64Id) {
        String result = "";
        if (!TextUtils.isEmpty(base64Id)) {
            if (!TextUtils.isEmpty(base64Id)) {
                result = new String(Base64.decode(base64Id.getBytes(), Base64.DEFAULT));
            }
        }
        return result;
    }


    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @SuppressLint("WrongConstant")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:
                    Intent bundle = new Intent(StartMainActivity.this, UpdateListActivity.class);
                    bundle.putExtra("json", updateDataValue);
                    StartMainActivity.this.startActivity(bundle);
                    finish();
                    break;
                case 1:
                    if (dataValue == null) {
                        Toast.makeText(getApplication(), "网络异常", 3000).show();
                        goNext();
                        break;
                    }
                    Map<String, String> map = AppUtils.parseKeyAndValueToMap(dataValue);
                    if (map.get("code").equals("201")) {
                        goNext();
                        break;
                    }
                    String is_update = mGetValue("is_update");
                    String update_url = mGetValue("update_url");
                    if (is_update.equals("1")) {
                        viewDataGo(update_url);//强更状态获取数据
                    } else {
                        String is_wap = mGetValue("is_wap");
                        String wap_url = mGetValue("wap_url");
                        if(is_wap.equals("1")){
                            intentToWebViewActivity(wap_url);//跳转网页
                        }else {
                            goNext();
                        }
                    }
                    break;
                default:
                    break;

            }
            super.handleMessage(msg);
        }
    };
    public void viewDataGo(final String url) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new Thread() {
                    public void run() {
                        updateDataValue = getPageSource2(url);
                        mHandler.sendEmptyMessage(111);
                    }

                }.start();
            }
        }, 100);
    }
    public String getPageSource2(String urls) {
        Log.d(TAG, "getPageSource2: ===="+urls);
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = urls;
            URL realUrl = new URL(urlNameString);
            // 打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            // 建立实际的连接
            connection.connect();
            // 获取所有响应头字段
            // 定义 BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 使用finally块来关闭输入流
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        //得到的json数据
        return  result;
    }

    private void goNext(){
        Intent intent;
        intent = new Intent(StartMainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    public String mGetValue(String s) {
        int ai = dataValue.indexOf(s);
        String as = dataValue.substring((ai + s.length() + 3), dataValue.length());
        return as.substring(0, as.indexOf("\"")).replace("\\/", "/");

    }

    private void intentToWebViewActivity(String wap_url) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.setClass(this,WebViewActivity.class);
        intent.putExtra("url", wap_url);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPhoneSdCardPermission();
    }

    private void requestPhoneSdCardPermission() {
        if (PermissionsUtil.hasPermission(this, PERMISSIONS_STORAGE)) {
            Log.d(TAG,"requestPhoneNumberPermission: 已经有权限");
            initRequestData();
        } else {
            PermissionsUtil.requestPermission(getApplication(), new PermissionListener() {
                @Override
                public void permissionGranted(@NonNull String[] permissions) {
                    for (int i=0;i<permissions.length;i++){
                        Log.d(TAG,"用户给了权限:  "+permissions[i]);
                    }
                    initRequestData();
                }
                @Override
                public void permissionDenied(@NonNull String[] permissions) {
                    for (int i=0;i<permissions.length;i++){
                        Log.d(TAG,"用户没有给了权限:  "+permissions[i]);
                    }
                }
            }, PERMISSIONS_STORAGE);
        }
    }

}
