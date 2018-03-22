package free.studio.tube.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import free.rm.GoTube.R;
import free.studio.tube.app.GoTubeApp;
import free.studio.tube.businessobjects.VideoCategory;
import free.studio.tube.businessobjects.YouTube.POJOs.YouTubeChannel;
import free.studio.tube.gui.businessobjects.adapters.VideoGridAdapter;

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
		return GoTubeApp.getStr(R.string.videos);
	}


	@Override
	public void onFragmentSelected() {
	}

}
