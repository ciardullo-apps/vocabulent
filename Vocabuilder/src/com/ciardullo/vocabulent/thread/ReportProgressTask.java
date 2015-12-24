package com.ciardullo.vocabulent.thread;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ciardullo.vocabulent.util.SceneManager;

/**
 * Reports progress of scene loading
 */
public class ReportProgressTask extends AsyncTask<Void, Integer, Void> {
	private int maxSceneCount;
	private ProgressDialog progressDialog;

	public ReportProgressTask(ProgressDialog dialog, int sceneCount) {
		super();
		this.maxSceneCount = sceneCount;
		this.progressDialog = dialog;
	}

	@Override
	protected void onPreExecute() {
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("Loading scenes");
		progressDialog.setMax(maxSceneCount);
		progressDialog.show();
	}

	/**
	 * Load all scenes
	 */
	@Override
	protected Void doInBackground(Void... params) {
		SceneManager sceneManager = SceneManager.getInstance();
		synchronized (sceneManager) {
			do {
				// Initialization still in progress
				int scenesLoadedThusFar = sceneManager.getCountOfLoadedScenes();
				publishProgress(scenesLoadedThusFar);
				try {
					if(!sceneManager.isInitialized())
						sceneManager.wait();
				} catch (InterruptedException e) {
					Log.e("Threading Exception", "ReportProgressTask was interrupted");
				}
			} while(!sceneManager.isInitialized());
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setProgress(values[0]);
	}

	/**
	 * All scenes have been loaded. Build the tabs for SelectSceneActivity.
	 */
	@Override
	protected void onPostExecute(Void result) {
		if(progressDialog.isShowing())
			progressDialog.dismiss();
	}
}