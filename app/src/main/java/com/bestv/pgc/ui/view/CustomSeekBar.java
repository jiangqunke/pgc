package com.bestv.pgc.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatSeekBar;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;


public class CustomSeekBar extends AppCompatSeekBar {
    private Rect rect_seek;
    private Paint trailerPaint;
    private float headTime;
    private int length;
    private PositionListening listening;

    public SeekListening getSeekListening() {
        return seekListening;
    }

    public void setSeekListening(SeekListening seekListening) {
        this.seekListening = seekListening;
    }

    private SeekListening seekListening;

    public CustomSeekBar(Context context) {
        super(context);
        init();
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        trailerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trailerPaint.setStyle(Paint.Style.FILL);
        trailerPaint.setColor(Color.WHITE);
        trailerPaint.setAntiAlias(true);
    }

    public void setHasScorll(boolean hasScorll) {
        this.hasScorll = hasScorll;
        invalidate();
    }

    private boolean hasScorll = false;  //true  禁止滑动  false 可以滑动

    public void startTrackVideoHeaderAndTrailer(float headTime, double playDuration, int length, PositionListening listening) {
        this.listening = listening;
        this.headTime = headTime;
        this.length = length;
        if (playDuration >= headTime) {
            trailerPaint.setColor(Color.RED);

        } else {
            trailerPaint.setColor(Color.WHITE);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        rect_seek = getProgressDrawable().getBounds();
        drawHeaderAndTailRect(canvas);
        super.onDraw(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (hasScorll) return true;
        return super.onTouchEvent(event);
    }

    private void drawHeaderAndTailRect(Canvas canvas) {
        if (headTime > 0) {
            float headerLeft = rect_seek.left + SizeUtils.dp2px(8) + rect_seek.width() * (headTime / (float) length);
            if (listening != null) {
                listening.getPosition((int) headerLeft - (int) (rect_seek.height() * 1.2));
            }
            canvas.drawCircle(headerLeft - (int) (rect_seek.height() * 1.2), rect_seek.centerY(), (int) (rect_seek.height() * 1.2), trailerPaint);
        } else {
            canvas.drawCircle(0, 0, 0, trailerPaint);
        }
        if (seekListening != null) {
            seekListening.getSeekPosition((int) (rect_seek.left + rect_seek.width() * (getProgress() / 100.0f)) - SizeUtils.dp2px(50));
        }
    }

    public interface PositionListening {
        public void getPosition(int position);
    }

    public interface SeekListening {
        public void getSeekPosition(int position);
    }
}
