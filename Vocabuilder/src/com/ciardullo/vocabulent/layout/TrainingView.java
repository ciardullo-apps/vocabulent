package com.ciardullo.vocabulent.layout;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;

import com.ciardullo.vocabulent.vo.Hotzone;
import com.ciardullo.vocabulent_it.R;

/**
 * The UI for Training Mode
 */
public class TrainingView extends SceneView {

	/**
	 * Used to position tvAnswer
	 */
	LayoutParams tvLayoutParams;

	public TrainingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TrainingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TrainingView(Context c) {
		super(c);
		init();
	}

	/**
	 * Post constructor initialization
	 */
	private void init() {
		bgColor = COLOR_BG;
		dimColor = COLOR_DIM;
		bgHighlightColor = COLOR_BG_HIGHLIGHT;
		fgHighlightColor = COLOR_FG_HIGHLIGHT;
		
		tvLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
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
				if(!hotzone.equals(selectedHotzone)) {
					// The touched hotzone was not already selected

					// Save the current hotzone
					selectedHotzone = hotzone;

					// Dim the View, made possible via the magic of transparency
					setBackgroundColor(dimColor);
					
					// Redraw the View
					invalidate();
				}
			} else {
				// Touch was out of a hotzone. Clear previous hotzone and redraw.

				if(selectedHotzone != null) {
					offHotzone();
				}
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
		boolean b = false;
		if(selectedHotzone != null) {
			if(compareHotzone != null) {
				if(!selectedHotzone.equals(compareHotzone)) {
					b = true;
					compareHotzone = selectedHotzone;
				}
			} else {
				b = true;
				compareHotzone = selectedHotzone;
			}
		}

		return b;
	}

	@Override
	protected void postHighlightHotzone(Rect viewHotzone) {
		// In Training mode, always show the answer of the touched hotzone
		tvAnswer.setText(selectedHotzone.getAnswer());

		// Show the hotzone and the answer
		ivHotzone.setVisibility(View.VISIBLE);
		tvAnswer.setVisibility(View.VISIBLE);

		int gravity = getAnswerGravity(viewHotzone);
		tvLayoutParams.gravity = gravity;
		tvAnswer.setLayoutParams(tvLayoutParams);

		// Animate the hotzone and the answer
		Animation animHotzone = AnimationUtils.loadAnimation(getContext(), R.anim.hotzone);
		Animation animAnswer = AnimationUtils.loadAnimation(getContext(), R.anim.push_left_in);
		
		ivHotzone.startAnimation(animHotzone);
		tvAnswer.startAnimation(animAnswer);

		// Speak answer after each hotzone is touched in training mode
		speakAnswer();
	}
}
