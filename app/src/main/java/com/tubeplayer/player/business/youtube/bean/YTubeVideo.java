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

package com.tubeplayer.player.business.youtube.bean;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.FileDownloader;
import com.tubeplayer.player.business.FileDownloaderHelper;
import com.tubeplayer.player.business.Logger;
import com.tubeplayer.player.business.youtube.VideoStream.ParseStreamMetaData;
import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaDataList;
import com.tubeplayer.player.business.db.BookmarksDb;
import com.tubeplayer.player.business.db.DownloadedVideosDb;
import com.tubeplayer.player.business.interfaces.GetStreamListener;
import com.tube.playtube.R;
import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaData;

/**
 * Represents a YouTube video.
 */
public class YTubeVideo implements Serializable {

	/** YouTube video ID. */
	private String	id;
	/** Video title. */
	private String	title;
	/** Channel ID. */
	private String channelId;
	/** Channel name. */
	private String	channelName;
	/** The total number of 'likes'. */
	private String	likeCount;
	/** The total number of 'dislikes'. */
	private String	dislikeCount;
	/** The percentage of people that thumbs-up this video (format:  "<percentage>%"). */
	private String	thumbsUpPercentageStr;
	private int		thumbsUpPercentage;
	/** Video duration string (e.g. "5:15"). */
	private String	duration;
	/** Total views count.  This can be <b>null</b> if the video does not allow the user to
	 * like/dislike it. */
	private String	viewsCount;
	/** The date/time of when this video was published. */
	private DateTime publishDate;
	private String   publishDatePretty;
	/** Thumbnail URL. */
	private String	thumbnailUrl;
	/** Thumbnail URL (maximum resolution). */
	private String  thumbnailMaxResUrl;
	/** The language of this video.  (This tends to be ISO 639-1).  */
	private String	language;
	/** The description of the video (set by the YouTuber/Owner). */
	private String	description;
	/** Set to true if the video is a current live stream. */
	private boolean isLiveStream;

	/** Default preferred language(s) -- by default, no language shall be filtered out. */
	private static final Set<String> defaultPrefLanguages = new HashSet<>(TubeApp.getStringArrayAsList(R.array.languages_iso639_codes));

	public YTubeVideo() {

	}

	public YTubeVideo(Video video) {
		this.id = video.getId();

		if (video.getSnippet() != null) {
			this.title       = video.getSnippet().getTitle();
			this.channelId   = video.getSnippet().getChannelId();
			this.channelName = video.getSnippet().getChannelTitle();
			setPublishDate(video.getSnippet().getPublishedAt());

			if (video.getSnippet().getThumbnails() != null) {
				Thumbnail thumbnail = video.getSnippet().getThumbnails().getHigh();
				if (thumbnail != null)
					this.thumbnailUrl = thumbnail.getUrl();

				thumbnail = video.getSnippet().getThumbnails().getMaxres();
				if (thumbnail != null)
					this.thumbnailMaxResUrl = thumbnail.getUrl();
			}

			this.language = video.getSnippet().getDefaultAudioLanguage() != null ? video.getSnippet().getDefaultAudioLanguage()
					: (video.getSnippet().getDefaultLanguage());

			this.description = video.getSnippet().getDescription();
		}

		if (video.getContentDetails() != null) {
			setDuration(video.getContentDetails().getDuration());
			setIsLiveStream();
		}

		if (video.getStatistics() != null) {
			BigInteger	likeCount = video.getStatistics().getLikeCount(),
						dislikeCount = video.getStatistics().getDislikeCount();

			setThumbsUpPercentage(likeCount, dislikeCount);

			this.viewsCount = String.format(TubeApp.getStr(R.string.views),
											video.getStatistics().getViewCount());

			if (likeCount != null)
				this.likeCount = String.format("%,d", video.getStatistics().getLikeCount());

			if (dislikeCount != null)
				this.dislikeCount = String.format("%,d", video.getStatistics().getDislikeCount());
		}
	}



