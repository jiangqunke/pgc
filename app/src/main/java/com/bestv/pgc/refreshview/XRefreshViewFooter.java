package com.bestv.pgc.refreshview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bestv.pgc.R;
import com.bestv.pgc.refreshview.callback.IFooterCallBack;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;


public class XRefreshViewFooter extends LinearLayout implements IFooterCallBack {
    private ImageView mImageView;
    private boolean showing = true;
    public XRefreshViewFooter(Context context) {
        super(context);
        initView(context);
    }

    public XRefreshViewFooter(Context context, AttributeSet attrs) {
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




    @Override
    public void callWhenNotAutoLoadMore(XRefreshView xRefreshView) {

    }

    @Override
    public void onStateReady() {
    }

    @Override
    public void onStateRefreshing() {
        show(true);
    }

    @Override
    public void onReleaseToLoadMore() {

    }

    @Override
    public void onStateFinish(boolean success) {
    }

    @Override
    public void onStateComplete() {

    }

    @Override
    public void show(boolean show) {
        if (show == showing) {
            return;
        }
        showing = show;
    }

    @Override
    public boolean isShowing() {
        return showing;
    }

    @Override
    public int getFooterHeight() {
        return getMeasuredHeight();
    }
}
