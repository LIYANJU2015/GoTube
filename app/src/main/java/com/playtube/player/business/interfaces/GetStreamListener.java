package com.playtube.player.business.interfaces;

import com.playtube.player.business.youtube.VideoStream.StreamMetaDataList;

/**
 * Interface to be used when retrieving the desired stream (per the user's preferences) from a Video.
 */
public interface GetStreamListener {

	void onGetStream(StreamMetaDataList streamMetaDataList);

	void onGetStreamError(String errorMessage);
}
