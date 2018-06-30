package com.tubeplayer.player.business.interfaces;

import java.util.List;

import com.tubeplayer.player.business.youtube.bean.YTubeVideo;

/**
 * An interface to be used by a SQLiteOpenHelperEx database object, indicating that the videos in the database may be reordered.
 */
public interface OrderableDatabase {
	void updateOrder(List<YTubeVideo> videos);
}
