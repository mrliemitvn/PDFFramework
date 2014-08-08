package com.cendrex;

import java.io.File;

import android.app.Application;

import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

public class CendrexApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SharePrefs.getInstance().init(this);

		// Create app folder.
		File appFolder = new File(Consts.APP_FOLDER);
		if (!appFolder.exists()) {
			appFolder.mkdir();
		}
		
		String language = "en";
		if (!SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			language = "fr";
		}
		Utils.changeLanguage(this, language);
	}
}