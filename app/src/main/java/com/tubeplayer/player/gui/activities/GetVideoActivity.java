package com.tubeplayer.player.gui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.ads.NativeAd;
import com.tubeplayer.player.business.FBAdUtils;
import com.tubeplayer.player.business.db.DownloadedVideosDb;
import com.tube.playtube.R;
import com.tubeplayer.player.gui.fragments.DownloadedVideosFragment;

/**
 * Created by liyanju on 2018/6/12.
 */

public class GetVideoActivity extends AppCompatActivity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, GetVideoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DownloadedVideosFragment downloadedVideosFragment = new DownloadedVideosFragment();
        DownloadedVideosDb.getVideoDownloadsDb().setListener(downloadedVideosFragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.download_frame, downloadedVideosFragment)
                .commitAllowingStateLoss();

        FrameLayout adFrameLayout = findViewById(R.id.ad_frame);
        NativeAd nativeAd = FBAdUtils.nextNativieAd();
        if (nativeAd != null && nativeAd.isAdLoaded()) {
            adFrameLayout.setVisibility(View.VISIBLE);
            adFrameLayout.removeAllViews();
            adFrameLayout.addView(FBAdUtils.setUpItemNativeAdView(this, nativeAd));
        } else {
            adFrameLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadedVideosDb.getVideoDownloadsDb().setListener(null);
    }
}
