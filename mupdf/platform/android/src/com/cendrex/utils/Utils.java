package com.cendrex.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cendrex.listener.OnObbMountedListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.SaveCallback;

public class Utils {
	private static String strDateTimeFormat = "yyyy-MM-dd HH:mm:ss Z";
	private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat(strDateTimeFormat);

	public static String[] getAPKExpansionFiles(Context ctx, int mainVersion, int patchVersion) {
		String packageName = ctx.getPackageName();
		Vector<String> ret = new Vector<String>();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			// Build the full path to the app's expansion files
			File root = Environment.getExternalStorageDirectory();
			File expPath = new File(root.toString() + Consts.EXP_PATH + packageName);

			// Check that expansion file path exists
			if (expPath.exists()) {
				if (mainVersion > 0) {
					String strMainPath = expPath + File.separator + "main." + mainVersion + "." + packageName + ".obb";
					File main = new File(strMainPath);
					if (main.isFile()) {
						ret.add(strMainPath);
					}
				}
				if (patchVersion > 0) {
					String strPatchPath = expPath + File.separator + "patch." + mainVersion + "." + packageName
							+ ".obb";
					File main = new File(strPatchPath);
					if (main.isFile()) {
						ret.add(strPatchPath);
					}
				}
			}
		}
		String[] retArray = new String[ret.size()];
		ret.toArray(retArray);
		return retArray;
	}

	/**
	 * Mount obb file.
	 * 
	 * @param path
	 *            obb file path.
	 */
	public static void mountObbFile(Context context, String path, final OnObbMountedListener onObbMountedListener) {
		StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		if (!TextUtils.isEmpty(path)) {
			if (!storageManager.isObbMounted(path)) {
				storageManager.mountObb(path, null, new OnObbStateChangeListener() {
					@Override
					public void onObbStateChange(String path, int state) {
						super.onObbStateChange(path, state);
						boolean isMounted = false;
						if (state == OnObbStateChangeListener.MOUNTED) {
							Log.d("Cendrex", "obb mounted");
							isMounted = true;
						} else {
							Log.d("Cendrex", "obb not mounted");
						}
						if (onObbMountedListener != null) onObbMountedListener.onObbMounted(isMounted);
					}
				});
			} else {
				Log.d("Cendrex", "obb already mounted");
				if (onObbMountedListener != null) onObbMountedListener.onObbMounted(true);
			}
		}
	}

	/**
	 * Get mounted obb file path.
	 * 
	 * @param context
	 * @return
	 */
	public static String getMountedObbFile(Context context) {
		String resourcePath = null;
		try {
			String[] fileList = getAPKExpansionFiles(context,
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode, 0);
			StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			if (fileList != null && fileList.length > 0) resourcePath = storageManager.getMountedObbPath(fileList[0]);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return resourcePath;
	}

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
