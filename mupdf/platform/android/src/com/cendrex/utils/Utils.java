package com.cendrex.utils;

import java.util.Locale;

import com.parse.ParseFile;
import com.parse.ParseObject;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

public class Utils {

	/**
	 * Change app language.
	 * 
	 * @param context
	 * @param lang
	 */
	public static void changeLanguage(Context context, String lang) {
		Resources res = context.getResources();
		// Change locale settings in the app.
		DisplayMetrics dm = res.getDisplayMetrics();
		android.content.res.Configuration conf = res.getConfiguration();
		conf.locale = new Locale(lang);
		res.updateConfiguration(conf, dm);
	}

	/**
	 * Check device is tablet or phone.
	 * 
	 * @param context
	 * @return true if device is tablet.
	 */
	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	/**
	 * Register new user.
	 */
	public void registerUser() {
		byte[] user = ("New user registered at 2014-11-05 03:44:50 +0000\n" + "\tFirst Name:\tA"
				+ "\n\tLast Name:\tAaa" + "\n\tCompany:\tA" + "\n\tCity:\t\tA" + "\n\tState:\t\tA"
				+ "\n\tEmail:\t\ta@a.com").getBytes();
		ParseFile userFile = new ParseFile(Consts.PARSE_USER_FILE_NAME, user);
		userFile.saveInBackground();
		ParseObject jobApplication = new ParseObject("Log");
		jobApplication.put("representative", "Abc");
		jobApplication.put("log_file", userFile);
		jobApplication.saveInBackground();
	}
}
