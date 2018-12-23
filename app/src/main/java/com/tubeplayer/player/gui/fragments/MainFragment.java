package com.tubeplayer.player.gui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.mintergalsdk.AppNextSDK;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.Logger;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.business.db.BookmarksDb;
import com.tubeplayer.player.gui.businessobjects.fragments.FragmentEx;

public class MainFragment extends FragmentEx implements BottomNavigationView.OnNavigationItemSelectedListener{
	private BottomNavigationView tabLayout = null;

	/** List of fragments that will be displayed as tabs. */
	private List<VideosGridFragment>	videoGridFragmentsList = new ArrayList<>();
	private FeaturedVideosFragment		featuredVideosFragment = null;
	private MostPopularVideosFragment	mostPopularVideosFragment = null;
	private SubscriptionsFeedFragment   subscriptionsFeedFragment = null;
	private BookmarksFragment			bookmarksFragment = null;

	// Constants for saving the state of this Fragment's child Fragments
	public static final String FEATURED_VIDEOS_FRAGMENT = "MainFragment.TubefeaturedVideosFragment";
	public static final String MOST_POPULAR_VIDEOS_FRAGMENT = "MainFragment.TubemostPopularVideosFragment";
	public static final String SUBSCRIPTIONS_FEED_FRAGMENT = "MainFragment.TubesubscriptionsFeedFragment";
	public static final String BOOKMARKS_FRAGMENT = "MainFragment.TubebookmarksFragment";
	public static final String DOWNLOADED_VIDEOS_FRAGMENT = "MainFragment.TubedownloadedVideosFragment";

	private VideosPagerAdapter			videosPagerAdapter = null;
	private ViewPager					viewPager;

	public static final String SHOULD_SELECTED_FEED_TAB = "MainFragment.TUBESHOULD_SELECTED_FEED_TAB";

