/*
 * GoTube
 * Copyright (C) 2015  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tubeplayer.player.app;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;


import com.liulishuo.filedownloader.FileDownloader;
import com.rating.RatingActivity;
import com.tubeplayer.player.business.FBAdUtils;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.gui.activities.SplashActivity;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Arrays;
import java.util.List;

import com.tube.playtube.R;
import com.tubeplayer.player.business.FeedUpdaterReceiver;

/**
 * GoTube application.
 */
public class TubeApp extends MultiDexApplication {

	/** GoTube Application databaseInstance. */
	private static TubeApp GoTubeApp = null;

	public static final String KEY_SUBSCRIPTIONS_LAST_UPDATED = "PlayTube.KEY_SUBSCRIPTIONS_LAST_UPDATED";
	public static final String NEW_VIDEOS_NOTIFICATION_CHANNEL = "com.play.tube.NEW_VIDEOS_NOTIFICATION_CHANNEL";
	public static final int NEW_VIDEOS_NOTIFICATION_CHANNEL_ID = 1;

	public static boolean isCoolStart = false;


	private String getCurrentProcessName() {
		int pid = android.os.Process.myPid();
		ActivityManager am = (ActivityManager)
				getSystemService(Context.ACTIVITY_SERVICE);
		final List<ActivityManager.RunningAppProcessInfo> appProcessInfos = am.getRunningAppProcesses();

		if (appProcessInfos != null) {
			for (ActivityManager.RunningAppProcessInfo appProcess : appProcessInfos) {
				if (appProcess.pid == pid) {
					return appProcess.processName;
				}
			}
		}
		return "";
	}

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		GoTubeApp = this;

		FileDownloader.setup(this);

		final String packageName = getPackageName();
		if (!TextUtils.isEmpty(packageName) && !packageName.equals(getCurrentProcessName())) {
			return;
		}

		SuperVersions.initSpecial();

		CrashReport.initCrashReport(getApplicationContext());
		initChannels(this);

		isCoolStart = true;

		if (!getPreferenceManager().getBoolean("shortcut", false)) {
			getPreferenceManager().edit().putBoolean("shortcut", true).apply();
			addShortcut(this, SplashActivity.class, getString(R.string.app_name), R.mipmap.ic_launcher);
		}

		FBAdUtils.init(this);
		FBAdUtils.loadFBAds(Utils.NATIVE_AD_ID);

