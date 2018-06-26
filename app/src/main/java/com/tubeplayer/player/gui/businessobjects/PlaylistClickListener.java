package com.tubeplayer.player.gui.businessobjects;

import com.tubeplayer.player.business.youtube.bean.YouTubePlaylist;

/**
 * Interface for an object that will respond to a Playlist being clicked on
 */
public interface PlaylistClickListener {
	void onClickPlaylist(YouTubePlaylist playlist);
}
