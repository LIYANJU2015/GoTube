/*
 * GoTube
 * Copyright (C) 2016  Ramon Mifsud
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

package com.tubeplayer.player.gui.businessobjects;

import android.content.Context;

import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.gui.player.YouTubePlayerActivity;

/**
 * Launches YouTube player.
 */
public class YouTubePlayer {

	private static final String TAG = YouTubePlayer.class.getSimpleName();

	public static void launch(Context context, YTubeVideo youTubeVideo) {
//		Intent i = new Intent(context, YouTubePlayerActivity.class);
//		i.putExtra(YouTubePlayerFragment.YOUTUBE_VIDEO_OBJ, youTubeVideo);
//		context.startActivity(i);
//		activity.overridePendingTransition(R.anim.slide_bottom_in, 0);

		if (SuperVersions.SuperVersionHandler.isShowAd()) {
			YouTubePlayerActivity.launch(context, youTubeVideo);
		} else if (youTubeVideo != null) {
			com.tubewebplayer.YouTubePlayerActivity.launch(context, youTubeVideo.getId());
		}
	}


}
