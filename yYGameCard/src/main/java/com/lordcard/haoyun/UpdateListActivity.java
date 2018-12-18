package com.lordcard.haoyun;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.okgo.db.DownloadManager;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;
import com.lzy.okserver.task.XExecutor;
import com.ylly.playcard.R;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.zhy.autolayout.AutoLayoutActivity;
import com.zhy.autolayout.utils.AutoUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class UpdateListActivity extends AutoLayoutActivity implements XExecutor.OnAllTaskEndListener, DownApkListAdapter.OnReloadListener {
    ArrayList<Map<String, String>> list;
    ArrayList<Map<String, String>> banner_list;
    private ArrayList<String> list_path;
    private List<DownloadTask> mDownloadTaskList;
    private OkDownload okDownload;
    private RecyclerView mRecyclerView;
    private ImageView mImg_bg;
    private ImageView img_red_envelope;
    private TextView mTv_title;
    private String json;
    private Map<String, String> stringMap;


    ViewGroup _root;
    private int dy;
    private int dx;
    private int firstX, firstY; //记录第一次按下的坐标
    private boolean isFirst = true;//判断是否是第一次按下按钮
    private int mLeft;
    private int mTop;
    private int mMeasuredWidth;
    private int mMeasuredHeight;
    private int lastX, lastY;
    long startTime = 0;
    private TextView mTv_right;

    private RelativeLayout mRelay_top;
    private Banner mImage_banner;
    private LinearLayout mLl_announcement;
    private ImageView mImg_gb;
    private TextView mTv_pao;
    private View mV_line;

    /**
     * 解决Subscription内存泄露问题
     *
     * @param
     */



    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }



    public void initView() {
        list_path = new ArrayList<>();
        mRecyclerView = findViewById(R.id.recyclerview);
        mImg_bg = findViewById(R.id.img_bg);
        img_red_envelope = findViewById(R.id.img_red_envelope);
        mTv_title = findViewById(R.id.tv_title);
        mTv_right = findViewById(R.id.tv_right);
        mRelay_top = findViewById(R.id.relay_top);
        mImage_banner = findViewById(R.id.image_banner);

        mLl_announcement = findViewById(R.id.ll_announcement);
        mImg_gb = findViewById(R.id.img_gb);
        mTv_pao = findViewById(R.id.tv_pao);
        mV_line = findViewById(R.id.v_line);



    }

    public void requestData() {
        json = getIntent().getStringExtra("json");
        stringMap = AppUtils.parseKeyAndValueToMap(json);
        goNext();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除状态栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去除状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_update_list);
        initView();
        okDownload = OkDownload.getInstance();
        okDownload.addOnAllTaskEndListener(this);

        requestData();
        okDownload.setFolder(Environment.getExternalStorageDirectory().getAbsolutePath() + "/haocai/");
        okDownload.getThreadPool().setCorePoolSize(3);
        _root = (ViewGroup) findViewById(R.id.root);

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        img_red_envelope.measure(w, h);
        mMeasuredWidth = img_red_envelope.getMeasuredWidth();
        mMeasuredHeight = img_red_envelope.getMeasuredHeight();

        img_red_envelope.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        if (isFirst) {
                            firstX = (int) event.getRawX();
                            firstY = (int) event.getRawY();
                            isFirst = false;
                        }
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        mLeft = img_red_envelope.getLeft();
                        mTop = img_red_envelope.getTop();
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_UP:
                        int rawY2 = (int) event.getRawY();
                        int rawX2 = (int) event.getRawX();
                        if (System.currentTimeMillis() - startTime < 100 && Math.abs(rawX2 - lastX) < 20 && Math.abs(rawY2 - lastY) < 20) {
                            if (test != null) {
                                Intent intent = new Intent(UpdateListActivity.this,WebViewActivity.class);
                                intent.putExtra("url", test.get("img_url"));
                                intent.putExtra("packName", test.get("packname"));
                                intent.putExtra("img_bg_url", test.get("img_bg_url"));
                                startActivity(intent);
                            }

                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int rawY = (int) event.getRawY();
                        int rawX = (int) event.getRawX();

                        dy = rawY - firstY > 0 ? rawY - lastY : rawY - lastY;//如果是从起点开始滑动的话，不让按钮向下滑动
                        dx = rawX - firstX > 0 ? rawX - lastX : rawX - lastX;//如果是从起点开始滑动的话，不让按钮向下滑动
                        setButtonMargin(dx, dy);
                        break;

                }
                _root.invalidate();
                return true;
            }
        });
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ObjectAnimator animator = AppUtils.tada(img_red_envelope,1f);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.start();

            }
        }, 1000);

    }


    @Override
    public void OnReload() {
        initData();
    }



    private void setButtonMargin(int dX, int dY) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                AutoUtils.getPercentWidthSize(300), AutoUtils.getPercentHeightSize(300));
        int top = mTop + dY;
        int left = mLeft + dX;
        int l = AppUtils.getScreenWidth(this) - mMeasuredWidth;
        int t = AppUtils.getScreenHeight(this) - mMeasuredHeight - getStatusBarHeight();
        //设置left和top的边界值
        if (left < 0) {
            left = 0;
        } else if (left > l) {
            left = l;
        }
        if (top < 0) {
            top = 0;
        } else if (top > t) {
            top = t;
        }
        layoutParams.topMargin = top;
        layoutParams.leftMargin = left;
        img_red_envelope.setLayoutParams(layoutParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int statusBarHeight;

    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }


    public void initData() {
        mDownloadTaskList = OkDownload.restore(DownloadManager.getInstance().getAll());
        if (mDownApkListAdapter != null) {
            mDownApkListAdapter.notifyDataSetChanged();
        }

    }

    DownApkListAdapter mDownApkListAdapter;


    public void goNext() {
        list = AppUtils.parseKeyAndValueToMapList(stringMap.get("page"));
        for (int i = 0; i < list.size(); i++) {
            list_path.add("");
        }
        goNext2();
        goNext3();

    }

    Map<String, String> test;

    public void goNext2() {
        test = AppUtils.parseKeyAndValueToMap(stringMap.get("test"));
        String img_url = test.get("background");
        String img_url2 = test.get("img");

        if (!img_url.startsWith("http")) {
            mImg_bg.setImageResource(AppUtils.getImageResourceId(img_url));
        } else {
           AppUtils.disImage(UpdateListActivity.this, img_url, mImg_bg);
        }

        if (!img_url2.startsWith("http")) {
            img_red_envelope.setImageResource(AppUtils.getImageResourceId(img_url2));
        } else {
           AppUtils.disImage(UpdateListActivity.this, img_url2, img_red_envelope);
        }

        final Map<String, String> title = AppUtils.parseKeyAndValueToMap(stringMap.get("title"));

        mTv_title.setText(title.get("text"));
        mTv_title.setTextColor(Color.parseColor(title.get("text_color")));
        mRelay_top.setBackgroundColor(Color.parseColor(title.get("text_bg")));
        mTv_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(Integer.parseInt(title.get("text_size"))));

        String text_right = title.get("text_right");
        if(AppUtils.isEmpty(text_right)){
            mTv_right.setVisibility(View.GONE);
        }else {
            mTv_right.setText(title.get("text_right"));
            mTv_right.setTextColor(Color.parseColor(title.get("text_right_color")));
            mTv_right.setTextSize(TypedValue.COMPLEX_UNIT_PX, AutoUtils.getPercentWidthSize(Integer.parseInt(title.get("text_right_size"))));

            final String text_right_type = title.get("text_right_type");
            mTv_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (text_right_type) {
                        case "QQ":
                            try {
                                String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + title.get("text_right_url");
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(UpdateListActivity.this,"当前手机没有安装QQ",Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case "WeiXin":
                            try {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                // 将文本内容放到系统剪贴板里。
                                cm.setText(title.get("text_right_url"));
                                Toast.makeText(UpdateListActivity.this, title.get("text_right_url_w"), Toast.LENGTH_LONG).show();
                                Intent intent = new Intent();
                                ComponentName cmp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                                intent.setAction(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setComponent(cmp);
                                startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(UpdateListActivity.this,"当前手机没有安装微信",Toast.LENGTH_SHORT).show();
                            }

                            break;
                        case "H5":
                            Intent bundle = new Intent(UpdateListActivity.this,WebViewActivity.class);
                            bundle.putExtra("url", title.get("text_right_url"));
                            bundle.putExtra("packName", "");
                            bundle.putExtra("img_bg_url", "");
                            startActivity(bundle);
                            break;
                    }
                }
            });

        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mTv_title.getLayoutParams();
        params.height = AutoUtils.getPercentHeightSize(Integer.parseInt(title.get("text_bg_h")));
        mTv_title.setLayoutParams(params);
    }

    public void goNext3() {
        banner_list = AppUtils.parseKeyAndValueToMapList(stringMap.get("banner"));
        if(banner_list.size()<0){
            mImage_banner.setVisibility(View.GONE);
        }else {
            List<String> bannerImageList=new ArrayList<>();
            for (int i=0;i<banner_list.size();i++){
                String img_url = banner_list.get(i).get("banner");
                bannerImageList.add(img_url);
            }
            mImage_banner.setVisibility(View.VISIBLE);
            mImage_banner.setImageLoader(new AppUtils.GlideImageLoader());
            mImage_banner.setImages(bannerImageList);
            mImage_banner.start();

            mLl_announcement.setVisibility(View.VISIBLE);
            mV_line.setVisibility(View.VISIBLE);
            String img_url3 = test.get("img_gonggao");
            if (!img_url3.startsWith("http")) {
                mImg_gb.setImageResource(AppUtils.getImageResourceId(img_url3));
            } else {
                AppUtils.disImage(UpdateListActivity.this, img_url3, mImg_gb);
            }
            mTv_pao.setText(test.get("text"));

            mImage_banner.setOnBannerListener(new OnBannerListener() {
                @Override
                public void OnBannerClick(int position) {
                    String url = banner_list.get(position).get("banner_url");
                    if (TextUtils.isEmpty(url)) {
                        return;
                    }
                    if (!TextUtils.isEmpty(list.get(position).get("packname")) && AppUtils.isAppExist(UpdateListActivity.this, list.get(position).get("packname"))) {
                        Intent intent = new Intent();
                        PackageManager packageManager = UpdateListActivity.this.getPackageManager();
                        intent = packageManager.getLaunchIntentForPackage(list.get(position).get("packname"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        UpdateListActivity.this.startActivity(intent);
                    }else {
                        if (url.startsWith("http")) {
                            Intent bundle = new Intent(UpdateListActivity.this, WebViewActivity.class);
                            bundle.putExtra("url", banner_list.get(position).get("banner_url"));
                            bundle.putExtra("packName", banner_list.get(position).get("packname"));
                            bundle.putExtra("img_bg_url", banner_list.get(position).get("img_bg_url"));
                            UpdateListActivity.this.startActivity(bundle);
                            return;
                        }
                    }

                }
            });
        }

        LinearLayoutManager manager2 = new LinearLayoutManager(UpdateListActivity.this);
        manager2.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(manager2);
        mDownApkListAdapter = new DownApkListAdapter(UpdateListActivity.this,list);
        mRecyclerView.setAdapter(mDownApkListAdapter);
        mDownApkListAdapter.setOnReloadListener(this);
        mDownloadTaskList = OkDownload.restore(DownloadManager.getInstance().getAll());

    }


    @Override
    public void onAllTaskEnd() {

    }


    /**
     * 双击退出App
     */
    private long exitTime;
    private void exitApp() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    /**
     * 监听back键处理DrawerLayout和SearchView
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitApp();
        }
        return true;
    }




}
