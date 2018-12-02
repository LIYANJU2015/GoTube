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

package com.tubeplayer.player.gui.businessobjects.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.mintergalsdk.MintergalSDK;
import com.mintergalsdk.NativeView;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.Logger;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.VideoCategory;
import com.tubeplayer.player.gui.businessobjects.AdViewWrapperAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

	public VideoCategory getVideoCategory() {
		return VideoCategory.FEATURED;
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

	/**
	 * Append the given items to the Adapter's list.
	 *
	 * @param l The items to append.
	 */
	public void appendList(List<T> l) {
		if (l != null  &&  l.size() > 0) {
			if (adViewWrapperAdapter != null) {
				int oldSize = list.size();
				this.list.addAll(l);
				if (l.size() > 2 && SuperVersions.SuperVersionHandler.isShowAd()) {
					int adPostion = oldSize + 1;
					Logger.d("recyleradperex", "viewType " + (oldSize + l.size())
							+ " adPostion " + adPostion);
					if (getVideoCategory() == VideoCategory.DOWNLOADED_VIDEOS
							|| getVideoCategory() == VideoCategory.SEARCH_QUERY) {
						View view = MintergalSDK.getNABannerView(TubeApp.NATIVE_AD_ID, TubeApp.callBack);
						if (view != null) {
							adViewWrapperAdapter.addAdView(oldSize + l.size(), new AdViewWrapperAdapter.
									AdViewItem(view, adPostion));
						}
					} else {
						View view = MintergalSDK.getNativeView(TubeApp.NATIVE_AD_ID, TubeApp.callBack);
						if (view != null) {
							adViewWrapperAdapter.addAdView(oldSize + l.size(), new AdViewWrapperAdapter.
									AdViewItem(view, adPostion));
						}
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
