package com.ciardullo.vocabulent;

import android.graphics.Color;

public interface AppConstants {
	public static final String EXTRA_BITMAP = "EXTRA_BITMAP";
	public static final String EXTRA_SCENE_KEY = "EXTRA_SCENE_KEY";
	public static final String EXTRA_GET_SCENE_BY_ID = "EXTRA_GET_SCENE_BY_ID";
	public static final String EXTRA_GET_SCENE_BY_POSITION = "EXTRA_GET_SCENE_BY_POSITION";
	public static int COLOR_BG = Color.WHITE;
	public static int COLOR_DIM = Color.GRAY;
	public static int COLOR_FG_HIGHLIGHT = Color.parseColor("#FFA441"); // Color.RED
	public static int COLOR_BG_HIGHLIGHT = Color.WHITE;
	public static int COLOR_APP_BG = Color.parseColor("#FFA441");
	public static int DIALOG_STATS = 0;
	public static int DIALOG_UPGRADE_NOW = 1;
	public static String EXTRA_ALBUM_ID = "EXTRA_ALBUM_ID";
	public static String EXTRA_MODE = "EXTRA_MODE";
	public static int MODE_TRAINING = 0;
	public static int MODE_TESTING = 1;
	public static String PREF_INIT_DONE = "PREF_INIT_DONE";
	public static String PREF_TESTING_MODE = "PREF_TESTING_MODE";
	public static String PREF_TESTING_MODE_TOUCH_HOTZONE = "0";
	public static String PREF_TESTING_MODE_MULTIPLE_CHOICE = "1";
	public static String PREF_SPEAK_RESPONSES = "PREF_SPEAK_RESPONSES";
	public static int PREF_RESULT_TESTING_MODE_CHANGED = 99;
	public static String DEFAULT_LANG_CD = "en";
	public static final int MENU_ITEM_ABOUT = 0;
	public static final int MENU_ITEM_UPGRADE = 1;
	public static final int DIALOG_ABOUT = 0;

	public static final int RESULT_OK_REFRESH_SCENE_MGR = 901;
	public static final int RESULT_OK_START_BILLING_ACTIVITY = 902;
	public static final int REQUEST_START_SCENE_ACTIVITY = 801;
	public static final int REQUEST_START_BILLING_ACTIVITY = 802;
}