	/**
	 * Sets the {@link #thumbsUpPercentageStr}, i.e. the percentage of people that thumbs-up this video
	 * (format:  "<percentage>%").
	 *
	 * @param likedCountInt		Total number of "likes".
	 * @param dislikedCountInt	Total number of "dislikes".
	 */
	private void setThumbsUpPercentage(BigInteger likedCountInt, BigInteger dislikedCountInt) {
		String	fullPercentageStr = null;
		int		percentageInt = -1;

		// some videos do not allow users to like/dislike them:  hence likedCountInt / dislikedCountInt
		// might be null in those cases
		if (likedCountInt != null  &&  dislikedCountInt != null) {
			BigDecimal	likedCount      = new BigDecimal(likedCountInt),
						dislikedCount   = new BigDecimal(dislikedCountInt),
						totalVoteCount  = likedCount.add(dislikedCount),    // liked and disliked counts
						likedPercentage = null;

			if (totalVoteCount.compareTo(BigDecimal.ZERO) != 0) {
				likedPercentage = (likedCount.divide(totalVoteCount, MathContext.DECIMAL128)).multiply(new BigDecimal(100));

				// round the liked percentage to 0 decimal places and convert it to string
				String percentageStr = likedPercentage.setScale(0, RoundingMode.HALF_UP).toString();
				fullPercentageStr = percentageStr + "%";
				percentageInt = Integer.parseInt(percentageStr);
			}
		}

		this.thumbsUpPercentageStr = fullPercentageStr;
		this.thumbsUpPercentage = percentageInt;
	}


	/**
	 * Sets the {@link #duration} by converts ISO 8601 duration to human readable string.
	 *
	 * @param duration ISO 8601 duration.
	 */
	private void setDuration(String duration) {
		this.duration = VideoDuration.toHumanReadableString(duration);
	}


	/**
	 * Using {@link #duration} it detects if the video/stream is live or not.
	 *
	 * <p>If it is live, then it will change {@link #duration} to "LIVE" and modify {@link #publishDate}
	 * to current time (which will appear as "moments ago" when using {@link PrettyTimeEx}).</p>
	 */
	private void setIsLiveStream() {
		// is live stream?
		if (duration.equals("0:00")) {
			isLiveStream = true;
			duration = TubeApp.getStr(R.string.LIVE);
			setPublishDate(new DateTime(new Date()));    // set publishDate to current (as there is a bug in YouTube API in which live videos's date is incorrect)
		} else {
			isLiveStream = false;
		}
	}


