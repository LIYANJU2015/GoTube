package com.playtube.player.business;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAd;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.rating.RatingActivity;

import java.io.File;

import com.playtube.player.business.db.DownloadedVideosDb;
import com.tube.playtube.R;
import com.playtube.player.app.PlayTubeApp;
import com.playtube.player.business.youtube.bean.YouTubeVideo;
import com.playtube.player.gui.activities.DownloadActivity;
import com.playtube.player.gui.activities.MainActivity;

/**
 * Created by liyanju on 2018/6/12.
 */

public class FileDownloaderHelper {

    public static File defaultfile = new File(Environment.getExternalStorageDirectory(),
            PlayTubeApp.getContext().getString(R.string.app_name));

    public static void addDownloadTask(YouTubeVideo youTubeVideo, String downloadurl) {
        if (youTubeVideo == null) {
            return;
        }

        if (!defaultfile.exists()) {
            defaultfile.mkdirs();
        }

        if (!defaultfile.exists() || !defaultfile.canWrite() || !defaultfile.canRead()) {
            Toast.makeText(PlayTubeApp.getContext(),
                    R.string.external_storage_not_available,
                    Toast.LENGTH_LONG).show();
            return;
        }

        String path = defaultfile + File.separator + String.valueOf(youTubeVideo.getTitle()) + ".mp4";

        com.liulishuo.filedownloader.FileDownloader.getImpl().create(downloadurl)
                .setPath(path)
                .setAutoRetryTimes(1)
                .setTag(youTubeVideo)
                .setListener(new SelfNotificationListener(new FileDownloadNotificationHelper()))
                .start();

        Toast.makeText(PlayTubeApp.getContext(),
                String.format(PlayTubeApp.getContext().getString(R.string.starting_video_download), youTubeVideo.getTitle()),
                Toast.LENGTH_LONG).show();

        FBAdUtils.interstitialLoad(Utils.CHAPING_HIGH_AD, new FBAdUtils.FBInterstitialAdListener(){
            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
            }

            @Override
            public void onLoaded(InterstitialAd interstitialAd) {
                super.onLoaded(interstitialAd);
                try {
                    if (interstitialAd != null && interstitialAd.isAdLoaded()) {
                        interstitialAd.show();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                super.onError(ad, adError);
                FBAdUtils.destoryInterstitial();
            }
        });

    }


    public static class SelfNotificationListener extends FileDownloadNotificationListener {

        private NotificationManager manager;

        public SelfNotificationListener(FileDownloadNotificationHelper helper) {
            super(helper);
            manager = (NotificationManager) FileDownloadHelper.getAppContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(), ((YouTubeVideo) task.getTag()).getTitle(),
                    "");
        }

        @Override
        public void destroyNotification(final BaseDownloadTask task) {
            super.destroyNotification(task);
            Utils.runSingleThread(new Runnable() {
                @Override
                public void run() {
                    if (task.getStatus() == FileDownloadStatus.completed) {
                        YouTubeVideo youTubeVideo = (YouTubeVideo) task.getTag();
                        String path = task.getPath();

                        DownloadedVideosDb.getVideoDownloadsDb().add(youTubeVideo, path);

                        showCompletedNotification(task.getId(), youTubeVideo.getTitle());

                        RatingActivity.launch(PlayTubeApp.getContext(), "", PlayTubeApp.getStr(R.string.rating_text));

                    } else {
                        File file = new File(task.getPath());
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            });
        }

        private void showCompletedNotification(int id, String title) {
            NotificationCompat.Builder builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext(), "download_finished");
            Intent intent = new Intent(PlayTubeApp.getContext(), DownloadActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(PlayTubeApp.getContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(String.format(PlayTubeApp.getContext().getResources().getString(R.string.video_downloaded), title))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.stat_sys_download_done);
            manager.notify(id, builder.build());
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
            e.printStackTrace();
            Utils.showLongToastSafe(R.string.error_download);
        }
    }

    public static class NotificationItem extends BaseNotificationItem {

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
            Intent intent = new Intent(PlayTubeApp.getContext(), MainActivity.class);

            this.pendingIntent = PendingIntent.getActivity(PlayTubeApp.getContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext(), "image_download");

            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setOngoing(true)
                    .setAutoCancel(true)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(android.R.drawable.stat_sys_download);

        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " prepare";
                    break;
                case FileDownloadStatus.started:
                    desc += " started";
                    break;
                case FileDownloadStatus.progress:
                    desc += " downloading... " + (int)(getSofar() * 1f / getTotal() * 1f * 100) + "%";
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    break;
            }

            builder.setContentTitle(getTitle())
                    .setContentText(desc);

            if (statusChanged) {
                builder.setTicker(desc);
            }

            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }

    }

}
