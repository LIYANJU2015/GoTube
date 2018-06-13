package free.studio.tube.gui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.rating.RatingActivity;

import butterknife.BindView;
import free.rm.gotube.R;
import free.studio.tube.app.GoTubeApp;
import free.studio.tube.businessobjects.AsyncTaskParallel;
import free.studio.tube.businessobjects.VideoCategory;
import free.studio.tube.businessobjects.db.DownloadedVideosDb;
import free.studio.tube.gui.businessobjects.adapters.OrderableVideoGridAdapter;
import free.studio.tube.gui.businessobjects.adapters.VideoGridAdapter;
import free.studio.tube.gui.businessobjects.fragments.OrderableVideosGridFragment;

/**
 * A fragment that holds videos downloaded by the user.
 */
public class DownloadedVideosFragment extends OrderableVideosGridFragment implements DownloadedVideosDb.DownloadedVideosListener {
	@BindView(R.id.noDownloadedVideosText)
	View noDownloadedVideosText;
	@BindView(R.id.downloadsDisabledWarning)
	View downloadsDisabledWarning;

	public static boolean sIsPlayDownload = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		videoGridAdapter = new VideoGridAdapter(getActivity(), false);
		setLayoutResource(R.layout.fragment_downloads);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		swipeRefreshLayout.setEnabled(false);
		populateList();
		displayDownloadsDisabledWarning();
	}

	@Override
	public void onFragmentSelected() {
		super.onFragmentSelected();

		if (DownloadedVideosDb.getVideoDownloadsDb().isHasUpdated()) {
			populateList();
			DownloadedVideosDb.getVideoDownloadsDb().setHasUpdated(false);
		}

		displayDownloadsDisabledWarning();
	}

	/**
	 * If the user is using mobile network (e.g. 4G) and the preferences setting was not ticked to
	 * allow downloading functionality over the mobile data, then inform the user by displaying the
	 * warning;  else hide such warning.
	 */
	private void displayDownloadsDisabledWarning() {
		if (downloadsDisabledWarning != null) {
			boolean allowDownloadsOnMobile = GoTubeApp.getPreferenceManager().getBoolean(GoTubeApp.getStr(R.string.pref_key_allow_mobile_downloads), false);
			downloadsDisabledWarning.setVisibility(GoTubeApp.isConnectedToMobile() && !allowDownloadsOnMobile ? View.VISIBLE : View.GONE);
		}
	}

	@Override
	protected VideoCategory getVideoCategory() {
		return VideoCategory.DOWNLOADED_VIDEOS;
	}

	@Override
	public String getFragmentName() {
		return GoTubeApp.getStr(R.string.downloads);
	}

	@Override
	public void onDownloadedVideosUpdated() {
		populateList();
		videoGridAdapter.refresh();
	}

	private void populateList() {
		new PopulateBookmarksTask().executeInParallel();
	}

	/**
	 * A task that:
	 *   1. gets the current total number of bookmarks
	 *   2. updated the UI accordingly (wrt step 1)
	 *   3. get the bookmarked videos asynchronously.
	 */
	private class PopulateBookmarksTask extends AsyncTaskParallel<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			return DownloadedVideosDb.getVideoDownloadsDb().getNumDownloads();
		}


		@Override
		protected void onPostExecute(Integer numVideosBookmarked) {
			// If no videos have been bookmarked, show the text notifying the user, otherwise
			// show the swipe refresh layout that contains the actual video grid.
			if (numVideosBookmarked <= 0) {
				swipeRefreshLayout.setVisibility(View.GONE);
				noDownloadedVideosText.setVisibility(View.VISIBLE);
			} else {
				swipeRefreshLayout.setVisibility(View.VISIBLE);
				noDownloadedVideosText.setVisibility(View.GONE);

				// set video category and get the bookmarked videos asynchronously
				videoGridAdapter.setVideoCategory(VideoCategory.DOWNLOADED_VIDEOS);
			}
		}

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (videoGridAdapter.getItemCount() > 0 && sIsPlayDownload) {
			sIsPlayDownload = false;
			RatingActivity.launch(GoTubeApp.getContext(), "", getString(R.string.rating_text));
		}
	}
}
