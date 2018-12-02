package com.tubeplayer.player.gui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mintergalsdk.LeadboltSDK;
import com.mintergalsdk.MintergalSDK;
import com.tube.playtube.R;
import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.FacebookReport;
import com.tubeplayer.player.business.Logger;
import com.tubeplayer.player.business.db.DownloadedVideosDb;
import com.tubeplayer.player.business.db.Tasks.CheckIfUserSubbedToChannelTask;
import com.tubeplayer.player.business.db.Tasks.IsVideoBookmarkedTask;
import com.tubeplayer.player.business.interfaces.GetStreamListener;
import com.tubeplayer.player.business.youtube.GetVideosDetailsByIDs;
import com.tubeplayer.player.business.youtube.Tasks.GetVideoDescriptionTask;
import com.tubeplayer.player.business.youtube.Tasks.GetYouTubeChannelInfoTask;
import com.tubeplayer.player.business.youtube.VideoStream.StreamMetaDataList;
import com.tubeplayer.player.business.youtube.bean.YTubeChannel;
import com.tubeplayer.player.business.youtube.bean.YTubeChannelInterface;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.gui.activities.MainActivity;
import com.tubeplayer.player.gui.activities.ThumbnailViewerActivity;
import com.tubeplayer.player.gui.businessobjects.MediaControllerEx;
import com.tubeplayer.player.gui.businessobjects.OnSwipeTouchListener;
import com.tubeplayer.player.gui.businessobjects.SubscribeButton;
import com.tubeplayer.player.gui.businessobjects.adapters.CommentsAdapter;
import com.tubeplayer.player.gui.businessobjects.fragments.ImmersiveModeFragment;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;

import hollowsoft.slidingdrawer.OnDrawerOpenListener;
import hollowsoft.slidingdrawer.SlidingDrawer;

/**
 * A fragment that holds a standalone YouTube player.
 */
public class YouTubePlayerFragment extends ImmersiveModeFragment implements MediaPlayer.OnPreparedListener {

	public static final String YOUTUBE_VIDEO_OBJ = "YouTubePlayerFragment.Tubeyt_video_obj";

	private YTubeVideo youTubeVideo = null;
	private YTubeChannel youTubeChannel = null;

	private VideoView			videoView = null;
	/** The current video position (i.e. play time). */
	private int					videoCurrentPosition = 0;
	private MediaControllerEx mediaController = null;

	private TextView			videoDescTitleTextView = null;
	private ImageView			videoDescChannelThumbnailImageView = null;
	private TextView			videoDescChannelTextView = null;
	private SubscribeButton		videoDescSubscribeButton = null;
	private TextView			videoDescViewsTextView = null;
	private ProgressBar			videoDescLikesBar = null;
	private TextView			videoDescLikesTextView = null;
	private TextView			videoDescDislikesTextView = null;
	private View                videoDescRatingsDisabledTextView = null;
	private TextView			videoDescPublishDateTextView = null;
	private TextView			videoDescriptionTextView = null;
	private View				voidView = null;
	private View				loadingVideoView = null;

	private SlidingDrawer		videoDescriptionDrawer = null;
	private View                videoDescriptionDrawerIconView = null;
	private SlidingDrawer		commentsDrawer = null;
	private View                commentsDrawerIconView = null;
	private View				commentsProgressBar = null,
								noVideoCommentsView = null;
	private CommentsAdapter commentsAdapter = null;
	private ExpandableListView	commentsExpandableListView = null;

	private Menu                menu = null;

	private Handler             hideHudTimerHandler = null;
	private Handler             hideVideoDescAndCommentsIconsTimerHandler = null;

	/** Timeout (in milliseconds) before the HUD (i.e. media controller + action/title bar) is hidden. */
	private static final int HUD_VISIBILITY_TIMEOUT = 5000;
	/** Timeout (in milliseconds) before the navigation bar is hidden (which will occur only after
	 * the HUD is hidden). */
	private static final int NAVBAR_VISIBILITY_TIMEOUT = 500;
	private static final String VIDEO_CURRENT_POSITION = "3YouTubePlayerFragment.2TubeVideoCurrentPosition";
	private static final String TAG = YouTubePlayerFragment.class.getSimpleName();
	private static final String TUTORIAL_COMPLETED = "3YouTubePlayerFragment.2TutorialCompleted";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// if immersive mode is enabled then hide the navigation bar
		if (userWantsImmersiveMode()) {
			hideNavigationBar();
		}

		// inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_youtube_player, container, false);

		// indicate that this fragment has an action bar menu
		setHasOptionsMenu(true);

		if (savedInstanceState != null)
			videoCurrentPosition = savedInstanceState.getInt(VIDEO_CURRENT_POSITION, 0);

		if (youTubeVideo == null) {
			// initialise the views
			initViews(view);

			// hide action bar
			getSupportActionBar().hide();

			// get which video we need to play...
			Bundle bundle = getActivity().getIntent().getExtras();
			if (bundle != null  &&  bundle.getSerializable(YOUTUBE_VIDEO_OBJ) != null) {
				// ... either the video details are passed through the previous activity
				youTubeVideo = (YTubeVideo) bundle.getSerializable(YOUTUBE_VIDEO_OBJ);
				setUpHUDAndPlayVideo();

				getVideoInfoTasks();
			} else {
				// ... or the video URL is passed to GoTube via another Android app
				new GetVideoDetailsTask().executeInParallel();
			}

		}

		registerScreen();

		return view;
	}

	private BroadcastReceiver screenReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null) {
				return;
			}
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) { // 锁屏
				try {
					if (videoView != null && videoView.isPlaying()) {
						videoView.pause();
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	};

	private void registerScreen() {
		try {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_USER_PRESENT);
			getActivity().registerReceiver(screenReceiver, filter);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		unregisterScreen();
	}

	private void unregisterScreen() {
		try {
			getActivity().unregisterReceiver(screenReceiver);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialise the views.
	 *
	 * @param view Fragment view.
	 */
	private void initViews(View view) {
		loadingVideoView = view.findViewById(R.id.loadingVideoView);

		videoView = view.findViewById(R.id.video_view);
		// videoView should log any errors
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				String msg = String.format(Locale.getDefault(),
						"Error has occurred while playing video, url='%s', what=%d, extra=%d",
						youTubeVideo != null ? youTubeVideo.getVideoUrl() : "null",
						what,
						extra);
				Log.e(TAG, msg);
				return false;
			}
		});
		// play the video once its loaded
		videoView.setOnPreparedListener(this);

		// setup the media controller (will control the video playing/pausing)
		mediaController = new MediaControllerEx(getActivity(), videoView);
		// ensure that the mediaController is always above the NavBar (given that the NavBar can
		// be in immersive mode)
		if (userWantsImmersiveMode()) {
			mediaController.setPadding(0, 0, 0, getNavBarHeightInPixels());
		}

		voidView = view.findViewById(R.id.void_view);
		// detect if user's swipes motions and taps...
		voidView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {

			@Override
			public boolean onSwipeLeft() {
				commentsDrawer.animateOpen();
				return true;
			}

			@Override
			public boolean onSwipeTop() {
				videoDescriptionDrawer.animateOpen();
				return true;
			}

			@Override
			public boolean onDoubleTap() {
				if (videoView.isPlaying()) {
					videoView.pause();
				} else {
					videoView.start();
				}
				return true;
			}

			@Override
			public boolean onSingleTap() {
				showOrHideHud();
				return true;
			}
		});

		videoDescriptionDrawer = view.findViewById(R.id.des_drawer);
		videoDescriptionDrawerIconView = view.findViewById(R.id.video_desc_icon_image_view);
		videoDescTitleTextView = view.findViewById(R.id.video_desc_title);
		videoDescChannelThumbnailImageView = view.findViewById(R.id.video_desc_channel_thumbnail_image_view);
		videoDescChannelThumbnailImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (youTubeChannel != null) {
					Intent i = new Intent(getActivity(), MainActivity.class);
					i.setAction(MainActivity.ACTION_VIEW_CHANNEL);
					i.putExtra(ChannelBrowserFragment.CHANNEL_OBJ, youTubeChannel);
					startActivity(i);
				}
			}
		});
		videoDescChannelTextView = view.findViewById(R.id.video_desc_channel);
		videoDescViewsTextView = view.findViewById(R.id.video_desc_views);
		videoDescLikesTextView = view.findViewById(R.id.video_desc_likes);
		videoDescDislikesTextView = view.findViewById(R.id.video_desc_dislikes);
		videoDescRatingsDisabledTextView = view.findViewById(R.id.video_desc_ratings_disabled);
		videoDescPublishDateTextView = view.findViewById(R.id.video_desc_publish_date);
