package free.studio.tube.businessobjects.interfaces;

import java.util.List;

import free.studio.tube.businessobjects.YouTube.POJOs.YouTubeVideo;

/**
 * An interface to be used by a SQLiteOpenHelperEx database object, indicating that the videos in the database may be reordered.
 */
public interface OrderableDatabase {
	void updateOrder(List<YouTubeVideo> videos);
}
