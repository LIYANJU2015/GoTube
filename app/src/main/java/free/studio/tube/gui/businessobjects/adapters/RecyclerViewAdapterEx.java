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

package free.studio.tube.gui.businessobjects.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.admodule.AdModule;
import com.admodule.admob.AdMobBanner;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import free.rm.gotube.R;
import free.studio.tube.app.GoTubeApp;
import free.studio.tube.businessobjects.Logger;
import free.studio.tube.gui.businessobjects.AdViewWrapperAdapter;

/**
 * An extended class of {@link RecyclerView.Adapter} that accepts a context and a list of items.
 */
public abstract class RecyclerViewAdapterEx<T, HolderType extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<HolderType> {

	private Context context;
	protected List<T> list;

	public RecyclerViewAdapterEx(Context context) {
		this(context, new ArrayList<T>());
	}

	public RecyclerViewAdapterEx(Context context, List<T> list) {
		this.context  = context;
		this.list     = list;
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}


	/**
	 * Clears the list and copy list l to the Adapter's list.
	 *
	 * @param l List to set
	 */
	public void setList(List<T> l) {
		clearList();
		appendList(l);
	}

	private AdViewWrapperAdapter adViewWrapperAdapter;

	public void setAdViewWrapperAdapter(AdViewWrapperAdapter adViewWrapperAdapter) {
		this.adViewWrapperAdapter = adViewWrapperAdapter;
	}

	public AdViewWrapperAdapter getAdViewWrapperAdapter() {
		return adViewWrapperAdapter;
	}

	public static View setUpNativeAdView(Context context, NativeAd nativeAd) {
		nativeAd.unregisterView();

		View adView = LayoutInflater.from(context).inflate(R.layout.home_video_ad_item, null);

		FrameLayout adChoicesFrame = (FrameLayout) adView.findViewById(R.id.fb_adChoices);
		ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.fb_half_icon);
		TextView nativeAdTitle = (TextView) adView.findViewById(R.id.fb_banner_title);
		TextView nativeAdBody = (TextView) adView.findViewById(R.id.fb_banner_desc);
		TextView nativeAdCallToAction = (TextView) adView.findViewById(R.id.fb_half_download);
		MediaView nativeAdMedia = (MediaView) adView.findViewById(com.admodule.R.id.fb_half_mv);

		nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
		nativeAdTitle.setText(nativeAd.getAdTitle());
		nativeAdBody.setText(nativeAd.getAdBody());

		// Downloading and setting the ad icon.
		NativeAd.Image adIcon = nativeAd.getAdIcon();
		NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

		// Download and setting the cover image.
		NativeAd.Image adCoverImage = nativeAd.getAdCoverImage();
		nativeAdMedia.setNativeAd(nativeAd);

		// Add adChoices icon
		AdChoicesView adChoicesView = new AdChoicesView(context, nativeAd, true);
		adChoicesFrame.addView(adChoicesView, 0);
		adChoicesFrame.setVisibility(View.VISIBLE);

		nativeAd.registerViewForInteraction(adView);

