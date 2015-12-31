package com.ciardullo.vocabulent.layout;

import java.util.Collections;
import java.util.Deque;
import java.util.Stack;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ciardullo.vocabulent.activity.TestingModeActivity;
import com.ciardullo.vocabulent.vo.Hotzone;
import com.ciardullo.vocabulent_it.R;

/**
 * The UI for Training Mode
 */
public class TestingView extends SceneView {
	/**
	 * Contains the checkmark or red x for feedback
	 */
	private ImageView ivFeedback;

	/**
	 * The LinearLayout to hold multipleChoiceView
	 */
	private LinearLayout layoutMultChoice;

	/**
	 * The LinearLayout inflated from multiple_choice.xml
	 */
	private LinearLayout multipleChoiceView;

	public TestingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TestingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TestingView(Context c) {
		super(c);
		init();
	}

	/**
	 * Post constructor initialization
	 */
	private void init() {
		bgColor = COLOR_BG;
		dimColor = COLOR_BG;	// Do not dim in testing mode
		bgHighlightColor = COLOR_BG_HIGHLIGHT;
		fgHighlightColor = Color.RED;
	}

	public void setIvFeedback(ImageView ivFeedback) {
		this.ivFeedback = ivFeedback;
	}

	public void setLayoutMultChoice(LinearLayout layoutMultChoice) {
		this.layoutMultChoice = layoutMultChoice;
	}

	/**
	 * Android multiplies the width and height of the
	 * source bitmap by the density. However, the width
	 * and height of the View are scaled to the device size.
	 * The hotzone coordinates in the database correspond to
	 * the source image width and height, which are not scaled by density.
	 * Touch coordinates correspond to the View, not the Bitmap.
	 * Thus, use View width and height when comparing the hotzone.
	 * Change the hotzone coordinates accordingly.
	 * Determine the ratio between the source bitmap to the scaled 
	 * bitmap then between the scaled bitmap and the view.
	 * XcoordinateInDb * density / mBitmap.getWidth() * View.width
	 * YcoordinateInDb * density / mBitmap.getHeight() * View.height
	*/
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float xF = event.getX();
		float yF = event.getY();
		int x = (int) xF;
		int y = (int) yF;

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:
			if(PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
				// Animate multiple choice
				animateMultipleChoice();
			} else {
				Hotzone hotzone = null;
				for(Hotzone h : theScene.getAllHotzones()) {
					Rect anImageRect = h.getStrikeArea();
					Rect viewHotzone = getViewHotzone(anImageRect);
	
					// If the touch point is in the hotzone, highlight the hotzone
					if(viewHotzone.contains(x, y)) {
						// This hotzone was touched
						hotzone = h;
						break;
					}
				}

				if(hotzone != null) {
					// A hotzone was touched
					// Save the current hotzone
					selectedHotzone = hotzone;
					
					// Is it the correct hotzone?
					if(selectedHotzone.equals(compareHotzone)) {
						// Right
						fgHighlightColor = Color.parseColor("#009503");	// Color.GREEN;
					} else {
						// Wrong
						fgHighlightColor = Color.RED;
					}
				} else {
					// In case answer covers compareHotzone
					FrameLayout.LayoutParams layoutParams =
							(LayoutParams) tvAnswer.getLayoutParams();
					int g = layoutParams.gravity;
					if(g == (Gravity.LEFT | Gravity.TOP))
						g = Gravity.RIGHT | Gravity.BOTTOM ;
					else
						g = Gravity.LEFT | Gravity.TOP;
					layoutParams.gravity = g;
				}

				// Redraw the View
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_DOWN:
			break;
		}

