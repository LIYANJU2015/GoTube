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

package com.tubeplayer.player.gui.activities;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.tubeplayer.player.gui.fragments.YouTubePlayerFragment;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.gui.businessobjects.BackButtonActivity;

/**
 * An {@link Activity} that contains an instance of
 * {@link YouTubePlayerFragment}.
 */
public class YouTubePlayerActivity extends BackButtonActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);

		FacebookReport.logSentVideoPlay();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		String str = TubeApp.getPreferenceManager().getString(getString(R.string.pref_key_screen_orientation), "auto");
		int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		if("landscape".equals(str)) orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
		if("portrait".equals(str)) orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
		if("sensor".equals(str)) orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
		setRequestedOrientation(orientation);
	}

	@Override
	protected void onStop() {
		super.onStop();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

}
