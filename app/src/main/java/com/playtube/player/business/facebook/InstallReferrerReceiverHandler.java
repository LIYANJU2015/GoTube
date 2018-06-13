package com.playtube.player.business.facebook;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.playtube.player.business.FacebookReport;
import com.playtube.player.business.SuperVersions;
import com.tube.playtube.BuildConfig;

import com.playtube.player.app.PlayTubeApp;

/**
 * Created by liyanju on 2018/6/13.
 */

public class InstallReferrerReceiverHandler {

    public static void onHandleIntent(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (referrer == null) {
            return;
        }

        boolean result = PlayTubeApp.getPreferenceManager().getBoolean("referrer_process", false);
        if (result) {
            return;
        }
        PlayTubeApp.getPreferenceManager().edit().putBoolean("referrer_process", true).apply();

        if (!BuildConfig.DEBUG) {
            if (PlayTubeApp.getPreferenceManager().getBoolean("can_referrer", false)) {
                return;
            }
        }

        Log.e("facebookReferrer:::::", referrer);
        FacebookReport.logSentReferrer(referrer);

        FacebookReport.logSentCountry(SuperVersions.SuperVersionHandler.getCountry(context));

        if (SuperVersions.SuperVersionHandler.isReferrerOpen3(referrer)) {
            FacebookReport.logSentBuyUser("form admob");
            SuperVersions.SuperVersionHandler.setSpecial();
            return;
        }

        if (SuperVersions.SuperVersionHandler.countryIfShow(context)) {
            SuperVersions.SuperVersionHandler.setSpecial();
            return;
        }


    }

}
