package com.cendrex.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cendrex.R;
import com.cendrex.adapter.AdvantagesAdapter;
import com.cendrex.mupdf.MuPDFActivity;
import com.cendrex.resource.AdvantagesResource;
import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

public class MainActivity extends Activity implements OnClickListener {

	private static final int ADVANTAGES_TYPE = 0;
	private static final int LIBRARY_TYPE = 1;
	private static final int NEW_TYPE = 2;
	private static final int FIRST = 1;
	private static final int SECOND = 2;

	/* View elements. */
	private ImageView mImgBackground;
	private ImageView mImgShare;
	private ImageView mImgSetting;
	private TextView mTvAdvantages;
	private TextView mTvLibrary;
	private TextView mTvNew;
	private TextView mTvContact;
	private LinearLayout mLlShare;
	private TextView mTvShare;
	private EditText mEtEmailShare;
	private View mViewOverlap;
	private RelativeLayout mRlAdvantagesInfo;
	private ImageView mImgClose;
	private ListView mLvAdvantagesInfo;
	private AdvantagesAdapter mAdvantagesAdapter;
	private ArrayList<AdvantagesResource> listAdvantagesResources;
	private ScrollView mSvInfo;
	private ImageView mImgTitle;

	/* Variables will be used. */
	private CharSequence[] mItemsFilesLanguage;
	CharSequence[] mItemsContact = { "Phone call", "Send email" };

	/**
	 * Initialize view.
	 */
	private void init() {
		mImgBackground = (ImageView) findViewById(R.id.imgBackground);
		mImgShare = (ImageView) findViewById(R.id.imgShare);
		mImgSetting = (ImageView) findViewById(R.id.imgSetting);
		mTvAdvantages = (TextView) findViewById(R.id.tvAdvantages);
		mTvLibrary = (TextView) findViewById(R.id.tvLibrary);
		mTvNew = (TextView) findViewById(R.id.tvNew);
		mTvContact = (TextView) findViewById(R.id.tvContact);
		mLlShare = (LinearLayout) findViewById(R.id.llShare);
		mTvShare = (TextView) findViewById(R.id.tvShare);
		mEtEmailShare = (EditText) findViewById(R.id.etEmailShare);
		mViewOverlap = (View) findViewById(R.id.viewOverlap);
		mRlAdvantagesInfo = (RelativeLayout) findViewById(R.id.rlAdvantagesInfo);
		mImgClose = (ImageView) findViewById(R.id.imgClose);
		mLvAdvantagesInfo = (ListView) findViewById(R.id.lvAdvantagesInfo);
		mSvInfo = (ScrollView) findViewById(R.id.svInfo);
		mImgTitle = (ImageView) findViewById(R.id.imgTitle);

		mImgShare.setOnClickListener(this);
		mImgSetting.setOnClickListener(this);
		mTvAdvantages.setOnClickListener(this);
		mTvLibrary.setOnClickListener(this);
		mTvNew.setOnClickListener(this);
		mTvContact.setOnClickListener(this);
		mLlShare.setOnClickListener(this);
		mTvShare.setOnClickListener(this);
		mViewOverlap.setOnClickListener(this);
		mImgClose.setOnClickListener(this);
		mImgTitle.setOnClickListener(this);

		if (Utils.isTablet(this)) {
			mImgBackground.setImageResource(R.drawable.bg_tablet);
		} else {
			mImgBackground.setImageResource(R.drawable.bg_phone);
		}

		Typeface orbitron = Typeface.createFromAsset(getAssets(), "orbitron-bold.otf");
		mTvAdvantages.setTypeface(orbitron);
		mTvLibrary.setTypeface(orbitron);
		mTvNew.setTypeface(orbitron);
		mTvContact.setTypeface(orbitron);

		loadAdvantages();
	}

