package com.ciardullo.vocabulent.dao;

import android.content.Context;
import android.database.Cursor;

import com.ciardullo.vocabulent.vo.Album;
import com.ciardullo.vocabulent.vo.CommonVO;

/**
 * Performs DML on the album table. Overrides parent's getAllRows() to use
 * SQLiteDatabase.rawQuery()
 */
public class LoadAlbumsDAO extends LoadLookupTableDAO {

	private static final String GET_ALBUMS = "select v.lkup_name as lkup_name, v._id as _id, " +
			"v.album_image_name as album_image_name, vd.lkup_name as album_desc " +
			"from album v, albumdesc vd ";
	private String[] selectionArgs;

	// Add support for purchase_level column
	private static String WHERE_CLAUSE = " where v._id = vd._id ";
	private static String SELECT_LANG_CD = " vd.lang_cd = ";
	private static String ORDER_BY = "order by ";

	@Override
	protected CommonVO createVO(int theId, String theName) {
		return new Album(theId, theName);
	}

	public LoadAlbumsDAO(Context context) {
		super(context);
		setSqlStmt(GET_ALBUMS);
		setSelection(SELECT_LANG_CD);
	}

	@Override
	public String[] getSelectionArgs() {
		return selectionArgs;
	}

	@Override
	public void setSelectionArgs(String[] args) {
		selectionArgs = args;
	}

	public String getOrderBy() {
		return COLUMN_LKUP_NAME;
	}

	/**
	 * In order to support album.purchase_level, support multi-column where
	 * clause. Always set purchase_level in the where clause
	 * 
	 * @return
	 */
	@Override
	public String getSelection() {
		StringBuffer sb = new StringBuffer();
		sb.append(WHERE_CLAUSE);
		if (this.selection != null && !"".equals(this.selection)) {
			// There is already a where clause... append to it
			sb.append(" AND ");
			sb.append(this.selection);
		}

		return sb.toString();
	}

	/**
	 * Need to override getAllRows() to use rawQuery()
	 */
	@Override
	public Cursor getAllRows() {
		StringBuffer sb = new StringBuffer();
		sb.append(GET_ALBUMS);
		sb.append(getSelection());
		if (getSelectionArgs() != null && getSelectionArgs().length > 0) {
			// Future Use: filter albums
			sb.append("'");
			sb.append(getSelectionArgs()[0]);
			sb.append("'");
			sb.append(" ");
		}
		sb.append(ORDER_BY);
		sb.append(getOrderBy());

		Cursor cursor = null;
		cursor = dbHelper.getDatabase().rawQuery(sb.toString(), null);
		return cursor;
	}

}
