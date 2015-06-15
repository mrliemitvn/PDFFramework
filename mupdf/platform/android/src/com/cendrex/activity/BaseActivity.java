package com.cendrex.activity;

import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String language = "en";
		if (SharePrefs.FR_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			language = "fr";
		}
		Utils.changeLanguage(getApplicationContext(), language);
	}
}
