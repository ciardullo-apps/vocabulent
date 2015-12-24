package com.ciardullo.vocabulent.thread;

import android.os.AsyncTask;

import com.ciardullo.vocabulent.activity.AbstractLoadSceneActivity;
import com.ciardullo.vocabulent.util.SceneManager;

/**
 * Loads albums, scenes, and hotzones from the database in a separate thread.
 */
public class AlbumLoaderTask extends AsyncTask<Void, Integer, Void> {
	private AbstractLoadSceneActivity context;

	public AlbumLoaderTask(AbstractLoadSceneActivity context) {
		super();
		this.context = context;
	}

	/**
	 * Load all scenes
	 */
	@Override
	protected Void doInBackground(Void... params) {
		SceneManager sceneManager = SceneManager.getInstance();
		sceneManager.initialize(context);
		return null;
	}

	/**
	 * All scenes have been loaded. Build the tabs for SelectSceneActivity.
	 */
	@Override
	protected void onPostExecute(Void result) {
		context.sceneLoaderCallback();
	}
}
