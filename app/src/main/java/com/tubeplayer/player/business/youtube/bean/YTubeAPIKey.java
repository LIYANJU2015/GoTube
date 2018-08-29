/*
 * GoTube
 * Copyright (C) 2018  Ramon Mifsud
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

package com.tubeplayer.player.business.youtube.bean;

import android.util.Log;

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.gui.fragments.preferences.OthersPreferenceFragment;
import com.tube.playtube.BuildConfig;
import com.tube.playtube.R;

import java.util.Random;

/**
 * Represents a YouTube API key.
 */
public class YTubeAPIKey {

	/** User's YouTube API key which is inputted via the
	 * {@link OthersPreferenceFragment}.  Will be null if the user did not
	 * input a key. **/
	private String userAPIKey;
	private Random random = new Random();

	private static YTubeAPIKey youTubeAPIKey = null;
	private static final String TAG = YTubeAPIKey.class.getSimpleName();


	/**
	 * Constructor.  Will retrieve user's YouTube API key if set.
	 */
	private YTubeAPIKey() {
		userAPIKey = getUserApiKey();
	}


	/**
	 * @return An instance of {@link YTubeAPIKey}.
	 */
	public static YTubeAPIKey get() {
		if (youTubeAPIKey == null) {
			youTubeAPIKey = new YTubeAPIKey();
		}

		return youTubeAPIKey;
	}

	public static final String[] YOUTUBE_API_KEYS = {
			"AIzaSyCLPqZsaLpfXJV7ZbDJTj-DDRD2pfzx8f0",
			"AIzaSyAtsBI7zz7U55Wk-01E-hFDLwl9Z6C8Kvs",
			"AIzaSyBn7hE7kjS3FDIMufjNiLWNlIhROBV2H18",
			"AIzaSyC2fa-e1AK0UXWG11DKAgYP0UEzUyx6cvY",
			"AIzaSyBNfmkNwhCRokZk1QNnQzvHEAcn5ug1jnA",
			"AIzaSyDQ5Nn51uNJO82JtBwkue-Q-lNZpHqt72U",
			"AIzaSyCZkpS6ll40jcuTvuH9ECz60e3_ZudOFpM",
			"AIzaSyB4ujp9i4-cHgowocYvuQ7_uLgDyIPFxl8",
			"AIzaSyAC4quGEFZE56y5UH1YWzqDyMOE61oIHmw",
			"AIzaSyAjKGdTWQxnylXaLr1UqABT8B047qg4zHc"
	};



	/**
	 * @return Return YouTube API key.
	 */
	public String getYouTubeAPIKey() {
		String key;

//		if (isUserApiKeySet()) {
//			// if the user has not set his own API key, then use the default GoTube key
//			key = userAPIKey;
//		} else {
			// else we are going to choose one of the defaults keys at random
			int i = random.nextInt(YOUTUBE_API_KEYS.length);
			key = YOUTUBE_API_KEYS[i];
//		}

		Log.d(TAG, "Key = " + key);
		return key;
	}


	/**
	 * @return True if the user has set his own YouTube API key (via the
	 * {@link OthersPreferenceFragment}); false otherwise.
	 */
	public boolean isUserApiKeySet() {
		return (userAPIKey != null);
	}


	/**
	 * @return User's YouTube API key (if set).  If the user did not set a key, then it will return null.
	 */
	private String getUserApiKey() {
		String userApiKey = TubeApp.getPreferenceManager().getString(TubeApp.getStr(R.string.pref_youtube_api_key), null);

		if (userApiKey != null) {
			userApiKey = userApiKey.trim();

			if (userApiKey.isEmpty())
				userApiKey = null;
		}

		return userApiKey;
	}

}
