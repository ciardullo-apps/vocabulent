package com.ciardullo.vocabulent.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utility methods for Bitmap management
 */
public class BitmapUtil {

	/**
	 * Decodes a bitmap from a resource id without changing sample size
	 * @param res
	 * @param resId
	 * @return
	 */
	public static Bitmap decodeUnsampledBitmapFromResource(Resources res,
			int resId) {
		BitmapFactory.Options options = getBitmapFactoryOptions(res, resId);
		
		// For now, do not subsample the image to acheive a smaller footprint.
		// A value of 1 will load the full image and that is OK because all 
		// images are in res/raw with a resolution of 940x640.
//		int displayWidth = res.getDisplayMetrics().widthPixels;
//		int displayHeight = res.getDisplayMetrics().heightPixels;
//		options.inSampleSize = calculateInSampleSize(options,displayWidth,displayHeight);

		// Decode bitmap and load into memory
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Decodes a bitmap from a resource id using a sample size
	 * @param res
	 * @param resId
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Resources res,
			int resId, int reqWidth, int reqHeight) {
		BitmapFactory.Options options = getBitmapFactoryOptions(res, resId);
		
		options.inSampleSize = calculateInSampleSize(options,reqWidth,reqHeight);

		// Decode bitmap and load into memory
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}

	/**
	 * Optimizes the sample size of the bitmap based on the dimensions
	 * of the target view to render it. In this app, the target is SceneView,
	 * which fully occupies width in portrait and height in landscape.
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		
		return inSampleSize;
	}

	/**
	 * Decodes the bitmap without loading into memory and returns
	 * the BitmapFactory.Options
	 * @param res
	 * @param resId
	 * @return
	 */
	public static BitmapFactory.Options getBitmapFactoryOptions(Resources res,
			int resId) {
		// First decode with inJustDecodeBounds=true to check dimensions
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		return options;
	}
}
