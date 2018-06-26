package com.tubeplayer.player.business.interfaces;

import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaDataList;

/**
 * Interface to be used when retrieving the desired stream (per the user's preferences) from a Video.
 */
public interface GetStreamListener {

	void onGetStream(StreamMetaDataList streamMetaDataList);

	void onGetStreamError(String errorMessage);
}
