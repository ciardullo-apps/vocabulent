package com.ciardullo.vocabulent.activity;

import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.dao.UtilityDAO;
import com.ciardullo.vocabulent.layout.ThumbnailAdapter;
import com.ciardullo.vocabulent.thread.AlbumLoaderTask;
import com.ciardullo.vocabulent.thread.ReportProgressTask;
import com.ciardullo.vocabulent.util.SceneManager;
import com.ciardullo.vocabulent.util.SceneUtil;
import com.ciardullo.vocabulent.vo.Album;
import com.ciardullo.vocabulent_it.R;

/**
 * The activity to select a scene for training or testing
 */
public class SelectSceneActivity extends AbstractLoadSceneActivity implements
		AppConstants {

	/**
	 * Testing or Training
	 */
	private int mode;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_scene);

		// While scenes are loading, set background to orange
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setBackgroundColor(COLOR_APP_BG);

		// Get the mode from the Intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		mode = extras.getInt(EXTRA_MODE);

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
		// Create a tab for each album
		final TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		// Revert to black needed after progress dialog
		tabHost.setBackgroundColor(Color.BLACK);
		tabHost.setup();

		// Change the background color of the tab indicator
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
					View tabChild = tabHost.getTabWidget().getChildAt(i);
					tabChild.setBackgroundColor(Color.parseColor("#000000")); // unselected
					TextView tv = (TextView) tabChild
							.findViewById(android.R.id.title);
					tv.setTextColor(Color.GRAY);
				}
				View tabChild = tabHost.getTabWidget().getChildAt(
						tabHost.getCurrentTab());
				tabChild.setBackgroundColor(Color.parseColor("#FFA441")); // selected
				TextView tv = (TextView) tabChild
						.findViewById(android.R.id.title);
				tv.setTextColor(Color.parseColor("#000099"));
			}
		});

		// Get the different Albums
		SceneManager sceneManager = SceneManager.getInstance();
		Map<Integer, Album> albums = sceneManager.getAlbums();

		// Render a thumbnail for each scene in the Album
		for (Map.Entry<Integer, Album> entry : albums.entrySet()) {
			// Create a new tab and gallery for each scene collection
			final Album album = entry.getValue();

			// Render each tab with the Album image and name
			TabSpec tabSpec = tabHost.newTabSpec(album.getAlbumDesc());
			// method 1 setIndicator()
			tabSpec.setIndicator(
					album.getAlbumDesc(),
					getResources().getDrawable(
							SceneUtil.getImageResourceId(R.drawable.class,
									album.getImageName())));

			// method 2 LinearLayout
			// View viewTab = View.inflate(SelectSceneActivity.this,
			// R.layout.mini_scene, null);
			// ImageView ivTab =
			// (ImageView)viewTab.findViewById(R.id.ivMiniScene);
			// ivTab.setImageDrawable(getResources().getDrawable(
			// SceneUtil.getImageResourceId(R.drawable.class,
			// album.getImageName())));
			// TextView tvTab =
			// (TextView)viewTab.findViewById(R.id.tvMiniScene);
			// tvTab.setText(album.getAlbumDesc());
			// tabSpec.setIndicator(viewTab);

			// Render the image gallery of scenes in a GridView
			tabSpec.setContent(new TabHost.TabContentFactory() {
				public View createTabContent(String tag) {
					View galleryAlbum = View.inflate(SelectSceneActivity.this,
							R.layout.album, null);
					// Gallery gallery = (Gallery)
					// album.findViewById(R.id.galleryAlbum);
					GridView gallery = (GridView) galleryAlbum
							.findViewById(R.id.galleryAlbum);
					// GridView gallery = (GridView)
					// View.inflate(SelectSceneActivity.this, R.layout.album,
					// null);
					gallery.setAdapter(new ThumbnailAdapter(
							SelectSceneActivity.this, album));

					gallery.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent, View v,
								int position, long id) {
							Intent intent = null;
							switch (mode) {
							case MODE_TESTING:
								intent = new Intent(SelectSceneActivity.this,
										TestingModeActivity.class);
								break;
							case MODE_TRAINING:
							default:
								intent = new Intent(SelectSceneActivity.this,
										TrainingModeActivity.class);
								break;
							}

							intent.putExtra(EXTRA_ALBUM_ID, album.getTheId());
							intent.putExtra(EXTRA_SCENE_KEY, position);
							intent.putExtra(EXTRA_GET_SCENE_BY_POSITION, true);
							startActivityForResult(intent, REQUEST_START_SCENE_ACTIVITY);
						}
					});

					return galleryAlbum;
				}
			});

			tabHost.addTab(tabSpec);
		}
	}

	@Override
	protected void onDestroy() {
		// Clear any listeners and adapters
		GridView gallery = (GridView) findViewById(R.id.galleryAlbum);
		// Fix exception from 2012-08-31
		if(gallery != null) {
			gallery.setAdapter(null);
			gallery.setOnItemClickListener(null);
		}
		TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
		if(tabHost != null)
			tabHost.setOnTabChangedListener(null);

		super.onDestroy();
	}

}