package com.ciardullo.vocabulent.layout;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ciardullo.vocabulent.AppConstants;
import com.ciardullo.vocabulent.util.SceneUtil;
import com.ciardullo.vocabulent.vo.Hotzone;
import com.ciardullo.vocabulent.vo.Scene;
import com.ciardullo.vocabulent_it.R;

/**
 * Models the view used for the scene. When a hotzone is touched,
 * a copy of the hotzone (subset of the original bitmap) is drawn
 * in onDraw(). This copy is scaled using the scale factor in a
 * Matrix and positioned using the coordinates of the View, which 
 * differ from the coordinates of the Bitmap. See onTouchEvent()
 * for more details.
 * 
 * Note that bitmaps are not subsampled according to
 * http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
 * because they are always 940x640 and subsampling to fit the device
 * dimensions will always yield a value for BitmapFactoryOptions.inSampleSize
 * of 1, which means no change.
 */
public abstract class SceneView extends View implements AppConstants, TextToSpeech.OnInitListener {

	protected int bgColor;
	protected int dimColor;
	protected int fgHighlightColor;
	protected int bgHighlightColor;
	protected float density;
	protected float scaleWidth, scaleHeight;
	protected Matrix matrix;
	protected Paint paint;
	protected Paint paintHotzone;
	protected LayoutParams layoutParams;
	protected View progressBar;

	/**
	 * The Bitmap of the image resource
	 */
	protected Bitmap mBitmap;
	
	/**
	 * The currently selected hotzone.
	 */
	protected Hotzone selectedHotzone;
	
	/**
	 * For TrainingView, the previously selected hotzone.
	 * For TestingView, the correct hotzone.
	 */
	protected Hotzone compareHotzone;
	
	/**
	 * The scene being rendered
	 */
	protected Scene theScene;

	/**
	 * The ImageView to contain the hotzone (in the layout)
	 */
	protected ImageView ivHotzone;

	/**
	 * The TextView to contain the answer (in the layout)
	 */
	protected TextView tvAnswer;
	
	/**
	 * In order to scale the bitmap properly in Landscape, need
	 * to compensate for the heights of the other Views that 
	 * require vertical space. Height space is at a premium in
	 * landscape orientation.
	 * TODO Change display height if other Views are added to the layout
	 * For now, only layoutHeader takes up vertical space in Landscape
	 * and its height is 33dp.
	 * N.B. You cannot set heightComp from the Activity because the
	 * height of the layoutHeader is not determined until after 
	 * onCreate(), onStart() and onResume() are complete. So it's 
	 * hardcoded here
	 */
	protected int heightComp = 35;

	/**
	 * From SharedPreferences key
	 */
	protected String testMode;

	/**
	 * For speech synthesis
	 */
	protected TextToSpeech mTts;
	
	public SceneView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SceneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SceneView(Context c) {
		super(c);
		init();
	}

	public void setTvAnswer(TextView tvAnswer) {
		this.tvAnswer = tvAnswer;
	}

	public void setIvHotzone(ImageView ivHotzone) {
		this.ivHotzone = ivHotzone;
	}
	
	public void setTheScene(Scene theScene) {
		this.theScene = theScene;
	}

	public void setBitmap(Bitmap mBitmap) {
		this.mBitmap = mBitmap;
	}

	public void setProgressBar(View progressBar) {
		this.progressBar = progressBar;
	}

	/**
	 * TestingView uses compareHotzone as the correct hotzone.
	 * @param compareHotzone
	 */
	public void setCompareHotzone(Hotzone compareHotzone) {
		this.compareHotzone = compareHotzone;
		tvAnswer.setVisibility(getAnswerVisibility());
		if(this.compareHotzone != null) {
			tvAnswer.setText(compareHotzone.getAnswer());
		}
	}

	public Hotzone getCompareHotzone() {
		return compareHotzone;
	}

	public void setTestMode(String testMode) {
		this.testMode = testMode;
	}