		return true;
	}

	@Override
	protected boolean preHighlightHotzone() {
		// Return true if anything was touched
		boolean b = true;
		if(selectedHotzone == null) {
			b = false;
		}

		// Always highlight after a touch in testing mode
		return b;
	}

	@Override
	protected void postHighlightHotzone(Rect viewHotzone) {
		// Show the hotzone and the answer
		ivHotzone.setVisibility(View.VISIBLE);
		tvAnswer.setVisibility(getAnswerVisibility());
		ivFeedback.setVisibility(View.VISIBLE);

		if(selectedHotzone != null) {
			if(compareHotzone != null) {
				if(!selectedHotzone.equals(compareHotzone)) {
					// Highlight Wrong answer
					compareHotzone.setWrong();

					// Animate the hotzone only
					Animation animHotzone = AnimationUtils.loadAnimation(getContext(), R.anim.hotzone);

					// Vibrate during animation
					// TODO Set a preference for vibrate
					Vibrator vibrator = 
							(Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
					vibrator.vibrate(30);

					ivHotzone.startAnimation(animHotzone);

					// Animate feedback image
					if(PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode)) {
						ivFeedback.setImageResource(R.drawable.no);
						ivFeedback.setVisibility(View.VISIBLE);
						Animation animFeedback = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
						ivFeedback.startAnimation(animFeedback);
					}
				} else {
					// Highlight Right answer, either when chosen correctly
					// or when starting or after choosing a wrong answer in
					// Multiple choice mode.
					
					// Animate the hotzone and the answer
					Animation animHotzone = AnimationUtils.loadAnimation(getContext(), R.anim.hotzone);
					ivHotzone.startAnimation(animHotzone);
					if(PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode)) {
						// This is done in rightAnswer(), so not needed in multiple choice mode
						TestingModeActivity activity = (TestingModeActivity)getContext();
						Hotzone nextHotzone = activity.getNextHotzone();
						if(nextHotzone != null) {
							// Initialize rightAnswerChoice
							setCompareHotzone(nextHotzone);
							Animation animAnswer = AnimationUtils.loadAnimation(getContext(), R.anim.push_left_in);
							tvAnswer.startAnimation(animAnswer);

							// Animate feedback image
							ivFeedback.setVisibility(View.VISIBLE);
							ivFeedback.setImageResource(R.drawable.si);
							Animation animFeedback = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
							ivFeedback.startAnimation(animFeedback);
							
							// Speak answer in touch mode when next hot zone is ready for testing
							speakAnswer();
						} else {
							// Testing complete. Parent will display stats
							setCompareHotzone(null);
							ivHotzone.setVisibility(View.INVISIBLE);
							tvAnswer.setVisibility(View.INVISIBLE);
						}
					}
				}
			}
		}

		// After each touch, clear the selectedHotzone and animate feedback image
		selectedHotzone = null;
		ivFeedback.setVisibility(View.INVISIBLE);
	}


	/**
	 * Overridden so that in multiple choice mode, the choices
	 * are displayed
	 */
	@Override
	public void renderTheScene(int displayWidth, int displayHeight) {
		if(PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
			dimColor = COLOR_DIM;
			bgColor = COLOR_DIM;
		} else {
			dimColor = COLOR_BG;	// Do not dim in hotzone testing mode
			bgColor = COLOR_BG;
		}

		super.renderTheScene(displayWidth, displayHeight);

		if(PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
			renderMultipleChoice();
		}
	}

	/**
	 * Returns an inflated and initialized version of the multiple_choice.xml
	 * layout.
	 * @param firstTime true if called from the activity, false otherwise
	 * @return
	 */
	private void renderMultipleChoice() {
		if(multipleChoiceView == null) {
			// Remove previous View containing multiple choices
//			layoutMultChoice.removeView(multipleChoiceView);
			multipleChoiceView = (LinearLayout) LayoutInflater.from(
					getContext()).inflate(R.layout.multiple_choice, null);
			layoutMultChoice.addView(multipleChoiceView);
		}
		layoutMultChoice.setVisibility(INVISIBLE);

		// Generate the multiple choices
		Stack<TextView> choices = new Stack<TextView>();
		choices.add((TextView) multipleChoiceView.findViewById(R.id.tvChoice1));
		choices.add((TextView) multipleChoiceView.findViewById(R.id.tvChoice2));
		choices.add((TextView) multipleChoiceView.findViewById(R.id.tvChoice3));
		choices.add((TextView) multipleChoiceView.findViewById(R.id.tvChoice4));

		final Hotzone testHotzone = getCompareHotzone();
		Collections.shuffle(choices);
		TextView testTv = choices.pop();
		testTv.setText(testHotzone.getAnswer());
		testTv.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Right answer touched
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
					// Right answer touched
					rightAnswer();
					break;
				case MotionEvent.ACTION_MOVE:
					break;
				case MotionEvent.ACTION_DOWN:
					break;
				}
				return true;
			}
		});

		Deque<Hotzone> randomHotzones = theScene.getHotzonesForTesting();
		while (!choices.isEmpty()) {
			Hotzone hotzone = randomHotzones.pop();
			if(hotzone.equals(testHotzone)) {
				// No repeats
				continue;
			}
			testTv = choices.pop();
			testTv.setText(hotzone.getAnswer());
			// Put the wrong Hotzone as a tag so that it can be highlighted
			testTv.setTag(hotzone);
			testTv.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					switch (event.getAction()) {
					case MotionEvent.ACTION_UP:
						// Wrong answer touched
						testHotzone.setWrong();
						selectedHotzone = (Hotzone)v.getTag();
						fgHighlightColor = Color.RED;

						// Hide the multi choice list, triggering onDraw
						layoutMultChoice.setVisibility(INVISIBLE);
						
						if(!isInEditMode()) {
							DelayShowingMultipleChoice delayTask = new DelayShowingMultipleChoice();
							delayTask.execute(750);
						}
						
						break;
					case MotionEvent.ACTION_MOVE:
						break;
					case MotionEvent.ACTION_DOWN:
						break;
					}
					return true;
				}
			});
			
			// Finally, needed to highlight the correct answer
			selectedHotzone = compareHotzone;
		}

		/**
		 * Need to start the multiple choice test in a separate thread off the UI
		 */
		if(!isInEditMode()) {
			DelayShowingMultipleChoice delayTask = new DelayShowingMultipleChoice();
			delayTask.execute(0);
		}
	}

	/**
	 * A right answer was touched in multiple choice mode.
	 * Get the next one from the TestingModeActivity's Stack
	 */
	private void rightAnswer() {
		TestingModeActivity activity = (TestingModeActivity)getContext();
		Hotzone nextHotzone = activity.getNextHotzone();
		if(nextHotzone != null) {
			// Initialize rightAnswerChoice
			setCompareHotzone(nextHotzone);

			// Animate feedback image
			ivFeedback.setVisibility(View.VISIBLE);
			ivFeedback.setImageResource(R.drawable.check);
			Animation animFeedback = AnimationUtils.loadAnimation(getContext(), R.anim.fade);
			ivFeedback.startAnimation(animFeedback);

			renderMultipleChoice();
		} else {
			// Testing complete
			setCompareHotzone(null);
			ivHotzone.setVisibility(View.INVISIBLE);
			tvAnswer.setVisibility(View.INVISIBLE);
			layoutMultChoice.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Wait 1 second before starting a multiple choice test.
	 * This must be done off the UI thread (ie a separate thread)
	 * because there is a latency issue with onDraw. To see, just call
	 * animageMultipleChoice() from renderMultipleChoice().
	 */
	class DelayShowingMultipleChoice extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			long then = System.currentTimeMillis();
			while(System.currentTimeMillis() - then < params[0])
				;
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			animateMultipleChoice();
		}
		
	}

	/**
	 * Whenever the multiple choice list is animated and displayed
	 * highlight the compareHotzone and animate that too. This is
	 * done by virtue of onDraw being called when the multiple
	 * choice list is animated and by having the selectedHotzone
	 * set equal to the compareHotzone.
	 */
	private void animateMultipleChoice() {
		// Highlight the hotzone when onDraw is called
		selectedHotzone = compareHotzone;
		fgHighlightColor = Color.parseColor("#009503");	// Color.GREEN;

		// Fade in multiple choice
		Animation animFadeChoiceOut = AnimationUtils.loadAnimation(getContext(), R.anim.fadein);
		multipleChoiceView.startAnimation(animFadeChoiceOut);
		layoutMultChoice.setVisibility(VISIBLE);
	}
}