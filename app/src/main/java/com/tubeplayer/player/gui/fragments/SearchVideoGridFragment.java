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

package com.tubeplayer.player.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adlibs.InMobiHelper;
import com.mintergalsdk.MintergalSDK;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.business.VideoCategory;

/**
 * Fragment that will hold a list of videos corresponding to the user's query.
 */
public class SearchVideoGridFragment extends VideosGridFragment {

	/** User's search query string. */
	private String  searchQuery = "";

	public static final String QUERY = "TubeSearchVideoGridFragment.Query";


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLayoutResource(R.layout.fragment_search);

		// set the user's search query
		searchQuery = getArguments().getString(QUERY);

		if (SuperVersions.isSpecial()) {
			InMobiHelper.init(TubeApp.getContext(), Utils.ACCOUNT_ID);
			InMobiHelper.createInterstitial(Utils.CHAPING_INMOBI);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (SuperVersions.isSpecial()) {
			if (!InMobiHelper.showInterstitial()) {
				MintergalSDK.showNativeFullScreen(TubeApp.NATIVE_AD_ID, TubeApp.callBack);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// inflate the layout for this fragment
		View view = super.onCreateView(inflater, container, savedInstanceState);
		return view;
	}


	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.SEARCH_QUERY;
	}


	@Override
	protected String getSearchString() {
		return searchQuery;
	}


	@Override
	public String getFragmentName() {
		return null;
	}

}
