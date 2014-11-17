package com.cendrex.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class Utils {
	private static String strDateTimeFormat = "yyyy-MM-dd HH:mm:ss Z";
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(strDateTimeFormat);

	/**
	 * Hide soft keyboard.
	 * 
	 * @param context
	 * @param v
	 */
	public static void hideKeyboard(Context context, View v) {
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

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
	public static void registerUser() {
		String currentDateTime = dateTimeFormat.format(new Date());
		String userInfo = new StringBuilder().append("New user registered at ").append(currentDateTime)
				.append("\n\tFirst Name:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_FIRST_NAME, ""))
				.append("\n\tLast Name:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_LAST_NAME, ""))
				.append("\n\tCompany:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_COMPANY, ""))
				.append("\n\tCity:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_CITY, ""))
				.append("\n\tState:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_STATE, ""))
				.append("\n\tEmail:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_EMAIL, ""))
				.toString();

		byte[] user = userInfo.getBytes();
		ParseFile userFile = new ParseFile(Consts.PARSE_USER_FILE_NAME, user);
		userFile.saveInBackground();
		ParseObject jobApplication = new ParseObject("Log");
		jobApplication.put("representative", SharePrefs.getInstance().get(SharePrefs.PARSE_USER_LAST_NAME, ""));
		jobApplication.put("log_file", userFile);
		jobApplication.saveInBackground(new SaveCallback() {
			@Override
			public void done(ParseException e) {
				if (e == null) { // Register new user successfully.
					SharePrefs.getInstance().setUserRegistered();
				}
			}
		});
	}

	/**
	 * Share email.
	 */
	public static void shareEmail(String email) {
		String currentDateTime = dateTimeFormat.format(new Date());
		String shareInfo = new StringBuilder().append("New app sharing at ").append(currentDateTime).append("\nFrom:")
				.append("\n\tFirst Name:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_FIRST_NAME, ""))
				.append("\n\tLast Name:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_LAST_NAME, ""))
				.append("\n\tCompany:\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_COMPANY, ""))
				.append("\n\tCity:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_CITY, ""))
				.append("\n\tState:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_STATE, ""))
				.append("\n\tEmail:\t\t").append(SharePrefs.getInstance().get(SharePrefs.PARSE_USER_EMAIL, ""))
				.append("\nTo:").append("\n\tEmail:\t\t").append(email).toString();

		byte[] share = shareInfo.getBytes();
		ParseFile shareFile = new ParseFile(Consts.PARSE_SHARE_FILE_NAME, share);
		shareFile.saveInBackground();
		ParseObject jobApplication = new ParseObject("Log");
		jobApplication.put("representative", SharePrefs.getInstance().get(SharePrefs.PARSE_USER_LAST_NAME, ""));
		jobApplication.put("log_file", shareFile);
		jobApplication.saveInBackground();
	}
}
