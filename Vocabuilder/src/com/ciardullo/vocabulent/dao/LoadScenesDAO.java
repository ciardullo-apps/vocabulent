package com.ciardullo.vocabulent.dao;

import android.content.Context;
import android.database.Cursor;

import com.ciardullo.vocabulent.vo.CommonVO;
import com.ciardullo.vocabulent.vo.Scene;

/**
 * Performs DML on the scene table. Overrides parent's getAllRows() to use
 * SQLiteDatabase.rawQuery()
 */
public class LoadScenesDAO extends LoadLookupTableDAO {

//	private static final String GET_SCENES = "select v.lkup_name as lkup_name, v._id as _id, " +
//			"v.scene_image_name as scene_image_name, vd.lkup_name as album_desc, " + 
//			" i._id as iab_id from scene v, scenedesc vd, iab i, iab_asset a ";
	// If iab._id as locked returns null, show the scene, but lock it out.
	// It has not been purchased
	private static final String GET_SCENES = " SELECT " + 
		" v.lkup_name as lkup_name, " +
		" v._id as _id, " +
		" v.scene_image_name as scene_image_name, " +
		" vd.lkup_name as album_desc, " +
		" i._id as locked " +
		" FROM scene v " +
		" JOIN scenedesc vd ON v._id = vd._id " +
		" LEFT JOIN iab_asset ia ON v.purchase_level = ia.purchase_level " +
		" LEFT OUTER JOIN iab i ON ia._id = i._id ";
	
	private String[] selectionArgs;

//	private static String WHERE_CLAUSE = "where i._id = a._id and " + 
//			" v.purchase_level = a.purchase_level and " +
//			"v._id = vd._id and ";
	private static String SELECT_ALBUM_ID = " WHERE album_id = ";
	private static String SELECT_LANG_CD = " vd.lang_cd = ";
	private static String ORDER_BY = "order by ";

	@Override
	protected CommonVO createVO(int theId, String theName) {
		return new Scene(theId, theName);
	}

	public LoadScenesDAO(Context context) {
		super(context);
		setSqlStmt(GET_SCENES);
//		setSelection(WHERE_CLAUSE);
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

	@Override
	public String getSelection() {
//		StringBuffer sb = new StringBuffer();
//		sb.append(WHERE_CLAUSE);
//		return sb.toString();
		return "";
	}
	
	/**
	 * Need to override getAllRows() to use rawQuery()
	 */
	@Override
	public Cursor getAllRows() {
		StringBuffer sb = new StringBuffer();
		sb.append(GET_SCENES);

		sb.append(getSelection());
		if (getSelectionArgs() != null && getSelectionArgs().length > 0) {
			// Filter scenes by album id
			sb.append(SELECT_ALBUM_ID);
			sb.append(getSelectionArgs()[0]);
			sb.append(" AND ");
			sb.append(SELECT_LANG_CD);
			sb.append("'");
			sb.append(getSelectionArgs()[1]);
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
