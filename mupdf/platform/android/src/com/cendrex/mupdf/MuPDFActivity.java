package com.cendrex.mupdf;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.SettingNotFoundException;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.cendrex.R;
import com.cendrex.adapter.InfoFilesAdapter;
import com.cendrex.adapter.TableContentsAdapter;
import com.cendrex.resource.InfoResource;
import com.cendrex.resource.TableOfContents;
import com.cendrex.utils.Consts;
import com.cendrex.utils.SharePrefs;
import com.cendrex.utils.Utils;

class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity implements FilePicker.FilePickerSupport {
	/* The core rendering instance */
	enum TopBarMode {
		Main, Search, Annot, Delete, More, Accept
	};

	enum AcceptMode {
		Highlight, Underline, StrikeOut, Ink, CopyText
	};

	private final int OUTLINE_REQUEST = 0;
	private final int PRINT_REQUEST = 1;
	private final int FILEPICK_REQUEST = 2;
	private MuPDFCore core;
	private String mFileName;
	private MuPDFReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;
	private EditText mPasswordView;
	private TextView mFilenameView;
	private SeekBar mPageSlider;
	private int mPageSliderRes;
	private TextView mPageNumberView;
	private TextView mInfoView;
	private ImageButton mSearchButton;
	private ImageButton mReflowButton;
	private ImageButton mOutlineButton;
	private ImageButton mMoreButton;
	private TextView mAnnotTypeText;
	private ImageButton mAnnotButton;
	private ViewAnimator mTopBarSwitcher;
	private ImageButton mLinkButton;
	private TopBarMode mTopBarMode = TopBarMode.Main;
	private AcceptMode mAcceptMode;
	private ImageButton mSearchBack;
	private ImageButton mSearchFwd;
	private EditText mSearchText;
	private ImageButton mBrightnessButton;
	private SeekBar mBrightnessSlide;
	private ImageButton mEmailButton;
	private ImageButton mInfoButton;
	private SearchTask mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private boolean mLinkHighlight = false;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive = false;
	private boolean mReflow = false;
	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;
	private FilePicker mFilePicker;

	private RelativeLayout mRlTableContents;
	private ListView mLvTableContents;
	private TableContentsAdapter tableContentsAdapter;
	private ArrayList<TableOfContents> listTableOfContents;

