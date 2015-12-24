package com.ciardullo.vocabulent.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.ciardullo.vocabulent.dao.DBConstants;
import com.ciardullo.vocabulent.dao.ScoreboardDAO;
import com.ciardullo.vocabulent.dao.UtilityDAO;
import com.ciardullo.vocabulent.thread.AlbumLoaderTask;
import com.ciardullo.vocabulent.thread.ReportProgressTask;
import com.ciardullo.vocabulent.util.SceneManager;
import com.ciardullo.vocabulent.util.SceneUtil;
import com.ciardullo.vocabulent_it.R;

public class ScoreboardActivity extends AbstractLoadSceneActivity implements
		DBConstants {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_stats);

		// Load scenes in a separate thread
		AlbumLoaderTask sceneLoaderTask = new AlbumLoaderTask(this);
		sceneLoaderTask.execute((Void[]) null);

		// Get the count of scenes to be loaded for the
		// initialization progress bar and report progress
		if (!SceneManager.getInstance().isInitialized()) {
			int sceneCount = UtilityDAO.getSceneCount(getApplicationContext());
			ProgressDialog dialog = new ProgressDialog(this);
			ReportProgressTask progressTask = new ReportProgressTask(dialog,
					sceneCount);
			progressTask.execute((Void[]) null);
		}
	}

	/**
	 * Post processing to render the view after SceneManager has been
	 * initialized
	 */
	@Override
	public void sceneLoaderCallback() {
		ListView lv = (ListView) findViewById(android.R.id.list);
		ScoreboardDAO dao = new ScoreboardDAO(getApplicationContext());
		Cursor cursor = null;
		try {
			dao.open();

			cursor = dao.getAllStats(SceneUtil.getInstalledLanguage(getApplicationContext()));
			startManagingCursor(cursor);
			ListAdapter adapter = new SimpleCursorAdapter(getApplicationContext(),
					R.layout.all_stats_row, cursor, new String[] {
							COLUMN_ALBUM_DESC, COLUMN_LKUP_NAME,
							COLUMN_TIMES_TESTED, COLUMN_BEST_TIME_IN_SEC,
							COLUMN_BEST_SCORE }, new int[] { R.id.tvAlbum,
							R.id.tvScene, R.id.tvTimesTested, R.id.tvBestTime,
							R.id.tvBestScore });
			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					// Start TestingModeActivity with the selected scene
					Cursor cursor = (Cursor) parent.getItemAtPosition(position);
					int albumId = cursor.getInt(0);
					int sceneId = (int) id;

					// Start TestingModeActivity
					Intent intent = new Intent(ScoreboardActivity.this,
							TestingModeActivity.class);
					intent.putExtra(EXTRA_ALBUM_ID, albumId);
					intent.putExtra(EXTRA_SCENE_KEY, sceneId);
					intent.putExtra(EXTRA_GET_SCENE_BY_ID, true);
					startActivityForResult(intent, REQUEST_START_SCENE_ACTIVITY);
				}
			});

		} catch (Exception e) {
			Log.e("ScoreboardActivity.onCreate exception", e.toString());
		} finally {
			// Activity will manage the cursor. Can't close here
		}
	}

	@Override
	protected void onDestroy() {
		// Clear any listeners and adapters
		ListView lv = (ListView) findViewById(android.R.id.list);
		lv.setAdapter(null);
		lv.setOnItemClickListener(null);
		super.onDestroy();
	}
}
