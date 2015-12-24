package com.ciardullo.vocabulent.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent_it.R;

public class VocabulentPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, AppConstants {
	private ListPreference mListPreference;
	boolean testModeChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		mListPreference = (ListPreference) getPreferenceScreen()
				.findPreference(PREF_TESTING_MODE);
		String s = getTestModePrefSummary();
		mListPreference.setSummary(s);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		testModeChanged = true;
		String s = getTestModePrefSummary();
		mListPreference.setSummary(s);
		onContentChanged();
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	private String getTestModePrefSummary() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String testMode = prefs.getString(PREF_TESTING_MODE, PREF_TESTING_MODE_TOUCH_HOTZONE);

		String s = null;
		if(PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode))
			s = "Touch the Item";
		else if(PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode))
			s = "Multiple Choice";
		
		return s;
	}

	@Override
	public void finish() {
		if(testModeChanged)
			setResult(PREF_RESULT_TESTING_MODE_CHANGED);
		super.finish();
	}

}