	private RelativeLayout mRlInfoFiles;
	private ListView mLvInfoFiles;
	private InfoFilesAdapter infoFilesAdapter;
	private ArrayList<InfoResource> listInfoResources;

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid stalling
		// the UI. Some calls can lead to javascript-invoked requests to display an
		// alert dialog and collect a reply from the user. The task has to be blocked
		// until the user's reply is received. This method creates an asynchronous task,
		// the purpose of which is to wait of these requests and produce the dialog
		// in response, while leaving the core blocked. When the dialog receives the
		// user's response, it is sent to the core via replyToAlert, unblocking it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive) return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null) return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1:
								index = 0;
								break;
							case AlertDialog.BUTTON2:
								index = 1;
								break;
							case AlertDialog.BUTTON3:
								index = 2;
								break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType) {
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.cancel), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.okay), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3, getString(R.string.cancel), listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON1, getString(R.string.yes), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2, getString(R.string.no), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(this, path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[]) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(this, buffer);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore) getLastNonConfigurationInstance();

			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			Intent intent = getIntent();
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file manager
					// Hopefully other file managers will use this same scheme, if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri, new String[] { "_data" }, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String reason = null;
						if (str == null) {
							try {
								InputStream is = getContentResolver().openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							} catch (java.lang.OutOfMemoryError e) {
								System.out.println("Out of memory during buffer reading");
								reason = e.toString();
							} catch (Exception e) {
								reason = e.toString();
							}
							if (reason != null) {
								buffer = null;
								Resources res = getResources();
								AlertDialog alert = mAlertBuilder.create();
								setTitle(String.format(res.getString(R.string.mupdf_cannot_open_document_Reason),
										reason));
								alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
										new DialogInterface.OnClickListener() {
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										});
								alert.show();
								return;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				requestPassword(savedInstanceState);
				return;
			}
			if (core != null && core.countPages() == 0) {
				core = null;
			}
		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.mupdf_cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}

		createUI(savedInstanceState);
	}

	public void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(this);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.mupdf_enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.okay), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (core.authenticatePassword(mPasswordView.getText().toString())) {
					createUI(savedInstanceState);
				} else {
					requestPassword(savedInstanceState);
				}
			}
		});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.show();
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null) return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null) return;
				mPageNumberView.setText(String.format("%d / %d", i + 1, core.countPages()));
				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
				} else {
					if (mTopBarMode == TopBarMode.Main) hideButtons();
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
			}

			@Override
			protected void onHit(Hit item) {
				switch (mTopBarMode) {
				case Annot:
					if (item == Hit.Annotation) {
						showButtons();
						mTopBarMode = TopBarMode.Delete;
						mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					}
					break;
				case Delete:
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
					if (pageView != null) pageView.deselectAnnotation();
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, this, core));

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Set the file-name text
		mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2) / mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				updatePageNumView((progress + mPageSliderRes / 2) / mPageSliderRes);
			}
		});

		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});

		// Activate the reflow button
		mReflowButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				toggleReflow();
			}
		});

		if (core.fileFormat().startsWith("PDF")) {
			mAnnotButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mTopBarMode = TopBarMode.Annot;
					mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
				}
			});
		} else {
			mAnnotButton.setVisibility(View.GONE);
		}

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				setButtonEnabled(mSearchBack, haveText);
				setButtonEnabled(mSearchFwd, haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null
						&& !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		// React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) search(1);
				return false;
			}
		});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

		mLinkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setLinkHighlight(!mLinkHighlight);
			}
		});

		// TODO: Old outline from library.
		// if (core.hasOutline()) {
		// mOutlineButton.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// OutlineItem outline[] = core.getOutline();
		// if (outline != null) {
		// OutlineActivityData.get().items = outline;
		// Intent intent = new Intent(MuPDFActivity.this, OutlineActivity.class);
		// startActivityForResult(intent, OUTLINE_REQUEST);
		// }
		// }
		// });
		// } else {
		// mOutlineButton.setVisibility(View.GONE);
		// }
		mOutlineButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mOutlineButton.setSelected(!mOutlineButton.isSelected());
				if (mOutlineButton.isSelected()) {
					mBrightnessButton.setSelected(false);
					mInfoButton.setSelected(false);
					mBrightnessSlide.setVisibility(View.GONE);
					mLvInfoFiles.setVisibility(View.GONE);
					mLvTableContents.setVisibility(View.VISIBLE);
					mRlTableContents.setVisibility(View.VISIBLE);
				} else {
					mLvTableContents.setVisibility(View.GONE);
					mRlTableContents.setVisibility(View.GONE);
				}
			}
		});

		mInfoButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listInfoResources == null || listInfoResources.size() <= 0) return;
				if (!mInfoButton.isSelected()) {
					InfoResource infoResourceCurrentPage = null;
					for (InfoResource infoResource : listInfoResources) {
						int pageHasInfoFiles = Integer.parseInt(infoResource.page) - 1;
						int currentPage = mDocView.getDisplayedViewIndex();
						if (pageHasInfoFiles == currentPage) {
							infoResourceCurrentPage = infoResource;
							break;
						}
					}
					if (infoResourceCurrentPage == null) return;
					infoFilesAdapter = new InfoFilesAdapter(MuPDFActivity.this, infoResourceCurrentPage.listFileName);
					mBrightnessButton.setSelected(false);
					mOutlineButton.setSelected(false);
					mBrightnessSlide.setVisibility(View.GONE);
					mLvTableContents.setVisibility(View.GONE);
					mRlTableContents.setVisibility(View.GONE);
					mLvInfoFiles.setAdapter(infoFilesAdapter);
					mLvInfoFiles.setVisibility(View.VISIBLE);
					mRlInfoFiles.setVisibility(View.VISIBLE);
				} else {
					mLvInfoFiles.setVisibility(View.GONE);
					mRlInfoFiles.setVisibility(View.GONE);
				}
				mInfoButton.setSelected(!mInfoButton.isSelected());
			}
		});

		// Brightness button.
		mBrightnessButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBrightnessButton.setSelected(!mBrightnessButton.isSelected());
				if (mBrightnessButton.isSelected()) {
					mOutlineButton.setSelected(false);
					mInfoButton.setSelected(false);
					mLvTableContents.setVisibility(View.GONE);
					mLvInfoFiles.setVisibility(View.GONE);
					mRlTableContents.setVisibility(View.GONE);
					mRlInfoFiles.setVisibility(View.GONE);
					try {
						float curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(),
								android.provider.Settings.System.SCREEN_BRIGHTNESS);
						int curBrightnessPercent = (int) (curBrightnessValue / 255 * 100);
						mBrightnessSlide.setProgress(curBrightnessPercent);
					} catch (SettingNotFoundException e) {
						e.printStackTrace();
					}
					mBrightnessSlide.setVisibility(View.VISIBLE);
				} else {
					mBrightnessSlide.setVisibility(View.GONE);
				}
			}
		});

		// Brightness Slide
		mBrightnessSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (progress <= 0) return;
				float brightnessValue = (float) progress / 100;
				WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
				layoutParams.screenBrightness = brightnessValue;
				getWindow().setAttributes(layoutParams);
			}
		});

		// Email button.
		mEmailButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Uri uri = getIntent().getData();
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "email@example.com" });
				intent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
				intent.putExtra(Intent.EXTRA_TEXT, "body text");
				intent.putExtra(Intent.EXTRA_STREAM, uri);
				startActivity(Intent.createChooser(intent, "Send email..."));
			}
		});

		// List table of contents.
		mLvTableContents.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TableOfContents tableOfContents = (TableOfContents) parent.getItemAtPosition(position);
				if (tableOfContents != null) {
					mOutlineButton.setSelected(false);
					mLvTableContents.setVisibility(View.GONE);
					mRlTableContents.setVisibility(View.GONE);
					int page = Integer.parseInt(tableOfContents.page);
					if (page > 1) mDocView.setDisplayedViewIndex(page - 1);
				}
			}

		});

		// List info files.
		mLvInfoFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String fileName = (String) parent.getItemAtPosition(position);
				if (!TextUtils.isEmpty(fileName)) {
					mInfoButton.setSelected(false);
					mLvInfoFiles.setVisibility(View.GONE);
					mRlInfoFiles.setVisibility(View.GONE);
					String resourcePath = Utils.getMountedObbFile(MuPDFActivity.this);
					Uri uri = Uri.parse(resourcePath + File.separator + fileName + ".pdf");
					Intent intentOpenFile = new Intent(MuPDFActivity.this, MuPDFActivity.class);
					intentOpenFile.setAction(Intent.ACTION_VIEW);
					intentOpenFile.setData(uri);
					startActivity(intentOpenFile);
				}
			}
		});

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false)) showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false)) searchModeOn();

		if (savedInstanceState != null && savedInstanceState.getBoolean("ReflowMode", false)) reflowModeSet(true);

		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		setContentView(layout);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case OUTLINE_REQUEST:
			if (resultCode >= 0) mDocView.setDisplayedViewIndex(resultCode);
			break;
		case PRINT_REQUEST:
			if (resultCode == RESULT_CANCELED) showInfo(getString(R.string.mupdf_print_failed));
			break;
		case FILEPICK_REQUEST:
			if (mFilePicker != null && resultCode == RESULT_OK) mFilePicker.onPick(data.getData());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	private void reflowModeSet(boolean reflow) {
		mReflow = reflow;
		mDocView.setAdapter(mReflow ? new MuPDFReflowAdapter(this, core) : new MuPDFPageAdapter(this, this, core));
		mReflowButton.setColorFilter(mReflow ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
		setButtonEnabled(mAnnotButton, !reflow);
		setButtonEnabled(mSearchButton, !reflow);
		if (reflow) setLinkHighlight(false);
		setButtonEnabled(mLinkButton, !reflow);
		setButtonEnabled(mMoreButton, !reflow);
		mDocView.refresh(mReflow);
	}

	private void toggleReflow() {
		reflowModeSet(!mReflow);
		showInfo(mReflow ? getString(R.string.mupdf_entering_reflow_mode)
				: getString(R.string.mupdf_leaving_reflow_mode));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}

		if (!mButtonsVisible) outState.putBoolean("ButtonsHidden", true);

		if (mTopBarMode == TopBarMode.Search) outState.putBoolean("SearchMode", true);

		if (mReflow) outState.putBoolean("ReflowMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mSearchTask != null) mSearchTask.stop();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}

	public void onDestroy() {
		if (mDocView != null) {
			mDocView.applyToChildren(new ReaderView.ViewMapper() {
				void applyToView(View view) {
					((MuPDFView) view).releaseBitmaps();
				}
			});
		}
		if (core != null) core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
		super.onDestroy();
	}

	private void setButtonEnabled(ImageButton button, boolean enabled) {
		button.setEnabled(enabled);
		button.setColorFilter(enabled ? Color.argb(255, 255, 255, 255) : Color.argb(255, 128, 128, 128));
	}

	private void setLinkHighlight(boolean highlight) {
		mLinkHighlight = highlight;
		// LINK_COLOR tint
		mLinkButton.setColorFilter(highlight ? Color.argb(0xFF, 172, 114, 37) : Color.argb(0xFF, 255, 255, 255));
		// Inform pages of the change.
		mDocView.setLinksEnabled(highlight);
	}

	private void showButtons() {
		if (core == null) return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			mPageSlider.setProgress(index * mPageSliderRes);
			if (mTopBarMode == TopBarMode.Search) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageSlider.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
					Bundle bundle = getIntent().getExtras();
					if (bundle != null && bundle.containsKey(Consts.OPEN_LIBRARY_FILE)
							&& bundle.getBoolean(Consts.OPEN_LIBRARY_FILE, false)) {
						// Show or hide info button.
						if (mInfoButton != null && listInfoResources != null && listInfoResources.size() > 0) {
							int visible = View.GONE;
							for (InfoResource infoResource : listInfoResources) {
								int pageHasInfoFiles = Integer.parseInt(infoResource.page) - 1;
								int currentPage = mDocView.getDisplayedViewIndex();
								if (pageHasInfoFiles == currentPage) {
									visible = View.VISIBLE;
									break;
								}
							}
							mInfoButton.setVisibility(visible);
						}
					}
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			// TODO
			mBrightnessButton.setSelected(false);
			mInfoButton.setSelected(false);
			mOutlineButton.setSelected(false);
			mBrightnessSlide.setVisibility(View.GONE);
			mLvInfoFiles.setVisibility(View.GONE);
			mLvTableContents.setVisibility(View.GONE);
			mRlTableContents.setVisibility(View.GONE);
			mRlInfoFiles.setVisibility(View.GONE);

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
					mInfoButton.setVisibility(View.INVISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageSlider.setVisibility(View.INVISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void searchModeOn() {
		if (mTopBarMode != TopBarMode.Search) {
			mTopBarMode = TopBarMode.Search;
			// Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		}
	}

	private void searchModeOff() {
		if (mTopBarMode == TopBarMode.Search) {
			mTopBarMode = TopBarMode.Main;
			hideKeyboard();
			mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	private void updatePageNumView(int index) {
		if (core == null) return;
		mPageNumberView.setText(String.format("%d / %d", index + 1, core.countPages()));
	}

	private void printDoc() {
		if (!core.fileFormat().startsWith("PDF")) {
			showInfo(getString(R.string.mupdf_format_currently_not_supported));
			return;
		}

		Intent myIntent = getIntent();
		Uri docUri = myIntent != null ? myIntent.getData() : null;

		if (docUri == null) {
			showInfo(getString(R.string.mupdf_print_failed));
		}

		if (docUri.getScheme() == null) docUri = Uri.parse("file://" + docUri.toString());

		Intent printIntent = new Intent(this, PrintDialogActivity.class);
		printIntent.setDataAndType(docUri, "aplication/pdf");
		printIntent.putExtra("title", mFileName);
		startActivityForResult(printIntent, PRINT_REQUEST);
	}

	private void showInfo(String message) {
		mInfoView.setText(message);

		int currentApiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentApiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			SafeAnimatorInflater safe = new SafeAnimatorInflater((Activity) this, R.anim.mupdf_info, (View) mInfoView);
		} else {
			mInfoView.setVisibility(View.VISIBLE);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					mInfoView.setVisibility(View.INVISIBLE);
				}
			}, 500);
		}
	}

	private void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.mupdf_buttons, null);
		mFilenameView = (TextView) mButtonsView.findViewById(R.id.mupdf_docNameText);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.mupdf_pageSlider);
		mPageNumberView = (TextView) mButtonsView.findViewById(R.id.mupdf_pageNumber);
		mInfoView = (TextView) mButtonsView.findViewById(R.id.mupdf_info);
		mSearchButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_searchButton);
		mReflowButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_reflowButton);
		mOutlineButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_outlineButton);
		mAnnotButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_editAnnotButton);
		mAnnotTypeText = (TextView) mButtonsView.findViewById(R.id.mupdf_annotType);
		mTopBarSwitcher = (ViewAnimator) mButtonsView.findViewById(R.id.mupdf_switcher);
		mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.mupdf_searchBack);
		mSearchFwd = (ImageButton) mButtonsView.findViewById(R.id.mupdf_searchForward);
		mSearchText = (EditText) mButtonsView.findViewById(R.id.mupdf_searchText);
		mLinkButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_linkButton);
		mMoreButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_moreButton);
		mBrightnessButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_brightnessButton);
		mBrightnessSlide = (SeekBar) mButtonsView.findViewById(R.id.mupdf_brightnessSlider);
		mEmailButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_emailButton);
		mInfoButton = (ImageButton) mButtonsView.findViewById(R.id.mupdf_infoButton);

		mRlTableContents = (RelativeLayout) mButtonsView.findViewById(R.id.rlTableContents);
		mLvTableContents = (ListView) mButtonsView.findViewById(R.id.lvTableContents);
		int paddingMedium = getResources().getDimensionPixelSize(R.dimen.margin_medium);
		TextView tvTableContents = new TextView(this);
		tvTableContents.setText(R.string.title_table_of_contents);
		tvTableContents.setTextColor(getResources().getColor(R.color.black));
		tvTableContents.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size_medium));
		tvTableContents.setPadding(paddingMedium, paddingMedium, paddingMedium, paddingMedium);
		tvTableContents.setBackgroundResource(R.color.white);
		mLvTableContents.addHeaderView(tvTableContents);

		mRlInfoFiles = (RelativeLayout) mButtonsView.findViewById(R.id.rlInfoFiles);
		mLvInfoFiles = (ListView) mButtonsView.findViewById(R.id.lvInfoFiles);

		mOutlineButton.setSelected(false);
		mBrightnessButton.setSelected(false);
		mBrightnessSlide.setVisibility(View.GONE);
		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
		mInfoView.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.INVISIBLE);
		mRlTableContents.setVisibility(View.GONE);
		mLvTableContents.setVisibility(View.GONE);
		mRlInfoFiles.setVisibility(View.GONE);
		mLvInfoFiles.setVisibility(View.GONE);
		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			mInfoButton.setImageResource(R.drawable.btn_techdata_en_state);
		} else {
			mInfoButton.setImageResource(R.drawable.btn_techdata_fr_state);
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey(Consts.OPEN_LIBRARY_FILE)
				&& bundle.getBoolean(Consts.OPEN_LIBRARY_FILE, false)) {
			mOutlineButton.setVisibility(View.VISIBLE);
			mOutlineButton.setSelected(true);
			// Setup table of contents list view.
			setUpTableContentsListView();
			// Setup list info files.
			setUpInfoFileListView();
		} else {
			mOutlineButton.setVisibility(View.GONE);
			mInfoButton.setVisibility(View.GONE);
		}

		if (Utils.isTablet(this)) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			int height = displaymetrics.heightPixels;
			int width = displaymetrics.widthPixels;
			RelativeLayout.LayoutParams layoutParamInfo = (LayoutParams) mLvInfoFiles.getLayoutParams();
			RelativeLayout.LayoutParams layoutParamContents = (LayoutParams) mLvTableContents.getLayoutParams();
			layoutParamInfo.width = width / 2;
			layoutParamInfo.height = height / 2;
			layoutParamInfo.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			layoutParamContents.width = width / 2;
			layoutParamContents.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			mLvInfoFiles.setLayoutParams(layoutParamInfo);
			mLvTableContents.setLayoutParams(layoutParamContents);
		}
	}

	private void setUpTableContentsListView() {
		if (tableContentsAdapter != null && tableContentsAdapter.getCount() > 0) return;
		listTableOfContents = new ArrayList<TableOfContents>();
		XmlResourceParser xmlParser = null;
		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			xmlParser = getResources().getXml(R.xml.table_contents_data_en);
		} else {
			xmlParser = getResources().getXml(R.xml.table_contents_data_fr);
		}
		try {
			int eventType = xmlParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && "item".equals(xmlParser.getName())) {
					TableOfContents tableOfContents = new TableOfContents();
					tableOfContents.page = xmlParser.getAttributeValue(0);
					tableOfContents.title = xmlParser.getAttributeValue(1);
					listTableOfContents.add(tableOfContents);
				}
				eventType = xmlParser.next();
			}
			// indicate app done reading the resource.
			xmlParser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		tableContentsAdapter = new TableContentsAdapter(this, listTableOfContents);
		mLvTableContents.setAdapter(tableContentsAdapter);
		mRlTableContents.setVisibility(View.VISIBLE);
		mLvTableContents.setVisibility(View.VISIBLE);
	}

	private void setUpInfoFileListView() {
		if (listInfoResources != null && listInfoResources.size() > 0) return;
		listInfoResources = new ArrayList<InfoResource>();
		XmlResourceParser xmlParser = null;
		if (SharePrefs.EN_LANGUAGE.equals(SharePrefs.getInstance().getFilesLanguageSetting())) {
			xmlParser = getResources().getXml(R.xml.info_data_en);
		} else {
			xmlParser = getResources().getXml(R.xml.info_data_fr);
		}
		try {
			int infoButtonVisible = View.GONE;
			int eventType = xmlParser.getEventType();
			InfoResource infoResource = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG && "data".equals(xmlParser.getName())) {
					infoResource = new InfoResource();
					infoResource.page = xmlParser.getAttributeValue(0);
					listInfoResources.add(infoResource);
					
					// Show or hide info button.
					int pageHasInfoFiles = Integer.parseInt(infoResource.page) - 1;
					int currentPage = mDocView.getDisplayedViewIndex();
					if (infoButtonVisible != View.VISIBLE && pageHasInfoFiles == currentPage) infoButtonVisible = View.VISIBLE;
				} else if (eventType == XmlPullParser.TEXT) {
					if (infoResource != null) infoResource.listFileName.add(xmlParser.getText());
				}
				eventType = xmlParser.next();
			}
			mInfoButton.setVisibility(infoButtonVisible);
			// indicate app done reading the resource.
			xmlParser.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void OnMoreButtonClick(View v) {
		mTopBarMode = TopBarMode.More;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelMoreButtonClick(View v) {
		mTopBarMode = TopBarMode.Main;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnPrintButtonClick(View v) {
		printDoc();
	}

	public void OnCopyTextButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.CopyText;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(getString(R.string.mupdf_copy_text));
		showInfo(getString(R.string.mupdf_select_text));
	}

	public void OnEditAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelAnnotButtonClick(View v) {
		mTopBarMode = TopBarMode.More;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnHighlightButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Highlight;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.mupdf_highlight);
		showInfo(getString(R.string.mupdf_select_text));
	}

	public void OnUnderlineButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Underline;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.mupdf_underline);
		showInfo(getString(R.string.mupdf_select_text));
	}

	public void OnStrikeOutButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.StrikeOut;
		mDocView.setMode(MuPDFReaderView.Mode.Selecting);
		mAnnotTypeText.setText(R.string.mupdf_strike_out);
		showInfo(getString(R.string.mupdf_select_text));
	}

	public void OnInkButtonClick(View v) {
		mTopBarMode = TopBarMode.Accept;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mAcceptMode = AcceptMode.Ink;
		mDocView.setMode(MuPDFReaderView.Mode.Drawing);
		mAnnotTypeText.setText(R.string.mupdf_ink);
		showInfo(getString(R.string.mupdf_draw_annotation));
	}

	public void OnCancelAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) {
			pageView.deselectText();
			pageView.cancelDraw();
		}
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
		switch (mAcceptMode) {
		case CopyText:
			mTopBarMode = TopBarMode.More;
			break;
		default:
			mTopBarMode = TopBarMode.Annot;
			break;
		}
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnAcceptButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		boolean success = false;
		switch (mAcceptMode) {
		case CopyText:
			if (pageView != null) success = pageView.copySelection();
			mTopBarMode = TopBarMode.More;
			showInfo(success ? getString(R.string.mupdf_copied_to_clipboard)
					: getString(R.string.mupdf_no_text_selected));
			break;

		case Highlight:
			if (pageView != null) success = pageView.markupSelection(Annotation.Type.HIGHLIGHT);
			mTopBarMode = TopBarMode.Annot;
			if (!success) showInfo(getString(R.string.mupdf_no_text_selected));
			break;

		case Underline:
			if (pageView != null) success = pageView.markupSelection(Annotation.Type.UNDERLINE);
			mTopBarMode = TopBarMode.Annot;
			if (!success) showInfo(getString(R.string.mupdf_no_text_selected));
			break;

		case StrikeOut:
			if (pageView != null) success = pageView.markupSelection(Annotation.Type.STRIKEOUT);
			mTopBarMode = TopBarMode.Annot;
			if (!success) showInfo(getString(R.string.mupdf_no_text_selected));
			break;

		case Ink:
			if (pageView != null) success = pageView.saveDraw();
			mTopBarMode = TopBarMode.Annot;
			if (!success) showInfo(getString(R.string.mupdf_nothing_to_save));
			break;
		}
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
		mDocView.setMode(MuPDFReaderView.Mode.Viewing);
	}

	public void OnCancelSearchButtonClick(View v) {
		searchModeOff();
	}

	public void OnDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) pageView.deleteSelectedAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	public void OnCancelDeleteButtonClick(View v) {
		MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
		if (pageView != null) pageView.deselectAnnotation();
		mTopBarMode = TopBarMode.Annot;
		mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) imm.showSoftInput(mSearchText, 0);
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	private void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarMode == TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && mTopBarMode != TopBarMode.Search) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (core.hasChanges()) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (which == AlertDialog.BUTTON_POSITIVE) core.save();

					finish();
				}
			};
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle("MuPDF");
			alert.setMessage(getString(R.string.mupdf_document_has_changes_save_them_));
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.yes), listener);
			alert.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.no), listener);
			alert.show();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void performPickFor(FilePicker picker) {
		mFilePicker = picker;
		Intent intent = new Intent(this, ChoosePDFActivity.class);
		startActivityForResult(intent, FILEPICK_REQUEST);
	}
}
