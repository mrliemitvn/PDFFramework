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
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
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
	private static final int NORMAL_BACKGROUND_TYPE = 0;
	private static final int TOOL_BACKGROUND_TYPE = 1;
	private static final int CALIFORNIA_NORTH = 1;
	private static final int CALIFORNIA_SOUTH = 2;
	private static final int COLORADO = 3;
	private static final int TEXAS = 4;
	private static final int ILLINOIS = 5;
	private static final int FLORIDA = 6;
	private static final int PHILADELPHIA = 7;
	private static final int MONTREAL = 8;

	private int mBackgroundType = 0;

	/* View elements. */
	private ImageView mImgBackground;
	private ImageView mImgShare;
	private TextView mTvSetting;
	private TextView mTvArchitects;
	private TextView mTvDistributors;
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
	private int mScreenWidth;
	private int mScreenHeight;
	private int mStatusBarHeight;
	private int mImageWidth;
	private int mXminCaliforniaNorth;
	private int mXmaxCaliforniaNorth;
	private int mYminCaliforniaNorth;
	private int mYmaxCaliforniaNorth;
	private int mXminCaliforniaSouth;
	private int mXmaxCaliforniaSouth;
	private int mYminCaliforniaSouth;
	private int mYmaxCaliforniaSouth;
	private int mXminColorado;
	private int mXmaxColorado;
	private int mYminColorado;
	private int mYmaxColorado;
	private int mXminTexas;
	private int mXmaxTexas;
	private int mYminTexas;
	private int mYmaxTexas;
	private int mXminIllinois;
	private int mXmaxIllinois;
	private int mYminIllinois;
	private int mYmaxIllinois;
	private int mXminFlorida;
	private int mXmaxFlorida;
	private int mYminFlorida;
	private int mYmaxFlorida;
	private int mXminPhiladelphia;
	private int mXmaxPhiladelphia;
	private int mYminPhiladelphia;
	private int mYmaxPhiladelphia;
	private int mXminMontreal;
	private int mXmaxMontreal;
	private int mYminMontreal;
	private int mYmaxMontreal;
	private int mXminAdvantages;
	private int mXmaxAdvantages;
	private int mYminAdvantages;
	private int mYmaxAdvantages;
	private int mXminLibrary;
	private int mXmaxLibrary;
	private int mYminLibrary;
	private int mYmaxLibrary;
	private int mXminNew;
	private int mXmaxNew;
	private int mYminNew;
	private int mYmaxNew;
	private int mXminContact;
	private int mXmaxContact;
	private int mYminContact;
	private int mYmaxContact;

	/**
	 * Initialize view.
	 */
	private void init() {
		mImgBackground = (ImageView) findViewById(R.id.imgBackground);
		mImgShare = (ImageView) findViewById(R.id.imgShare);
		mTvSetting = (TextView) findViewById(R.id.tvSetting);
		// mTvAdvantages = (TextView) findViewById(R.id.tvAdvantages);
		// mTvLibrary = (TextView) findViewById(R.id.tvLibrary);
		// mTvNew = (TextView) findViewById(R.id.tvNew);
		// mTvContact = (TextView) findViewById(R.id.tvContact);
		mLlShare = (LinearLayout) findViewById(R.id.llShare);
		mTvShare = (TextView) findViewById(R.id.tvShare);
		mEtEmailShare = (EditText) findViewById(R.id.etEmailShare);
		mViewOverlap = (View) findViewById(R.id.viewOverlap);
		mRlAdvantagesInfo = (RelativeLayout) findViewById(R.id.rlAdvantagesInfo);
		mImgClose = (ImageView) findViewById(R.id.imgClose);
		mLvAdvantagesInfo = (ListView) findViewById(R.id.lvAdvantagesInfo);
		mSvInfo = (ScrollView) findViewById(R.id.svInfo);
		mImgTitle = (ImageView) findViewById(R.id.imgTitle);
		mTvArchitects = (TextView) findViewById(R.id.tvArchitects);
		mTvDistributors = (TextView) findViewById(R.id.tvDistributors);

		mImgShare.setOnClickListener(this);
		mTvSetting.setOnClickListener(this);
		mLlShare.setOnClickListener(this);
		mTvShare.setOnClickListener(this);
		mViewOverlap.setOnClickListener(this);
		mImgClose.setOnClickListener(this);
		mImgTitle.setOnClickListener(this);
		mTvArchitects.setOnClickListener(this);
		mTvDistributors.setOnClickListener(this);

		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			// Current setting is English, set text setting is French.
			mTvSetting.setText(R.string.setting_language_fr);
		} else {
			// Current setting is French, set text setting is English.
			mTvSetting.setText(R.string.setting_language_en);
		}

		changeBackgroundImage(NORMAL_BACKGROUND_TYPE);

		loadAdvantages();
	}

	/**
	 * Change image background.
	 * 
	 * @param backgroundType
	 *            background type
	 */
	private void changeBackgroundImage(int backgroundType) {
		if (Utils.isTablet(this)) {
			if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
				if (NORMAL_BACKGROUND_TYPE == backgroundType) {
					mImgBackground.setImageResource(R.drawable.bg_tablet_en);
				} else {
					mImgBackground.setImageResource(R.drawable.bg_tablet_tool_en);
				}
			} else {
				if (NORMAL_BACKGROUND_TYPE == backgroundType) {
					mImgBackground.setImageResource(R.drawable.bg_tablet_fr);
				} else {
					mImgBackground.setImageResource(R.drawable.bg_tablet_tool_fr);
				}
			}
		} else {
			if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
				if (NORMAL_BACKGROUND_TYPE == backgroundType) {
					mImgBackground.setImageResource(R.drawable.bg_phone_en);
				} else {
					mImgBackground.setImageResource(R.drawable.bg_phone_tool_en);
				}
			} else {
				if (NORMAL_BACKGROUND_TYPE == backgroundType) {
					mImgBackground.setImageResource(R.drawable.bg_phone_fr);
				} else {
					mImgBackground.setImageResource(R.drawable.bg_phone_tool_fr);
				}
			}
		}
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
		startActivity(new Intent(this, IntroActivity.class));
		finish();
	}

	/**
	 * Show alert dialog for setting files language.
	 */
	private void showFilesLanguageSetting() {
		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			// Current is English, change to French.
			SharePrefs.getInstance().saveFilesLanguageSetting(SharePrefs.FR_LANGUAGE);
			Utils.changeLanguage(MainActivity.this, "fr");
		} else {
			// Current is French, change to English.
			SharePrefs.getInstance().saveFilesLanguageSetting(SharePrefs.EN_LANGUAGE);
			Utils.changeLanguage(MainActivity.this, "en");
		}
		handlerAfterChangeLanguage();
	}

	/**
	 * Send email.
	 */
	private void sendEmail(String[] emails) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_EMAIL, emails);
		intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
		intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.email_message));
		startActivity(Intent.createChooser(intent, "Send email..."));
	}

	/**
	 * Handle event when click on Contact Us menu.
	 */
	private void handlerContact() {
		final Intent intentCall = new Intent(Intent.ACTION_DIAL);
		final String[] emailsContact = new String[] { Consts.EMAIL_CONTACT };
		if (Utils.isTablet(this)) {
			sendEmail(emailsContact);
		} else {
			final Dialog dialog = new Dialog(this);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.layout_contact_selection);

			// Set dialog content.
			TextView tvMail = (TextView) dialog.findViewById(R.id.tvMail);
			TextView tvTel = (TextView) dialog.findViewById(R.id.tvTel);

			tvMail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Send email.
					sendEmail(emailsContact);
					dialog.dismiss();
				}
			});
			tvTel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// Phone call.
					startActivity(intentCall);
					dialog.dismiss();
				}
			});

			dialog.show();
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
		String resourcePath = Utils.getMountedObbFile(this);
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && LIBRARY_TYPE == type) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_Access_Doors_Catalog_2014_HR_Doc_en.pdf");
			intentOpenFile.putExtra(Consts.OPEN_LIBRARY_FILE, true);
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && LIBRARY_TYPE == type) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_Porte_Acces_Catalogue_2014_HR_Doc_fr.pdf");
			intentOpenFile.putExtra(Consts.OPEN_LIBRARY_FILE, true);
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && FIRST == order) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_Advantage_2013_Pitch_en_1.pdf");
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && SECOND == order) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_CTA_flyer_Pitch_en_2.pdf");
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && FIRST == order) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_Avantage_2013_Pitch_fr_1.pdf");
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && ADVANTAGES_TYPE == type && SECOND == order) {
			uri = Uri.parse(resourcePath + File.separator + "CENDREX_CTA_flyer_Pitch_fr_2.pdf");
		}
		if (SharePrefs.EN_LANGUAGE.equals(languageFile) && type == NEW_TYPE) {
			uri = Uri.parse(resourcePath + File.separator + "New_Products_en.pdf");
		}
		if (SharePrefs.FR_LANGUAGE.equals(languageFile) && type == NEW_TYPE) {
			uri = Uri.parse(resourcePath + File.separator + "Nouveaux_Produits_fr.pdf");
		}

		intentOpenFile.setAction(Intent.ACTION_VIEW);
		intentOpenFile.setData(uri);
		startActivity(intentOpenFile);
	}

	/**
	 * Show office address.
	 * 
	 * @param officeNumber
	 */
	private void showOfficeAddress(int officeNumber) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_office_address);

		// Set dialog content.
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tvTitle);
		TextView tvAddress = (TextView) dialog.findViewById(R.id.tvAddress);

		switch (officeNumber) {
		case CALIFORNIA_NORTH:
			tvTitle.setText(R.string.california_north_title);
			tvAddress.setText(R.string.california_north_address);
			break;
		case CALIFORNIA_SOUTH:
			tvTitle.setText(R.string.california_south_title);
			tvAddress.setText(R.string.california_south_address);
			break;
		case COLORADO:
			tvTitle.setText(R.string.colorado_title);
			tvAddress.setText(R.string.colorado_address);
			break;
		case TEXAS:
			tvTitle.setText(R.string.texas_title);
			tvAddress.setText(R.string.texas_address);
			break;
		case ILLINOIS:
			tvTitle.setText(R.string.illinois_title);
			tvAddress.setText(R.string.illinois_address);
			break;
		case FLORIDA:
			tvTitle.setText(R.string.florida_title);
			tvAddress.setText(R.string.florida_address);
			break;
		case PHILADELPHIA:
			tvTitle.setText(R.string.philadelphia_title);
			tvAddress.setText(R.string.philadelphia_address);
			break;
		default:
			tvTitle.setText(R.string.montreal_title);
			tvAddress.setText(R.string.montreal_address);
			break;
		}

		dialog.show();
	}

	/**
	 * Show Bim objects logo.
	 */
	private void showBimObjectsLogo() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_advantages_logo);

		// Set dialog content.
		ImageView imgSpecLogo = (ImageView) dialog.findViewById(R.id.imgSpecLogo);
		ImageView imgMasterLogo = (ImageView) dialog.findViewById(R.id.imgMasterLogo);
		ImageView imgAgentLogo = (ImageView) dialog.findViewById(R.id.imgAgentLogo);
		ImageView imgAutoDeskLogo = (ImageView) dialog.findViewById(R.id.imgAutoDeskLogo);

		imgSpecLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open spec link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(Consts.URL, Consts.SPECLINK_URL);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		imgMasterLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open master link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(Consts.URL, Consts.MASTER_URL);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		imgAgentLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open agent link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(Consts.URL, Consts.AGENT_URL);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		imgAutoDeskLogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open auto desk link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(Consts.URL, Consts.AUTO_DESK_URL);
				startActivity(intent);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	/**
	 * Show catalog and LEED link selection.
	 */
	private void showCatalogAndLeedLinkSelection() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_document_selection);

		// Set dialog content.
		TextView tvCatalog = (TextView) dialog.findViewById(R.id.tvCatalog);
		TextView tvLeedLink = (TextView) dialog.findViewById(R.id.tvLeedLink);

		tvCatalog.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open catalog file.
				openFile(LIBRARY_TYPE, 0);
				dialog.dismiss();
			}
		});
		tvLeedLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open LEED link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
					intent.putExtra(Consts.URL, Consts.LEED_LINK_EN_URL);
				} else {
					intent.putExtra(Consts.URL, Consts.LEED_LINK_FR_URL);
				}
				startActivity(intent);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	/**
	 * Show AIA logo.
	 */
	private void showAIALogo() {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.view_aia_logo);

		// Set dialog content.
		ImageView imgAIALogo = (ImageView) dialog.findViewById(R.id.imgAIALogo);

		imgAIALogo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open AIA link.
				Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra(Consts.URL, Consts.AIA_URL);
				startActivity(intent);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	/**
	 * Show file name to open.
	 * 
	 * @param type
	 */
	private void showFilesToOpen(int type) {
		final Dialog dialog = new Dialog(this);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.layout_new_product);

		// Set dialog content.
		TextView tvPlayVideo = (TextView) dialog.findViewById(R.id.tvPlayVideo);
		TextView tvNewProduct = (TextView) dialog.findViewById(R.id.tvNewProduct);

		tvPlayVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open Video file.
				Intent intent = new Intent(MainActivity.this, PlayVideoActivity.class);
				startActivity(intent);
				dialog.dismiss();
			}
		});
		tvNewProduct.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Open NEW file in English or French.
				openFile(NEW_TYPE, 0);
				dialog.dismiss();
			}
		});

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
		// Current version use obb file to store resources. So we didn't need this action.
		// copyAssets();

		mStatusBarHeight = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			mStatusBarHeight = getResources().getDimensionPixelSize(resourceId);
		}
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		mScreenWidth = size.x;
		mScreenHeight = size.y;
		if (Utils.isTablet(this)) {
			mImageWidth = Consts.IMAGE_WIDTH_TABLET;
			mXminCaliforniaNorth = Consts.XMIN_CALIFORNIA_NORTH_TABLET;
			mXmaxCaliforniaNorth = Consts.XMAX_CALIFORNIA_NORTH_TABLET;
			mYminCaliforniaNorth = Consts.YMIN_CALIFORNIA_NORTH_TABLET;
			mYmaxCaliforniaNorth = Consts.YMAX_CALIFORNIA_NORTH_TABLET;
			mXminCaliforniaSouth = Consts.XMIN_CALIFORNIA_SOUTH_TABLET;
			mXmaxCaliforniaSouth = Consts.XMAX_CALIFORNIA_SOUTH_TABLET;
			mYminCaliforniaSouth = Consts.YMIN_CALIFORNIA_SOUTH_TABLET;
			mYmaxCaliforniaSouth = Consts.YMAX_CALIFORNIA_SOUTH_TABLET;
			mXminColorado = Consts.XMIN_COLORADO_TABLET;
			mXmaxColorado = Consts.XMAX_COLORADO_TABLET;
			mYminColorado = Consts.YMIN_COLORADO_TABLET;
			mYmaxColorado = Consts.YMAX_COLORADO_TABLET;
			mXminTexas = Consts.XMIN_TEXAS_TABLET;
			mXmaxTexas = Consts.XMAX_TEXAS_TABLET;
			mYminTexas = Consts.YMIN_TEXAS_TABLET;
			mYmaxTexas = Consts.YMAX_TEXAS_TABLET;
			mXminIllinois = Consts.XMIN_ILLINOIS_TABLET;
			mXmaxIllinois = Consts.XMAX_ILLINOIS_TABLET;
			mYminIllinois = Consts.YMIN_ILLINOIS_TABLET;
			mYmaxIllinois = Consts.YMAX_ILLINOIS_TABLET;
			mXminFlorida = Consts.XMIN_FLORIDA_TABLET;
			mXmaxFlorida = Consts.XMAX_FLORIDA_TABLET;
			mYminFlorida = Consts.YMIN_FLORIDA_TABLET;
			mYmaxFlorida = Consts.YMAX_FLORIDA_TABLET;
			mXminPhiladelphia = Consts.XMIN_PHILADELPHIA_TABLET;
			mXmaxPhiladelphia = Consts.XMAX_PHILADELPHIA_TABLET;
			mYminPhiladelphia = Consts.YMIN_PHILADELPHIA_TABLET;
			mYmaxPhiladelphia = Consts.YMAX_PHILADELPHIA_TABLET;
			mXminMontreal = Consts.XMIN_MONTREAL_TABLET;
			mXmaxMontreal = Consts.XMAX_MONTREAL_TABLET;
			mYminMontreal = Consts.YMIN_MONTREAL_TABLET;
			mYmaxMontreal = Consts.YMAX_MONTREAL_TABLET;
			mXminAdvantages = Consts.XMIN_ADVANTAGES_TABLET;
			mXmaxAdvantages = Consts.XMAX_ADVANTAGES_TABLET;
			mYminAdvantages = Consts.YMIN_ADVANTAGES_TABLET;
			mYmaxAdvantages = Consts.YMAX_ADVANTAGES_TABLET;
			mXminLibrary = Consts.XMIN_LIBRARY_TABLET;
			mXmaxLibrary = Consts.XMAX_LIBRARY_TABLET;
			mYminLibrary = Consts.YMIN_LIBRARY_TABLET;
			mYmaxLibrary = Consts.YMAX_LIBRARY_TABLET;
			mXminNew = Consts.XMIN_NEW_TABLET;
			mXmaxNew = Consts.XMAX_NEW_TABLET;
			mYminNew = Consts.YMIN_NEW_TABLET;
			mYmaxNew = Consts.YMAX_NEW_TABLET;
			mXminContact = Consts.XMIN_CONTACT_TABLET;
			mXmaxContact = Consts.XMAX_CONTACT_TABLET;
			mYminContact = Consts.YMIN_CONTACT_TABLET;
			mYmaxContact = Consts.YMAX_CONTACT_TABLET;
		} else {
			mImageWidth = Consts.IMAGE_WIDTH_PHONE;
			mXminCaliforniaNorth = Consts.XMIN_CALIFORNIA_NORTH_PHONE;
			mXmaxCaliforniaNorth = Consts.XMAX_CALIFORNIA_NORTH_PHONE;
			mYminCaliforniaNorth = Consts.YMIN_CALIFORNIA_NORTH_PHONE;
			mYmaxCaliforniaNorth = Consts.YMAX_CALIFORNIA_NORTH_PHONE;
			mXminCaliforniaSouth = Consts.XMIN_CALIFORNIA_SOUTH_PHONE;
			mXmaxCaliforniaSouth = Consts.XMAX_CALIFORNIA_SOUTH_PHONE;
			mYminCaliforniaSouth = Consts.YMIN_CALIFORNIA_SOUTH_PHONE;
			mYmaxCaliforniaSouth = Consts.YMAX_CALIFORNIA_SOUTH_PHONE;
			mXminColorado = Consts.XMIN_COLORADO_PHONE;
			mXmaxColorado = Consts.XMAX_COLORADO_PHONE;
			mYminColorado = Consts.YMIN_COLORADO_PHONE;
			mYmaxColorado = Consts.YMAX_COLORADO_PHONE;
			mXminTexas = Consts.XMIN_TEXAS_PHONE;
			mXmaxTexas = Consts.XMAX_TEXAS_PHONE;
			mYminTexas = Consts.YMIN_TEXAS_PHONE;
			mYmaxTexas = Consts.YMAX_TEXAS_PHONE;
			mXminIllinois = Consts.XMIN_ILLINOIS_PHONE;
			mXmaxIllinois = Consts.XMAX_ILLINOIS_PHONE;
			mYminIllinois = Consts.YMIN_ILLINOIS_PHONE;
			mYmaxIllinois = Consts.YMAX_ILLINOIS_PHONE;
			mXminFlorida = Consts.XMIN_FLORIDA_PHONE;
			mXmaxFlorida = Consts.XMAX_FLORIDA_PHONE;
			mYminFlorida = Consts.YMIN_FLORIDA_PHONE;
			mYmaxFlorida = Consts.YMAX_FLORIDA_PHONE;
			mXminPhiladelphia = Consts.XMIN_PHILADELPHIA_PHONE;
			mXmaxPhiladelphia = Consts.XMAX_PHILADELPHIA_PHONE;
			mYminPhiladelphia = Consts.YMIN_PHILADELPHIA_PHONE;
			mYmaxPhiladelphia = Consts.YMAX_PHILADELPHIA_PHONE;
			mXminMontreal = Consts.XMIN_MONTREAL_PHONE;
			mXmaxMontreal = Consts.XMAX_MONTREAL_PHONE;
			mYminMontreal = Consts.YMIN_MONTREAL_PHONE;
			mYmaxMontreal = Consts.YMAX_MONTREAL_PHONE;
			mXminAdvantages = Consts.XMIN_ADVANTAGES_PHONE;
			mXmaxAdvantages = Consts.XMAX_ADVANTAGES_PHONE;
			mYminAdvantages = Consts.YMIN_ADVANTAGES_PHONE;
			mYmaxAdvantages = Consts.YMAX_ADVANTAGES_PHONE;
			mXminLibrary = Consts.XMIN_LIBRARY_PHONE;
			mXmaxLibrary = Consts.XMAX_LIBRARY_PHONE;
			mYminLibrary = Consts.YMIN_LIBRARY_PHONE;
			mYmaxLibrary = Consts.YMAX_LIBRARY_PHONE;
			mXminNew = Consts.XMIN_NEW_PHONE;
			mXmaxNew = Consts.XMAX_NEW_PHONE;
			mYminNew = Consts.YMIN_NEW_PHONE;
			mYmaxNew = Consts.YMAX_NEW_PHONE;
			mXminContact = Consts.XMIN_CONTACT_PHONE;
			mXmaxContact = Consts.XMAX_CONTACT_PHONE;
			mYminContact = Consts.YMIN_CONTACT_PHONE;
			mYmaxContact = Consts.YMAX_CONTACT_PHONE;
		}
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
		case R.id.tvSetting:
			showFilesLanguageSetting();
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
				mEtEmailShare.setText("");
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
		case R.id.tvArchitects:
			// Hide mTvTool, show mTvBack.
			// Change background image to tool image, change mBackgroundType to TOOL_BACKGROUND_TYPE.
			mTvArchitects.setVisibility(View.GONE);
			mTvDistributors.setVisibility(View.VISIBLE);
			changeBackgroundImage(TOOL_BACKGROUND_TYPE);
			mBackgroundType = TOOL_BACKGROUND_TYPE;
			break;
		case R.id.tvDistributors:
			// Show mTvTool, hide mTvBack.
			// Change background image to normal image, change mBackgroundType to NORMAL_BACKGROUND_TYPE.
			mTvArchitects.setVisibility(View.VISIBLE);
			mTvDistributors.setVisibility(View.GONE);
			changeBackgroundImage(NORMAL_BACKGROUND_TYPE);
			mBackgroundType = NORMAL_BACKGROUND_TYPE;
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		Log.e("Test", "x = " + x + " y = " + y);
		y = y - mStatusBarHeight;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			if (x > (mScreenWidth / 2 + mXminAdvantages * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxAdvantages * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminAdvantages * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxAdvantages * mScreenWidth / mImageWidth)) {
				if (NORMAL_BACKGROUND_TYPE == mBackgroundType) {
					mViewOverlap.setVisibility(View.VISIBLE);
					mRlAdvantagesInfo.setVisibility(View.VISIBLE);
				} else {
					// Show Bim objects logo.
					showBimObjectsLogo();
				}
			} else if (x > (mScreenWidth / 2 + mXminLibrary * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxLibrary * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminLibrary * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxLibrary * mScreenWidth / mImageWidth)) {
				if (NORMAL_BACKGROUND_TYPE == mBackgroundType) {
					// Open Doc file.
					openFile(LIBRARY_TYPE, 0);
				} else {
					// Show a selection: open catalog file or open LEED link.
					showCatalogAndLeedLinkSelection();
				}
			} else if (x > (mScreenWidth / 2 + mXminNew * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxNew * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminNew * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxNew * mScreenWidth / mImageWidth)) {
				if (NORMAL_BACKGROUND_TYPE == mBackgroundType) {
					showFilesToOpen(NEW_TYPE);
				} else {
					// Show AIA logo.
					showAIALogo();
				}
			} else if (x > (mScreenWidth / 2 + mXminContact * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxContact * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminContact * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxContact * mScreenWidth / mImageWidth)) {
				handlerContact();
			} else if (x > (mScreenWidth / 2 + mXminCaliforniaNorth * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxCaliforniaNorth * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminCaliforniaNorth * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxCaliforniaNorth * mScreenWidth / mImageWidth)) {
				showOfficeAddress(CALIFORNIA_NORTH);
			} else if (x > (mScreenWidth / 2 + mXminCaliforniaSouth * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxCaliforniaSouth * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminCaliforniaSouth * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxCaliforniaSouth * mScreenWidth / mImageWidth)) {
				showOfficeAddress(CALIFORNIA_SOUTH);
			} else if (x > (mScreenWidth / 2 + mXminColorado * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxColorado * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminColorado * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxColorado * mScreenWidth / mImageWidth)) {
				showOfficeAddress(COLORADO);
			} else if (x > (mScreenWidth / 2 + mXminTexas * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxTexas * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminTexas * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxTexas * mScreenWidth / mImageWidth)) {
				showOfficeAddress(TEXAS);
			} else if (x > (mScreenWidth / 2 + mXminIllinois * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxIllinois * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminIllinois * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxIllinois * mScreenWidth / mImageWidth)) {
				showOfficeAddress(ILLINOIS);
			} else if (x > (mScreenWidth / 2 + mXminFlorida * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxFlorida * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminFlorida * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxFlorida * mScreenWidth / mImageWidth)) {
				showOfficeAddress(FLORIDA);
			} else if (x > (mScreenWidth / 2 + mXminPhiladelphia * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxPhiladelphia * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminPhiladelphia * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxPhiladelphia * mScreenWidth / mImageWidth)) {
				showOfficeAddress(PHILADELPHIA);
			} else if (x > (mScreenWidth / 2 + mXminMontreal * mScreenWidth / mImageWidth)
					&& x < (mScreenWidth / 2 + mXmaxMontreal * mScreenWidth / mImageWidth)
					&& y > (mScreenHeight / 2 + mYminMontreal * mScreenWidth / mImageWidth)
					&& y < (mScreenHeight / 2 + mYmaxMontreal * mScreenWidth / mImageWidth)) {
				showOfficeAddress(MONTREAL);
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
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
