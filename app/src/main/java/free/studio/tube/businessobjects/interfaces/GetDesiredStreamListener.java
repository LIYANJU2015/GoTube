package free.studio.tube.businessobjects.interfaces;

import free.studio.tube.businessobjects.YouTube.VideoStream.StreamMetaData;

/**
 * Interface to be used when retrieving the desired stream (per the user's preferences) from a Video.
 */
public interface GetDesiredStreamListener {
	void onGetDesiredStream(StreamMetaData desiredStream);
	void onGetDesiredStreamError(String errorMessage);
}
