package com.playtube.player.gui.player;

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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.dueeeke.videoplayer.listener.VideoListener;
import com.dueeeke.videoplayer.player.IjkPlayer;
import com.dueeeke.videoplayer.player.PlayerConfig;
import com.facebook.ads.Ad;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.LinkedHashMap;

import com.playtube.player.app.PlayTubeApp;
import com.playtube.player.business.FBAdUtils;
import com.playtube.player.business.FacebookReport;
import com.playtube.player.business.Utils;
import com.playtube.player.business.youtube.bean.YouTubeChannel;
import com.playtube.player.business.youtube.Tasks.GetYouTubeChannelInfoTask;
import com.playtube.player.business.youtube.VideoStream.StreamMetaDataList;
import com.playtube.player.business.interfaces.GetStreamListener;
import com.playtube.player.gui.businessobjects.SubscribeButton;
import com.playtube.player.gui.fragments.ChannelBrowserFragment;
import com.playtube.player.gui.player.widget.DefinitionIjkVideoView;
import com.tube.playtube.R;
import com.playtube.player.business.youtube.bean.YouTubeChannelInterface;
import com.playtube.player.business.youtube.bean.YouTubeVideo;
import com.playtube.player.business.youtube.Tasks.GetVideoDescriptionTask;
import com.playtube.player.business.youtube.VideoStream.StreamMetaData;
import com.playtube.player.business.db.Tasks.CheckIfUserSubbedToChannelTask;
import com.playtube.player.gui.activities.MainActivity;
import com.playtube.player.gui.businessobjects.adapters.CommentsAdapter;
import com.playtube.player.gui.player.controller.DefinitionController;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by liyanju on 2018/6/8.
 */

public class TubePlayerActivity extends AppCompatActivity implements VideoListener{

    public static final String TAG = "TubePlayerActivity";

    public static final String YOUTUBE_VIDEO_OBJ = "TubePlayerActivity.yt_video_obj";

    private DefinitionIjkVideoView ijkVideoView;
    private ExpandableListView commentsExpandableListView;
    private View commentsProgressBar;
    private View noVideoCommentsView;

    private YouTubeVideo youTubeVideo = null;
    private YouTubeChannel youTubeChannel = null;

    private DefinitionController controller;

    public static void launch(Context context, YouTubeVideo youTubeVideo) {
        Intent i = new Intent(context, TubePlayerActivity.class);
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

        if (youTubeVideo == null) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.getSerializable(YOUTUBE_VIDEO_OBJ) != null) {
                youTubeVideo = (YouTubeVideo) bundle.getSerializable(YOUTUBE_VIDEO_OBJ);
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
                        ijkVideoView.setVideoListener(TubePlayerActivity.this);
                        ijkVideoView.start();

                        downloadVideoIV.setVisibility(View.VISIBLE);
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

            new CommentsAdapter(this, youTubeVideo.getId(), commentsExpandableListView, commentsProgressBar, noVideoCommentsView);

            initVideoDetailHeader();

            FacebookReport.logSentVideoPlay();
        }

        FBAdUtils.interstitialLoad(Utils.CHAPING_COMMON_AD, new FBAdUtils.FBInterstitialAdListener(){
            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
                FBAdUtils.destoryInterstitial();
            }
        });
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
                    Intent i = new Intent(TubePlayerActivity.this, MainActivity.class);
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
                if (!Utils.checkAndRequestPermissions(TubePlayerActivity.this)) {
                    return;
                }
                if (youTubeVideo != null && !TextUtils.isEmpty(ijkVideoView.getCurrentUrl())) {
                    youTubeVideo.downloadVideo(PlayTubeApp.getContext(), ijkVideoView.getCurrentUrl());
                }
            }
        });

        commentsExpandableListView.addHeaderView(headerView);

        setVideoDetail();
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
        getYouTubeChannelInfoTask = new GetYouTubeChannelInfoTask(new YouTubeChannelInterface() {
            @Override
            public void onGetYouTubeChannel(YouTubeChannel youTubeChannel) {
                TubePlayerActivity.this.youTubeChannel = youTubeChannel;

                videoDescSubscribeButton.setChannel(TubePlayerActivity.this.youTubeChannel);
                if (youTubeChannel != null && !isFinishing()) {
                    Glide.with(TubePlayerActivity.this)
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
                    if (ijkVideoView != null && ijkVideoView.isPlaying()) {
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

        if (FBAdUtils.isInterstitialLoaded()) {
            FBAdUtils.showInterstitial();
        }
        FBAdUtils.destoryInterstitial();
    }


    @Override
    public void onBackPressed() {
        if (!ijkVideoView.onBackPressed()) {
            super.onBackPressed();
        }
    }




}
