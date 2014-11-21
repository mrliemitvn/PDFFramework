package com.cendrex.activity;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.cendrex.R;
import com.cendrex.utils.Utils;

public class DownloadObbDataActivity extends Activity {

	/* View elements. */
	private ImageView mImgBackground;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_obb_data);

		// Set background.
		mImgBackground = (ImageView) findViewById(R.id.imgBackground);
		if (Utils.isTablet(this)) {
			mImgBackground.setImageResource(R.drawable.bg_tablet);
		} else {
			mImgBackground.setImageResource(R.drawable.bg_phone);
		}

		try {
			String[] fileList = Utils.getAPKExpansionFiles(this, getPackageManager()
					.getPackageInfo(getPackageName(), 0).versionCode, 0);
			Log.d("Test", "call here");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
