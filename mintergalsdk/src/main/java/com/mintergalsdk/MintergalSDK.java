package com.mintergalsdk;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mintegral.msdk.MIntegralConstans;
import com.mintegral.msdk.MIntegralSDK;
import com.mintegral.msdk.interstitialvideo.out.InterstitialVideoListener;
import com.mintegral.msdk.interstitialvideo.out.MTGInterstitialVideoHandler;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.Frame;
import com.mintegral.msdk.out.InterstitialListener;
import com.mintegral.msdk.out.MIntegralSDKFactory;
import com.mintegral.msdk.out.MTGInterstitialHandler;
import com.mintegral.msdk.out.MtgNativeHandler;
import com.mintegral.msdk.out.MtgWallHandler;
import com.mintegral.msdk.out.NativeListener;
import com.mintegral.msdk.videocommon.download.NetStateOnReceive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.mintegral.msdk.MIntegralConstans.NATIVE_VIDEO_SUPPORT;

/**
 * Created by liyanju on 2018/11/24.
 */

public class MintergalSDK {

    private static final String TAG = "MintergalSDK";

    private static Context sContext;

    private static NetStateOnReceive mNetStateOnReceive;

    private static final HashMap<String, Object> sProperties = new HashMap<>(8);

    private static volatile MTGInterstitialVideoHandler mMtgInterstitalVideoHandler;

    private static final int AD_NUM =30;