	/**
	 * Sets the publishDate and publishDatePretty.
	 */
	private void setPublishDate(DateTime publishDate) {
		try {
			this.publishDate = publishDate;
			this.publishDatePretty = (publishDate != null) ? new PrettyTimeEx().format(publishDate) : "???";
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getChannelId() {
		return channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	/**
	 * @return True if the video allows the users to like/dislike it.
	 */
	public boolean isThumbsUpPercentageSet() {
		return (thumbsUpPercentageStr != null);
	}

	/**
	 * @return The thumbs up percentage (as an integer).  Can return <b>-1</b> if the video does not
	 * allow the users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
	 */
	public int getThumbsUpPercentage() {
		return thumbsUpPercentage;
	}

	/**
	 * @return The thumbs up percentage (format:  "«percentage»%").  Can return <b>null</b> if the
	 * video does not allow the users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
	 */
	public String getThumbsUpPercentageStr() {
		return thumbsUpPercentageStr;
	}

	/**
	 * @return The total number of 'likes'.  Can return <b>null</b> if the video does not allow the
	 * users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
	 */
	public String getLikeCount() {
		return likeCount;
	}

	/**
	 * @return The total number of 'dislikes'.  Can return <b>null</b> if the video does not allow the
	 * users to like/dislike it.  Refer to {@link #isThumbsUpPercentageSet}.
	 */
	public String getDislikeCount() {
		return dislikeCount;
	}

	public String getDuration() {
		return duration;
	}

	public String getViewsCount() {
		return viewsCount;
	}

	public DateTime getPublishDate() {
		return publishDate;
	}

	/**
	 * Gets the {@link #publishDate} as a pretty string.
	 */
	public String getPublishDatePretty() {
		return publishDatePretty;
	}

	/**
	 * Given that {@link #publishDatePretty} is being cached once generated, this method will allow
	 * you to regenerate and reset the {@link #publishDatePretty}.
	 */
	public void forceRefreshPublishDatePretty() {
		setPublishDate(publishDate);
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getThumbnailMaxResUrl() {
		return thumbnailMaxResUrl;
	}

	public String getVideoUrl() {
		return String.format("https://youtu.be/%s", id);
	}

	public String getLanguage() {
		return language;
	}

	public String getDescription() {
		return description;
	}

	public boolean isLiveStream() {
		return isLiveStream;
	}

	/**
	 * Return true if this video does not meet the preferred language criteria;  false otherwise.
	 * Many YouTube videos do not set the language, hence this method will not be accurate.
	 *
	 * @return  True to filter out the video; false otherwise.
	 */
	public boolean filterVideoByLanguage() {
		Set<String> preferredLanguages = getPreferredLanguages();

		// if the video's language is not defined (i.e. null)
		//    OR if there is no linguistic content to the video (zxx)
		//    OR if the language is undefined (und)
		// then we are NOT going to filter this video
		if (getLanguage() == null  ||  getLanguage().equalsIgnoreCase("zxx")  ||  getLanguage().equalsIgnoreCase("und"))
			return false;

		// if there are no preferred languages, then it means we must not filter this video
		if (preferredLanguages == null  ||  preferredLanguages.isEmpty())
			return false;

		// if this video's language is equal to the user's preferred one... then do NOT filter it out
		for (String prefLanguage : preferredLanguages) {
			if (getLanguage().matches(prefLanguage))
				return false;
		}

		// this video is undesirable, hence we are going to filter it
		Log.i("FILTERING Video", getTitle() + "[" + getLanguage() + "]");
		return true;
	}


	/**
	 * @return Set of user's preferred ISO 639 language codes (regex).
	 */
	private Set<String> getPreferredLanguages() {
		return TubeApp.getPreferenceManager().getStringSet(TubeApp.getStr(R.string.pref_key_preferred_languages), defaultPrefLanguages);
	}


	/**
	 * Extracts the video ID from the given video URL.
	 *
	 * @param url	YouTube video URL.
	 * @return ID if everything went as planned; null otherwise.
	 */
	public static String getYouTubeIdFromUrl(String url) {
		if (url == null)
			return null;

		// TODO:  support playlists (i.e. video_ids=... <-- URL submitted via email by YouTube)
		final String pattern = "(?<=v=|/videos/|embed/|youtu\\.be/|/v/|/e/|video_ids=)[^#&?%]*";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url);

		return matcher.find() ? matcher.group() /*video id*/ : null;
	}

	public void bookmarkVideo(Context context, Menu menu) {
		boolean successBookmark = BookmarksDb.getBookmarksDb().add(this);
		Toast.makeText(context,
						successBookmark  ?  R.string.video_bookmarked  :  R.string.video_bookmarked_error,
						Toast.LENGTH_LONG).show();

		if (successBookmark) {
			menu.findItem(R.id.bookmark_video).setVisible(false);
			menu.findItem(R.id.unbookmark_video).setVisible(true);
		}
	}

	public void unbookmarkVideo(Context context, Menu menu) {
		boolean successUnbookmark = BookmarksDb.getBookmarksDb().remove(this);
		Toast.makeText(context,
						successUnbookmark  ?  R.string.video_unbookmarked  :  R.string.video_unbookmarked_error,
						Toast.LENGTH_LONG).show();

		if (successUnbookmark) {
			menu.findItem(R.id.bookmark_video).setVisible(true);
			menu.findItem(R.id.unbookmark_video).setVisible(false);
		}
	}

	public void shareVideo(Context context) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, getVideoUrl());
		context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)));
	}

	public void copyUrl(Context context) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("Video URL", getVideoUrl());
		clipboard.setPrimaryClip(clip);
		Toast.makeText(context, R.string.url_copied_to_clipboard, Toast.LENGTH_SHORT).show();
	}


	/**
	 * Get the stream for this Video, based on the user's preference. When done, the stream will be returned
	 * via the passed GetDesiredStreamListener.
	 * @param listener Instance of {@link GetStreamListener} to pass the stream through
	 */
	public void getStream(GetStreamListener listener) {
		getStreamTask = new GetStreamTask(listener);
		getStreamTask.executeInParallel();
	}

	private transient GetStreamTask getStreamTask;

	public void cancelGetStream() {
		if (getStreamTask != null) {
			getStreamTask.cancel(true);
		}
	}


	/**
	 * Remove local copy of this video, and delete it from the VideoDownloads DB
	 */
	public void removeDownload() {
		Uri uri = DownloadedVideosDb.getVideoDownloadsDb().getVideoFileUri(YTubeVideo.this);
		File file = new File(uri.getPath());
		if(file.exists()) {
			file.delete();
		}
		DownloadedVideosDb.getVideoDownloadsDb().remove(YTubeVideo.this);
	}

	/**
	 * Get the Uri for the local copy of this Video.
	 * @return Uri
	 */
	public Uri getFileUri() {
		return DownloadedVideosDb.getVideoDownloadsDb().getVideoFileUri(this);
	}

	/**
	 * Returns whether or not this video has been downloaded.
	 * @return
	 */
	public boolean isDownloaded() {
		return DownloadedVideosDb.getVideoDownloadsDb().isVideoDownloaded(YTubeVideo.this);
	}

	public void downloadVideo(Context context, String downloadurl) {
		if (isDownloaded()) {
			Toast.makeText(context, R.string.downloaded_tip, Toast.LENGTH_LONG).show();
			return;
		}

		FileDownloaderHelper.addDownloadTask(this, downloadurl);
	}

	/**
	 * Downloads this video.
	 *
	 * @param context Context
	 */
	public void downloadVideo(final Context context) {
		if (isDownloaded()) {
			Toast.makeText(context, R.string.downloaded_tip, Toast.LENGTH_LONG).show();
			return;
		}

		FacebookReport.logSentDownloadStart(title);

		getStream(new GetStreamListener() {
			@Override
			public void onGetStream(StreamMetaDataList streamMetaDataList) {
				StreamMetaData desiredStream = streamMetaDataList.getDesiredStream();
				if (desiredStream == null) {
					return;
				}

				FileDownloaderHelper.addDownloadTask(YTubeVideo.this, desiredStream.getUri().toString());
			}
			@Override
			public void onGetStreamError(String errorMessage) {
				Logger.e(YTubeVideo.this, "Stream error: %s", errorMessage);
				Toast.makeText(TubeApp.getContext(),
								String.format(TubeApp.getContext().getString(R.string.video_download_stream_error), getTitle()),
								Toast.LENGTH_LONG).show();
			}
		});
	}



	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * AsyncTask to retrieve the desired stream for this Video.
	 */
	private class GetStreamTask extends AsyncTaskParallel<Void, Exception, StreamMetaDataList> {

		private GetStreamListener listener;

		GetStreamTask(GetStreamListener listener) {
			this.listener = listener;
		}

		@Override
		protected StreamMetaDataList doInBackground(Void... param) {
			return getVideoStreamList();
		}

		/**
		 * Returns a list of video/stream meta-data that is supported by this app (with respect to this
		 * video).
		 *
		 * @return A list of {@link StreamMetaDataList}.
		 */
		private StreamMetaDataList getVideoStreamList() {
			StreamMetaDataList streamMetaDataList;

			ParseStreamMetaData streamParser = new ParseStreamMetaData(id);
			streamMetaDataList = streamParser.getStreamMetaDataList();

			return streamMetaDataList;
		}

		@Override
		protected void onPostExecute(StreamMetaDataList streamMetaDataList) {
			if (streamMetaDataList == null || streamMetaDataList.size() <= 0) {
				listener.onGetStreamError(streamMetaDataList.getErrorMessage());
			} else {
				listener.onGetStream(streamMetaDataList);
			}
		}

	}



	/**
	 * Downloads a YouTube video.
	 */
	private class VideoDownloader extends FileDownloader implements Serializable {

		@Override
		public void onFileDownloadStarted() {
			Toast.makeText(TubeApp.getContext(),
					String.format(TubeApp.getContext().getString(R.string.starting_video_download), getTitle()),
					Toast.LENGTH_LONG).show();

		}

		@Override
		public void onFileDownloadCompleted(boolean success, Uri localFileUri) {
			if (success) {
				success = DownloadedVideosDb.getVideoDownloadsDb().add(YTubeVideo.this, localFileUri.toString());
			}

			Toast.makeText(TubeApp.getContext(),
					String.format(TubeApp.getContext().getString(success ? R.string.video_downloaded : R.string.video_download_stream_error), getTitle()),
					Toast.LENGTH_LONG).show();

			FacebookReport.logSentDownloadEnd(title);
		}

		@Override
		public void onExternalStorageNotAvailable() {
			Toast.makeText(TubeApp.getContext(),
					R.string.external_storage_not_available,
					Toast.LENGTH_LONG).show();
		}

	}

}
