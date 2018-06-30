package com.tubeplayer.player.business.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.tubeplayer.player.app.TubeApp;
import com.tubeplayer.player.business.AsyncTaskParallel;
import com.tubeplayer.player.business.youtube.bean.YTubeVideo;
import com.tubeplayer.player.business.interfaces.OrderableDatabase;

/**
 * A database (DB) that stores user's downloaded videos.
 */
public class DownloadedVideosDb extends SQLiteOpenHelperEx implements OrderableDatabase {
	private static volatile DownloadedVideosDb downloadsDb = null;
	private static boolean hasUpdated = false;

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "tubedownloads.db";

	private DownloadedVideosListener listener;

	public static synchronized DownloadedVideosDb getVideoDownloadsDb() {
		if (downloadsDb == null) {
			downloadsDb = new DownloadedVideosDb(TubeApp.getContext());
		}

		return downloadsDb;
	}

	private DownloadedVideosDb(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	protected void clearDatabaseInstance() {

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DownloadedVideosTable.getCreateStatement());
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * Get the list of Videos that have been downloaded.
	 *
	 * @return List of Videos
	 */
	public List<YTubeVideo> getDownloadedVideos() {
		Cursor	cursor = getReadableDatabase().query(
						DownloadedVideosTable.TABLE_NAME,
						new String[]{DownloadedVideosTable.COL_YOUTUBE_VIDEO, DownloadedVideosTable.COL_FILE_URI},
						null,
						null, null, null, null);
		List<YTubeVideo> videos = new ArrayList<>();

		if(cursor.moveToNext()) {
			do {
				String youtubeJson = cursor.getString(cursor.getColumnIndex(DownloadedVideosTable.COL_YOUTUBE_VIDEO));
				YTubeVideo video = new Gson().fromJson(youtubeJson, new TypeToken<YTubeVideo>(){}.getType());
				videos.add(video);
			} while(cursor.moveToNext());
		}
		cursor.close();

		return videos;
	}

	public boolean add(YTubeVideo video, String fileUri) {
		Gson gson = new Gson();
		ContentValues values = new ContentValues();
		String json = gson.toJson(video);
		values.put(DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID, video.getId());
		values.put(DownloadedVideosTable.COL_YOUTUBE_VIDEO, json);
		values.put(DownloadedVideosTable.COL_FILE_URI, fileUri);
		Log.v("XX", "json :::" + json);

		int order = getNumDownloads();
		order++;
		values.put(DownloadedVideosTable.COL_ORDER, order);

		boolean addSuccessful = getWritableDatabase().replace(DownloadedVideosTable.TABLE_NAME, null, values) != -1;
		onUpdated();
		return addSuccessful;
	}

	public boolean remove(String videoId) {
		int rowsDeleted = getWritableDatabase().delete(DownloadedVideosTable.TABLE_NAME,
						DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID + " = ?",
						new String[]{videoId});

		onUpdated();
		return (rowsDeleted >= 0);
	}

	public boolean remove(YTubeVideo video) {
		return remove(video.getId());
	}

	public boolean isVideoDownloaded(YTubeVideo video) {
		Cursor cursor = getReadableDatabase().query(
						DownloadedVideosTable.TABLE_NAME,
						new String[]{DownloadedVideosTable.COL_FILE_URI},
						DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID + " = ?",
						new String[]{video.getId()}, null, null, null);

		boolean isDownloaded = false;
		if (cursor.moveToNext()) {
			String uri = cursor.getString(cursor.getColumnIndex(DownloadedVideosTable.COL_FILE_URI));
			isDownloaded = uri != null;
		}
		cursor.close();
		return isDownloaded;
	}

	public Uri getVideoFileUri(YTubeVideo video) {
		Cursor cursor = null;
		try {
			cursor = getReadableDatabase().query(
					DownloadedVideosTable.TABLE_NAME,
					new String[]{DownloadedVideosTable.COL_FILE_URI},
					DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID + " = ?",
					new String[]{video.getId()}, null, null, null);

			if (cursor.moveToNext()) {
				String uri = cursor.getString(cursor.getColumnIndex(DownloadedVideosTable.COL_FILE_URI));
				return Uri.parse(uri);
			}
			return null;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private void onUpdated() {
		hasUpdated = true;
		if(listener != null)
			listener.onDownloadedVideosUpdated();
	}

	public interface DownloadedVideosListener {
		void onDownloadedVideosUpdated();
	}

	public void setListener(DownloadedVideosListener listener) {
		this.listener = listener;
	}

	/**
	 * When a Video in the Downloads tab is drag & dropped to a new position, this will be
	 * called with the new updated list of videos. Since the videos are displayed in descending order,
	 * the first video in the list will have the highest number.
	 *
	 * @param videos List of Videos to update their order.
	 */
	@Override
	public void updateOrder(List<YTubeVideo> videos) {
		int order = videos.size();

		for(YTubeVideo video : videos) {
			ContentValues cv = new ContentValues();
			cv.put(DownloadedVideosTable.COL_ORDER, order--);
			getWritableDatabase().update(DownloadedVideosTable.TABLE_NAME, cv, DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID + " = ?", new String[]{video.getId()});
		}
	}

	/**
	 * @return The total number of downloaded videos.
	 */
	public int getNumDownloads() {
		String	query = String.format("SELECT COUNT(*) FROM %s", DownloadedVideosTable.TABLE_NAME);
		Cursor	cursor = DownloadedVideosDb.getVideoDownloadsDb().getReadableDatabase().rawQuery(query, null);
		int		totalDownloads = 0;

		if (cursor.moveToFirst()) {
			totalDownloads = cursor.getInt(0);
		}

		cursor.close();
		return totalDownloads;
	}

	public static boolean isHasUpdated() {
		return hasUpdated;
	}

	public static void setHasUpdated(boolean hasUpdated) {
		DownloadedVideosDb.hasUpdated = hasUpdated;
	}

	/**
	 * AsyncTask to remove any videos from the Database whose local files have gone missing.
	 */
	public static class RemoveMissingVideosTask extends AsyncTaskParallel<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			Cursor	cursor = getVideoDownloadsDb().getReadableDatabase().query(
							DownloadedVideosTable.TABLE_NAME,
							new String[]{DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID, DownloadedVideosTable.COL_FILE_URI},
							null,
							null, null, null, null);

			if(cursor.moveToNext()) {
				do {
					try {
						String videoId = cursor.getString(cursor.getColumnIndex(DownloadedVideosTable.COL_YOUTUBE_VIDEO_ID));
						Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(DownloadedVideosTable.COL_FILE_URI)));
						if (uri != null) {
							File file = new File(uri.getPath());
							if (!file.exists()) {
								getVideoDownloadsDb().remove(videoId);
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
				} while(cursor.moveToNext());
			}
			cursor.close();
			return null;
		}
	}

}
