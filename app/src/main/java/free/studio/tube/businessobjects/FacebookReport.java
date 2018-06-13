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
        bundle.putString("tel", android.os.Build.MODEL);
        bundle.putString("simCard1", SuperVersions.SuperVersionHandler.getCountry2(GoTubeApp.getContext()));
        bundle.putString("simCard2", SuperVersions.SuperVersionHandler.getCountry(GoTubeApp.getContext()));
        bundle.putString("version", String.valueOf(Build.VERSION.SDK_INT));
        logger.logEvent("logMainPage_UserInfo",bundle);
    }

    public static void logSentDownloadPlay() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("logSentDownloadPlay");
    }

    public static void logSentDownloadStart(String title) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("download_video", "title " + title);
        logger.logEvent("logStartDownload",bundle);
    }

    public static void logSentChannelBrowser() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("logChannelBrowser Enter Into");
    }

    public static void logSentDownloadEnd(String title) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("download_video", "title " + title);
        logger.logEvent("logEndDownload",bundle);
    }

    public static void logSentVideoPlayStart() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("logSentVideoPlay_Start");
    }

    public static void logSentVideoPlay() {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        logger.logEvent("logSentVideoPlay_Page");
    }

    public static void logSentReferrer2(String from) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("faster", "success " + from);
        logger.logEvent("country_Open",bundle);
    }

    public static void logSendAppRating(String star) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("star", star);
        logger.logEvent("logRating",bundle);
    }

    public static void logSentBuyUser(String from) {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("from", from);
        logger.logEvent("logSentBuyNewUser",bundle);
    }

    public static void logSentReferrer(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("referrer", referrer);
        logger.logEvent("logReferrer",bundle);
    }

    public static void logSentCountry(String country)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(GoTubeApp.getContext());
        Bundle bundle = new Bundle();
        bundle.putString("country", country);
        bundle.putString("phone", android.os.Build.MODEL);
        logger.logEvent("logSentCountry",bundle);
    }
}
