package com.cendrex.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

import com.cendrex.R;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

public class IntroActivity extends Activity implements OnClickListener {

	/* View element. */
	private VideoView mVvPlayVideo;
	private View mIgnoreView;

	/**
	 * Go to MainActivty and finish this activity.
	 */
	private void goToMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	/**
	 * Play video intro.
	 */
	private void playVideo() {
		mVvPlayVideo.requestFocus();
		mVvPlayVideo.start();
	}

	/**
	 * Show register view.
	 * 
	 * @param visible
	 */
	private void showRegisterView(int visible) {
		// TODO
	}

	/**
	 * Initialize view.
	 */
	private void init() {
		mIgnoreView = (View) findViewById(R.id.ignoreView);
		mIgnoreView.setOnClickListener(this);

		Uri uriVideo = null;
		if (Utils.isTablet(this)) {
			if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
				// Choose tablet English file.
				uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.intro_video_en);
			} else {
				// Choose tablet French file.
				uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.intro_video_fr);
			}
		} else {
			if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
				uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.intro_phone_en);
			} else {
				// Choose French file.
				uriVideo = Uri.parse("android.resource://com.cendrex/" + R.raw.intro_phone_fr);
			}
		}

		mVvPlayVideo = (VideoView) findViewById(R.id.vvPlayVideo);
		mVvPlayVideo.setMediaController(null);
		mVvPlayVideo.setVideoURI(uriVideo);
		mVvPlayVideo.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				goToMainActivity();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		// Initialize view.
		init();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (SharePrefs.getInstance().isUserRegistered()) {
			showRegisterView(View.GONE);
			playVideo();
		} else {
			showRegisterView(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mVvPlayVideo.stopPlayback();
	}

	@Override
	public void onClick(View v) {
		if (v == mIgnoreView) {
			goToMainActivity();
		}
	}
}
