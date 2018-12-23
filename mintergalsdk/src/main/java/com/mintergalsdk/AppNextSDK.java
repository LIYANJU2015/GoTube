package com.mintergalsdk;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.appnext.ads.interstitial.Interstitial;
import com.appnext.banners.BannerAdRequest;
import com.appnext.banners.BannerListener;
import com.appnext.banners.BannerSize;
import com.appnext.banners.BannerView;
import com.appnext.core.AppnextError;
import com.appnext.core.callbacks.OnAdClosed;
import com.appnext.core.callbacks.OnAdError;
import com.appnext.core.callbacks.OnAdLoaded;

import java.util.ArrayList;

/**
 * Created by liyanju on 2018/12/22.
 */

public class AppNextSDK {

    public static final String TAG = "AppNextSDK";

    private static ArrayList<Interstitial> interstitialList = new ArrayList<>();
    private static int interstitialCount = 0;

    private static Context sContext;

    private static String sInterstitialId;
    private static String sBannerId;

    public static void init(Context context, String interstitialId, String bannerId) {
        sContext = context;
        sInterstitialId = interstitialId;
        sBannerId = bannerId;
    }

    public static synchronized void showInterstitial() {
        try {
            Log.e(TAG, "showInterstitial size::" + interstitialList.size());
            if (interstitialList.size() > 0) {
                Interstitial interstitial = interstitialList.remove(0);
                if (interstitial != null) {
                    interstitial.showAd();
                }
            } else {
                loadInterstitial();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static synchronized View createBannerView() {
        try {
            final BannerView bannerView = new BannerView(sContext);
            bannerView.setBannerSize(BannerSize.LARGE_BANNER);
            bannerView.setPlacementId(sBannerId);
            bannerView.setVisibility(View.GONE);
            bannerView.loadAd(new BannerAdRequest());
            bannerView.setBannerListener(new BannerListener() {
                @Override
                public void onError(AppnextError appnextError) {
                    super.onError(appnextError);
                    Log.e(TAG, "createBannerView onError" + appnextError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(String s) {
                    super.onAdLoaded(s);
                    bannerView.setVisibility(View.VISIBLE);
                }
            });
            return bannerView;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static synchronized void loadInterstitialInnter() {
        final Interstitial interstitial = new Interstitial(sContext, sInterstitialId);
        interstitial.setOnAdClosedCallback(new OnAdClosed() {
            @Override
            public void onAdClosed() {
                try {
                    if (interstitialList.indexOf(interstitial) != -1) {
                        interstitialList.remove(interstitial);
                        Log.e(TAG, " loadInterstitial onAdClosed remove size: "
                                + interstitialList.size());
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
        interstitial.setOnAdErrorCallback(new OnAdError() {
            @Override
            public void adError(String s) {
                Log.e(TAG, "loadInterstitial adError >>" + s);
            }
        });
        interstitial.setOnAdLoadedCallback(new OnAdLoaded() {
            @Override
            public void adLoaded(String s) {
                Log.e(TAG, "loadInterstitial adLoaded interstitialCount>>" + interstitialCount);
                if (interstitialCount < 5) {
                    loadInterstitialInnter();
                }

                interstitialList.add(interstitial);

                interstitialCount++;
            }
        });
        interstitial.loadAd();
    }

    public static synchronized void loadInterstitial() {
        try {
            interstitialList.clear();
            interstitialCount = 0;

            loadInterstitialInnter();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
