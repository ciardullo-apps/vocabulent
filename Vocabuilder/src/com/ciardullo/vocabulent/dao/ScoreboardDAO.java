package com.ciardullo.vocabulent.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ciardullo.vocabulent.vo.ScoreCard;

/**
 * DAO for the scoreboard table
 */
public class ScoreboardDAO extends AbstractDAO {
	private int sceneId;
	private int albumId;

	private static String[] allColumns = {
		COLUMN_ALBUM_ID,
		COLUMN_SCENE_ID,
		COLUMN_TIMES_TESTED,
		COLUMN_BEST_TIME_IN_SEC,
		COLUMN_BEST_SCORE
	};

	private static String SQL_ALL_STATS =
		" SELECT a._id as album_id, s._id as _id, ad.lkup_name as album_desc, sd.lkup_name as lkup_name, ifnull(times_tested, 0) as times_tested, " + 
		" ifnull(best_time_in_sec, '-') || ' sec' as best_time_in_sec, ifnull(best_score, 0) || '%' as best_score " +
		" FROM album a " +
		" JOIN albumdesc ad ON a._id = ad._id " +
		" JOIN scene s on a._id = s.album_id " +
		" JOIN scenedesc sd ON s._id = sd._id " +
		" JOIN iab_asset ia ON s.purchase_level = ia.purchase_level " +
		" LEFT OUTER JOIN iab i ON ia._id = i._id " +
		" LEFT OUTER JOIN scoreboard r ON a._id = r.album_id AND s._id = r.scene_id " +
		" WHERE ad.lang_cd = ? AND " +
		" sd.lang_cd = ? " +
		" ORDER BY album_desc, r.best_score desc, r.times_tested desc";


	public ScoreboardDAO(Context context, int albumId, int sceneId) {
		super(context);
		this.albumId = albumId;
		this.sceneId = sceneId;
	}

	public ScoreboardDAO(Context context) {
		super(context);
	}

	/**
	 * Saves one test results for a given album and scene
	 */
	public void saveScoreCard(ScoreCard rc) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_ALBUM_ID, rc.getAlbum_id());
		values.put(COLUMN_SCENE_ID, rc.getScene_id());
		values.put(COLUMN_TIMES_TESTED, rc.getTimes_tested());
		int n = rc.getScore();
		if(n < rc.getPrev_best_score())
			n = rc.getPrev_best_score();
		values.put(COLUMN_BEST_SCORE, n);
		n = rc.getTime_in_sec();
		if(n >= rc.getPrev_best_time_in_sec() && rc.getPrev_best_time_in_sec() > 0)
			n = rc.getPrev_best_time_in_sec();
		values.put(COLUMN_BEST_TIME_IN_SEC, n);
		SQLiteDatabase db = null;
		try {
			dbHelper.close();
			db = dbHelper.getWritableDatabase();
			db.replace(TBL_NM_SCOREBOARD, null, values);
		} finally {
			try {
				dbHelper.close();
			} catch(Exception e) {}
		}
	}

	/**
	 * Returns a ScoreCard for a given album and scene
	 * @param albumId
	 * @param sceneId
	 * @return
	 */
	public ScoreCard getScoreCard() {
		ScoreCard rc = null;
		Cursor cursor = null;
		try {
			// Only one row at most is returned
			cursor = getAllRows();
			if(cursor != null) {
				if(cursor.moveToFirst()) {
					rc = new ScoreCard();
					rc.setAlbum_id(cursor.getInt(0));
					rc.setScene_id(sceneId);
					rc.setTimes_tested(cursor.getInt(2));
					rc.setPrev_best_time_in_sec(cursor.getInt(3));
					rc.setPrev_best_score(cursor.getInt(4));
				}
			}

			if(rc == null) {
				// No scoreboard exists for this scene
				rc = new ScoreCard();
				rc.setAlbum_id(albumId);
				rc.setScene_id(sceneId);
				rc.setTimes_tested(0);
				rc.setTime_in_sec(0);
				rc.setPrev_best_score(0);
			}
		} finally {
			try {
				if(cursor != null) {
					cursor.close();
				}
			} catch(Exception e) {}
		}
		
		return rc;
	}

	@Override
	public String[] getAllColumns() {
		return allColumns;
	}

	@Override
	public Cursor getAllRows() {
		Cursor cursor = dbHelper.getReadableDatabase().query(
					TBL_NM_SCOREBOARD, 
					getAllColumns(), 
					"album_id = ? and scene_id = ?", 
					getSelectionArgs(), 
					null, null, null);
			
		return cursor;
	}

	@Override
	protected String[] getSelectionArgs() {
		return new String[] { String.valueOf(albumId), String.valueOf(sceneId) };
	}

	/**
	 * Gets statistics for all purchased scene packs
	 * @param langCd
	 * @return
	 */
	public Cursor getAllStats(String langCd) {
		Cursor cursor = dbHelper.getReadableDatabase()
				.rawQuery(SQL_ALL_STATS, new String[] { langCd, langCd });
		return cursor;
	}
}
