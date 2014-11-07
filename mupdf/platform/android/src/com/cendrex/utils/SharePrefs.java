package com.cendrex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharePrefs {

	public static final String FILES_LANGUAGE = "files_language";
	public static final String EN_LANGUAGE = "en";
	public static final String FR_LANGUAGE = "fr";
	public static final String REGISTERED_USER = "registered_user";
	public static final String PARSE_USER_FIRST_NAME = "First Name";
	public static final String PARSE_USER_LAST_NAME = "Last Name";
	public static final String PARSE_USER_COMPANY = "Company";
	public static final String PARSE_USER_CITY = "City";
	public static final String PARSE_USER_STATE = "State";
	public static final String PARSE_USER_EMAIL = "Email";

	private static SharePrefs instance = new SharePrefs();
	private SharedPreferences sharedPreferences;

	public static SharePrefs getInstance() {
		return instance;
	}

	public void init(Context ctx) {
		if (sharedPreferences == null) {
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		}
	}

	/**
	 * Clear shared preferences.
	 */
	public void clear() {
		sharedPreferences.edit().clear().commit();
	}

	/**
	 * Save string value to shared preferences.
	 * 
	 * @param key
	 *            The name of the preference to modify.
	 * @param value
	 *            The new value for the preference
	 */
	public void save(String key, String value) {
		sharedPreferences.edit().putString(key, value).commit();
	}

	/**
	 * Retrieve a String value from the preferences.
	 * 
	 * @param key
	 *            The name of the preference to retrieve.
	 * @param _defValue
	 *            Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue
	 */
	public String get(String key, String _defValue) {
		return sharedPreferences.getString(key, _defValue);
	}

	/**
	 * Save integer value to shared preferences.
	 * 
	 * @param key
	 *            The name of the preference to modify.
	 * @param value
	 *            The new value for the preference
	 */
	public void save(String key, int value) {
		sharedPreferences.edit().putInt(key, value).commit();
	}

	/**
	 * Retrieve a integer value from the preferences.
	 * 
	 * @param key
	 *            The name of the preference to retrieve.
	 * @param _defValue
	 *            Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue
	 */
	public int get(String key, int _defValue) {
		return sharedPreferences.getInt(key, _defValue);
	}

	/**
	 * Save boolean value to shared preferences.
	 * 
	 * @param key
	 *            The name of the preference to modify.
	 * @param value
	 *            The new value for the preference
	 */
	public void save(String key, boolean value) {
		sharedPreferences.edit().putBoolean(key, value).commit();
	}

	/**
	 * Retrieve a boolean value from the preferences.
	 * 
	 * @param key
	 *            The name of the preference to retrieve.
	 * @param _defValue
	 *            Value to return if this preference does not exist.
	 * @return Returns the preference value if it exists, or defValue
	 */
	public boolean get(String key, boolean _defValue) {
		return sharedPreferences.getBoolean(key, _defValue);
	}

	/**
	 * Save language of files setting.
	 * 
	 * @param value
	 *            the value to save.
	 */
	public void saveFilesLanguageSetting(String value) {
		save(FILES_LANGUAGE, value);
	}

	/**
	 * Get language of files setting.
	 * 
	 * @return
	 */
	public String getFilesLanguageSetting() {
		return get(FILES_LANGUAGE, EN_LANGUAGE);
	}

	/**
	 * Set user is registered.
	 */
	public void setUserRegistered() {
		save(REGISTERED_USER, true);
	}

	/**
	 * Check user is registered or not.
	 * 
	 * @return true if user is registered.
	 */
	public boolean isUserRegistered() {
		return get(REGISTERED_USER, false);
	}
}