	private Activity activity;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		activity = getActivity();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			featuredVideosFragment = (FeaturedVideosFragment) getChildFragmentManager().getFragment(savedInstanceState, FEATURED_VIDEOS_FRAGMENT);
			mostPopularVideosFragment = (MostPopularVideosFragment) getChildFragmentManager().getFragment(savedInstanceState, MOST_POPULAR_VIDEOS_FRAGMENT);
			subscriptionsFeedFragment = (SubscriptionsFeedFragment)getChildFragmentManager().getFragment(savedInstanceState, SUBSCRIPTIONS_FEED_FRAGMENT);
			bookmarksFragment = (BookmarksFragment) getChildFragmentManager().getFragment(savedInstanceState, BOOKMARKS_FRAGMENT);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		viewPager.setCurrentItem(item.getOrder());
		return true;
	}

	private int mSelectPosition = 0;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		videosPagerAdapter = new VideosPagerAdapter(getChildFragmentManager());
		viewPager = view.findViewById(R.id.pager);
		viewPager.setOffscreenPageLimit(videoGridFragmentsList.size() - 1);
		viewPager.setAdapter(videosPagerAdapter);
		viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
			@Override
			public void onPageSelected(int position) {
				int menuId = tabLayout.getMenu().getItem(position).getItemId();
				tabLayout.setSelectedItemId(menuId);
				mSelectPosition = position;
			}
		});

		tabLayout = view.findViewById(R.id.main_navigation);
		tabLayout.setOnNavigationItemSelectedListener(this);
		disableShiftMode(tabLayout);
		// select the default tab:  the default tab is defined by the user through the Preferences
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// If the app is being opened via the Notification that new videos from Subscribed channels have been found, select the Subscriptions Feed Fragment
		Bundle args = getArguments();
		if(args != null && args.getBoolean(SHOULD_SELECTED_FEED_TAB, false)) {
			viewPager.setCurrentItem(videoGridFragmentsList.indexOf(subscriptionsFeedFragment));
		} else {
			viewPager.setCurrentItem(Integer.parseInt(sp.getString(getString(R.string.pref_key_default_tab), "0")));
		}

		// Set the current viewpager fragment as selected, as when the Activity is recreated, the Fragment
		// won't know that it's selected. When the Feeds fragment is the default tab, this will prevent the
		// refresh dialog from showing when an automatic refresh happens.
		videoGridFragmentsList.get(viewPager.getCurrentItem()).onFragmentSelected();

		RelativeLayout adRelativew = view.findViewById(R.id.in_ad_relative);
		if (SuperVersions.isShowAd()) {
			View adView = AppNextSDK.createBannerView();
			if (adView != null) {
				adRelativew.removeAllViews();
				adRelativew.addView(adView);
			}
		} else {
			adRelativew.setVisibility(View.GONE);
		}
		return view;
	}

	@SuppressLint("RestrictedApi")
	public static void disableShiftMode(BottomNavigationView view) {
		ViewGroup menuView = (ViewGroup) view.getChildAt(0);
		try {
			Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
			shiftingMode.setAccessible(true);
			shiftingMode.setBoolean(menuView, false);
			shiftingMode.setAccessible(false);
			for (int i = 0; i < menuView.getChildCount(); i++) {
				BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
				item.setShiftingMode(false);
				item.setChecked(item.getItemData().isChecked());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	@Override
	public void onResume() {
		super.onResume();

		// when the MainFragment is resumed (e.g. after Preferences is minimized), inform the
		// current fragment that it is selected.
		if (videoGridFragmentsList != null  &&  tabLayout != null) {
			Logger.d(this, "MAINFRAGMENT RESUMED " + mSelectPosition);
			videoGridFragmentsList.get(mSelectPosition).onFragmentSelected();
		}
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Pass the event to ActionBarDrawerToggle, if it returns true, then it has handled the app
		// icon touch event

		// Handle your other action bar items...
		return super.onOptionsItemSelected(item);
	}

	private class VideosPagerAdapter extends FragmentPagerAdapter {

		public VideosPagerAdapter(FragmentManager fm) {
			super(fm);

			// initialise fragments
			if (featuredVideosFragment == null)
				featuredVideosFragment = new FeaturedVideosFragment();

			if (mostPopularVideosFragment == null)
				mostPopularVideosFragment = new MostPopularVideosFragment();

			if (subscriptionsFeedFragment == null)
				subscriptionsFeedFragment = new SubscriptionsFeedFragment();

			if (bookmarksFragment == null) {
				bookmarksFragment = new BookmarksFragment();
				BookmarksDb.getBookmarksDb().addListener(bookmarksFragment);
			}

			// add fragments to list:  do NOT forget to ***UPDATE*** @string/default_tab and @string/default_tab_values
			videoGridFragmentsList.clear();
			videoGridFragmentsList.add(mostPopularVideosFragment);
			videoGridFragmentsList.add(featuredVideosFragment);

			videoGridFragmentsList.add(subscriptionsFeedFragment);
			videoGridFragmentsList.add(bookmarksFragment);
		}

		@Override
		public int getCount() {
			return videoGridFragmentsList.size();
		}

		@Override
		public Fragment getItem(int position) {
			return videoGridFragmentsList.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return videoGridFragmentsList.get(position).getFragmentName();
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if(featuredVideosFragment != null && featuredVideosFragment.isAdded())
			getChildFragmentManager().putFragment(outState, FEATURED_VIDEOS_FRAGMENT, featuredVideosFragment);
		if(mostPopularVideosFragment != null && mostPopularVideosFragment.isAdded())
			getChildFragmentManager().putFragment(outState, MOST_POPULAR_VIDEOS_FRAGMENT, mostPopularVideosFragment);
		if(subscriptionsFeedFragment != null && subscriptionsFeedFragment.isAdded())
			getChildFragmentManager().putFragment(outState, SUBSCRIPTIONS_FEED_FRAGMENT, subscriptionsFeedFragment);
		if(bookmarksFragment != null && bookmarksFragment.isAdded())
			getChildFragmentManager().putFragment(outState, BOOKMARKS_FRAGMENT, bookmarksFragment);

		super.onSaveInstanceState(outState);
	}
}