//		videoDescriptionTextView = view.findViewById(R.id.video_desc_description);
		videoDescLikesBar = view.findViewById(R.id.video_desc_likes_bar);
		videoDescSubscribeButton = view.findViewById(R.id.video_desc_subscribe_button);

		commentsExpandableListView = view.findViewById(R.id.commentsExpandableListView);
		commentsProgressBar = view.findViewById(R.id.comments_progress_bar);
		noVideoCommentsView = view.findViewById(R.id.no_video_comments_text_view);
		commentsDrawer = view.findViewById(R.id.comments_drawer);
		commentsDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			@Override
			public void onDrawerOpened() {
				if (commentsAdapter == null) {
					commentsAdapter = new CommentsAdapter(getActivity(), youTubeVideo.getId(), commentsExpandableListView, commentsProgressBar, noVideoCommentsView);
				}
			}
		});
		commentsDrawerIconView = view.findViewById(R.id.comments_icon_image_view);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(VIDEO_CURRENT_POSITION, videoCurrentPosition);
	}


	/**
	 * Will asynchronously retrieve additional video information such as channgel avatar ...etc
	 */
	private void getVideoInfoTasks() {
		// get Channel info (e.g. avatar...etc) task
		new GetYouTubeChannelInfoTask(new YTubeChannelInterface() {
			@Override
			public void onGetYouTubeChannel(YTubeChannel youTubeChannel) {
				YouTubePlayerFragment.this.youTubeChannel = youTubeChannel;

				videoDescSubscribeButton.setChannel(YouTubePlayerFragment.this.youTubeChannel);
				if (youTubeChannel != null) {
					if(getActivity() != null)
						Glide.with(getActivity())
										.load(youTubeChannel.getThumbnailNormalUrl())
										.apply(new RequestOptions().placeholder(R.drawable.buddy))
										.into(videoDescChannelThumbnailImageView);
				}
			}
		}).executeInParallel(youTubeVideo.getChannelId());

		// check if the user has subscribed to a channel... if he has, then change the state of
		// the subscribe button
		new CheckIfUserSubbedToChannelTask(videoDescSubscribeButton, youTubeVideo.getChannelId()).execute();
	}


	/**
	 * Will setup the HUD's details according to the contents of {@link #youTubeVideo}.  Then it
	 * will try to load and play the video.
	 */
	private void setUpHUDAndPlayVideo() {
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

		// load the video
		loadVideo();
	}



	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {
		loadingVideoView.setVisibility(View.GONE);
		videoView.seekTo(videoCurrentPosition);

		FacebookReport.logSentVideoPlayStart();

		// was the video player tutorial displayed before?
		if (wasTutorialDisplayedBefore()) {
			videoView.start();
		} else {
			// display the tutorial dialog boxes, then play the video
			displayTutorialDialog(R.string.tutorial_comments_icon, Gravity.TOP | Gravity.RIGHT, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					displayTutorialDialog(R.string.tutorial_video_info_icon, Gravity.BOTTOM | Gravity.LEFT, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							displayTutorialDialog(R.string.tutorial_pause_video, Gravity.CENTER, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									videoView.start();
								}
							});
						}
					});
				}
			});
		}
		showHud();
	}



	@Override
	public void onPause() {
		if (videoView != null && videoView.isPlaying()) {
			videoCurrentPosition = videoView.getCurrentPosition();
		}

		super.onPause();
	}



	/**
	 * @return True if the HUD is visible (provided that this Fragment is also visible).
	 */
	private boolean isHudVisible() {
		return isVisible()  &&  (mediaController.isShowing()  ||  getSupportActionBar().isShowing());
	}



	/**
	 * Hide or display the HUD depending if the HUD is currently visible or not.
	 */
	private void showOrHideHud() {
		if (commentsDrawer.isOpened()) {
			commentsDrawer.animateClose();
		} else if (videoDescriptionDrawer.isOpened()) {
			videoDescriptionDrawer.animateClose();
		} else if (isHudVisible()) {
			hideHud();
		} else {
			showHud();
		}
	}



	/**
	 * Show the HUD (head-up display), i.e. the Action Bar and Media Controller.
	 */
	private void showHud() {
		try {
			if (!isHudVisible()) {
				getSupportActionBar().show();
				getSupportActionBar().setTitle(youTubeVideo != null ? youTubeVideo.getTitle() : "");
				mediaController.show(0);

				videoDescriptionDrawer.close();
				videoDescriptionDrawerIconView.setVisibility(View.INVISIBLE);
				commentsDrawer.close();
				commentsDrawerIconView.setVisibility(View.INVISIBLE);

				// hide UI after a certain timeout (defined in HUD_VISIBILITY_TIMEOUT)
				hideHudTimerHandler = new Handler();
				hideHudTimerHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						hideHud();
						hideHudTimerHandler = null;
					}
				}, HUD_VISIBILITY_TIMEOUT);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	/**
	 * Hide the HUD.
	 */
	private void hideHud() {
		if (isHudVisible()) {
			getSupportActionBar().hide();
			mediaController.hideController();

			// if the user wants the IMMERSIVE mode experience...
			if (userWantsImmersiveMode()) {
				// Hide the navigation bar.  Due to Android pre-defined mechanisms, the nav bar can
				// only be hidden after all animation have been rendered (e.g. mediaController is
				// fully closed).  As a result, a delay is needed in order to explicitly hide the
				// nav bar.
				hideVideoDescAndCommentsIconsTimerHandler = new Handler();
				hideVideoDescAndCommentsIconsTimerHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						hideNavigationBar();
						hideVideoDescAndCommentsIconsTimerHandler = null;
					}
				}, NAVBAR_VISIBILITY_TIMEOUT);
			} else {
				videoDescriptionDrawerIconView.setVisibility(View.VISIBLE);
				commentsDrawerIconView.setVisibility(View.VISIBLE);
			}

			// If there is a hideHudTimerHandler running, then cancel it (stop if from running).  This way,
			// if the HUD was hidden on the 5th second, and the user reopens the HUD, this code will
			// prevent the HUD to re-disappear 2 seconds after it was displayed (assuming that
			// HUD_VISIBILITY_TIMEOUT = 5 seconds).
			if (hideHudTimerHandler != null) {
				hideHudTimerHandler.removeCallbacksAndMessages(null);
				hideHudTimerHandler = null;
			}
		}
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (youTubeVideo == null) {
			return;
		}
		// Hide the download video option if mobile downloads are not allowed and the device is connected through mobile, and the video isn't already downloaded
		boolean allowDownloadsOnMobile = TubeApp.getPreferenceManager().getBoolean(TubeApp.getStr(R.string.pref_key_allow_mobile_downloads), false);
		if((youTubeVideo != null && !youTubeVideo.isDownloaded()) && (TubeApp.isConnectedToWiFi() || (TubeApp.isConnectedToMobile() && allowDownloadsOnMobile))) {
			menu.findItem(R.id.download_video).setVisible(true);
		} else {
			menu.findItem(R.id.download_video).setVisible(false);
		}

		if (youTubeVideo != null && youTubeVideo.isDownloaded()) {
			FacebookReport.logSentDownloadPlay();
		}

		if (!TubeApp.isSpecial()) {
			menu.findItem(R.id.download_video).setVisible(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_youtube_player, menu);

		this.menu = menu;

		// Will now check if the video is bookmarked or not (and then update the menu accordingly).
		//
		// youTubeVideo might be null if we have only passed the video URL to this fragment (i.e.
		// the app is still trying to construct youTubeVideo in the background).
		if (youTubeVideo != null)
			new IsVideoBookmarkedTask(youTubeVideo, menu).executeInParallel();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_reload_video:
				loadVideo();
				return true;

			case R.id.menu_open_video_with:
				playVideoExternally();
				videoView.pause();
				return true;

			case R.id.share:
				youTubeVideo.shareVideo(getContext());
				return true;

			case R.id.copyurl:
				youTubeVideo.copyUrl(getContext());
				return true;

			case R.id.bookmark_video:
				youTubeVideo.bookmarkVideo(getContext(), menu);
				return true;

			case R.id.unbookmark_video:
				youTubeVideo.unbookmarkVideo(getContext(), menu);
				return true;

			case R.id.view_thumbnail:
				Intent i = new Intent(getActivity(), ThumbnailViewerActivity.class);
				i.putExtra(ThumbnailViewerActivity.YOUTUBE_VIDEO, youTubeVideo);
				startActivity(i);
				return true;

			case R.id.download_video:
				youTubeVideo.downloadVideo(getContext());
				MintergalSDK.showInterstitialVideo(getActivity(), TubeApp.CHA_VIDEO_AD_ID, new Runnable() {
					@Override
					public void run() {
						LeadboltSDK.showModule(getContext(), TubeApp.LE_AD_ID);
					}
				});
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LeadboltSDK.initModule(getContext(), TubeApp.LE_AD_ID);
		LeadboltSDK.loadModuleToCache(getContext(), TubeApp.LE_AD_ID);
	}

	/**
	 * Play the video using an external app
	 */
	private void playVideoExternally() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(youTubeVideo.getVideoUrl()));
		startActivity(browserIntent);
	}


	/**
	 * Loads the video specified in {@link #youTubeVideo}.
	 */
	private void loadVideo() {
		// if the video is NOT live
		if (!youTubeVideo.isLiveStream()) {
			videoView.pause();
			videoView.stopPlayback();
			loadingVideoView.setVisibility(View.VISIBLE);
			if(youTubeVideo.isDownloaded()) {
				Uri uri = youTubeVideo.getFileUri();
				File file = new File(uri.getPath());
				// If the file for this video has gone missing, remove it from the Database and then play remotely.
				if(!file.exists()) {
					DownloadedVideosDb.getVideoDownloadsDb().remove(youTubeVideo);
					Toast.makeText(getContext(),
									getContext().getString(R.string.playing_video_file_missing),
									Toast.LENGTH_LONG).show();
					loadVideo();
				} else {
					Logger.i(YouTubePlayerFragment.this, ">> PLAYING LOCALLY: %s", youTubeVideo);
					videoView.setVideoURI(uri);
				}
			} else {
				youTubeVideo.getStream(new GetStreamListener() {
//					@Override
//					public void onGetStream(StreamMetaData desiredStream) {
//						// play the video
//						Logger.i(YouTubePlayerFragment.this, ">> PLAYING: %s", desiredStream);
//						videoView.setVideoURI(desiredStream.getUri());
//					}

					@Override
					public void onGetStream(StreamMetaDataList streamMetaDataList) {

					}

					@Override
					public void onGetStreamError(String errorMessage) {
						if (errorMessage != null && getContext() != null) {
							try {
								new AlertDialog.Builder(getContext())
										.setMessage(errorMessage)
										.setTitle(R.string.error_video_play)
										.setCancelable(false)
										.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												getActivity().finish();
											}
										})
										.show();
							} catch (Throwable e) {
								e.printStackTrace();
							}
						}
					}
				});
			}

			// get the video description
			new GetVideoDescriptionTask(youTubeVideo, new GetVideoDescriptionTask.GetVideoDescriptionTaskListener() {
				@Override
				public void onFinished(String description) {
					videoDescriptionTextView.setText(description);
				}
			}).executeInParallel();
		} else {
			// video is live:  ask the user if he wants to play the video using an other app
			new AlertDialog.Builder(getContext())
							.setMessage(R.string.warning_live_video)
							.setTitle(R.string.error_video_play)
							.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									closeActivity();
								}
							})
							.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									playVideoExternally();
									closeActivity();
								}
							})
							.show();
		}
	}


	/**
	 * Will check whether the video player tutorial was completed before.  If no, it will return
	 * false and will save the value accordingly.
	 *
	 * @return True if the tutorial was completed in the past.
	 */
	private boolean wasTutorialDisplayedBefore() {
		SharedPreferences preferences = TubeApp.getPreferenceManager();
		boolean wasTutorialDisplayedBefore = preferences.getBoolean(TUTORIAL_COMPLETED, false);

		preferences.edit().putBoolean(TUTORIAL_COMPLETED, true).commit();

		return wasTutorialDisplayedBefore;
	}


	/**
	 * Display a tutorial dialog.
	 *
	 * @param messageResId          Message resource ID.
	 * @param dialogGravityFlags    Gravity flags, e.g. Gravity.RIGHT.
	 * @param onClickListener       onClickListener which will be called once the user taps on OK
	 *                              button.
	 */
	private void displayTutorialDialog(int messageResId, int dialogGravityFlags, DialogInterface.OnClickListener onClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(messageResId);
		builder.setPositiveButton(R.string.ok, onClickListener);

		AlertDialog dialog = builder.create();
		WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

		if (wmlp != null) {
			if (dialogGravityFlags != Gravity.CENTER) {
				wmlp.gravity = dialogGravityFlags;
				wmlp.x = 50;   // x position
				wmlp.y = 50;   // y position
			}

			dialog.show();
		}
	}


	////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * This task will, from the given video URL, get the details of the video (e.g. video name,
	 * likes ...etc).
	 */
	private class GetVideoDetailsTask extends AsyncTaskParallel<Void, Void, YTubeVideo> {

		private String videoUrl = null;


		@Override
		protected void onPreExecute() {
			String url = getUrlFromIntent(getActivity().getIntent());

			try {
				// YouTube sends subscriptions updates email in which its videos' URL are encoded...
				// Hence we need to decode them first...
				videoUrl = URLDecoder.decode(url, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException on " + videoUrl + " encoding = UTF-8", e);
				videoUrl = url;
			}
		}


		/**
		 * Returns an instance of {@link YTubeVideo} from the given {@link #videoUrl}.
		 *
		 * @return {@link YTubeVideo}; null if an error has occurred.
		 */
		@Override
		protected YTubeVideo doInBackground(Void... params) {
			String videoId = YTubeVideo.getYouTubeIdFromUrl(videoUrl);
			YTubeVideo youTubeVideo = null;

			if (videoId != null) {
				try {
					GetVideosDetailsByIDs getVideo = new GetVideosDetailsByIDs();
					getVideo.init(videoId);
					List<YTubeVideo> youTubeVideos = getVideo.getNextVideos();

					if (youTubeVideos.size() > 0)
						youTubeVideo = youTubeVideos.get(0);
				} catch (IOException ex) {
					Log.e(TAG, "Unable to get video details, where id="+videoId, ex);
				}
			}

			return youTubeVideo;
		}


		@Override
		protected void onPostExecute(YTubeVideo youTubeVideo) {
			try {
				if (youTubeVideo == null) {
					// invalid URL error (i.e. we are unable to decode the URL)
					String err = String.format(getString(R.string.error_invalid_url), videoUrl);
					Toast.makeText(getActivity(), err, Toast.LENGTH_LONG).show();

					// log error
					Log.e(TAG, err);

					// close the video player activity
					closeActivity();
				} else {
					YouTubePlayerFragment.this.youTubeVideo = youTubeVideo;

					// setup the HUD and play the video
					setUpHUDAndPlayVideo();

					getVideoInfoTasks();

					// will now check if the video is bookmarked or not (and then update the menu
					// accordingly)
					new IsVideoBookmarkedTask(youTubeVideo, menu).executeInParallel();
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}


		/**
		 * The video URL is passed to GoTube via another Android app (i.e. via an intent).
		 *
		 * @return The URL of the YouTube video the user wants to play.
		 */
		private String getUrlFromIntent(final Intent intent) {
			String url = null;

			if (Intent.ACTION_VIEW.equals(intent.getAction()) && intent.getData() != null) {
				url = intent.getData().toString();
			}

			return url;
		}

	}


	////////////////////////////////////////////////////////////////////////////////////////////////


}
