package com.ciardullo.vocabulent.util;

import java.lang.reflect.Field;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.ciardullo.vocabulent.AppConstants;

/**
 * Contains static utility methods
 */
public class SceneUtil {
	
	/**
	 * Returns the installed language, as taken from the 
	 * app's package name
	 */
	public static String getInstalledLanguage(Context context) {
		// Last two characters of package name is the installed language code
		String s = context.getPackageName();
		String t = null;
		
		if(s != null)
			t = s.substring(s.length()-2);
		else
			t = AppConstants.DEFAULT_LANG_CD;
		
		return t;
	}
	
	public static Locale getLocaleForSpeech(Context context) {
		String s = getInstalledLanguage(context);
		Locale l = null;
		if("en".equals(s)) {
			l = Locale.US;
		} else if("fr".equals(s)) {
			l = Locale.FRANCE;
		} else if("it".equals(s)) {
			l = Locale.ITALY;
		} else if("de".equals(s)) {
			l = Locale.GERMANY;
		} else if("es".equals(s)) {
			l = new Locale("es_ES");
		}
		
		return l;
	}

	/**
	 * Gets a resource id for the given name from Class c, which must
	 * be android's generated R.raw
	 * @param c
	 * @param resourceName
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static int getImageResourceId(Class c, String resourceName) {
		// From the image name, find the resource id
		int n = 0;
		try {
			Field field = c.getField(resourceName);
			// Pass null because its a static field, there's no instance
			Integer resourceId = (Integer)field.get(null);
			n = resourceId.intValue();
		} catch (Exception e) {
			Log.e("ERROR", e.getMessage());
		}
		return n;
	}
}
