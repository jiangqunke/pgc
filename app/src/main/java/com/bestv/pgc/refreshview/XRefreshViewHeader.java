package com.bestv.pgc.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bestv.pgc.R;
import com.bestv.pgc.refreshview.callback.IHeaderCallBack;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


public class XRefreshViewHeader extends LinearLayout implements IHeaderCallBack {
    private ImageView mImageView;

    public XRefreshViewHeader(Context context) {
        super(context);
        initView(context);
    }

    public XRefreshViewHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        ViewGroup mContent = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.xrefreshview_header, this);
        mImageView = findViewById(R.id.xrefreshview_header_iv);
        mContent.setBackgroundResource(R.color.transparent);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.dp_30), getResources().getDimensionPixelSize(R.dimen.dp_30));
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mImageView.setLayoutParams(layoutParams);
        Glide.with(this)
                .asGif()
                .load(R.mipmap.refresh_adult).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(mImageView);
    }


    /**
     * hide footer when disable pull refresh
     */
    public void hide() {
        setVisibility(View.GONE);
    }

    public void show() {
        setVisibility(View.VISIBLE);
    }

    @Override
    public void onStateNormal() {
    }

    @Override
    public void onStateReady() {
    }

    @Override
    public void onStateRefreshing() {
    }

    @Override
    public void onStateFinish(boolean success) {
    }

    @Override
    public void onHeaderMove(double headerMovePercent, int offsetY, int deltaY) {

    }

    @Override
    public void setRefreshTime(long lastRefreshTime) {

    }

    @Override
    public int getHeaderHeight() {
        return getMeasuredHeight();
    }
}