		RatingActivity.setRatingClickListener(new RatingActivity.RatingClickListener() {
			@Override
			public void onClickFiveStart() {
				FacebookReport.logSendAppRating("five_star");
			}

			@Override
			public void onClickReject() {
				FacebookReport.logSendAppRating("no_star");
			}
		});
		RatingActivity.setPopTotalCount(this, 2);
	}

	public static void addShortcut(Context context, Class clazz, String appName, int ic_launcher) {
		// 安装的Intent
		Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

		Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
		shortcutIntent.putExtra("tName", appName);
		shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
		shortcutIntent.setClassName(context, clazz.getName());
		//        shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		// 快捷名称
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getResources().getString(R.string.app_name));
		// 快捷图标是否允许重复
		shortcut.putExtra("duplicate", false);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		// 快捷图标
		Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, ic_launcher);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
		// 发送广播
		context.sendBroadcast(shortcut);
	}


	/**
	 * Returns a localised string.
	 *
	 * @param  stringResId	String resource ID (e.g. R.string.my_string)
	 * @return Localised string, from the strings XML file.
	 */
	public static String getStr(int stringResId) {
		return GoTubeApp.getString(stringResId);
	}


	/**
	 * Given a string array resource ID, it returns an array of strings.
	 *
	 * @param stringArrayResId String array resource ID (e.g. R.string.my_array_string)
	 * @return Array of String.
	 */
	public static String[] getStringArray(int stringArrayResId) {
		return GoTubeApp.getResources().getStringArray(stringArrayResId);
	}


	/**
	 * Given a string array resource ID, it returns an list of strings.
	 *
	 * @param stringArrayResId String array resource ID (e.g. R.string.my_array_string)
	 * @return List of String.
	 */
	public static List<String> getStringArrayAsList(int stringArrayResId) {
		return Arrays.asList(getStringArray(stringArrayResId));
	}

	public static void setSpecial() {
		SuperVersions.setSpecial();
	}

	/**
	 * 默认false
	 * @return
	 */
	public static boolean isSpecial() {
		return SuperVersions.isSpecial();
	}

	/**
	 * Returns the App's {@link SharedPreferences}.
	 *
	 * @return {@link SharedPreferences}
	 */
	public static SharedPreferences getPreferenceManager() {
		return PreferenceManager.getDefaultSharedPreferences(GoTubeApp);
	}


	/**
	 * Returns the dimension value that is specified in R.dimens.*.  This value is NOT converted into
	 * pixels, but rather it is kept as it was originally written (e.g. dp).
	 *
	 * @return The dimension value.
	 */
	public static float getDimension(int dimensionId) {
		return GoTubeApp.getResources().getDimension(dimensionId);
	}


	/**
	 * @return {@link Context}.
	 */
	public static Context getContext() {
		return GoTubeApp.getBaseContext();
	}


	/**
	 * Restart the app.
	 */
	public static void restartApp() {
		Context context = getContext();
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
		ComponentName componentName = intent.getComponent();
		Intent mainIntent = Intent.makeRestartActivityTask(componentName);
		context.startActivity(mainIntent);
		System.exit(0);
	}


	/**
	 * @return  True if the device is a tablet; false otherwise.
	 */
	public static boolean isTablet() {
		return getContext().getResources().getBoolean(R.bool.is_tablet);
	}

	/**
	 * @return boolean determining if the device is connected via WiFi
	 */
	public static boolean isConnectedToWiFi() {
		final ConnectivityManager connMgr = (ConnectivityManager)
						getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi != null && wifi.isConnectedOrConnecting();
	}

	/**
	 * @return boolean determining if the device is connected via Mobile
	 */
	public static boolean isConnectedToMobile() {
		final ConnectivityManager connMgr = (ConnectivityManager)
						getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return mobile != null && mobile.isConnectedOrConnecting();
	}

	/*
	 * Initialize Notification Channels (for Android OREO)
	 * @param context
	 */
	@TargetApi(26)
	private void initChannels(Context context) {

		if(Build.VERSION.SDK_INT < 26) {
			return;
		}
		NotificationManager notificationManager =
						(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		String channelId = NEW_VIDEOS_NOTIFICATION_CHANNEL;
		CharSequence channelName = context.getString(R.string.notification_channel_feed_title);
		int importance = NotificationManager.IMPORTANCE_LOW;
		NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
		notificationChannel.enableLights(true);
		notificationChannel.setLightColor(Color.RED);
		notificationChannel.enableVibration(false);
		notificationManager.createNotificationChannel(notificationChannel);
	}

	/**
	 * Get the stored interval (in milliseconds) to pass to the below method.
	 */
	public static void setFeedUpdateInterval() {
		int feedUpdaterInterval = Integer.parseInt(GoTubeApp.getPreferenceManager().getString(GoTubeApp.getStr(R.string.pref_key_feed_notification), "0"));
		setFeedUpdateInterval(feedUpdaterInterval);
	}

	/**
	 * Setup the Feed Updater Service. First, cancel the Alarm that will trigger the next fetch (if there is one), then set the
	 * Alarm with the passed interval, if it's greater than 0. 
	 * @param interval The number of milliseconds between each time new videos for subscribed channels should be fetched.
	 */
	public static void setFeedUpdateInterval(int interval) {
		Intent alarm = new Intent(getContext(), FeedUpdaterReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarm, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

		// Feed Auto Updater has been cancelled. If the selected interval is greater than 0, set the new alarm to call FeedUpdaterService
		if(interval > 0) {
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+interval, interval, pendingIntent);
		}
	}

}
