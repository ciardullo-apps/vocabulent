package com.ciardullo.vocabulent.dao;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

/**
 * Contains static methods to invoke simple DML
 */
public class UtilityDAO implements DBConstants {

	/**
	 * Returns the count of rows in the scene table joined to assets so that
	 * only paid scenes are loaded
	 * 
	 * @param context
	 * @return
	 */
	public static int getSceneCount(Context context) {
		VocabulentDBHelper dbHelper = null;
		Cursor cursor = null;
		int n = 0;
		try {
			dbHelper = new VocabulentDBHelper(context);
			// If necessary, copy database from assets
			try {
				dbHelper.createDatabase();
			} catch(IOException e) {}
			
			try {
				dbHelper.openDatabase();
			} catch(SQLException e) {}
			cursor = dbHelper.getReadableDatabase().rawQuery(SQL_SCENE_COUNT,
					null);
			if (cursor != null && cursor.moveToFirst())
				n = cursor.getInt(0);
		} finally {
			try {
				if (cursor != null)
					cursor.close();
			} catch (Exception e) {
			}
			try {
				if (dbHelper != null)
					dbHelper.close();
			} catch (Exception e) {
			}
		}

		return n;
	}
}
