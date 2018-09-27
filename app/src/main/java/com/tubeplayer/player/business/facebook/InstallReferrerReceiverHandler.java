package com.tubeplayer.player.business.facebook;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.SuperVersions;
import com.tube.playtube.BuildConfig;

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.Utils;

/**
 * Created by liyanju on 2018/6/13.
 */

public class InstallReferrerReceiverHandler {

    public static void onHandleIntent(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (referrer == null) {
            return;
        }

        boolean result = TubeApp.getPreferenceManager().getBoolean("referrer_get", false);
        if (result) {
            return;
        }
        TubeApp.getPreferenceManager().edit().putBoolean("referrer_get", true).apply();

        if (!BuildConfig.DEBUG) {
            if (TubeApp.getPreferenceManager().getBoolean("can_referrer", false)) {
                return;
            }
        }

        if (Utils.isNotCommongUser()) {
            return;
        }

        Log.e("facebookReferrer:::::", referrer);
        FacebookReport.logSentReferrer(referrer);

        FacebookReport.logSentCountry(SuperVersions.SuperVersionHandler.getCountry(context));

        if (SuperVersions.SuperVersionHandler.isReferrerOpen3(referrer)) {
            FacebookReport.logSentBuyUser("form admob");
            SuperVersions.SuperVersionHandler.setSpecial();
            return;
        }

        if (SuperVersions.SuperVersionHandler.isFacebookOpen(referrer)) {
            FacebookReport.logSentBuyUser("form facebook");
            SuperVersions.SuperVersionHandler.setSpecial();
            return;
        }

        if (SuperVersions.SuperVersionHandler.countryIfShow(context)) {
            SuperVersions.SuperVersionHandler.setSpecial();
            return;
        }


    }

}
