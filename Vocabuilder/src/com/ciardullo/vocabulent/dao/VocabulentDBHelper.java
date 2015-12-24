package com.ciardullo.vocabulent.dao;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class VocabulentDBHelper extends SQLiteOpenHelper implements DBConstants {

	private final Context context;
	private SQLiteDatabase database;
	private boolean alreadyOpened = false;

	public VocabulentDBHelper(Context _context) {
		super(_context, DATABASE_NAME, null, 1);
		context = _context;
	}

	public boolean isAlreadyOpened() {
		return alreadyOpened;
	}

	public void createDatabase() throws IOException {
		boolean dbExist = checkDataBase();
		if(!dbExist) {
//			Log.i(ConjugatorDBHelper.class.getName(), "Creating database " + DATABASE_NAME);
			this.getReadableDatabase();
			try {
				copyDatabase();
			} catch(IOException e) {
				String s = e.getMessage();
				String t = e.getMessage();
			}
		} else {
//			Log.i(ConjugatorDBHelper.class.getName(), "Found existing database " + DATABASE_NAME);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
//		Log.i(ConjugatorDBHelper.class.getName(), "onCreate() called");
//		db.execSQL(CREATE_TBL_EXPRESSION_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		Log.i(ConjugatorDBHelper.class.getName(), "onUpgrade() called");
//		db.execSQL(DROP_TBL_EXPRESSION_SQL);
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each
	 * time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;

		try {
			String myPath = getConjugatorDatabasePath() + DATABASE_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			// database does't exist yet.
		}

		if (checkDB != null) {
			checkDB.close();
		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies the initial database from assets folder to the just created
	 * empty database in the system folder
	 * */
	private void copyDatabase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = context.getAssets().open(DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = getConjugatorDatabasePath() + DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();
	}

	public void openDatabase() throws SQLException {
		String myPath = getConjugatorDatabasePath() + DATABASE_NAME;

		database = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);
		
		alreadyOpened = true;
	}

	@Override
	public synchronized void close() {
		if(database != null)
			database.close();
		super.close();
	}

	public SQLiteDatabase getDatabase() {
		if(database == null || !database.isOpen())
			openDatabase();
		return database;
	}

	/**
	 * Replaces {package} in DBConstants.DATABASE_PATH
	 * @return
	 */
	private String getConjugatorDatabasePath() {
		String s = DATABASE_PATH.replace("{package}", context.getPackageName());
		return s;
	}
}
