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

package com.tubeplayer.player.business.youtube.Tasks;

import android.widget.Toast;

import com.google.api.client.util.DateTime;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.tubeplayer.player.app.PlayTubeApp;
import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.youtube.GetChannelVideos;
import com.tubeplayer.player.business.youtube.bean.YouTubeChannel;
import com.tubeplayer.player.business.db.SubscriptionsDb;
import com.tube.playtube.R;
import com.tubeplayer.player.business.youtube.bean.YouTubeVideo;

/**
 * Task to asynchronously get videos for a specific channel.
 */
public class GetChannelVideosTask extends AsyncTaskParallel<Void, Void, List<YouTubeVideo>> {

	private GetChannelVideos getChannelVideos = new GetChannelVideos();
	private YouTubeChannel channel;
	private GetChannelVideosTaskInterface getChannelVideosTaskInterface;


	public GetChannelVideosTask(YouTubeChannel channel) {
		try {
			getChannelVideos.init();
			getChannelVideos.setPublishedAfter(getOneMonthAgo());
			getChannelVideos.setQuery(channel.getId());
			this.channel = channel;
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(PlayTubeApp.getContext(),
							String.format(PlayTubeApp.getContext().getString(R.string.could_not_get_videos), channel.getTitle()),
							Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Once set, this class will only return videos published after the specified date.  If the date
	 * is set to null, then the class will return videos that are less than one month old.
	 */
	public GetChannelVideosTask setPublishedAfter(DateTime publishedAfter) {
		getChannelVideos.setPublishedAfter(publishedAfter != null ? publishedAfter : getOneMonthAgo());
		return this;
	}

	public GetChannelVideosTask setGetChannelVideosTaskInterface(GetChannelVideosTaskInterface getChannelVideosTaskInterface) {
		this.getChannelVideosTaskInterface = getChannelVideosTaskInterface;
		return this;
	}

	@Override
	protected List<YouTubeVideo> doInBackground(Void... voids) {
		List<YouTubeVideo> videos = null;

		if (!isCancelled()) {
			videos = getChannelVideos.getNextVideos();
		}

		if(videos != null) {
			for (YouTubeVideo video : videos)
				channel.addYouTubeVideo(video);
			if(channel.isUserSubscribed())
				SubscriptionsDb.getSubscriptionsDb().saveChannelVideos(channel);
		}
		return videos;
	}


	@Override
	protected void onPostExecute(List<YouTubeVideo> youTubeVideos) {
		if(getChannelVideosTaskInterface != null)
			getChannelVideosTaskInterface.onGetVideos(youTubeVideos);
	}


	private DateTime getOneMonthAgo() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MONTH, -1);
		Date date = calendar.getTime();
		return new DateTime(date);
	}

}