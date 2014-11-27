package com.cendrex.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Messenger;
import android.util.Log;

import com.cendrex.R;
import com.cendrex.SampleDownloaderService;
import com.cendrex.listener.OnObbMountedListener;
import com.cendrex.utils.Utils;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;

public class DownloadObbDataActivity extends Activity implements IDownloaderClient {

	/* View elements. */
	private ProgressDialog mDialog;
	private OnObbMountedListener onObbMountedListener;
	private IDownloaderService mRemoteService;
	private IStub mDownloaderClientStub;
	private boolean isCallMountObbFile = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_obb_data);
		isCallMountObbFile = false;

		mDialog = ProgressDialog.show(this, null, getResources().getString(R.string.loading), true, false);
		onObbMountedListener = new OnObbMountedListener() {
			@Override
			public void onObbMounted(boolean isMounted) {
				mDialog.dismiss();
				startActivity(new Intent(DownloadObbDataActivity.this, IntroActivity.class));
				finish();
			}
		};

		try {
			String packageName = getPackageName();
			String[] fileList = Utils.getAPKExpansionFiles(this,
					getPackageManager().getPackageInfo(packageName, 0).versionCode, 0);
			if (fileList != null && fileList.length > 0) {
				Utils.mountObbFile(this, fileList[0], onObbMountedListener);
				isCallMountObbFile = true;
			} else {
				// Start download obb file
				// Build an Intent to start this activity from the Notification
				Intent launchIntent = DownloadObbDataActivity.this.getIntent();
				Intent intentToLaunchThisActivityFromNotification = new Intent(DownloadObbDataActivity.this,
						DownloadObbDataActivity.this.getClass());
				intentToLaunchThisActivityFromNotification.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intentToLaunchThisActivityFromNotification.setAction(launchIntent.getAction());

				if (launchIntent.getCategories() != null) {
					for (String category : launchIntent.getCategories()) {
						intentToLaunchThisActivityFromNotification.addCategory(category);
					}
				}

				// Build PendingIntent used to open this activity from
				// Notification
				PendingIntent pendingIntent = PendingIntent.getActivity(DownloadObbDataActivity.this, 0,
						intentToLaunchThisActivityFromNotification, PendingIntent.FLAG_UPDATE_CURRENT);

				// Start the download service (if required)
				int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent,
						SampleDownloaderService.class);
				// If download has started, initialize this activity to show download progress
				if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED) {
					// This is where you do set up to display the download progress (next step)
					mDownloaderClientStub = DownloaderClientMarshaller.CreateStub(this, SampleDownloaderService.class);
					return;
				} // If the download wasn't necessary, fall through to start the app
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Connect the stub to our service on start.
	 */
	@Override
	protected void onStart() {
		if (null != mDownloaderClientStub) {
			mDownloaderClientStub.connect(this);
		}
		super.onStart();
	}

	/**
	 * Disconnect the stub from our service on stop
	 */
	@Override
	protected void onStop() {
		if (null != mDownloaderClientStub) {
			mDownloaderClientStub.disconnect(this);
		}
		super.onStop();
	}

	/**
	 * Critical implementation detail. In onServiceConnected we create the remote service and marshaler. This is how we
	 * pass the client information back to the service so the client can be properly notified of changes. We must do
	 * this every time we reconnect to the service.
	 */
	@Override
	public void onServiceConnected(Messenger m) {
		mRemoteService = DownloaderServiceMarshaller.CreateProxy(m);
		mRemoteService.onClientUpdated(mDownloaderClientStub.getMessenger());
	}

	@Override
	public void onDownloadStateChanged(int newState) {
		if (IDownloaderClient.STATE_COMPLETED == newState && !isCallMountObbFile) {
			Log.d("Cendrex", "finish download ");
			String[] fileList;
			try {
				fileList = Utils.getAPKExpansionFiles(this,
						getPackageManager().getPackageInfo(getPackageName(), 0).versionCode, 0);
				Utils.mountObbFile(this, fileList[0], onObbMountedListener);
				isCallMountObbFile = true;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDownloadProgress(DownloadProgressInfo progress) {
		Log.d("Cendrex", "progress " + progress.mOverallProgress);
	}
}
