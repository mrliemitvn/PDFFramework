package com.cendrex.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.cendrex.R;
import com.cendrex.utils.SharePrefs;

public class MainActivity extends Activity implements OnClickListener {

	/* View elements. */
	private ImageView mImgSetting;
	private TextView mTvPitch;
	private TextView mTvDocs;
	private TextView mTvNew;
	private TextView mTvCall;

	/* Variables will be used. */
	private CharSequence[] mItemsFilesLanguage;

	/**
	 * Initialize view.
	 */
	private void init() {
		mImgSetting = (ImageView) findViewById(R.id.imgSetting);
		mTvPitch = (TextView) findViewById(R.id.tvPitch);
		mTvDocs = (TextView) findViewById(R.id.tvDocs);
		mTvNew = (TextView) findViewById(R.id.tvNew);
		mTvCall = (TextView) findViewById(R.id.tvCall);

		mImgSetting.setOnClickListener(this);
		mTvPitch.setOnClickListener(this);
		mTvDocs.setOnClickListener(this);
		mTvNew.setOnClickListener(this);
		mTvCall.setOnClickListener(this);
	}

	/**
	 * Show alert dialog for setting files language.
	 */
	private void showFilesLanguageSetting() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.title_choose_file_language);
		int itemChecked = 0;
		if (!SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			itemChecked = 1;
		}
		builder.setSingleChoiceItems(mItemsFilesLanguage, itemChecked, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {

				switch (item) {
				case 0:
					SharePrefs.getInstance().saveFilesLanguageSetting(SharePrefs.EN_LANGUAGE);
					break;
				case 1:
					SharePrefs.getInstance().saveFilesLanguageSetting(SharePrefs.FR_LANGUAGE);
					break;

				}
				dialog.dismiss();
			}
		});
		AlertDialog filesLanguageDialog = builder.create();
		filesLanguageDialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize view.
		init();
		// TODO Class find pdf files.
		// Intent intent = new Intent(this, ChoosePDFActivity.class);
		// intent.putExtra(Consts.KEY_CHOOSE_PDF, true);
		// startActivity(intent);
		// finish();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Initialize variables.
		if (mItemsFilesLanguage == null) {
			String en = getString(R.string.setting_language_en);
			String fr = getString(R.string.setting_language_fr);
			mItemsFilesLanguage = new CharSequence[] { en, fr };
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgSetting:
			showFilesLanguageSetting();
			break;

		default:
			break;
		}
	}
}
