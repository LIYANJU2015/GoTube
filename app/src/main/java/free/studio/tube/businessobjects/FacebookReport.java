package free.studio.tube.businessobjects;

import android.os.Build;
import android.os.Bundle;

import com.facebook.appevents.AppEventsLogger;

import free.studio.tube.app.GoTubeApp;

/**
 * Created by liyanju on 2018/3/22.
 */

public class FacebookReport {

    public static void logSentMainUserInfo() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("phone", android.os.Build.MODEL);
        bundle.putString("simCard1", SuperVersions.SuperVersionHandler.getCountry2(GoTubeApp.getContext()));
        bundle.putString("simCard2", SuperVersions.SuperVersionHandler.getCountry(GoTubeApp.getContext()));
        bundle.putString("version", String.valueOf(Build.VERSION.SDK_INT));
        logger.logEvent("MainPage_UserInfo",bundle);
    }

    public static void logSentDownloadPlay() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("SentDownloadPlay");
    }

    public static void logSentDownloadStart(String title) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("download", "title " + title);
        logger.logEvent("StartDownload",bundle);
    }

    public static void logSentChannelBrowser() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("ChannelBrowser Enter Into");
    }

    public static void logSentDownloadEnd(String title) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("download", "title " + title);
        logger.logEvent("EndDownload",bundle);
    }

    public static void logSentVideoPlayStart() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("SentVideoPlay Start");
    }

    public static void logSentVideoPlay() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("SentVideoPlay Enter Into");
    }

    public static void logSentReferrer2(String from) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("special", "success " + from);
        logger.logEvent("ReferrerReceiver3",bundle);
    }

    public static void logSendAppRating(String star) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("start", star);
        logger.logEvent("AppRating",bundle);
    }

    public static void logSentBuyUser(String from) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("from", from);
        logger.logEvent("SentBuyUser",bundle);
    }

    public static void logSentReferrer4(String linkData) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("linkData", linkData);
        logger.logEvent("ReferrerReceiver4",bundle);
    }

    public static void logSentReferrer(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("cus_referrer", referrer);
        logger.logEvent("ReferrerReceiver",bundle);
    }

    public static void logSentCountry(String country)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("country", country);
        bundle.putString("phone", android.os.Build.MODEL);
        logger.logEvent("ReferrerReceiverCountry",bundle);
    }
}
