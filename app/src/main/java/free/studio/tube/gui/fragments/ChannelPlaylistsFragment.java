package free.studio.tube.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import free.rm.gotube.R;
import free.studio.tube.app.GoTubeApp;
import free.studio.tube.businessobjects.VideoCategory;
import free.studio.tube.businessobjects.YouTube.POJOs.YouTubeChannel;
import free.studio.tube.businessobjects.YouTube.POJOs.YouTubePlaylist;
import free.studio.tube.gui.businessobjects.MainActivityListener;
import free.studio.tube.gui.businessobjects.PlaylistClickListener;
import free.studio.tube.gui.businessobjects.adapters.PlaylistsGridAdapter;

/**
 * A fragment that displays the Playlists belonging to a Channel
 */
public class ChannelPlaylistsFragment extends VideosGridFragment implements PlaylistClickListener, SwipeRefreshLayout.OnRefreshListener {
	private PlaylistsGridAdapter    playlistsGridAdapter;
	private YouTubeChannel          channel;
	private MainActivityListener mainActivityListener;

	@BindView(R.id.swipeRefreshLayout)
	SwipeRefreshLayout swipeRefreshLayout;


	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.videos_gridview, container, false);

		ButterKnife.bind(this, view);
		swipeRefreshLayout.setOnRefreshListener(this);
		// setup the video grid view
		gridView = view.findViewById(R.id.grid_view);

		if (playlistsGridAdapter == null) {
			playlistsGridAdapter = new PlaylistsGridAdapter(getActivity(), this);
		} else {
			playlistsGridAdapter.setContext(getActivity());
		}


		channel = (YouTubeChannel)getArguments().getSerializable(ChannelBrowserFragment.CHANNEL_OBJ);
		playlistsGridAdapter.setYouTubeChannel(channel);


		gridView.setHasFixedSize(true);
		gridView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.video_grid_num_columns)));
		gridView.setAdapter(playlistsGridAdapter);

		return view;
	}

	@Override
	public String getFragmentName() {
		return GoTubeApp.getStr(R.string.playlists);
	}

	@Override
	public void onClickPlaylist(YouTubePlaylist playlist) {
		if (mainActivityListener != null)
			mainActivityListener.onPlaylistClick(playlist);
	}

	public void setMainActivityListener(MainActivityListener mainActivityListener) {
		this.mainActivityListener = mainActivityListener;
	}

	@Override
	public void onRefresh() {
		playlistsGridAdapter.refresh(new Runnable() {
			@Override
			public void run() {
				swipeRefreshLayout.setRefreshing(false);
			}
		});
	}


	@Override
	protected VideoCategory getVideoCategory() {
		return null;
	}

}
