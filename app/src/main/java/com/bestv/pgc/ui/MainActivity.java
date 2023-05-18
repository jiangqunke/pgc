package com.bestv.pgc.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bestv.pgc.R;
import com.bestv.pgc.player.ExoVideoView;
import com.bestv.pgc.player.VideoListener;

public class MainActivity extends AppCompatActivity implements VideoListener {
    private ExoVideoView ijkVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ijkVideoView = findViewById(R.id.ijk);
        ijkVideoView.setVideoListener(this);
        ijkVideoView.setLooping(true);
        ijkVideoView.setUrl("https://bp-resource.bestv.com.cn/shortVideos/3feb1894898dbcc3a8a5c1a7e892f733.mp4?auth_key=1683562816-1ce47b94e6aa45c2b5f4ae08c7d3d7c8-0-e7cc1cc3140957c6bf4dc3b5b30c296d");
        ijkVideoView.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
//        ijkVrVideoView.onPause();
        ijkVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        ijkVrVideoView.onResume();
//            ijkVideoView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ijkVrVideoView.onDestroy();
        ijkVideoView.stopPlayback();
    }


    @Override
    public void startPrepare() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onInfo(int what, int extra) {

    }

    @Override
    public void onProgress(int progress, long currentPosition, long duration) {

    }
}