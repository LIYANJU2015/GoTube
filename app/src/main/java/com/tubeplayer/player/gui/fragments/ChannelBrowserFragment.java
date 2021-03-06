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

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.tubeplayer.player.gui.businessobjects.SubscribeButton;
import com.tube.playtube.R;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.business.youtube.Tasks.GetYouTubeChannelInfoTask;
import com.tubeplayer.player.gui.businessobjects.adapters.SubsAdapter;
import com.tubeplayer.player.gui.businessobjects.fragments.FragmentEx;
import com.tubeplayer.player.gui.businessobjects.fragments.TabFragment;

/**
 * A Fragment that displays information about a channel.
 *
 * This fragment is made up of two other fragments:
 * <ul>
 *     <li>{@link ChannelVideosFragment}</li>
 *     <li>{@link ChannelPlaylistsFragment}.</li>
 * </ul>
 */
public class ChannelBrowserFragment extends FragmentEx {

	private YTubeChannel channel = null;

	public static final String FRAGMENT_CHANNEL_VIDEOS = "TubeChannelBrowserFragment.FRAGMENT_CHANNEL_VIDEOS";
	public static final String FRAGMENT_CHANNEL_PLAYLISTS = "TubeChannelBrowserFragment.FRAGMENT_CHANNEL_PLAYLISTS";

	private ImageView 			channelThumbnailImage = null;
	private ImageView			channelBannerImage = null;
	private TextView			channelSubscribersTextView = null;
	private SubscribeButton channelSubscribeButton = null;
	private GetChannelInfoTask	task = null;

	public static final String CHANNEL_OBJ = "TubeChannelBrowserFragment.ChannelObj";
	public static final String CHANNEL_ID  = "TubeChannelBrowserFragment.ChannelID";

	// The fragments that will be displayed
	private ChannelVideosFragment       channelVideosFragment;
	private ChannelPlaylistsFragment    channelPlaylistsFragment;
	private ChannelAboutFragment        channelAboutFragment;

	/** List of fragments that will be displayed as tabs. */
	private List<TabFragment> channelBrowserFragmentList = new ArrayList<>();

	private ChannelPagerAdapter channelPagerAdapter;
	private ViewPager viewPager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final String channelId;
		final Bundle bundle = getArguments();

		if (savedInstanceState != null) {
			channelVideosFragment = (ChannelVideosFragment)getChildFragmentManager().getFragment(savedInstanceState, FRAGMENT_CHANNEL_VIDEOS);
			channelPlaylistsFragment = (ChannelPlaylistsFragment)getChildFragmentManager().getFragment(savedInstanceState, FRAGMENT_CHANNEL_PLAYLISTS);
		}
		// we need to create a YouTubeChannel object:  this can be done by either:
		//   (1) the YouTubeChannel object is passed to this Fragment
		//   (2) passing the channel ID... a task is then created to create a YouTubeChannel
		//       instance using the given channel ID
		if (bundle != null  &&  bundle.getSerializable(CHANNEL_OBJ) != null) {
			this.channel = (YTubeChannel) bundle.getSerializable(CHANNEL_OBJ);
			channelId = channel.getId();
		} else {
			channelId = bundle.getString(CHANNEL_ID);
		}

		// inflate the layout for this fragment
		View fragment = inflater.inflate(R.layout.fragment_channel_browser, container, false);
		viewPager = fragment.findViewById(R.id.pager);

		if (Build.VERSION.SDK_INT < 21) {
			fragment.findViewById(R.id.channel_toolbar_shadow).setVisibility(View.VISIBLE);
		}

