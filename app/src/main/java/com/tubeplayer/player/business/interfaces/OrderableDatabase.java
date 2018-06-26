package com.tubeplayer.player.business.interfaces;

import java.util.List;

import com.tubeplayer.player.business.youtube.bean.YouTubeVideo;

/**
 * An interface to be used by a SQLiteOpenHelperEx database object, indicating that the videos in the database may be reordered.
 */
public interface OrderableDatabase {
	void updateOrder(List<YouTubeVideo> videos);
}
