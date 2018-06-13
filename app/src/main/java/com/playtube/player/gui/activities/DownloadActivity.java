package com.playtube.player.gui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.playtube.player.businessobjects.db.DownloadedVideosDb;
import com.tube.playtube.R;
import com.playtube.player.gui.fragments.DownloadedVideosFragment;

/**
 * Created by liyanju on 2018/6/12.
 */

public class DownloadActivity extends AppCompatActivity {

    public static void launch(Context context) {
        Intent intent = new Intent(context, DownloadActivity.class);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DownloadedVideosDb.getVideoDownloadsDb().setListener(null);
    }
}