		TabLayout tabLayout = fragment.findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				viewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});

		viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				channelBrowserFragmentList.get(position).onFragmentSelected();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

		channelBannerImage = fragment.findViewById(R.id.channel_banner_image_view);
		channelThumbnailImage = fragment.findViewById(R.id.channel_thumbnail_image_view);
		channelSubscribersTextView = fragment.findViewById(R.id.channel_subs_text_view);
		channelSubscribeButton = fragment.findViewById(R.id.channel_subscribe_button);
		channelSubscribeButton.setFetchChannelVideosOnSubscribe(false);
		if(channel != null)
				channelSubscribeButton.setChannel(channel);
		channelSubscribeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// If we're subscribing to the channel, save the list of videos we have into the channel (to be stored in the database by SubscribeToChannelTask)
				if (channel != null && !channel.isUserSubscribed()) {
					Iterator<YTubeVideo> iterator = channelVideosFragment.getVideoGridAdapter().getIterator();
					while (iterator.hasNext()) {
						channel.addYouTubeVideo(iterator.next());
					}
				}
			}
		});

		if (channel == null) {
			if (task == null) {
				task = new GetChannelInfoTask();
				task.execute(channelId);
			}
		} else {
			try {
				initViews();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

		FacebookReport.logSentChannelBrowser();

		return fragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		try {
			if (channelVideosFragment != null) {
				getChildFragmentManager().putFragment(outState, FRAGMENT_CHANNEL_VIDEOS, channelVideosFragment);
			}
			if (channelPlaylistsFragment != null) {
				getChildFragmentManager().putFragment(outState, FRAGMENT_CHANNEL_PLAYLISTS, channelPlaylistsFragment);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise views that are related to {@link #channel}.
	 */
	private void initViews() {
		if (channel != null) {
			channelPagerAdapter = new ChannelPagerAdapter(getChildFragmentManager());
			viewPager.setOffscreenPageLimit(2);
			viewPager.setAdapter(channelPagerAdapter);


			Glide.with(getActivity())
					.load(channel.getThumbnailNormalUrl())
					.into(channelThumbnailImage);

			Glide.with(getActivity())
					.load(channel.getBannerUrl())
					.apply(new RequestOptions().placeholder(R.drawable.channel_banner))
					.into(channelBannerImage);

			channelSubscribersTextView.setText(channel.getTotalSubscribers());

			ActionBar actionBar = getSupportActionBar();
			if (actionBar != null) {
				actionBar.setTitle(channel.getTitle());
			}

			// if the user has subscribed to this channel, then change the state of the
			// subscribe button
			if (channel.isUserSubscribed()) {
				channelSubscribeButton.setUnsubscribeState();
			} else {
				channelSubscribeButton.setSubscribeState();
			}

			if (channel.isUserSubscribed()) {
				// the user is visiting the channel, so we need to update the last visit time
				channel.updateLastVisitTime();

				if (channel.newVideosSinceLastVisit()) {
					// since we are visiting the channel, then we need to disable the new videos notification
					channel.setNewVideosSinceLastVisit(false);
					// since this.channel and SubsAdapter's channel are different instances, then we
					// need to modify both of them [different because of bundle.getSerializable(channel)]
					SubsAdapter.get(getActivity()).changeChannelNewVideosStatus(channel.getId(), false);
				}
			}
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * A task that given a channel ID it will try to initialize and return {@link YTubeChannel}.
	 */
	private class GetChannelInfoTask extends GetYouTubeChannelInfoTask {

		GetChannelInfoTask() {
			super(null);
		}

		@Override
		protected void onPostExecute(YTubeChannel youTubeChannel) {
			// In the event this fragment is passed a channel id and not a channel object, set the
			// channel the subscribe button is for since there wasn't a channel object to set when
			// the button was created.
			channel = youTubeChannel;
			try {
				initViews();
				channelSubscribeButton.setChannel(youTubeChannel);
				if (channelVideosFragment != null) {
					channelVideosFragment.setYouTubeChannel(youTubeChannel);
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}


	private class ChannelPagerAdapter extends FragmentPagerAdapter {
		public ChannelPagerAdapter(FragmentManager fm) {
			super(fm);

			// Initialize fragments
			if (channelVideosFragment == null)
				channelVideosFragment = new ChannelVideosFragment();

			if (channelPlaylistsFragment == null)
				channelPlaylistsFragment = new ChannelPlaylistsFragment();

			if (channelAboutFragment == null)
				channelAboutFragment = new ChannelAboutFragment();

			Bundle bundle = new Bundle();
			bundle.putSerializable(CHANNEL_OBJ, channel);

			channelVideosFragment.setArguments(bundle);
			channelPlaylistsFragment.setArguments(bundle);
			channelAboutFragment.setArguments(bundle);

			channelBrowserFragmentList.clear();
			channelBrowserFragmentList.add(channelVideosFragment);
			channelBrowserFragmentList.add(channelPlaylistsFragment);
			channelBrowserFragmentList.add(channelAboutFragment);
		}

		@Override
		public Fragment getItem(int position) {
			return channelBrowserFragmentList.get(position);
		}

		@Override
		public int getCount() {
			return channelBrowserFragmentList.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return channelBrowserFragmentList.get(position).getFragmentName();
		}
	}

	/**
	 * Return the Channel Playlists Fragment. This is needed so that the fragment can have a reference to MainActivity
	 * @return {@link ChannelPlaylistsFragment}
	 */
	public ChannelPlaylistsFragment getChannelPlaylistsFragment() {
		if(channelPlaylistsFragment == null)
			channelPlaylistsFragment = new ChannelPlaylistsFragment();
		return channelPlaylistsFragment;
	}
}
