package com.ciardullo.vocabulent.util;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Contains utility methods as they pertain to Android layouts
 */
public class LayoutUtil {
	/**
	 * Returns a TextView instance with default attributes set
	 * @param context
	 * @return
	 */
	public static TextView getATextView(Context context) {
		TextView tv = new TextView(context);
		tv.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PT, 4);
		tv.setTextColor(Color.BLACK);
		tv.setPadding(
				tv.getPaddingLeft() + 50, 
				tv.getPaddingTop() + 5,
				tv.getPaddingRight(), 
				tv.getPaddingBottom() + 5);

		return tv;
	}
}
