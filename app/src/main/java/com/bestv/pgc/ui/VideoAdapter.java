package com.bestv.pgc.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bestv.pgc.R;
import com.bestv.pgc.beans.SpotBean;
import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private Context mContext;
    private List<SpotBean> mDatas;

    public VideoAdapter(Context context, List<SpotBean> datas) {
        mContext = context;
        mDatas = datas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_tiktok_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        SpotBean data = mDatas.get(position);
//        if (!TextUtils.isEmpty(data.getBgCover())) {
//            holder.iv_bg.setVisibility(View.VISIBLE);
//            Glide.with(mContext)
//                    .load(data.getBgCover())
//                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL).error( R.mipmap.zwt_all_adult ))
//                    .into(holder.iv_bg);
//            if (data.getResolutionHeight() < data.getResolutionWidth() ) {
//                setUpViewMargin(holder.iv_bg, mContext.getResources().getDimensionPixelOffset(R.dimen.dp_100));
//            } else {
//                setUpViewMargin(holder.iv_bg, mContext.getResources().getDimensionPixelOffset(R.dimen.dp_0));
//            }
//        } else {
//            holder.iv_bg.setVisibility(View.GONE);
//        }
    }
    private void setUpViewMargin(View view, int height) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
        lp.bottomMargin = height;
        view.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_bg;
        public ViewHolder(View itemView) {
            super(itemView);
            iv_bg= itemView.findViewById(R.id.iv_bg);
        }
    }

}
