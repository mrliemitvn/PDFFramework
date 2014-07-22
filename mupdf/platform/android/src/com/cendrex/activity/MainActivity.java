package com.cendrex.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cendrex.R;
import com.cendrex.mupdf.ChoosePDFActivity;
import com.cendrex.mupdf.MuPDFActivity;
import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;

public class MainActivity extends Activity implements OnClickListener {

	private static final int PITCH_TYPE = 0;
	private static final int NEW_TYPE = 1;

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

	/**
	 * Show file name to open.
	 * 
	 * @param type
	 */
	private void showFilesToOpen(int type) {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.first_screen_alert_layout);
		dialog.setTitle(R.string.title_choose_file_to_open);

		// Set dialog content.
		RelativeLayout rlFile1 = (RelativeLayout) dialog.findViewById(R.id.rlFile1);
		RelativeLayout rlFile2 = (RelativeLayout) dialog.findViewById(R.id.rlFile2);
		TextView tvFileName1 = (TextView) dialog.findViewById(R.id.tvFileName1);
		TextView tvFileName2 = (TextView) dialog.findViewById(R.id.tvFileName2);
		ImageView imgIcon1 = (ImageView) dialog.findViewById(R.id.imgIcon1);

		String languageFile = SharePrefs.getInstance().getFilesLanguageSetting();
		switch (type) {
		case PITCH_TYPE:
			if (SharePrefs.EN_LANGUAGE.equals(languageFile)) {
				tvFileName1.setText(R.string.first_screen_pitch_file_1_en);
				tvFileName2.setText(R.string.first_screen_pitch_file_2_en);
			} else {
				tvFileName1.setText(R.string.first_screen_pitch_file_1_fr);
				tvFileName2.setText(R.string.first_screen_pitch_file_2_fr);
			}
			rlFile1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Open PITCH file 1 in English or French.
					dialog.dismiss();
				}
			});
			rlFile2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Open PITCH file 2 in English or French.
					dialog.dismiss();
				}
			});
			break;
		default:
			imgIcon1.setImageResource(R.drawable.ic_play_video);
			tvFileName1.setText(R.string.first_screen_new_video_file);
			if (SharePrefs.EN_LANGUAGE.equals(languageFile)) {
				tvFileName2.setText(R.string.first_screen_new_file_en);
			} else {
				tvFileName2.setText(R.string.first_screen_new_file_fr);
			}
			rlFile1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Open Video file.
					dialog.dismiss();
				}
			});
			rlFile2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Open NEW file in English or French.
					dialog.dismiss();
				}
			});
			break;
		}

		dialog.show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize view.
		init();
		// TODO Class find pdf files.
//		 Intent intent = new Intent(this, ChoosePDFActivity.class);
//		 intent.putExtra(Consts.KEY_CHOOSE_PDF, true);
//		 startActivity(intent);
//		 finish();
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
		case R.id.tvPitch:
			showFilesToOpen(PITCH_TYPE);
			break;
		case R.id.tvDocs:
			// TODO: Open Doc file.
			Uri uri = Uri
					.parse((new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
							+ "cendrex" + File.separator + "DOC_EN_CENDREX_Access_Doors_Catalog_2014_HR.pdf")
							.getAbsolutePath()));
			Intent intentOpenDocFile = new Intent(this, MuPDFActivity.class);
			intentOpenDocFile.setAction(Intent.ACTION_VIEW);
			intentOpenDocFile.setData(uri);
			startActivity(intentOpenDocFile);
			break;
		case R.id.tvNew:
			showFilesToOpen(NEW_TYPE);
			break;
		case R.id.tvCall:
			Intent intent = new Intent(Intent.ACTION_DIAL);
			startActivity(intent);
			break;
		default:
			break;
		}
	}
}
