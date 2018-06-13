/*
 * GoTube
 * Copyright (C) 2018  Ramon Mifsud
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

package com.playtube.player.gui.businessobjects.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.playtube.player.app.GoTubeApp;
import com.playtube.player.businessobjects.Utils;
import com.playtube.player.businessobjects.YouTube.POJOs.YouTubeVideo;
import com.playtube.player.businessobjects.db.Tasks.IsVideoBookmarkedTask;
import com.playtube.player.gui.activities.ThumbnailViewerActivity;
import com.playtube.player.gui.businessobjects.MainActivityListener;
import com.playtube.player.gui.businessobjects.YouTubePlayer;
import com.tube.playtube.R;

/**
 * A ViewHolder for the videos grid view.
 */
class GridViewHolder extends RecyclerView.ViewHolder {
	/** YouTube video */
	private YouTubeVideo youTubeVideo = null;
	private Context                 context = null;
	private MainActivityListener mainActivityListener;
	private boolean                 showChannelInfo;

	private TextView titleTextView;
	private TextView channelTextView;
	private TextView thumbsUpPercentageTextView;
	private TextView videoDurationTextView;
	private TextView publishDateTextView;
	private ImageView thumbnailImageView;
	private TextView viewsTextView;



	/**
	 * Constructor.
	 *
	 * @param view              Cell view (parent).
	 * @param listener          MainActivity listener.
	 * @param showChannelInfo   True to display channel information (e.g. channel name) and allows
	 *                          user to open and browse the channel; false to hide such information.
	 */
	public GridViewHolder(View view, MainActivityListener listener, boolean showChannelInfo) {
		super(view);

		titleTextView = view.findViewById(R.id.title_text_view);
		channelTextView = view.findViewById(R.id.channel_text_view);
		thumbsUpPercentageTextView = view.findViewById(R.id.thumbs_up_text_view);
		videoDurationTextView = view.findViewById(R.id.video_duration_text_view);
		publishDateTextView = view.findViewById(R.id.publish_date_text_view);
		thumbnailImageView = view.findViewById(R.id.thumbnail_image_view);
		viewsTextView = view.findViewById(R.id.views_text_view);

		this.mainActivityListener = listener;
		this.showChannelInfo = showChannelInfo;

		thumbnailImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View thumbnailView) {
				if (youTubeVideo != null) {
					YouTubePlayer.launch(context, youTubeVideo);
				}
			}
		});

		View.OnClickListener channelOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mainActivityListener != null)
					mainActivityListener.onChannelClick(youTubeVideo.getChannelId());
			}
		};

		view.findViewById(R.id.channel_layout).setOnClickListener(showChannelInfo ? channelOnClickListener : null);

		view.findViewById(R.id.options_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsButtonClick(v);
			}
		});
	}



	/**
	 * Updates the contents of this ViewHold such that the data of these views is equal to the
	 * given youTubeVideo.
	 *
	 * @param youTubeVideo		{@link YouTubeVideo} instance.
	 */
	public void updateInfo(YouTubeVideo youTubeVideo, Context context, MainActivityListener listener) {
		this.youTubeVideo = youTubeVideo;
		this.context = context;
		this.mainActivityListener = listener;
		updateViewsData(this.youTubeVideo);
	}


	/**
	 * This method will update the {@link View}s of this object reflecting the supplied video.
	 *
	 * @param video		{@link YouTubeVideo} instance.
	 */
	private void updateViewsData(YouTubeVideo video) {
		titleTextView.setText(video.getTitle());
		channelTextView.setText(showChannelInfo ? video.getChannelName() : "");
		publishDateTextView.setText(video.getPublishDatePretty());
		videoDurationTextView.setText(video.getDuration());

		if (!showChannelInfo || TextUtils.isEmpty(video.getChannelName())) {
			((RelativeLayout.LayoutParams)viewsTextView.getLayoutParams())
					.topMargin = Utils.dp2px(8);
		}
		viewsTextView.setText(video.getViewsCount());


		Glide.with(context)
				.load(video.getThumbnailUrl())
				.apply(new RequestOptions().placeholder(R.drawable.default_thumbnail))
				.into(thumbnailImageView);

		if (video.getThumbsUpPercentageStr() != null) {
			thumbsUpPercentageTextView.setVisibility(View.VISIBLE);
			thumbsUpPercentageTextView.setText(video.getThumbsUpPercentageStr());
		} else {
			thumbsUpPercentageTextView.setVisibility(View.INVISIBLE);
		}
	}



 	private void onOptionsButtonClick(final View view) {
		final PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
		popupMenu.getMenuInflater().inflate(R.menu.video_options_menu, popupMenu.getMenu());
		Menu menu = popupMenu.getMenu();
		new IsVideoBookmarkedTask(youTubeVideo, menu).executeInParallel();


		if (youTubeVideo.isDownloaded()) {
			popupMenu.getMenu().findItem(R.id.delete_download).setVisible(true);
			popupMenu.getMenu().findItem(R.id.download_video).setVisible(false);
		} else {
			popupMenu.getMenu().findItem(R.id.delete_download).setVisible(false);
			boolean allowDownloadsOnMobile = GoTubeApp.getPreferenceManager().getBoolean(GoTubeApp.getStr(R.string.pref_key_allow_mobile_downloads), false);
			if(GoTubeApp.isConnectedToWiFi() || (GoTubeApp.isConnectedToMobile() && allowDownloadsOnMobile))
				popupMenu.getMenu().findItem(R.id.download_video).setVisible(true);
			else
				popupMenu.getMenu().findItem(R.id.download_video).setVisible(false);
		}

		if (!GoTubeApp.isSpecial()) {
			popupMenu.getMenu().findItem(R.id.delete_download).setVisible(false);
			popupMenu.getMenu().findItem(R.id.download_video).setVisible(false);
		}

		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.menu_open_video_with:
						Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youTubeVideo.getVideoUrl()));
						context.startActivity(browserIntent);
						return true;
					case R.id.share:
						youTubeVideo.shareVideo(view.getContext());
						return true;
					case R.id.copyurl:
						youTubeVideo.copyUrl(context);
						return true;
					case R.id.bookmark_video:
						youTubeVideo.bookmarkVideo(context, popupMenu.getMenu());
						return true;
					case R.id.unbookmark_video:
						youTubeVideo.unbookmarkVideo(context, popupMenu.getMenu());
						return true;
					case R.id.view_thumbnail:
						Intent i = new Intent(context, ThumbnailViewerActivity.class);
						i.putExtra(ThumbnailViewerActivity.YOUTUBE_VIDEO, youTubeVideo);
						context.startActivity(i);
						return true;
					case R.id.delete_download:
						youTubeVideo.removeDownload();
						return true;
					case R.id.download_video:
						youTubeVideo.downloadVideo(context);
						return true;
				}
				return false;
			}
		});
		popupMenu.show();
	}

}
