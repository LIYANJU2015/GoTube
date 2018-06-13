/*
 * GoTube
 * Copyright (C) 2016  Ramon Mifsud
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

package com.playtube.player.gui.businessobjects;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;

import com.playtube.player.businessobjects.Logger;
import com.playtube.player.businessobjects.YouTube.Tasks.GetYouTubeVideosTask;

/**
 * Loading progress bar that will be displayed by {@link GetYouTubeVideosTask}
 * when data is being retrieved from YouTube servers.
 */
public class LoadingProgressBar {

	private View progressBar = null;

	private static volatile LoadingProgressBar	loadingProgressBar = null;

	private Handler mMainHandler = new Handler(Looper.getMainLooper());

	private long startTime;


	public synchronized static LoadingProgressBar get() {
		return loadingProgressBar;
	}


	public synchronized void setProgressBar(View progressBar) {
		// hide the old progress bar (if any)
//		hide();

		// set the new progress bar
		this.progressBar = progressBar;
	}


	public synchronized void show() {
		if (progressBar != null && progressBar instanceof SwipeRefreshLayout) {
			mMainHandler.removeCallbacksAndMessages(null);
			((SwipeRefreshLayout)progressBar).setRefreshing(true);
			startTime = System.currentTimeMillis();
			Logger.d("loading", "loading show time" );
		}

	}


	public synchronized void hide() {
		if (progressBar != null && progressBar instanceof SwipeRefreshLayout) {
			if (startTime == 0) {
				((SwipeRefreshLayout) progressBar).setRefreshing(false);
				return;
			}

			long in = Math.abs(System.currentTimeMillis() - startTime);
			Logger.d("loading", "loading hide time" + in);

			if (in < 3000) {
				mMainHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						Logger.d("loading", "hide success " );
						((SwipeRefreshLayout) progressBar).setRefreshing(false);
					}
				}, 3000 - in);
			} else {
				((SwipeRefreshLayout) progressBar).setRefreshing(false);
			}
		}
	}

}
