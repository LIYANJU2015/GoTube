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

import java.util.ArrayList;
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
			"AIzaSyBn7hE7kjS3FDIMufjNiLWNlIhROBV2H18",
			"AIzaSyD1uZpVIUkDqWmvjcQ4y3qTGQ7i5N_th_M",
			"AIzaSyC2fa-e1AK0UXWG11DKAgYP0UEzUyx6cvY",
			"AIzaSyBNfmkNwhCRokZk1QNnQzvHEAcn5ug1jnA",
			"AIzaSyC757n1OQBHIjUXI8QzWbMo6Db_l0cGiEA",
			"AIzaSyCi5A54twV5YtEvd9ZByU-DSX6oArQOTyM",
			"AIzaSyCBbdoHmR9x212p6SGiOYBF6-PNXnvo1So",
			"AIzaSyCZkpS6ll40jcuTvuH9ECz60e3_ZudOFpM",
			"AIzaSyB4ujp9i4-cHgowocYvuQ7_uLgDyIPFxl8",
	};

	public static final String[] YOUTUBE_API_KEYS2 = {
			"AIzaSyD9xJnLrVcy2OopbtlSlKAmpukvXjxc44E",
			"AIzaSyBW9XHx8HkQwWV80q9T31jgqUTBPCYQS4A",
			"AIzaSyD7pbE5_g037sQbZc0z2W61qNJVgNVP3z4",
			"AIzaSyAhOzqBAzknpPLAvEn0bdE8bJFAyOmbAkc",
			"AIzaSyAg9FgFFvfVAqh8zLK7boj3l3Ie0u_S-J4",
			"AIzaSyCe525PZxX7vWbCkhhzSWiT3iNuzGTtEJI",
			"AIzaSyBPqO18zJ1jgi9PhfZK3BEK8uD8QVOzD_k",
			"AIzaSyAtsBI7zz7U55Wk-01E-hFDLwl9Z6C8Kvs",
			"AIzaSyAC4quGEFZE56y5UH1YWzqDyMOE61oIHmw",
	};

	public static final String[] YOUTUBE_API_KEYS3 = {
			"AIzaSyBYTI70eBiMauIuz1qJ0mD9Rputl3oKPuE",
			"AIzaSyBzm4yb2fkCT2uOAo5ETIerfQy5WWk5qsk",
			"AIzaSyCVgz2gIvm1mSMIOeWAPXzAa5MTmd2bn8U",
			"AIzaSyBqLrFwFuI6jcRM7TlFtOvl41jmvyM4220",
			"AIzaSyBqoRhH750jR0gV2xe3k48bxra5CBE9-RQ",
			"AIzaSyBGT6sbFzyUnhS4nctVY64QeL86uR0RYqo",
			"AIzaSyBnZWReoUeXJ2GMwHq6SYi51YUeXbkbyzM",
			"AIzaSyAjKGdTWQxnylXaLr1UqABT8B047qg4zHc",
			"AIzaSyDQ5Nn51uNJO82JtBwkue-Q-lNZpHqt72U",
	};

	public static final String[] YOUTUBE_API_KEYS4 = {
			"AIzaSyB5IsxA_-IjXbnBoZ7zx6fSAAPgpCydIxk",
			"AIzaSyCP4LYHGAIfCuKki2P53-8_RWyRJWJE6O0",
			"AIzaSyBGMS9KNd0F4qlrA7czJEnTbF6077VeU0E",
			"AIzaSyByPNGqgQb1WuAVNAma9T-_xwR-3kFkqzg",
			"AIzaSyBRLbG43XBP9UYuqRNQKLpTakOIb9pIsMQ",
			"AIzaSyCb-84HDKi4hFgjs7G7M0skOOgND7okNb8",
			"AIzaSyBXVRuj0UBcAkWWwZUYiJpDEcLCmB8iDeM",
			"AIzaSyBxeCTQ9eNe5gtkhrVPg_YNZxuOS9kkNN0",
			"AIzaSyD2aLOfH18P889oz5OUi3Eckb3qGzJ9lvU",
	};

	private static ArrayList<String[]> SKEYLIST = new ArrayList<>();
	private static ArrayList<String[]> ALL_SKEYLIST = new ArrayList<>();

	static {
		SKEYLIST.add(YOUTUBE_API_KEYS3);
		SKEYLIST.add(YOUTUBE_API_KEYS2);
		SKEYLIST.add(YOUTUBE_API_KEYS);

		ALL_SKEYLIST.add(SKEYLIST.toArray(new String[SKEYLIST.size()]));
		ALL_SKEYLIST.add(YOUTUBE_API_KEYS4);
	}




	/**
	 * @return Return YouTube API key.
	 */
	public String getYouTubeAPIKey() {
		String key;
		String KEY[] = null;
		try {
			KEY = ALL_SKEYLIST.get(random.nextInt(2));
			int i = random.nextInt(KEY.length);
			key = KEY[i];
		} catch (Throwable e) {
			e.printStackTrace();
			key = YOUTUBE_API_KEYS4[0];
		}
//		if (BuildConfig.DEBUG) {
//			Log.d(TAG, ">>>Key = " + key + " KEY " + KEY);
//			key = "AIzaSyBXVRuj0UBcAkWWwZUYiJpDEcLCmB8iDeM";
//		}
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
