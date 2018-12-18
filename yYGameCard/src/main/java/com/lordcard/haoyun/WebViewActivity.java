package com.lordcard.haoyun;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;

import java.io.File;


public class WebViewActivity extends FragmentActivity {
    private String TAG = "WebViewActivity";
    WebView mWebView;
    //    退出时间
    private String url;
    WebSettings webSettings = null;
    private String packName;
    private String img_bg_url;
    private ImageView mImageView;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,  WindowManager.LayoutParams.FLAG_FULLSCREEN);
        boolean networkConnected = isNetworkConnected(this);
        if(networkConnected==false){
            initGoActivity();
            return;
        }
        initView();
    }

    private void initGoActivity() {
        Toast.makeText(this,"网络异常，请稍后再试试",Toast.LENGTH_SHORT).show();
    }

    private void initView() {
        url = getIntent().getStringExtra("url");
        packName = getIntent().getStringExtra("packName");
        img_bg_url = getIntent().getStringExtra("img_bg_url");
        if (!url.startsWith("http")) {
            url = "http://" + url;
        }
        if (url.endsWith(".apk")) {
            mImageView = new ImageView(this);
            setContentView(mImageView);
            if (AppUtils.isAppExist(this, packName)) {
                Intent intent = new Intent();
                PackageManager packageManager = this.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(packName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                this.startActivity(intent);
                finish();
                return;
            } else {
                showDownloadDialog(this);
                downloadApk(url);
                if (!TextUtils.isEmpty(img_bg_url)) {
                    AppUtils.disImage(this, img_bg_url, mImageView);
                }

            }

        }else {
            mWebView = new WebView(this);
            setContentView(mWebView);
            mWebView.loadUrl(url);
            this.webSettings = this.mWebView.getSettings();
            this.webSettings.setJavaScriptEnabled(true);
            this.webSettings.setUseWideViewPort(true);
            this.webSettings.setSupportZoom(true);
            this.webSettings.setBuiltInZoomControls(true);
            this.webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
            this.webSettings.setLoadWithOverviewMode(true);
            this.webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
            if (isNetworkConnected(this)) {
                this.webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
            } else {
                this.webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            }
            this.webSettings.setDomStorageEnabled(true);
            this.webSettings.setDatabaseEnabled(true);
            String cacheDirPath = getCacheDir().getAbsolutePath();
            Log.e(this.TAG, "cacheDirPath=" + cacheDirPath);
            this.webSettings.setDatabasePath(cacheDirPath);
            this.webSettings.setAppCachePath(cacheDirPath);
            this.webSettings.setAppCacheEnabled(true);
            initWebView();
        }


    }
    private void initWebView() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                return super.shouldOverrideUrlLoading(webView, s);
            }
            /***
             * 设置Web视图的方法
             */
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.d(TAG, "onReceivedError: "+failingUrl);
                showErrorPage();//显示错误页面
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                //处理网页加载成功时
                if(url.contains("blank")){
                    showErrorPage();
                    return;
                }

            }

        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            /**
             * 全屏播放配置
             */
            @Override
            public void onHideCustomView() {
                if (mWebView != null) {
                    ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
                    viewGroup.removeView(mWebView);
                }
            }
            @Override
            public void onReceivedTitle(WebView arg0, final String title) {
                super.onReceivedTitle(arg0, title);
                Log.d(TAG, "onReceivedTitle: "+title);

            }
        });
        mWebView.setDownloadListener(new android.webkit.DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.d(TAG, "onDownloadStart: ");
                showDownloadDialog(WebViewActivity.this);
                downloadApk(url);
            }
        });

    }
    protected void showErrorPage() {
        initGoActivity();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mWebView != null) {
            Log.d(TAG, "onDestroy: mWebView");
            final ViewGroup viewGroup = (ViewGroup) mWebView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(mWebView);
            }
            if(mProgressDialog!=null){
                mProgressDialog.dismiss();
                mProgressDialog=null;
            }
            mWebView.removeAllViews();
            mWebView.destroy();
        }
    }


    @Override
    // 设置回退
    // 5、覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下返回键并且webview界面可以返回
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack(); // goBack()表示返回WebView的上一页面
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    /**
     * 判断是否有网络连接
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }
        NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
        if (net_info == null) {
            return false;
        }
        for (NetworkInfo state : net_info) {
            if (state.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }


    public void showDownloadDialog(Activity activity){
        if(mProgressDialog==null&&!activity.isFinishing()){
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setTitle("下载中");
            mProgressDialog.setMessage("更新中...");
            mProgressDialog.show();
            mProgressDialog.setCancelable(false);
        }

    }


    public void downloadApk(String url) {
        ApkDownModel apk1 = new ApkDownModel();
        apk1.url = url;
        String fileName = packName + ".apk";
        Log.d(TAG, "downloadApk: ==="+fileName);
        OkDownload  okDownload = OkDownload.getInstance();
        okDownload.setFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/haocai/");
        GetRequest<File> request = OkGo.<File>get(apk1.url);
        okDownload.request(fileName, request)
                .priority(apk1.priority)
                .extra1(apk1)
                .save().fileName(fileName)
                .start();
        DownloadTask task = OkDownload.restore(DownloadManager.getInstance().get(fileName));
        task .register(new DownloadListener(apk1.url) {
            @Override
            public void onStart(Progress progress) {

            }

            @Override
            public void onProgress(Progress progress) {
                refreshProgress(progress);
            }

            @Override
            public void onError(Progress progress) {

            }

            @Override
            public void onFinish(File file, Progress progress) {
                mProgressDialog.dismiss();
                AppUtils.installApk(WebViewActivity.this,file);
                finish();
            }

            @Override
            public void onRemove(Progress progress) {

            }
        });
    }




    public void refreshProgress(Progress progress) {
        switch (progress.status) {
            case Progress.NONE:
                mProgressDialog.setMessage("已停止");
                break;
            case Progress.PAUSE:
                mProgressDialog.setMessage("暂停中");
                break;
            case Progress.ERROR:
                mProgressDialog.setMessage("下载出错");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WebViewActivity.this,"下载错误",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(WebViewActivity.this,StartMainActivity.class));
                    }
                }, 1000);
                break;
            case Progress.WAITING:
                mProgressDialog.setMessage("等待中...");
                break;
            case Progress.FINISH:
                mProgressDialog.setMessage("下载完成");
                break;
            case Progress.LOADING:
               /* String speed = Formatter.formatFileSize(this, progress.speed);
                mProgressDialog.setMessage(String.format("%s/s", speed));*/
                break;
        }
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress((int) ((progress.fraction) * 100));
    }




}
