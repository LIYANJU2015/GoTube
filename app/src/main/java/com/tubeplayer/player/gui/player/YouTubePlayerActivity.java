package com.tubeplayer.player.gui.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.dueeeke.videoplayer.listener.VideoListener;
import com.dueeeke.videoplayer.player.IjkPlayer;
import com.dueeeke.videoplayer.player.PlayerConfig;
import com.mintergalsdk.AppNextSDK;
import com.mintergalsdk.MintergalSDK;
import com.mintergalsdk.NativeView;
import com.ms.square.android.expandabletextview.ExpandableTextView;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.SuperVersions;
import com.tubeplayer.player.business.Utils;
import com.tubeplayer.player.business.db.Tasks.CheckIfUserSubbedToChannelTask;
import com.tubeplayer.player.business.interfaces.GetStreamListener;
import com.tubeplayer.player.business.youtube.Tasks.GetVideoDescriptionTask;
import com.tubeplayer.player.business.youtube.Tasks.GetYouTubeChannelInfoTask;
import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaData;
import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaDataList;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.bean.YTubeChannelInterface;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.gui.activities.MainActivity;
import com.tubeplayer.player.gui.businessobjects.SubscribeButton;
import com.tubeplayer.player.gui.businessobjects.adapters.CommentsAdapter;
import com.tubeplayer.player.gui.fragments.ChannelBrowserFragment;
import com.tubeplayer.player.gui.player.controller.DefinitionController;
import com.tubeplayer.player.gui.player.widget.DefinitionIjkVideoView;

import java.util.LinkedHashMap;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by liyanju on 2018/6/8.
 */

public class YouTubePlayerActivity extends AppCompatActivity implements VideoListener{

    public static final String TAG = "TubePlayerActivity";

    public static final String YOUTUBE_VIDEO_OBJ = "TubePlayerActivity.yt_video_obj";

    private DefinitionIjkVideoView ijkVideoView;
    private ExpandableListView commentsExpandableListView;
    private View commentsProgressBar;
    private View noVideoCommentsView;

    private YTubeVideo youTubeVideo = null;
    private YTubeChannel youTubeChannel = null;

    private DefinitionController controller;

