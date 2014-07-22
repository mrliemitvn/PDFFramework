package com.cendrex;

import java.io.File;

import android.app.Application;

import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;

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
	}
}