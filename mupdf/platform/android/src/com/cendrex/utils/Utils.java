package com.cendrex.utils;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Utils {
	public static void changeLanguage(Context context, String lang) {
		Resources res = context.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(lang);
		res.updateConfiguration(conf, dm);
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
}
