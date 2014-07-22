package com.cendrex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import com.cendrex.utils.SharePrefs;

public class CendrexApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		SharePrefs.getInstance().init(this);

		// Create app folder.
		File appFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
				+ "cendrex");
		if (!appFolder.exists()) {
			appFolder.mkdir();
		}

		// Copy assets to internal storage.
		copyAssets();
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		for (String filename : files) {
			InputStream in = null;
			OutputStream out = null;
			try {
				in = assetManager.open(filename);
				File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
						+ "cendrex" + File.separator, filename);
				out = new FileOutputStream(outFile);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			} catch (IOException e) {
				Log.e("tag", "Failed to copy asset file: " + filename, e);
			}
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
}