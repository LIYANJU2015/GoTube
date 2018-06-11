package free.studio.tube.businessobjects.interfaces;

import free.studio.tube.businessobjects.YouTube.VideoStream.StreamMetaDataList;

/**
 * Interface to be used when retrieving the desired stream (per the user's preferences) from a Video.
 */
public interface GetStreamListener {

	void onGetStream(StreamMetaDataList streamMetaDataList);

	void onGetStreamError(String errorMessage);
}
