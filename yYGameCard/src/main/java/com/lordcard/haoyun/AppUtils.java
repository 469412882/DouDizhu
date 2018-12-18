package com.lordcard.haoyun;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ylly.playcard.R;
import com.youth.banner.loader.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AppUtils {
    private static final String TAG = "AppUtils";
    public static boolean isPrintException = true;

    public static Map<String, String> parseKeyAndValueToMap(JSONObject sourceObj) {
        if (sourceObj == null) {
            return null;
        } else {
            HashMap keyAndValueMap = new HashMap();
            Iterator iter = sourceObj.keys();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                putMapNotEmptyKey(keyAndValueMap, key, getString(sourceObj, key, ""));
            }
            return keyAndValueMap;
        }
    }


    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static  String getString(JSONObject jsonObject, String key, String defaultValue) {
        if (jsonObject != null && !isEmpty(key)) {
            try {
                return jsonObject.getString(key);
            } catch (JSONException var4) {
                if (isPrintException) {
                    var4.printStackTrace();
                }
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }



    public static void disImage(Context context, String imgUrl, ImageView imgv) {
        if(TextUtils.isEmpty(imgUrl)){
            return;
        }
        Glide.with(context)
                .load(imgUrl).diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imgv);
    }


    public static void disImgRound(Context context, String imgUrl, ImageView imgv) {
        Glide.with(context).load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade().into(imgv);

    }



    public static Map<String, String> parseKeyAndValueToMap(String source) {
        if (isEmpty(source)) {
            return null;
        } else {
            try {
                JSONObject e = new JSONObject(source);
                return parseKeyAndValueToMap(e);
            } catch (JSONException var2) {
                if (isPrintException) {
                    var2.printStackTrace();
                }
                return null;
            }
        }
    }

    public static ArrayList<Map<String, String>> parseKeyAndValueToMapList(String source) {
        if (isEmpty(source)) {
            return null;
        } else if (!source.startsWith("[") && !source.endsWith("]")) {
            return null;
        } else {
            try {
                ArrayList e = new ArrayList();
                JSONArray jsonArray = new JSONArray(source);
                for (int i = 0; i < jsonArray.length(); ++i) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    e.add(parseKeyAndValueToMap(jsonObject));
                }
                return e;
            } catch (JSONException var5) {
                if (isPrintException) {
                    var5.printStackTrace();
                }
                return null;
            }
        }
    }

    public static boolean putMapNotEmptyKey(Map<String, String> map, String key, String value) {
        if (map != null && !isEmpty(key)) {
            map.put(key, value);
            return true;
        } else {
            return false;
        }
    }




    public static int getImageResourceId(String imageName) {
        Class drawable = R.drawable.class;
        Field field = null;
        int r_id;
        try {
            field = drawable.getField(imageName);
            r_id = field.getInt(field.getName());
        } catch (Exception e) {
            r_id = R.mipmap.ic_gonggao;
            Log.e("ERROR", "PICTURE NOT　FOUND！");
        }
        return r_id;
    }
        /**
         * 检查手机上是否安装了指定的软件
         *
         * @param context
         * @param packageName：应用包名
         * @return
         */
    public static boolean isAppExist(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<String> packageNames = new ArrayList<String>();
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (int i = 0; i < packageInfos.size(); i++) {
                String packName = packageInfos.get(i).packageName;
                packageNames.add(packName);
            }
        }
        return packageNames.contains(packageName);
    }


    /**
     * 获取屏幕的宽度
     *
     * @return
     */
    public static int getScreenWidth(Activity context) {
        WindowManager windowManager = context.getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        return defaultDisplay.getWidth();
    }

    /**
     * 获取屏幕的高度
     *
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display defaultDisplay = windowManager.getDefaultDisplay();
        return defaultDisplay.getHeight();
    }


    /**
     * 安装apk
     * @param
     */
    public static  void installApk(final Activity context, File apkFile){
        if (!apkFile.exists()) {
            return;
        }
        //有权限，开始安装应用程序
        install(context, apkFile, getPackageName(context) + ".FileProvider");

    }


    public static String getPackageName(Context context) {
        //当前应用pid
        int pid = android.os.Process.myPid();
        //任务管理类
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //遍历所有应用
        List<ActivityManager.RunningAppProcessInfo> infos = manager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.pid == pid)//得到当前应用
                return info.processName;//返回包名
        }
        return "";
    }

    public static void install(Context context, File file, String fileProvider) {
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(context, fileProvider, file);
        } else {
            apkUri = Uri.fromFile(file);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void startInstallPermissionSettingActivity(Activity context) {
        //注意这个是8.0新API
       /* Uri packageURI = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivityForResult(intent,10086);*/

    }

    public static class GlideImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            /**
             注意：
             1.图片加载器由自己选择，这里不限制，只是提供几种使用方法
             2.返回的图片路径为Object类型，由于不能确定你到底使用的那种图片加载器，
             传输的到的是什么格式，那么这种就使用Object接收和返回，你只需要强转成你传输的类型就行，
             切记不要胡乱强转！
             */
            //Glide 加载图片简单用法
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(context).load(path)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .crossFade().into(imageView);

        }

    }


    public static ObjectAnimator tada(View view, float shakeFactor) {

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofKeyframe(View.SCALE_X,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofKeyframe(View.SCALE_Y,
                Keyframe.ofFloat(0f, 1f),
                Keyframe.ofFloat(.1f, .9f),
                Keyframe.ofFloat(.2f, .9f),
                Keyframe.ofFloat(.3f, 1.1f),
                Keyframe.ofFloat(.4f, 1.1f),
                Keyframe.ofFloat(.5f, 1.1f),
                Keyframe.ofFloat(.6f, 1.1f),
                Keyframe.ofFloat(.7f, 1.1f),
                Keyframe.ofFloat(.8f, 1.1f),
                Keyframe.ofFloat(.9f, 1.1f),
                Keyframe.ofFloat(1f, 1f)
        );

        PropertyValuesHolder pvhRotate = PropertyValuesHolder.ofKeyframe(View.ROTATION,
                Keyframe.ofFloat(0f, 0f),
                Keyframe.ofFloat(.1f, -3f * shakeFactor),
                Keyframe.ofFloat(.2f, -3f * shakeFactor),
                Keyframe.ofFloat(.3f, 3f * shakeFactor),
                Keyframe.ofFloat(.4f, -3f * shakeFactor),
                Keyframe.ofFloat(.5f, 3f * shakeFactor),
                Keyframe.ofFloat(.6f, -3f * shakeFactor),
                Keyframe.ofFloat(.7f, 3f * shakeFactor),
                Keyframe.ofFloat(.8f, -3f * shakeFactor),
                Keyframe.ofFloat(.9f, 3f * shakeFactor),
                Keyframe.ofFloat(1f, 0)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhScaleX, pvhScaleY, pvhRotate).
                setDuration(3000);
    }


}
