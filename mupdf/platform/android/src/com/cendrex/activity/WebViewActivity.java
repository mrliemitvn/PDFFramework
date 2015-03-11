package com.cendrex.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cendrex.R;
import com.cendrex.utils.Consts;

public class WebViewActivity extends BaseActivity implements OnClickListener {

	private WebView mWebView;
	private ProgressBar mPbLoading;
	private ImageView mImgLeft;
	private ImageView mImgRight;
	private TextView mTvAddress;
	private Button mBtnDone;

	private void init() {
		String url = "";
		if (getIntent().hasExtra(Consts.URL)) {
			url = getIntent().getStringExtra(Consts.URL);
		}

		mPbLoading = (ProgressBar) findViewById(R.id.pbLoading);
		mImgLeft = (ImageView) findViewById(R.id.imgLeft);
		mImgRight = (ImageView) findViewById(R.id.imgRight);
		mTvAddress = (TextView) findViewById(R.id.tvAddress);
		mBtnDone = (Button) findViewById(R.id.btnDone);
		mWebView = (WebView) findViewById(R.id.webView);

		mTvAddress.setText(url);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				super.onProgressChanged(view, newProgress);
				mPbLoading.setProgress(newProgress);
				if (newProgress < 100) {
					mPbLoading.setVisibility(View.VISIBLE);
				} else {
					mPbLoading.setVisibility(View.GONE);
				}
			}
		});
		mWebView.setWebViewClient(new WebViewClient());
		mWebView.loadUrl(url);

		mImgLeft.setOnClickListener(this);
		mImgRight.setOnClickListener(this);
		mBtnDone.setOnClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_web_view);

		init();
	}

	@Override
	public void onClick(View v) {
		if (v == mBtnDone) {
			finish();
		} else if (v == mImgLeft) {
			if (mWebView.canGoBack()) mWebView.goBack();
		} else if (v == mImgRight) {
			if (mWebView.canGoForward()) mWebView.goForward();
		}
	}
}
