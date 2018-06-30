package com.tubeplayer.player.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.VideoCategory;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.bean.YTubePlaylist;
import com.tubeplayer.player.gui.businessobjects.MainActivityListener;
import com.tubeplayer.player.gui.businessobjects.PlaylistClickListener;
import com.tubeplayer.player.gui.businessobjects.adapters.PlaylistsGridAdapter;

/**
 * A fragment that displays the Playlists belonging to a Channel
 */
public class ChannelPlaylistsFragment extends VideosGridFragment implements PlaylistClickListener, SwipeRefreshLayout.OnRefreshListener {
	private PlaylistsGridAdapter    playlistsGridAdapter;
	private YTubeChannel channel;
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


		channel = (YTubeChannel)getArguments().getSerializable(ChannelBrowserFragment.CHANNEL_OBJ);
		playlistsGridAdapter.setYouTubeChannel(channel);


		gridView.setHasFixedSize(true);
		gridView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.video_grid_num_columns)));
		gridView.setAdapter(playlistsGridAdapter);

		return view;
	}

	@Override
	public String getFragmentName() {
		return TubeApp.getStr(R.string.playlists);
	}

	@Override
	public void onClickPlaylist(YTubePlaylist playlist) {
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