	/**
	 * Post constructor initialization
	 */
	private void init() {
		// Initialize objects needed by Canvas
		paint = new Paint();
		paint.setFilterBitmap(true);

		// Needed for transparent rounded corners of the hotzone
		int color = 0xff424242;
		paintHotzone = new Paint();
		paintHotzone.setAntiAlias(true);
		paintHotzone.setColor(color);
		paintHotzone.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));

		// Use the density for scaling
		// Hot zones must be scaled from px (pixels) to 
		// dp (density-independent) pixels using:
		// px = dp x (dpi/160), where dpi is the device-density
		density = getResources().getDisplayMetrics().density;

		// Allows visual editing in Eclipse
		if(isInEditMode()) {
			// Get device size
			int displayWidth = getResources().getDisplayMetrics().widthPixels;
			int displayHeight = getResources().getDisplayMetrics().heightPixels;
			int resourceId = R.raw.bathroom;
			setBitmap(BitmapFactory.decodeResource(getResources(), resourceId));
			renderTheScene(displayWidth, displayHeight);
		}
	}

	/**
	 * Initializes the view dimensions and scale based on
	 * the device display size and the orientation
	 * @param displayWidth
	 * @param displayHeight
	 */
	public void renderTheScene(int displayWidth, int displayHeight) {
		// TODO May not be able to rely on orientation attribute. If not, use width/height
		int lpWidth = 0, lpHeight=0;
		switch(getResources().getConfiguration().orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			displayHeight-=heightComp;	// Needed to scale properly in landscape
			scaleHeight = (float)displayHeight/mBitmap.getHeight();
			scaleWidth = scaleHeight;
			lpWidth = Math.round(mBitmap.getWidth()*displayHeight/mBitmap.getHeight());
			lpHeight = displayHeight;
			break;
		case Configuration.ORIENTATION_PORTRAIT:
		default:
			scaleWidth = (float)displayWidth/mBitmap.getWidth();
			scaleHeight = scaleWidth;
			lpWidth = displayWidth;
			lpHeight = Math.round(mBitmap.getHeight()*displayWidth/mBitmap.getWidth());
		}
		layoutParams = new LayoutParams(lpWidth, lpHeight);

		// When no hotzone is selected, white background shows through transparent pixels
		setBackgroundColor(bgColor);

		// Need to scale bitmap manually because we are using
		// a raw resource and a generic View class SceneView
		matrix = new Matrix();
		matrix.setScale(scaleWidth, scaleHeight);

		if(!isInEditMode()) {
			// Hide the progress bar
			progressBar.setVisibility(View.GONE);
			tvAnswer.setMaxWidth(getWidth()*2/3);
			tvAnswer.setMaxHeight(getHeight()*2/3);
		}

		// Speak answer in touch test mode right after scene is rendered
		if(PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode))
			speakAnswer();
