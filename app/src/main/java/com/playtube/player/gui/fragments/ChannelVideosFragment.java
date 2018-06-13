package com.playtube.player.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.playtube.player.business.youtube.bean.YouTubeChannel;
import com.tube.playtube.R;
import com.playtube.player.app.PlayTubeApp;
import com.playtube.player.business.VideoCategory;
import com.playtube.player.gui.businessobjects.adapters.VideoGridAdapter;

/**
 * A fragment that displays the videos belonging to a channel.
 */
public class ChannelVideosFragment extends VideosGridFragment {
	/** YouTube Channel */
	private YouTubeChannel channel;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// get the channel
		channel = (YouTubeChannel)getArguments().getSerializable(ChannelBrowserFragment.CHANNEL_OBJ);

		// create and return the view
		View view =  super.onCreateView(inflater, container, savedInstanceState);

		if (channel != null)
			videoGridAdapter.setYouTubeChannel(channel);

		return view;
	}


	public void setYouTubeChannel(YouTubeChannel youTubeChannel) {
		channel = youTubeChannel;
		videoGridAdapter.setYouTubeChannel(youTubeChannel);
	}


	public VideoGridAdapter getVideoGridAdapter() {
		return videoGridAdapter;
	}


	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.CHANNEL_VIDEOS;
	}


	@Override
	protected String getSearchString() {
		return channel.getId();
	}


	@Override
	public String getFragmentName() {
		return PlayTubeApp.getStr(R.string.videos);
	}


	@Override
	public void onFragmentSelected() {
	}

}
