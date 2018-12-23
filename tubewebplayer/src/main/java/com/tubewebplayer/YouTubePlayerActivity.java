package com.tubewebplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * Created by liyanju on 2018/1/8.
 */

public class YouTubePlayerActivity extends YouTubeFailureRecoveryActivity{

    private YouTubePlayerView videoPlayerView;

    private YouTubePlayer youTubePlayer;

    public static final String VID_KEY = "vid";

    private String currentVid;

    public static void launch(Context context, String vid) {
        if (TextUtils.isEmpty(DEVELOPER_KEY)) {
            Log.e("player", "DEVELOPER_KEY is null !!!!");
            return;
        }

        Intent intent = new Intent(context, YouTubePlayerActivity.class);
        intent.putExtra(VID_KEY, vid);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.native_youtube_player);

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }

        videoPlayerView = (YouTubePlayerView) findViewById(R.id.video_player);

        if (savedInstanceState == null) {
            currentVid = getIntent().getStringExtra(VID_KEY);
        } else {
            currentVid = savedInstanceState.getString(VID_KEY);
        }

        init();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(VID_KEY, currentVid);
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return videoPlayerView;
    }

    private boolean isFullScreen;

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.youTubePlayer = youTubePlayer;
        youTubePlayer.loadVideo(currentVid);
        youTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
            @Override
            public void onFullscreen(boolean b) {
                isFullScreen = b;
            }
        });
    }

    private void init() {
        initializeYoutubePlayer();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (isFullScreen && youTubePlayer != null) {
            youTubePlayer.setFullscreen(false);
            return;
        }
        super.onBackPressed();
    }

    private void initializeYoutubePlayer() {
        try {
            videoPlayerView.initialize(DEVELOPER_KEY, this);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
