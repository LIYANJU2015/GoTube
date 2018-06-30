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

import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.Logger;

import java.io.IOException;

import com.tubeplayer.player.business.youtube.GetChannelsDetails;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.bean.YTubeChannelInterface;

/**
 * A task that given a channel ID it will try to initialize and return {@link YTubeChannel}.
 */
public class GetYouTubeChannelInfoTask extends AsyncTaskParallel<String, Void, YTubeChannel> {

	private YTubeChannelInterface youTubeChannelInterface;


	public GetYouTubeChannelInfoTask(YTubeChannelInterface youTubeChannelInterface) {
		this.youTubeChannelInterface = youTubeChannelInterface;
	}


	@Override
	protected YTubeChannel doInBackground(String... channelId) {
		YTubeChannel channel;

		try {
			channel = new GetChannelsDetails().getYouTubeChannel(channelId[0]);
		} catch (IOException e) {
			Logger.e(this, "Unable to get channel info.  ChannelID=" + channelId[0], e);
			channel = null;
		}

		return channel;
	}


	@Override
	protected void onPostExecute(YTubeChannel youTubeChannel) {
		if(youTubeChannelInterface != null) {
			youTubeChannelInterface.onGetYouTubeChannel(youTubeChannel);
		}
	}

}