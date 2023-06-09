package com.bestv.pgc.util;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

/**
 * 计时器
 * <p>
 * xesamguo@gmail.com
 */
public class PlayerCountTimer {
    public static final int TIMER_NOT_START = 0;
    public static final int TIMER_RUNNING = 1;
    public static final int TIMER_PAUSED = 2;
    private long mMillisInterval;
    private long mMillisStart = -1;
    private long mMillisPause;
    private long mMillisLastTickStart;
    private long mTotalPausedFly;

    /**
     * representing the timer state
     */
    private volatile int mState = TIMER_NOT_START;

    public PlayerCountTimer(long interval) {
        mMillisInterval = interval;
    }

    protected synchronized void setInterval(long interval) {
        mMillisInterval = interval;
    }

    /**
     * Start the timer.
     */
    public synchronized void start() {
        if (mState == TIMER_RUNNING) {
            return;
        }
        mTotalPausedFly = 0;
        mMillisStart = SystemClock.elapsedRealtime();
        mState = TIMER_RUNNING;
        onStart(0);
        mHandler.sendEmptyMessageDelayed(MSG, mMillisInterval);
    }

    /**
     * Pause the timer.
     * if the timer has been canceled or is running --> skip
     */
    public synchronized void pause() {
        if (mState != TIMER_RUNNING) {
            return;
        }
        mHandler.removeMessages(MSG);
        mState = TIMER_PAUSED;

        mMillisPause = SystemClock.elapsedRealtime();
        onPause(mMillisPause - mMillisStart - mTotalPausedFly);
    }

    /**
     * Resume the timer.
     */
    public synchronized void resume() {
        if (mState != TIMER_PAUSED) {
            return;
        }
        mState = TIMER_RUNNING;

        onResume(mMillisPause - mMillisStart - mTotalPausedFly);

        long delay = mMillisInterval - (mMillisPause - mMillisLastTickStart);
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause;
        mHandler.sendEmptyMessageDelayed(MSG, delay);
    }

    /**
     * Cancel the timer.
     */
    public synchronized void cancel() {
        if (mState == TIMER_NOT_START) {
            return;
        }
        final int preState = mState;
        mHandler.removeMessages(MSG);
        mState = TIMER_NOT_START;

        if (preState == TIMER_RUNNING) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly);
        } else if (preState == TIMER_PAUSED) { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly);
        }
    }

    public int getState() {
        return mState;
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onStart(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onCancel(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onPause(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onResume(long millisFly) {
    }

    /**
     * @param millisFly The amount of time fly,not include paused time.
     */
    protected void onTick(long millisFly) {
    }

    // handles counting
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (PlayerCountTimer.this) {
                if (mState != TIMER_RUNNING) {
                    return;
                }

                mMillisLastTickStart = SystemClock.elapsedRealtime();
                onTick(mMillisLastTickStart - mMillisStart - mTotalPausedFly);
                if (mState != TIMER_RUNNING) {
                    return;
                }

                // take into account user's onTick taking time to execute
                long delay = mMillisLastTickStart + mMillisInterval - SystemClock.elapsedRealtime();

                // special case: user's onTick took more than interval to
                // complete, skip to next interval
                while (delay < 0) {
                    delay += mMillisInterval;
                }

                sendMessageDelayed(obtainMessage(MSG), delay);
            }
        }
    };

    private static final int MSG = 1;

}
