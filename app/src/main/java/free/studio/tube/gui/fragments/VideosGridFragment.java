/*
 * GoTube
 * Copyright (C) 2015  Ramon Mifsud
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License).
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package free.studio.tube.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.bumptech.glide.Glide;

import free.rm.gotube.R;
import free.studio.tube.businessobjects.VideoCategory;
import free.studio.tube.gui.businessobjects.MainActivityListener;
import free.studio.tube.gui.businessobjects.adapters.VideoGridAdapter;
import free.studio.tube.gui.businessobjects.fragments.BaseVideosGridFragment;

/**
 * A fragment that will hold a {@link GridView} full of YouTube videos.
 */
public abstract class VideosGridFragment extends BaseVideosGridFragment {

	protected RecyclerView	gridView;
	private View			progressBar = null;
	private int 			layoutResource = 0;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayoutResource(R.layout.videos_gridview);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// inflate the layout for this fragment
		View view = inflater.inflate(layoutResource, container, false);

		// set up the loading progress bar
		progressBar = view.findViewById(R.id.loading_progress_bar);

		// setup the video grid view
		gridView = view.findViewById(R.id.grid_view);
		if (videoGridAdapter == null) {
			videoGridAdapter = new VideoGridAdapter(getActivity());
		} else {
			videoGridAdapter.setContext(getActivity());
		}
//		videoGridAdapter.setProgressBar(progressBar);

		videoGridAdapter.setListener((MainActivityListener)getActivity());

		gridView.setHasFixedSize(true);
		gridView.setLayoutManager(new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.video_grid_num_columns)));
		gridView.setAdapter(videoGridAdapter);

		return view;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getVideoCategory() != null && videoGridAdapter != null) {
			videoGridAdapter.setGetVideosTaskCallBack(new SwipeRefreshCallBack() {
				@Override
				public void onRefreshStart() {
					if (swipeRefreshLayout != null) {
						swipeRefreshLayout.setRefreshing(true);
					}
				}

				@Override
				public void onRefreshEnd() {
					if (swipeRefreshLayout != null) {
						swipeRefreshLayout.setRefreshing(false);
					}
				}
			});
			videoGridAdapter.setVideoCategory(getVideoCategory(), getSearchString());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Glide.get(getActivity()).clearMemory();
	}

	/**
	 * In case a subclass of VideosGridFragment wants to use an alternate layout resource (e.g. Subscriptions).
 	 */
	protected void setLayoutResource(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	/**
	 * @return Returns the category of videos being displayed by this fragment.
	 */
	protected abstract VideoCategory getVideoCategory();


	/**
	 * @return Returns the search string used when setting the video category.  (Can be used to
	 * set the channel ID in case of VideoCategory.CHANNEL_VIDEOS).
	 */
	protected String getSearchString() {
		return null;
	}

	/**
	 * @return The fragment/tab name/title.
	 */
	public abstract String getFragmentName();
}
