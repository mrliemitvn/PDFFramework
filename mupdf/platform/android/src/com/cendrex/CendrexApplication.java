package com.cendrex;

import android.app.Application;

import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class CendrexApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SharePrefs.getInstance().init(this);

		// Create app folder.
		// Current version use obb file to store resources. So we didn't need this action.
		// File appFolder = new File(Consts.APP_FOLDER);
		// if (!appFolder.exists()) {
		// appFolder.mkdir();
		// }

		String language = "en";
		if (!SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			language = "fr";
		}
		Utils.changeLanguage(this, language);

		// Initialize parse.
		Parse.initialize(this, Consts.PARSE_APP_ID, Consts.PARSE_CLIENT_ID);
		ParsePush.subscribeInBackground("cendrex_android_user", new SaveCallback() {
			@Override
			public void done(ParseException e) {
			}
		});
	}
}