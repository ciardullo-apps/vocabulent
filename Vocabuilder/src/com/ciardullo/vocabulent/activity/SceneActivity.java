package com.ciardullo.vocabulent.activity;

import java.lang.ref.WeakReference;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.layout.SceneView;
import com.ciardullo.vocabulent.util.BitmapUtil;
import com.ciardullo.vocabulent.util.SceneManager;
import com.ciardullo.vocabulent.vo.Album;
import com.ciardullo.vocabulent.vo.Scene;
import com.ciardullo.vocabulent_it.R;

/**
 * Abstract parent of the Training and Testing activities
 */
public abstract class SceneActivity extends Activity implements AppConstants {
	protected Scene theScene;
	protected int albumId;
	private WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the album and scene ids from Intent
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		albumId = extras.getInt(EXTRA_ALBUM_ID);
		Boolean getSceneById = extras.getBoolean(EXTRA_GET_SCENE_BY_ID);
		Integer sceneId = extras.getInt(EXTRA_SCENE_KEY);
		Boolean getSceneByPosition = extras
				.getBoolean(EXTRA_GET_SCENE_BY_POSITION);
		Integer scenePos = extras.getInt(EXTRA_SCENE_KEY);

		// Set the scene name
		SceneManager sceneManager = SceneManager.getInstance();
		Map<Integer, Album> albums = sceneManager.getAlbums();

		Album album = albums.get(albumId);
		if (getSceneByPosition) {
			// Started by SelectSceneActivity
			theScene = album.getSceneAtPosition(scenePos);
		} else if (getSceneById) {
			// Started by ScoreboardActivity
			theScene = album.getSceneById(sceneId);
		}

		TextView tvSceneDesc = (TextView) findViewById(R.id.tvSceneDesc);
		tvSceneDesc.setText(theScene.getSceneDesc());

		// Use a View to display the hotzone so that
		// you can use View animation via XML
		ImageView ivHotzone = (ImageView) findViewById(R.id.ivHotzone);
		ivHotzone.setVisibility(View.INVISIBLE);

		TextView tvAnswer = (TextView) findViewById(R.id.tvAnswer);
		tvAnswer.setVisibility(View.INVISIBLE);

		View progressBar = findViewById(R.id.progressBar);

		SceneView sceneView = (SceneView) findViewById(R.id.theScene);
		sceneView.setIvHotzone(ivHotzone);
		sceneView.setTvAnswer(tvAnswer);
		sceneView.setProgressBar(progressBar);
		sceneView.setTheScene(theScene);

		// Load the image off the UI thread
		BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(sceneView);
		bitmapWorkerTask.execute(theScene.getImageResourceId());
		bitmapWorkerTaskReference = 
				new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
	}

	@Override
	protected void onDestroy() {
		SceneView theSceneView = (SceneView) findViewById(R.id.theScene);
		if(theSceneView != null) {
			theSceneView.cleanUp();
		}
		theScene = null;

		if (bitmapWorkerTaskReference != null) {
			AsyncTask task = bitmapWorkerTaskReference.get();
			if(task != null) {
				boolean b = task.cancel(true);
				bitmapWorkerTaskReference = null;
//				Log.e("Hello", (b ? "Cancelled" : "Could not cancel")
//						+ "BitmapWorker");
			}
		}
		
		// 20120907 Prevent OOM after using table setting scene in Training
		// or Test mode. Happens only in emulator, not on device
		// Reproduce by Training on table setting then
		// Touch tablecloth
		// Touch placemat
		// Back to List of Scenes
		// Choose Living Room
		// Choose Kitchen
		// Kaboom
		// Calling System.gc() prevents OOM
		System.gc();

		super.onDestroy();
	}

	class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
		private final WeakReference<SceneView> sceneViewReference;

		// Get device size
		int displayWidth = 0, displayHeight = 0;

		public BitmapWorkerTask(SceneView sceneView) {
			super();
			sceneViewReference = new WeakReference<SceneView>(sceneView);

			// Get device size
			displayWidth = getResources().getDisplayMetrics().widthPixels;
			displayHeight = getResources().getDisplayMetrics().heightPixels;
		}

		/**
		 * Loads the image in the background. The image is mutable
		 * 
		 * @param params
		 *            contains the resource id of the image
		 */
		@Override
		protected Bitmap doInBackground(Integer... params) {
			Bitmap immutableBitmap = BitmapUtil
					.decodeUnsampledBitmapFromResource(getResources(),
							params[0]);
			// return immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
			return immutableBitmap;
		}

		// Once complete, see if SceneView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (sceneViewReference != null && bitmap != null) {
				final SceneView sceneView = sceneViewReference.get();
				if (sceneView != null) {
					sceneView.setBitmap(bitmap);
					sceneView.renderTheScene(displayWidth, displayHeight);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(!theScene.isPurchased()) {
			TextView tvAnswer = (TextView) findViewById(R.id.tvAnswer);
			tvAnswer.setVisibility(View.INVISIBLE);

			// Show upgrade now
			showDialog(DIALOG_UPGRADE_NOW);
		} else {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			boolean speak = prefs.getBoolean(PREF_SPEAK_RESPONSES, false);
			SceneView sceneView = (SceneView) findViewById(R.id.theScene);
			if(speak) {
				sceneView.startupTextToSpeech(getApplicationContext());
			} else {
				sceneView.shutdownTextToSpeech();
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		switch (id) {
		case DIALOG_STATS:
			dialog.setContentView(R.layout.stats);
			dialog.setCancelable(true);
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// Restart the activity
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
			});
			break;
		case DIALOG_UPGRADE_NOW:
			dialog.setContentView(R.layout.upgrade_now);
			dialog.setCancelable(true);
			
			// Get the album name
			// Set the scene name
			SceneManager sceneManager = SceneManager.getInstance();
			Map<Integer, Album> albums = sceneManager.getAlbums();
			Album album = albums.get(albumId);
			TextView tvUpgradeNow = (TextView)dialog.findViewById(R.id.tvUpgradeNowB);
			String s = getResources().getString(R.string.upgradeNowB);
			String t = s.replace("XXX", album.getAlbumDesc());
			tvUpgradeNow.setText(t);
			
			Button btnUpgrade = (Button)dialog.findViewById(R.id.btnUpgrade);
			btnUpgrade.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					setResult(RESULT_OK_START_BILLING_ACTIVITY);
					finish();
					SceneActivity.this.finish();
				}

			});
			
			dialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
					setResult(RESULT_CANCELED);
					SceneActivity.this.finish();
				}
			});
			break;
		default:
			dialog = null;
		}

		return dialog;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// 0 indicates that the request code will not be returned
		startActivityForResult(new Intent(this, VocabulentPreferenceActivity.class), 0);
		return true;
	}
}
