package com.ciardullo.vocabulent.activity;


/**
 * Used by AlbumLoaderTask for post initialization processing, which
 * depends on the activity
 */
public abstract class AbstractLoadSceneActivity extends AbstractMenuActivity {
	/**
	 * Post processing to render the view after SceneManager has been
	 * initialized
	 */
	public abstract void sceneLoaderCallback();
}
