package com.cendrex.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

import com.cendrex.R;

public class IntroActivity extends Activity implements OnClickListener {

	/* View element. */
	private VideoView mVvPlayVideo;

	/**
	 * Initialize view.
	 */
	private void init() {
		mVvPlayVideo = (VideoView) findViewById(R.id.vvPlayVideo);
		mVvPlayVideo.setMediaController(null);

		Uri uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.video_intro_fr);
		mVvPlayVideo.setVideoURI(uriVideo);
		mVvPlayVideo.requestFocus();
		mVvPlayVideo.start();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		// Initialize view.
		init();
	}

	@Override
	public void onClick(View v) {

	}
}
