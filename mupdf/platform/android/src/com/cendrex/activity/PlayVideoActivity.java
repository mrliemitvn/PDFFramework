package com.cendrex.activity;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cendrex.R;
import com.cendrex.utils.Utils;

public class PlayVideoActivity extends BaseActivity {

	/* View element. */
	private VideoView mVvPlayVideo;

	/**
	 * Initialize view.
	 */
	private void init() {
		mVvPlayVideo = (VideoView) findViewById(R.id.vvPlayVideo);
		mVvPlayVideo.setMediaController(new MediaController(this));

		String resourcePath = Utils.getMountedObbFile(this);
		if (TextUtils.isEmpty(resourcePath)) finish();
		Uri uriVideo = Uri.parse(resourcePath + File.separator + "cendrex_cta_adjustable_access_door_video.mp4");
		mVvPlayVideo.setVideoURI(uriVideo);
		mVvPlayVideo.requestFocus();
		mVvPlayVideo.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play_video);

		// Initialize view.
		init();
	}
}
