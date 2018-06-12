package free.studio.tube.gui.businessobjects.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import free.rm.gotube.R;
import free.studio.tube.businessobjects.Logger;
import free.studio.tube.businessobjects.Utils;
import free.studio.tube.businessobjects.YouTube.POJOs.YouTubeVideo;
import free.studio.tube.businessobjects.db.DownloadedVideosDb;
import free.studio.tube.gui.businessobjects.YouTubePlayer;
import free.studio.tube.gui.fragments.YouTubePlayerFragment;

/**
 * Created by liyanju on 2018/6/12.
 */

public class ListViewHolder extends RecyclerView.ViewHolder{

    private ImageView thumbnailIV;
    private TextView durationTV;
    private TextView titleTV;
    private TextView uploaderTV;
    private TextView itemadditionalDetails;

    public ListViewHolder(View itemView) {
        super(itemView);
        thumbnailIV = itemView.findViewById(R.id.itemThumbnailView);
        durationTV = itemView.findViewById(R.id.itemDurationView);
        titleTV = itemView.findViewById(R.id.itemVideoTitleView);
        uploaderTV = itemView.findViewById(R.id.itemUploaderView);
        itemadditionalDetails = itemView.findViewById(R.id.itemAdditionalDetails);
    }

    public void updateInfo(final Context context, final YouTubeVideo video) {
        Glide.with(context)
                .load(video.getThumbnailUrl())
                .apply(new RequestOptions().placeholder(R.drawable.dummy_thumbnail))
                .into(thumbnailIV);
        titleTV.setText(video.getTitle());
        durationTV.setText(video.getDuration());
        uploaderTV.setText(video.getPublishDatePretty());
        itemadditionalDetails.setText(video.getViewsCount());

        itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Utils.runSingleThread(new Runnable() {
                    @Override
                    public void run() {
                        if (video.isDownloaded()) {
                            Uri uri = video.getFileUri();
                            File file = new File(uri.getPath());

                            if(!file.exists()) {
                                DownloadedVideosDb.getVideoDownloadsDb().remove(video);
                                Utils.showLongToastSafe(R.string.playing_video_file_missing);
                            } else {
                                Utils.playDownloadVideo(context, uri);
                            }
                        } else {
                            YouTubePlayer.launch(context, video);
                        }
                    }
                });
            }
        });

    }
}
