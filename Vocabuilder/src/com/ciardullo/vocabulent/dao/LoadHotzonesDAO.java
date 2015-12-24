package com.ciardullo.vocabulent.dao;

import android.content.Context;
import android.database.Cursor;

import com.ciardullo.vocabulent.vo.CommonVO;
import com.ciardullo.vocabulent.vo.Hotzone;

public class LoadHotzonesDAO extends LoadLookupTableDAO {
	private static final String GET_HOTZONES = "select v.hot_desc as lkup_name, v.hot_id as _id, " +
			"v.point_left as point_left, v.point_top as point_top, " +
			"v.point_right as point_right, v.point_bottom as point_bottom, " +
			"vd.answer as hot_desc " +
			"from hotzone v, answerkey vd ";
	private String[] selectionArgs;

	// Add support for purchase_level column
	private static String WHERE_CLAUSE = " where v.scene_id = vd.scene_id and v.hot_id = vd.hot_id and ";
	private static String SELECT_SCENE_ID = " v.scene_id = ";
	private static String SELECT_LANG_CD = " vd.lang_cd = ";
	private static String ORDER_BY = "order by ";

	@Override
	protected CommonVO createVO(int theId, String theName) {
		return new Hotzone(theId, theName);
	}

	public LoadHotzonesDAO(Context context) {
		super(context);
		setSqlStmt(GET_HOTZONES);
		setSelection(WHERE_CLAUSE);
	}

	@Override
	public String[] getSelectionArgs() {
		return selectionArgs;
	}

	@Override
	public void setSelectionArgs(String[] args) {
		selectionArgs = args;
	}

	/**
	 * Must order by hot_id so that smaller hotzones are
	 * tested before larger ones that may obscure them
	 */
	public String getOrderBy() {
		return COLUMN_ID;
	}

	@Override
	public String getSelection() {
		StringBuffer sb = new StringBuffer();
		sb.append(WHERE_CLAUSE);

		return sb.toString();
	}

	/**
	 * Need to override getAllRows() to use rawQuery()
	 */
	@Override
	public Cursor getAllRows() {
		StringBuffer sb = new StringBuffer();
		sb.append(GET_HOTZONES);

		sb.append(getSelection());
		if (getSelectionArgs() != null && getSelectionArgs().length > 0) {
			// Filter scenes by album id
			sb.append(SELECT_SCENE_ID);
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
