package com.ciardullo.vocabulent.activity;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.util.SceneManager;
import com.ciardullo.vocabulent.util.SceneUtil;
import com.ciardullo.vocabulent_it.R;

/**
 * Top-level activity controlling the UI
 */
public class StartMenuActivity extends AbstractMenuActivity implements
		AppConstants, TextToSpeech.OnInitListener {

	/**
	 * For speech synthesis
	 */
	protected TextToSpeech mTts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start_menu);
		startupTextToSpeech(getApplicationContext());

//		// Show heap size
//		ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
//		int n = activityManager.getMemoryClass();
//		Log.e("HELLO", "MEMORY IS "  + n);
	}

	@Override
	protected void onResume() {
		super.onResume();
		CheckBox cb = (CheckBox) findViewById(R.id.cbSpeak);
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean b = prefs.getBoolean(PREF_SPEAK_RESPONSES, false);
		cb.setChecked(b);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Clear SceneManager
		SceneManager.getInstance().cleanup();
		
		shutdownTextToSpeech();
	}

	public void onClickTraining(View v) {
		Intent intent = new Intent(this, SelectSceneActivity.class);
		intent.putExtra(EXTRA_MODE, MODE_TRAINING);
		startActivity(intent);
	}

	public void onClickTesting(View v) {
		Intent intent = new Intent(this, SelectSceneActivity.class);
		intent.putExtra(EXTRA_MODE, MODE_TESTING);
		startActivity(intent);
	}

	public void onClickScores(View v) {
		Intent intent = new Intent(this, ScoreboardActivity.class);
		startActivity(intent);
	}

	public void onClickPrefSpeak(View v) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor edit = prefs.edit();
		CheckBox cb = (CheckBox) v;
		edit.putBoolean(PREF_SPEAK_RESPONSES, cb.isChecked());
		edit.commit();

		if (cb.isChecked()) {
			speakWelcome();
		}
	}

	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		boolean problem = false;
		if (status == TextToSpeech.SUCCESS) {
			Locale locale = SceneUtil
					.getLocaleForSpeech(getApplicationContext());
			int result = 0;
			if (locale != null) {
				result = mTts.setLanguage(locale);
			} else {
				problem = true;
			}

			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				problem = true;
			} else {
				// TODO Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.
			}
		} else {
			problem = true;
		}

		if (problem) {
			// Initialization failed, Locale is null, or
			// Lanuage data is missing or the language is not supported.
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.languageNotAvail),
					Toast.LENGTH_SHORT);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor edit = prefs.edit();
			edit.putBoolean(PREF_SPEAK_RESPONSES, false);
			edit.commit();
		}
	}

	protected void speakWelcome() {
		// The TTS engine has been successfully initialized.
		if (mTts != null) {
			mTts.speak(
					String.valueOf(getResources().getString(R.string.welcome)),
					TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	protected void startupTextToSpeech(Context context) {
		// Initialize TextToSpeech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		if (mTts == null)
			mTts = new TextToSpeech(context, this);
	}

	protected void shutdownTextToSpeech() {
		// Shutdown TextToSpeech
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		mTts = null;
	}
}
