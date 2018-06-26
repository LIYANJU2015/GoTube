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

package com.tubeplayer.player.gui.activities;

import java.util.List;

import com.tubeplayer.player.gui.businessobjects.preferences.ActionBarPreferenceActivity;
import com.tubeplayer.player.gui.fragments.preferences.AboutPreferenceFragment;
import com.tubeplayer.player.gui.fragments.preferences.LanguagesPreferenceFragment;
import com.tubeplayer.player.gui.fragments.preferences.OthersPreferenceFragment;
import com.tubeplayer.player.gui.fragments.preferences.VideoPlayerPreferenceFragment;
import com.tube.playtube.R;
import com.tubeplayer.player.gui.fragments.preferences.BackupPreferenceFragment;

/**
 * The preferences activity allows the user to change the settings of this app.
 */
public class PreferencesActivity extends ActionBarPreferenceActivity {

	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return (fragmentName.equals(LanguagesPreferenceFragment.class.getName())
			|| fragmentName.equals(VideoPlayerPreferenceFragment.class.getName())
			|| fragmentName.equals(BackupPreferenceFragment.class.getName())
			|| fragmentName.equals(OthersPreferenceFragment.class.getName())
			|| fragmentName.equals(AboutPreferenceFragment.class.getName()));
	}

}
