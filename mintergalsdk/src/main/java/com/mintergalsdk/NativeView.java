package com.mintergalsdk;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mintegral.msdk.nativex.view.MTGMediaView;
import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.MtgNativeHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by liyanju on 2018/11/25.
 */

public class NativeView extends FrameLayout {

    private MTGMediaView mMediaview;
    private ImageView mIvIcon;
    private TextView mTvAppName;
    private TextView mTvAppDesc;
    private TextView mTvCta;
    private StarLevelLayoutView mStarLayout;

    private static Set<MTGMediaView> mediaViewSet = new HashSet();

    public NativeView(@NonNull Context context, Campaign campaign, MtgNativeHandler mtgNativeHandler, NativeCallBack callBack) {
        super(context);
        mCampaign = campaign;
        mNativeHandle = mtgNativeHandler;
        mCallBack = callBack;

        View view = LayoutInflater.from(getContext()).inflate(R.layout.native_layout, null);
        addView(view);

        mMediaview = (MTGMediaView) findViewById(R.id.mintegral_mediaview);
        if (mediaViewSet != null){
            mediaViewSet.add(mMediaview);
        }
        mIvIcon = (ImageView) findViewById(R.id.mintegral_feeds_icon);
        mTvAppName = (TextView) findViewById(R.id.mintegral_feeds_app_name);
        mTvCta = (TextView) findViewById(R.id.mintegral_feeds_tv_cta);
        mTvAppDesc = (TextView) findViewById(R.id.mintegral_feeds_app_desc);
        mStarLayout = (StarLevelLayoutView) findViewById(R.id.mintegral_feeds_star);

        if (mCampaign != null) {
            mMediaview.setNativeAd(mCampaign);
            if (!TextUtils.isEmpty(mCampaign.getIconUrl()) && mCallBack != null) {
                mCallBack.loadImage(mCampaign.getIconUrl(), mIvIcon);
            }
            mTvAppName.setText(mCampaign.getAppName() + "");
            mTvAppDesc.setText(mCampaign.getAppDesc() + "");
            mTvCta.setText(mCampaign.getAdCall());
            int rating = (int) mCampaign.getRating();
            mStarLayout.setRating(rating);
            if (mNativeHandle != null) {
                mNativeHandle.registerView(this, mCampaign);
            }
        }
    }

    private void destoryMediaView(){
        try {
            if (mediaViewSet != null && mediaViewSet.size() > 0) {
                for (MTGMediaView mediaview : mediaViewSet) {
                    mediaview.destory();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destoryMediaView();
        mediaViewSet.clear();
    }

    private Campaign mCampaign;
    private MtgNativeHandler mNativeHandle;
    private NativeCallBack mCallBack;


    public interface NativeCallBack {

        void loadImage(String iconUrl, ImageView imageView);

        void loadFaild();
    }

}
