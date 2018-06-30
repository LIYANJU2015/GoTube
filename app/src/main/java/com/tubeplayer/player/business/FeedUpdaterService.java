package com.tubeplayer.player.business;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.Tasks.GetSubscriptionVideosTaskListener;
import com.tube.playtube.R;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.business.youtube.Tasks.GetSubscriptionVideosTask;
import com.tubeplayer.player.gui.activities.MainActivity;

/**
 * A Service to automatically refresh the Subscriptions Database for Subscribed Channels. If any new videos have been found,
 * the Service will create a notification.
 */
public class FeedUpdaterService extends Service implements GetSubscriptionVideosTaskListener {
	private GetSubscriptionVideosTask getSubscriptionVideosTask;
	private List<YTubeVideo> newVideosFetched;

	public static final String NEW_SUBSCRIPTION_VIDEOS_FOUND = "FeedUpdaterService.TubeNEW_SUBSCRIPTION_VIDEOS_FOUND";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Need to instantiate the task here since you can only run a task once.
		int feedUpdaterInterval = Integer.parseInt(TubeApp.getPreferenceManager().getString(TubeApp.getStr(R.string.pref_key_feed_notification), "0"));
		if(feedUpdaterInterval > 0) {
			newVideosFetched = new ArrayList<>();
			getSubscriptionVideosTask = new GetSubscriptionVideosTask(this);
			getSubscriptionVideosTask.executeInParallel();
		}
		return START_STICKY;

	}

	@Override
	public void onChannelVideosFetched(YTubeChannel channel, List<YTubeVideo> videosFetched, boolean videosDeleted) {
		if(videosFetched.size() > 0)
			newVideosFetched.addAll(videosFetched);
	}

	@Override
	public void onAllChannelVideosFetched() {
		if(newVideosFetched.size() > 0) {
			Intent clickIntent = new Intent(this, MainActivity.class);
			clickIntent.setAction(MainActivity.ACTION_VIEW_FEED);

			PendingIntent clickPendingIntent = PendingIntent.getActivity(this, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			Notification notification = new NotificationCompat.Builder(this, TubeApp.NEW_VIDEOS_NOTIFICATION_CHANNEL)
							.setSmallIcon(R.drawable.ic_notification_icon)
							.setContentTitle(getString(R.string.app_name))
							.setContentText(String.format(getString(R.string.notification_new_videos_found), newVideosFetched.size()))
							.setContentIntent(clickPendingIntent)
							.setAutoCancel(true)
							.build();

			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(TubeApp.NEW_VIDEOS_NOTIFICATION_CHANNEL_ID, notification);

			// Send a broadcast that new subscription videos have been found. The feed tab will receive the broadcast and
			// refresh its video grid to show the new videos.
			Intent feedTabIntent = new Intent(NEW_SUBSCRIPTION_VIDEOS_FOUND);
			sendBroadcast(feedTabIntent);
		}
	}
}