package com.cendrex.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.cendrex.R;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

public class IntroActivity extends Activity implements OnClickListener {

	/* View element. */
	private VideoView mVvPlayVideo;
	private View mIgnoreView;
	private ScrollView mSvRegister;
	private EditText mEtLastName;
	private EditText mEtFirstName;
	private EditText mEtCompany;
	private EditText mEtCity;
	private EditText mEtState;
	private EditText mEtEmail;
	private TextView mTvRegister;

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
		mSvRegister.setVisibility(visible);
	}

	/**
	 * Register user.
	 */
	private void registerUser() {
		// Check fill all information.
		if (TextUtils.isEmpty(mEtLastName.getText()) || TextUtils.isEmpty(mEtFirstName.getText())
				|| TextUtils.isEmpty(mEtCompany.getText()) || TextUtils.isEmpty(mEtCity.getText())
				|| TextUtils.isEmpty(mEtState.getText()) || TextUtils.isEmpty(mEtEmail.getText())) {
			Toast.makeText(this, "Please enter all information", Toast.LENGTH_SHORT).show();
		} else {
			String lastName = mEtLastName.getText().toString();
			String firstName = mEtFirstName.getText().toString();
			String company = mEtCompany.getText().toString();
			String city = mEtCity.getText().toString();
			String state = mEtState.getText().toString();
			String email = mEtEmail.getText().toString();
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_LAST_NAME, lastName);
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_FIRST_NAME, firstName);
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_COMPANY, company);
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_CITY, city);
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_STATE, state);
			SharePrefs.getInstance().save(SharePrefs.PARSE_USER_EMAIL, email);
			Utils.registerUser();
			showRegisterView(View.GONE);
			playVideo();
		}
	}

	/**
	 * Initialize view.
	 */
	private void init() {
		mIgnoreView = (View) findViewById(R.id.ignoreView);
		mSvRegister = (ScrollView) findViewById(R.id.svRegisterUser);
		mEtLastName = (EditText) findViewById(R.id.etLastName);
		mEtFirstName = (EditText) findViewById(R.id.etFirstName);
		mEtCompany = (EditText) findViewById(R.id.etCompany);
		mEtCity = (EditText) findViewById(R.id.etCity);
		mEtState = (EditText) findViewById(R.id.etState);
		mEtEmail = (EditText) findViewById(R.id.etEmail);
		mTvRegister = (TextView) findViewById(R.id.tvRegister);
		mSvRegister.setOnClickListener(this);
		mIgnoreView.setOnClickListener(this);
		mTvRegister.setOnClickListener(this);

		String resourcePath = Utils.getMountedObbFile(this);
		Uri uriVideo = null;
		if (TextUtils.isEmpty(resourcePath)) {
			resourcePath = "android.resource://com.cendrex/";
			Uri.parse(resourcePath + R.raw.intro_phone_en);
		} else {
			if (Utils.isTablet(this)) {
				if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
					// Choose tablet English file.
					uriVideo = Uri.parse(resourcePath + File.separator + "intro_video_en.mp4");
				} else {
					// Choose tablet French file.
					uriVideo = Uri.parse(resourcePath + File.separator + "intro_video_fr.mp4");
				}
			} else {
				if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
					// Choose English file.
					uriVideo = Uri.parse(resourcePath + File.separator + "intro_phone_en.mp4");
				} else {
					// Choose French file.
					uriVideo = Uri.parse(resourcePath + File.separator + "intro_phone_fr.mp4");
				}
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
		} else if (v == mTvRegister) {
			if (getCurrentFocus() != null) {
				Utils.hideKeyboard(this, getCurrentFocus());
			}
			registerUser();
		}
	}
}
