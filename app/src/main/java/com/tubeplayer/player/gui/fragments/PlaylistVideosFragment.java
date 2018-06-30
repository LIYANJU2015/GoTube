package com.tubeplayer.player.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.tube.playtube.R;
import com.tubeplayer.player.business.VideoCategory;
import com.tubeplayer.player.business.youtube.bean.YTubePlaylist;

/**
 * A Fragment that displays the videos of a playlist in a {@link VideosGridFragment}
 */
public class PlaylistVideosFragment extends VideosGridFragment {

	private YTubePlaylist youTubePlaylist;

	@BindView(R.id.playlist_banner_image_view)
	ImageView   playlistBannerImageView;
	@BindView(R.id.playlist_thumbnail_image_view)
	ImageView   playlistThumbnailImageView;
	@BindView(R.id.playlist_title_text_view)
	TextView    playlistTitleTextView;

	public static final String PLAYLIST_OBJ = "TubePlaylistVideosFragment.PLAYLIST_OBJ";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// sets the play list
		youTubePlaylist = (YTubePlaylist)getArguments().getSerializable(PLAYLIST_OBJ);

		// sets the layout resource which is used by onCreateView()
		setLayoutResource(R.layout.fragment_playlist_videos);

		View view = super.onCreateView(inflater, container, savedInstanceState);

		ButterKnife.bind(this, view);
		playlistTitleTextView.setText(youTubePlaylist.getTitle());

		// set the playlist's thumbnail
		Glide.with(getActivity())
				.load(youTubePlaylist.getThumbnailUrl())
				.apply(new RequestOptions().placeholder(R.drawable.buddy))
				.into(playlistThumbnailImageView);

		// set the channel's banner
		Glide.with(getActivity())
				.load(youTubePlaylist.getBannerUrl())
				.apply(new RequestOptions().placeholder(R.drawable.banner_default))
				.into(playlistBannerImageView);

		return view;
	}


	@Override
	public String getFragmentName() {
		return null;
	}


	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.PLAYLIST_VIDEOS;
	}


	@Override
	protected String getSearchString() {
		return youTubePlaylist.getId();
	}

}
