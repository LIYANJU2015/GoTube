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

import android.util.Log;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.CommentThread;
import com.google.api.services.youtube.model.CommentThreadListResponse;
import com.tubeplayer.player.business.youtube.bean.YTubeCommentThread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tubeplayer.player.business.youtube.bean.YTubeAPI;
import com.tubeplayer.player.business.youtube.bean.YTubeAPIKey;

/**
 * Queries the YouTube service and gets the comments of a video.
 */
public class GetCommentThreads {

	private String	videoId;
	private String	nextPageToken = null;
	private boolean	noMoreCommentPages = false;
	private YouTube.CommentThreads.List commentsList = null;

	private static final Long	MAX_RESULTS = 20L;
	private static final String	TAG = GetCommentThreads.class.getSimpleName();


	public void init(String videoId) throws IOException {
		this.videoId = videoId;
		this.commentsList = YTubeAPI.create().commentThreads()
				.list("snippet, replies")
				.setFields("items(snippet, replies), nextPageToken")
				.setKey(YTubeAPIKey.get().getYouTubeAPIKey())
				.setVideoId(videoId)
				.setTextFormat("plainText")
				.setMaxResults(MAX_RESULTS)
				.setOrder("relevance");
	}


	/**
	 * Will return the next page of comment threads.  If there are no more pages, then it will
	 * return null.
	 *
	 * @return	A list/page of {@link YTubeCommentThread}.
	 */
	public List<YTubeCommentThread> get() {
		List<YTubeCommentThread> commentThreadList = new ArrayList<>();

		if (noMoreCommentPages  ||  commentsList == null) {
			commentThreadList = null;
		} else {
			try {
				// set the page token/id to retrieve
				commentsList.setPageToken(nextPageToken);
				commentsList.setKey(YTubeAPIKey.get().getYouTubeAPIKey());

				// communicate with YouTube and get the comments
				CommentThreadListResponse response = commentsList.execute();
				List<CommentThread> videoComments = response.getItems();

				// convert the comments from CommentThread to YouTubeCommentThread
				if (!videoComments.isEmpty()) {
					for (CommentThread thread : videoComments) {
						commentThreadList.add(new YTubeCommentThread(thread));
					}
				}

				// set the next page token
				nextPageToken = response.getNextPageToken();

				// if nextPageToken is null, it means that there are no more comments
				if (nextPageToken == null)
					noMoreCommentPages = true;
			} catch (IOException ex) {
				Log.e(TAG, "An error has occurred while retrieving comments for video with id=" + videoId, ex);
			}
		}

		return commentThreadList;
	}
}
