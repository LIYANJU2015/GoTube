package com.playtube.player.business.db;

/**
 * Downloaded Videos Table
 */
public class DownloadedVideosTable {
	public static final String TABLE_NAME = "imagedownload";
	public static final String COL_YOUTUBE_VIDEO_ID = "img_Id";
	public static final String COL_YOUTUBE_VIDEO = "img_info";
	public static final String COL_FILE_URI = "img_uri";
	public static final String COL_ORDER = "img_order";


	public static String getCreateStatement() {
		return "CREATE TABLE " + TABLE_NAME + " (" +
						COL_YOUTUBE_VIDEO_ID + " TEXT PRIMARY KEY NOT NULL, " +
						COL_YOUTUBE_VIDEO + " TEXT, " +
						COL_FILE_URI + " TEXT, " +
						COL_ORDER + " INTEGER " +
						" )";
	}

}
