package com.tubewebplayer;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;

/**
 * Created by liyanju on 2018/1/3.
 */

public abstract class YouTubeFailureRecoveryActivity extends YouTubeBaseActivity implements
        YouTubePlayer.OnInitializedListener{

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    public static String DEVELOPER_KEY;

    public static void setDeveloperKey(String key) {
        DEVELOPER_KEY = key;
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            try {
                getYouTubePlayerProvider().initialize(DEVELOPER_KEY, this);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract YouTubePlayer.Provider getYouTubePlayerProvider();
}