    public static void init(Context context, String appId, String appKey) {
        try {
            sContext = context;
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, String> map = sdk.getMTGConfigurationMap(appId, appKey);
            sdk.init(map, context);

            mNetStateOnReceive = new NetStateOnReceive();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            context.registerReceiver(mNetStateOnReceive, filter);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setDebug() {
        com.mintegral.msdk.MIntegralConstans.DEBUG = true;
    }

    public static void setAppwallConfig(String key, Object value) {
        if (sProperties != null) {
            sProperties.put(key, value);
        }
    }

    public static void setAppwallTabBgColor(int color) {
        setAppwallConfig(MIntegralConstans.PROPERTIES_WALL_TAB_BACKGROUND_ID, color);
        setAppwallConfig(MIntegralConstans.PROPERTIES_WALL_TITLE_BACKGROUND_COLOR, color);
    }

    public static void openAppwall(Context context, String unitId) {
        try {
            Map<String, Object> properties = MtgWallHandler.getWallProperties(unitId);
            if (sProperties.size() > 0) {
                properties.putAll(sProperties);
            }
            MtgWallHandler mtgHandler = new MtgWallHandler(properties, context);
            mtgHandler.startWall();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void preloadWall(String unitId) {
        try {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, Object> preloadMap = new HashMap<String, Object>();
            preloadMap.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE, MIntegralConstans.LAYOUT_APPWALL);
            preloadMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            sdk.preload(preloadMap);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void loadInterstitialVideo(Activity activity, String unitId) {
        loadInterstitialVideo(activity, unitId, null, null);
    }

    private static void loadInterstitialVideo(Activity activity, String unitId, final Runnable showRunnable,
                                              final Runnable faildRunnable) {
        try {
            mMtgInterstitalVideoHandler = new MTGInterstitialVideoHandler(activity, unitId);
            mMtgInterstitalVideoHandler.setInterstitialVideoListener(new InterstitialVideoListener() {
                @Override
                public void onLoadSuccess(String s) {
                    if (showRunnable != null) {
                        showRunnable.run();
                    }
                }

                @Override
                public void onVideoLoadSuccess(String s) {

                }

                @Override
                public void onVideoLoadFail(String s) {
                    Log.e(TAG, "onVideoLoadFail error " + s);
                    mMtgInterstitalVideoHandler = null;
                    if (faildRunnable != null) {
                        faildRunnable.run();
                    }
                }

                @Override
                public void onAdShow() {

                }

                @Override
                public void onAdClose(boolean b) {
                    mMtgInterstitalVideoHandler = null;
                }

                @Override
                public void onShowFail(String s) {
                    mMtgInterstitalVideoHandler = null;
                    if (faildRunnable != null) {
                        faildRunnable.run();
                    }
                }

                @Override
                public void onVideoAdClicked(String s) {

                }
            });
            mMtgInterstitalVideoHandler.load();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void showInterstitialVideo(Activity activity, String unitId,
                                             final Runnable faildRunnable) {
        try {
            if (activity.isFinishing()) {
                if (faildRunnable != null) {
                    faildRunnable.run();
                }
                return;
            }

            if (mMtgInterstitalVideoHandler != null) {
                mMtgInterstitalVideoHandler.show();
                mMtgInterstitalVideoHandler = null;
            } else {
                loadInterstitialVideo(activity, unitId, new Runnable() {
                    @Override
                    public void run() {
                        if (mMtgInterstitalVideoHandler != null) {
                            mMtgInterstitalVideoHandler.show();
                            mMtgInterstitalVideoHandler = null;
                        }
                    }
                }, faildRunnable);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void preLoadNativeAd(String unitId) {
        try {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, Object> preloadMap = new HashMap<>();
            preloadMap.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE, MIntegralConstans.LAYOUT_NATIVE);
            preloadMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            preloadMap.put(MIntegralConstans.PROPERTIES_AD_NUM, AD_NUM);
            preloadMap.put(MIntegralConstans.NATIVE_VIDEO_WIDTH, 720);
            preloadMap.put(MIntegralConstans.NATIVE_VIDEO_HEIGHT, 480);
            preloadMap.put(MIntegralConstans.NATIVE_VIDEO_SUPPORT, true);
            preloadMap.put(MIntegralConstans.PREIMAGE, true);
            sdk.preload(preloadMap);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static MtgNativeHandler sMtgNativeHandler;
    private static List<Campaign> sCurrentNativelist = new ArrayList<>();
    private static Iterator<Campaign> sCurrentNativeIterator;

    private static Campaign getCurrentNativelist(String unitId) {
        try {
            if (sCurrentNativelist.size() > 0 && sCurrentNativeIterator != null && sCurrentNativeIterator.hasNext()) {
                return sCurrentNativeIterator.next();
            }

            loadNativeAd(unitId);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

    public static View getNativeView(String unitId, NativeView.NativeCallBack callBack) {
        try {
            Campaign campaign = getCurrentNativelist(unitId);
            if (campaign != null) {
                return new NativeView(sContext, campaign, sMtgNativeHandler, callBack);
            } else {
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void preNativeFullScreen(String unitId) {
        try {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, Object> preloadMap = new HashMap<>();
            preloadMap.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE, MIntegralConstans.LAYOUT_NATIVE);
            preloadMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);

            List<NativeListener.Template> list = new ArrayList<NativeListener.Template>();
            list.add(new NativeListener.Template(MIntegralConstans.TEMPLATE_BIG_IMG, AD_NUM));
            preloadMap.put(MIntegralConstans.NATIVE_INFO, MtgNativeHandler.getTemplateString(list));
            sdk.preload(preloadMap);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static MtgNativeHandler sFullCreenMtgNativeHandler;

    private static List<Campaign> sFullScreenCampaign = new ArrayList<>();
    private static Iterator<Campaign> sFullScreenIterator;

    public static void showOnlyNativeFullScreen(String unitId, final NativeView.NativeCallBack callBack) {
        Campaign campaign = null;
        if (sFullScreenCampaign.size() > 0 && sFullScreenIterator != null) {
            if (sFullScreenIterator.hasNext()) {
                campaign = sFullScreenIterator.next();
                sFullScreenIterator.remove();
            }
        }
        if (campaign != null) {
            if (campaign.hashCode() %2 == 0) {
                FullScreenActivity.launch(sContext, campaign, callBack,
                        sFullCreenMtgNativeHandler);
            } else {
                NativeInterstitialActivity.launch(sContext, campaign, callBack,
                        sFullCreenMtgNativeHandler);
            }
        }
    }


    public static void showNativeFullScreen(String unitId, final NativeView.NativeCallBack callBack,
                                            Runnable faildRunnable) {
        try {
            Campaign campaign = null;
            if (sFullScreenCampaign.size() > 0 && sFullScreenIterator != null) {
                if (sFullScreenIterator.hasNext()) {
                    campaign = sFullScreenIterator.next();
                    sFullScreenIterator.remove();
                }
            }
            if (campaign != null) {
                if (campaign.hashCode() %2 == 0) {
                    FullScreenActivity.launch(sContext, campaign, callBack,
                            sFullCreenMtgNativeHandler);
                } else {
                    NativeInterstitialActivity.launch(sContext, campaign, callBack,
                            sFullCreenMtgNativeHandler);
                }
            } else if (faildRunnable != null){
                faildRunnable.run();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static MTGInterstitialHandler sInterstitialHandler;

    public static void preInterstitialAd(String unitId) {
        if (sInterstitialHandler == null) {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            sInterstitialHandler = new MTGInterstitialHandler(sContext, hashMap);
            sInterstitialHandler.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialLoadSuccess() {

                }

                @Override
                public void onInterstitialLoadFail(String s) {
                    Log.e(TAG, "onInterstitialLoadFail error: " + s);
                }

                @Override
                public void onInterstitialShowSuccess() {

                }

                @Override
                public void onInterstitialShowFail(String s) {

                }

                @Override
                public void onInterstitialClosed() {

                }

                @Override
                public void onInterstitialAdClick() {

                }
            });
        }
        sInterstitialHandler.preload();
    }

    public static void showInterstitialAd(String unitId, final Runnable faildRunnable) {
        try {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            sInterstitialHandler = new MTGInterstitialHandler(sContext, hashMap);
            sInterstitialHandler.setInterstitialListener(new InterstitialListener() {
                @Override
                public void onInterstitialLoadSuccess() {
                    if (sInterstitialHandler != null) {
                        sInterstitialHandler.preload();
                    }
                }

                @Override
                public void onInterstitialLoadFail(String s) {
                    Log.e(TAG, "onInterstitialLoadFail error: " + s);
                    sInterstitialHandler = null;
                    if (faildRunnable != null) {
                        faildRunnable.run();
                    }
                }

                @Override
                public void onInterstitialShowSuccess() {

                }

                @Override
                public void onInterstitialShowFail(String s) {
                    Log.e(TAG, "onInterstitialShowFail error: " + s);
                    sInterstitialHandler = null;
                    if (faildRunnable != null) {
                        faildRunnable.run();
                    }
                }

                @Override
                public void onInterstitialClosed() {

                }

                @Override
                public void onInterstitialAdClick() {

                }
            });
            sInterstitialHandler.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void loadNativeFullScreen(String unitId, final Runnable loadedRunnable,
                                            final Runnable faildRunable) {
        Map<String, Object> properties = MtgNativeHandler.getNativeProperties(unitId);
        if (sFullCreenMtgNativeHandler == null) {
            sFullCreenMtgNativeHandler = new MtgNativeHandler(properties, sContext);
            sFullCreenMtgNativeHandler.addTemplate(new NativeListener
                    .Template(MIntegralConstans.TEMPLATE_BIG_IMG, AD_NUM));
            sFullCreenMtgNativeHandler.setAdListener(new NativeListener.NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {
                    sFullScreenCampaign.clear();
                    sFullScreenCampaign.addAll(campaigns);
                    sFullScreenIterator = sFullScreenCampaign.iterator();

                    if (loadedRunnable != null) {
                        loadedRunnable.run();
                    }
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.e(TAG, "onAdLoadError:" + message);
                    if (faildRunable != null) {
                        faildRunable.run();
                    }
                }

                @Override
                public void onAdFramesLoaded(List<Frame> list) {

                }

                @Override
                public void onLoggingImpression(int adsourceType) {

                }

                @Override
                public void onAdClick(Campaign campaign) {
                    Log.e(TAG, "onAdClick");
                }
            });
        }
        sFullCreenMtgNativeHandler.load();
    }

    private static void loadNativeAd(String unitId) {
        Map<String, Object> properties = MtgNativeHandler.getNativeProperties(unitId);
        properties.put(MIntegralConstans.NATIVE_VIDEO_WIDTH, 720);
        properties.put(MIntegralConstans.NATIVE_VIDEO_HEIGHT, 480);
        properties.put(NATIVE_VIDEO_SUPPORT, true);
        properties.put(MIntegralConstans.PROPERTIES_AD_NUM, AD_NUM);

        if (sMtgNativeHandler == null) {
            sMtgNativeHandler = new MtgNativeHandler(properties , sContext);
            sMtgNativeHandler.setAdListener(new NativeListener.NativeAdListener() {
                @Override
                public void onAdLoaded(List<Campaign> list, int i) {
                    sCurrentNativelist.clear();
                    sCurrentNativelist.addAll(list);
                    sCurrentNativeIterator = sCurrentNativelist.iterator();
                }

                @Override
                public void onAdLoadError(String s) {

                }

                @Override
                public void onAdClick(Campaign campaign) {

                }

                @Override
                public void onAdFramesLoaded(List<Frame> list) {

                }

                @Override
                public void onLoggingImpression(int i) {

                }
            });
        }
        sMtgNativeHandler.load();
    }

    public static void preNativeBanner(String unitId) {
        try {
            MIntegralSDK sdk = MIntegralSDKFactory.getMIntegralSDK();
            Map<String, Object> preloadMap = new HashMap<>();
            preloadMap.put(MIntegralConstans.PROPERTIES_LAYOUT_TYPE, MIntegralConstans.LAYOUT_NATIVE);
            preloadMap.put(MIntegralConstans.PROPERTIES_UNIT_ID, unitId);
            List<NativeListener.Template> list = new ArrayList<>();
            list.add(new NativeListener.Template(MIntegralConstans.TEMPLATE_MULTIPLE_IMG, AD_NUM));
            preloadMap.put(MIntegralConstans.NATIVE_INFO, MtgNativeHandler.getTemplateString(list));
            sdk.preload(preloadMap);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static View getNABannerView(String unitId, NativeView.NativeCallBack callBack) {
        try {
            Campaign campaign = null;
            if (sBannerList.size() > 0 && sIteratorCampaign != null && sIteratorCampaign.hasNext()) {
                campaign = sIteratorCampaign.next();
            }

            if (campaign != null) {
                return setupNABannerView(campaign, callBack, sNABannerHandler);
            } else {
                loadNABanner(unitId);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }


    private static MtgNativeHandler sNABannerHandler;
    private static ArrayList<Campaign> sBannerList = new ArrayList<>();
    private static Iterator<Campaign> sIteratorCampaign;

    private static View setupNABannerView(Campaign campaign, NativeView.NativeCallBack callBack, MtgNativeHandler handler) {
        View view = null;
        try {
            view = LayoutInflater.from(sContext).inflate(R.layout.na_banner_layout, null);
            RelativeLayout mRl_Root = (RelativeLayout) view.findViewById(R.id.mintegral_banner_rl_root);
            ImageView mIvIcon = (ImageView) view.findViewById(R.id.mintegral_banner_iv_icon);
            TextView mTvAppName = (TextView) view.findViewById(R.id.mintegral_banner_tv_title);
            TextView mTvAppDesc = (TextView) view.findViewById(R.id.mintegral_banner_tv_app_desc);
            TextView mTvCta = (TextView) view.findViewById(R.id.mintegral_banner_tv_cta);

            if (!TextUtils.isEmpty(campaign.getIconUrl())) {
                callBack.loadImage(campaign.getIconUrl(), mIvIcon);
            }

            mTvAppName.setText(campaign.getAppName());
            mTvAppDesc.setText(campaign.getAppDesc());
            mTvCta.setText(campaign.getAdCall());
            handler.registerView(mRl_Root, campaign);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return view;
    }

    public static void loadNABanner(final String unitId) {
        try {
            Map<String, Object> properties = MtgNativeHandler.getNativeProperties(unitId);
            sNABannerHandler = new MtgNativeHandler(properties, sContext);
            sNABannerHandler.addTemplate(new NativeListener.Template(MIntegralConstans.TEMPLATE_MULTIPLE_IMG, AD_NUM));
            sNABannerHandler.setAdListener(new NativeListener.NativeAdListener() {

                @Override
                public void onAdLoaded(List<Campaign> campaigns, int template) {
                    sBannerList.clear();
                    sBannerList.addAll(campaigns);
                    sIteratorCampaign = sBannerList.iterator();
                    preNativeBanner(unitId);
                }

                @Override
                public void onAdLoadError(String message) {
                    Log.e(TAG, "onAdLoadError:" + message);
                }

                @Override
                public void onAdFramesLoaded(List<Frame> list) {

                }

                @Override
                public void onLoggingImpression(int adsourceType) {

                }

                @Override
                public void onAdClick(Campaign campaign) {

                }
            });
            sNABannerHandler.load();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
