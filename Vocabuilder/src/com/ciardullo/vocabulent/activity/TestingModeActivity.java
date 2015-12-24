package com.ciardullo.vocabulent.activity;

import java.text.NumberFormat;
import java.util.Deque;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.dao.ScoreboardDAO;
import com.ciardullo.vocabulent.layout.TestingView;
import com.ciardullo.vocabulent.vo.Hotzone;
import com.ciardullo.vocabulent.vo.ScoreCard;
import com.ciardullo.vocabulent_it.R;

public class TestingModeActivity extends SceneActivity implements AppConstants {

	/**
	 * Time test started in millisec
	 */
	long startTimeInMs;
	
	/**
	 * Elapsed test time in sec
	 */
	int elapsedTimeInSec;

	/**
	 * The hotzones in theScene, in a randomly ordered stack
	 */
	Deque<Hotzone> stackHotzones;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.testing_mode);
		super.onCreate(savedInstanceState);

		// super.onCreate() has been called and the View instantiated
		// It is now OK to access the View's members
		TestingView testingView = (TestingView) findViewById(R.id.theScene);

		// TODO Use a preferences menu item
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String testMode = prefs.getString(PREF_TESTING_MODE,
				PREF_TESTING_MODE_TOUCH_HOTZONE);
		testingView.setTestMode(testMode);

		// Set the feedback ImageView in TestingView
		ImageView ivFeedback = (ImageView) findViewById(R.id.imageFeedback);
		testingView.setIvFeedback(ivFeedback);
		ivFeedback.setVisibility(View.INVISIBLE);

		if (PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode)) {
		} else if (PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
			LinearLayout layoutMultChoice = 
					(LinearLayout) findViewById(R.id.layoutMultChoice);
			testingView.setLayoutMultChoice(layoutMultChoice);
		}

		// Get a stack of the hotzones in random order
		stackHotzones = theScene.getHotzonesForTesting();

		// Get the first hotzone to test
		Hotzone hotzone = getNextHotzone();

		// Initialize the View with the first hotzone to test
		testingView.setCompareHotzone(hotzone);

		if (PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode)) {
		} else if (PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
			LinearLayout layoutMultChoice = 
					(LinearLayout) findViewById(R.id.layoutMultChoice);
			testingView.setLayoutMultChoice(layoutMultChoice);
		}
		
		elapsedTimeInSec = 0;
	}

	/**
	 * Pops the next hotzone of the stack of stackHotzones
	 * 
	 * @return
	 */
	public Hotzone getNextHotzone() {
		Hotzone hotzone = null;
		if (!stackHotzones.isEmpty()) {
			hotzone = (Hotzone) stackHotzones.pop();
//			Log.e("getNextHotzone()", hotzone.getAnswer());
		} else {
			// Show statistics
			showDialog(DIALOG_STATS);
		}
		return hotzone;
	}

	@Override
	protected void onPrepareDialog(int id, final Dialog dialog) {
		switch (id) {
		case DIALOG_STATS:
			// Count the number of wrong answers
			int wrong = 0;
			for (Hotzone h : theScene.getAllHotzones()) {
				if (!h.isRight())
					wrong++;

				// Revert hotzone to default
				h.setRight();
			}

			// Save scoreboard table
			int total = theScene.getAllHotzones().size();
			double score = (double) (total - wrong) / total;
			elapsedTimeInSec += Math
					.round((System.currentTimeMillis() - startTimeInMs) / 1000);

			ScoreCard rc = saveScoreCard(wrong, theScene.getAllHotzones()
					.size(), (int)Math.round(score*100));

			TextView tvResults = (TextView) dialog
					.findViewById(R.id.tvResults);
			StringBuffer sb = new StringBuffer();
			sb.append(String.valueOf(total - wrong));
			sb.append(" out of ");
			sb.append(String.valueOf(total));
			tvResults.setText(sb.toString());

			// TODO Use locale
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMaximumFractionDigits(0);
			nf.setMinimumIntegerDigits(1);

			TextView tvScore = (TextView) dialog.findViewById(R.id.tvScore);
			tvScore.setText(nf.format(score));

			TextView tvBestScore = (TextView) dialog.findViewById(R.id.tvBestScore);
			String s = String.valueOf(rc.getPrev_best_score()) + "%";
			if(rc.getPrev_best_score() <= 0)
				s = "--";
			tvBestScore.setText(s);
			
			TextView tvTime = (TextView) dialog.findViewById(R.id.tvTime);
			s = String.valueOf(elapsedTimeInSec);
			tvTime.setText(s + " sec");

			TextView tvBestTime = (TextView) dialog.findViewById(R.id.tvBestTime);
			s = String.valueOf(rc.getPrev_best_time_in_sec());
			if(rc.getPrev_best_time_in_sec() <= 0) 
				s = "--";
			tvBestTime.setText(s + " sec");

			dialog.setCanceledOnTouchOutside(true);
			TextView tv = (TextView) dialog.findViewById(R.id.tvStatsTitle);
			long roundedScore = Math.round(score * 100);
			if (roundedScore > 90)
				tv.setText(R.string.fantastic);
			else if (roundedScore > 80)
				tv.setText(R.string.veryGood);
			else if (roundedScore > 70)
				tv.setText(R.string.niceJob);
			else if (roundedScore > 60)
				tv.setText(R.string.keepTrying);
			else
				tv.setText(R.string.keepPracticing);
			
			break;
		default:
			;
		}
	}
	
	/**
	 * Called after startActivityForResult() from onOptionsItemSelected()
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == PREF_RESULT_TESTING_MODE_CHANGED) {
			// The testing mode preference was changed.
			// Restart the activity
			Intent intent = getIntent();
			finish();
			startActivity(intent);
		}
	}
	
	/**
	 * Updates statistics in the scoreboard table after a test
	 */
	private ScoreCard saveScoreCard(int wrongCt, int hotzoneCt, int score) {
		ScoreboardDAO dao = new ScoreboardDAO(getApplicationContext(), albumId, theScene.getTheId());
		ScoreCard rc = dao.getScoreCard();
		rc.setTime_in_sec(elapsedTimeInSec);
		rc.setTimes_tested(rc.getTimes_tested()+1);
		rc.setScore(score);
		
		dao.saveScoreCard(rc);
		return rc;
	}

	@Override
	protected void onPause() {
		if(startTimeInMs > 0) {
			elapsedTimeInSec += Math
					.round((System.currentTimeMillis() - startTimeInMs) / 1000);
			startTimeInMs = 0;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		startTimeInMs = System.currentTimeMillis();
		
		if(!theScene.isPurchased()) {
			LinearLayout layoutMultChoice = 
					(LinearLayout) findViewById(R.id.layoutMultChoice);
			if(layoutMultChoice != null)
				layoutMultChoice.setVisibility(View.INVISIBLE);
		}

		// Be sure to call super
		super.onResume();
	}

}
