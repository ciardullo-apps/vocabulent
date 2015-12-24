package com.ciardullo.vocabulent.layout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Supports multiple choice testing of a highlighted hotzone
 */
public class TestingChoiceView extends TestingView {

	public TestingChoiceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TestingChoiceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TestingChoiceView(Context c) {
		super(c);
		init();
	}

	@Override
	protected boolean preHighlightHotzone() {
		return false;
	}

	@Override
	protected void postHighlightHotzone(Rect viewHotzone) {
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
}
