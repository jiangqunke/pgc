package com.bestv.pgc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.bestv.pgc.media.player.IjkVideoView;

public class MainActivity extends AppCompatActivity {
    private IjkVideoView ijkVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ijkVideoView = findViewById(R.id.ijk);
        ijkVideoView.setUrl("https://bp-resource.bestv.com.cn/shortVideos/3feb1894898dbcc3a8a5c1a7e892f733.mp4?auth_key=1683562816-1ce47b94e6aa45c2b5f4ae08c7d3d7c8-0-e7cc1cc3140957c6bf4dc3b5b30c296d");
        ijkVideoView.start();
    }
}