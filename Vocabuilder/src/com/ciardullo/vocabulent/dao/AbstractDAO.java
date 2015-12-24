package com.ciardullo.vocabulent.dao;

import java.io.IOException;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public abstract class AbstractDAO implements DBConstants {
	protected VocabulentDBHelper dbHelper;
	protected String sqlStmt;

	public abstract String[] getAllColumns();
	public abstract Cursor getAllRows();
	protected abstract String[] getSelectionArgs();

	
	public AbstractDAO(Context context) {
		dbHelper = new VocabulentDBHelper(context);
	}

	public void open() throws SQLException, IOException {
		if(!dbHelper.isAlreadyOpened()) {
			try {
				dbHelper.createDatabase();
			} catch(IOException e) {
				throw e;
			}
			
			try {
				dbHelper.openDatabase();
			} catch(SQLException e) {
				throw e;
			}
		}
	}
	
	public void close() throws SQLException {
		dbHelper.close();
	}

	public String getSqlStmt() {
		return sqlStmt;
	}

	public void setSqlStmt(String sqlStmt) {
		this.sqlStmt = sqlStmt;
	}
}
