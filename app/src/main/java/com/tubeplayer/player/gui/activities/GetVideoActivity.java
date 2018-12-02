package com.tubeplayer.player.gui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.adlibs.InMobiHelper;
import com.mintergalsdk.LeadboltSDK;
import com.mintergalsdk.MintergalSDK;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.business.db.DownloadedVideosDb;
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

        InMobiHelper.init(TubeApp.getContext(), Utils.ACCOUNT_ID);
        InMobiHelper.createInterstitial(Utils.CHAPING_INMOBI);

        RelativeLayout adRelative = findViewById(R.id.in_ad_relative);
        if (SuperVersions.isShowAd()) {
            adRelative.setVisibility(View.VISIBLE);
            adRelative.removeAllViews();
            InMobiHelper.addBanner(this, adRelative, Utils.BANNER_INMOBI2);
        } else {
            adRelative.setVisibility(View.GONE);
        }

        LeadboltSDK.initModule(getApplication(), TubeApp.LE_AD_ID);
        LeadboltSDK.loadModuleToCache(getApplication(), TubeApp.LE_AD_ID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadedVideosDb.getVideoDownloadsDb().setListener(null);

        if (!InMobiHelper.showInterstitial()) {
            MintergalSDK.showInterstitialAd(TubeApp.CHA_PING_AD_ID, new Runnable() {
                @Override
                public void run() {
                    LeadboltSDK.showModule(getApplication(), TubeApp.LE_AD_ID);
                }
            });
        }
    }
}
