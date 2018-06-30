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

import java.io.IOException;
import java.util.List;

import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.business.db.BookmarksDb;


/**
 * Get user's bookmarked videos.
 */
public class GetBookmarksVideos extends GetYouTubeVideos {

	@Override
	public void init() throws IOException {
		noMoreVideoPages = false;
	}

	@Override
	public List<YTubeVideo> getNextVideos() {
		if (!noMoreVideoPages()) {
			noMoreVideoPages = true;
			return BookmarksDb.getBookmarksDb().getBookmarkedVideos();
		}

		return null;
	}

	@Override
	public boolean noMoreVideoPages() {
		return noMoreVideoPages;
	}

}