package com.tubeplayer.player.gui.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.mintergalsdk.AppNextSDK;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.SuperVersions;

/**
 * Created by liyanju on 2018/3/22.
 */

public class SplashActivity extends AppCompatActivity{

    private View container;

    private static final String PRIVACY_POLICY_URL = "http://songtome1919.blogspot.com/2018/09/privacy-policy.html";

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.activity_fade_out);
    }

    private void initSplash() {
        TextView privacyTV = findViewById(R.id.privacy_policy_link);
        privacyTV.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        privacyTV.getPaint().setAntiAlias(true);
        privacyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(PRIVACY_POLICY_URL);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMain();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFirstStart()) {
            setContentView(R.layout.splash_first_activity);
            initSplash();
            TubeApp.getPreferenceManager().edit().putBoolean("isFirstStart", false).apply();
        } else {
            setContentView(R.layout.splash_activity);
            initViews();
        }
    }

    private boolean isFirstStart() {
        return TubeApp.getPreferenceManager().getBoolean("isFirstStart", true) && !TubeApp.isSpecial();
    }

    private void initViews() {
        container = findViewById(R.id.splash_container);
        container.post(new Runnable() {
            @Override
            public void run() {
                startFinalAnim();
            }
        });
    }


    private void startFinalAnim() {
        final ImageView image = findViewById(R.id.splash_logo);
        final TextView name = findViewById(R.id.splash_name);

        ValueAnimator alpha = ObjectAnimator.ofFloat(image, "alpha", 0.0f, 1.0f);
        alpha.setDuration(1000);
        ValueAnimator alphaN = ObjectAnimator.ofFloat(name, "alpha", 0.0f, 1.0f);
        alphaN.setDuration(1000);
        ValueAnimator tranY = ObjectAnimator.ofFloat(image, "translationY", -image.getHeight() / 3, 0);
        tranY.setDuration(1000);
        ValueAnimator wait = ObjectAnimator.ofInt(0, 100);
        wait.setDuration(1000);
        wait.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startMain();
                            if (SuperVersions.isShowAd()) {
                                AppNextSDK.showInterstitial();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                image.setVisibility(View.VISIBLE);
                name.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.play(alpha).with(alphaN).with(tranY).before(wait);
        set.start();
    }

    private void startMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_fade_in, 0);
        finish();
        FacebookReport.logSentMainUserInfo();
    }
}
