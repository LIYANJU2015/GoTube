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

package com.tubeplayer.player.business.youtube;

import com.google.api.services.youtube.model.Video;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.youtube.Tasks.GetYouTubeVideosTask;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;

/**
 * Returns a list of YouTube videos.
 *
 * <p>Do not run this directly, but rather use {@link GetYouTubeVideosTask}.</p>
 */
public abstract class GetYouTubeVideos {
	protected String nextPageToken = null;
	protected boolean noMoreVideoPages = false;

	/**
	 * Initialise this object.
	 *
	 * @throws IOException
	 */
	public abstract void init() throws IOException;


	/**
	 * Sets user's query. [optional]
	 */
	public void setQuery(String query) {
	}


	/**
	 * Gets the next page of videos.
	 *
	 * @return List of {@link YTubeVideo}s.
	 */
	public abstract List<YTubeVideo> getNextVideos();


	/**
	 * @return True if YouTube states that there will be no more video pages; false otherwise.
	 */
	public abstract boolean noMoreVideoPages();


	/**
	 * Converts {@link List} of {@link Video} to {@link List} of {@link YTubeVideo}.
	 *
	 * @param videoList {@link List} of {@link Video}.
	 * @return {@link List} of {@link YTubeVideo}.
	 */
	protected List<YTubeVideo> toYouTubeVideoList(List<Video> videoList) {
		List<YTubeVideo> youTubeVideoList = new ArrayList<>();

		if (videoList != null) {
			YTubeVideo youTubeVideo;

			for (Video video : videoList) {
				youTubeVideo = new YTubeVideo(video);
				if (!youTubeVideo.filterVideoByLanguage())
					youTubeVideoList.add(youTubeVideo);
			}
		}

		return youTubeVideoList;
	}


	protected String getPreferredRegion() {
		String region = TubeApp.getPreferenceManager()
				.getString(TubeApp.getStr(R.string.pref_key_preferred_region), "").trim();
		return (region.isEmpty() ? null : region);
	}

	/**
	 * Reset the fetching of videos. This will be called when a swipe to refresh is done.
	 */
	public void reset() {
		nextPageToken = null;
		noMoreVideoPages = false;
	}
}
