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

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.youtube.GetChannelVideos;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.db.SubscriptionsDb;
import com.tube.playtube.R;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;

/**
 * Task to asynchronously get videos for a specific channel.
 */
public class GetChannelVideosTask extends AsyncTaskParallel<Void, Void, List<YTubeVideo>> {

	private GetChannelVideos getChannelVideos = new GetChannelVideos();
	private YTubeChannel channel;
	private GetChannelVideosTaskInterface getChannelVideosTaskInterface;


	public GetChannelVideosTask(YTubeChannel channel) {
		try {
			getChannelVideos.init();
			getChannelVideos.setPublishedAfter(getOneMonthAgo());
			getChannelVideos.setQuery(channel.getId());
			this.channel = channel;
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(TubeApp.getContext(),
							String.format(TubeApp.getContext().getString(R.string.could_not_get_videos), channel.getTitle()),
							Toast.LENGTH_LONG).show();
		} catch(Exception e) {
			e.printStackTrace();
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
	protected List<YTubeVideo> doInBackground(Void... voids) {
		List<YTubeVideo> videos = null;

		try {
			if (!isCancelled()) {
				videos = getChannelVideos.getNextVideos();
			}

			if (videos != null) {
				for (YTubeVideo video : videos)
					channel.addYouTubeVideo(video);
				if (channel.isUserSubscribed())
					SubscriptionsDb.getSubscriptionsDb().saveChannelVideos(channel);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return videos;
	}


	@Override
	protected void onPostExecute(List<YTubeVideo> youTubeVideos) {
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