//		Log.e("View width:height", getWidth()+":"+getHeight());
//		Log.e("Display width:height", displayWidth+":"+displayHeight);
//		Log.e("Bitmap width:height", mBitmap.getWidth()+":"+mBitmap.getHeight());
//		Log.e("Scales width:height", scaleWidth+":"+scaleHeight);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if(mBitmap != null) {
			canvas.drawBitmap(mBitmap, matrix, paint);
			setLayoutParams(layoutParams);
			if(preHighlightHotzone())
				highlightHotzone(canvas);
		}
	}

	/**
	 * Performs the logic needed before calling highlightHotzone
	 * @return
	 */
	protected abstract boolean preHighlightHotzone();
	
	/**
	 * Performs post touch processing like animation
	 */
	protected abstract void postHighlightHotzone(Rect viewHotzone);

	/**
	 * Called by onDraw().
	 * Using the provided Canvas, copies pixels from the bitmapHotzone
	 * (scaled using matrix), highlights with red and white, 
	 * and draws the bitmap at the x,y coordinates in viewHotzone.
	 * @param canvas
	 */
	protected void highlightHotzone(Canvas canvas) {
		// The zone to colorize pixels is based on the Bitmap dimensions
		Rect bitmapHotzone = getBitmapHotzone(selectedHotzone.getStrikeArea());

		// Create a new bitmap with a copy of the bitmapHotzone region,
		// scaled using matrix
		Bitmap littleBitmap = Bitmap.createBitmap(mBitmap, 
				bitmapHotzone.left, bitmapHotzone.top, 
				bitmapHotzone.width(), bitmapHotzone.height(), 
				matrix, true);
		
		// Make the bitmap copy mutable to allow for highlighting
		Bitmap littleMutableBitmap = littleBitmap.copy(Bitmap.Config.ARGB_8888, true);
		
		// Highlight by changing pixel colors
		int[] pixels = new int[littleMutableBitmap.getWidth() * littleMutableBitmap.getHeight()];
		littleMutableBitmap.getPixels(pixels, 0, littleMutableBitmap.getWidth(), 0, 0,
				littleMutableBitmap.getWidth(), littleMutableBitmap.getHeight());
		for(int i = 0; i < pixels.length; i++) {
			int color = pixels[i];
			switch(color) {
			case Color.TRANSPARENT:
				pixels[i] = bgHighlightColor;
				break;
			default:
				pixels[i] = fgHighlightColor;
			}
		}
		littleMutableBitmap.setPixels(pixels, 0, littleMutableBitmap.getWidth(), 0, 0,
				littleMutableBitmap.getWidth(), littleMutableBitmap.getHeight());
		
		// Draw the bitmap copy, which has been scaled and pixel-colored for highlighting
//		canvas.drawBitmap(littleMutableBitmap, viewHotzone.left, viewHotzone.top, paint);
		
		Bitmap roundContainer = getRoundedCornerBitmap(littleMutableBitmap);
		// Instead of drawing the bitmap directly onto SceneView,
		// use an ImageView so that you can use View animation
//		canvas.drawBitmap(roundContainer, viewHotzone.left, viewHotzone.top, paint);
		ivHotzone.setImageBitmap(roundContainer);

		// The zone to colorize pixels is based on the Bitmap dimensions
		Rect viewHotzone = getViewHotzone(selectedHotzone.getStrikeArea());

		// Since layout uses a FrameLayout, we cannot place Views at specific
		// locations (unless you use the deprecated AbsoluteLayout).
		// Use padding (you cannot use margins if the View is not on the first half of its parent
		ivHotzone.setPadding(viewHotzone.left, viewHotzone.top, 0, 0);

		// Display answer depending on location of ImageView

		postHighlightHotzone(viewHotzone);
}

	/**
	 * Before using a new scaled, subset Bitmap in highlightHotzone(),
	 * this code was used to highlight pixels in the same, original 
	 * Bitmap. Decided that using a copy was more efficient.
	 * @deprecated
	 */
	protected void highlightPixels() {
		// If using pixel manipulation below, leave bitmapHotzone set as above
//		bitmapHotzone = viewHotzone;

		// The zone to colorize pixels is based on the Bitmap dimensions
		Rect bitmapHotzone = getBitmapHotzone(selectedHotzone.getStrikeArea());

		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());
		
		for (int i = bitmapHotzone.left; i < bitmapHotzone.right; i++) {
			for (int j = bitmapHotzone.top; j < bitmapHotzone.bottom; j++) {
				int color = pixels[(j * mBitmap.getWidth()) + i];
				switch(color) {
				case Color.TRANSPARENT:
					pixels[(j * mBitmap.getWidth()) + i] = Color.WHITE;
					break;
				default:
					pixels[(j * mBitmap.getWidth()) + i] = Color.RED;
				}
			}
		}
		mBitmap.setPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());
	}
	
	/**
	 * Logs a list of distinct colors in the source bitmap,
	 * along with a count of the number of pixels.
	 */
	@SuppressLint({ "UseSparseArrays", "UseValueOf" })
	protected void countColors() {
		// Get the count of distinct colors in the image
		int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
		mBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0,
				mBitmap.getWidth(), mBitmap.getHeight());
		Map<Integer, Integer> colors = new HashMap<Integer, Integer>();
		for (int i = 0; i < mBitmap.getWidth(); i++) {
			for (int j = 0; j < mBitmap.getHeight(); j++) {
				Integer color = pixels[i * j];
				Integer count = colors.get(color);
				if (count == null)
					count = new Integer(1);
				else
					count++;
				colors.put(color, count);
			}
		}
		for (Integer color : colors.keySet()) {
			Log.e("COLOR is", "" + color + ":" + colors.get(color));
		}

	}

	/**
	 * Returns a new Bitmap with rounded corners displaying
	 * the parameter bitmap therein.
	 * @param bitmap
	 * @return
	 */
	public Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		int color = 0xff424242;
		Paint paintHotzone = new Paint();
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);
		float roundPx = 12;

		paintHotzone.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paintHotzone.setColor(color);

		canvas.drawRoundRect(rectF, roundPx, roundPx, paintHotzone);
		paintHotzone.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paintHotzone);

		return output;
	}

	/**
	 * Returns a Rect that is appropriately scaled for the View
	 * hotzone, using another Rect (imageRect) as a reference
	 * @param r
	 * @return
	 */
	protected Rect getViewHotzone(Rect r) {
		int viewWidth = getWidth();
		int viewHeight = getHeight();
		Rect v = new Rect(
				Math.round(r.left*density/mBitmap.getWidth()*viewWidth),
				Math.round(r.top*density/mBitmap.getHeight()*viewHeight), 
				Math.round(r.right*density/mBitmap.getWidth()*viewWidth), 
				Math.round(r.bottom*density/mBitmap.getHeight()*viewHeight));
		return v;
	}

	/**
	 * Returns a Rect that is appropriately scaled for the Bitmap
	 * hotzone, using another Rect (imageRect) as a reference
	 * @param r
	 * @return
	 */
	protected Rect getBitmapHotzone(Rect r) {
		Rect b = new Rect(
				Math.round(r.left*density),
				Math.round(r.top*density), 
				Math.round(r.right*density), 
				Math.round(r.bottom*density));
		return b;
	}
	
	/**
	 * Determines the gravity of the answer TextView so
	 * that it aligns near the hotzone
	 * @param viewHotzone
	 * @return
	 */
	protected int getAnswerGravity(Rect viewHotzone) {
		// In GingerBread, need to use gravity for TextView placement inside FrameLayout
		// Divide the View into 9 zones to determine where to place the answer
		Rect topLeftZone = new Rect(0, 0, getWidth()/3, getHeight()/3);
		Rect centerLeftZone = new Rect(0, getHeight()/3, getWidth()/3, getHeight()*2/3);
		Rect bottomLeftZone = new Rect(0, getHeight()*2/3, getWidth()/3, getHeight());
		Rect topMiddleZone = new Rect(getWidth()/3, 0, getWidth()*2/3, getHeight()/3);
		Rect centerMiddleZone = new Rect(getWidth()/3, getHeight()/3, getWidth()*2/3, getHeight()*2/3);
		Rect bottomMiddleZone = new Rect(getWidth()/3, getHeight()*2/3, getWidth()*2/3, getHeight());
		Rect topRightZone = new Rect(getWidth()*2/3, 0, getWidth(), getHeight()/3);
		Rect centerRightZone = new Rect(getWidth()*2/3, getHeight()/3, getWidth(), getHeight()*2/3);
		Rect bottomRightZone = new Rect(getWidth()*2/3, getHeight()*2/3, getWidth(), getHeight());
		
		int n = Gravity.NO_GRAVITY;

		if(topLeftZone.contains(viewHotzone)) {
			n = Gravity.CENTER | Gravity.TOP;
		} else if(bottomLeftZone.contains(viewHotzone)) {
			n = Gravity.CENTER | Gravity.BOTTOM;
		} else if(centerLeftZone.contains(viewHotzone)) {
			n = Gravity.CENTER;
		} else if(topMiddleZone.contains(viewHotzone)) {
			n = Gravity.CENTER;
		} else if(bottomMiddleZone.contains(viewHotzone)) {
			n = Gravity.CENTER;
		} else if(centerMiddleZone.contains(viewHotzone)) {
			n = Gravity.CENTER | Gravity.TOP;
		} else if(topRightZone.contains(viewHotzone)) {
			n = Gravity.CENTER | Gravity.TOP;
		} else if(bottomRightZone.contains(viewHotzone)) {
			n = Gravity.CENTER | Gravity.BOTTOM;
		} else if(centerRightZone.contains(viewHotzone)) {
			n = Gravity.CENTER;
		} else {
			// It's not contained in one of the nine zones. Try intersects
			if(Rect.intersects(topLeftZone, viewHotzone)) {
				n = Gravity.RIGHT | Gravity.TOP;
			} else if(Rect.intersects(bottomLeftZone, viewHotzone)) {
				n = Gravity.RIGHT | Gravity.BOTTOM;
			} else if(Rect.intersects(centerLeftZone, viewHotzone)) {
				n = Gravity.RIGHT | Gravity.CENTER;
			} else if(Rect.intersects(topRightZone, viewHotzone)) {
				n = Gravity.LEFT | Gravity.TOP;
			} else if(Rect.intersects(bottomRightZone, viewHotzone)) {
				n = Gravity.LEFT | Gravity.BOTTOM;
			} else if(Rect.intersects(centerRightZone, viewHotzone)) {
				n = Gravity.LEFT | Gravity.CENTER;
			} else if(Rect.intersects(topMiddleZone, viewHotzone)) {
				n = Gravity.BOTTOM | Gravity.CENTER;
			} else if(Rect.intersects(bottomMiddleZone, viewHotzone)) {
				n = Gravity.TOP | Gravity.CENTER;
			} else if(Rect.intersects(centerMiddleZone, viewHotzone)) {
				n = Gravity.CENTER | Gravity.TOP;
			}
		}
		return n;
	}

	/**
	 * Performs animation when a selected hotzone is done,
	 * either by choosing a new hotzone or a non-hotzone
	 */
	protected void offHotzone() {
		selectedHotzone = null;
		compareHotzone = null;

		ivHotzone.clearAnimation();
		tvAnswer.clearAnimation();
		Animation animHotzone = AnimationUtils.loadAnimation(getContext(), R.anim.hotzone);
		Animation animAnswer= AnimationUtils.loadAnimation(getContext(), R.anim.push_left_out);
		ivHotzone.startAnimation(animHotzone);
		tvAnswer.startAnimation(animAnswer);

		ivHotzone.setVisibility(View.INVISIBLE);
		tvAnswer.setVisibility(View.INVISIBLE);

		setBackgroundColor(bgColor);
		invalidate();
	}
	
	public void cleanUp() {
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		theScene = null;
		ivHotzone = null;
		tvAnswer = null;
		layoutParams = null;
		matrix = null;
		paint = null;
		paintHotzone = null;
		selectedHotzone = null;

		shutdownTextToSpeech();
	}
	
	/**
	 * Returns visibility of tvAnswer based on testMode
	 * @return
	 */
	protected int getAnswerVisibility() {
		int n = View.VISIBLE;
		if(PREF_TESTING_MODE_TOUCH_HOTZONE.equals(testMode))
			n = View.VISIBLE;
		else if (PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode))
			n = View.INVISIBLE;
		return n;
	}

	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		boolean problem = false;
		if (status == TextToSpeech.SUCCESS) {
			Locale locale = SceneUtil.getLocaleForSpeech((getContext()
					.getApplicationContext()));
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
			Toast.makeText(getContext().getApplicationContext(), getResources()
					.getString(R.string.languageNotAvail), Toast.LENGTH_SHORT);

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getContext()
							.getApplicationContext());
			SharedPreferences.Editor edit = prefs.edit();
			edit.putBoolean(PREF_SPEAK_RESPONSES, false);
			edit.commit();

			shutdownTextToSpeech();
		}
	}

	protected void speakAnswer() {
		// The TTS engine has been successfully initialized.
		if (mTts != null) {
			// Don't speak answer in multiple choice test mode
			// Only in training or touch test mode
			if (!PREF_TESTING_MODE_MULTIPLE_CHOICE.equals(testMode)) {
				mTts.speak(String.valueOf(tvAnswer.getText()),
						TextToSpeech.QUEUE_FLUSH, null);
			}
		}
	}

	public void startupTextToSpeech(Context context) {
		// Initialize TextToSpeech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		if (mTts == null)
			mTts = new TextToSpeech(context, this);
	}

	public void shutdownTextToSpeech() {
		// Shutdown TextToSpeech
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		mTts = null;
	}
}