package com.cendrex;

import android.app.Application;

import com.cendrex.utils.SharePrefs;

public class CendrexApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SharePrefs.getInstance().init(this);
	}
}