package com.ciardullo.vocabulent.dao;

import android.content.Context;
import android.database.Cursor;

import com.ciardullo.vocabulent.vo.CommonVO;

/**
 * Implements the Factory method pattern for the creation of specific CommonVO
 * descendants
 */
public abstract class LoadLookupTableDAO extends AbstractDAO {
	/**
	 * The where clause, without "where", passed to SQLiteDatabase.query()
	 */
	protected String selection;

	protected String[] allColumns = { COLUMN_LKUP_NAME, COLUMN_ID };

	public String getSelection() {
		return selection;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public LoadLookupTableDAO(Context context) {
		super(context);
	}

	public String[] getAllColumns() {
		return allColumns;
	}

	/**
	 * The handy Factory method
	 */
	protected abstract CommonVO createVO(int theId, String theName);

	public Cursor getAllRows() {
		Cursor cursor = null;
		cursor = dbHelper.getDatabase().query(sqlStmt, allColumns,
				getSelection(), getSelectionArgs(), null, null, getOrderBy());

		return cursor;
	}

	/**
	 * Gets the array of where clause arguments
	 */
	public abstract String[] getSelectionArgs();

	/**
	 * Sets the array of where clause arguments
	 * 
	 * @param args
	 */
	public abstract void setSelectionArgs(String[] args);

	/**
	 * Returns the order by column for the lookup
	 * 
	 * @return
	 */
	public abstract String getOrderBy();
}