    public static void launch(Context context, YTubeVideo youTubeVideo) {
        Intent i = new Intent(context, YouTubePlayerActivity.class);
        i.putExtra(YOUTUBE_VIDEO_OBJ, youTubeVideo);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    @Override
    public void onVideoStarted() {
        FacebookReport.logSentVideoPlayStart();
    }

    @Override
    public void onVideoPaused() {

    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onPrepared() {

    }

    @Override
    public void onError() {

    }

    @Override
    public void onInfo(int i, int i1) {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.transparence(this);
        setContentView(R.layout.tube_player_layout);

        if (savedInstanceState != null) {
            finish();
        }

        if (youTubeVideo == null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getSerializable(YOUTUBE_VIDEO_OBJ) != null) {
                youTubeVideo = (YTubeVideo) bundle.getSerializable(YOUTUBE_VIDEO_OBJ);
            }
        }

        initView();

        registerScreen();

        if (youTubeVideo != null) {
            ijkVideoView.setTitle(youTubeVideo.getTitle());
            controller.showLoading();
            if (youTubeVideo.isLiveStream()) {
                controller.setLive();
            }
            youTubeVideo.getStream(new GetStreamListener() {
                @Override
                public void onGetStream(StreamMetaDataList streamMetaDataList) {
                    if (streamMetaDataList != null && !isFinishing()) {
                        LinkedHashMap<String, String> videos = new LinkedHashMap<>();
                        for (StreamMetaData streamMetaData : streamMetaDataList) {
                            videos.put(streamMetaData.getResolution().toString(),
                                    streamMetaData.getUri().toString());
                        }

                        ijkVideoView.setDefinitionVideos(videos);
                        ijkVideoView.setVideoListener(YouTubePlayerActivity.this);
                        ijkVideoView.start();

                        if (TubeApp.isSpecial()) {
                            downloadVideoIV.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onGetStreamError(String errorMessage) {
                    Log.e(TAG, "onGetStreamError " + errorMessage);
                    if (!isFinishing()) {
                        controller.hideLoading();
                        controller.showUrlError(errorMessage);
                    }
                }
            });

            initVideoDetailHeader();

            new CommentsAdapter(this, youTubeVideo.getId(), commentsExpandableListView, commentsProgressBar, noVideoCommentsView);

            FacebookReport.logSentVideoPlay();
        }

        if (SuperVersions.isSpecial()) {

            MintergalSDK.preInterstitialAd(TubeApp.CHA_PING_AD_ID);
            MintergalSDK.preNativeFullScreen(TubeApp.NATIVE_AD_ID);

        }
    }

    private TextView videoDescTitleTextView;
    private TextView videoDescChannelTextView;
    private TextView videoDescViewsTextView;
    private TextView videoDescLikesTextView;
    private TextView videoDescPublishDateTextView;
    private TextView videoDescDislikesTextView;
    private ProgressBar videoDescLikesBar;
    private View videoDescRatingsDisabledTextView;
    private SubscribeButton videoDescSubscribeButton;
    private ImageView videoDescChannelThumbnailImageView;
    private ExpandableTextView videoDescriptionTextView;
    private View downloadVideoIV;

    private void initVideoDetailHeader() {
        View headerView = LayoutInflater.from(this).inflate(R.layout.video_description, null);
        videoDescTitleTextView = headerView.findViewById(R.id.video_desc_title);
        videoDescChannelTextView = headerView.findViewById(R.id.video_desc_channel);
        videoDescViewsTextView = headerView.findViewById(R.id.video_desc_views);
        videoDescLikesTextView = headerView.findViewById(R.id.video_desc_likes);
        videoDescPublishDateTextView = headerView.findViewById(R.id.video_desc_publish_date);
        videoDescDislikesTextView = headerView.findViewById(R.id.video_desc_dislikes);
        videoDescLikesBar = headerView.findViewById(R.id.video_desc_likes_bar);
        videoDescRatingsDisabledTextView = headerView.findViewById(R.id.video_desc_ratings_disabled);
        videoDescSubscribeButton = headerView.findViewById(R.id.video_desc_subscribe_button);
        videoDescChannelThumbnailImageView = headerView.findViewById(R.id.video_desc_channel_thumbnail_image_view);
        videoDescriptionTextView = headerView.findViewById(R.id.expand_text_view);
        videoDescChannelThumbnailImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (youTubeChannel != null) {
                    Intent i = new Intent(YouTubePlayerActivity.this, MainActivity.class);
                    i.setAction(MainActivity.ACTION_VIEW_CHANNEL);
                    i.putExtra(ChannelBrowserFragment.CHANNEL_OBJ, youTubeChannel);
                    startActivity(i);
                }
            }
        });
        downloadVideoIV = headerView.findViewById(R.id.download_video_iv);
        downloadVideoIV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!Utils.checkAndRequestPermissions(YouTubePlayerActivity.this)) {
                    return;
                }
                if (youTubeVideo != null && !TextUtils.isEmpty(ijkVideoView.getCurrentUrl())) {
                    youTubeVideo.downloadVideo(TubeApp.getContext(), ijkVideoView.getCurrentUrl());
                }
            }
        });

        commentsExpandableListView.addHeaderView(headerView);

        setVideoDetail();

        RelativeLayout adFrameLayout = headerView.findViewById(R.id.ad_frame);
        if (SuperVersions.isShowAd()) {
            adFrameLayout.setVisibility(View.VISIBLE);
            adFrameLayout.removeAllViews();
            View adView = MintergalSDK.getNABannerView(TubeApp.NATIVE_AD_ID, TubeApp.callBack);
            adFrameLayout.addView(adView);
        } else {
            adFrameLayout.setVisibility(View.GONE);
        }
    }

    private void setVideoDetail() {
        if (youTubeVideo == null) {
            return;
        }

        videoDescTitleTextView.setText(youTubeVideo.getTitle());
        videoDescChannelTextView.setText(youTubeVideo.getChannelName());
        videoDescViewsTextView.setText(youTubeVideo.getViewsCount());
        videoDescPublishDateTextView.setText(youTubeVideo.getPublishDatePretty());

        if (youTubeVideo.isThumbsUpPercentageSet()) {
            videoDescLikesTextView.setText(youTubeVideo.getLikeCount());
            videoDescDislikesTextView.setText(youTubeVideo.getDislikeCount());
            videoDescLikesBar.setProgress(youTubeVideo.getThumbsUpPercentage());
        } else {
            videoDescLikesTextView.setVisibility(View.GONE);
            videoDescDislikesTextView.setVisibility(View.GONE);
            videoDescLikesBar.setVisibility(View.GONE);
            videoDescRatingsDisabledTextView.setVisibility(View.VISIBLE);
        }

        getVideoInfoTasks();
    }

    private void getVideoInfoTasks() {
        // get Channel info (e.g. avatar...etc) task
        getYouTubeChannelInfoTask = new GetYouTubeChannelInfoTask(new YTubeChannelInterface() {
            @Override
            public void onGetYouTubeChannel(YTubeChannel youTubeChannel) {
                YouTubePlayerActivity.this.youTubeChannel = youTubeChannel;

                videoDescSubscribeButton.setChannel(YouTubePlayerActivity.this.youTubeChannel);
                if (youTubeChannel != null && !isFinishing()) {
                    Glide.with(YouTubePlayerActivity.this)
                                .load(youTubeChannel.getThumbnailNormalUrl())
                                .apply(RequestOptions.bitmapTransform(new CircleCrop()).placeholder(R.drawable.buddy))
                                .into(videoDescChannelThumbnailImageView);
                }
            }
        });
        getYouTubeChannelInfoTask.executeInParallel(youTubeVideo.getChannelId());

        getVideoDescriptionTask = new GetVideoDescriptionTask(youTubeVideo, new GetVideoDescriptionTask.GetVideoDescriptionTaskListener() {
            @Override
            public void onFinished(String description) {
                videoDescriptionTextView.setText(description);
            }
        });
        getVideoDescriptionTask.executeInParallel();

        // check if the user has subscribed to a channel... if he has, then change the state of
        // the subscribe button
        checkIfUserSubbedToChannelTask = new CheckIfUserSubbedToChannelTask(videoDescSubscribeButton, youTubeVideo.getChannelId());
        checkIfUserSubbedToChannelTask.execute();
    }

    private void unregisterScreen() {
        try {
            unregisterReceiver(screenReceiver);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void registerScreen() {
        try {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            registerReceiver(screenReceiver, filter);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                try {
                    if (ijkVideoView != null && ijkVideoView.isPlaying() && !TubeApp.isSpecial()) {
                        ijkVideoView.pause();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void initView() {
        ijkVideoView = findViewById(R.id.player);
        controller = new DefinitionController(this);
        ijkVideoView.setPlayerConfig(new PlayerConfig.Builder()
                .setCustomMediaPlayer(new IjkPlayer(this) {
                    @Override
                    public void setOptions() {
                        mMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,
                                "enable-accurate-seek", 1);
                    }
                })
                .autoRotate()
                .savingProgress()
                .build());
        ijkVideoView.setVideoController(controller);

        commentsExpandableListView = findViewById(R.id.commentsExpandableListView);
        commentsProgressBar = findViewById(R.id.comments_progress_bar);
        noVideoCommentsView = findViewById(R.id.no_video_comments_text_view);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ijkVideoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ijkVideoView.resume();
    }

    private GetVideoDescriptionTask getVideoDescriptionTask;
    private GetYouTubeChannelInfoTask getYouTubeChannelInfoTask;
    private CheckIfUserSubbedToChannelTask checkIfUserSubbedToChannelTask;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkVideoView.release();
        ijkVideoView.setVideoListener(null);
        unregisterScreen();
        if (youTubeVideo != null) {
            youTubeVideo.cancelGetStream();
        }
        if (getVideoDescriptionTask != null) {
            getVideoDescriptionTask.cancel(true);
        }
        if (getYouTubeChannelInfoTask != null) {
            getYouTubeChannelInfoTask.cancel(true);
        }
        if (checkIfUserSubbedToChannelTask != null) {
            checkIfUserSubbedToChannelTask.cancel(true);
        }

        if (SuperVersions.isSpecial()) {
            MintergalSDK.showNativeFullScreen(TubeApp.NATIVE_AD_ID, new NativeView.NativeCallBack() {
                @Override
                public void loadImage(String iconUrl, ImageView imageView) {
                    Glide.with(getApplication()).load(iconUrl).into(imageView);
                }

                @Override
                public void loadFaild() {

                }
            }, new Runnable() {
                @Override
                public void run() {
                    AppNextSDK.showInterstitial();
                }
            });

        }
    }


    @Override
    public void onBackPressed() {
        if (!ijkVideoView.onBackPressed()) {
            super.onBackPressed();
        }
    }




}