	/**
	 * Load list advantages info and add to list.
	 */
	private void loadAdvantages() {
		listAdvantagesResources = new ArrayList<AdvantagesResource>();
		XmlResourceParser xmlParser = null;
		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			xmlParser = getResources().getXml(R.xml.advantages_data_en);
		} else {
			xmlParser = getResources().getXml(R.xml.advantages_data_fr);
		}
		try {
			int eventType = xmlParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && "item".equals(xmlParser.getName())) {
					AdvantagesResource advantagesResource = new AdvantagesResource();
					advantagesResource.message = xmlParser.getAttributeValue(0);
					advantagesResource.title = xmlParser.getAttributeValue(1);
					listAdvantagesResources.add(advantagesResource);
				}
				eventType = xmlParser.next();
			}
			// indicate app done reading the resource.
			xmlParser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mAdvantagesAdapter = new AdvantagesAdapter(this, listAdvantagesResources);
		mLvAdvantagesInfo.setAdapter(mAdvantagesAdapter);
		mLvAdvantagesInfo.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AdvantagesResource advantagesResource = (AdvantagesResource) parent.getItemAtPosition(position);
				if (advantagesResource != null) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
					// set title
					alertDialogBuilder.setTitle(R.string.advantage_title);
					// set dialog message
					alertDialogBuilder.setMessage(advantagesResource.message);
					alertDialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					});
					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();
					// show it
					alertDialog.show();
				}
			}
		});
	}

	/**
	 * Handler event after change language.
	 */
	private void handlerAfterChangeLanguage() {
		mTvAdvantages.setText(R.string.advantages_menu);
		mTvLibrary.setText(R.string.library_menu);
		mTvNew.setText(R.string.new_menu);
		mTvContact.setText(R.string.contact_us_menu);
		startActivity(new Intent(this, IntroActivity.class));
		finish();
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
					Utils.changeLanguage(MainActivity.this, "en");
					handlerAfterChangeLanguage();
					break;
				case 1:
					SharePrefs.getInstance().saveFilesLanguageSetting(SharePrefs.FR_LANGUAGE);
					Utils.changeLanguage(MainActivity.this, "fr");
					handlerAfterChangeLanguage();
					break;

				}
				dialog.dismiss();
			}
		});
		AlertDialog filesLanguageDialog = builder.create();
		filesLanguageDialog.show();
	}

	/**
	 * Send email.
	 */
	private void sendEmail(String[] emails) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL, emails);
		intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
		intent.putExtra(Intent.EXTRA_TEXT, "body text");
		startActivity(Intent.createChooser(intent, "Send email..."));
	}

	/**
	 * Handle event when click on Contact Us menu.
	 */
	private void handlerContact() {
		final Intent intentCall = new Intent(Intent.ACTION_DIAL);
		final String[] emailsContact = new String[] { "adaveluy@cendrex.com", "bdesjardins@cendrex.com" };
		if (Utils.isTablet(this)) {
			sendEmail(emailsContact);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Make your selection");
			builder.setItems(mItemsContact, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0) {
						startActivity(intentCall);
					} else {
						sendEmail(emailsContact);
					}
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	/**
	 * Open file.
	 * 
	 * @param type
	 */
	private void openFile(int type, int order) {
		String languageFile = SharePrefs.getInstance().getFilesLanguageSetting();
		Intent intentOpenFile = new Intent(this, MuPDFActivity.class);
		Uri uri = null;
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && LIBRARY_TYPE == type) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator
					+ "CENDREX_Access_Doors_Catalog_2014_HR_Doc_en.pdf").getAbsolutePath()));
			intentOpenFile.putExtra(Consts.OPEN_LIBRARY_FILE, true);
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && LIBRARY_TYPE == type) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator
					+ "CENDREX_Porte_Acces_Catalogue_2014_HR_Doc_fr.pdf").getAbsolutePath()));
			intentOpenFile.putExtra(Consts.OPEN_LIBRARY_FILE, true);
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && FIRST == order) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "CENDREX_Advantage_2013_Pitch_en_1.pdf")
					.getAbsolutePath()));
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && SECOND == order) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "CENDREX_CTA_flyer_Pitch_en_2.pdf")
					.getAbsolutePath()));
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && FIRST == order) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "CENDREX_Avantage_2013_Pitch_fr_1.pdf")
					.getAbsolutePath()));
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && SECOND == order) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "CENDREX_CTA_flyer_Pitch_fr_2.pdf")
					.getAbsolutePath()));
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && type == NEW_TYPE) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "New_Products_en.pdf").getAbsolutePath()));
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && type == NEW_TYPE) {
			uri = Uri.parse((new File(Consts.APP_FOLDER + File.separator + "Nouveaux_Produits_fr.pdf")
					.getAbsolutePath()));
		}

		intentOpenFile.setAction(Intent.ACTION_VIEW);
		intentOpenFile.setData(uri);
		startActivity(intentOpenFile);
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
		case ADVANTAGES_TYPE:
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
					// Open PITCH file 1 in English or French.
					openFile(ADVANTAGES_TYPE, FIRST);
					dialog.dismiss();
				}
			});
			rlFile2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Open PITCH file 2 in English or French.
					openFile(ADVANTAGES_TYPE, SECOND);
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
					// Open Video file.
					Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
					startActivity(intent);
					dialog.dismiss();
				}
			});
			rlFile2.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Open NEW file in English or French.
					openFile(NEW_TYPE, 0);
					dialog.dismiss();
				}
			});
			break;
		}

		dialog.show();
	}

	/**
	 * Copy all assets files to storage.
	 */
	private void copyAssets() {
		new CopyAssetsAsyn(this).execute();
	}

	/**
	 * Copy file.
	 * 
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize view.
		init();

		// Copy assets to internal storage.
		copyAssets();
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
		case R.id.imgShare:
			mLlShare.setVisibility(View.VISIBLE);
			break;
		case R.id.imgSetting:
			showFilesLanguageSetting();
			break;
		case R.id.tvAdvantages:
			mViewOverlap.setVisibility(View.VISIBLE);
			mRlAdvantagesInfo.setVisibility(View.VISIBLE);
			break;
		case R.id.tvLibrary:
			// Open Doc file.
			openFile(LIBRARY_TYPE, 0);
			break;
		case R.id.tvNew:
			showFilesToOpen(NEW_TYPE);
			break;
		case R.id.tvContact:
			handlerContact();
			break;
		case R.id.llShare:
			if (getCurrentFocus() != null) {
				Utils.hideKeyboard(this, getCurrentFocus());
			}
			mLlShare.setVisibility(View.GONE);
			break;
		case R.id.tvShare:
			if (TextUtils.isEmpty(mEtEmailShare.getText())) {
				Toast.makeText(this, "Please enter recipient", Toast.LENGTH_SHORT).show();
			} else {
				if (getCurrentFocus() != null) {
					Utils.hideKeyboard(this, getCurrentFocus());
				}
				mLlShare.setVisibility(View.GONE);
				Utils.shareEmail(mEtEmailShare.getText().toString());
				sendEmail(new String[] { mEtEmailShare.getText().toString() });
			}
			break;
		case R.id.imgClose:
			mViewOverlap.setVisibility(View.GONE);
			mRlAdvantagesInfo.setVisibility(View.GONE);
			break;
		case R.id.imgTitle:
			mSvInfo.setVisibility(View.VISIBLE);
			mViewOverlap.setVisibility(View.VISIBLE);
			break;
		case R.id.viewOverlap:
			if (mSvInfo.getVisibility() == View.VISIBLE) {
				mViewOverlap.setVisibility(View.GONE);
				mSvInfo.setVisibility(View.GONE);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Class copy all assets files.
	 */
	private class CopyAssetsAsyn extends AsyncTask<Void, Void, Void> {

		private Context context;
		private ProgressDialog dialog;

		private CopyAssetsAsyn(Context context) {
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(context, null, getResources().getString(R.string.loading), true, false);
		}

		@Override
		protected Void doInBackground(Void... params) {
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
					File outFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
							+ File.separator + "cendrex" + File.separator, filename);
					if (!outFile.exists()) {
						out = new FileOutputStream(outFile);
						copyFile(in, out);
						out.flush();
						out.close();
						out = null;
					}
					in.close();
					in = null;
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}
}
