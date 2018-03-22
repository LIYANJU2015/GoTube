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

package free.studio.tube.gui.activities;

import java.util.List;

import free.rm.GoTube.R;
import free.studio.tube.gui.businessobjects.preferences.ActionBarPreferenceActivity;
import free.studio.tube.gui.fragments.preferences.AboutPreferenceFragment;
import free.studio.tube.gui.fragments.preferences.BackupPreferenceFragment;
import free.studio.tube.gui.fragments.preferences.LanguagesPreferenceFragment;
import free.studio.tube.gui.fragments.preferences.OthersPreferenceFragment;
import free.studio.tube.gui.fragments.preferences.VideoPlayerPreferenceFragment;

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
