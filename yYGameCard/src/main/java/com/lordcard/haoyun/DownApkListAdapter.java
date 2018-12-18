package com.lordcard.haoyun;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.ylly.playcard.R;
import com.zhy.autolayout.utils.AutoUtils;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class DownApkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LayoutInflater inflater;
    ArrayList<Map<String, String>> list;
    public static final int TYPE_ALL = 0;
    private NumberFormat numberFormat;
    private Activity mContext;
    private File mApkDownFile;
    private ArrayList<String> list_path=new ArrayList<>();
    private OnReloadListener mOnReloadListener;
    public DownApkListAdapter(Activity context, List<Map<String, String>> dataList) {
        inflater = LayoutInflater.from(context);
        mContext=context;
        numberFormat = NumberFormat.getPercentInstance();
        numberFormat.setMinimumFractionDigits(2);
        list= (ArrayList<Map<String, String>>) dataList;
        for (int i = 0; i < list.size(); i++) {
            list_path.add("");
        }
    }

    public void updateData(int type) {
        if (type == TYPE_ALL);
        if (mOnReloadListener != null) {
            mOnReloadListener.OnReload();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        view = inflater.inflate(R.layout.item_update_list, parent, false);
        return new fGoldViewHolder(view);
    }

    private String createTag(DownloadTask task) {
        return TYPE_ALL + "_" + task.progress.tag;
    }



    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        fGoldViewHolder viewHolder = (fGoldViewHolder) holder;
        String img_url = list.get(position).get("img_url");
        if (!img_url.startsWith("http")) {
            viewHolder.img_background.setImageResource(AppUtils.getImageResourceId(img_url));
        } else {
            AppUtils.disImgRound(mContext, list.get(position).get("img_url"), viewHolder.img_background);

        }
        viewHolder.tv_name.setText(list.get(position).get("name"));
        viewHolder.tv_name2.setText(list.get(position).get("des"));
        String downTaskId = list.get(position).get("id");


        if (position == list.size() - 1) {
            viewHolder.img_bottom.setVisibility(View.GONE);
        } else {
            viewHolder.img_bottom.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(list.get(position).get("packname")) && AppUtils.isAppExist(mContext, list.get(position).get("packname"))) {
            viewHolder.relay_pro.setVisibility(View.GONE);
            viewHolder.tv_down.setVisibility(View.VISIBLE);
            viewHolder.tv_down.setText("打开");
            viewHolder.relay_01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    PackageManager packageManager = mContext.getPackageManager();
                    intent = packageManager.getLaunchIntentForPackage(list.get(position).get("packname"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mContext.startActivity(intent);
                }
            });
        } else {
            if (OkDownload.getInstance().getTask(downTaskId) != null) {

                DownloadTask task = OkDownload.getInstance().getTask(downTaskId);
                final Progress progress = task.progress;

                list_path.set(position, progress.filePath);
                String tag = createTag(task);
                task.register(new ListDownloadListener(tag, viewHolder));
                viewHolder.setTag(tag);
                viewHolder.setTask(task);
                viewHolder.refresh(task.progress);
                viewHolder.relay_pro.setVisibility(View.VISIBLE);
                viewHolder.tv_down.setVisibility(View.GONE);
            } else {
                viewHolder.relay_pro.setVisibility(View.GONE);
                viewHolder.tv_down.setVisibility(View.VISIBLE);
            }

            viewHolder.relay_01.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String taskId = list.get(position).get("id");
                    if (OkDownload.getInstance().getTask(taskId) == null) {
                        String fileName = list.get(position).get("packname") + ".apk";
                        ApkDownModel apk1 = new ApkDownModel();
                        apk1.name = list.get(position).get("name");
                        apk1.url = list.get(position).get("apk_url");
                        apk1.iconUrl = list.get(position).get("img_url");
                        GetRequest<File> request = OkGo.<File>get(apk1.url);
                        OkDownload.request(taskId, request)//
                                .priority(apk1.priority)//
                                .extra1(apk1)//
                                .save().fileName(fileName)//
                                .start();

                        if (mOnReloadListener != null) {
                            mOnReloadListener.OnReload();
                        }
                    } else {
                        DownloadTask task = OkDownload.getInstance().getTask(taskId);
                        Progress progress = task.progress;
                        switch (progress.status) {
                            case Progress.LOADING:
                                task.pause();
                                break;
                            case Progress.PAUSE:
                            case Progress.NONE:
                            case Progress.ERROR:
                                task.start();
                                break;
                            case Progress.FINISH:
                                mApkDownFile = new File(list_path.get(position));
                                AppUtils.installApk(mContext,mApkDownFile);
                                break;

                        }
                    }

                }
            });
        }


    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    private class ListDownloadListener extends DownloadListener {

        private fGoldViewHolder holder;

        ListDownloadListener(Object tag, fGoldViewHolder holder) {
            super(tag);
            this.holder = holder;
        }

        @Override
        public void onStart(Progress progress) {
        }

        @Override
        public void onProgress(Progress progress) {
            if (tag == holder.getTag()) {
                holder.refresh(progress);
            }
        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
            updateData(TYPE_ALL);
            AppUtils.installApk(mContext,file);

        }

        @Override
        public void onRemove(Progress progress) {
        }
    }


    class fGoldViewHolder extends RecyclerView.ViewHolder {
        TextView tv_down;
        TextView tv_01;
        TextView tv_name;
        TextView tv_name2;
        ImageView img_background;
        public ProgressBar pbProgress;
        public RelativeLayout relay_pro;
        public RelativeLayout relay_01;
        public ImageView img_bottom;

        private DownloadTask task;
        private String tag;

        public fGoldViewHolder(View itemView) {
            super(itemView);
            tv_down = itemView.findViewById(R.id.tv_down);
            tv_01 = itemView.findViewById(R.id.tv_01);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_name2 = itemView.findViewById(R.id.tv_name2);
            img_background = itemView.findViewById(R.id.img_background);

            pbProgress = itemView.findViewById(R.id.pbProgress);
            relay_01 = itemView.findViewById(R.id.relay_01);
            relay_pro = itemView.findViewById(R.id.relay_pro);



            img_bottom = itemView.findViewById(R.id.img_bottom);
            AutoUtils.autoSize(itemView);
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        public void setTask(DownloadTask task) {
            this.task = task;
        }


        public void refresh(Progress progress) {
            switch (progress.status) {
                case Progress.NONE:
                    break;
                case Progress.PAUSE:
                    tv_01.setText("继续");
                    break;
                case Progress.ERROR:
                    break;
                case Progress.WAITING:
                    tv_01.setText("等待");
                    break;
                case Progress.FINISH:
                    tv_01.setText("安装");
                    break;
                case Progress.LOADING:
                    tv_01.setText("暂停");
                    break;
            }
            pbProgress.setMax(100);
            pbProgress.setProgress((int) (progress.fraction * 100));
        }
    }

    public void setOnReloadListener (OnReloadListener listener) {
        mOnReloadListener = listener;
    }

    public interface OnReloadListener {
        void OnReload();
    }
}
