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

package free.studio.tube.businessobjects.YouTube.Tasks;

import java.util.List;

import free.studio.tube.businessobjects.YouTube.POJOs.YouTubeVideo;

/**
 * Interface used by {@link GetChannelVideosTask} to return the videos belonging to a channel.
 */
public interface GetChannelVideosTaskInterface {
	void onGetVideos(List<YouTubeVideo> videos);
}
