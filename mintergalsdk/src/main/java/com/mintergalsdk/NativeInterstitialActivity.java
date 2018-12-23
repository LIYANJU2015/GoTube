package com.mintergalsdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.MtgNativeHandler;

/**
 * Created by liyanju on 2018/12/1.
 */

public class NativeInterstitialActivity extends Activity{

    public int BIG_IMG_REQUEST_AD_NUM = 1;
    private ImageView mIvIcon;
    private ImageView mIvImage;
    private TextView mTvAppName;
    private TextView mTvAppDesc;
    private TextView mTvCta;
    private RelativeLayout mRlClose;
    private StarLevelLayoutView mStarLayout;
    private LinearLayout mLl_Root;

    private static Campaign sCampaign;
    private static NativeView.NativeCallBack sCallBack;
    private static MtgNativeHandler sNativeHandle;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sCampaign = null;
        sCallBack = null;
        sNativeHandle = null;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mintegral_native_interstitial);

        if (savedInstanceState != null) {
            finish();
        }

        initView();

        if (sCampaign == null) {
            finish();
            return;
        }

        setup(sCampaign);
    }

    private void setup(Campaign campaign) {
        try {
            if (!TextUtils.isEmpty(campaign.getIconUrl()) && sCallBack != null) {
                sCallBack.loadImage(campaign.getIconUrl(), mIvIcon);
            }
            if (!TextUtils.isEmpty(campaign.getImageUrl()) && sCallBack != null) {
                sCallBack.loadImage(campaign.getIconUrl(), mIvImage);
            }

            mTvAppName.setText(campaign.getAppName());
            mTvAppDesc.setText(campaign.getAppDesc());
            mTvCta.setText(campaign.getAdCall());
            int rating = (int) campaign.getRating();
            mStarLayout.setRating(rating);
            sNativeHandle.registerView(mLl_Root, campaign);

            mLl_Root.setBackground(ContextCompat.getDrawable(this, R.drawable.mt_shape_splash_bg));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean init(Campaign campaign, NativeView.NativeCallBack callBack,
                                MtgNativeHandler nativeHandle) {
        sCampaign = campaign;
        sCallBack = callBack;
        sNativeHandle = nativeHandle;

        if (sCampaign != null && sCallBack != null && sNativeHandle != null) {
            return true;
        }

        return false;
    }

    public static boolean launch(Context context, Campaign campaign, NativeView.NativeCallBack callBack,
                                 MtgNativeHandler nativeHandle) {
        if (init(campaign, callBack, nativeHandle)) {
            Intent intent = new Intent(context, NativeInterstitialActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

    private void initView() {
        mIvIcon = (ImageView) findViewById(R.id.mintegral_interstitial_iv_icon);
        mIvImage = (ImageView) findViewById(R.id.mintegral_interstitial_iv_image);
        mTvAppName = (TextView) findViewById(R.id.mintegral_interstitial_iv_app_name);
        mTvAppDesc = (TextView) findViewById(R.id.mintegral_interstitial_tv_app_desc);
        mTvCta = (TextView) findViewById(R.id.mintegral_interstitial_tv_cta);
        mRlClose = (RelativeLayout) findViewById(R.id.mintegral_interstitial_rl_close);
        mStarLayout = (StarLevelLayoutView) findViewById(R.id.mintegral_interstitial_star);
        mLl_Root = (LinearLayout) findViewById(R.id.mintegral_interstitial_ll_root);

        mRlClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