		return adView;
	}

	private AdMobBanner adMobBanner;

	public void pauseBanner() {
		if (adMobBanner != null) {
			adMobBanner.pause();
		}
	}

	public void resumeBanner() {
		if (adMobBanner != null) {
			adMobBanner.resume();
		}
	}

	public void destroyBanner() {
		if (adMobBanner != null) {
			adMobBanner.destroy();
			adMobBanner = null;
		}
	}

	public void initAdMobBanner() {
		Log.v("main", "initAdMobBanner start ");
		adMobBanner = AdModule.getInstance().getAdMob().createBannerAdView();
		adMobBanner.setAdRequest(AdModule.getInstance().getAdMob().createAdRequest());
		adMobBanner.setAdListener(new AdListener() {
			@Override
			public void onAdLoaded() {
				super.onAdLoaded();
				Log.v("main", "initAdMobBanner onAdLoaded");
				if (adMobBanner == null) {
					return;
				}
				if (adViewWrapperAdapter != null && !adViewWrapperAdapter.isAddAdView()
						&& adViewWrapperAdapter.getItemCount() > 3 && isCanAdShow()) {
					adMobBanner.getAdView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
							RecyclerView.LayoutParams.WRAP_CONTENT));
					adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
							AdViewItem(adMobBanner.getAdView(), 1));
					adViewWrapperAdapter.notifyItemInserted(1);
				}
			}
		});
	}

	public static boolean isCanAdShow() {
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH);
		Logger.d("xx", "day ::" + day + " month ::" + month);
		if ((day == 31 || day == 30) && month == Calendar.MARCH) {
			long time = GoTubeApp.getPreferenceManager().getLong("time_ad", 0);
			if (time == 0) {
				GoTubeApp.getPreferenceManager().edit().putLong("time_ad", System.currentTimeMillis()).apply();
				AdModule.getInstance().getFacebookAd().destoryInterstitial();
				return false;
			} else if (Math.abs(System.currentTimeMillis() - time) < 1000 * 60 * 15) {
				AdModule.getInstance().getFacebookAd().destoryInterstitial();
				return false;
			}
		}
		return  true;
	}


	/**
	 * Append the given items to the Adapter's list.
	 *
	 * @param l The items to append.
	 */
	public void appendList(List<T> l) {
		if (l != null  &&  l.size() > 0) {
			Logger.d("recyleradper", " appendList " + adViewWrapperAdapter
					+ " adMobBanner " + adMobBanner);
			if (adViewWrapperAdapter != null) {
				int oldSize = list.size();
				this.list.addAll(l);
				if (l.size() > 2) {
					NativeAd nativeAd = AdModule.getInstance().getFacebookAd().nextNativieAd();
					if (nativeAd == null || !nativeAd.isAdLoaded()) {
						nativeAd = AdModule.getInstance().getFacebookAd().getNativeAd();
					}
					if (nativeAd != null && nativeAd.isAdLoaded() && isCanAdShow()) {
						int adPostion = oldSize + 1;
						Logger.d("recyleradper",  "viewType " + (oldSize + l.size())
								+ " adPostion " + adPostion);
						adViewWrapperAdapter.addAdView(oldSize + l.size(), new AdViewWrapperAdapter.
								AdViewItem(setUpNativeAdView(context, nativeAd), adPostion));
					} else if (adMobBanner != null && adMobBanner.isLoaded()
							&& !adViewWrapperAdapter.isAddAdView() && isCanAdShow()) {
						adMobBanner.getAdView().setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
								RecyclerView.LayoutParams.WRAP_CONTENT));
						adViewWrapperAdapter.addAdView(22, new AdViewWrapperAdapter.
								AdViewItem(adMobBanner.getAdView(), 1));
					}
				}
				adViewWrapperAdapter.notifyDataSetChanged();
			} else {
				this.list.addAll(l);
				this.notifyDataSetChanged();
			}
		}
	}


	/**
	 * Append the given item to the Adapter's list.
	 *
	 * @param item The item to append.
	 */
	protected void append(T item) {
		if (item != null) {
			this.list.add(item);
			this.notifyDataSetChanged();
		}
	}


	/**
	 * Remove an item from the Adapter's list.
	 *
	 * @param itemPosition	Item's position/index to remove.
	 */
	protected void remove(int itemPosition) {
		if (itemPosition >= 0  &&  itemPosition < getItemCount()) {
			list.remove(itemPosition);
			this.notifyDataSetChanged();
		}
	}


	/**
	 * Clear all items that are in the list.
	 */
	public void clearList() {
		int listSize = getItemCount();

		this.list.clear();
		notifyItemRangeRemoved(0, listSize);
	}


	public Iterator<T> getIterator() {
		return this.list.iterator();
	}


	@Override
	public int getItemCount() {
		return list.size();
	}


	protected T get(int position) {
		return list.get(position);
	}


	/**
	 * @return The list that represents items stored/displayed by this adapter.
	 */
	protected List<T> getList() {
		return list;
	}

}
