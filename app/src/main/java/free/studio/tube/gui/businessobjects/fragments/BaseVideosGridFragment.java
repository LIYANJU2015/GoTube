/*
 * GoTube
 * Copyright (C) 2017  Ramon Mifsud
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

package free.studio.tube.gui.businessobjects.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import free.rm.gotube.R;
import free.studio.tube.businessobjects.Logger;
import free.studio.tube.businessobjects.YouTube.Tasks.GetYouTubeVideosTask;
import free.studio.tube.gui.businessobjects.adapters.VideoGridAdapter;
import free.studio.tube.gui.fragments.VideosGridFragment;

/**
 * A class that supports swipe-to-refresh on {@link VideosGridFragment}.
 */
public abstract class BaseVideosGridFragment extends TabFragment implements SwipeRefreshLayout.OnRefreshListener {

	protected VideoGridAdapter videoGridAdapter;

	@BindView(R.id.swipeRefreshLayout)
	protected SwipeRefreshLayout swipeRefreshLayout;

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ButterKnife.bind(this, view);
		swipeRefreshLayout.setOnRefreshListener(this);
		swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getContext(), R.color.colorPrimary));
	}

	@Override
	public void onRefresh() {
		if (GetYouTubeVideosTask.sIsLoadMore) {
			Logger.e("onRefresh", "loading more no finish....");
			return;
		}
		videoGridAdapter.refresh(new Runnable() {
			@Override
			public void run() {
				if (swipeRefreshLayout != null) {
					swipeRefreshLayout.setRefreshing(false);
				}
			}
		});
	}

	public interface SwipeRefreshCallBack {

		void onRefreshStart();

		void onRefreshEnd();
	}

}
