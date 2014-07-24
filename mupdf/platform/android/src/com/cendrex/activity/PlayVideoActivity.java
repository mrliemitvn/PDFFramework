package com.cendrex.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cendrex.R;

public class PlayVideoActivity extends Activity {

	/* View element. */
	private VideoView mVvPlayVideo;

	/**
	 * Initialize view.
	 */
	private void init() {
		mVvPlayVideo = (VideoView) findViewById(R.id.vvPlayVideo);
		mVvPlayVideo.setMediaController(new MediaController(this));

		Uri uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.cendrex_cta_adjustable_access_door_video);
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
