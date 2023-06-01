package com.bestv.pgc.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.bestv.pgc.util.WindowUtil;


public class MySlideRelativeLayout extends RelativeLayout   {
    private GestureDetector mGestureDetector ;
    private boolean mHasScroll ;
    public MySlideRelativeLayout(Context context) {
        this(context, null);
    }

    public MySlideRelativeLayout(Context context, AttributeSet attrs,
                                 int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MySlideRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void initView() {
        mGestureDetector =  new GestureDetector(getContext(), new MyGestureListener());
    }

    private boolean mScrolling;
    private float touchDownX;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mGestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        Log.e("touch1", "touch3=" + event);

        if (event != null && event.getAction() == MotionEvent.ACTION_DOWN){
            mHasScroll = false;
        }else if (event != null && mHasScroll && event.getAction() == MotionEvent.ACTION_UP) {
            //如果产生了滑动，则不传递事件到子view了
            return true;
        }
        return super.onInterceptTouchEvent(event);
    }


    private setOnSlideListener mSetOnSlideListener;

    public setOnSlideListener getmSetOnSlideListener() {
        return mSetOnSlideListener;
    }

    public void setmSetOnSlideListener(setOnSlideListener mSetOnSlideListener) {
        this.mSetOnSlideListener = mSetOnSlideListener;
    }

    public interface setOnSlideListener {
        void onSingleClick();
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent e) {
            if (WindowUtil.isSmallEdge(getContext(), e)) return super.onDown(e);
            return true;
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mSetOnSlideListener != null && !mHasScroll){
                mSetOnSlideListener.onSingleClick();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) < 10) {
                return true;
            }else {
                mHasScroll = true;
            }
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

    }
}
