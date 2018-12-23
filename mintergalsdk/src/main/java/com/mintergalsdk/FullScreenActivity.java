package com.mintergalsdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mintegral.msdk.out.Campaign;
import com.mintegral.msdk.out.MtgNativeHandler;

/**
 * Created by liyanju on 2018/11/25.
 */

public class FullScreenActivity extends FragmentActivity{

    private ImageView mIvIcon;
    private ImageView mIvImage;
    private TextView mTvAppName;
    private TextView mTvAppDesc;
    private TextView mTvCta;
    private RelativeLayout mRlClose;
    private StarLevelLayoutView mStarLayout;
    private LinearLayout mLl_Root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mintegral_native_full_screen_ad);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        if (savedInstanceState != null) {
            finish();
        }

        mIvIcon = (ImageView) findViewById(R.id.mintegral_full_screen_iv_icon);
        mIvImage = (ImageView) findViewById(R.id.mintegral_full_screen_iv_image);
        mTvAppName = (TextView) findViewById(R.id.mintegral_full_screen_iv_app_name);
        mTvAppDesc = (TextView) findViewById(R.id.mintegral_full_screen_tv_app_desc);
        mTvCta = (TextView) findViewById(R.id.mintegral_full_screen_tv_cta);
        mRlClose = (RelativeLayout) findViewById(R.id.mintegral_full_screen_rl_close);
        mStarLayout = (StarLevelLayoutView) findViewById(R.id.mintegral_full_screen_star);
        mLl_Root = (LinearLayout) findViewById(R.id.mintegral_full_screen_ll_root);

        mRlClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupView();
    }

    public static boolean launch(Context context, Campaign campaign, NativeView.NativeCallBack callBack,
                                 MtgNativeHandler nativeHandle) {
        if (init(campaign, callBack, nativeHandle)) {
            Intent intent = new Intent(context, FullScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
        return false;
    }

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

    private void setupView() {
        try {
            if (sCampaign == null) {
                return;
            }

            if (!TextUtils.isEmpty(sCampaign.getIconUrl()) && sCallBack != null) {
                sCallBack.loadImage(sCampaign.getIconUrl(), mIvIcon);
            }
            if (!TextUtils.isEmpty(sCampaign.getImageUrl()) && sCallBack != null) {
                sCallBack.loadImage(sCampaign.getImageUrl(), mIvImage);
            }
            mTvAppName.setText(sCampaign.getAppName());
            mTvAppDesc.setText(sCampaign.getAppDesc());
            mTvCta.setText(sCampaign.getAdCall());
            int rating = (int) sCampaign.getRating();
            mStarLayout.setRating(rating);
            sNativeHandle.registerView(mTvCta, sCampaign);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
