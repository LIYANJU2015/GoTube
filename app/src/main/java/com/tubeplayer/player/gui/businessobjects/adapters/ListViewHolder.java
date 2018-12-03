package com.tubeplayer.player.gui.businessobjects.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.tube.playtube.R;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.business.VideoCategory;
import com.tubeplayer.player.business.db.DownloadedVideosDb;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.gui.businessobjects.YouTubePlayer;
import com.tubeplayer.player.gui.fragments.DownloadedVideosFragment;

import java.io.File;

/**
 * Created by liyanju on 2018/6/12.
 */

public class ListViewHolder extends RecyclerView.ViewHolder {

    private ImageView thumbnailIV;
    private TextView durationTV;
    private TextView titleTV;
    private TextView uploaderTV;
    private TextView itemadditionalDetails;
    private VideoCategory videoCategory;

    public ListViewHolder(View itemView, VideoCategory videoCategory) {
        super(itemView);
        thumbnailIV = itemView.findViewById(R.id.itemThumbnailView);
        durationTV = itemView.findViewById(R.id.itemDurationView);
        titleTV = itemView.findViewById(R.id.itemVideoTitleView);
        uploaderTV = itemView.findViewById(R.id.itemUploaderView);
        itemadditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
        this.videoCategory = videoCategory;
    }

    public void updateInfo(final Context context, final YTubeVideo video) {
        Glide.with(context)
                .load(video.getThumbnailUrl())
                .apply(new RequestOptions().placeholder(R.drawable.default_thumbnail))
                .into(thumbnailIV);
        titleTV.setText(video.getTitle());
        durationTV.setText(video.getDuration());
        uploaderTV.setText(video.getPublishDatePretty());
        itemadditionalDetails.setText(video.getViewsCount());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.runSingleThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (video.isDownloaded()) {
                                Uri uri = video.getFileUri();
                                File file = new File(uri.getPath());

                                if (!file.exists()) {
                                    DownloadedVideosDb.getVideoDownloadsDb().remove(video);
                                    Utils.showLongToastSafe(R.string.playing_video_file_missing);
                                } else {
                                    Utils.playDownloadVideo(context, uri);
//                                try {
//                                    if (GetVideoActivity.sActivity != null) {
//                                        FBAdUtils.showAdDialog(GetVideoActivity.sActivity, Utils.NATIVE_AD_HIGHT_ID);
//                                    }
//                                } catch (Throwable e) {
//                                    e.printStackTrace();
//                                }
                                    DownloadedVideosFragment.sIsPlayDownload = true;
                                }
                            } else {
                                YouTubePlayer.launch(context, video);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (videoCategory == VideoCategory.DOWNLOADED_VIDEOS) {
                    try {
                        new MaterialDialog.Builder(context)
                                .contentColor(ContextCompat.getColor(context, R.color.black_color)).content(R.string.delete_download).onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        }).negativeColor(ContextCompat.getColor(context, R.color.colorPrimary)).negativeText("no")
                                .positiveColor(ContextCompat.getColor(context, R.color.colorPrimary)).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Utils.runSingleThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            video.removeDownload();
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }

                                });
                            }
                        }).positiveText("yes").show();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

    }
}
